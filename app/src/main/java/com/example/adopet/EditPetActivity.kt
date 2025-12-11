package com.example.adopet
import com.bumptech.glide.Glide
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.adopet.utils.PinataUploader
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.example.adopet.BuildConfig

class EditPetActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etBreed: EditText
    private lateinit var etAge: EditText
    private lateinit var etWeight: EditText
    private lateinit var etCity: EditText
    private lateinit var etDistrict: EditText
    private lateinit var etDesc: EditText
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button
    private lateinit var btnSelectPhoto: Button

    private var selectedImageUri: Uri? = null
    private var currentHash: String? = null
    private lateinit var petId: String
    private lateinit var ivPetPhoto: ImageView
    private val PICK_IMAGE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_pet)
        ivPetPhoto = findViewById(R.id.ivPetPhoto)
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()

        // XML bağlama
        etName = findViewById(R.id.etNameEdit)
        etBreed = findViewById(R.id.etBreedEdit)
        etAge = findViewById(R.id.etAgeEdit)
        etWeight = findViewById(R.id.etWeightEdit)
        etCity = findViewById(R.id.etCityEdit)
        etDistrict = findViewById(R.id.etDistrictEdit)
        etDesc = findViewById(R.id.etDescEdit)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnDelete = findViewById(R.id.btnDelete)
        btnSelectPhoto = findViewById(R.id.btnSelectPhotoEdit)

        petId = intent.getStringExtra("petId") ?: return

        // Firestore’dan bilgileri çek
        db.collection("pets").document(petId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    etName.setText(doc.getString("name"))
                    etBreed.setText(doc.getString("breed"))
                    etAge.setText(doc.get("age").toString())
                    etWeight.setText(doc.get("weight").toString())
                    etCity.setText(doc.getString("city"))
                    etDistrict.setText(doc.getString("district"))
                    etDesc.setText(doc.getString("description"))
                    currentHash = doc.getString("photoHash")

                    if (!currentHash.isNullOrEmpty()) {
                        val url = "https://gateway.pinata.cloud/ipfs/$currentHash"
                        Glide.with(this)
                            .load(url)
                            .into(ivPetPhoto)
                    }
                }
            }

        // Fotoğraf seç
        btnSelectPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE)
        }

        // Güncelle
        btnUpdate.setOnClickListener {

            val updatedData = hashMapOf<String, Any>(
                "name" to etName.text.toString(),
                "breed" to etBreed.text.toString(),
                "age" to (etAge.text.toString().toIntOrNull() ?: 0),
                "weight" to (etWeight.text.toString().toDoubleOrNull() ?: 0.0),
                "city" to etCity.text.toString(),
                "district" to etDistrict.text.toString(),
                "description" to etDesc.text.toString(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            if (selectedImageUri != null) {
                CoroutineScope(Dispatchers.Main).launch {
                    val uploader = PinataUploader(BuildConfig.PINATA_JWT)
                    val newCid = uploader.uploadImage(this@EditPetActivity, selectedImageUri!!)
                    if (newCid != null) {
                        updatedData["photoHash"] = newCid
                    }
                    updateFirestore(db, updatedData)
                }
            } else {
                updateFirestore(db, updatedData)
            }
        }

        // Silme
        btnDelete.setOnClickListener {
            db.collection("pets").document(petId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "İlan silindi", Toast.LENGTH_SHORT).show()
                    finish()
                }
        }
    }

    private fun updateFirestore(db: FirebaseFirestore, updatedData: HashMap<String, Any>) {
        db.collection("pets").document(petId)
            .update(updatedData)
            .addOnSuccessListener {
                Toast.makeText(this, "İlan güncellendi", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Hata: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            Toast.makeText(this, "Yeni fotoğraf seçildi", Toast.LENGTH_SHORT).show()
            selectedImageUri?.let {
                ivPetPhoto.setImageURI(it)
            }
        }
    }
}
