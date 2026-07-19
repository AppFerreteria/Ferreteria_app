package com.example.ferreteria_app

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class YapePagoActivity : AppCompatActivity() {

    private lateinit var viewModel: YapePagoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_yape_pago)

        viewModel = ViewModelProvider(this)[YapePagoViewModel::class.java]

        findViewById<TextView>(R.id.btnBackYape).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tvTotalYape).text = viewModel.total

        findViewById<MaterialButton>(R.id.btnConfirmarYape).setOnClickListener {
            CheckoutSession.metodoPago = MetodoPago.YAPE
            startActivity(Intent(this, ProcesandoPagoActivity::class.java))
        }

        viewModel.iniciarContador()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.tiempoRestante.collect { tiempo ->
                        findViewById<TextView>(R.id.tvCountdownYape).text = tiempo
                    }
                }
            }
        }
    }
}
