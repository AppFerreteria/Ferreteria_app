package com.example.ferreteria_app

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TarjetaFormData(
    val numero: String = "",
    val titular: String = "",
    val vencimiento: String = "",
    val cvv: String = "",
    val marca: String = "Tarjeta",
    val numeroPreview: String = ".... .... .... ....",
    val titularPreview: String = "TITULAR",
    val vencimientoPreview: String = "MM/AA",
    val esValido: Boolean = false
)

class TarjetaPagoViewModel : ViewModel() {

    private val _formData = MutableStateFlow(TarjetaFormData())
    val formData: StateFlow<TarjetaFormData> = _formData.asStateFlow()

    val totalPagar: String
        get() = "Pagar ${PagoLogic.formatearSoles(CheckoutSession.carrito.total)}"

    fun actualizarNumero(valor: String) {
        val formateado = PagoLogic.formatearNumeroTarjeta(valor)
        val marca = PagoLogic.detectarMarcaTarjeta(formateado)
        val preview = formateado.ifBlank { ".... .... .... ...." }
        _formData.value = _formData.value.copy(
            numero = formateado,
            marca = marca,
            numeroPreview = preview
        )
        actualizarValidez()
    }

    fun actualizarTitular(valor: String) {
        val preview = valor.uppercase().ifBlank { "TITULAR" }
        _formData.value = _formData.value.copy(titular = valor, titularPreview = preview)
        actualizarValidez()
    }

    fun actualizarVencimiento(valor: String) {
        val formateado = PagoLogic.formatearVencimiento(valor)
        val preview = formateado.ifBlank { "MM/AA" }
        _formData.value = _formData.value.copy(vencimiento = formateado, vencimientoPreview = preview)
        actualizarValidez()
    }

    fun actualizarCvv(valor: String) {
        _formData.value = _formData.value.copy(cvv = valor)
        actualizarValidez()
    }

    fun confirmarPago() {
        CheckoutSession.metodoPago = MetodoPago.TARJETA
    }

    private fun actualizarValidez() {
        val data = _formData.value
        val esValido = PagoLogic.validarTarjeta(
            data.numero,
            data.titular,
            data.vencimiento,
            data.cvv
        )
        _formData.value = data.copy(esValido = esValido)
    }
}
