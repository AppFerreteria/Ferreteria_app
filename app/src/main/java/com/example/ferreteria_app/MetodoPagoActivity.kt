package com.example.ferreteria_app

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MetodoPagoActivity : AppCompatActivity() {
    private var metodoSeleccionado: MetodoPago? = null

    private lateinit var cardYape: LinearLayout
    private lateinit var cardTarjeta: LinearLayout
    private lateinit var cardTransferencia: LinearLayout
    private lateinit var radioYape: RadioButton
    private lateinit var radioTarjeta: RadioButton
    private lateinit var radioTransferencia: RadioButton
    private lateinit var btnContinuar: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_metodo_pago)

        findViewById<TextView>(R.id.tvTotalMetodoPago).text = PagoLogic.formatearSoles(CheckoutSession.carrito.total)
        findViewById<TextView>(R.id.btnBackMetodoPago).setOnClickListener { finish() }

        cardYape = findViewById(R.id.cardYape)
        cardTarjeta = findViewById(R.id.cardTarjeta)
        cardTransferencia = findViewById(R.id.cardTransferencia)
        radioYape = findViewById(R.id.radioYape)
        radioTarjeta = findViewById(R.id.radioTarjeta)
        radioTransferencia = findViewById(R.id.radioTransferencia)
        btnContinuar = findViewById(R.id.btnContinuarMetodoPago)

        cardYape.setOnClickListener { seleccionar(MetodoPago.YAPE) }
        cardTarjeta.setOnClickListener { seleccionar(MetodoPago.TARJETA) }
        cardTransferencia.setOnClickListener { seleccionar(MetodoPago.TRANSFERENCIA) }
        radioYape.setOnClickListener { seleccionar(MetodoPago.YAPE) }
        radioTarjeta.setOnClickListener { seleccionar(MetodoPago.TARJETA) }
        radioTransferencia.setOnClickListener { seleccionar(MetodoPago.TRANSFERENCIA) }

        btnContinuar.setOnClickListener {
            CheckoutSession.metodoPago = metodoSeleccionado
            when (metodoSeleccionado) {
                MetodoPago.YAPE -> startActivity(Intent(this, YapePagoActivity::class.java))
                MetodoPago.TARJETA -> startActivity(Intent(this, TarjetaPagoActivity::class.java))
                MetodoPago.TRANSFERENCIA -> startActivity(Intent(this, ProcesandoPagoActivity::class.java))
                null -> Unit
            }
        }
    }

    private fun seleccionar(metodo: MetodoPago) {
        metodoSeleccionado = metodo
        btnContinuar.isEnabled = true
        radioYape.isChecked = metodo == MetodoPago.YAPE
        radioTarjeta.isChecked = metodo == MetodoPago.TARJETA
        radioTransferencia.isChecked = metodo == MetodoPago.TRANSFERENCIA
        cardYape.setBackgroundResource(if (metodo == MetodoPago.YAPE) R.drawable.bg_method_selected else R.drawable.bg_method_normal)
        cardTarjeta.setBackgroundResource(if (metodo == MetodoPago.TARJETA) R.drawable.bg_method_selected else R.drawable.bg_method_normal)
        cardTransferencia.setBackgroundResource(if (metodo == MetodoPago.TRANSFERENCIA) R.drawable.bg_method_selected else R.drawable.bg_method_normal)
    }
}
