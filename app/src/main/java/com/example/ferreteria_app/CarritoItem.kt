package com.example.ferreteria_app

data class CarritoItem(
    val idProducto: String = "",
    val nombre: String = "",
    val precio: Double = 0.0,
    val cantidad: Int = 1
)