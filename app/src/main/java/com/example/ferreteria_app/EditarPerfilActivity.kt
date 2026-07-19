package com.example.ferreteria_app

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
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
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class EditarPerfilActivity : AppCompatActivity() {

    private lateinit var viewModel: EditarPerfilViewModel

    private lateinit var etNombre: TextInputEditText
    private lateinit var etTelefono: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var actvTipoDoc: AutoCompleteTextView
    private lateinit var etDocumento: TextInputEditText
    private lateinit var etDireccion: TextInputEditText
    private lateinit var etReferencia: TextInputEditText

    private lateinit var tilNombre: TextInputLayout
    private lateinit var tilTelefono: TextInputLayout
    private lateinit var tilDocumento: TextInputLayout
    private lateinit var tilDireccion: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_perfil)

        viewModel = ViewModelProvider(this)[EditarPerfilViewModel::class.java]

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etNombre = findViewById(R.id.etNombreEditar)
        etTelefono = findViewById(R.id.etTelefonoEditar)
        etEmail = findViewById(R.id.etEmailEditar)
        actvTipoDoc = findViewById(R.id.actvTipoDocumento)
        etDocumento = findViewById(R.id.etDocumentoEditar)
        etDireccion = findViewById(R.id.etDireccionEditar)
        etReferencia = findViewById(R.id.etReferenciaEditar)

        tilNombre = findViewById(R.id.tilNombreEditar)
        tilTelefono = findViewById(R.id.tilTelefonoEditar)
        tilDocumento = findViewById(R.id.tilDocumentoEditar)
        tilDireccion = findViewById(R.id.tilDireccionEditar)

        findViewById<ImageView>(R.id.ivAtrasEditar).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.btnGuardarCambios).setOnClickListener { guardar() }

        configurarSelectorDocumento()
        cargarDatosYObservar()
    }

    private fun configurarSelectorDocumento() {
        val opciones = arrayOf(
            getString(R.string.opcion_dni),
            getString(R.string.opcion_ruc)
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opciones)
        actvTipoDoc.setAdapter(adapter)
    }

    private fun cargarDatosYObservar() {
        etEmail.setText(viewModel.email)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.datosCargados.collect { cliente ->
                        cliente?.let {
                            etNombre.setText(it.nombre)
                            etTelefono.setText(it.telefono)
                            etDireccion.setText(it.direccion)
                            etReferencia.setText(it.referencia)
                            if (it.tipoDocumento.isNotEmpty()) {
                                actvTipoDoc.setText(it.tipoDocumento, false)
                            }
                            etDocumento.setText(it.numeroDocumento)
                        }
                    }
                }
                launch {
                    viewModel.formState.collect { form ->
                        tilNombre.error = form.nombreError
                        tilTelefono.error = form.telefonoError
                        tilDocumento.error = form.documentoError
                        tilDireccion.error = form.direccionError
                    }
                }
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                findViewById<MaterialButton>(R.id.btnGuardarCambios).isEnabled = false
                            }
                            is UiState.Success -> {
                                Toast.makeText(
                                    this@EditarPerfilActivity,
                                    getString(R.string.toast_perfil_guardado),
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                            is UiState.Error -> {
                                findViewById<MaterialButton>(R.id.btnGuardarCambios).isEnabled = true
                                Toast.makeText(
                                    this@EditarPerfilActivity,
                                    state.mensaje,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }

        viewModel.cargarDatosActuales()
    }

    private fun guardar() {
        viewModel.limpiarErrores()
        viewModel.validarYGuardar(
            nombre = etNombre.text.toString().trim(),
            telefono = etTelefono.text.toString().trim(),
            tipoDoc = actvTipoDoc.text.toString(),
            numDoc = etDocumento.text.toString().trim(),
            direccion = etDireccion.text.toString().trim(),
            referencia = etReferencia.text.toString().trim()
        )
    }
}
