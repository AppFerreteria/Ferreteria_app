package com.example.ferreteria_app

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PerfilActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        configurarNavegacionInferior()

        findViewById<MaterialButton>(R.id.btnEditProfile).setOnClickListener {
            startActivity(Intent(this, EditarPerfilActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnLogout).setOnClickListener {
            cerrarSesion()
        }
    }

    override fun onResume() {
        super.onResume()
        cargarDatosUsuario()
    }

    private fun cargarDatosUsuario() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("usuarios").document(uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val cliente = doc.toObject(Cliente::class.java) ?: Cliente()
                        findViewById<TextView>(R.id.tvNombrePerfil).text = cliente.nombre
                        findViewById<TextView>(R.id.tvEmailPerfil).text = auth.currentUser?.email
                        findViewById<TextView>(R.id.tvTelefonoPerfil).text =
                            if (cliente.telefono.isNotEmpty()) cliente.telefono else getString(R.string.texto_sin_telefono)
                    }
                }
        }
    }

    private fun cerrarSesion() {
        auth.signOut()
        val intent = Intent(this, IniciarSesionActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()

        Toast.makeText(this, getString(R.string.toast_cierre_sesion), Toast.LENGTH_SHORT).show()
    }

    private fun configurarNavegacionInferior() {
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigationPerfil)
        nav.selectedItemId = R.id.nav_perfil
        nav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    startActivity(Intent(this, CatalogoActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_buscar -> {
                    startActivity(Intent(this, BuscarActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_carrito -> {
                    startActivity(Intent(this, CarritoActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_pedidos -> {
                    startActivity(Intent(this, PedidoActivity::class.java))
                    finish()
                    true
                }

                else -> true
            }
        }
    }
}