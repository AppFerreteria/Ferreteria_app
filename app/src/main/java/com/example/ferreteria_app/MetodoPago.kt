package com.example.ferreteria_app

enum class MetodoPago(val etiqueta: String) {
    YAPE("Yape"),
    TARJETA("Tarjeta de crédito / débito"),
    TRANSFERENCIA("Transferencia bancaria")
}
