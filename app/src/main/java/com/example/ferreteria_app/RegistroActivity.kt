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
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class RegistroActivity : AppCompatActivity() {

    private lateinit var viewModel: RegistroViewModel
    private val rol: RolUsuario = RolUsuario.CLIENTE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)

        viewModel = ViewModelProvider(this)[RegistroViewModel::class.java]

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etNombre = findViewById<TextInputEditText>(R.id.etNombre)
        val etRegistroEmail = findViewById<TextInputEditText>(R.id.etRegistroEmail)
        val etTelefono = findViewById<TextInputEditText>(R.id.etTelefono)
        val etRegistroPassword = findViewById<TextInputEditText>(R.id.etRegistroPassword)
        val etConfirmarPassword = findViewById<TextInputEditText>(R.id.etConfirmarPassword)
        val cbTerminos = findViewById<MaterialCheckBox>(R.id.cbTerminos)
        val btnSubmitRegistro = findViewById<MaterialButton>(R.id.btnSubmitRegistro)
        val tvYaTienesCuenta = findViewById<TextView>(R.id.tvYaTienesCuenta)

        tvYaTienesCuenta.setOnClickListener { finish() }

        btnSubmitRegistro.setOnClickListener {
            viewModel.registrar(
                nombre = etNombre.text.toString().trim(),
                email = etRegistroEmail.text.toString().trim(),
                telefono = etTelefono.text.toString().trim(),
                password = etRegistroPassword.text.toString().trim(),
                confirmarPassword = etConfirmarPassword.text.toString().trim(),
                aceptaTerminos = cbTerminos.isChecked,
                rol = rol
            )
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            btnSubmitRegistro.isEnabled = false
                        }
                        is UiState.Success -> {
                            Toast.makeText(this@RegistroActivity, "Cuenta creada con exito", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@RegistroActivity, CatalogoActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                        is UiState.Error -> {
                            btnSubmitRegistro.isEnabled = true
                            Toast.makeText(this@RegistroActivity, state.mensaje, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
}
