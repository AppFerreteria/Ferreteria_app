package com.example.ferreteria_app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConfirmacionEntregaViewModel : ViewModel() {

    private val repository = PedidoRepository()

    private val _estadoConfirmacion = MutableStateFlow<Result<Pedido>?>(null)
    val estadoConfirmacion = _estadoConfirmacion.asStateFlow()

    private val _confirmacionExitosa = MutableStateFlow<Boolean>(false)
    val confirmacionExitosa = _confirmacionExitosa.asStateFlow()

    fun validarQR(token: String) {
        viewModelScope.launch {
            val result = repository.validarQRCode(token)
            _estadoConfirmacion.value = result
        }
    }

    fun confirmarEntrega(pedidoId: String, token: String) {
        viewModelScope.launch {
            val exito = repository.confirmarEntrega(pedidoId, token)
            _confirmacionExitosa.value = exito
        }
    }

    fun resetearEstado() {
        _estadoConfirmacion.value = null
        _confirmacionExitosa.value = false
    }
}