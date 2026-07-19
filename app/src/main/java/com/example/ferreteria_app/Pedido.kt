package com.example.ferreteria_app

data class Pedido(
    val id: String = "",
    val clienteId: String = "",
    val repartidorId: String = "",
    val numeroPedido: String = "",
    val fecha: Long = System.currentTimeMillis(),
    val estado: String = EstadoPedido.PENDIENTE,
    val subtotal: Double = 0.0,
    val descuento: Double = 0.0,
    val costoEnvio: Double = 0.0,
    val total: Double = 0.0,
    val direccionEntrega: String = "",
    val items: List<CarritoItem> = emptyList(),
    val descripcionItems: String = ""
)

object EstadoPedido {
    const val PENDIENTE = "pendiente"
    const val PREPARACION = "preparacion"
    const val EN_CAMINO = "en_camino"
    const val ENTREGADO = "entregado"
}