package com.example.ferreteria_app

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inicializar motor de Firebase Auth
        auth = FirebaseAuth.getInstance()

        // 2. Interceptor de Persistencia de Sesión
        if (auth.currentUser != null) {
            // Si el usuario ya está logueado, lo enviamos directo al catálogo
            val intent = Intent(this, CatalogoActivity::class.java)
            startActivity(intent)
            finish()
            return // Detiene la ejecución para no cargar la vista de MainActivity
        }

        // 3. Carga normal de la vista (solo si no hay sesión activa)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Lógica de tus botones originales
        val btnClient = findViewById<MaterialButton>(R.id.btnClient)

        btnClient.setOnClickListener {
            val intent = Intent(this, ClienteActivity::class.java)
            startActivity(intent)
        }
    }
}