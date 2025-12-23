package com.example.adopet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adopet.databinding.ActivityFavoritesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

class FavoritesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var petsAdapter: MyPetsAdapter
    private val favoritePetsList = mutableListOf<Pet>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
    }

    private fun setupRecyclerView() {
        petsAdapter = MyPetsAdapter(favoritePetsList,
            onItemClick = { pet ->
                val intent = Intent(this, PetDetailActivity::class.java)
                intent.putExtra("petId", pet.id)
                startActivity(intent)
            },
            onFavoriteClick = { pet, _ ->
                removeFavorite(pet)
            }
        )
        binding.recyclerFavorites.layoutManager = LinearLayoutManager(this)
        binding.recyclerFavorites.adapter = petsAdapter
    }

    private fun loadFavorites() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            updateFavoritesUI(emptyList())
            Toast.makeText(this, "Giriş yapmalısınız.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                if (document == null || !document.exists()) {
                    updateFavoritesUI(emptyList())
                    return@addOnSuccessListener
                }

                val petIds = document.get("favoritePetIds") as? List<String>

                if (petIds.isNullOrEmpty()) {
                    updateFavoritesUI(emptyList())
                } else {
                    fetchFavoritePets(petIds)
                }
            }
            .addOnFailureListener { e ->
                Log.e("FavoritesActivity", "Favoriler yüklenemedi", e)
                updateFavoritesUI(emptyList())
                Toast.makeText(this, "Favoriler yüklenemedi.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchFavoritePets(petIds: List<String>) {

        val cleanedIds = petIds
            .filter { it.isNotBlank() }
            .distinct()

        if (cleanedIds.isEmpty()) {
            updateFavoritesUI(emptyList())
            return
        }

        val chunks = cleanedIds.chunked(10)

        val allPets = mutableListOf<Pet>()
        var pending = chunks.size
        var anyFail = false

        chunks.forEach { chunk ->
            db.collection("pets")
                .whereEqualTo("status", "approved")
                .whereIn(FieldPath.documentId(), chunk)
                .get()
                .addOnSuccessListener { snap ->
                    allPets.addAll(
                        snap.documents.mapNotNull { doc ->
                            doc.toObject(Pet::class.java)?.apply { id = doc.id }
                        }
                    )
                }
                .addOnFailureListener { e ->
                    anyFail = true
                    Log.e("FavoritesActivity", "İlanlar alınamadı (chunk)", e)
                }
                .addOnCompleteListener {
                    pending--
                    if (pending == 0) {
                        if (anyFail) {
                            Log.e("FavoritesActivity", "Bazı favoriler çekilemedi, loga bak.")
                        }


                        val map = allPets.associateBy { it.id }
                        val sorted = cleanedIds.mapNotNull { map[it] }.reversed()

                        updateFavoritesUI(sorted)
                    }
                }
        }
    }


    private fun removeFavorite(pet: Pet) {
        val currentUser = auth.currentUser ?: return
        if (pet.id.isBlank()) return

        db.collection("users").document(currentUser.uid)
            .update("favoritePetIds", FieldValue.arrayRemove(pet.id))
            .addOnSuccessListener { 
                Toast.makeText(this, "'${pet.petName}' favorilerden kaldırıldı.", Toast.LENGTH_SHORT).show()
                loadFavorites()
            }
            .addOnFailureListener { 
                Toast.makeText(this, "Hata: Favori kaldırılamadı.", Toast.LENGTH_SHORT).show() 
            }
    }

    private fun updateFavoritesUI(pets: List<Pet>) {
        binding.progressBar.visibility = View.GONE
        favoritePetsList.clear()
        favoritePetsList.addAll(pets)
        petsAdapter.notifyDataSetChanged()

        if (pets.isEmpty()) {
            binding.recyclerFavorites.visibility = View.GONE
            binding.tvNoFavorites.visibility = View.VISIBLE
        } else {
            binding.recyclerFavorites.visibility = View.VISIBLE
            binding.tvNoFavorites.visibility = View.GONE
        }
    }
}