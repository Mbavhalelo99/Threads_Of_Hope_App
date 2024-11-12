package com.example.toh

import android.app.Activity
import android.app.DatePickerDialog
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
import java.util.Calendar

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

    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate)

        nameEditText = findViewById(R.id.nameEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        addressEditText = findViewById(R.id.addressEditText)
        emailEditText = findViewById(R.id.emailEditText)
        typeOfClothingSpinnerDonate = findViewById(R.id.typeOfClothingSpinnerDonate)
        conditionOfClothingSpinner = findViewById(R.id.conditionOfClothingSpinner)
        sizeOfClothingSpinner = findViewById(R.id.sizeOfClothingSpinner)
        deliveryRadioGroup = findViewById(R.id.deliveryRadioGroup)
        dateEditText = findViewById(R.id.dateEditText)
        takePictureButton = findViewById(R.id.takePictureButton)
        uploadPictureButton = findViewById(R.id.uploadPictureButton)
        pictureImageView = findViewById(R.id.pictureImageView)
        submitButton = findViewById(R.id.submitButton)

        // Firebase initialization
        auth = FirebaseAuth.getInstance()
        realtimeDatabase = FirebaseDatabase.getInstance().reference
        firestore = FirebaseFirestore.getInstance()

        // Set up date picker for dateEditText
        dateEditText.setOnClickListener {
            showDatePicker()
        }

        takePictureButton.setOnClickListener {
            dispatchTakePictureIntent()
        }

        uploadPictureButton.setOnClickListener {
            dispatchUploadPictureIntent()
        }

        submitButton.setOnClickListener {
            handleSubmit()
        }

        val homeBtn: ImageView = findViewById(R.id.homeBtn)
        homeBtn.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Set the selected date on the EditText
                val formattedDate = "${selectedDay.toString().padStart(2, '0')}/" +
                        "${(selectedMonth + 1).toString().padStart(2, '0')}/" +
                        "$selectedYear"
                dateEditText.setText(formattedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun handleSubmit() {
        val name = nameEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()
        val address = addressEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val clothType = typeOfClothingSpinnerDonate.selectedItem.toString().trim()
        val size = sizeOfClothingSpinner.selectedItem.toString().trim()
        val condition = conditionOfClothingSpinner.selectedItem.toString().trim()
        val deliveryType = findViewById<RadioButton>(deliveryRadioGroup.checkedRadioButtonId).text.toString()
        val dropPickDate = dateEditText.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty() || email.isEmpty() ||
            clothType.isEmpty() || condition.isEmpty() || dropPickDate.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid ?: return
        val donationData = mapOf(
            "name" to name,
            "phone" to phone,
            "address" to address,
            "email" to email,
            "cloth_type" to clothType,
            "size" to size,
            "condition" to condition,
            "delivery_type" to deliveryType,
            "drop_pick_date" to dropPickDate,
            "image" to (imageUri?.toString() ?: "") // Save image URI if available
        )

        realtimeDatabase.child("donations").child(userId).push().setValue(donationData)
            .addOnSuccessListener {
                addNotificationToFirestore(userId, name, clothType)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Donation Failed, Please Try Again", Toast.LENGTH_SHORT).show()
            }

        firestore.collection("users").document(userId).collection("donations").add(donationData)
            .addOnSuccessListener {
                Toast.makeText(this, "Thank You For Donating", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Home::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Please Try Again", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addNotificationToFirestore(userId: String, name: String, clothType: String) {
        val notificationData = mapOf(
            "message" to "New donation from $name: $clothType",
            "timestamp" to System.currentTimeMillis() // Optionally add a timestamp
        )

        firestore.collection("users").document(userId).collection("notification").add(notificationData)
            .addOnSuccessListener {
                // Notification added successfully
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to add notification", Toast.LENGTH_SHORT).show()
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







