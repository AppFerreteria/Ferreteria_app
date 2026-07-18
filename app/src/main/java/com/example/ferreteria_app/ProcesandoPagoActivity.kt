package com.example.ferreteria_app

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProcesandoPagoActivity : AppCompatActivity() {
    private val pasarela = PasarelaPagosRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_procesando_pago)
        procesarTransaccion()
    }

    private fun procesarTransaccion() {
        lifecycleScope.launch {
            cambiarEstado(EstadoTransaccion.VALIDANDO_DATOS)
            delay(700L)
            cambiarEstado(EstadoTransaccion.CONECTANDO_PASARELA)
            delay(700L)
            cambiarEstado(EstadoTransaccion.PROCESANDO_TRANSACCION)

            val total = CheckoutSession.carrito.total
            val metodo = CheckoutSession.metodoPago ?: MetodoPago.TARJETA
            
            // 1. Simular procesamiento de pasarela
            val resultado = pasarela.procesarPago(total, metodo)
            
            if (resultado.exitoso) {
                cambiarEstado(EstadoTransaccion.GENERANDO_COMPROBANTE)
                
                // 2. Intentar registrar pedido y actualizar stock en Firebase
                val exitoFirebase = registrarPedidoYActualizarStock(total)
                
                if (exitoFirebase) {
                    CheckoutSession.resultadoPago = resultado
                    CheckoutSession.comprobantePago = PagoLogic.crearComprobante(CheckoutSession.carrito, resultado)
                    cambiarEstado(EstadoTransaccion.APROBADO)
                } else {
                    // Si falla Firebase, revertimos el éxito de la pasarela para el usuario
                    CheckoutSession.resultadoPago = PagoLogic.crearResultadoRechazado(total, metodo, "ERR_FIREBASE_SYNC")
                    cambiarEstado(EstadoTransaccion.RECHAZADO)
                }
            } else {
                CheckoutSession.resultadoPago = resultado
                cambiarEstado(EstadoTransaccion.RECHAZADO)
            }

            delay(700L)
            startActivity(Intent(this@ProcesandoPagoActivity, ResultadoPagoActivity::class.java))
            finish()
        }
    }

    private fun cambiarEstado(estado: EstadoTransaccion) {
        CheckoutSession.estadoTransaccion = estado
        findViewById<TextView>(R.id.tvPasoValidando).text = paso("Validando datos", estado, EstadoTransaccion.VALIDANDO_DATOS)
        findViewById<TextView>(R.id.tvPasoPasarela).text = paso("Conectando con pasarela de pago", estado, EstadoTransaccion.CONECTANDO_PASARELA)
        findViewById<TextView>(R.id.tvPasoProcesando).text = paso("Procesando transacción", estado, EstadoTransaccion.PROCESANDO_TRANSACCION)
        findViewById<TextView>(R.id.tvPasoComprobante).text = paso("Generando comprobante", estado, EstadoTransaccion.GENERANDO_COMPROBANTE)
    }

    private suspend fun registrarPedidoYActualizarStock(total: Double): Boolean {
        return try {
            val db = FirebaseFirestore.getInstance()
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return false
            val batch = db.batch()

            // 1. Crear documento de Pedido
            val pedidoRef = db.collection("pedidos").document()
            val numeroPedido = PagoLogic.generarNumeroPedido()
            val descripcion = CheckoutSession.carrito.items.joinToString(", ") { "${it.cantidad}x ${it.nombre}" }
            
            // Log de depuración para verificar el ID antes de guardar
            val repartidorAsignado = "n1wV4YSLvLOIY37XgbyyuSedjNn1"
            android.util.Log.d("PedidoAsignacion", "Asignando pedido a repartidorId: $repartidorAsignado")

            val nuevoPedido = Pedido(
                id = pedidoRef.id,
                clienteId = uid,
                repartidorId = repartidorAsignado,
                numeroPedido = numeroPedido,
                fecha = System.currentTimeMillis(),
                estado = EstadoPedido.PENDIENTE,
                descripcionItems = descripcion,
                total = total
            )
            batch.set(pedidoRef, nuevoPedido)

            // 2. Referencias del carrito
            val cartDocRef = db.collection("usuarios").document(uid).collection("carrito").document("actual")
            val itemsCollectionRef = cartDocRef.collection("items")

            // 3. Actualizar stock y preparar limpieza de items
            for (item in CheckoutSession.carrito.items) {
                if (item.idProducto.isNotEmpty()) {
                    val productoRef = db.collection("productos").document(item.idProducto)
                    batch.update(productoRef, "stock", FieldValue.increment(-item.cantidad.toLong()))
                    
                    val itemCarritoRef = itemsCollectionRef.document(item.idProducto)
                    batch.delete(itemCarritoRef)
                }
            }

            // 4. Borrar documento raíz del carrito
            batch.delete(cartDocRef)

            // 5. Ejecutar TODO y esperar éxito
            batch.commit().await()
            true
        } catch (e: Exception) {
            android.util.Log.e("FirebaseSync", "Error en la transacción de Firebase", e)
            false
        }
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
            actualIndex > pasoIndex -> "✓ $texto"
            actual == paso -> "● $texto"
            else -> "○ $texto"
        }
    }
}
