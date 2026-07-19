package com.example.ferreteria_app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegistroViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<UiState<RolUsuario>>(UiState.Loading)
    val uiState: StateFlow<UiState<RolUsuario>> = _uiState.asStateFlow()

    fun registrar(
        nombre: String,
        email: String,
        telefono: String,
        password: String,
        confirmarPassword: String,
        aceptaTerminos: Boolean,
        rol: RolUsuario = RolUsuario.CLIENTE
    ) {
        if (nombre.isBlank() || email.isBlank() || telefono.isBlank() || password.isBlank()) {
            _uiState.value = UiState.Error("Completa todos los campos obligatorios")
            return
        }

        if (password != confirmarPassword) {
            _uiState.value = UiState.Error("Las contrasenas no coinciden")
            return
        }

        if (password.length < 6) {
            _uiState.value = UiState.Error("La contrasena debe tener al menos 6 caracteres")
            return
        }

        if (!aceptaTerminos) {
            _uiState.value = UiState.Error("Debes aceptar los terminos y condiciones")
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val userId = authResult.user?.uid

                if (userId != null) {
                    val cliente = Cliente(
                        nombre = nombre,
                        email = email,
                        telefono = telefono,
                        rol = rol.name
                    )
                    db.collection("usuarios").document(userId).set(cliente).await()

                    _uiState.value = UiState.Success(rol)
                } else {
                    _uiState.value = UiState.Error("Error al crear el usuario")
                }
            } catch (e: FirebaseAuthWeakPasswordException) {
                _uiState.value = UiState.Error("La contrasena es demasiado debil")
            } catch (e: FirebaseAuthUserCollisionException) {
                _uiState.value = UiState.Error("Este correo ya esta registrado")
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error en el registro: ${e.message}")
            }
        }
    }
}
