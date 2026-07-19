package com.example.ferreteria_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch

class PedidoActivity : AppCompatActivity() {

    private lateinit var adaptador: PedidoAdapter
    private lateinit var viewModel: PedidoViewModel
    private lateinit var tvSinPedidos: TextView
    private lateinit var btnVerHistorial: MaterialButton
    private lateinit var cgFiltroEstado: ChipGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pedido)

        viewModel = ViewModelProvider(this)[PedidoViewModel::class.java]

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvSinPedidos = findViewById(R.id.tvSinPedidos)
        btnVerHistorial = findViewById(R.id.btnVerHistorial)
        cgFiltroEstado = findViewById(R.id.cgFiltroEstado)

        configurarChips()
        configurarRecyclerView()
        configurarNavegacionInferior()

        btnVerHistorial.setOnClickListener {
            viewModel.toggleHistorial()
        }

        observarPedidos()
    }

    private fun configurarChips() {
        val estados = mapOf(
            "ACTIVOS" to "Activos",
            "PREPARACION" to "Preparando",
            "EN_CAMINO" to "En camino",
            "ENTREGADOS" to "Entregados"
        )

        for ((key, label) in estados) {
            val chip = Chip(this)
            chip.text = label
            chip.tag = key
            chip.isCheckable = true
            chip.isChecked = key == "ACTIVOS"
            cgFiltroEstado.addView(chip)
        }

        cgFiltroEstado.setOnCheckedStateChangeListener { _, _ ->
            val chipId = cgFiltroEstado.checkedChipId
            if (chipId != View.NO_ID) {
                val chip = findViewById<Chip>(chipId)
                val key = chip.tag as String
                viewModel.setFiltro(key)
            }
        }
    }

    private fun configurarRecyclerView() {
        val rv = findViewById<RecyclerView>(R.id.rvListaPedidos)
        rv.layoutManager = LinearLayoutManager(this)

        adaptador = PedidoAdapter(
            lista = emptyList(),
            onSeguirPedido = { pedido ->
                val intent = Intent(this, MapaSeguimientoActivity::class.java)
                intent.putExtra(MapaSeguimientoActivity.EXTRA_PEDIDO_ID, pedido.id)
                startActivity(intent)
            }
        )
        rv.adapter = adaptador
    }

    private fun observarPedidos() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.mostrandoHistorial.collect { mostrando ->
                        btnVerHistorial.text = if (mostrando) "Ver menos"
                        else getString(R.string.btn_ver_historial)
                    }
                }
                launch {
                    viewModel.pedidosData.collect { data ->
                        adaptador.actualizarLista(data.pedidosFiltrados)

                        val hayAlgo = data.pedidosFiltrados.isNotEmpty()
                        tvSinPedidos.visibility = if (hayAlgo) View.GONE else View.VISIBLE

                        btnVerHistorial.visibility =
                            if (data.hayMasEntregados) View.VISIBLE
                            else View.GONE
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.iniciarEscucha()
    }

    override fun onStop() {
        super.onStop()
        viewModel.detenerEscucha()
    }

    private fun configurarNavegacionInferior() {
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigationPedidos)
        nav.selectedItemId = R.id.nav_pedidos
        nav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    startActivity(Intent(this, CatalogoActivity::class.java))
                    finish(); true
                }
                R.id.nav_buscar -> {
                    startActivity(Intent(this, BuscarActivity::class.java))
                    finish(); true
                }
                R.id.nav_carrito -> {
                    startActivity(Intent(this, CarritoActivity::class.java))
                    finish(); true
                }
                R.id.nav_perfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                    finish(); true
                }
                else -> true
            }
        }
    }
}
