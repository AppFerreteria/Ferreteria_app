package com.example.ferreteria_app

import androidx.lifecycle.ViewModel

class ComprobanteViewModel : ViewModel() {

    val comprobante: ComprobantePago?
        get() = CheckoutSession.comprobantePago

    val existeComprobante: Boolean
        get() = comprobante != null

    fun formatearSoles(monto: Double): String = PagoLogic.formatearSoles(monto)

    fun formatearFecha(fechaHora: Long): String = PagoLogic.formatearFecha(fechaHora)

    fun calcularValorVenta(total: Double): Double = PagoLogic.calcularValorVenta(total)

    fun calcularIgv(total: Double): Double = PagoLogic.calcularIgv(total)

    fun textoComprobante(comprobante: ComprobantePago): String =
        PagoLogic.textoComprobante(comprobante)

    fun resetearCheckout() {
        CheckoutSession.resetear()
    }
}
