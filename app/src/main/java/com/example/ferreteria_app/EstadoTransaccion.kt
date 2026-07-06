package com.example.ferreteria_app

enum class EstadoTransaccion {
    PENDIENTE,
    VALIDANDO_DATOS,
    CONECTANDO_PASARELA,
    PROCESANDO_TRANSACCION,
    GENERANDO_COMPROBANTE,
    APROBADO,
    RECHAZADO
}
