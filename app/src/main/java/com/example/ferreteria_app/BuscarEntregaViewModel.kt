package com.example.ferreteria_app

import android.util.Log
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class BuscarEntregaData(
    val pedido: Pedido? = null,
    val estaBuscando: Boolean = false,
    val sinResultados: Boolean = false
)

class BuscarEntregaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()

    private val repartidorId: String
        get() = SessionManager.getRepartidorId(getApplication())

    private val _uiState = MutableStateFlow(BuscarEntregaData())
    val uiState: StateFlow<BuscarEntregaData> = _uiState.asStateFlow()

    fun buscarOrden(numeroOrden: String) {
        if (numeroOrden.isBlank()) return

        viewModelScope.launch {
            _uiState.value = BuscarEntregaData(estaBuscando = true)
            try {
                val snapshot = db.collection("pedidos")
                    .whereEqualTo("numeroPedido", numeroOrden)
                    .get()
                    .await()

                if (snapshot.isEmpty) {
                    _uiState.value = BuscarEntregaData(sinResultados = true)
                } else {
                    val pedido = snapshot.documents.firstOrNull()
                        ?.toObject(Pedido::class.java)?.copy(id = snapshot.documents[0].id)
                    _uiState.value = BuscarEntregaData(pedido = pedido)
                }
            } catch (e: Exception) {
                Log.e("BuscarEntregaVM", "Error al buscar orden", e)
                _uiState.value = BuscarEntregaData(sinResultados = true)
            }
        }
    }

    fun iniciarEntrega(pedidoId: String) {
        val uid = repartidorId
        if (uid.isEmpty()) return
        viewModelScope.launch {
            try {
                db.collection("pedidos").document(pedidoId).update(
                    mapOf(
                        "repartidorId" to uid,
                        "estado" to EstadoPedido.EN_CAMINO
                    )
                ).await()
                buscarOrden(_uiState.value.pedido?.numeroPedido ?: return@launch)
            } catch (e: Exception) {
                Log.e("BuscarEntregaVM", "Error al iniciar entrega", e)
            }
        }
    }

    fun marcarEntregado(pedidoId: String) {
        viewModelScope.launch {
            try {
                db.collection("pedidos").document(pedidoId).update(
                    "estado", EstadoPedido.ENTREGADO
                ).await()
                buscarOrden(_uiState.value.pedido?.numeroPedido ?: return@launch)
            } catch (e: Exception) {
                Log.e("BuscarEntregaVM", "Error al marcar entregado", e)
            }
        }
    }

    fun limpiarBusqueda() {
        _uiState.value = BuscarEntregaData()
    }
}
