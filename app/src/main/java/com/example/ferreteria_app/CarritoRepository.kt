package com.example.ferreteria_app

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CarritoRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private fun uid(): String? = auth.currentUser?.uid

    private fun cartDocRef() = uid()?.let { uid ->
        db.collection("usuarios").document(uid).collection("carrito").document("actual")
    }

    private fun itemsCollectionRef() = uid()?.let { uid ->
        db.collection("usuarios").document(uid).collection("carrito").document("actual")
            .collection("items")
    }

    fun escucharCarrito(): Flow<Carrito> = callbackFlow {
        val uid = uid() ?: run { close(); return@callbackFlow }
        val configRef = db.collection("configuracion").document("tienda")
        val itemsRef = db.collection("usuarios").document(uid).collection("carrito")
            .document("actual").collection("items")

        val listeners = mutableListOf<ListenerRegistration>()

        var configActual = ConfiguracionTienda()

        val configListener = configRef.addSnapshotListener { doc, error ->
            if (error == null && doc != null && doc.exists()) {
                configActual = doc.toObject(ConfiguracionTienda::class.java) ?: ConfiguracionTienda()
            }
        }
        listeners.add(configListener)

        val itemsListener = itemsRef.addSnapshotListener { snap, error ->
            if (error != null) return@addSnapshotListener
            val items = snap?.documents?.mapNotNull { doc ->
                doc.toObject(CarritoItem::class.java)?.copy(idProducto = doc.id)
            } ?: emptyList()

            val subtotal = items.sumOf { it.precio * it.cantidad }
            val totalArticulos = items.sumOf { it.cantidad }
            val descuento = if (subtotal > configActual.umbralDescuento) configActual.montoDescuento else 0.0
            val costoEnvio = if (totalArticulos > 0) configActual.costoEnvio else 0.0
            val total = subtotal - descuento + costoEnvio

            trySend(
                Carrito(
                    id = "actual",
                    uid = uid,
                    items = items,
                    subtotal = subtotal,
                    descuento = descuento,
                    costoEnvio = costoEnvio,
                    total = total
                )
            )
        }
        listeners.add(itemsListener)

        awaitClose {
            listeners.forEach { it.remove() }
        }
    }

    fun agregarProducto(
        producto: Producto,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = uid() ?: run { onError("Usuario no autenticado"); return }
        if (producto.stock <= 0) run { onError("Producto sin stock"); return }

        val cartDoc = cartDocRef() ?: return
        val itemDoc = itemsCollectionRef()?.document(producto.id) ?: return

        cartDoc.get().addOnSuccessListener { cartSnap ->
            if (!cartSnap.exists()) {
                cartDoc.set(
                    hashMapOf(
                        "id" to "actual",
                        "uid" to uid,
                        "fechaCreacion" to System.currentTimeMillis(),
                        "estado" to EstadoCarrito.ACTIVO.name
                    )
                )
            }
            itemDoc.get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val cantidadActual = doc.getLong("cantidad")?.toInt() ?: 1
                    if (cantidadActual < producto.stock) {
                        itemDoc.update("cantidad", cantidadActual + 1)
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { onError(it.message ?: "Error al actualizar") }
                    } else {
                        onError("Stock máximo alcanzado")
                    }
                } else {
                    val item = CarritoItem(
                        idProducto = producto.id,
                        nombre = producto.nombre,
                        precio = producto.precio,
                        cantidad = 1
                    )
                    itemDoc.set(item)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onError(it.message ?: "Error al agregar") }
                }
            }.addOnFailureListener { onError(it.message ?: "Error al consultar item") }
        }.addOnFailureListener { onError(it.message ?: "Error al obtener carrito") }
    }

    fun actualizarCantidad(item: CarritoItem, nuevaCantidad: Int) {
        itemsCollectionRef()?.document(item.idProducto)?.update("cantidad", nuevaCantidad)
    }

    fun eliminarItem(item: CarritoItem) {
        itemsCollectionRef()?.document(item.idProducto)?.delete()
    }
}
