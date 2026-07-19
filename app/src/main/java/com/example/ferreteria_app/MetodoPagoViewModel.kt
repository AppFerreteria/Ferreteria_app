package com.example.ferreteria_app

import androidx.lifecycle.ViewModel

class MetodoPagoViewModel : ViewModel() {

    val total: String
        get() = PagoLogic.formatearSoles(CheckoutSession.carrito.total)

    fun seleccionarMetodo(metodo: MetodoPago) {
        CheckoutSession.metodoPago = metodo
    }

    fun getMetodoSeleccionado(): MetodoPago? = CheckoutSession.metodoPago
}
