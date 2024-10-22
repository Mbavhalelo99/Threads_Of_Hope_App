package com.example.toh
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class Home : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val donateBtn = findViewById<ImageView>(R.id.donateImage)
        val receiveBtn = findViewById<ImageView>(R.id.recieveImage)

        donateBtn.setOnClickListener {
            val intent = Intent(this, Donate::class.java)
            startActivity(intent)
        }

        receiveBtn.setOnClickListener {
            val intent = Intent(this, Receive::class.java)
            startActivity(intent)
        }
    }
}