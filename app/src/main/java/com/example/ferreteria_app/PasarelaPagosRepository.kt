package com.example.ferreteria_app

import kotlinx.coroutines.delay
import kotlin.random.Random

class PasarelaPagosRepository {

    suspend fun procesarPago(monto: Double, metodo: MetodoPago): PagoResultado {
        val now = System.currentTimeMillis()

        if (!PagoLogic.validarMonto(monto)) {
            delay(300L)
            return PagoLogic.crearResultadoRechazado(monto, metodo, "ERR_MONTO_INVALIDO", now)
        }

        delay(1500L)

        val escenario = Random.nextDouble()

        return when {
            escenario < 0.70 -> {
                val resultado = PagoLogic.crearResultadoAprobado(monto, metodo, now)
                resultado.copy(mensaje = "Transaccion aprobada por ${metodo.etiqueta}")
            }
            escenario < 0.85 -> {
                delay(3000L)
                PagoLogic.crearResultadoRechazado(monto, metodo, "ERR_TIEMPO_EXCEDIDO", now)
                    .copy(mensaje = "La pasarela de pagos no respondio a tiempo")
            }
            escenario < 0.95 -> {
                PagoLogic.crearResultadoRechazado(monto, metodo, "ERR_FONDOS_INSUFICIENTES", now)
                    .copy(mensaje = "Fondos insuficientes para completar la transaccion")
            }
            else -> {
                PagoLogic.crearResultadoRechazado(monto, metodo, "ERR_PASARELA_NO_DISPONIBLE", now)
                    .copy(mensaje = "La pasarela de pagos no esta disponible en este momento")
            }
        }
    }
}
