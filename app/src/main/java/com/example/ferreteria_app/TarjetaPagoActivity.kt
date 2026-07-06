package com.example.ferreteria_app

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class TarjetaPagoActivity : AppCompatActivity() {
    private lateinit var etNumero: EditText
    private lateinit var etTitular: EditText
    private lateinit var etVencimiento: EditText
    private lateinit var etCvv: EditText
    private lateinit var btnPagar: MaterialButton
    private var editandoNumero = false
    private var editandoVencimiento = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tarjeta_pago)

        findViewById<TextView>(R.id.btnBackTarjeta).setOnClickListener { finish() }
        etNumero = findViewById(R.id.etNumeroTarjeta)
        etTitular = findViewById(R.id.etTitularTarjeta)
        etVencimiento = findViewById(R.id.etVencimientoTarjeta)
        etCvv = findViewById(R.id.etCvvTarjeta)
        btnPagar = findViewById(R.id.btnPagarTarjeta)
        btnPagar.text = "Pagar ${PagoLogic.formatearSoles(CheckoutSession.carrito.total)}"

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = actualizarVistaTarjeta()
            override fun afterTextChanged(s: Editable?) = Unit
        }

        etNumero.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                if (editandoNumero) return
                editandoNumero = true
                val formateado = PagoLogic.formatearNumeroTarjeta(s?.toString().orEmpty())
                etNumero.setText(formateado)
                etNumero.setSelection(formateado.length)
                editandoNumero = false
                actualizarVistaTarjeta()
            }
        })
        etTitular.addTextChangedListener(watcher)
        etCvv.addTextChangedListener(watcher)
        etVencimiento.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                if (editandoVencimiento) return
                editandoVencimiento = true
                val formateado = PagoLogic.formatearVencimiento(s?.toString().orEmpty())
                etVencimiento.setText(formateado)
                etVencimiento.setSelection(formateado.length)
                editandoVencimiento = false
                actualizarVistaTarjeta()
            }
        })

        btnPagar.setOnClickListener {
            CheckoutSession.metodoPago = MetodoPago.TARJETA
            startActivity(Intent(this, ProcesandoPagoActivity::class.java))
        }
        actualizarVistaTarjeta()
    }

    private fun actualizarVistaTarjeta() {
        val numero = etNumero.text?.toString().orEmpty()
        val titular = etTitular.text?.toString().orEmpty().uppercase().ifBlank { "TITULAR" }
        val vencimiento = etVencimiento.text?.toString().orEmpty().ifBlank { "MM/AA" }
        findViewById<TextView>(R.id.tvMarcaTarjeta).text = PagoLogic.detectarMarcaTarjeta(numero)
        findViewById<TextView>(R.id.tvPreviewNumeroTarjeta).text = numero.ifBlank { "•••• •••• •••• ••••" }
        findViewById<TextView>(R.id.tvPreviewTitular).text = titular
        findViewById<TextView>(R.id.tvPreviewVencimiento).text = vencimiento
        btnPagar.isEnabled = PagoLogic.validarTarjeta(numero, etTitular.text.toString(), etVencimiento.text.toString(), etCvv.text.toString())
    }
}
