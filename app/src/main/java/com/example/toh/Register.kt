package com.example.toh

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class Register : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var realtimeDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        realtimeDatabase = FirebaseDatabase.getInstance()

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
            registerUserWithEmail(name, username, phone, email, password)
        }
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordPattern = Regex("^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=!]).{8,}$")
        return passwordPattern.matches(password)
    }

    private fun registerUserWithEmail(name: String, username: String, phone: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // User registration successful
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                    // Save user data in Realtime Database
                    saveUserDataToRealtimeDatabase(userId, name, username, phone, email)

                    // Save user data in Firestore with additional collections
                    saveUserDataToFirestore(userId, name, username, phone, email)

                    // Navigate to Login activity
                    Toast.makeText(this, "Successfully registered", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, Login::class.java))
                    finish()
                } else {
                    // User registration failed
                    Toast.makeText(this, "Registration failed, Try Again", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserDataToRealtimeDatabase(userId: String, name: String, username: String, phone: String, email: String) {
        val user = mapOf(
            "name" to name,
            "username" to username,
            "phone" to phone,
            "email" to email
        )
        realtimeDatabase.reference.child("users").child(userId).setValue(user)
            .addOnSuccessListener {
                Toast.makeText(this, "User saved to Realtime Database", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save user to Realtime Database", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserDataToFirestore(userId: String, name: String, username: String, phone: String, email: String) {
        val user = mapOf(
            "name" to name,
            "username" to username,
            "phone" to phone,
            "email" to email
        )
        val userDocRef = firestore.collection("users").document(userId)

        userDocRef.set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "User saved to Firestore", Toast.LENGTH_SHORT).show()

                // Create subcollections in Firestore
                createSubcollectionsForUser(userDocRef)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save user to Firestore", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createSubcollectionsForUser(userDocRef: DocumentReference) {
        val donationsCollection = userDocRef.collection("donations")
        val receiversCollection = userDocRef.collection("receivers")
        val notificationsCollection = userDocRef.collection("notification")
    }
}


