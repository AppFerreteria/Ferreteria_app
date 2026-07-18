package com.example.ferreteria_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class RepartidorPedidosActivity : AppCompatActivity() {

    private lateinit var adaptador: PedidoAdapter
    private lateinit var viewModel: PedidoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_repartidor_pedidos)

        viewModel = ViewModelProvider(this)[PedidoViewModel::class.java]

        findViewById<View>(R.id.btnCerrarSesionRepartidor).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, PrincipalActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        configurarRecyclerView()
        observarPedidos()
    }

    private fun configurarRecyclerView() {
        val rv = findViewById<RecyclerView>(R.id.rvPedidosRepartidor)
        rv.layoutManager = LinearLayoutManager(this)

        adaptador = PedidoAdapter(
            lista = emptyList(),
            isRepartidor = true,
            onSeguirPedido = { pedido ->
                // Este botón ahora abre la generación de QR si el pedido ya está en camino
                val intent = Intent(this, GenerarQRActivity::class.java)
                intent.putExtra("pedidoId", pedido.id)
                startActivity(intent)
            },
            onAceptarPedido = { pedido ->
                // Cambiar estado a "En camino"
                viewModel.actualizarEstado(pedido.id, EstadoPedido.EN_CAMINO)
            }
        )
        rv.adapter = adaptador
    }

    private fun observarPedidos() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pedidosState.collect { pedidos ->
                    android.util.Log.d("UIRepartidor", "Pedidos recibidos en la UI: ${pedidos.size}")
                    // Forzamos la actualización completa para asegurar que el botón cambie
                    adaptador = PedidoAdapter(
                        lista = pedidos,
                        isRepartidor = true,
                        onSeguirPedido = { pedido ->
                            val intent = Intent(this@RepartidorPedidosActivity, GenerarQRActivity::class.java)
                            intent.putExtra("pedidoId", pedido.id)
                            startActivity(intent)
                        },
                        onAceptarPedido = { pedido ->
                            viewModel.actualizarEstado(pedido.id, EstadoPedido.EN_CAMINO)
                        }
                    )
                    findViewById<RecyclerView>(R.id.rvPedidosRepartidor).adapter = adaptador
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.iniciarEscuchaRepartidor()
    }

    override fun onStop() {
        super.onStop()
        viewModel.detenerEscucha()
    }
}