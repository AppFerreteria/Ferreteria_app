package com.example.ferreteria_app

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

            val metodo = CheckoutSession.metodoPago ?: MetodoPago.TARJETA
            val resultado = pasarela.procesarPago(CheckoutSession.carrito.total, metodo)
            CheckoutSession.resultadoPago = resultado

            cambiarEstado(EstadoTransaccion.GENERANDO_COMPROBANTE)
            delay(700L)

            if (resultado.exitoso) {
                // Actualizar stock en Firebase antes de finalizar
                actualizarStockProductos(CheckoutSession.carrito)

                CheckoutSession.comprobantePago = PagoLogic.crearComprobante(CheckoutSession.carrito, resultado)
                cambiarEstado(EstadoTransaccion.APROBADO)
            } else {
                cambiarEstado(EstadoTransaccion.RECHAZADO)
            }

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

    private fun actualizarStockProductos(carrito: Carrito) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val batch = db.batch()

        for (item in carrito.items) {
            val productoRef = db.collection("productos").document(item.idProducto)
            
            // Usamos una operación de decremento atómico de Firestore
            batch.update(productoRef, "stock", com.google.firebase.firestore.FieldValue.increment(-item.cantidad.toLong()))
        }

        // También vaciamos el carrito en la nube al completar la compra
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val carritoRef = db.collection("usuarios").document(uid).collection("carrito").document("actual")
            batch.delete(carritoRef)
            
            // Nota: En una app real también deberías borrar la sub-colección 'items', 
            // pero para esta prueba el decremento de stock es lo principal.
        }

        batch.commit().addOnFailureListener { e ->
            android.util.Log.e("StockUpdate", "Error al actualizar stock", e)
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
