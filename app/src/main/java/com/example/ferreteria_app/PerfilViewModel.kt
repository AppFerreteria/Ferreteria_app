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

class PerfilViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<UiState<Cliente>>(UiState.Loading)
    val uiState: StateFlow<UiState<Cliente>> = _uiState.asStateFlow()

    val email: String
        get() = auth.currentUser?.email ?: ""

    fun cargarDatos() {
        val uid = auth.currentUser?.uid ?: run {
            _uiState.value = UiState.Error("Usuario no autenticado")
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val doc = db.collection("usuarios").document(uid).get().await()
                val cliente = doc.toObject(Cliente::class.java) ?: Cliente()
                _uiState.value = UiState.Success(cliente)
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error al cargar perfil: ${e.message}")
            }
        }
    }

    fun cerrarSesion() {
        auth.signOut()
    }
}
