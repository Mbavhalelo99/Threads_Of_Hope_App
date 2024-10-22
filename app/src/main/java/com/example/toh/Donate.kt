package com.example.toh

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class Donate : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var typeOfClothingSpinnerDonate: Spinner
    private lateinit var conditionOfClothingSpinner: Spinner
    private lateinit var sizeOfClothingSpinner: Spinner
    private lateinit var deliveryRadioGroup: RadioGroup
    private lateinit var dateEditText: EditText
    private lateinit var takePictureButton: Button
    private lateinit var uploadPictureButton: Button
    private lateinit var pictureImageView: ImageView
    private lateinit var submitButton: Button

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_UPLOAD = 2

    private lateinit var realtimeDatabase: DatabaseReference
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var imageUri: Uri? = null // Store the URI of the uploaded image

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate)

        nameEditText = findViewById(R.id.nameEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        addressEditText = findViewById(R.id.addressEditText)
        emailEditText = findViewById(R.id.emailEditText)
        typeOfClothingSpinnerDonate = findViewById(R.id.typeOfClothingSpinnerDonate)
        conditionOfClothingSpinner = findViewById(R.id.conditionOfClothingSpinner)
        sizeOfClothingSpinner = findViewById(R.id.sizeOfClothingSpinner) // Initialize sizeOfClothingSpinner here
        deliveryRadioGroup = findViewById(R.id.deliveryRadioGroup)
        takePictureButton = findViewById(R.id.takePictureButton)
        uploadPictureButton = findViewById(R.id.uploadPictureButton)
        pictureImageView = findViewById(R.id.pictureImageView)
        submitButton = findViewById(R.id.submitButton)

        // Firebase initialization
        auth = FirebaseAuth.getInstance()
        realtimeDatabase = FirebaseDatabase.getInstance().reference
        firestore = FirebaseFirestore.getInstance()

        takePictureButton.setOnClickListener {
            dispatchTakePictureIntent()
        }

        uploadPictureButton.setOnClickListener {
            dispatchUploadPictureIntent()
        }

        submitButton.setOnClickListener {
            handleSubmit()
        }
    }

    private fun handleSubmit() {
        val name = nameEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()
        val address = addressEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val typeOfClothing = typeOfClothingSpinnerDonate.selectedItem.toString().trim()
        val sizeOfClothing = sizeOfClothingSpinner.selectedItem.toString().trim() // Fixed variable name from sizeClothing to sizeOfClothing
        val conditionOfClothing = conditionOfClothingSpinner.selectedItem.toString().trim()
        val deliveryOption = findViewById<RadioButton>(deliveryRadioGroup.checkedRadioButtonId).text.toString()
        val date = dateEditText.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty() || email.isEmpty() ||
            typeOfClothing.isEmpty() || conditionOfClothing.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        } else {
            val userId = auth.currentUser?.uid ?: return
            val donationData = mapOf(
                "name" to name,
                "phone" to phone,
                "address" to address,
                "email" to email,
                "typeOfClothing" to typeOfClothing,
                "sizeOfClothing" to sizeOfClothing, // Fixed variable name here
                "conditionOfClothing" to conditionOfClothing,
                "deliveryOption" to deliveryOption,
                "date" to date,
                "imageUri" to imageUri.toString() // Save the image URI
            )

            // Save donation data to Realtime Database
            realtimeDatabase.child("donations").child(userId).push().setValue(donationData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Donation details saved to Realtime Database", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save to Realtime Database: ${it.message}", Toast.LENGTH_SHORT).show()
                }

            // Save donation data to Firestore
            firestore.collection("donations").add(donationData)
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(this, "Donation details saved to Firestore with ID: ${documentReference.id}", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to save to Firestore: ${exception.message}", Toast.LENGTH_SHORT).show()
                }

            // Redirect to the profile page
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish() // Optionally finish this activity
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    private fun dispatchUploadPictureIntent() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also { uploadPictureIntent ->
            uploadPictureIntent.type = "image/*"
            startActivityForResult(uploadPictureIntent, REQUEST_IMAGE_UPLOAD)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    pictureImageView.setImageBitmap(imageBitmap)
                    pictureImageView.visibility = ImageView.VISIBLE
                    imageUri = null // Clear any previous URI
                }
                REQUEST_IMAGE_UPLOAD -> {
                    imageUri = data?.data // Store the URI of the uploaded image
                    pictureImageView.setImageURI(imageUri)
                    pictureImageView.visibility = ImageView.VISIBLE
                }
            }
        }
    }
}




