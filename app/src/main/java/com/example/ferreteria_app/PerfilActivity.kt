package com.example.ferreteria_app

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class PerfilActivity : AppCompatActivity() {

    private lateinit var viewModel: PerfilViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil)

        viewModel = ViewModelProvider(this)[PerfilViewModel::class.java]

        configurarNavegacionInferior()

        findViewById<MaterialButton>(R.id.btnEditProfile).setOnClickListener {
            startActivity(Intent(this, EditarPerfilActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnLogout).setOnClickListener {
            viewModel.cerrarSesion()
            FirebaseAuth.getInstance().signOut()
            SessionManager.cerrarSesionRepartidor(this)
            val intent = Intent(this, PrincipalActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            Toast.makeText(this, getString(R.string.toast_cierre_sesion), Toast.LENGTH_SHORT).show()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {}
                        is UiState.Success -> {
                            val cliente = state.data
                            findViewById<TextView>(R.id.tvNombrePerfil).text = cliente.nombre
                            findViewById<TextView>(R.id.tvEmailPerfil).text = viewModel.email
                            findViewById<TextView>(R.id.tvTelefonoPerfil).text =
                                if (cliente.telefono.isNotEmpty()) cliente.telefono
                                else getString(R.string.texto_sin_telefono)
                        }
                        is UiState.Error -> {
                            Toast.makeText(this@PerfilActivity, state.mensaje, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.cargarDatos()
    }

    private fun configurarNavegacionInferior() {
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigationPerfil)
        nav.selectedItemId = R.id.nav_perfil
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
                R.id.nav_pedidos -> {
                    startActivity(Intent(this, PedidoActivity::class.java))
                    finish(); true
                }
                else -> true
            }
        }
    }
}
