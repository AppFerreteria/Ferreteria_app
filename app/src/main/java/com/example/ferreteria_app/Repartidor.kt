package com.example.ferreteria_app

import com.google.firebase.firestore.GeoPoint

data class Repartidor(
    val id: String = "",
    val codigo: String = "",
    val nombre: String = "",
    val telefono: String = "",
    val ubicacionActual: GeoPoint? = null
)