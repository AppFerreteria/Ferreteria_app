package com.example.ferreteria_app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Size
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class EscaneoQRActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewView: PreviewView
    private lateinit var progressBar: ProgressBar
    private lateinit var viewModel: ConfirmacionEntregaViewModel
    private var pedidoId: String? = null
    private var isScanning = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_escaneo_qr)

        pedidoId = intent.getStringExtra(MapaSeguimientoActivity.EXTRA_PEDIDO_ID)
        viewModel = ViewModelProvider(this)[ConfirmacionEntregaViewModel::class.java]

        previewView = findViewById(R.id.previewView)
        progressBar = findViewById(R.id.progressBar)
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        observarEstado()
    }

    private fun observarEstado() {
        lifecycleScope.launch {
            viewModel.estadoConfirmacion.collect { result ->
                result?.onSuccess { pedido ->
                    if (pedido.id == pedidoId) {
                        val token = intent.getStringExtra("TOKEN_TEMP") ?: ""
                        viewModel.confirmarEntrega(pedido.id, token)
                    } else {
                        Toast.makeText(this@EscaneoQRActivity, "QR no corresponde a este pedido", Toast.LENGTH_SHORT).show()
                        isScanning = true
                    }
                }?.onFailure {
                    Toast.makeText(this@EscaneoQRActivity, it.message, Toast.LENGTH_SHORT).show()
                    isScanning = true
                }
            }
        }

        lifecycleScope.launch {
            viewModel.confirmacionExitosa.collect { exito ->
                if (exito) {
                    val pedidoObj = viewModel.estadoConfirmacion.value?.getOrNull()
                    val repartidorId = pedidoObj?.repartidorId ?: ""
                    
                    // Buscar el nombre del repartidor en Firestore
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    db.collection("usuarios").document(repartidorId).get()
                        .addOnSuccessListener { doc ->
                            val nombreRepartidor = doc.getString("nombre") ?: "Repartidor"
                            
                            val intentSuccess = Intent(this@EscaneoQRActivity, ConfirmacionExitosaActivity::class.java)
                            intentSuccess.putExtra("pedidoId", pedidoId)
                            intentSuccess.putExtra("numeroPedido", pedidoObj?.numeroPedido ?: "")
                            intentSuccess.putExtra("nombreRepartidor", nombreRepartidor)
                            
                            startActivity(intentSuccess)
                            finish()
                        }
                        .addOnFailureListener {
                            // En caso de fallo, mostrar la pantalla igual con nombre genérico
                            val intentSuccess = Intent(this@EscaneoQRActivity, ConfirmacionExitosaActivity::class.java)
                            intentSuccess.putExtra("pedidoId", pedidoId)
                            intentSuccess.putExtra("numeroPedido", pedidoObj?.numeroPedido ?: "")
                            intentSuccess.putExtra("nombreRepartidor", "Repartidor")
                            startActivity(intentSuccess)
                            finish()
                        }
                }
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor, object : ImageAnalysis.Analyzer {
                @ExperimentalGetImage
                override fun analyze(imageProxy: ImageProxy) {
                    if (isScanning) {
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                            val scanner = BarcodeScanning.getClient()
                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        val rawValue = barcode.rawValue
                                        if (rawValue != null) {
                                            isScanning = false
                                            intent.putExtra("TOKEN_TEMP", rawValue)
                                            viewModel.validarQR(rawValue)
                                        }
                                    }
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    } else {
                        imageProxy.close()
                    }
                }
            })

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            } catch (exc: Exception) {
                // Error binding
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        baseContext, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, getString(R.string.error_permiso_camara), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}