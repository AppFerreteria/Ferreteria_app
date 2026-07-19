package com.example.ferreteria_app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificacionFlujoTest {

    private fun crearPedido(id: String, estado: String): Pedido = Pedido(
        id = id,
        numeroPedido = "#$id",
        estado = estado
    )

    @Test
    fun flujoCompleto_desdePendienteHastaEntregado_reflejaCambiosEnTiempoReal() {
        val viewModel = PedidoViewModel()

        viewModel.cargarPedidosDePrueba(
            listOf(crearPedido("1", EstadoPedido.PENDIENTE))
        )

        var data = viewModel.pedidosData.value
        assertEquals(1, data.pedidosFiltrados.size)
        assertEquals(EstadoPedido.PENDIENTE, data.pedidosFiltrados[0].estado)

        viewModel.cargarPedidosDePrueba(
            listOf(crearPedido("1", EstadoPedido.PREPARACION))
        )

        data = viewModel.pedidosData.value
        assertEquals(1, data.pedidosFiltrados.size)
        assertEquals(EstadoPedido.PREPARACION, data.pedidosFiltrados[0].estado)

        viewModel.cargarPedidosDePrueba(
            listOf(crearPedido("1", EstadoPedido.EN_CAMINO))
        )

        viewModel.setFiltro("EN_CAMINO")
        data = viewModel.pedidosData.value
        assertEquals(1, data.pedidosFiltrados.size)
        assertEquals(EstadoPedido.EN_CAMINO, data.pedidosFiltrados[0].estado)
        assertEquals("1", data.pedidosFiltrados[0].id)

        viewModel.cargarPedidosDePrueba(
            listOf(crearPedido("1", EstadoPedido.ENTREGADO))
        )

        viewModel.setFiltro("ENTREGADOS")
        data = viewModel.pedidosData.value
        assertEquals(1, data.pedidosFiltrados.size)
        assertEquals(EstadoPedido.ENTREGADO, data.pedidosFiltrados[0].estado)
    }

    @Test
    fun notificacionEnCamino_pedidoApareceEnPestanaCorrecta_conIdParaNavegacion() {
        val viewModel = PedidoViewModel()
        val pedidoId = "pedido-123"
        val numeroPedido = "#NN-001"

        viewModel.cargarPedidosDePrueba(
            listOf(Pedido(id = pedidoId, numeroPedido = numeroPedido, estado = EstadoPedido.EN_CAMINO))
        )

        viewModel.setFiltro("EN_CAMINO")

        val data = viewModel.pedidosData.value
        assertEquals(1, data.pedidosFiltrados.size)

        val pedido = data.pedidosFiltrados[0]
        assertEquals(pedidoId, pedido.id)
        assertEquals(numeroPedido, pedido.numeroPedido)
        assertEquals(EstadoPedido.EN_CAMINO, pedido.estado)
        assertTrue(pedido.estado != EstadoPedido.ENTREGADO)
    }

    @Test
    fun notificacionEntregado_pedidoDesapareceDeActivos_yApareceEnEntregados() {
        val viewModel = PedidoViewModel()

        viewModel.cargarPedidosDePrueba(
            listOf(
                crearPedido("x", EstadoPedido.EN_CAMINO),
                crearPedido("y", EstadoPedido.ENTREGADO)
            )
        )

        viewModel.setFiltro("ACTIVOS")
        var data = viewModel.pedidosData.value
        assertEquals(1, data.pedidosFiltrados.size)
        assertEquals(EstadoPedido.EN_CAMINO, data.pedidosFiltrados[0].estado)

        viewModel.cargarPedidosDePrueba(
            listOf(
                crearPedido("x", EstadoPedido.ENTREGADO),
                crearPedido("y", EstadoPedido.ENTREGADO)
            )
        )

        viewModel.setFiltro("ACTIVOS")
        data = viewModel.pedidosData.value
        assertEquals(0, data.pedidosFiltrados.size)

        viewModel.setFiltro("ENTREGADOS")
        data = viewModel.pedidosData.value
        assertEquals(2, data.pedidosFiltrados.size)
        assertTrue(data.pedidosFiltrados.all { it.estado == EstadoPedido.ENTREGADO })
    }

    @Test
    fun multiplesActualizaciones_soloPedidoAfectadoCambiaDePestana() {
        val viewModel = PedidoViewModel()

        viewModel.cargarPedidosDePrueba(
            listOf(
                crearPedido("a", EstadoPedido.PENDIENTE),
                crearPedido("b", EstadoPedido.PREPARACION),
                crearPedido("c", EstadoPedido.EN_CAMINO),
                crearPedido("d", EstadoPedido.ENTREGADO)
            )
        )

        viewModel.setFiltro("ACTIVOS")
        var data = viewModel.pedidosData.value
        assertEquals(3, data.pedidosFiltrados.size)
        assertEquals(3, data.pedidosFiltrados.count { it.estado != EstadoPedido.ENTREGADO })

        viewModel.cargarPedidosDePrueba(
            listOf(
                crearPedido("a", EstadoPedido.PENDIENTE),
                crearPedido("b", EstadoPedido.EN_CAMINO),
                crearPedido("c", EstadoPedido.EN_CAMINO),
                crearPedido("d", EstadoPedido.ENTREGADO)
            )
        )

        viewModel.setFiltro("PREPARACION")
        data = viewModel.pedidosData.value
        assertEquals(0, data.pedidosFiltrados.size)

        viewModel.setFiltro("EN_CAMINO")
        data = viewModel.pedidosData.value
        assertEquals(2, data.pedidosFiltrados.size)
        assertTrue(data.pedidosFiltrados.all { it.estado == EstadoPedido.EN_CAMINO })
    }
}
