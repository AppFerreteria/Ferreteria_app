package com.example.ferreteria_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class ComprobanteActivity : AppCompatActivity() {
    private lateinit var comprobante: ComprobantePago

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_comprobante_pago)

        val comprobanteActual = CheckoutSession.comprobantePago
        if (comprobanteActual == null) {
            Toast.makeText(this, "Comprobante no disponible", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        comprobante = comprobanteActual
        pintarComprobante()
        configurarAcciones()
    }

    private fun pintarComprobante() {
        val resultado = comprobante.resultado
        findViewById<TextView>(R.id.tvSerieComprobanteHeader).text = "${comprobante.tipo.etiqueta} · Serie ${comprobante.serie}"
        findViewById<TextView>(R.id.tvEmpresaComprobante).text = comprobante.empresa
        findViewById<TextView>(R.id.tvRucComprobante).text = "RUC: ${comprobante.ruc}"
        findViewById<TextView>(R.id.tvNumeroComprobante).text = "Nro. de operación ${resultado.numeroOperacion}"
        findViewById<TextView>(R.id.tvFechaComprobante).text = "Fecha: ${PagoLogic.formatearFecha(resultado.fechaHora)}"
        findViewById<TextView>(R.id.tvMetodoComprobante).text = resultado.metodo.etiqueta
        findViewById<TextView>(R.id.tvClienteComprobante).text = "CLIENTE\n${comprobante.cliente}\n${comprobante.documentoCliente} · ${comprobante.direccion}"
        findViewById<TextView>(R.id.tvValorVentaComprobante).text = "Valor venta: ${PagoLogic.formatearSoles(PagoLogic.calcularValorVenta(resultado.monto))}"
        findViewById<TextView>(R.id.tvIgvComprobante).text = "IGV 18%: ${PagoLogic.formatearSoles(PagoLogic.calcularIgv(resultado.monto))}"
        findViewById<TextView>(R.id.tvTotalComprobante).text = "TOTAL PAGADO  ${PagoLogic.formatearSoles(resultado.monto)}"
        pintarProductos()
    }

    private fun pintarProductos() {
        val contenedor = findViewById<LinearLayout>(R.id.contenedorProductosComprobante)
        contenedor.removeAllViews()
        contenedor.addView(texto("DETALLE DE PRODUCTOS", 14f, true, R.color.payment_text_gray))
        comprobante.carrito.items.forEach { item ->
            val fila = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, dp(12), 0, dp(12))
            }
            val detalle = texto("${item.nombre}\n${item.cantidad} x ${PagoLogic.formatearSoles(item.precio)}", 14f, false, R.color.payment_text_dark)
            detalle.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            val subtotal = texto(PagoLogic.formatearSoles(item.precio * item.cantidad), 15f, true, R.color.payment_text_dark)
            fila.addView(detalle)
            fila.addView(subtotal)
            contenedor.addView(fila)
            contenedor.addView(View(this).apply {
                setBackgroundResource(R.color.divider_gray)
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
            })
        }
    }

    private fun configurarAcciones() {
        val compartir = {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Comprobante FerreMax ${comprobante.resultado.numeroOperacion}")
                putExtra(Intent.EXTRA_TEXT, PagoLogic.textoComprobante(comprobante))
            }
            startActivity(Intent.createChooser(intent, "Enviar comprobante"))
        }
        findViewById<TextView>(R.id.btnCompartirComprobante).setOnClickListener { compartir() }
        findViewById<MaterialButton>(R.id.btnEnviarComprobante).setOnClickListener { compartir() }
        findViewById<TextView>(R.id.btnImprimirComprobante).setOnClickListener {
            Toast.makeText(this, "Función de impresión lista para integrar", Toast.LENGTH_SHORT).show()
        }
        findViewById<MaterialButton>(R.id.btnVolverInicioComprobante).setOnClickListener {
            CheckoutSession.resetear()
            val intent = Intent(this, CatalogoActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun texto(valor: String, sizeSp: Float, bold: Boolean, color: Int): TextView = TextView(this).apply {
        text = valor
        textSize = sizeSp
        setTextColor(getColor(color))
        if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}