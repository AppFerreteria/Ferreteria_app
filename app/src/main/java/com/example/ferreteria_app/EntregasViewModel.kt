package com.example.ferreteria_app

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class EntregaActualEstado(
    val isLoading: Boolean = false,
    val entrega: Pedido? = null,
    val sinEntregas: Boolean = false
)

class EntregasViewModel(application: android.app.Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()

    private val repartidorId: String
        get() = SessionManager.getRepartidorId(getApplication())

    private val _uiState = MutableStateFlow(EntregaActualEstado(isLoading = true))
    val uiState: StateFlow<EntregaActualEstado> = _uiState.asStateFlow()

    fun cargarEntregaActual() {
        val uid = repartidorId
        if (uid.isEmpty()) {
            _uiState.value = EntregaActualEstado(sinEntregas = true)
            return
        }

        viewModelScope.launch {
            _uiState.value = EntregaActualEstado(isLoading = true)
            try {
                val snapshot = db.collection("pedidos")
                    .whereEqualTo("repartidorId", uid)
                    .whereIn("estado", listOf(EstadoPedido.PENDIENTE, EstadoPedido.PREPARACION, EstadoPedido.EN_CAMINO))
                    .get()
                    .await()

                val primerActivo = snapshot.documents
                    .mapNotNull { it.toObject(Pedido::class.java)?.copy(id = it.id) }
                    .maxByOrNull { it.fecha }

                _uiState.value = if (primerActivo != null) {
                    EntregaActualEstado(entrega = primerActivo)
                } else {
                    EntregaActualEstado(sinEntregas = true)
                }
            } catch (_: Exception) {
                _uiState.value = EntregaActualEstado(sinEntregas = true)
            }
        }
    }

    fun marcarEnCamino() {
        val pedido = _uiState.value.entrega ?: return
        viewModelScope.launch {
            try {
                db.collection("pedidos").document(pedido.id)
                    .update("estado", EstadoPedido.EN_CAMINO)
                cargarEntregaActual()
            } catch (_: Exception) { }
        }
    }

    fun marcarEntregado() {
        val pedido = _uiState.value.entrega ?: return
        viewModelScope.launch {
            try {
                db.collection("pedidos").document(pedido.id)
                    .update("estado", EstadoPedido.ENTREGADO)
                cargarEntregaActual()
            } catch (_: Exception) { }
        }
    }
}
