package com.example.ferreteria_app

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class ResultadoPagoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_resultado_pago)

        val resultado = CheckoutSession.resultadoPago
        if (resultado == null) {
            finish()
            return
        }
        pintarResultado(resultado)
    }

    private fun pintarResultado(resultado: PagoResultado) {
        val btn = findViewById<MaterialButton>(R.id.btnAccionResultado)
        findViewById<TextView>(R.id.tvMontoResultado).text = PagoLogic.formatearSoles(resultado.monto)
        findViewById<TextView>(R.id.tvOperacionResultado).text = "Número de operación: ${resultado.numeroOperacion}"
        findViewById<TextView>(R.id.tvMetodoResultado).text = "Método de pago: ${resultado.metodo.etiqueta}"
        findViewById<TextView>(R.id.tvFechaResultado).text = "Fecha y hora: ${PagoLogic.formatearFecha(resultado.fechaHora)}"

        if (resultado.exitoso) {
            findViewById<TextView>(R.id.tvIconResultado).text = "✓"
            findViewById<TextView>(R.id.tvResultadoTitulo).text = "¡Pago exitoso!"
            findViewById<TextView>(R.id.tvResultadoSubtitulo).text = "Tu pedido ha sido confirmado"
            findViewById<TextView>(R.id.tvEstadoResultado).text = "Estado: Aprobado"
            btn.text = "Ver comprobante digital"
            btn.setOnClickListener { startActivity(Intent(this, ComprobanteActivity::class.java)) }
        } else {
            findViewById<TextView>(R.id.tvIconResultado).text = "✕"
            findViewById<TextView>(R.id.tvResultadoTitulo).text = "Pago fallido"
            findViewById<TextView>(R.id.tvResultadoSubtitulo).text = "No pudimos confirmar la transacción"
            findViewById<TextView>(R.id.tvEstadoResultado).text = "Estado: Rechazado (${resultado.codigoError ?: "ERR_GENERAL"})"
            btn.text = "Volver a intentar"
            btn.setOnClickListener {
                CheckoutSession.metodoPago = null
                startActivity(Intent(this, MetodoPagoActivity::class.java))
                finish()
            }
        }
    }
}
