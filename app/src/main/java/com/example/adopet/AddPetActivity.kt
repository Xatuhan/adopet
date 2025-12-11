package com.example.adopet

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.adopet.utils.PinataUploader

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.adopet.BuildConfig

class AddPetActivity : AppCompatActivity() {

    // 🔑 SADECE JWT BURAYA GELECEK (API key / secret DEĞİL)
    companion object {
        val uploader = PinataUploader(BuildConfig.PINATA_JWT)
    // Örnek: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    }

    private lateinit var etName: EditText
    private lateinit var spType: Spinner
    private lateinit var etBreed: EditText
    private lateinit var etAge: EditText
    private lateinit var etWeight: EditText
    private lateinit var etCity: EditText
    private lateinit var etDistrict: EditText
    private lateinit var etDesc: EditText
    private lateinit var btnSave: Button
    private lateinit var btnSelectLocation: Button
    private lateinit var btnSelectPhoto: Button

    private lateinit var auth: FirebaseAuth

    private var selectedImageUri: Uri? = null
    private var ipfsImageHash: String? = null

    private val PICK_IMAGE = 101

    private val listPets = listOf(
        "Kedi", "Köpek", "Tavşan", "Kaplumbağa",
        "Hamster", "Balık", "Kuş", "Böcek Türleri",
        "Sürüngenler", "Salyangoz", "Diğer"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_pet)

        // Firebase
        auth = FirebaseAuth.getInstance()
        val db = Firebase.firestore

        // View bağlama
        etName = findViewById(R.id.etName)
        spType = findViewById(R.id.spType)
        etBreed = findViewById(R.id.etBreed)
        etAge = findViewById(R.id.etAge)
        etWeight = findViewById(R.id.etWeight)
        etCity = findViewById(R.id.etCity)
        etDistrict = findViewById(R.id.etDistrict)
        etDesc = findViewById(R.id.etDesc)
        btnSave = findViewById(R.id.btnSave)
        btnSelectLocation = findViewById(R.id.btnSelectLocation)
        btnSelectPhoto = findViewById(R.id.btnSelectPhoto)

        // Tür spinner'ı
        val arrayAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listPets)
        spType.adapter = arrayAdapter

        // Konum seçme
        btnSelectLocation.setOnClickListener {
            val city = etCity.text.toString().trim()
            val district = etDistrict.text.toString().trim()

            val intent = Intent(this, MapSelectLocationActivity::class.java)
            intent.putExtra("city", city)
            intent.putExtra("district", district)
            startActivity(intent)
        }

        // Fotoğraf seçme
        btnSelectPhoto.setOnClickListener {
            val intent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE)
        }

        // Kaydet
        btnSave.setOnClickListener {
            val user = auth.currentUser
            if (user == null) {
                Toast.makeText(this, "Önce giriş yap", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val name = etName.text.toString().trim()
            val type = spType.selectedItem?.toString()?.trim() ?: ""
            val breed = etBreed.text.toString().trim()
            val age = etAge.text.toString().toIntOrNull() ?: 0
            val weight = etWeight.text.toString().toDoubleOrNull() ?: 0.0
            val city = etCity.text.toString().trim()
            val district = etDistrict.text.toString().trim()
            val desc = etDesc.text.toString().trim()

            if (name.isEmpty() || type.isEmpty()) {
                Toast.makeText(this, "İsim ve tür zorunlu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Fotoğraf varsa önce Pinata'ya yükle
            if (selectedImageUri != null) {
                CoroutineScope(Dispatchers.Main).launch {
                    val uploader = PinataUploader(BuildConfig.PINATA_JWT)
                    val cid = uploader.uploadImage(this@AddPetActivity, selectedImageUri!!)

                    if (cid == null) {
                        Toast.makeText(
                            this@AddPetActivity,
                            "Fotoğraf Pinata'ya yüklenemedi",
                            Toast.LENGTH_LONG
                        ).show()
                        return@launch
                    }

                    ipfsImageHash = cid
                    savePetToFirestore(
                        db = db,
                        userId = user.uid,
                        name = name,
                        type = type,
                        breed = breed,
                        age = age,
                        weight = weight,
                        city = city,
                        district = district,
                        desc = desc
                    )
                }
            } else {
                // Foto yoksa hash olmadan kaydet
                savePetToFirestore(
                    db = db,
                    userId = user.uid,
                    name = name,
                    type = type,
                    breed = breed,
                    age = age,
                    weight = weight,
                    city = city,
                    district = district,
                    desc = desc
                )
            }
        }
    }

    private fun savePetToFirestore(
        db: com.google.firebase.firestore.FirebaseFirestore,
        userId: String,
        name: String,
        type: String,
        breed: String,
        age: Int,
        weight: Double,
        city: String,
        district: String,
        desc: String
    ) {
        val petId = db.collection("pets").document().id

        val petData = hashMapOf(
            "petId" to petId,
            "ownerId" to userId,
            "name" to name,
            "type" to type,
            "breed" to breed,
            "age" to age,
            "weight" to weight,
            "city" to city,
            "district" to district,
            "description" to desc,
            "photoHash" to ipfsImageHash,  // Pinata CID burada
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("pets").document(petId).set(petData)
            .addOnSuccessListener {
                Toast.makeText(this, "İlan eklendi", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Hata: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    // Fotoğraf seçimi sonucu
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            Toast.makeText(this, "Fotoğraf seçildi", Toast.LENGTH_SHORT).show()
        }
    }
}
