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
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class IniciarSesionActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private var rolTemporal: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_iniciar_sesion)

        auth = FirebaseAuth.getInstance()
        
        // Capturamos el rol de la variable temporal (Intent) al iniciar
        rolTemporal = intent.getStringExtra("ROL")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnSignIn = findViewById<MaterialButton>(R.id.btnSignIn)
        val btnCreateAccount = findViewById<MaterialButton>(R.id.btnCreateAccount)

        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)

        btnCreateAccount.setOnClickListener {
            val intentRegistro = Intent(this, RegistroActivity::class.java)
            intentRegistro.putExtra("ROL", rolTemporal)
            startActivity(intentRegistro)
        }

        tvForgotPassword.setOnClickListener {
            val intent = Intent(this, RecuperarActivity::class.java)
            startActivity(intent)
        }

        btnSignIn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_credenciales_vacias), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Capturamos y almacenamos el ID del usuario en la sesión
                        CheckoutSession.userId = auth.currentUser?.uid

                        verificarRolYRedirigir()
                    } else {
                        Toast.makeText(this, getString(R.string.toast_error_acceso, task.exception?.message), Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun verificarRolYRedirigir() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                val rolReal = doc.getString("rol")
                
                // Comparamos el rol que viene de Firebase con nuestra variable temporal
                if (rolReal != null && rolReal.equals(rolTemporal, ignoreCase = true)) {
                    // Almacenamos el UID real autenticado
                    CheckoutSession.userId = uid

                    // Los roles coinciden, permitimos el ingreso
                    val intent = if (rolReal.equals("REPARTIDOR", ignoreCase = true)) {
                        Intent(this, RepartidorPedidosActivity::class.java)
                    } else {
                        Intent(this, CatalogoActivity::class.java)
                    }
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    // Los roles NO coinciden, cerramos sesión y mostramos error
                    auth.signOut()
                    val mensaje = if (rolTemporal == "REPARTIDOR") {
                        "Usted no es REPARTIDOR"
                    } else {
                        "Usted no es CLIENTE"
                    }
                    Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                auth.signOut()
                Toast.makeText(this, "Error al verificar perfil", Toast.LENGTH_SHORT).show()
            }
    }
}