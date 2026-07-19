package com.example.ferreteria_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class BuscarEntregaFragment : Fragment(R.layout.fragment_buscar_entrega) {

    private lateinit var viewModel: BuscarEntregaViewModel
    private lateinit var etNumeroOrden: EditText
    private lateinit var btnBuscar: MaterialButton
    private lateinit var llEstadoInicial: View
    private lateinit var llContenedorCarga: View
    private lateinit var llResultadoEncontrado: View
    private lateinit var llSinResultados: View
    private lateinit var tvNumeroPedidoEncontrado: TextView
    private lateinit var tvDescripcionEncontrada: TextView
    private lateinit var rvItemsEncontrados: RecyclerView
    private lateinit var btnIniciarEntrega: MaterialButton
    private lateinit var btnMarcarEntregado: MaterialButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[BuscarEntregaViewModel::class.java]

        etNumeroOrden = view.findViewById(R.id.etNumeroOrden)
        btnBuscar = view.findViewById(R.id.btnBuscarOrden)
        llEstadoInicial = view.findViewById(R.id.llEstadoInicial)
        llContenedorCarga = view.findViewById(R.id.llContenedorCarga)
        llResultadoEncontrado = view.findViewById(R.id.llResultadoEncontrado)
        llSinResultados = view.findViewById(R.id.llSinResultados)
        tvNumeroPedidoEncontrado = view.findViewById(R.id.tvNumeroPedidoEncontrado)
        tvDescripcionEncontrada = view.findViewById(R.id.tvDescripcionEncontrada)
        rvItemsEncontrados = view.findViewById(R.id.rvItemsEncontrados)
        btnIniciarEntrega = view.findViewById(R.id.btnIniciarEntrega)
        btnMarcarEntregado = view.findViewById(R.id.btnMarcarEntregadoBusqueda)

        rvItemsEncontrados.layoutManager = LinearLayoutManager(requireContext())

        view.findViewById<TextView>(R.id.tvEmpresaLogo).setOnClickListener {
            viewModel.limpiarBusqueda()
            etNumeroOrden.text?.clear()
        }

        btnBuscar.setOnClickListener {
            val orden = etNumeroOrden.text.toString().trim()
            viewModel.buscarOrden(orden)
        }

        btnIniciarEntrega.setOnClickListener {
            val pedido = viewModel.uiState.value.pedido ?: return@setOnClickListener
            viewModel.iniciarEntrega(pedido.id)
        }

        btnMarcarEntregado.setOnClickListener {
            val pedido = viewModel.uiState.value.pedido ?: return@setOnClickListener
            viewModel.marcarEntregado(pedido.id)
        }

        observarEstado()
    }

    private fun observarEstado() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { data ->
                    llEstadoInicial.visibility = View.GONE
                    llContenedorCarga.visibility = View.GONE
                    llResultadoEncontrado.visibility = View.GONE
                    llSinResultados.visibility = View.GONE

                    when {
                        data.estaBuscando -> {
                            llContenedorCarga.visibility = View.VISIBLE
                        }
                        data.sinResultados -> {
                            llSinResultados.visibility = View.VISIBLE
                        }
                        data.pedido != null -> {
                            llResultadoEncontrado.visibility = View.VISIBLE
                            pintarPedido(data.pedido)
                        }
                        else -> {
                            llEstadoInicial.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    private fun pintarPedido(pedido: Pedido) {
        tvNumeroPedidoEncontrado.text =
            getString(R.string.label_numero_pedido, pedido.numeroPedido)

        val textoItems = if (pedido.items.isNotEmpty()) {
            "${pedido.items.size} items encontrados"
        } else {
            pedido.descripcionItems
        }
        tvDescripcionEncontrada.text = textoItems

        val adapter = PedidoAdapter(
            lista = listOf(pedido),
            onSeguirPedido = { }
        )
        rvItemsEncontrados.adapter = adapter

        val uid = SessionManager.getRepartidorId(requireContext())
        val esMiPedido = pedido.repartidorId == uid || pedido.repartidorId.isBlank()

        when {
            pedido.estado == EstadoPedido.ENTREGADO -> {
                btnIniciarEntrega.visibility = View.GONE
                btnMarcarEntregado.visibility = View.GONE
            }
            pedido.estado == EstadoPedido.EN_CAMINO && esMiPedido -> {
                btnIniciarEntrega.visibility = View.GONE
                btnMarcarEntregado.visibility = View.VISIBLE
            }
            esMiPedido -> {
                btnIniciarEntrega.visibility = View.VISIBLE
                btnIniciarEntrega.text = if (pedido.repartidorId.isBlank()) "Tomar Pedido"
                else "Iniciar Entrega"
                btnMarcarEntregado.visibility = View.GONE
            }
            else -> {
                btnIniciarEntrega.visibility = View.GONE
                btnMarcarEntregado.visibility = View.GONE
            }
        }
    }
}
