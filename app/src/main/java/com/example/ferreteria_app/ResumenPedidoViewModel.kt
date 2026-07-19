package com.example.ferreteria_app

import androidx.lifecycle.ViewModel

class ResumenPedidoViewModel : ViewModel() {

    val carrito: Carrito
        get() = CheckoutSession.carrito

    val numeroPedido: String
        get() = CheckoutSession.numeroPedido

    fun estaVacio(): Boolean = carrito.items.isEmpty()

    fun formatearSoles(monto: Double): String = PagoLogic.formatearSoles(monto)

    fun calcularValorVenta(total: Double): Double = PagoLogic.calcularValorVenta(total)

    fun calcularIgv(total: Double): Double = PagoLogic.calcularIgv(total)
}
