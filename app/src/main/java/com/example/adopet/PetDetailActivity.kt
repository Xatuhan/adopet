package com.example.adopet

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.adopet.databinding.ActivityPetDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PetDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPetDetailBinding
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private var petId: String? = null
    private var currentPet: Pet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPetDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        petId = intent.getStringExtra("petId")

        if (petId == null) {
            Toast.makeText(this, "İlan ID'si bulunamadı!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        fetchPetDetails()
        setupAdoptionButton()
    }

    private fun fetchPetDetails() {
        db.collection("pets").document(petId!!).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val pet = document.toObject(Pet::class.java)
                    if (pet != null) {
                        currentPet = pet
                        updateUi(pet)
                    }
                } else {
                    Toast.makeText(this, "İlan bulunamadı.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Veri alınamadı: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUi(pet: Pet) {
        binding.tvPetName.text = pet.petName
        binding.tvPetInfo.text = "${pet.type} - ${pet.breed} - ${pet.city}"
        binding.tvDescription.text = pet.description

        if (pet.imageUrl.isNotBlank()) {
            Glide.with(this).load(pet.imageUrl).into(binding.ivPetPhoto)
        } else {
            binding.ivPetPhoto.setImageResource(R.drawable.ic_launcher_foreground)
        }

        if (currentUser?.uid == pet.ownerId) {
            binding.btnRequestAdoption.visibility = View.GONE
        }
    }

    private fun setupAdoptionButton() {
        binding.btnRequestAdoption.setOnClickListener {
            if (currentUser == null) {
                Toast.makeText(this, "Giriş yapmalısınız.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (currentPet == null) {
                Toast.makeText(this, "İlan bilgileri bekleniyor...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnRequestAdoption.isEnabled = false
            createAdoptionRequest()
        }
    }

    private fun createAdoptionRequest() {
        db.collection("users").document(currentUser!!.uid).get()
            .addOnSuccessListener { userDocument ->
                val requesterName = userDocument.getString("name") ?: "İsimsiz Kullanıcı"

                val requestId = db.collection("adoption_requests").document().id
                val request = AdoptionRequest(
                    id = requestId,
                    petId = currentPet!!.id,
                    petName = currentPet!!.petName,
                    petImageUrl = currentPet!!.imageUrl,
                    ownerId = currentPet!!.ownerId,
                    requesterId = currentUser.uid,
                    requesterName = requesterName,
                    status = "pending",
                    timestamp = System.currentTimeMillis(),
                    // DÜZELTİLDİ: Katılımcı ID'leri listesini ekle
                    participantIds = listOf(currentUser.uid, currentPet!!.ownerId)
                )

                db.collection("adoption_requests").document(requestId).set(request)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Sahiplenme isteğiniz başarıyla gönderildi!", Toast.LENGTH_LONG).show()
                        binding.btnRequestAdoption.text = "İstek Gönderildi"
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                        binding.btnRequestAdoption.isEnabled = true
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Kullanıcı bilgileri alınamadı.", Toast.LENGTH_SHORT).show()
                binding.btnRequestAdoption.isEnabled = true
            }
    }
}
