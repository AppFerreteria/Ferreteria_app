package com.example.ferreteria_app

data class Producto(
    val id: String = "",
    val nombre: String = "",
    val precio: Double = 0.0,
    val imagenUrl: String = "",
    val categoria: String = "",
    val stock: Int = 0
)