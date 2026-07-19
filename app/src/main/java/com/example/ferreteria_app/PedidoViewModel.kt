package com.example.ferreteria_app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PedidosData(
    val todosLosPedidos: List<Pedido> = emptyList(),
    val pedidosFiltrados: List<Pedido> = emptyList(),
    val entregadosLimitados: List<Pedido> = emptyList(),
    val totalEntregados: Int = 0,
    val hayMasEntregados: Boolean = false
)

class PedidoViewModel : ViewModel() {

    private val repository by lazy { PedidoRepository() }

    private val _pedidosData = MutableStateFlow(PedidosData())
    val pedidosData: StateFlow<PedidosData> = _pedidosData.asStateFlow()

    private val _mostrandoHistorial = MutableStateFlow(false)
    val mostrandoHistorial: StateFlow<Boolean> = _mostrandoHistorial.asStateFlow()

    private val _filtroEstado = MutableStateFlow("ACTIVOS")
    val filtroEstado: StateFlow<String> = _filtroEstado.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<PedidosData>>(UiState.Loading)
    val uiState: StateFlow<UiState<PedidosData>> = _uiState.asStateFlow()

    private var todosLosPedidos: List<Pedido> = emptyList()

    fun iniciarEscucha() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.escucharPedidos().collect { pedidos ->
                todosLosPedidos = pedidos
                aplicarFiltro()
            }
        }
    }

    fun detenerEscucha() {
        _pedidosData.value = PedidosData()
        _mostrandoHistorial.value = false
        _uiState.value = UiState.Loading
    }

    fun setFiltro(estado: String) {
        _filtroEstado.value = estado
        _mostrandoHistorial.value = false
        aplicarFiltro()
    }

    fun cargarPedidosDePrueba(pedidos: List<Pedido>) {
        todosLosPedidos = pedidos
        aplicarFiltro()
    }

    fun toggleHistorial() {
        _mostrandoHistorial.value = !_mostrandoHistorial.value
        aplicarFiltro()
    }

    private fun aplicarFiltro() {
        val estadoSeleccionado = _filtroEstado.value

        val pedidosFiltrados = when (estadoSeleccionado) {
            "ACTIVOS" -> todosLosPedidos.filter { it.estado != EstadoPedido.ENTREGADO }
            "ENTREGADOS" -> todosLosPedidos.filter { it.estado == EstadoPedido.ENTREGADO }
            else -> todosLosPedidos.filter { it.estado == estadoSeleccionado.lowercase() }
        }

        val entregados = todosLosPedidos.filter { it.estado == EstadoPedido.ENTREGADO }
        val mostrarTodo = _mostrandoHistorial.value
        val entregadosMostrar = if (mostrarTodo) entregados else entregados.take(3)

        val data = PedidosData(
            todosLosPedidos = todosLosPedidos,
            pedidosFiltrados = if (estadoSeleccionado == "ENTREGADOS") entregadosMostrar else pedidosFiltrados,
            entregadosLimitados = entregadosMostrar,
            totalEntregados = entregados.size,
            hayMasEntregados = estadoSeleccionado == "ENTREGADOS" && entregados.size > 3 && !mostrarTodo
        )

        _pedidosData.value = data
        _uiState.value = UiState.Success(data)
    }
}
