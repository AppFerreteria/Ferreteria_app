package com.example.ferreteria_app

data class PagoResultado(
    val exitoso: Boolean,
    val numeroOperacion: String,
    val monto: Double,
    val metodo: MetodoPago,
    val fechaHora: Long,
    val estado: EstadoTransaccion,
    val codigoError: String? = null,
    val mensaje: String = ""
)
