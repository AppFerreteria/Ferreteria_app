package com.example.ferreteria_app

import android.content.Intent
import android.os.Bundle
import android.view.View
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

class LoginRepartidorActivity : AppCompatActivity() {

    private lateinit var viewModel: LoginRepartidorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_repartidor)

        viewModel = ViewModelProvider(this)[LoginRepartidorViewModel::class.java]

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etCodigo = findViewById<TextInputEditText>(R.id.etCodigoRepartidor)
        val btnIniciarSesion = findViewById<MaterialButton>(R.id.btnIniciarSesionRepartidor)
        val tvError = findViewById<TextView>(R.id.tvErrorRepartidor)

        btnIniciarSesion.setOnClickListener {
            val codigo = etCodigo.text.toString().trim()
            viewModel.iniciarSesion(codigo)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { estado ->
                    when {
                        estado.isLoading -> {
                            btnIniciarSesion.isEnabled = false
                            tvError.visibility = View.GONE
                        }
                        estado.repartidorEncontrado != null -> {
                            val intent = Intent(this@LoginRepartidorActivity, BuscarEntregaActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                        estado.error != null -> {
                            btnIniciarSesion.isEnabled = true
                            tvError.text = estado.error
                            tvError.visibility = View.VISIBLE
                        }
                        else -> {
                            btnIniciarSesion.isEnabled = true
                            tvError.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }
}
