package com.example.ferreteria_app

import android.content.Intent
import android.os.Bundle
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class CarritoActivity : AppCompatActivity() {

    private lateinit var adaptador: CarritoAdapter
    private lateinit var viewModel: CarritoViewModel

    private lateinit var tvTotalFinal: TextView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvDescuento: TextView
    private lateinit var tvEnvio: TextView
    private var carritoActual: Carrito = Carrito()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_carrito)

        viewModel = ViewModelProvider(this)[CarritoViewModel::class.java]

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvTotalFinal = findViewById(R.id.tvTotalFinal)
        tvSubtotal = findViewById(R.id.tvSubtotal)
        tvDescuento = findViewById(R.id.tvDescuento)
        tvEnvio = findViewById(R.id.tvEnvio)

        configurarRecyclerView()
        configurarNavegacionInferior()
        observarCarrito()

        findViewById<MaterialButton>(R.id.btnContinuarCompra).setOnClickListener {
            if (carritoActual.items.isEmpty()) {
                Toast.makeText(this, "Tu carrito esta vacio", Toast.LENGTH_SHORT).show()
            } else {
                CheckoutSession.iniciar(carritoActual)
                startActivity(Intent(this, ResumenPedidoActivity::class.java))
            }
        }
    }

    private fun configurarRecyclerView() {
        val rv = findViewById<RecyclerView>(R.id.rvListaCarrito)
        rv.layoutManager = LinearLayoutManager(this)

        adaptador = CarritoAdapter(
            lista = emptyList(),
            onCambiarCantidad = { item, nuevaCantidad ->
                viewModel.actualizarCantidad(item, nuevaCantidad)
            },
            onEliminar = { item -> viewModel.eliminarItem(item) }
        )
        rv.adapter = adaptador
    }

    private fun observarCarrito() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.carritoState.collect { carrito ->
                    carritoActual = carrito
                    adaptador.actualizarLista(carrito.items)
                    tvTotalFinal.text = getString(R.string.formato_precio, carrito.total)
                    tvSubtotal.text = getString(R.string.formato_precio, carrito.subtotal)
                    tvDescuento.text = getString(R.string.formato_precio, carrito.descuento)
                    tvEnvio.text = getString(R.string.formato_precio, carrito.costoEnvio)
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
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigationCarrito)
        nav.selectedItemId = R.id.nav_carrito
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
                R.id.nav_pedidos -> {
                    startActivity(Intent(this, PedidoActivity::class.java))
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
