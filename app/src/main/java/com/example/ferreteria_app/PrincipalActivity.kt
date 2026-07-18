package com.example.ferreteria_app

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class PrincipalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_principal)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnClient = findViewById<MaterialButton>(R.id.btnClient)
        val btnDelivery = findViewById<MaterialButton>(R.id.btnDelivery)

        btnClient.setOnClickListener {
            val intent = Intent(this, IniciarSesionActivity::class.java)
            intent.putExtra("ROL", "CLIENTE")
            startActivity(intent)
        }

        btnDelivery.setOnClickListener {
            val intent = Intent(this, IniciarSesionActivity::class.java)
            intent.putExtra("ROL", "REPARTIDOR")
            startActivity(intent)
        }
    }
}