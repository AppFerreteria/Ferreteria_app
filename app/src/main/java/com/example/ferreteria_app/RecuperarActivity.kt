package com.example.ferreteria_app

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
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class RecuperarActivity : AppCompatActivity() {

    private lateinit var viewModel: RecuperarViewModel
    private lateinit var etEmail: TextInputEditText
    private lateinit var tilEmail: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recuperar)

        viewModel = ViewModelProvider(this)[RecuperarViewModel::class.java]

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etEmail = findViewById(R.id.etEmailRecuperar)
        tilEmail = findViewById(R.id.tilEmailRecuperar)

        findViewById<MaterialButton>(R.id.btnEnviarEnlace).setOnClickListener {
            viewModel.enviarEnlaceRecuperacion(etEmail.text.toString().trim())
        }

        findViewById<TextView>(R.id.tvVolverLogin).setOnClickListener { finish() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.emailError.collect { error ->
                        tilEmail.error = error
                    }
                }
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                findViewById<MaterialButton>(R.id.btnEnviarEnlace).isEnabled = false
                            }
                            is UiState.Success -> {
                                Toast.makeText(
                                    this@RecuperarActivity,
                                    getString(R.string.toast_recuperacion_enviada),
                                    Toast.LENGTH_LONG
                                ).show()
                                finish()
                            }
                            is UiState.Error -> {
                                findViewById<MaterialButton>(R.id.btnEnviarEnlace).isEnabled = true
                                Toast.makeText(
                                    this@RecuperarActivity,
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
}
