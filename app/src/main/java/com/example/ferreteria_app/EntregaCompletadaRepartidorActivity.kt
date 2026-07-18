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

class EntregaCompletadaRepartidorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_entrega_completada_repartidor)

        val numeroPedido = intent.getStringExtra("numeroPedido") ?: "#P-0000"
        val nombreCliente = intent.getStringExtra("nombreCliente") ?: "Cliente"

        findViewById<TextView>(R.id.tvIdPedidoSuccessRepartidor).text = numeroPedido
        findViewById<TextView>(R.id.tvNombreClienteSuccess).text = nombreCliente
        
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        findViewById<TextView>(R.id.tvHoraSuccessRepartidor).text = sdf.format(Date())

        findViewById<MaterialButton>(R.id.btnVerSiguientePedido).setOnClickListener {
            val intent = Intent(this, RepartidorPedidosActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}