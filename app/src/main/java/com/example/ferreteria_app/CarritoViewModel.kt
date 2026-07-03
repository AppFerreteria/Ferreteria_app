package com.example.ferreteria_app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CarritoViewModel : ViewModel() {

    private val repository = CarritoRepository()

    private val _carritoState = MutableStateFlow(Carrito())
    val carritoState: StateFlow<Carrito> = _carritoState.asStateFlow()

    fun iniciarEscucha() {
        viewModelScope.launch {
            repository.escucharCarrito().collect { carrito ->
                _carritoState.value = carrito
            }
        }
    }

    fun detenerEscucha() {
        _carritoState.value = Carrito()
    }

    fun actualizarCantidad(item: CarritoItem, nuevaCantidad: Int) {
        repository.actualizarCantidad(item, nuevaCantidad)
    }

    fun eliminarItem(item: CarritoItem) {
        repository.eliminarItem(item)
    }
}
