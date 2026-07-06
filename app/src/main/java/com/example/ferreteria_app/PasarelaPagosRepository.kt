package com.example.ferreteria_app

import kotlinx.coroutines.delay

class PasarelaPagosRepository {
    suspend fun procesarPago(monto: Double, metodo: MetodoPago): PagoResultado {
        delay(900L)
        return if (PagoLogic.validarMonto(monto)) {
            PagoLogic.crearResultadoAprobado(monto, metodo)
        } else {
            PagoLogic.crearResultadoRechazado(monto, metodo, "ERR_MONTO_INVALIDO")
        }
    }
}
