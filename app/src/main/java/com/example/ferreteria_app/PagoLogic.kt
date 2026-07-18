package com.example.ferreteria_app

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.round

object PagoLogic {
    private val localePe = Locale("es", "PE")

    fun formatearSoles(monto: Double): String = "S/ %.2f".format(localePe, monto)

    fun formatearFecha(fechaHora: Long): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", localePe).format(Date(fechaHora))

    fun generarNumeroPedido(now: Long = System.currentTimeMillis()): String =
        "#" + now.toString().takeLast(6)

    fun generarNumeroOperacion(now: Long = System.currentTimeMillis()): String =
        "OP" + now.toString().takeLast(8)

    fun generarNumeroComprobante(serie: String = "B001", now: Long = System.currentTimeMillis()): String =
        "$serie-" + now.toString().takeLast(7)

    fun validarMonto(monto: Double): Boolean = monto > 0.0

    fun calcularValorVenta(totalConIgv: Double): Double = redondear(totalConIgv / 1.18)

    fun calcularIgv(totalConIgv: Double): Double = redondear(totalConIgv - calcularValorVenta(totalConIgv))

    fun detectarMarcaTarjeta(numero: String): String {
        val limpio = soloDigitos(numero)
        return when {
            limpio.startsWith("4") -> "Visa"
            limpio.startsWith("5") -> "Mastercard"
            limpio.startsWith("3") -> "American Express"
            limpio.isBlank() -> "Tarjeta"
            else -> "Tarjeta bancaria"
        }
    }

    fun validarTarjeta(numero: String, titular: String, vencimiento: String, cvv: String): Boolean {
        val digitos = soloDigitos(numero)
        val cvvLimpio = soloDigitos(cvv)
        val venceCorrecto = Regex("^(0[1-9]|1[0-2])/[0-9]{2}$").matches(vencimiento.trim())
        return digitos.length in 15..16 && titular.trim().length >= 5 && venceCorrecto && cvvLimpio.length in 3..4
    }

    fun formatearNumeroTarjeta(valor: String): String =
        soloDigitos(valor).take(16).chunked(4).joinToString(" ")

    fun formatearVencimiento(valor: String): String {
        val limpio = soloDigitos(valor).take(4)
        return if (limpio.length > 2) limpio.substring(0, 2) + "/" + limpio.substring(2) else limpio
    }

    fun crearResultadoAprobado(monto: Double, metodo: MetodoPago, now: Long = System.currentTimeMillis()): PagoResultado =
        PagoResultado(
            exitoso = true,
            numeroOperacion = generarNumeroOperacion(now),
            monto = monto,
            metodo = metodo,
            fechaHora = now,
            estado = EstadoTransaccion.APROBADO,
            mensaje = "Transacción aprobada por la pasarela de pagos"
        )

    fun crearResultadoRechazado(monto: Double, metodo: MetodoPago, codigoError: String, now: Long = System.currentTimeMillis()): PagoResultado =
        PagoResultado(
            exitoso = false,
            numeroOperacion = generarNumeroOperacion(now),
            monto = monto,
            metodo = metodo,
            fechaHora = now,
            estado = EstadoTransaccion.RECHAZADO,
            codigoError = codigoError,
            mensaje = "La pasarela rechazó la transacción"
        )

    fun crearComprobante(carrito: Carrito, resultado: PagoResultado): ComprobantePago {
        val tipo = CheckoutSession.tipoComprobante
        val esFactura = tipo == TipoComprobante.FACTURA
        val serie = if (esFactura) "F001" else "B001"
        val documentoCliente = if (esFactura) "RUC: ${CheckoutSession.rucFacturacion}" else "DNI: 00000000"

        return ComprobantePago(
            serie = serie,
            numero = generarNumeroComprobante(serie, resultado.fechaHora),
            empresa = "FerreMax S.A.C.",
            ruc = "20512345678",
            cliente = "Cliente FerreMax",
            documentoCliente = documentoCliente,
            direccion = "Av. Arequipa 2450, Lima",
            resultado = resultado,
            carrito = carrito,
            tipo = tipo,
            rucCliente = if (esFactura) CheckoutSession.rucFacturacion else ""
        )
    }

    fun textoComprobante(comprobante: ComprobantePago): String {
        val productos = comprobante.carrito.items.joinToString("\n") { item ->
            "${item.nombre} x${item.cantidad} - ${formatearSoles(item.precio * item.cantidad)}"
        }
        return """
            ${comprobante.empresa}
            RUC: ${comprobante.ruc}
            ${comprobante.tipo.etiqueta} ${comprobante.numero}
            Operación: ${comprobante.resultado.numeroOperacion}
            Fecha: ${formatearFecha(comprobante.resultado.fechaHora)}
            Método: ${comprobante.resultado.metodo.etiqueta}
            Estado: Aprobado
            Total pagado: ${formatearSoles(comprobante.resultado.monto)}

            Productos:
            $productos
        """.trimIndent()
    }

    private fun soloDigitos(valor: String): String = valor.filter { it.isDigit() }

    private fun redondear(valor: Double): Double = round(valor * 100.0) / 100.0
}