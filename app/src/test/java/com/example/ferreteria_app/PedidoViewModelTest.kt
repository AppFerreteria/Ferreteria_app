package com.example.ferreteria_app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PedidoViewModelTest {

    private fun crearPedido(id: String, estado: String): Pedido = Pedido(
        id = id,
        numeroPedido = "#$id",
        estado = estado
    )

    @Test
    fun filtroActivos_muestraSoloNoEntregados() {
        val viewModel = PedidoViewModel()
        val pedidos = listOf(
            crearPedido("1", EstadoPedido.PENDIENTE),
            crearPedido("2", EstadoPedido.PREPARACION),
            crearPedido("3", EstadoPedido.EN_CAMINO),
            crearPedido("4", EstadoPedido.ENTREGADO)
        )
        viewModel.cargarPedidosDePrueba(pedidos)

        val data = viewModel.pedidosData.value
        assertEquals(3, data.pedidosFiltrados.size)
        assertTrue(data.pedidosFiltrados.all { it.estado != EstadoPedido.ENTREGADO })
    }

    @Test
    fun filtroPreparando_muestraSoloPreparacion() {
        val viewModel = PedidoViewModel()
        val pedidos = listOf(
            crearPedido("1", EstadoPedido.PENDIENTE),
            crearPedido("2", EstadoPedido.PREPARACION),
            crearPedido("3", EstadoPedido.EN_CAMINO),
            crearPedido("4", EstadoPedido.ENTREGADO)
        )
        viewModel.setFiltro("PREPARACION")
        viewModel.cargarPedidosDePrueba(pedidos)

        val data = viewModel.pedidosData.value
        assertEquals(1, data.pedidosFiltrados.size)
        assertEquals(EstadoPedido.PREPARACION, data.pedidosFiltrados[0].estado)
    }

    @Test
    fun filtroEnCamino_muestraSoloEnCamino() {
        val viewModel = PedidoViewModel()
        val pedidos = listOf(
            crearPedido("1", EstadoPedido.PENDIENTE),
            crearPedido("2", EstadoPedido.EN_CAMINO),
            crearPedido("3", EstadoPedido.EN_CAMINO),
            crearPedido("4", EstadoPedido.ENTREGADO)
        )
        viewModel.setFiltro("EN_CAMINO")
        viewModel.cargarPedidosDePrueba(pedidos)

        val data = viewModel.pedidosData.value
        assertEquals(2, data.pedidosFiltrados.size)
        assertTrue(data.pedidosFiltrados.all { it.estado == EstadoPedido.EN_CAMINO })
    }

    @Test
    fun filtroEntregados_limitaATresMasRecientes() {
        val viewModel = PedidoViewModel()
        val pedidos = listOf(
            crearPedido("1", EstadoPedido.PENDIENTE),
            crearPedido("2", EstadoPedido.ENTREGADO),
            crearPedido("3", EstadoPedido.ENTREGADO),
            crearPedido("4", EstadoPedido.ENTREGADO),
            crearPedido("5", EstadoPedido.ENTREGADO)
        )
        viewModel.setFiltro("ENTREGADOS")
        viewModel.cargarPedidosDePrueba(pedidos)

        val data = viewModel.pedidosData.value
        assertEquals(3, data.pedidosFiltrados.size)
        assertTrue(data.pedidosFiltrados.all { it.estado == EstadoPedido.ENTREGADO })
        assertTrue(data.hayMasEntregados)
        assertEquals(4, data.totalEntregados)
    }

    @Test
    fun toggleHistorial_muestraTodosLosEntregados() {
        val viewModel = PedidoViewModel()
        val pedidos = listOf(
            crearPedido("1", EstadoPedido.ENTREGADO),
            crearPedido("2", EstadoPedido.ENTREGADO),
            crearPedido("3", EstadoPedido.ENTREGADO),
            crearPedido("4", EstadoPedido.ENTREGADO),
            crearPedido("5", EstadoPedido.ENTREGADO)
        )
        viewModel.setFiltro("ENTREGADOS")
        viewModel.cargarPedidosDePrueba(pedidos)

        val antesDeExpandir = viewModel.pedidosData.value
        assertEquals(3, antesDeExpandir.pedidosFiltrados.size)
        assertTrue(antesDeExpandir.hayMasEntregados)

        viewModel.toggleHistorial()

        val despuesDeExpandir = viewModel.pedidosData.value
        assertEquals(5, despuesDeExpandir.pedidosFiltrados.size)
        assertFalse(despuesDeExpandir.hayMasEntregados)
    }

    @Test
    fun cambioDeFiltro_reseteaHistorial() {
        val viewModel = PedidoViewModel()
        val pedidos = listOf(
            crearPedido("1", EstadoPedido.ENTREGADO),
            crearPedido("2", EstadoPedido.ENTREGADO),
            crearPedido("3", EstadoPedido.ENTREGADO),
            crearPedido("4", EstadoPedido.ENTREGADO),
            crearPedido("5", EstadoPedido.ENTREGADO),
            crearPedido("6", EstadoPedido.PENDIENTE)
        )
        viewModel.setFiltro("ENTREGADOS")
        viewModel.cargarPedidosDePrueba(pedidos)
        viewModel.toggleHistorial()

        val expandido = viewModel.pedidosData.value
        assertEquals(5, expandido.pedidosFiltrados.size)
        assertFalse(expandido.hayMasEntregados)

        viewModel.setFiltro("ACTIVOS")

        val activos = viewModel.pedidosData.value
        assertEquals(1, activos.pedidosFiltrados.size)
        assertEquals(EstadoPedido.PENDIENTE, activos.pedidosFiltrados[0].estado)

        viewModel.setFiltro("ENTREGADOS")

        val vuelta = viewModel.pedidosData.value
        assertEquals(3, vuelta.pedidosFiltrados.size)
        assertTrue(vuelta.hayMasEntregados)
    }
}
