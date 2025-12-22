package com.example.adopet

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.adopet.utils.PinataUploader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddPetActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    private lateinit var ivPetPhotoPreview: ImageView
    private lateinit var etName: EditText
    private lateinit var spType: Spinner
    private lateinit var spGender: Spinner
    private lateinit var etBreed: EditText
    private lateinit var etAge: EditText
    private lateinit var etCity: EditText
    private lateinit var etDesc: EditText
    private lateinit var btnSave: Button
    private lateinit var btnSelectLocation: Button
    private lateinit var btnSelectPhoto: Button
    private lateinit var tvSelectedLocation: TextView

    private var selectedImageUri: Uri? = null
    private var selectedLat: Double? = null
    private var selectedLng: Double? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            ivPetPhotoPreview.setImageURI(it)
            ivPetPhotoPreview.visibility = View.VISIBLE
            Toast.makeText(this, "Fotoğraf seçildi.", Toast.LENGTH_SHORT).show()
        }
    }

    private val mapResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            selectedLat = data?.getDoubleExtra("latitude", 0.0)
            selectedLng = data?.getDoubleExtra("longitude", 0.0)
            tvSelectedLocation.text = "Konum seçildi: ${String.format("%.4f", selectedLat)}, ${String.format("%.4f", selectedLng)}"
            tvSelectedLocation.visibility = TextView.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_pet)

        auth = FirebaseAuth.getInstance()
        bindViews()
        setupSpinners()
        setupListeners()
    }

    private fun bindViews() {
        ivPetPhotoPreview = findViewById(R.id.ivPetPhotoPreview)
        etName = findViewById(R.id.etName)
        spType = findViewById(R.id.spType)
        spGender = findViewById(R.id.spGender)
        etBreed = findViewById(R.id.etBreed)
        etAge = findViewById(R.id.etAge)
        etCity = findViewById(R.id.etCity)
        etDesc = findViewById(R.id.etDesc)
        btnSave = findViewById(R.id.btnSave)
        btnSelectLocation = findViewById(R.id.btnSelectLocation)
        btnSelectPhoto = findViewById(R.id.btnSelectPhoto)
        tvSelectedLocation = findViewById(R.id.tvSelectedLocation)
    }

    private fun setupSpinners() {
        val petTypes = listOf("Kedi", "Köpek", "Kuş", "Balık", "Diğer")
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, petTypes)
        spType.adapter = typeAdapter

        val petGenders = listOf("Dişi", "Erkek")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, petGenders)
        spGender.adapter = genderAdapter
    }

    private fun setupListeners() {
        btnSelectPhoto.setOnClickListener { pickImageLauncher.launch("image/*") }
        btnSelectLocation.setOnClickListener {
            val intent = Intent(this, MapSelectLocationActivity::class.java)
            intent.putExtra("city", etCity.text.toString().trim())
            mapResultLauncher.launch(intent)
        }
        btnSave.setOnClickListener { savePet() }
    }

    private fun savePet() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Önce giriş yapmalısınız.", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri == null || etName.text.isBlank() || etCity.text.isBlank()) {
            Toast.makeText(this, "İsim, Şehir ve Fotoğraf alanları zorunludur.", Toast.LENGTH_SHORT).show()
            return
        }
        
        Toast.makeText(this, "İlan yükleniyor, lütfen bekleyin...", Toast.LENGTH_LONG).show()
        btnSave.isEnabled = false

        CoroutineScope(Dispatchers.Main).launch {
            val uploader = PinataUploader(BuildConfig.PINATA_JWT)
            val cid = uploader.uploadImage(this@AddPetActivity, selectedImageUri!!)

            if (cid == null) {
                Toast.makeText(this@AddPetActivity, "Fotoğraf yüklenemedi.", Toast.LENGTH_LONG).show()
                btnSave.isEnabled = true
                return@launch
            }

            val imageUrl = "https://gateway.pinata.cloud/ipfs/$cid"
            savePetToFirestore(user.uid, imageUrl)
        }
    }

    private fun savePetToFirestore(userId: String, imageUrl: String) {
        val newPetRef = db.collection("pets").document()

        val newPet = Pet(
            id = newPetRef.id, // ID'yi buradan alıyoruz
            ownerId = userId,
            petName = etName.text.toString().trim(),
            type = spType.selectedItem.toString(),
            gender = spGender.selectedItem.toString(),
            breed = etBreed.text.toString().trim(),
            age = etAge.text.toString().toIntOrNull() ?: 0,
            description = etDesc.text.toString().trim(),
            city = etCity.text.toString().trim(),
            imageUrl = imageUrl,
            lat = selectedLat ?: 0.0,
            lng = selectedLng ?: 0.0,
            status = "pending_approval",
            timestamp = System.currentTimeMillis()
        )

        newPetRef.set(newPet)
            .addOnSuccessListener {
                Toast.makeText(this, "İlanınız onaya gönderildi.", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Hata: ${e.message}", Toast.LENGTH_LONG).show()
                btnSave.isEnabled = true
            }
    }
}