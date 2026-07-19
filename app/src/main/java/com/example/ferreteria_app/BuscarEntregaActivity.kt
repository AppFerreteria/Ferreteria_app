package com.example.ferreteria_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class BuscarEntregaActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buscar_entrega)

        bottomNav = findViewById(R.id.bottomNavigationEntrega)
        bottomNav.selectedItemId = R.id.nav_buscar_entrega
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_buscar_entrega -> {
                    mostrarFragment(BuscarEntregaFragment(), "buscar")
                    true
                }
                R.id.nav_entregas -> {
                    mostrarFragment(EntregasFragment(), "entregas")
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            mostrarFragment(BuscarEntregaFragment(), "buscar")
        }

        guardarFCMToken()
    }

    private fun mostrarFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment, tag)
            .commit()
    }

    private fun guardarFCMToken() {
        val uid = SessionManager.getRepartidorId(this)
        if (uid.isEmpty()) return
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                FirebaseFirestore.getInstance()
                    .collection("usuarios").document(uid)
                    .update("fcmToken", token)
            }
        }
    }
}
