package com.example.toh

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Receive : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var typeOfClothingSpinner: Spinner
    private lateinit var sizeOfClothingSpinner: Spinner
    private lateinit var checkAvailabilityButton: Button
    private lateinit var resultTextView: TextView
    private lateinit var requestItemButton: Button
    private lateinit var db: FirebaseFirestore
    private lateinit var realtimeDatabase: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive)

        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore
        realtimeDatabase = FirebaseDatabase.getInstance()

        nameEditText = findViewById(R.id.nameEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        typeOfClothingSpinner = findViewById(R.id.typeOfClothingSpinner)
        sizeOfClothingSpinner = findViewById(R.id.sizeOfClothingSpinner)
        checkAvailabilityButton = findViewById(R.id.checkAvailabilityButton)
        resultTextView = findViewById(R.id.resultTextView)
        requestItemButton = findViewById(R.id.requestItemButton)

        val homeBtn: ImageView = findViewById(R.id.homeBtn)
        homeBtn.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

        typeOfClothingSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (parent.getItemAtPosition(position).toString() == "Blankets") {
                    sizeOfClothingSpinner.visibility = View.GONE
                } else {
                    sizeOfClothingSpinner.visibility = View.VISIBLE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        checkAvailabilityButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val phone = phoneEditText.text.toString()
            val typeOfClothing = typeOfClothingSpinner.selectedItem.toString()
            val sizeOfClothing = if (typeOfClothing != "Blankets") sizeOfClothingSpinner.selectedItem.toString() else "N/A"

            if (name.isEmpty() || phone.isEmpty() || typeOfClothing.isEmpty() || (typeOfClothing != "Blankets" && sizeOfClothing.isEmpty())) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            checkItemAvailability(typeOfClothing, sizeOfClothing, name, phone)
        }
    }

    private fun checkItemAvailability(typeOfClothing: String, sizeOfClothing: String, name: String, phone: String) {
        // Reference to the 'donations' node in Realtime Database
        val donationsRef = realtimeDatabase.reference.child("donations")

        donationsRef.get()
            .addOnSuccessListener { dataSnapshot ->
                var itemAvailable = false

                for (userSnapshot in dataSnapshot.children) {
                    for (itemSnapshot in userSnapshot.children) {
                        val clothType = itemSnapshot.child("cloth_type").value.toString()
                        val size = itemSnapshot.child("size").value.toString()

                        if (clothType == typeOfClothing && (size == sizeOfClothing || sizeOfClothing == "N/A")) {
                            itemAvailable = true
                            break
                        }
                    }
                    if (itemAvailable) break
                }

                if (itemAvailable) {
                    // Show confirmation dialog if item is available
                    showConfirmationDialog(name, phone, typeOfClothing, sizeOfClothing)
                } else {
                    resultTextView.visibility = View.VISIBLE
                    resultTextView.text = "Item is not available"
                    requestItemButton.visibility = View.GONE
                    addNotification("Item Not Available")
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error checking availability: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun saveReceiverInfo(name: String, phone: String, typeOfClothing: String, sizeOfClothing: String) {
        val receiverData = mapOf(
            "name" to name,
            "phone" to phone,
            "cloth_type" to typeOfClothing,
            "size" to sizeOfClothing,
            "status" to "Requested"
        )

        val userId = auth.currentUser?.uid ?: return

        // Save to Firestore under user's 'donations' subcollection
        db.collection("users").document(userId)
            .collection("receivers")
            .add(receiverData)
            .addOnSuccessListener {
                // Save to Realtime Database
                val receiverRef = realtimeDatabase.reference.child("receivers").push()
                receiverRef.setValue(receiverData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Request Successful", Toast.LENGTH_SHORT).show()
                        displayNotificationActivity()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed:", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addNotification(message: String) {
        val notificationData = hashMapOf(
            "message" to message,
            "timestamp" to System.currentTimeMillis()
        )

        val userId = auth.currentUser?.uid ?: return

        // Save notification under the user's 'notifications' subcollection
        db.collection("users").document(userId)
            .collection("notification")
            .add(notificationData)
            .addOnSuccessListener {
                Toast.makeText(this, "Notification saved", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error saving notification: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayNotificationActivity() {
        val intent = Intent(this, Notification::class.java)
        startActivity(intent)
        finish()
    }

    private fun showConfirmationDialog(name: String, phone: String, typeOfClothing: String, sizeOfClothing: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Confirm Request")
        builder.setMessage("The item is available. Do you want to proceed with the request?")
        builder.setPositiveButton("Accept") { dialog, _ ->
            // Proceed with saving the receiver information and notifications
            saveReceiverInfo(name, phone, typeOfClothing, sizeOfClothing)
            addNotification("New request from $name: $typeOfClothing")
            Toast.makeText(this, "Item requested successfully", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

}



