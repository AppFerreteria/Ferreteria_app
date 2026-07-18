package com.example.ferreteria_app

data class ComprobantePago(
    val serie: String,
    val numero: String,
    val empresa: String,
    val ruc: String,
    val cliente: String,
    val documentoCliente: String,
    val direccion: String,
    val resultado: PagoResultado,
    val carrito: Carrito,
    val tipo: TipoComprobante = TipoComprobante.BOLETA,
    val rucCliente: String = ""
)