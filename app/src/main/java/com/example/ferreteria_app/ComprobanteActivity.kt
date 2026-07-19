package com.example.ferreteria_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton

class ComprobanteActivity : AppCompatActivity() {

    private lateinit var viewModel: ComprobanteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_comprobante_pago)

        viewModel = ViewModelProvider(this)[ComprobanteViewModel::class.java]

        val comprobante = viewModel.comprobante
        if (!viewModel.existeComprobante || comprobante == null) {
            Toast.makeText(this, "Comprobante no disponible", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        pintarComprobante(comprobante)
        configurarAcciones(comprobante)
    }

    private fun pintarComprobante(comprobante: ComprobantePago) {
        val resultado = comprobante.resultado

        findViewById<TextView>(R.id.tvSerieComprobanteHeader).text =
            "Boleta electronica . Serie ${comprobante.serie}"
        findViewById<TextView>(R.id.tvEmpresaComprobante).text = comprobante.empresa
        findViewById<TextView>(R.id.tvRucComprobante).text = "RUC: ${comprobante.ruc}"
        findViewById<TextView>(R.id.tvNumeroComprobante).text =
            "Nro. de operacion ${resultado.numeroOperacion}"
        findViewById<TextView>(R.id.tvFechaComprobante).text =
            "Fecha: ${viewModel.formatearFecha(resultado.fechaHora)}"
        findViewById<TextView>(R.id.tvMetodoComprobante).text = resultado.metodo.etiqueta
        findViewById<TextView>(R.id.tvClienteComprobante).text =
            "CLIENTE\n${comprobante.cliente}\n${comprobante.documentoCliente} . ${comprobante.direccion}"
        findViewById<TextView>(R.id.tvValorVentaComprobante).text =
            "Valor venta: ${viewModel.formatearSoles(viewModel.calcularValorVenta(resultado.monto))}"
        findViewById<TextView>(R.id.tvIgvComprobante).text =
            "IGV 18%: ${viewModel.formatearSoles(viewModel.calcularIgv(resultado.monto))}"
        findViewById<TextView>(R.id.tvTotalComprobante).text =
            "TOTAL PAGADO  ${viewModel.formatearSoles(resultado.monto)}"

        pintarProductos(comprobante)
    }

    private fun pintarProductos(comprobante: ComprobantePago) {
        val contenedor = findViewById<LinearLayout>(R.id.contenedorProductosComprobante)
        contenedor.removeAllViews()
        contenedor.addView(
            texto("DETALLE DE PRODUCTOS", 14f, true, R.color.payment_text_gray)
        )

        comprobante.carrito.items.forEach { item ->
            val fila = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, dp(12), 0, dp(12))
            }
            val detalle = texto(
                "${item.nombre}\n${item.cantidad} x ${viewModel.formatearSoles(item.precio)}",
                14f, false, R.color.payment_text_dark
            )
            detalle.layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            )
            val subtotal = texto(
                viewModel.formatearSoles(item.precio * item.cantidad),
                15f, true, R.color.payment_text_dark
            )
            fila.addView(detalle)
            fila.addView(subtotal)
            contenedor.addView(fila)
            contenedor.addView(View(this).apply {
                setBackgroundResource(R.color.divider_gray)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1
                )
            })
        }
    }

    private fun configurarAcciones(comprobante: ComprobantePago) {
        val compartir = {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "Comprobante FerreMax ${comprobante.resultado.numeroOperacion}"
                )
                putExtra(Intent.EXTRA_TEXT, viewModel.textoComprobante(comprobante))
            }
            startActivity(Intent.createChooser(intent, "Enviar comprobante"))
        }

        findViewById<TextView>(R.id.btnCompartirComprobante).setOnClickListener { compartir() }
        findViewById<MaterialButton>(R.id.btnEnviarComprobante).setOnClickListener { compartir() }
        findViewById<TextView>(R.id.btnImprimirComprobante).setOnClickListener {
            Toast.makeText(this, "Funcion de impresion lista para integrar", Toast.LENGTH_SHORT).show()
        }
        findViewById<MaterialButton>(R.id.btnVolverInicioComprobante).setOnClickListener {
            viewModel.resetearCheckout()
            val intent = Intent(this, CatalogoActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun texto(
        valor: String, sizeSp: Float, bold: Boolean, color: Int
    ): TextView = TextView(this).apply {
        text = valor
        textSize = sizeSp
        setTextColor(getColor(color))
        if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
