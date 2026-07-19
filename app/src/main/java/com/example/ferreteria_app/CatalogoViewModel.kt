package com.example.ferreteria_app

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class CatalogoData(
    val productos: List<Producto> = emptyList(),
    val categorias: List<String> = emptyList(),
    val nombreUsuario: String = ""
)

class CatalogoViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val carritoRepository = CarritoRepository()

    private val _uiState = MutableStateFlow<UiState<CatalogoData>>(UiState.Loading)
    val uiState: StateFlow<UiState<CatalogoData>> = _uiState.asStateFlow()

    private var productosMaestros: List<Producto> = emptyList()

    val sesionActiva: Boolean
        get() = auth.currentUser != null

    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val snapshot = db.collection("productos").get().await()

                if (snapshot.isEmpty) {
                    subirProductosDePrueba()
                    return@launch
                }

                val productos = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Producto::class.java)?.copy(id = doc.id)
                }
                productosMaestros = productos

                val categorias = listOf("Todos") + productos.map { it.categoria }.distinct()

                var nombreUsuario = ""
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    try {
                        val userDoc = db.collection("usuarios").document(uid).get().await()
                        nombreUsuario = userDoc.getString("nombre") ?: ""
                    } catch (_: Exception) {}
                }

                _uiState.value = UiState.Success(
                    CatalogoData(
                        productos = productos,
                        categorias = categorias,
                        nombreUsuario = nombreUsuario
                    )
                )
            } catch (e: Exception) {
                Log.e("CatalogoViewModel", "Error al cargar datos", e)
                _uiState.value = UiState.Error("Error al cargar el catalogo: ${e.message}")
            }
        }
    }

    fun filtrarPorCategoria(categoria: String) {
        val productos = if (categoria == "Todos") {
            productosMaestros
        } else {
            productosMaestros.filter { it.categoria == categoria }
        }

        val current = _uiState.value
        if (current is UiState.Success) {
            _uiState.value = UiState.Success(current.data.copy(productos = productos))
        }
    }

    fun agregarProductoAlCarrito(producto: Producto, onResult: (Boolean, String) -> Unit) {
        carritoRepository.agregarProducto(
            producto = producto,
            onSuccess = { onResult(true, "Producto agregado al carrito") },
            onError = { mensaje -> onResult(false, mensaje) }
        )
    }

    fun solicitarPermisoNotificacionesYToken() {
        val uid = auth.currentUser?.uid ?: return
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    db.collection("usuarios").document(uid)
                        .update("fcmToken", token)
                        .addOnFailureListener { e ->
                            Log.e("FCM", "Error al guardar FCM token", e)
                        }
                }
            }
    }

    private fun subirProductosDePrueba() {
        viewModelScope.launch {
            try {
                val batch = db.batch()
                for (producto in obtenerListaMock()) {
                    val docRef = db.collection("productos").document()
                    batch.set(docRef, producto)
                }
                batch.commit().await()
                Log.d("FirebaseSeeding", "Productos subidos correctamente")
                cargarDatos()
            } catch (e: Exception) {
                Log.e("FirebaseSeeding", "Error al subir productos", e)
                _uiState.value = UiState.Error("Error de permisos en Firestore")
            }
        }
    }

    private fun obtenerListaMock(): List<Producto> {
        return listOf(
            Producto(nombre = "Martillo Mango Fibra 16oz", precio = 18.90, sku = "HER001", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/6954209-1000-1000/106342.jpg", categoria = "Herramientas", stock = 25),
            Producto(nombre = "Juego de Destornilladores (6 pzas)", precio = 35.50, sku = "HER002", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/6344304-1000-1000/113813.jpg", categoria = "Herramientas", stock = 15),
            Producto(nombre = "Llave Francesa 10\"", precio = 24.90, sku = "HER003", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/6016666-1000-1000/118318.jpg", categoria = "Herramientas", stock = 20),
            Producto(nombre = "Alicate Universal 8\"", precio = 15.00, sku = "HER004", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/6954215-1000-1000/106343.jpg", categoria = "Herramientas", stock = 30),
            Producto(nombre = "Taladro Percutor 600W", precio = 159.00, sku = "MAQ001", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7667857-1000-1000/137351.jpg", categoria = "Maquinaria", stock = 10),
            Producto(nombre = "Sierra Circular 7-1/4\"", precio = 299.00, sku = "MAQ002", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7014169-1000-1000/114515.jpg", categoria = "Maquinaria", stock = 5),
            Producto(nombre = "Amoladora Angular 4-1/2\"", precio = 145.00, sku = "MAQ003", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7014175-1000-1000/114518.jpg", categoria = "Maquinaria", stock = 8),
            Producto(nombre = "Rotomartillo SDS Plus 800W", precio = 420.00, sku = "MAQ004", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7014180-1000-1000/114520.jpg", categoria = "Maquinaria", stock = 4),
            Producto(nombre = "Pintura Latex Pato Blanco Galon", precio = 42.50, sku = "PIN001", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7085750-1000-1000/125219.jpg", categoria = "Pinturas", stock = 40),
            Producto(nombre = "Rodillo antigota 9\"", precio = 12.90, sku = "PIN002", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/682123-1000-1000/112345.jpg", categoria = "Pinturas", stock = 50),
            Producto(nombre = "Brocha Cerda Natural 3\"", precio = 6.50, sku = "PIN003", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/682130-1000-1000/112346.jpg", categoria = "Pinturas", stock = 60),
            Producto(nombre = "Esmalte Sintetico Negro", precio = 28.00, sku = "PIN004", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7085755-1000-1000/125220.jpg", categoria = "Pinturas", stock = 20),
            Producto(nombre = "Cerradura de Dormitorio", precio = 32.90, sku = "SEG001", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7018898-1000-1000/120614.jpg", categoria = "Seguridad", stock = 18),
            Producto(nombre = "Candado de Acero 50mm", precio = 22.00, sku = "SEG002", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7018905-1000-1000/120615.jpg", categoria = "Seguridad", stock = 25),
            Producto(nombre = "Guantes de Seguridad Cuero", precio = 14.50, sku = "SEG003", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7018910-1000-1000/120616.jpg", categoria = "Seguridad", stock = 40),
            Producto(nombre = "Casco de Seguridad Amarillo", precio = 19.90, sku = "SEG004", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7018915-1000-1000/120617.jpg", categoria = "Seguridad", stock = 15),
            Producto(nombre = "Foco LED 9W Luz Blanca", precio = 5.90, sku = "ELE001", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7018920-1000-1000/120618.jpg", categoria = "Electricidad", stock = 100),
            Producto(nombre = "Interruptor Simple Blanco", precio = 8.50, sku = "ELE002", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7018925-1000-1000/120619.jpg", categoria = "Electricidad", stock = 50),
            Producto(nombre = "Cinta Aislante 20m Negra", precio = 4.50, sku = "ELE003", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7018930-1000-1000/120620.jpg", categoria = "Electricidad", stock = 80),
            Producto(nombre = "Cable Electrico 14 AWG 100m", precio = 145.00, sku = "ELE004", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7018935-1000-1000/120621.jpg", categoria = "Electricidad", stock = 12),
            Producto(nombre = "Escalera Telescopica 3.8m", precio = 380.00, sku = "CON001", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/5657591-1000-1000/129712.jpg", categoria = "Construccion", stock = 6),
            Producto(nombre = "Wincha 5 metros", precio = 12.50, sku = "CON002", imagenUrl = "https://www.corporacionferremax.com/product/wincha-global-plus-5-metros-stanley", categoria = "Construccion", stock = 30),
            Producto(nombre = "Nivel de Mano 24\"", precio = 28.90, sku = "CON003", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7018945-1000-1000/120623.jpg", categoria = "Construccion", stock = 15),
            Producto(nombre = "Combo de Goma 16oz", precio = 16.50, sku = "CON004", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7018950-1000-1000/120624.jpg", categoria = "Construccion", stock = 20)
        )
    }
}
