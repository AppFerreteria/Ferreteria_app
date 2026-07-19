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

class PrincipalViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val sesionActiva: Boolean
        get() = auth.currentUser != null

    private val _rol = MutableStateFlow<RolUsuario?>(null)
    val rol: StateFlow<RolUsuario?> = _rol.asStateFlow()

    fun verificarSesionYRol() {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            _rol.value = null
            return
        }

        viewModelScope.launch {
            try {
                val doc = db.collection("usuarios").document(uid).get().await()
                val rolStr = doc.getString("rol") ?: RolUsuario.CLIENTE.name
                _rol.value = try {
                    RolUsuario.valueOf(rolStr)
                } catch (_: Exception) {
                    RolUsuario.CLIENTE
                }
            } catch (_: Exception) {
                _rol.value = null
            }
        }
    }
}
