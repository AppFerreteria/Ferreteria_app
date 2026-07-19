package com.example.ferreteria_app

import android.os.Bundle
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

class MapaSeguimientoActivity :
    AppCompatActivity(),
    OnMapReadyCallback {

    companion object {
        const val EXTRA_PEDIDO_ID = "pedidoId"
    }

    private lateinit var viewModel: MapaSeguimientoViewModel
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

        viewModel = ViewModelProvider(this)[MapaSeguimientoViewModel::class.java]

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

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        viewModel.escucharPedido(pedidoId)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { data ->
                    tvNumeroPedido.text = getString(
                        R.string.label_numero_pedido,
                        data.numeroPedido
                    )

                    clEstadoPreparacion.visibility = if (data.mostrarPreparacion) View.VISIBLE else View.GONE
                    clMapaEnCamino.visibility = if (data.mostrarMapa) View.VISIBLE else View.GONE
                    clEstadoEntregado.visibility = if (data.mostrarEntregado) View.VISIBLE else View.GONE

                    tvUbicacionRepartidor.text = data.textoUbicacion

                    if (data.mostrarMapa && this@MapaSeguimientoActivity::googleMap.isInitialized) {
                        val lat = data.ubicacionLat
                        val lng = data.ubicacionLng
                        if (lat != null && lng != null) {
                            val posicion = LatLng(lat, lng)
                            if (marker == null) {
                                marker = googleMap.addMarker(
                                    MarkerOptions().position(posicion).title("Repartidor")
                                )
                                googleMap.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(posicion, 16f)
                                )
                            } else {
                                marker?.position = posicion
                                googleMap.animateCamera(CameraUpdateFactory.newLatLng(posicion))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isCompassEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true
        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(LatLng(-12.0464, -77.0428), 14f)
        )
    }
}
