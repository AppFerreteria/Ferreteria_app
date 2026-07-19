package com.example.ferreteria_app

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class YapePagoViewModel : ViewModel() {

    private val _tiempoRestante = MutableStateFlow("Expira en 05:00")
    val tiempoRestante: StateFlow<String> = _tiempoRestante.asStateFlow()

    private val _expirado = MutableStateFlow(false)
    val expirado: StateFlow<Boolean> = _expirado.asStateFlow()

    private var timer: CountDownTimer? = null

    val total: String
        get() = PagoLogic.formatearSoles(CheckoutSession.carrito.total)

    fun iniciarContador() {
        timer?.cancel()
        timer = object : CountDownTimer(300_000L, 1_000L) {
            override fun onTick(millisUntilFinished: Long) {
                val totalSegundos = millisUntilFinished / 1000
                val minutos = totalSegundos / 60
                val segundos = totalSegundos % 60
                _tiempoRestante.value = "Expira en %02d:%02d".format(minutos, segundos)
            }

            override fun onFinish() {
                _tiempoRestante.value = "QR expirado"
                _expirado.value = true
            }
        }.start()
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}
