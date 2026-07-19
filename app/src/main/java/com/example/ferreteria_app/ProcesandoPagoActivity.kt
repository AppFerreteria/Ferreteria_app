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
import kotlinx.coroutines.launch

class ProcesandoPagoActivity : AppCompatActivity() {

    private lateinit var viewModel: ProcesandoPagoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_procesando_pago)

        viewModel = ViewModelProvider(this)[ProcesandoPagoViewModel::class.java]

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {}
                        is UiState.Success -> {
                            val data = state.data
                            findViewById<TextView>(R.id.tvPasoValidando).text = data.pasoValidando
                            findViewById<TextView>(R.id.tvPasoPasarela).text = data.pasoPasarela
                            findViewById<TextView>(R.id.tvPasoProcesando).text = data.pasoProcesando
                            findViewById<TextView>(R.id.tvPasoComprobante).text = data.pasoComprobante

                            if (data.estadoActual == EstadoTransaccion.APROBADO ||
                                data.estadoActual == EstadoTransaccion.RECHAZADO
                            ) {
                                startActivity(
                                    Intent(this@ProcesandoPagoActivity, ResultadoPagoActivity::class.java)
                                )
                                finish()
                            }
                        }
                        is UiState.Error -> {
                            CheckoutSession.resultadoPago = PagoLogic.crearResultadoRechazado(
                                CheckoutSession.carrito.total,
                                CheckoutSession.metodoPago ?: MetodoPago.TARJETA,
                                "ERR_SISTEMA"
                            )
                            startActivity(
                                Intent(this@ProcesandoPagoActivity, ResultadoPagoActivity::class.java)
                            )
                            finish()
                        }
                    }
                }
            }
        }

        viewModel.procesarTransaccion()
    }
}
