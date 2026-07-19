package com.example.ferreteria_app

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class BuscarActivity : AppCompatActivity() {

    private lateinit var adaptadorResultados: CatalogoAdapter
    private lateinit var viewModel: BuscarViewModel
    private lateinit var cgFiltrosActivos: ChipGroup
    private lateinit var tvTituloResultados: TextView
    private lateinit var etBuscadorPrincipal: TextInputEditText
    private lateinit var chipAbrirFiltros: Chip

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_buscar)

        viewModel = ViewModelProvider(this)[BuscarViewModel::class.java]

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        cgFiltrosActivos = findViewById(R.id.cgFiltrosActivos)
        tvTituloResultados = findViewById(R.id.tvTituloResultados)
        etBuscadorPrincipal = findViewById(R.id.etBuscadorPrincipal)
        chipAbrirFiltros = findViewById(R.id.chipAbrirFiltros)

        configurarNavegacionInferior()
        configurarBuscador()

        val rvResultados = findViewById<RecyclerView>(R.id.rvResultadosBusqueda)
        rvResultados.layoutManager = GridLayoutManager(this, 2)
        adaptadorResultados = CatalogoAdapter(emptyList()) { producto ->
            viewModel.agregarProductoAlCarrito(producto) { success, mensaje ->
                runOnUiThread {
                    Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
                }
            }
        }
        rvResultados.adapter = adaptadorResultados

        chipAbrirFiltros.setOnClickListener { mostrarPanelDeFiltros() }

        observarEstado()
        viewModel.cargarDatos()
    }

    private fun observarEstado() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {}
                        is UiState.Success -> {
                            adaptadorResultados.actualizarLista(state.data.productos)

                            val nombre = state.data.nombreUsuario
                            if (nombre.isNotEmpty()) {
                                findViewById<TextView>(R.id.tvNombreUsuarioBuscar).text = nombre
                            }

                            tvTituloResultados.text = if (state.data.textoBusqueda.isNotEmpty()) {
                                getString(R.string.formato_resultados_para, state.data.textoBusqueda)
                            } else {
                                getString(R.string.titulo_explorar_productos)
                            }
                        }
                        is UiState.Error -> {
                            Toast.makeText(this@BuscarActivity, state.mensaje, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun configurarBuscador() {
        etBuscadorPrincipal.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.buscar(s.toString().trim())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun mostrarPanelDeFiltros() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_filtros, null)
        dialog.setContentView(view)

        val cgFiltrosCategoria = view.findViewById<ChipGroup>(R.id.cgFiltrosCategoria)
        val sliderPrecio = view.findViewById<Slider>(R.id.sliderPrecio)
        val tvPrecioSeleccionado = view.findViewById<TextView>(R.id.tvPrecioSeleccionado)
        val chipSoloStock = view.findViewById<Chip>(R.id.chipSoloStock)
        val btnAplicarFiltros = view.findViewById<MaterialButton>(R.id.btnAplicarFiltros)

        val currentState = viewModel.uiState.value
        val currentData = (currentState as? UiState.Success)?.data
        val categorias = viewModel.categorias.value

        for (categoria in categorias) {
            val chip = Chip(this)
            chip.text = categoria
            chip.isCheckable = true
            if (currentData?.filtroCategoria == categoria) chip.isChecked = true
            cgFiltrosCategoria.addView(chip)
        }

        val precioMaximo = viewModel.precioMaximoCatalogo.value
        sliderPrecio.valueFrom = 0f
        sliderPrecio.valueTo = precioMaximo

        val valorActualSlider = currentData?.filtroPrecioMaximo?.toFloat() ?: precioMaximo
        sliderPrecio.value = valorActualSlider.coerceIn(0f, precioMaximo)

        if (sliderPrecio.value >= precioMaximo) {
            tvPrecioSeleccionado.text = getString(R.string.texto_sin_limite)
        } else {
            tvPrecioSeleccionado.text = getString(R.string.formato_max_precio, sliderPrecio.value)
        }

        sliderPrecio.addOnChangeListener { _, value, _ ->
            if (value >= precioMaximo) {
                tvPrecioSeleccionado.text = getString(R.string.texto_sin_limite)
            } else {
                tvPrecioSeleccionado.text = getString(R.string.formato_max_precio, value)
            }
        }

        chipSoloStock.isChecked = currentData?.filtroSoloStock ?: false

        btnAplicarFiltros.setOnClickListener {
            val chipCatId = cgFiltrosCategoria.checkedChipId
            val filtroCategoria = if (chipCatId != View.NO_ID) {
                view.findViewById<Chip>(chipCatId).text.toString()
            } else null

            val precioSeleccionado = sliderPrecio.value
            val filtroPrecioMaximo = if (precioSeleccionado >= precioMaximo) null
            else precioSeleccionado.toDouble()

            val filtroSoloStock = chipSoloStock.isChecked

            dialog.dismiss()
            viewModel.aplicarFiltros(filtroCategoria, filtroPrecioMaximo, filtroSoloStock)
            dibujarChipsActivos()
        }

        dialog.show()
    }

    private fun dibujarChipsActivos() {
        cgFiltrosActivos.removeAllViews()
        cgFiltrosActivos.addView(chipAbrirFiltros)

        val currentData = (viewModel.uiState.value as? UiState.Success)?.data ?: return

        currentData.filtroCategoria?.let { cat ->
            agregarChipRemovible(cat) {
                viewModel.quitarFiltroCategoria()
            }
        }

        currentData.filtroPrecioMaximo?.let { max ->
            agregarChipRemovible(getString(R.string.formato_max_precio, max)) {
                viewModel.quitarFiltroPrecio()
            }
        }

        if (currentData.filtroSoloStock) {
            agregarChipRemovible(getString(R.string.chip_en_stock)) {
                viewModel.quitarFiltroStock()
            }
        }
    }

    private fun agregarChipRemovible(texto: String, alCerrar: () -> Unit) {
        val chip = Chip(this)
        chip.text = texto
        chip.isCloseIconVisible = true
        chip.setChipBackgroundColorResource(android.R.color.background_light)
        chip.setOnCloseIconClickListener {
            cgFiltrosActivos.removeView(chip)
            alCerrar()
        }
        cgFiltrosActivos.addView(chip)
    }

    private fun configurarNavegacionInferior() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigationBuscar)
        bottomNavigation.selectedItemId = R.id.nav_buscar

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    startActivity(Intent(this, CatalogoActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })
                    finish()
                    true
                }
                R.id.nav_buscar -> true
                R.id.nav_carrito -> {
                    startActivity(Intent(this, CarritoActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_pedidos -> {
                    startActivity(Intent(this, PedidoActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_perfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}
