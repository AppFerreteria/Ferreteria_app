package com.example.ferreteria_app

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PedidoRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private fun uid(): String? = auth.currentUser?.uid

    /**
     * Escucha en tiempo real los pedidos del cliente autenticado,
     * ordenados del más reciente al más antiguo.
     */
    fun escucharPedidos(): Flow<List<Pedido>> = callbackFlow {
        val uid = uid() ?: run { close(); return@callbackFlow }

        var listener: ListenerRegistration? = null

        // Eliminamos el orderBy temporalmente para evitar problemas de índices compuestos
        listener = db.collection("pedidos")
            .whereEqualTo("clienteId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirestoreError", "Error al escuchar pedidos cliente", error)
                    return@addSnapshotListener
                }
                val pedidos = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Pedido::class.java)?.copy(id = doc.id)
                }?.sortedByDescending { it.fecha } ?: emptyList() // Ordenamos localmente

                trySend(pedidos)
            }

        awaitClose { listener?.remove() }
    }

    fun escucharPedidosRepartidor(): Flow<List<Pedido>> = callbackFlow {
        val uid = uid() ?: run { 
            android.util.Log.e("FirestoreError", "UID es nulo en escucharPedidosRepartidor")
            close(); return@callbackFlow 
        }

        android.util.Log.d("FirestoreDebug", "Escuchando pedidos para repartidorId: $uid")

        var listener: ListenerRegistration? = null

        listener = db.collection("pedidos")
            .whereEqualTo("repartidorId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirestoreError", "Error al escuchar pedidos repartidor", error)
                    return@addSnapshotListener
                }
                
                val pedidos = snapshot?.documents?.mapNotNull { doc ->
                    val p = doc.toObject(Pedido::class.java)?.copy(id = doc.id)
                    p
                }?.filter { it.estado != EstadoPedido.ENTREGADO } // Ocultar entregados para el repartidor
                ?.sortedByDescending { it.fecha } ?: emptyList()

                trySend(pedidos)
            }

        awaitClose { listener?.remove() }
    }

    suspend fun obtenerPedido(pedidoId: String): Pedido? {
        return try {
            val doc = db.collection("pedidos").document(pedidoId).get().await()
            doc.toObject(Pedido::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun generarTokenQR(pedidoId: String): String? {
        val pedido = obtenerPedido(pedidoId) ?: return null
        val token = UUID.randomUUID().toString()
        val fechaCreacion = System.currentTimeMillis()
        val fechaExpiracion = fechaCreacion + (5 * 60 * 1000) // 5 minutos

        val tokenData = hashMapOf(
            "token" to token,
            "pedidoId" to pedidoId,
            "fechaCreacion" to fechaCreacion,
            "fechaExpiracion" to fechaExpiracion,
            "utilizado" to false
        )

        return try {
            db.collection("tokensQR").document(token).set(tokenData).await()
            db.collection("pedidos").document(pedidoId).update(
                "tokenQR", token,
                "fechaExpiracionQR", fechaExpiracion
            ).await()
            token
        } catch (e: Exception) {
            null
        }
    }

    suspend fun validarQRCode(token: String): Result<Pedido> {
        return try {
            val tokenDoc = db.collection("tokensQR").document(token).get().await()
            if (!tokenDoc.exists()) return Result.failure(Exception("Token no válido"))
            
            val utilizado = tokenDoc.getBoolean("utilizado") ?: true
            val fechaExpiracion = tokenDoc.getLong("fechaExpiracion") ?: 0
            val pedidoId = tokenDoc.getString("pedidoId") ?: ""

            if (utilizado) return Result.failure(Exception("Token ya utilizado"))
            if (System.currentTimeMillis() > fechaExpiracion) return Result.failure(Exception("Token expirado"))

            val pedido = obtenerPedido(pedidoId) ?: return Result.failure(Exception("Pedido no encontrado"))
            
            Result.success(pedido)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun confirmarEntrega(pedidoId: String, token: String): Boolean {
        return try {
            val batch = db.batch()
            
            val pedidoRef = db.collection("pedidos").document(pedidoId)
            val tokenRef = db.collection("tokensQR").document(token)
            val confirmacionRef = db.collection("confirmacionesEntrega").document()

            val pedido = obtenerPedido(pedidoId) ?: return false

            batch.update(pedidoRef, mapOf(
                "estado" to EstadoPedido.ENTREGADO,
                "fechaEntrega" to System.currentTimeMillis(),
                "confirmado" to true
            ))

            batch.update(tokenRef, "utilizado", true)

            val confirmacionData = hashMapOf(
                "idConfirmacion" to confirmacionRef.id,
                "pedidoId" to pedidoId,
                "clienteId" to pedido.clienteId,
                "repartidorId" to pedido.repartidorId,
                "fechaConfirmacion" to System.currentTimeMillis(),
                "tokenQR" to token,
                "metodo" to "QR",
                "estado" to "CONFIRMADO"
            )
            batch.set(confirmacionRef, confirmacionData)

            batch.commit().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun actualizarEstadoPedido(pedidoId: String, nuevoEstado: String): Boolean {
        return try {
            db.collection("pedidos").document(pedidoId).update("estado", nuevoEstado).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}