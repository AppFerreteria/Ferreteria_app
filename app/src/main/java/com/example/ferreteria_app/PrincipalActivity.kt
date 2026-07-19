package com.example.ferreteria_app

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class PrincipalActivity : AppCompatActivity() {

    private lateinit var viewModel: PrincipalViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[PrincipalViewModel::class.java]

        if (SessionManager.sesionRepartidorActiva(this)) {
            startActivity(Intent(this, BuscarEntregaActivity::class.java))
            finish()
            return
        }

        val usuarioAnonimo = FirebaseAuth.getInstance().currentUser?.isAnonymous == true
        if (usuarioAnonimo) {
            FirebaseAuth.getInstance().signOut()
            SessionManager.cerrarSesionRepartidor(this)
            mostrarVistaPrincipal()
            return
        }

        if (!viewModel.sesionActiva) {
            mostrarVistaPrincipal()
            return
        }

        viewModel.verificarSesionYRol()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.rol.collect { rol ->
                    when (rol) {
                        RolUsuario.CLIENTE -> {
                            startActivity(Intent(this@PrincipalActivity, CatalogoActivity::class.java))
                            finish()
                        }
                        RolUsuario.REPARTIDOR -> {
                            startActivity(Intent(this@PrincipalActivity, BuscarEntregaActivity::class.java))
                            finish()
                        }
                        null -> mostrarVistaPrincipal()
                    }
                }
            }
        }
    }

    private fun mostrarVistaPrincipal() {
        enableEdgeToEdge()
        setContentView(R.layout.activity_principal)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<MaterialButton>(R.id.btnClient).setOnClickListener {
            startActivity(Intent(this, ClienteActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnDelivery).setOnClickListener {
            startActivity(Intent(this, LoginRepartidorActivity::class.java))
        }
    }
}
