package com.example.ferreteria_app

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

class GenerarQRActivity : AppCompatActivity() {

    private lateinit var viewModel: PedidoViewModel
    private lateinit var ivQRCode: ImageView
    private lateinit var pbCargandoQR: ProgressBar
    private var pedidoId: String? = null
    private var snapshotListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_generar_qr)

        pedidoId = intent.getStringExtra("pedidoId")
        viewModel = ViewModelProvider(this)[PedidoViewModel::class.java]

        ivQRCode = findViewById(R.id.ivQRCode)
        pbCargandoQR = findViewById(R.id.pbCargandoQR)

        findViewById<View>(R.id.ivVolverGenerar).setOnClickListener { finish() }
        findViewById<View>(R.id.btnGenerarNuevo).setOnClickListener {
            generarQR()
        }

        generarQR()
        iniciarEscuchaEstadoPedido()
    }

    private fun iniciarEscuchaEstadoPedido() {
        val id = pedidoId ?: return
        val db = FirebaseFirestore.getInstance()
        
        // Listener en tiempo real directo a Firestore para máxima velocidad de respuesta
        snapshotListener = db.collection("pedidos").document(id)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                if (snapshot != null && snapshot.exists()) {
                    val estado = snapshot.getString("estado")
                    if (estado?.equals(EstadoPedido.ENTREGADO, ignoreCase = true) == true) {
                        val numeroPedido = snapshot.getString("numeroPedido") ?: ""
                        val clienteId = snapshot.getString("clienteId") ?: ""
                        
                        // Buscar el nombre del cliente en Firestore
                        db.collection("usuarios").document(clienteId).get()
                            .addOnSuccessListener { docUser ->
                                val nombreCliente = docUser.getString("nombre") ?: "Cliente"
                                
                                val intentSuccess = Intent(this@GenerarQRActivity, EntregaCompletadaRepartidorActivity::class.java)
                                intentSuccess.putExtra("numeroPedido", numeroPedido)
                                intentSuccess.putExtra("nombreCliente", nombreCliente)
                                
                                startActivity(intentSuccess)
                                finish()
                            }
                            .addOnFailureListener {
                                val intentSuccess = Intent(this@GenerarQRActivity, EntregaCompletadaRepartidorActivity::class.java)
                                intentSuccess.putExtra("numeroPedido", numeroPedido)
                                intentSuccess.putExtra("nombreCliente", "Cliente")
                                startActivity(intentSuccess)
                                finish()
                            }
                    }
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        snapshotListener?.remove()
    }

    private fun generarQR() {
        val id = pedidoId ?: return
        pbCargandoQR.visibility = View.VISIBLE
        viewModel.generarQR(id) { token ->
            pbCargandoQR.visibility = View.GONE
            if (token != null) {
                mostrarQR(token)
            } else {
                Toast.makeText(this, "Error al generar el token QR", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarQR(token: String) {
        try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(token, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            ivQRCode.setImageBitmap(bmp)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}