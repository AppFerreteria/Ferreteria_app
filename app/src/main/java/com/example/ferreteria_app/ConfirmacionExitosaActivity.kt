package com.example.ferreteria_app

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConfirmacionExitosaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_confirmacion_exitosa)

        val pedidoId = intent.getStringExtra("pedidoId") ?: ""
        val numeroPedido = intent.getStringExtra("numeroPedido") ?: "#P-0000"
        val nombreRepartidor = intent.getStringExtra("nombreRepartidor") ?: "Repartidor"

        findViewById<TextView>(R.id.tvIdPedidoSuccess).text = numeroPedido
        findViewById<TextView>(R.id.tvNombreRepartidorSuccess).text = nombreRepartidor
        
        val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        findViewById<TextView>(R.id.tvFechaSuccess).text = sdfDate.format(Date())

        val sdfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
        findViewById<TextView>(R.id.tvHoraSuccess).text = sdfTime.format(Date())

        findViewById<MaterialButton>(R.id.btnVerDetalle).setOnClickListener {
            val intent = Intent(this, MapaSeguimientoActivity::class.java)
            intent.putExtra(MapaSeguimientoActivity.EXTRA_PEDIDO_ID, pedidoId)
            startActivity(intent)
            finish()
        }

        findViewById<MaterialButton>(R.id.btnVolverPedidos).setOnClickListener {
            val intent = Intent(this, PedidoActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}