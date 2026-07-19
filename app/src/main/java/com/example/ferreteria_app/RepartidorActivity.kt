package com.example.ferreteria_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class RepartidorActivity : AppCompatActivity() {

    private lateinit var viewModel: RepartidorViewModel
    private lateinit var adaptador: RepartidorPedidoAdapter
    private lateinit var tvSinPedidos: TextView
    private lateinit var tvNombreRepartidor: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_repartidor)

        viewModel = ViewModelProvider(this)[RepartidorViewModel::class.java]

        tvNombreRepartidor = findViewById(R.id.tvNombreRepartidor)
        tvSinPedidos = findViewById(R.id.tvSinPedidosRepartidor)

        val rv = findViewById<RecyclerView>(R.id.rvPedidosRepartidor)
        rv.layoutManager = LinearLayoutManager(this)

        val uid = SessionManager.getRepartidorId(this)
        adaptador = RepartidorPedidoAdapter(
            lista = emptyList(),
            repartidorUid = uid,
            onTomarPedido = { pedido -> viewModel.tomarPedido(pedido.id) },
            onEnCamino = { pedido -> viewModel.marcarEnCamino(pedido.id) },
            onEntregado = { pedido -> viewModel.marcarEntregado(pedido.id) }
        )
        rv.adapter = adaptador

        findViewById<MaterialButton>(R.id.btnCerrarSesionRepartidor).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            SessionManager.cerrarSesionRepartidor(this)
            val intent = Intent(this, PrincipalActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {}
                        is UiState.Success -> {
                            val data = state.data
                            if (data.nombreRepartidor.isNotEmpty()) {
                                tvNombreRepartidor.text = data.nombreRepartidor
                            }
                            adaptador.actualizarLista(data.pedidos)
                            tvSinPedidos.visibility =
                                if (data.pedidos.isEmpty()) View.VISIBLE else View.GONE
                        }
                        is UiState.Error -> {
                            Toast.makeText(this@RepartidorActivity, state.mensaje, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.cargarPedidos()
        viewModel.guardarFCMToken()
    }
}
