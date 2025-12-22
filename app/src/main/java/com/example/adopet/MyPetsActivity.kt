package com.example.adopet

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adopet.databinding.ActivityMyPetsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MyPetsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyPetsBinding
    private lateinit var petAdapter: PetCardAdapter
    private val myPetsList = mutableListOf<Pet>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyPetsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        fetchMyPets()
    }

    private fun setupRecyclerView() {
        petAdapter = PetCardAdapter(myPetsList) { pet ->

            val intent = Intent(this, EditPetActivity::class.java)
            intent.putExtra("petId", pet.id)
            startActivity(intent)
        }
        binding.recyclerMyPets.layoutManager = LinearLayoutManager(this)
        binding.recyclerMyPets.adapter = petAdapter
    }

    private fun fetchMyPets() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Giriş yapmalısınız.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        db.collection("pets")
            .whereEqualTo("ownerId", currentUser.uid) // Sadece mevcut kullanıcının ilanlarını getir
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "İlanlar yüklenemedi.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    myPetsList.clear()
                    val pets = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Pet::class.java)?.apply {
                            id = doc.id
                        }
                    }
                    myPetsList.addAll(pets)
                    petAdapter.updateList(myPetsList)

                    // Eğer liste boşsa "ilan yok" mesajını göster
                    if (myPetsList.isEmpty()) {
                        binding.tvNoPets.visibility = View.VISIBLE
                        binding.recyclerMyPets.visibility = View.GONE
                    } else {
                        binding.tvNoPets.visibility = View.GONE
                        binding.recyclerMyPets.visibility = View.VISIBLE
                    }
                }
            }
    }
}