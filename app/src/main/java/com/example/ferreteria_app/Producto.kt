package com.example.ferreteria_app

data class Producto(
    val id: String = "",
    val nombre: String = "",
    val categoria: String = "",
    val precio: Double = 0.0,
    val imagenUrl: String = ""
)