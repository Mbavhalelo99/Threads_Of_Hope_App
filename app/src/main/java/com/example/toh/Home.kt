package com.example.toh

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class Home : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val donateBtn = findViewById<ImageView>(R.id.donateImage)
        val receiveBtn = findViewById<ImageView>(R.id.recieveImage)
        val profBtn = findViewById<ImageView>(R.id.profBtn)
        val infoBtn = findViewById<ImageView>(R.id.infoBtn)
        val notBtn = findViewById<ImageView>(R.id.notBtn)
        val delBtn = findViewById<ImageView>(R.id.delBtn)

        // Navigate to different activities
        donateBtn.setOnClickListener {
            startActivity(Intent(this, Donate::class.java))
        }

        receiveBtn.setOnClickListener {
            startActivity(Intent(this, Receive::class.java))
        }

        profBtn.setOnClickListener {
            startActivity(Intent(this, Profile::class.java))
        }

        infoBtn.setOnClickListener {
            startActivity(Intent(this, Information::class.java))
        }

        notBtn.setOnClickListener {
            startActivity(Intent(this, Notification::class.java))
        }

        // Logout and delete user data on delBtn click
        delBtn.setOnClickListener {
            val user = auth.currentUser
            if (user != null) {
                deleteUser(user)
            } else {
                Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteUser(user: FirebaseUser) {
        // Delete user data from Firestore
        firestore.collection("users").document(user.uid)
            .delete()
            .addOnSuccessListener {
                // Delete the user from Firebase Authentication
                user.delete()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Sign out and redirect to login screen
                            auth.signOut()
                            Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, Login::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete user data", Toast.LENGTH_SHORT).show()
            }
    }
}
