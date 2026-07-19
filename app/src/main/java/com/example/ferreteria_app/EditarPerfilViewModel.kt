package com.example.ferreteria_app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class PerfilFormState(
    val nombreError: String? = null,
    val telefonoError: String? = null,
    val documentoError: String? = null,
    val direccionError: String? = null
)

class EditarPerfilViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<UiState<Boolean>>(UiState.Loading)
    val uiState: StateFlow<UiState<Boolean>> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(PerfilFormState())
    val formState: StateFlow<PerfilFormState> = _formState.asStateFlow()

    private val _datosCargados = MutableStateFlow<Cliente?>(null)
    val datosCargados: StateFlow<Cliente?> = _datosCargados.asStateFlow()

    val email: String
        get() = auth.currentUser?.email ?: ""

    fun cargarDatosActuales() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val doc = db.collection("usuarios").document(uid).get().await()
                val cliente = doc.toObject(Cliente::class.java) ?: Cliente()
                _datosCargados.value = cliente
            } catch (e: Exception) {
                _datosCargados.value = Cliente()
            }
        }
    }

    fun validarYGuardar(
        nombre: String,
        telefono: String,
        tipoDoc: String,
        numDoc: String,
        direccion: String,
        referencia: String
    ) {
        var esValido = true
        var nombreError: String? = null
        var telefonoError: String? = null
        var documentoError: String? = null
        var direccionError: String? = null

        if (nombre.isBlank()) {
            nombreError = "El nombre es obligatorio"
            esValido = false
        }

        if (telefono.length < 9) {
            telefonoError = "El telefono debe tener al menos 9 digitos"
            esValido = false
        }

        if (tipoDoc == "DNI" && numDoc.length != 8) {
            documentoError = "El DNI debe tener exactamente 8 digitos"
            esValido = false
        } else if (tipoDoc == "RUC" && numDoc.length != 11) {
            documentoError = "El RUC debe tener exactamente 11 digitos"
            esValido = false
        }

        if (direccion.isBlank()) {
            direccionError = "La direccion es obligatoria"
            esValido = false
        }

        _formState.value = PerfilFormState(
            nombreError = nombreError,
            telefonoError = telefonoError,
            documentoError = documentoError,
            direccionError = direccionError
        )

        if (!esValido) return

        val uid = auth.currentUser?.uid ?: run {
            _uiState.value = UiState.Error("Usuario no autenticado")
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val datosActualizados = mapOf(
                    "nombre" to nombre,
                    "telefono" to telefono,
                    "tipoDocumento" to tipoDoc,
                    "numeroDocumento" to numDoc,
                    "direccion" to direccion,
                    "referencia" to referencia
                )
                db.collection("usuarios").document(uid).update(datosActualizados).await()
                _uiState.value = UiState.Success(true)
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error al guardar: ${e.message}")
            }
        }
    }

    fun limpiarErrores() {
        _formState.value = PerfilFormState()
    }
}
