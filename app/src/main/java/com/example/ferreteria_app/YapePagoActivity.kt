package com.example.ferreteria_app

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class YapePagoActivity : AppCompatActivity() {
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_yape_pago)

        findViewById<TextView>(R.id.btnBackYape).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tvTotalYape).text = PagoLogic.formatearSoles(CheckoutSession.carrito.total)
        findViewById<MaterialButton>(R.id.btnConfirmarYape).setOnClickListener {
            CheckoutSession.metodoPago = MetodoPago.YAPE
            startActivity(Intent(this, ProcesandoPagoActivity::class.java))
        }
        iniciarContador()
    }

    private fun iniciarContador() {
        val tvCountdown = findViewById<TextView>(R.id.tvCountdownYape)
        timer = object : CountDownTimer(300_000L, 1_000L) {
            override fun onTick(millisUntilFinished: Long) {
                val totalSegundos = millisUntilFinished / 1000
                val minutos = totalSegundos / 60
                val segundos = totalSegundos % 60
                tvCountdown.text = "Expira en %02d:%02d".format(minutos, segundos)
            }

            override fun onFinish() {
                tvCountdown.text = "QR expirado"
            }
        }.start()
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
}
