package com.example.adopet

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adopet.databinding.ActivityHomeBinding // ViewBinding import
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: MyPetsAdapter
    private val allPetsList = mutableListOf<Pet>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()
        fetchApprovedPets()
    }

    private fun setupRecyclerView() {
        adapter = MyPetsAdapter(allPetsList) { pet ->
            // Tıklanan ilanın detay sayfasına git
            val intent = Intent(this, PetDetailActivity::class.java) // YENİ AKTİVİTE
            intent.putExtra("petId", pet.id)
            startActivity(intent)
        }
        binding.recyclerHomePets.layoutManager = LinearLayoutManager(this)
        binding.recyclerHomePets.adapter = adapter
    }

    private fun setupListeners() {
        // Yeni İlan Ekle Butonu
        binding.fabAddPet.setOnClickListener {
            startActivity(Intent(this, AddPetActivity::class.java))
        }

        // Alt Navigasyon Menüsü
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Zaten bu ekrandayız, bir şey yapmaya gerek yok
                    true
                }
                R.id.navigation_map -> {
                    startActivity(Intent(this, MapActivity::class.java))
                    true
                }
                R.id.navigation_mypets -> {
                    startActivity(Intent(this, MyPetsActivity::class.java))
                    true
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchApprovedPets() {
        db.collection("pets")
            .whereEqualTo("status", "approved") // <-- SADECE ONAYLANMIŞ İLANLAR
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Veriler alınamadı: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    allPetsList.clear()
                    val pets = snapshot.toObjects(Pet::class.java)
                    allPetsList.addAll(pets)
                    adapter.notifyDataSetChanged()
                }
            }
    }
}
