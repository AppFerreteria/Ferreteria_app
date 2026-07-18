package com.example.ferreteria_app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PedidoViewModel : ViewModel() {

    private val repository = PedidoRepository()

    private val _pedidosState = MutableStateFlow<List<Pedido>>(emptyList())
    val pedidosState: StateFlow<List<Pedido>> = _pedidosState.asStateFlow()

    private val _pedidoActual = MutableStateFlow<Pedido?>(null)
    val pedidoActual = _pedidoActual.asStateFlow()

    fun iniciarEscucha() {
        viewModelScope.launch {
            repository.escucharPedidos().collect { pedidos ->
                _pedidosState.value = pedidos
            }
        }
    }

    fun iniciarEscuchaRepartidor() {
        viewModelScope.launch {
            repository.escucharPedidosRepartidor().collect { pedidos ->
                _pedidosState.value = pedidos
            }
        }
    }

    fun detenerEscucha() {
        _pedidosState.value = emptyList()
    }

    fun obtenerPedido(pedidoId: String) {
        viewModelScope.launch {
            val pedido = repository.obtenerPedido(pedidoId)
            _pedidoActual.value = pedido
        }
    }

    fun generarQR(pedidoId: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val token = repository.generarTokenQR(pedidoId)
            onResult(token)
        }
    }

    fun actualizarEstado(pedidoId: String, nuevoEstado: String) {
        viewModelScope.launch {
            repository.actualizarEstadoPedido(pedidoId, nuevoEstado)
        }
    }
}