package com.example.ferreteria_app

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Nuevo token: $token")
        enviarTokenAlServidor(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // Aquí se puede manejar la notificación push en primer plano si es necesario.
        // Si la app está en segundo plano, el sistema de Android muestra la notificación automáticamente
        // basándose en el campo 'notification' del payload de Firebase.
        Log.d("FCM", "Mensaje recibido de: ${message.from}")
    }

    private fun enviarTokenAlServidor(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("usuarios").document(uid).update("fcmToken", token)
                .addOnSuccessListener { Log.d("FCM", "Token actualizado en Firestore") }
                .addOnFailureListener { e -> Log.e("FCM", "Error al guardar el token", e) }
        }
    }
}
