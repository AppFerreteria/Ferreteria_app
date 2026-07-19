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

class ResumenPedidoActivity : AppCompatActivity() {

    private lateinit var viewModel: ResumenPedidoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_resumen_pedido)

        viewModel = ViewModelProvider(this)[ResumenPedidoViewModel::class.java]

        if (viewModel.estaVacio()) {
            Toast.makeText(this, "No hay productos para pagar", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val carrito = viewModel.carrito

        findViewById<TextView>(R.id.btnBackResumen).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tvNumeroPedido).text = viewModel.numeroPedido
        findViewById<TextView>(R.id.tvValorVentaResumen).text = "Valor venta: ${
            viewModel.formatearSoles(viewModel.calcularValorVenta(carrito.total))
        }"
        findViewById<TextView>(R.id.tvIgvResumen).text = "IGV 18%: ${
            viewModel.formatearSoles(viewModel.calcularIgv(carrito.total))
        }"
        findViewById<TextView>(R.id.tvTotalResumen).text = "Total: ${
            viewModel.formatearSoles(carrito.total)
        }"

        pintarProductos(carrito)

        findViewById<MaterialButton>(R.id.btnContinuarPago).setOnClickListener {
            startActivity(Intent(this, MetodoPagoActivity::class.java))
        }
    }

    private fun pintarProductos(carrito: Carrito) {
        val contenedor = findViewById<LinearLayout>(R.id.contenedorProductosResumen)
        contenedor.removeAllViews()
        contenedor.addView(
            texto(
                "Productos (${carrito.items.sumOf { it.cantidad }})",
                18f, true, R.color.payment_text_dark
            )
        )
        carrito.items.forEach { item ->
            val fila = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, dp(12), 0, dp(12))
            }
            val detalle = texto(
                "${item.nombre}\nCant: ${item.cantidad} x ${viewModel.formatearSoles(item.precio)}",
                14f, false, R.color.payment_text_gray
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
