package com.example.adopet

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.adopet.utils.PinataUploader
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditPetActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var petId: String

    // Views
    private lateinit var ivPetPhoto: ImageView
    private lateinit var etName: EditText
    private lateinit var etBreed: EditText
    private lateinit var etAge: EditText
    private lateinit var etDesc: EditText
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button
    private lateinit var btnSelectPhoto: Button

    private var newImageUri: Uri? = null
    private var existingImageUrl: String? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            newImageUri = it
            ivPetPhoto.setImageURI(it) // Show preview of the new image
            Toast.makeText(this, "Yeni fotoğraf seçildi. Kaydetmeyi unutmayın.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_pet)

        petId = intent.getStringExtra("petId") ?: run {
            Toast.makeText(this, "İlan ID'si bulunamadı!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        bindViews()
        loadPetData()
        setupListeners()
    }

    private fun bindViews() {
        ivPetPhoto = findViewById(R.id.ivPetPhoto)
        etName = findViewById(R.id.etNameEdit)
        etBreed = findViewById(R.id.etBreedEdit)
        etAge = findViewById(R.id.etAgeEdit)
        etDesc = findViewById(R.id.etDescEdit)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnDelete = findViewById(R.id.btnDelete)
        btnSelectPhoto = findViewById(R.id.btnSelectPhotoEdit)
    }

    private fun loadPetData() {
        db.collection("pets").document(petId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    // Using the consistent Pet data class
                    val pet = doc.toObject(Pet::class.java) ?: return@addOnSuccessListener
                    etName.setText(pet.petName) // Correct field
                    etBreed.setText(pet.breed)
                    etAge.setText(pet.age.toString())
                    etDesc.setText(pet.description)
                    existingImageUrl = pet.imageUrl // Correct field

                    if (existingImageUrl?.isNotEmpty() == true) {
                        Glide.with(this).load(existingImageUrl).into(ivPetPhoto)
                    }
                } else {
                    Toast.makeText(this, "İlan veritabanında bulunamadı.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { 
                Toast.makeText(this, "İlan bilgileri yüklenemedi.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupListeners() {
        btnSelectPhoto.setOnClickListener { 
            pickImageLauncher.launch("image/*") 
        }

        btnUpdate.setOnClickListener { 
            // If a new image is selected, upload it first
            if (newImageUri != null) {
                uploadImageAndUpdatePet()
            } else {

                updatePetData(existingImageUrl ?: "")
            }
        }

        btnDelete.setOnClickListener { showDeleteConfirmationDialog() }
    }

    private fun uploadImageAndUpdatePet() {
        Toast.makeText(this, "Yeni fotoğraf yükleniyor...", Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.Main).launch {
            val uploader = PinataUploader(BuildConfig.PINATA_JWT)
            val cid = uploader.uploadImage(this@EditPetActivity, newImageUri!!)

            if (cid != null) {
                val newImageUrl = "https://gateway.pinata.cloud/ipfs/$cid"
                updatePetData(newImageUrl)
            } else {
                Toast.makeText(this@EditPetActivity, "Fotoğraf yüklenemedi. Değişiklikler kaydedilmedi.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updatePetData(imageUrl: String) {
        val updates = mapOf(
            "petName" to etName.text.toString().trim(),
            "breed" to etBreed.text.toString().trim(),
            "age" to (etAge.text.toString().toIntOrNull() ?: 0),
            "description" to etDesc.text.toString().trim(),
            "imageUrl" to imageUrl
        )

        db.collection("pets").document(petId)
            .set(updates, SetOptions.merge()) // Use merge to only update these fields
            .addOnSuccessListener {
                Toast.makeText(this, "İlan başarıyla güncellendi", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Güncelleme hatası: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("İlanı Sil")
            .setMessage("Bu ilanı kalıcı olarak silmek istediğinize emin misiniz?")
            .setPositiveButton("Evet, Sil") { _, _ ->
                db.collection("pets").document(petId).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "İlan silindi", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Silme hatası: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .setNegativeButton("İptal", null)
            .show()
    }
}
