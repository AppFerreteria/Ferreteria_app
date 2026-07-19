package com.example.ferreteria_app

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SeguimientoData(
    val numeroPedido: String = "",
    val estado: String = "",
    val ubicacionLat: Double? = null,
    val ubicacionLng: Double? = null,
    val textoUbicacion: String = "Esperando ubicacion del repartidor...",
    val mostrarPreparacion: Boolean = true,
    val mostrarMapa: Boolean = false,
    val mostrarEntregado: Boolean = false
)

class MapaSeguimientoViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private var pedidoListener: ListenerRegistration? = null
    private var repartidorListener: ListenerRegistration? = null

    private val _uiState = MutableStateFlow(SeguimientoData())
    val uiState: StateFlow<SeguimientoData> = _uiState.asStateFlow()

    fun escucharPedido(pedidoId: String) {
        pedidoListener = db.collection("pedidos").document(pedidoId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    return@addSnapshotListener
                }

                val pedido = snapshot.toObject(Pedido::class.java)?.copy(id = snapshot.id)
                    ?: return@addSnapshotListener

                _uiState.value = _uiState.value.copy(
                    numeroPedido = pedido.numeroPedido,
                    estado = pedido.estado
                )

                when (pedido.estado) {
                    EstadoPedido.EN_CAMINO -> {
                        _uiState.value = _uiState.value.copy(
                            mostrarPreparacion = false,
                            mostrarMapa = true,
                            mostrarEntregado = false
                        )
                        escucharUbicacionRepartidor(pedido.repartidorId)
                    }
                    EstadoPedido.ENTREGADO -> {
                        repartidorListener?.remove()
                        repartidorListener = null
                        _uiState.value = _uiState.value.copy(
                            mostrarPreparacion = false,
                            mostrarMapa = false,
                            mostrarEntregado = true
                        )
                    }
                    else -> {
                        repartidorListener?.remove()
                        repartidorListener = null
                        _uiState.value = _uiState.value.copy(
                            mostrarPreparacion = true,
                            mostrarMapa = false,
                            mostrarEntregado = false
                        )
                    }
                }
            }
    }

    private fun escucharUbicacionRepartidor(repartidorId: String) {
        if (repartidorListener != null) return
        if (repartidorId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                textoUbicacion = "Esperando ubicacion del repartidor..."
            )
            return
        }

        repartidorListener = db.collection("repartidores").document(repartidorId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    _uiState.value = _uiState.value.copy(
                        textoUbicacion = "Esperando ubicacion del repartidor..."
                    )
                    return@addSnapshotListener
                }

                val repartidor = snapshot.toObject(Repartidor::class.java)
                val geo = repartidor?.ubicacionActual

                if (geo != null) {
                    _uiState.value = _uiState.value.copy(
                        ubicacionLat = geo.latitude,
                        ubicacionLng = geo.longitude,
                        textoUbicacion = "Ubicacion: %.4f, %.4f".format(geo.latitude, geo.longitude)
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        textoUbicacion = "Esperando ubicacion del repartidor..."
                    )
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        pedidoListener?.remove()
        repartidorListener?.remove()
    }
}
