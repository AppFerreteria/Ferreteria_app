package com.example.ferreteria_app

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BuscarActivity : AppCompatActivity() {

    private lateinit var adaptadorResultados: CatalogoAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var cgFiltrosActivos: ChipGroup
    private lateinit var tvTituloResultados: TextView
    private lateinit var etBuscadorPrincipal: TextInputEditText
    private lateinit var chipAbrirFiltros: Chip

    private var listaProductosMaestra: List<Producto> = emptyList()

    private var textoBusquedaActual: String = ""
    private var filtroCategoria: String? = null
    private var filtroPrecioMaximo: Double? = null
    private var filtroSoloStock: Boolean = false

    private var precioMaximoCatalogo: Float = 1000f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_buscar)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

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
            agregarProductoAlCarrito(producto)
        }
        rvResultados.adapter = adaptadorResultados

        chipAbrirFiltros.setOnClickListener {
            mostrarPanelDeFiltros()
        }

        obtenerNombreUsuario()
        obtenerDatosMaestros()
    }

    private fun obtenerNombreUsuario() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("usuarios").document(uid)
                .get()
                .addOnSuccessListener { documento ->
                    if (documento != null && documento.exists()) {
                        val nombre = documento.getString("nombre")
                        val tvNombreUsuarioBuscar = findViewById<TextView>(R.id.tvNombreUsuarioBuscar)

                        if (!nombre.isNullOrEmpty()) {
                            tvNombreUsuarioBuscar.text = nombre
                        }
                    }
                }
                .addOnFailureListener { excepcion ->
                    Log.e("FirestoreError", "Error al obtener datos de usuario", excepcion)
                }
        }
    }

    private fun configurarBuscador() {
        etBuscadorPrincipal.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textoBusquedaActual = s.toString().trim()
                ejecutarMotorDeFiltros()
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

        val categoriasUnicas = listaProductosMaestra.map { it.categoria }.distinct()
        for (categoria in categoriasUnicas) {
            val chip = Chip(this)
            chip.text = categoria
            chip.isCheckable = true
            if (filtroCategoria == categoria) chip.isChecked = true
            cgFiltrosCategoria.addView(chip)
        }

        sliderPrecio.valueFrom = 0f
        sliderPrecio.valueTo = precioMaximoCatalogo

        val valorActualSlider = filtroPrecioMaximo?.toFloat() ?: precioMaximoCatalogo
        sliderPrecio.value = valorActualSlider.coerceIn(0f, precioMaximoCatalogo)

        if (sliderPrecio.value >= precioMaximoCatalogo) {
            tvPrecioSeleccionado.text = getString(R.string.texto_sin_limite)
        } else {
            tvPrecioSeleccionado.text = getString(R.string.formato_max_precio, sliderPrecio.value)
        }

        sliderPrecio.addOnChangeListener { _, value, _ ->
            if (value >= precioMaximoCatalogo) {
                tvPrecioSeleccionado.text = getString(R.string.texto_sin_limite)
            } else {
                tvPrecioSeleccionado.text = getString(R.string.formato_max_precio, value)
            }
        }

        if (filtroSoloStock) chipSoloStock.isChecked = true

        btnAplicarFiltros.setOnClickListener {
            val chipCatId = cgFiltrosCategoria.checkedChipId
            filtroCategoria = if (chipCatId != View.NO_ID) {
                view.findViewById<Chip>(chipCatId).text.toString()
            } else null

            val precioSeleccionado = sliderPrecio.value
            filtroPrecioMaximo = if (precioSeleccionado >= precioMaximoCatalogo) {
                null
            } else {
                precioSeleccionado.toDouble()
            }

            filtroSoloStock = chipSoloStock.isChecked

            dialog.dismiss()
            dibujarChipsActivos()
            ejecutarMotorDeFiltros()
        }

        dialog.show()
    }

    private fun dibujarChipsActivos() {
        cgFiltrosActivos.removeAllViews()
        cgFiltrosActivos.addView(chipAbrirFiltros)

        filtroCategoria?.let { cat ->
            agregarChipRemovible(cat) {
                filtroCategoria = null
                ejecutarMotorDeFiltros()
            }
        }

        filtroPrecioMaximo?.let { max ->
            agregarChipRemovible(getString(R.string.formato_max_precio, max)) {
                filtroPrecioMaximo = null
                ejecutarMotorDeFiltros()
            }
        }

        if (filtroSoloStock) {
            agregarChipRemovible(getString(R.string.chip_en_stock)) {
                filtroSoloStock = false
                ejecutarMotorDeFiltros()
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

    private fun ejecutarMotorDeFiltros() {
        val listaResultante = listaProductosMaestra.filter { producto ->

            val coincideTexto = textoBusquedaActual.isEmpty() ||
                    producto.nombre.contains(textoBusquedaActual, ignoreCase = true)

            val coincideCategoria = filtroCategoria == null ||
                    producto.categoria == filtroCategoria

            val coincidePrecio = filtroPrecioMaximo == null ||
                    producto.precio <= filtroPrecioMaximo!!

            val coincideStock = !filtroSoloStock ||
                    producto.stock > 0

            coincideTexto && coincideCategoria && coincidePrecio && coincideStock
        }

        tvTituloResultados.text = if (textoBusquedaActual.isNotEmpty()) {
            getString(R.string.formato_resultados_para, textoBusquedaActual)
        } else {
            getString(R.string.titulo_explorar_productos)
        }

        adaptadorResultados.actualizarLista(listaResultante)
    }

    private fun obtenerDatosMaestros() {
        db.collection("productos").get()
            .addOnSuccessListener { resultado ->
                val listaDescargada = mutableListOf<Producto>()
                for (documento in resultado) {
                    val producto = documento.toObject(Producto::class.java).copy(id = documento.id)
                    listaDescargada.add(producto)
                }
                listaProductosMaestra = listaDescargada

                val maxPrecioDb = listaProductosMaestra.maxOfOrNull { it.precio }?.toFloat() ?: 1000f
                precioMaximoCatalogo = if (maxPrecioDb > 0f) maxPrecioDb else 1000f

                ejecutarMotorDeFiltros()
            }
            .addOnFailureListener { excepcion ->
                Log.e("BuscarError", "Fallo al obtener base de datos", excepcion)
            }
    }

    private fun configurarNavegacionInferior() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigationBuscar)
        bottomNavigation.selectedItemId = R.id.nav_buscar

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    val intent = Intent(this, CatalogoActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_buscar -> true

                R.id.nav_carrito -> {
                    val intent = Intent(this, CarritoActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }

                R.id.nav_perfil -> {
                    val intent = Intent(this, PerfilActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun agregarProductoAlCarrito(producto: Producto) {
        val repo = CarritoRepository()
        repo.agregarProducto(
            producto = producto,
            onSuccess = {
                runOnUiThread {
                    Toast.makeText(this, R.string.toast_producto_agregado, Toast.LENGTH_SHORT).show()
                }
            },
            onError = { mensaje ->
                runOnUiThread {
                    Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}