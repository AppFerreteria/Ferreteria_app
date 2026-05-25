package com.example.ferreteria_app

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegistroActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)

        // 1. Inicializar motores de Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Ajuste de márgenes del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 2. Referencias exactas a la Interfaz
        val etNombre = findViewById<TextInputEditText>(R.id.etNombre)
        val etRegistroEmail = findViewById<TextInputEditText>(R.id.etRegistroEmail)
        val etTelefono = findViewById<TextInputEditText>(R.id.etTelefono)
        val etRegistroPassword = findViewById<TextInputEditText>(R.id.etRegistroPassword)
        val etConfirmarPassword = findViewById<TextInputEditText>(R.id.etConfirmarPassword)
        val cbTerminos = findViewById<MaterialCheckBox>(R.id.cbTerminos)
        val btnSubmitRegistro = findViewById<MaterialButton>(R.id.btnSubmitRegistro)
        val tvYaTienesCuenta = findViewById<TextView>(R.id.tvYaTienesCuenta)

        // Retorno al Login
        tvYaTienesCuenta.setOnClickListener {
            finish()
        }

        // 3. Lógica de Ejecución
        btnSubmitRegistro.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val email = etRegistroEmail.text.toString().trim()
            val telefono = etTelefono.text.toString().trim()
            val password = etRegistroPassword.text.toString().trim()
            val confirmarPassword = etConfirmarPassword.text.toString().trim()
            val aceptaTerminos = cbTerminos.isChecked

            // Validaciones Estrictas
            if (nombre.isEmpty() || email.isEmpty() || telefono.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmarPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                // Especificación oficial: Firebase Auth requiere un mínimo de 6 caracteres
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!aceptaTerminos) {
                Toast.makeText(this, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 4. Inserción en la Nube
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid

                        // Mapeo de datos para Firestore
                        val usuarioData = hashMapOf(
                            "nombre" to nombre,
                            "email" to email,
                            "telefono" to telefono
                        )

                        if (userId != null) {
                            db.collection("usuarios").document(userId)
                                .set(usuarioData)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Cuenta creada con éxito", Toast.LENGTH_SHORT).show()

                                    // Enrutamiento seguro al Catálogo (Cierra las pantallas anteriores)
                                    val intent = Intent(this, CatalogoActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Error al guardar el perfil", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        // Captura de errores de Auth (ej. El correo ya existe o formato inválido)
                        Toast.makeText(this, "Fallo en el registro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}