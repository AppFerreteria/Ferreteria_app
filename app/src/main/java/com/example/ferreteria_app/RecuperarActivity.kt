package com.example.ferreteria_app

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class RecuperarActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etEmail: TextInputEditText
    private lateinit var tilEmail: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recuperar)

        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etEmail = findViewById(R.id.etEmailRecuperar)
        tilEmail = findViewById(R.id.tilEmailRecuperar)

        findViewById<MaterialButton>(R.id.btnEnviarEnlace).setOnClickListener {
            ejecutarRecuperacion()
        }

        findViewById<TextView>(R.id.tvVolverLogin).setOnClickListener {
            finish() // Cierra la pantalla y regresa al Login
        }
    }

    private fun ejecutarRecuperacion() {
        val email = etEmail.text.toString().trim()
        tilEmail.error = null

        if (email.isEmpty()) {
            tilEmail.error = getString(R.string.error_email_vacio)
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = getString(R.string.toast_recuperacion_error) // Reutilizando error para formato
            return
        }

        // Método oficial de Firebase Auth para restablecimiento
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { tarea ->
                if (tarea.isSuccessful) {
                    Toast.makeText(this, getString(R.string.toast_recuperacion_enviada), Toast.LENGTH_LONG).show()
                    finish() // Regresa al login tras el éxito
                } else {
                    Log.e("AuthError", "Error al enviar correo", tarea.exception)
                    Toast.makeText(this, getString(R.string.toast_recuperacion_error), Toast.LENGTH_LONG).show()
                }
            }
    }
}