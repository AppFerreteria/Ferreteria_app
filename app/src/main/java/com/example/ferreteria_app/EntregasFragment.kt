package com.example.ferreteria_app

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class EntregasFragment : Fragment(R.layout.fragment_entregas) {

    private lateinit var viewModel: EntregasViewModel
    private lateinit var cvEntregaActiva: View
    private lateinit var llSinEntregas: View
    private lateinit var tvNumeroPedido: TextView
    private lateinit var tvBadgeEstado: TextView
    private lateinit var tvDireccion: TextView
    private lateinit var tvDescripcion: TextView
    private lateinit var tvTotal: TextView
    private lateinit var btnAccion: MaterialButton
    private lateinit var btnVerHistorial: MaterialButton
    private lateinit var tvNombreRepartidor: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[EntregasViewModel::class.java]

        cvEntregaActiva = view.findViewById(R.id.cvEntregaActiva)
        llSinEntregas = view.findViewById(R.id.llSinEntregas)
        tvNumeroPedido = view.findViewById(R.id.tvNumeroPedidoEntrega)
        tvBadgeEstado = view.findViewById(R.id.tvBadgeEstadoEntrega)
        tvDireccion = view.findViewById(R.id.tvDireccionEntrega)
        tvDescripcion = view.findViewById(R.id.tvDescripcionEntrega)
        tvTotal = view.findViewById(R.id.tvTotalEntrega)
        btnAccion = view.findViewById(R.id.btnAccionEntrega)
        btnVerHistorial = view.findViewById(R.id.btnVerHistorial)
        tvNombreRepartidor = view.findViewById(R.id.tvNombreRepartidor)

        val nombreGuardado = SessionManager.getRepartidorNombre(requireContext())
        if (nombreGuardado.isNotEmpty()) {
            tvNombreRepartidor.text = nombreGuardado
        }

        btnAccion.setOnClickListener {
            val entrega = viewModel.uiState.value.entrega ?: return@setOnClickListener
            when (entrega.estado) {
                EstadoPedido.EN_CAMINO -> viewModel.marcarEntregado()
                else -> viewModel.marcarEnCamino()
            }
        }

        btnVerHistorial.setOnClickListener {
            android.widget.Toast.makeText(
                requireContext(),
                "Historial proximamente",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }

        observarEstado()
        viewModel.cargarEntregaActual()
    }

    override fun onResume() {
        super.onResume()
        viewModel.cargarEntregaActual()
    }

    private fun observarEstado() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { estado ->
                    val entrega = estado.entrega
                    if (entrega != null) {
                        cvEntregaActiva.visibility = View.VISIBLE
                        llSinEntregas.visibility = View.GONE
                        pintarEntrega(entrega)
                    } else if (estado.sinEntregas) {
                        cvEntregaActiva.visibility = View.GONE
                        llSinEntregas.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun pintarEntrega(pedido: Pedido) {
        tvNumeroPedido.text = getString(R.string.label_numero_pedido, pedido.numeroPedido)
        tvDireccion.text = if (pedido.direccionEntrega.isNotEmpty()) {
            pedido.direccionEntrega
        } else {
            "Sin direccion registrada"
        }
        tvDescripcion.text = if (pedido.items.isNotEmpty()) {
            pedido.items.joinToString(separator = ", ") { "${it.cantidad}x ${it.nombre}" }
        } else {
            pedido.descripcionItems
        }
        tvTotal.text = getString(R.string.formato_precio, pedido.total)

        when (pedido.estado) {
            EstadoPedido.PENDIENTE, EstadoPedido.PREPARACION -> {
                tvBadgeEstado.text = "Preparacion"
                btnAccion.text = "Iniciar Entrega"
            }
            EstadoPedido.EN_CAMINO -> {
                tvBadgeEstado.text = "En camino"
                btnAccion.text = "Marcar Entregado"
            }
            else -> {
                tvBadgeEstado.text = pedido.estado
                btnAccion.visibility = View.GONE
            }
        }
    }
}
