package com.example.ferreteria_app

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CatalogoActivity : AppCompatActivity() {

    private lateinit var adaptadorProductos: CatalogoAdapter
    private lateinit var adaptadorCategorias: CategoriasAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_catalogo)

        // 1. Inicializar motores de Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 2. Configurar Carrusel Horizontal de Categorías
        val rvCategorias = findViewById<RecyclerView>(R.id.rvCategorias)
        rvCategorias.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adaptadorCategorias = CategoriasAdapter(emptyList())
        rvCategorias.adapter = adaptadorCategorias

        // 3. Configurar Cuadrícula de Productos Destacados
        val rvProductos = findViewById<RecyclerView>(R.id.rvContenedorPrincipal)
        rvProductos.layoutManager = GridLayoutManager(this, 2)
        adaptadorProductos = CatalogoAdapter(emptyList())
        rvProductos.adapter = adaptadorProductos

        // 4. Descargar datos de productos y categorías
        obtenerDatosDesdeFirebase()

        // 5. Descargar el nombre del perfil del usuario logueado
        obtenerNombreUsuario()
    }

    private fun obtenerDatosDesdeFirebase() {
        db.collection("productos")
            .get()
            .addOnSuccessListener { resultado ->
                val listaProductos = mutableListOf<Producto>()

                for (documento in resultado) {
                    val producto = documento.toObject(Producto::class.java)
                    listaProductos.add(producto)
                }

                val listaCategoriasUnicas = listaProductos.map { it.categoria }.distinct()

                adaptadorCategorias.actualizarLista(listaCategoriasUnicas)
                adaptadorProductos.actualizarLista(listaProductos)
            }
            .addOnFailureListener { excepcion ->
                Log.e("FirebaseError", "Error de conexión", excepcion)
                Toast.makeText(this, "Fallo al cargar catálogo", Toast.LENGTH_SHORT).show()
            }
    }

    private fun obtenerNombreUsuario() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            // Buscamos el documento exacto del usuario usando su UID de autenticación
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