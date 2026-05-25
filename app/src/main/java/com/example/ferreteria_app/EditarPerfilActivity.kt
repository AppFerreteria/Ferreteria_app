package com.example.ferreteria_app

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditarPerfilActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

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

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Mapeo estricto de vistas
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
        findViewById<MaterialButton>(R.id.btnGuardarCambios).setOnClickListener { validarYGuardar() }

        configurarSelectorDocumento()
        cargarDatosActuales()
    }

    private fun configurarSelectorDocumento() {
        val opciones = arrayOf(getString(R.string.opcion_dni), getString(R.string.opcion_ruc))
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opciones)
        actvTipoDoc.setAdapter(adapter)
    }

    private fun cargarDatosActuales() {
        val uid = auth.currentUser?.uid
        val emailAutenticado = auth.currentUser?.email
        etEmail.setText(emailAutenticado)

        if (uid != null) {
            db.collection("usuarios").document(uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        etNombre.setText(doc.getString("nombre"))
                        etTelefono.setText(doc.getString("telefono"))
                        etDireccion.setText(doc.getString("direccion"))
                        etReferencia.setText(doc.getString("referencia"))

                        val tipoDocDb = doc.getString("tipoDocumento")
                        if (!tipoDocDb.isNullOrEmpty()) {
                            actvTipoDoc.setText(tipoDocDb, false)
                        }
                        etDocumento.setText(doc.getString("numeroDocumento"))
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("FirestoreError", "Error al descargar perfil", e)
                }
        }
    }

    private fun validarYGuardar() {
        // Limpieza de estados de error previos
        tilNombre.error = null
        tilTelefono.error = null
        tilDocumento.error = null
        tilDireccion.error = null

        val nombre = etNombre.text.toString().trim()
        val telefono = etTelefono.text.toString().trim()
        val tipoDoc = actvTipoDoc.text.toString()
        val numDoc = etDocumento.text.toString().trim()
        val direccion = etDireccion.text.toString().trim()
        val referencia = etReferencia.text.toString().trim()

        var esValido = true

        if (nombre.isEmpty()) {
            tilNombre.error = getString(R.string.error_nombre_vacio)
            esValido = false
        }

        if (telefono.length < 9) {
            tilTelefono.error = getString(R.string.error_telefono)
            esValido = false
        }

        // Validación condicional del documento de facturación
        if (tipoDoc == getString(R.string.opcion_dni) && numDoc.length != 8) {
            tilDocumento.error = getString(R.string.error_dni)
            esValido = false
        } else if (tipoDoc == getString(R.string.opcion_ruc) && numDoc.length != 11) {
            tilDocumento.error = getString(R.string.error_ruc)
            esValido = false
        }

        // Validación logística obligatoria (HU-003)
        if (direccion.isEmpty()) {
            tilDireccion.error = getString(R.string.error_direccion_vacia)
            esValido = false
        }

        if (!esValido) return

        val uid = auth.currentUser?.uid
        if (uid != null) {
            val datosActualizados = mapOf(
                "nombre" to nombre,
                "telefono" to telefono,
                "tipoDocumento" to tipoDoc,
                "numeroDocumento" to numDoc,
                "direccion" to direccion,
                "referencia" to referencia
            )

            db.collection("usuarios").document(uid).update(datosActualizados)
                .addOnSuccessListener {
                    Toast.makeText(this, getString(R.string.toast_perfil_guardado), Toast.LENGTH_SHORT).show()
                    finish() // Cierra la actividad y regresa al Perfil
                }
                .addOnFailureListener { e ->
                    Log.e("FirestoreError", "Error de guardado", e)
                    Toast.makeText(this, getString(R.string.toast_error_guardar), Toast.LENGTH_SHORT).show()
                }
        }
    }
}