package com.example.ferreteria_app

import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class RecuperarPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recuperar_password)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Navegación de regreso al Login mediante el botón superior
        val tvVolverLogin = findViewById<TextView>(R.id.tvVolverLogin)
        tvVolverLogin.setOnClickListener {
            finish()
        }

        // Componentes del formulario
        val etRecuperarEmail = findViewById<TextInputEditText>(R.id.etRecuperarEmail)
        val btnEnviarEnlace = findViewById<MaterialButton>(R.id.btnEnviarEnlace)

        // Lógica de validación al presionar el botón
        btnEnviarEnlace.setOnClickListener {
            val email = etRecuperarEmail.text.toString().trim()

            // Validación de campo vacío
            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor, ingrese su correo electrónico", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validación de estructura del correo electrónico
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Formato de correo electrónico inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Flujo correcto
            Toast.makeText(this, "Enlace enviado correctamente", Toast.LENGTH_SHORT).show()
        }
    }
}