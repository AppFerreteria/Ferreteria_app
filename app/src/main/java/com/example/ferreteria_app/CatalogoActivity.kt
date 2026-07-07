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
        adaptadorProductos = CatalogoAdapter(emptyList()) { producto ->
            agregarProductoAlCarrito(producto)
        }
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

    private fun obtenerDatosDesdeFirebase() {
        db.collection("productos")
            .get()
            .addOnSuccessListener { resultado ->
                if (resultado.isEmpty) {
                    Log.d("FirebaseSeeding", "La colección está vacía, intentando subir productos...")
                    subirProductosDePrueba()
                    return@addOnSuccessListener
                }
                val listaDescargada = mutableListOf<Producto>()

                for (documento in resultado) {
                    try {
                        val producto = documento.toObject(Producto::class.java)?.copy(id = documento.id)
                        if (producto != null) {
                            listaDescargada.add(producto)
                        }
                    } catch (e: Exception) {
                        Log.e("FirebaseError", "Error al convertir producto", e)
                    }
                }

                actualizarUI(listaDescargada)
            }
            .addOnFailureListener { excepcion ->
                Log.e("FirebaseError", "Error al conectar con el catálogo", excepcion)
                Toast.makeText(this, "Error al cargar catálogo de Firebase", Toast.LENGTH_SHORT).show()
            }
    }

    private fun actualizarUI(productos: List<Producto>) {
        listaProductosMaestra = productos
        val listaCategoriasDinamicas = mutableListOf(getString(R.string.categoria_todos))
        listaCategoriasDinamicas.addAll(listaProductosMaestra.map { it.categoria }.distinct())

        adaptadorCategorias.actualizarLista(listaCategoriasDinamicas)
        adaptadorProductos.actualizarLista(listaProductosMaestra)
    }

    private fun obtenerListaMock(): List<Producto> {
        return listOf(
            // HERRAMIENTAS MANUALES
            Producto(nombre = "Martillo Mango Fibra 16oz", precio = 18.90, sku = "HER001", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/6954209-1000-1000/106342.jpg", categoria = "Herramientas", stock = 25),
            Producto(nombre = "Juego de Destornilladores (6 pzas)", precio = 35.50, sku = "HER002", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/6344304-1000-1000/113813.jpg", categoria = "Herramientas", stock = 15),
            Producto(nombre = "Llave Francesa 10\"", precio = 24.90, sku = "HER003", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/6016666-1000-1000/118318.jpg", categoria = "Herramientas", stock = 20),
            Producto(nombre = "Alicate Universal 8\"", precio = 15.00, sku = "HER004", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/6954215-1000-1000/106343.jpg", categoria = "Herramientas", stock = 30),
            
            // MAQUINARIA Y ELÉCTRICAS
            Producto(nombre = "Taladro Percutor 600W", precio = 159.00, sku = "MAQ001", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7667857-1000-1000/137351.jpg", categoria = "Maquinaria", stock = 10),
            Producto(nombre = "Sierra Circular 7-1/4\"", precio = 299.00, sku = "MAQ002", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7014169-1000-1000/114515.jpg", categoria = "Maquinaria", stock = 5),
            Producto(nombre = "Amoladora Angular 4-1/2\"", precio = 145.00, sku = "MAQ003", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7014175-1000-1000/114518.jpg", categoria = "Maquinaria", stock = 8),
            Producto(nombre = "Rotomartillo SDS Plus 800W", precio = 420.00, sku = "MAQ004", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7014180-1000-1000/114520.jpg", categoria = "Maquinaria", stock = 4),

            // PINTURAS
            Producto(nombre = "Pintura Látex Pato Blanco Galón", precio = 42.50, sku = "PIN001", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7085750-1000-1000/125219.jpg", categoria = "Pinturas", stock = 40),
            Producto(nombre = "Rodillo antigota 9\"", precio = 12.90, sku = "PIN002", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/682123-1000-1000/112345.jpg", categoria = "Pinturas", stock = 50),
            Producto(nombre = "Brocha Cerda Natural 3\"", precio = 6.50, sku = "PIN003", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/682130-1000-1000/112346.jpg", categoria = "Pinturas", stock = 60),
            Producto(nombre = "Esmalte Sintético Negro", precio = 28.00, sku = "PIN004", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7085755-1000-1000/125220.jpg", categoria = "Pinturas", stock = 20),

            // SEGURIDAD Y HOGAR
            Producto(nombre = "Cerradura de Dormitorio", precio = 32.90, sku = "SEG001", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7018898-1000-1000/120614.jpg", categoria = "Seguridad", stock = 18),
            Producto(nombre = "Candado de Acero 50mm", precio = 22.00, sku = "SEG002", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7018905-1000-1000/120615.jpg", categoria = "Seguridad", stock = 25),
            Producto(nombre = "Guantes de Seguridad Cuero", precio = 14.50, sku = "SEG003", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7018910-1000-1000/120616.jpg", categoria = "Seguridad", stock = 40),
            Producto(nombre = "Casco de Seguridad Amarillo", precio = 19.90, sku = "SEG004", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7018915-1000-1000/120617.jpg", categoria = "Seguridad", stock = 15),

            // ILUMINACIÓN Y ELECTRICIDAD
            Producto(nombre = "Foco LED 9W Luz Blanca", precio = 5.90, sku = "ELE001", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7018920-1000-1000/120618.jpg", categoria = "Electricidad", stock = 100),
            Producto(nombre = "Interruptor Simple Blanco", precio = 8.50, sku = "ELE002", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7018925-1000-1000/120619.jpg", categoria = "Electricidad", stock = 50),
            Producto(nombre = "Cinta Aislante 20m Negra", precio = 4.50, sku = "ELE003", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7018930-1000-1000/120620.jpg", categoria = "Electricidad", stock = 80),
            Producto(nombre = "Cable Eléctrico 14 AWG 100m", precio = 145.00, sku = "ELE004", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7018935-1000-1000/120621.jpg", categoria = "Electricidad", stock = 12),

            // CONSTRUCCIÓN
            Producto(nombre = "Escalera Telescópica 3.8m", precio = 380.00, sku = "CON001", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/5657591-1000-1000/129712.jpg", categoria = "Construcción", stock = 6),
            Producto(nombre = "Wincha 5 metros", precio = 12.50, sku = "CON002", imagenUrl = "https://www.corporacionferremax.com/product/wincha-global-plus-5-metros-stanley", categoria = "Construcción", stock = 30),
            Producto(nombre = "Nivel de Mano 24\"", precio = 28.90, sku = "CON003", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7018945-1000-1000/120623.jpg", categoria = "Construcción", stock = 15),
            Producto(nombre = "Combo de Goma 16oz", precio = 16.50, sku = "CON004", imagenUrl = "https://promart.vteximg.com.br/arquivos/ids/7018950-1000-1000/120624.jpg", categoria = "Construcción", stock = 20)
        )
    }

    private fun filtrarPorCategoria(categoria: String) {
        if (categoria == getString(R.string.categoria_todos)) {
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

    private fun subirProductosDePrueba() {
        val productos = obtenerListaMock()

        val batch = db.batch()
        for (producto in productos) {
            val docRef = db.collection("productos").document()
            batch.set(docRef, producto)
        }

        batch.commit()
            .addOnSuccessListener {
                Log.d("FirebaseSeeding", "Productos subidos correctamente")
                Toast.makeText(this, "Conectado: Productos iniciales creados", Toast.LENGTH_SHORT).show()
                obtenerDatosDesdeFirebase()
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseSeeding", "Error al subir productos", e)
                Toast.makeText(this, "Error de permisos en Firestore", Toast.LENGTH_LONG).show()
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

