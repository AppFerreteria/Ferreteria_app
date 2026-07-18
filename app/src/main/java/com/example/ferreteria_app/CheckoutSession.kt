package com.example.ferreteria_app

object CheckoutSession {
    var carrito: Carrito = Carrito()
        private set

    var numeroPedido: String = PagoLogic.generarNumeroPedido()
        private set

    var metodoPago: MetodoPago? = null
    var estadoTransaccion: EstadoTransaccion = EstadoTransaccion.PENDIENTE
    var resultadoPago: PagoResultado? = null
    var comprobantePago: ComprobantePago? = null

    var tipoComprobante: TipoComprobante = TipoComprobante.BOLETA
    var rucFacturacion: String = ""

    fun iniciar(carritoActual: Carrito) {
        carrito = carritoActual
        numeroPedido = PagoLogic.generarNumeroPedido()
        metodoPago = null
        estadoTransaccion = EstadoTransaccion.PENDIENTE
        resultadoPago = null
        comprobantePago = null
        tipoComprobante = TipoComprobante.BOLETA
        rucFacturacion = ""
    }

    fun resetear() {
        carrito = Carrito()
        numeroPedido = PagoLogic.generarNumeroPedido()
        metodoPago = null
        estadoTransaccion = EstadoTransaccion.PENDIENTE
        resultadoPago = null
        comprobantePago = null
        tipoComprobante = TipoComprobante.BOLETA
        rucFacturacion = ""
    }
}