package com.example.ferreteria_app

data class Carrito(
    val id: String = "",
    val uid: String = "",
    val fechaCreacion: Long = System.currentTimeMillis(),
    val estado: EstadoCarrito = EstadoCarrito.ACTIVO,
    val subtotal: Double = 0.0,
    val descuento: Double = 0.0,
    val costoEnvio: Double = 0.0,
    val total: Double = 0.0,
    val items: List<CarritoItem> = emptyList()
)
