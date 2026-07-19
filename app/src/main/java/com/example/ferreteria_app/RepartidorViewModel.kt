package com.example.ferreteria_app

import android.util.Log
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class RepartidorData(
    val pedidos: List<Pedido> = emptyList(),
    val nombreRepartidor: String = ""
)

class RepartidorViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<UiState<RepartidorData>>(UiState.Loading)
    val uiState: StateFlow<UiState<RepartidorData>> = _uiState.asStateFlow()

    private val uid: String
        get() = SessionManager.getRepartidorId(getApplication())

    fun cargarPedidos() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val snapshot = db.collection("pedidos")
                    .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()

                val pedidos = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Pedido::class.java)?.copy(id = doc.id)
                }

                var nombreRepartidor = ""
                try {
                    val userDoc = db.collection("usuarios").document(uid).get().await()
                    nombreRepartidor = userDoc.getString("nombre") ?: ""
                } catch (_: Exception) {}

                _uiState.value = UiState.Success(
                    RepartidorData(pedidos = pedidos, nombreRepartidor = nombreRepartidor)
                )
            } catch (e: Exception) {
                Log.e("RepartidorVM", "Error al cargar pedidos", e)
                _uiState.value = UiState.Error("Error al cargar pedidos: ${e.message}")
            }
        }
    }

    fun tomarPedido(pedidoId: String) {
        viewModelScope.launch {
            try {
                db.collection("pedidos").document(pedidoId).update(
                    mapOf(
                        "repartidorId" to uid,
                        "estado" to EstadoPedido.PREPARACION
                    )
                ).await()
                cargarPedidos()
            } catch (e: Exception) {
                Log.e("RepartidorVM", "Error al tomar pedido", e)
            }
        }
    }

    fun marcarEnCamino(pedidoId: String) {
        viewModelScope.launch {
            try {
                db.collection("pedidos").document(pedidoId).update(
                    "estado", EstadoPedido.EN_CAMINO
                ).await()
                cargarPedidos()
            } catch (e: Exception) {
                Log.e("RepartidorVM", "Error al actualizar estado", e)
            }
        }
    }

    fun marcarEntregado(pedidoId: String) {
        viewModelScope.launch {
            try {
                db.collection("pedidos").document(pedidoId).update(
                    "estado", EstadoPedido.ENTREGADO
                ).await()
                cargarPedidos()
            } catch (e: Exception) {
                Log.e("RepartidorVM", "Error al actualizar estado", e)
            }
        }
    }

    fun guardarFCMToken() {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful && uid.isNotEmpty()) {
                    val token = task.result
                    db.collection("usuarios").document(uid)
                        .update("fcmToken", token)
                        .addOnFailureListener { e ->
                            Log.e("FCM", "Error al guardar FCM token", e)
                        }
                }
            }
    }
}
