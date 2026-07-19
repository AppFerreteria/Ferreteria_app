package com.example.ferreteria_app

import androidx.lifecycle.ViewModel

class ResultadoPagoViewModel : ViewModel() {

    val resultado: PagoResultado?
        get() = CheckoutSession.resultadoPago

    val esExitoso: Boolean
        get() = resultado?.exitoso ?: false

    val existeResultado: Boolean
        get() = resultado != null

    fun formatearSoles(monto: Double): String = PagoLogic.formatearSoles(monto)

    fun formatearFecha(fechaHora: Long): String = PagoLogic.formatearFecha(fechaHora)
}
