package com.example.ferreteria_app

import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class LoginRepartidorEstado(
    val isLoading: Boolean = false,
    val error: String? = null,
    val repartidorEncontrado: Repartidor? = null
)

class LoginRepartidorViewModel(application: android.app.Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(LoginRepartidorEstado())
    val uiState: StateFlow<LoginRepartidorEstado> = _uiState.asStateFlow()

    fun iniciarSesion(codigoRepartidor: String) {
        val codigoBuscado = codigoRepartidor.trim()
        if (codigoBuscado.isBlank()) {
            _uiState.value = LoginRepartidorEstado(error = "Ingrese su codigo de repartidor")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginRepartidorEstado(isLoading = true)
            try {
                if (auth.currentUser == null) {
                    auth.signInAnonymously().await()
                }

                val todosDocs = db.collection("repartidores")
                    .get()
                    .await()

                Log.d("LoginRepartidorVM", "Total documentos en repartidores: ${todosDocs.size()}")
                todosDocs.documents.forEach { d ->
                    Log.d("LoginRepartidorVM", "Doc id=${d.id} data=${d.data}")
                }

                val doc = todosDocs.documents.firstOrNull { d ->
                    val campoCodigo = d.getString("codigo")
                        ?: d.getString("Codigo")
                        ?: d.getString("CODIGO")
                        ?: d.getString("id")
                        ?: d.getString("Id")
                    campoCodigo?.trim() == codigoBuscado
                }

                if (doc == null) {
                    Log.w("LoginRepartidorVM", "No se encontro doc con codigo: $codigoBuscado")
                    _uiState.value = LoginRepartidorEstado(
                        error = "Codigo de repartidor no encontrado"
                    )
                    return@launch
                }

                val repartidor = doc.toObject(Repartidor::class.java)?.copy(id = doc.id)

                if (repartidor != null) {
                    SessionManager.guardarSesionRepartidor(
                        getApplication(),
                        repartidor.id,
                        repartidor.nombre
                    )
                    _uiState.value = LoginRepartidorEstado(repartidorEncontrado = repartidor)
                } else {
                    _uiState.value = LoginRepartidorEstado(error = "Datos del repartidor invalidos")
                }
            } catch (e: Exception) {
                Log.e("LoginRepartidorVM", "Error al validar repartidor", e)
                _uiState.value = LoginRepartidorEstado(error = "Error de conexion: ${e.message}")
            }
        }
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
