package com.example.toh

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class Notification : AppCompatActivity() {

    private lateinit var notificationListView: ListView
    private lateinit var notificationList: ArrayList<String>
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Retrieve the current user ID
        userId = auth.currentUser?.uid

        // Initialize the ListView and ArrayList
        notificationListView = findViewById(R.id.notificationListView)
        notificationList = ArrayList()

        // Set up the ArrayAdapter
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, notificationList)
        notificationListView.adapter = adapter

        // Fetch notifications from Firestore if userId is available
        userId?.let {
            fetchNotifications(it)
        } ?: run {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }

        val homeBtn: ImageView = findViewById(R.id.homeBtn)
        homeBtn.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }
    }

    private fun fetchNotifications(userId: String) {
        db.collection("users").document(userId).collection("notification")
            .get()
            .addOnSuccessListener { querySnapshot: QuerySnapshot ->
                // Clear previous data
                notificationList.clear()

                // Iterate through all documents in the user's "notifications" subcollection
                for (document in querySnapshot.documents) {
                    // Get the notification message as a String
                    val message = document.getString("message")
                    if (message != null) {
                        notificationList.add(message)
                    }
                }

                // Notify adapter of data changes
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Display an error message if data retrieval fails
                Toast.makeText(this@Notification, "Failed to load notifications: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}


