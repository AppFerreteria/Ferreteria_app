package com.example.ferreteria_app

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale

class MapaSeguimientoActivity :
    AppCompatActivity(),
    OnMapReadyCallback {

    companion object {
        const val EXTRA_PEDIDO_ID = "pedidoId"
    }

    private val db = FirebaseFirestore.getInstance()
    private var pedidoListener: ListenerRegistration? = null
    private var repartidorListener: ListenerRegistration? = null

    private lateinit var googleMap: GoogleMap
    private var marker: Marker? = null

    private lateinit var tvNumeroPedido: TextView
    private lateinit var clEstadoPreparacion: View
    private lateinit var clMapaEnCamino: View
    private lateinit var clEstadoEntregado: View
    private lateinit var tvUbicacionRepartidor: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mapa_seguimiento)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvNumeroPedido = findViewById(R.id.tvNumeroPedidoSeguimiento)
        clEstadoPreparacion = findViewById(R.id.clEstadoPreparacion)
        clMapaEnCamino = findViewById(R.id.clMapaEnCamino)
        clEstadoEntregado = findViewById(R.id.clEstadoEntregado)
        tvUbicacionRepartidor = findViewById(R.id.tvUbicacionRepartidor)

        findViewById<View>(R.id.ivVolver).setOnClickListener { finish() }

        val pedidoId = intent.getStringExtra(EXTRA_PEDIDO_ID)
        if (pedidoId == null) {
            Toast.makeText(this, getString(R.string.error_pedido_no_encontrado), Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map)
                    as SupportMapFragment

        mapFragment.getMapAsync(this)

        escucharPedido(pedidoId)

    }
    private fun escucharPedido(pedidoId: String) {
        pedidoListener = db.collection("pedidos").document(pedidoId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    return@addSnapshotListener
                }
                val pedido = snapshot.toObject(Pedido::class.java)?.copy(id = snapshot.id)
                    ?: return@addSnapshotListener

                tvNumeroPedido.text = getString(R.string.label_numero_pedido, pedido.numeroPedido)

                when (pedido.estado) {
                    EstadoPedido.EN_CAMINO -> {
                        mostrarVista(clMapaEnCamino)
                        escucharUbicacionRepartidor(pedido.repartidorId)
                    }
                    EstadoPedido.ENTREGADO -> {

                        mostrarVista(clEstadoEntregado)

                        repartidorListener?.remove()
                        repartidorListener = null

                        marker?.remove()
                        marker = null
                    }
                    else -> {
                        // PENDIENTE o PREPARACION: aún no hay repartidor en ruta
                        mostrarVista(clEstadoPreparacion)
                        repartidorListener?.remove()
                        repartidorListener = null
                    }
                }
            }
    }

    /**
     * Listener dinámico sobre la ubicación del repartidor.
     * Solo se crea una vez (evita duplicar suscripciones si el snapshot
     * del pedido se emite varias veces con el mismo estado "en_camino").
     */
    private fun escucharUbicacionRepartidor(repartidorId: String) {
        if (repartidorListener != null) return
        if (repartidorId.isBlank()) {
            tvUbicacionRepartidor.text = getString(R.string.texto_esperando_ubicacion)
            return
        }

        repartidorListener = db.collection("repartidores").document(repartidorId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    tvUbicacionRepartidor.text = getString(R.string.texto_esperando_ubicacion)
                    return@addSnapshotListener
                }
                val repartidor = snapshot.toObject(Repartidor::class.java)
                val geo = repartidor?.ubicacionActual

                if (!::googleMap.isInitialized) {
                    return@addSnapshotListener
                }

                tvUbicacionRepartidor.text = if (geo != null) {
                    getString(
                        R.string.label_ubicacion_repartidor,
                        String.format(Locale.US, "%.4f, %.4f", geo.latitude, geo.longitude)
                    )
                } else {
                    getString(R.string.texto_esperando_ubicacion)
                }

                if (geo != null) {

                    val posicion = LatLng(
                        geo.latitude,
                        geo.longitude
                    )

                    if (marker == null) {

                        marker = googleMap.addMarker(
                            MarkerOptions()
                                .position(posicion)
                                .title("Repartidor")
                        )

                        googleMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                posicion,
                                16f
                            )
                        )

                    } else {

                        marker?.position = posicion

                        googleMap.animateCamera(
                            CameraUpdateFactory.newLatLng(posicion)
                        )

                    }
                }
            }
    }

    private fun mostrarVista(vistaVisible: View) {
        clEstadoPreparacion.visibility = if (vistaVisible == clEstadoPreparacion) View.VISIBLE else View.GONE
        clMapaEnCamino.visibility = if (vistaVisible == clMapaEnCamino) View.VISIBLE else View.GONE
        clEstadoEntregado.visibility = if (vistaVisible == clEstadoEntregado) View.VISIBLE else View.GONE
    }

    override fun onMapReady(map: GoogleMap) {

        googleMap = map

        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isCompassEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true

        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(-12.0464, -77.0428),
                14f
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        pedidoListener?.remove()
        repartidorListener?.remove()
    }
}