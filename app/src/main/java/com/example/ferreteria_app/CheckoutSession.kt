package com.example.ferreteria_app

object CheckoutSession {
    var userId: String? = null

    var carrito: Carrito = Carrito()
        private set

    var numeroPedido: String = PagoLogic.generarNumeroPedido()
        private set

    var metodoPago: MetodoPago? = null
    var estadoTransaccion: EstadoTransaccion = EstadoTransaccion.PENDIENTE
    var resultadoPago: PagoResultado? = null
    var comprobantePago: ComprobantePago? = null

    fun iniciar(carritoActual: Carrito) {
        carrito = carritoActual
        numeroPedido = PagoLogic.generarNumeroPedido()
        metodoPago = null
        estadoTransaccion = EstadoTransaccion.PENDIENTE
        resultadoPago = null
        comprobantePago = null
    }

    fun resetear() {
        carrito = Carrito()
        numeroPedido = PagoLogic.generarNumeroPedido()
        metodoPago = null
        estadoTransaccion = EstadoTransaccion.PENDIENTE
        resultadoPago = null
        comprobantePago = null
    }
}
