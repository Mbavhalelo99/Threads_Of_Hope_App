package com.example.toh

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class Register : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        nameEditText = findViewById(R.id.nameEditText)
        usernameEditText = findViewById(R.id.usernameEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        registerButton = findViewById(R.id.registerButton)

        registerButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val username = usernameEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (name.isEmpty() || username.isEmpty() || phone.isEmpty() || email.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidPassword(password)) {
                Toast.makeText(this, "Password must be at least 8 characters long, contain a number, and a symbol", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Register the user with Firebase Authentication
            registerUserWithEmail(email, password)
        }
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordPattern = Regex("^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=!]).{8,}$")
        return passwordPattern.matches(password)
    }

    private fun registerUserWithEmail(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // User registration successful
                    Toast.makeText(this, "Successfully registered", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Login::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // User registration failed
                    Toast.makeText(this, "Registration failed, Try Again", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

