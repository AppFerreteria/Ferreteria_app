package com.example.ferreteria_app

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ProcesamientoData(
    val estadoActual: EstadoTransaccion = EstadoTransaccion.VALIDANDO_DATOS,
    val pasoValidando: String = "o Validando datos",
    val pasoPasarela: String = "o Conectando con pasarela de pago",
    val pasoProcesando: String = "o Procesando transaccion",
    val pasoComprobante: String = "o Generando comprobante",
    val resultado: PagoResultado? = null,
    val error: String? = null
)

class ProcesandoPagoViewModel : ViewModel() {

    private val pasarela = PasarelaPagosRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<UiState<ProcesamientoData>>(UiState.Loading)
    val uiState: StateFlow<UiState<ProcesamientoData>> = _uiState.asStateFlow()

    fun procesarTransaccion() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Success(
                    ProcesamientoData(estadoActual = EstadoTransaccion.VALIDANDO_DATOS)
                )
                delay(700L)
                actualizarEstado(EstadoTransaccion.CONECTANDO_PASARELA)
                delay(700L)
                actualizarEstado(EstadoTransaccion.PROCESANDO_TRANSACCION)

                val metodo = CheckoutSession.metodoPago ?: MetodoPago.TARJETA
                val resultado = pasarela.procesarPago(CheckoutSession.carrito.total, metodo)
                CheckoutSession.resultadoPago = resultado

                actualizarEstado(EstadoTransaccion.GENERANDO_COMPROBANTE)
                delay(700L)

                if (resultado.exitoso) {
                    guardarPedidoEnFirebase(CheckoutSession.carrito)
                    actualizarStockProductos(CheckoutSession.carrito)
                    CheckoutSession.comprobantePago = PagoLogic.crearComprobante(
                        CheckoutSession.carrito, resultado
                    )
                    val data = (_uiState.value as UiState.Success).data
                    _uiState.value = UiState.Success(
                        data.copy(estadoActual = EstadoTransaccion.APROBADO, resultado = resultado)
                    )
                } else {
                    val data = (_uiState.value as UiState.Success).data
                    _uiState.value = UiState.Success(
                        data.copy(estadoActual = EstadoTransaccion.RECHAZADO, resultado = resultado)
                    )
                }
            } catch (e: Exception) {
                Log.e("ProcesandoPago", "Error en transaccion", e)
                _uiState.value = UiState.Error("Error al procesar el pago: ${e.message}")
            }
        }
    }

    private fun actualizarEstado(estado: EstadoTransaccion) {
        val current = _uiState.value
        if (current !is UiState.Success) return
        _uiState.value = UiState.Success(
            current.data.copy(
                estadoActual = estado,
                pasoValidando = paso("Validando datos", estado, EstadoTransaccion.VALIDANDO_DATOS),
                pasoPasarela = paso("Conectando con pasarela de pago", estado, EstadoTransaccion.CONECTANDO_PASARELA),
                pasoProcesando = paso("Procesando transaccion", estado, EstadoTransaccion.PROCESANDO_TRANSACCION),
                pasoComprobante = paso("Generando comprobante", estado, EstadoTransaccion.GENERANDO_COMPROBANTE)
            )
        )
    }

    private fun paso(texto: String, actual: EstadoTransaccion, paso: EstadoTransaccion): String {
        val orden = listOf(
            EstadoTransaccion.VALIDANDO_DATOS,
            EstadoTransaccion.CONECTANDO_PASARELA,
            EstadoTransaccion.PROCESANDO_TRANSACCION,
            EstadoTransaccion.GENERANDO_COMPROBANTE,
            EstadoTransaccion.APROBADO,
            EstadoTransaccion.RECHAZADO
        )
        val actualIndex = orden.indexOf(actual)
        val pasoIndex = orden.indexOf(paso)
        return when {
            actualIndex > pasoIndex -> "v $texto"
            actual == paso -> "o $texto"
            else -> "o $texto"
        }
    }

    private suspend fun actualizarStockProductos(carrito: Carrito) {
        try {
            val batch = db.batch()
            for (item in carrito.items) {
                val productoRef = db.collection("productos").document(item.idProducto)
                batch.update(productoRef, "stock", FieldValue.increment(-item.cantidad.toLong()))
            }
            val uid = auth.currentUser?.uid
            if (uid != null) {
                val carritoRef = db.collection("usuarios").document(uid)
                    .collection("carrito").document("actual")
                batch.delete(carritoRef)
            }
            batch.commit().await()
        } catch (e: Exception) {
            Log.e("StockUpdate", "Error al actualizar stock", e)
        }
    }

    private suspend fun guardarPedidoEnFirebase(carrito: Carrito) {
        try {
            val uid = auth.currentUser?.uid ?: return
            val pedidoRef = db.collection("pedidos").document()
            val numeroPedido = CheckoutSession.numeroPedido
            val descripcionItems = carrito.items.joinToString(separator = ", ") {
                "${it.cantidad}x ${it.nombre}"
            }
            val pedido = Pedido(
                id = pedidoRef.id,
                clienteId = uid,
                numeroPedido = numeroPedido,
                fecha = System.currentTimeMillis(),
                estado = EstadoPedido.PENDIENTE,
                subtotal = carrito.subtotal,
                descuento = carrito.descuento,
                costoEnvio = carrito.costoEnvio,
                total = carrito.total,
                direccionEntrega = "Direccion de entrega",
                items = carrito.items,
                descripcionItems = descripcionItems
            )
            pedidoRef.set(pedido).await()
        } catch (e: Exception) {
            Log.e("PedidoGuardado", "Error al guardar el pedido", e)
        }
    }
}
