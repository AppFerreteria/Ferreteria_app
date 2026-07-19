package com.example.ferreteria_app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class CatalogoActivity : AppCompatActivity() {

    private lateinit var adaptadorProductos: CatalogoAdapter
    private lateinit var adaptadorCategorias: CategoriasAdapter
    private lateinit var viewModel: CatalogoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_catalogo)

        viewModel = ViewModelProvider(this)[CatalogoViewModel::class.java]

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        configurarNavegacionInferior()

        val rvCategorias = findViewById<RecyclerView>(R.id.rvCategorias)
        rvCategorias.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adaptadorCategorias = CategoriasAdapter(emptyList()) { categoria ->
            viewModel.filtrarPorCategoria(categoria)
        }
        rvCategorias.adapter = adaptadorCategorias

        val rvProductos = findViewById<RecyclerView>(R.id.rvContenedorPrincipal)
        rvProductos.layoutManager = GridLayoutManager(this, 2)
        adaptadorProductos = CatalogoAdapter(emptyList()) { producto ->
            viewModel.agregarProductoAlCarrito(producto) { success, mensaje ->
                runOnUiThread {
                    Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
                }
            }
        }
        rvProductos.adapter = adaptadorProductos

        observarEstado()
        viewModel.cargarDatos()
        solicitarPermisoNotificacionesYToken()
    }

    private fun observarEstado() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {}
                        is UiState.Success -> {
                            adaptadorCategorias.actualizarLista(state.data.categorias)
                            adaptadorProductos.actualizarLista(state.data.productos)
                            val tvNombre = findViewById<TextView>(R.id.tvNombreUsuario)
                            if (state.data.nombreUsuario.isNotEmpty()) {
                                tvNombre.text = state.data.nombreUsuario
                            }
                        }
                        is UiState.Error -> {
                            Toast.makeText(this@CatalogoActivity, state.mensaje, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun configurarNavegacionInferior() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_inicio

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> true
                R.id.nav_buscar -> {
                    startActivity(Intent(this, BuscarActivity::class.java))
                    true
                }
                R.id.nav_carrito -> {
                    startActivity(Intent(this, CarritoActivity::class.java))
                    true
                }
                R.id.nav_pedidos -> {
                    startActivity(Intent(this, PedidoActivity::class.java))
                    true
                }
                R.id.nav_perfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun solicitarPermisoNotificacionesYToken() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            } else {
                viewModel.solicitarPermisoNotificacionesYToken()
            }
        } else {
            viewModel.solicitarPermisoNotificacionesYToken()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.solicitarPermisoNotificacionesYToken()
        }
    }
}
