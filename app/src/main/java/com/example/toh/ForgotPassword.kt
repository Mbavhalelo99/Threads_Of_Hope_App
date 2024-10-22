package com.example.toh

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class ForgotPassword : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var resetButton: Button
    private lateinit var database: FirebaseDatabase
    private lateinit var usersRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        emailEditText = findViewById(R.id.emailEditText)
        // resetButton = findViewById(R.id.resetButton)

        database = FirebaseDatabase.getInstance()
        usersRef = database.getReference("users")

        resetButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            } else {
                // Simulate sending email and then redirect to login screen
                Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}




