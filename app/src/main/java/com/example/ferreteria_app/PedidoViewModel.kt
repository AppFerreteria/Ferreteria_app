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

    fun iniciarEscucha() {
        viewModelScope.launch {
            repository.escucharPedidos().collect { pedidos ->
                _pedidosState.value = pedidos
            }
        }
    }

    fun detenerEscucha() {
        _pedidosState.value = emptyList()
    }
}