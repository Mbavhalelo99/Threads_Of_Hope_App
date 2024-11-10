package com.example.toh

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class Notification : AppCompatActivity() {

    private lateinit var notificationListView: ListView
    private lateinit var notificationList: ArrayList<String>
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        // Initialize the ListView and ArrayList
        notificationListView = findViewById(R.id.notificationListView)
        notificationList = ArrayList()

        // Set up the ArrayAdapter
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, notificationList)
        notificationListView.adapter = adapter

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Fetch notifications from Firestore
        fetchNotifications()
    }

    private fun fetchNotifications() {
        db.collection("notifications")
            .get()
            .addOnSuccessListener { querySnapshot: QuerySnapshot ->
                // Clear previous data
                notificationList.clear()

                // Iterate through all documents in the "notifications" collection
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

