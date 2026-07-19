package com.example.ferreteria_app

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class TarjetaPagoActivity : AppCompatActivity() {

    private lateinit var viewModel: TarjetaPagoViewModel
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

        viewModel = ViewModelProvider(this)[TarjetaPagoViewModel::class.java]

        findViewById<TextView>(R.id.btnBackTarjeta).setOnClickListener { finish() }

        etNumero = findViewById(R.id.etNumeroTarjeta)
        etTitular = findViewById(R.id.etTitularTarjeta)
        etVencimiento = findViewById(R.id.etVencimientoTarjeta)
        etCvv = findViewById(R.id.etCvvTarjeta)
        btnPagar = findViewById(R.id.btnPagarTarjeta)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.formData.collect { data ->
                    btnPagar.text = viewModel.totalPagar
                    findViewById<TextView>(R.id.tvMarcaTarjeta).text = data.marca
                    findViewById<TextView>(R.id.tvPreviewNumeroTarjeta).text = data.numeroPreview
                    findViewById<TextView>(R.id.tvPreviewTitular).text = data.titularPreview
                    findViewById<TextView>(R.id.tvPreviewVencimiento).text = data.vencimientoPreview
                    btnPagar.isEnabled = data.esValido
                }
            }
        }

        etNumero.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                if (editandoNumero) return
                editandoNumero = true
                val limpio = s?.toString().orEmpty().filter { it.isDigit() }
                viewModel.actualizarNumero(limpio)
                val pos = viewModel.formData.value.numero.length
                etNumero.setText(viewModel.formData.value.numero)
                etNumero.setSelection(pos)
                editandoNumero = false
            }
        })

        etTitular.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                viewModel.actualizarTitular(s?.toString().orEmpty())
            }
        })

        etVencimiento.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                if (editandoVencimiento) return
                editandoVencimiento = true
                val limpio = s?.toString().orEmpty().filter { it.isDigit() }
                viewModel.actualizarVencimiento(limpio)
                val pos = viewModel.formData.value.vencimiento.length
                etVencimiento.setText(viewModel.formData.value.vencimiento)
                etVencimiento.setSelection(pos)
                editandoVencimiento = false
            }
        })

        etCvv.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                viewModel.actualizarCvv(s?.toString().orEmpty())
            }
        })

        btnPagar.setOnClickListener {
            viewModel.confirmarPago()
            startActivity(Intent(this, ProcesandoPagoActivity::class.java))
        }
    }
}
