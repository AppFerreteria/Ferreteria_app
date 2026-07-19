package com.example.ferreteria_app

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RecuperarViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<UiState<Boolean>>(UiState.Loading)
    val uiState: StateFlow<UiState<Boolean>> = _uiState.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    fun enviarEnlaceRecuperacion(email: String) {
        _emailError.value = null

        if (email.isBlank()) {
            _emailError.value = "El correo es obligatorio"
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailError.value = "Formato de correo invalido"
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                auth.sendPasswordResetEmail(email).await()
                _uiState.value = UiState.Success(true)
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error al enviar el enlace: ${e.message}")
            }
        }
    }

    fun limpiarEmailError() {
        _emailError.value = null
    }
}
