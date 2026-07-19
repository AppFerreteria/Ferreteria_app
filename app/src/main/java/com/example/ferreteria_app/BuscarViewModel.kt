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

data class BuscarData(
    val productos: List<Producto> = emptyList(),
    val textoBusqueda: String = "",
    val filtroCategoria: String? = null,
    val filtroPrecioMaximo: Double? = null,
    val filtroSoloStock: Boolean = false,
    val nombreUsuario: String = ""
)

class BuscarViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val carritoRepository = CarritoRepository()

    private val _uiState = MutableStateFlow<UiState<BuscarData>>(UiState.Loading)
    val uiState: StateFlow<UiState<BuscarData>> = _uiState.asStateFlow()

    private val _categorias = MutableStateFlow<List<String>>(emptyList())
    val categorias: StateFlow<List<String>> = _categorias.asStateFlow()

    private val _precioMaximoCatalogo = MutableStateFlow(1000f)
    val precioMaximoCatalogo: StateFlow<Float> = _precioMaximoCatalogo.asStateFlow()

    private var productosMaestros: List<Producto> = emptyList()

    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val snapshot = db.collection("productos").get().await()
                val productos = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Producto::class.java)?.copy(id = doc.id)
                }
                productosMaestros = productos

                val maxPrecio = productos.maxOfOrNull { it.precio }?.toFloat() ?: 1000f
                _precioMaximoCatalogo.value = if (maxPrecio > 0f) maxPrecio else 1000f

                _categorias.value = productos.map { it.categoria }.distinct()

                var nombreUsuario = ""
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    try {
                        val userDoc = db.collection("usuarios").document(uid).get().await()
                        nombreUsuario = userDoc.getString("nombre") ?: ""
                    } catch (_: Exception) {}
                }

                _uiState.value = UiState.Success(
                    BuscarData(nombreUsuario = nombreUsuario)
                )
            } catch (e: Exception) {
                Log.e("BuscarViewModel", "Error al cargar datos", e)
                _uiState.value = UiState.Error("Error al cargar datos: ${e.message}")
            }
        }
    }

    fun buscar(texto: String) {
        val current = _uiState.value
        if (current !is UiState.Success) return

        _uiState.value = UiState.Success(current.data.copy(textoBusqueda = texto))
        ejecutarFiltros()
    }

    fun aplicarFiltros(categoria: String?, precioMaximo: Double?, soloStock: Boolean) {
        val current = _uiState.value
        if (current !is UiState.Success) return

        _uiState.value = UiState.Success(
            current.data.copy(
                filtroCategoria = categoria,
                filtroPrecioMaximo = precioMaximo,
                filtroSoloStock = soloStock
            )
        )
        ejecutarFiltros()
    }

    fun quitarFiltroCategoria() {
        aplicarFiltros(null, _uiState.value?.let { (it as? UiState.Success)?.data?.filtroPrecioMaximo }, (_uiState.value as? UiState.Success)?.data?.filtroSoloStock ?: false)
    }

    fun quitarFiltroPrecio() {
        aplicarFiltros((_uiState.value as? UiState.Success)?.data?.filtroCategoria, null, (_uiState.value as? UiState.Success)?.data?.filtroSoloStock ?: false)
    }

    fun quitarFiltroStock() {
        aplicarFiltros((_uiState.value as? UiState.Success)?.data?.filtroCategoria, (_uiState.value as? UiState.Success)?.data?.filtroPrecioMaximo, false)
    }

    private fun ejecutarFiltros() {
        val current = _uiState.value
        if (current !is UiState.Success) return

        val filtro = current.data
        val resultados = productosMaestros.filter { producto ->
            val coincideTexto = filtro.textoBusqueda.isBlank() ||
                    producto.nombre.contains(filtro.textoBusqueda, ignoreCase = true)

            val coincideCategoria = filtro.filtroCategoria == null ||
                    producto.categoria == filtro.filtroCategoria

            val coincidePrecio = filtro.filtroPrecioMaximo == null ||
                    producto.precio <= filtro.filtroPrecioMaximo!!

            val coincideStock = !filtro.filtroSoloStock || producto.stock > 0

            coincideTexto && coincideCategoria && coincidePrecio && coincideStock
        }

        _uiState.value = UiState.Success(current.data.copy(productos = resultados))
    }

    fun agregarProductoAlCarrito(producto: Producto, onResult: (Boolean, String) -> Unit) {
        carritoRepository.agregarProducto(
            producto = producto,
            onSuccess = { onResult(true, "Producto agregado al carrito") },
            onError = { mensaje -> onResult(false, mensaje) }
        )
    }
}
