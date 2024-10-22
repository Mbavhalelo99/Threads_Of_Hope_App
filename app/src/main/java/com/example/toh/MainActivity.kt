package com.example.toh

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Using postDelayed to simulate loading time
        window.decorView.postDelayed({
            startActivity(Intent(this, Login::class.java))
            finish()
        }, 3000) // 3 seconds
    }
}
