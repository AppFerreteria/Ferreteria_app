package com.example.ferreteria_app

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

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

        listener = db.collection("pedidos")
            .whereEqualTo("clienteId", uid)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val pedidos = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Pedido::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(pedidos)
            }

        awaitClose { listener?.remove() }
    }
}