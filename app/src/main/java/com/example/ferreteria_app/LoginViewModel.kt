package com.example.ferreteria_app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<UiState<RolUsuario>>(UiState.Loading)
    val uiState: StateFlow<UiState<RolUsuario>> = _uiState.asStateFlow()

    fun iniciarSesion(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = UiState.Error("Correo y contrasena son obligatorios")
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid

                if (uid != null) {
                    val rol = try {
                        val doc = db.collection("usuarios").document(uid).get().await()
                        val rolStr = doc.getString("rol") ?: RolUsuario.CLIENTE.name
                        try { RolUsuario.valueOf(rolStr) } catch (_: Exception) { RolUsuario.CLIENTE }
                    } catch (_: Exception) {
                        RolUsuario.CLIENTE
                    }
                    _uiState.value = UiState.Success(rol)
                } else {
                    _uiState.value = UiState.Error("Error al autenticar usuario")
                }
            } catch (e: FirebaseAuthInvalidUserException) {
                _uiState.value = UiState.Error("Usuario no registrado")
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _uiState.value = UiState.Error("Correo o contrasena incorrectos")
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error de conexion: ${e.message}")
            }
        }
    }
}
