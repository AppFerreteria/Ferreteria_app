package com.example.ferreteria_app

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class IniciarSesionActivity : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_iniciar_sesion)

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

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
            startActivity(Intent(this, RegistroActivity::class.java))
        }

        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, RecuperarActivity::class.java))
        }

        btnSignIn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            viewModel.iniciarSesion(email, password)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            btnSignIn.isEnabled = false
                            btnSignIn.text = "Iniciando sesion..."
                        }
                        is UiState.Success -> {
                            Toast.makeText(
                                this@IniciarSesionActivity,
                                getString(R.string.toast_inicio_sesion_correcto),
                                Toast.LENGTH_SHORT
                            ).show()
                            val destino = when (state.data) {
                                RolUsuario.CLIENTE -> CatalogoActivity::class.java
                                RolUsuario.REPARTIDOR -> BuscarEntregaActivity::class.java
                            }
                            val intent = Intent(this@IniciarSesionActivity, destino)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                        is UiState.Error -> {
                            btnSignIn.isEnabled = true
                            btnSignIn.text = getString(R.string.btn_iniciar_sesion)
                            Toast.makeText(
                                this@IniciarSesionActivity,
                                state.mensaje,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }
}
