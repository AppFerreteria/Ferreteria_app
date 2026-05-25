package com.example.ferreteria_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CatalogoActivity : AppCompatActivity() {

    private lateinit var adaptadorProductos: CatalogoAdapter
    private lateinit var adaptadorCategorias: CategoriasAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var listaProductosMaestra: List<Producto> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_catalogo)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        configurarNavegacionInferior()

        // Carrusel de Categorías
        val rvCategorias = findViewById<RecyclerView>(R.id.rvCategorias)
        rvCategorias.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adaptadorCategorias = CategoriasAdapter(emptyList()) { categoriaSeleccionada ->
            filtrarPorCategoria(categoriaSeleccionada)
        }
        rvCategorias.adapter = adaptadorCategorias

        // Cuadrícula de Productos
        val rvProductos = findViewById<RecyclerView>(R.id.rvContenedorPrincipal)
        rvProductos.layoutManager = GridLayoutManager(this, 2)
        adaptadorProductos = CatalogoAdapter(emptyList())
        rvProductos.adapter = adaptadorProductos

        obtenerDatosDesdeFirebase()
        obtenerNombreUsuario()
    }

    // Configuración del Menú Inferior
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

                R.id.nav_perfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun obtenerDatosDesdeFirebase() {
        db.collection("productos")
            .get()
            .addOnSuccessListener { resultado ->
                val listaDescargada = mutableListOf<Producto>()

                for (documento in resultado) {
                    val producto = documento.toObject(Producto::class.java)
                    listaDescargada.add(producto)
                }

                listaProductosMaestra = listaDescargada

                val listaCategoriasDinamicas = mutableListOf("Todos")
                listaCategoriasDinamicas.addAll(listaProductosMaestra.map { it.categoria }.distinct())

                adaptadorCategorias.actualizarLista(listaCategoriasDinamicas)
                adaptadorProductos.actualizarLista(listaProductosMaestra)
            }
            .addOnFailureListener { excepcion ->
                Log.e("FirebaseError", "Error de conexión", excepcion)
                Toast.makeText(this, "Fallo al cargar catálogo", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filtrarPorCategoria(categoria: String) {
        if (categoria == "Todos") {
            adaptadorProductos.actualizarLista(listaProductosMaestra)
        } else {
            val listaFiltrada = listaProductosMaestra.filter { it.categoria == categoria }
            adaptadorProductos.actualizarLista(listaFiltrada)
        }
    }

    private fun obtenerNombreUsuario() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("usuarios").document(uid)
                .get()
                .addOnSuccessListener { documento ->
                    if (documento != null && documento.exists()) {
                        val nombre = documento.getString("nombre")
                        val tvNombreUsuario = findViewById<TextView>(R.id.tvNombreUsuario)

                        if (!nombre.isNullOrEmpty()) {
                            tvNombreUsuario.text = nombre
                        }
                    }
                }
                .addOnFailureListener { excepcion ->
                    Log.e("FirestoreError", "Error al obtener datos de usuario", excepcion)
                }
        }
    }
}