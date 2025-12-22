package com.example.adopet

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.adopet.databinding.ActivityPetDetailBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class PetDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityPetDetailBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    
    private var petId: String? = null
    private var currentPet: Pet? = null
    private var isFavorite = false

    private var map: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPetDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        petId = intent.getStringExtra("petId")

        if (petId == null) {
            Toast.makeText(this, "İlan ID'si bulunamadı!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        fetchPetDetails()
        checkIfFavorite()
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnRequestAdoption.setOnClickListener { createAdoptionRequest() }
        binding.fabFavorite.setOnClickListener { toggleFavorite() }
    }

    private fun fetchPetDetails() {
        db.collection("pets").document(petId!!).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    currentPet = document.toObject(Pet::class.java)?.apply { id = document.id }
                    currentPet?.let { updateUi(it) }
                } else {
                    Toast.makeText(this, "İlan bulunamadı.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateUi(pet: Pet) {
        binding.collapsingToolbar.title = pet.petName
        binding.tvPetName.text = pet.petName
        binding.chipGender.text = "Cinsiyet: ${pet.gender}"
        binding.chipBreed.text = "Irk: ${pet.breed}"
        binding.chipAge.text = "Yaş: ${pet.age} aylık"
        binding.tvDescription.text = pet.description

        if (pet.lat != 0.0 && pet.lng != 0.0) {
            binding.tvLocation.text = "${pet.city}, ${pet.district}"
            val petLocation = LatLng(pet.lat, pet.lng)
            map?.addMarker(MarkerOptions().position(petLocation).title(pet.petName))
            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(petLocation, 15f))
        } else {
            binding.tvLocation.text = "Konum belirtilmemiş"
        }

        if (pet.imageUrl.isNotBlank()) {
            GlideApp.with(this).load(pet.imageUrl).into(binding.ivPetPhoto)
        } 

        if (currentUser?.uid == pet.ownerId) {
            binding.btnRequestAdoption.visibility = android.view.View.GONE
            binding.fabFavorite.visibility = android.view.View.GONE
        } else {
            binding.btnRequestAdoption.visibility = android.view.View.VISIBLE
            binding.fabFavorite.visibility = android.view.View.VISIBLE
        }
    }

    private fun checkIfFavorite() {
        if (currentUser == null || petId == null) return
        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                val favoriteIds = document.get("favoritePetIds") as? List<String>
                isFavorite = favoriteIds?.contains(petId) == true
                updateFavoriteButton()
            }
    }

    private fun updateFavoriteButton() {
        if (isFavorite) {
            binding.fabFavorite.setImageResource(R.drawable.ic_favorite)
        } else {
            binding.fabFavorite.setImageResource(R.drawable.ic_favorite_border)
        }
    }

    private fun toggleFavorite() {
        if (currentUser == null || petId == null) {
            Toast.makeText(this, "Giriş yapmalısınız.", Toast.LENGTH_SHORT).show()
            return
        }
        val userDocRef = db.collection("users").document(currentUser.uid)

        val updateAction = if (isFavorite) {
            FieldValue.arrayRemove(petId!!)
        } else {
            FieldValue.arrayUnion(petId!!)
        }

        userDocRef.update("favoritePetIds", updateAction)
            .addOnSuccessListener {
                isFavorite = !isFavorite
                updateFavoriteButton()
                val message = if (isFavorite) "Favorilere eklendi!" else "Favorilerden kaldırıldı."
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("PetDetailActivity", "Favori güncellenemedi", e)
                Toast.makeText(this, "Hata: Favori güncellenemedi.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createAdoptionRequest() {
        // ... (Mevcut kod değişmedi)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        currentPet?.let { pet ->
            if (pet.lat != 0.0 && pet.lng != 0.0) {
                val petLocation = LatLng(pet.lat, pet.lng)
                map?.addMarker(MarkerOptions().position(petLocation).title(pet.petName))
                map?.moveCamera(CameraUpdateFactory.newLatLngZoom(petLocation, 15f))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}