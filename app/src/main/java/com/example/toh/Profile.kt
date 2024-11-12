package com.example.toh

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class Profile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var nameTextView: TextView
    private lateinit var usernameTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var donationCountTextView: TextView
    private lateinit var requestCountTextView: TextView
    private lateinit var statusTextView: TextView

    private var profileListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        nameTextView = findViewById(R.id.nameTextView)
        usernameTextView = findViewById(R.id.usernameTextView)
        phoneTextView = findViewById(R.id.phoneTextView)
        emailTextView = findViewById(R.id.emailTextView)
        donationCountTextView = findViewById(R.id.donationCountTextView)
        requestCountTextView = findViewById(R.id.requestCountTextView)
        statusTextView = findViewById(R.id.statusTextView)

        val homeBtn: ImageView = findViewById(R.id.homeBtn)
        homeBtn.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

        // Start listening to changes on the user's profile data
        startProfileListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove the Firestore listener when the activity is destroyed to avoid memory leaks
        profileListener?.remove()
    }

    private fun startProfileListener() {
        val user = auth.currentUser
        user?.let {
            val userRef: DocumentReference = firestore.collection("users").document(user.uid)

            profileListener = userRef.addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    // Handle any errors
                    return@addSnapshotListener
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    nameTextView.text = "Name and Surname: ${documentSnapshot.getString("name")}"
                    usernameTextView.text = "Username: ${documentSnapshot.getString("username")}"
                    phoneTextView.text = "Phone Number: ${documentSnapshot.getString("phone")}"
                    emailTextView.text = "Email Address: ${user.email}"

                    // Count documents in the "donations" subcollection
                    userRef.collection("donations").addSnapshotListener { donationSnapshot, donationError ->
                        if (donationError == null && donationSnapshot != null) {
                            val donationCount = donationSnapshot.size()
                            donationCountTextView.text = "Donations: $donationCount"

                            // Determine status based on donations
                            val status = when {
                                donationCount > 10 -> "Legendary"
                                donationCount in 6..10 -> "Star"
                                else -> "Rookie"
                            }
                            statusTextView.text = "Status: $status"
                        }
                    }

                    // Count documents in the "receivers" subcollection
                    userRef.collection("receivers").addSnapshotListener { receiverSnapshot, receiverError ->
                        if (receiverError == null && receiverSnapshot != null) {
                            val requestCount = receiverSnapshot.size()
                            requestCountTextView.text = "Requests: $requestCount"
                        }
                    }
                }
            }
        }
    }
}


