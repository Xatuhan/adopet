package com.example.adopet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adopet.databinding.ActivityFavoritesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FavoritesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var adapter: MyPetsAdapter
    private val favoritePetsList = mutableListOf<Pet>()
    private val favoritePetIds = mutableSetOf<String>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadFavoriteIdsAndThenPets()
    }

    override fun onResume() {
        super.onResume()
        // Ekran her görünür olduğunda listeyi yenilemek, başka ekranlarda yapılan
        // favori değişikliklerinin buraya yansımasını sağlar.
        loadFavoriteIdsAndThenPets()
    }

    private fun setupRecyclerView() {
        adapter = MyPetsAdapter(favoritePetsList, favoritePetIds,
            onItemClick = { pet ->
                val intent = Intent(this, PetDetailActivity::class.java)
                intent.putExtra("petId", pet.id)
                startActivity(intent)
            },
            onFavoriteClick = { pet ->
                toggleFavorite(pet)
            }
        )
        binding.recyclerFavorites.layoutManager = LinearLayoutManager(this)
        binding.recyclerFavorites.adapter = adapter
    }

    private fun loadFavoriteIdsAndThenPets() {
        val currentUser = auth.currentUser ?: return

        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    val serverFavorites = user?.favoritePetIds?.toSet() ?: emptySet()
                    
                    // Sadece gerçekten bir değişiklik varsa listeyi yeniden yükle
                    if (serverFavorites != favoritePetIds) {
                        favoritePetIds.clear()
                        favoritePetIds.addAll(serverFavorites)
                        fetchFavoritePets()
                    }
                }
            }
    }

    private fun fetchFavoritePets() {
        favoritePetsList.clear()
        if (favoritePetIds.isEmpty()) {
            adapter.notifyDataSetChanged()
            return
        }

        db.collection("pets").whereIn("id", favoritePetIds.toList()).get()
            .addOnSuccessListener { documents ->
                favoritePetsList.clear() // Önceki sonuçları temizle
                for (doc in documents) {
                    favoritePetsList.add(doc.toObject(Pet::class.java))
                }
                favoritePetsList.sortByDescending { it.timestamp }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { 
                Toast.makeText(this, "Favori ilanlar yüklenemedi.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun toggleFavorite(pet: Pet) {
        val currentUser = auth.currentUser ?: return
        val userDocRef = db.collection("users").document(currentUser.uid)
        val petId = pet.id

        // Bu ekranda sadece favoriden çıkarma işlemi yapılır.
        if (!favoritePetIds.contains(petId)) return

        // Adım 1: UI'ı anında güncelle (Optimistic Update)
        val petIndex = favoritePetsList.indexOfFirst { it.id == petId }
        if (petIndex != -1) {
            favoritePetsList.removeAt(petIndex)
            favoritePetIds.remove(petId)
            adapter.notifyItemRemoved(petIndex)
        } else {
            return
        }

        // Adım 2: Firestore'u arka planda güncelle
        userDocRef.update("favoritePetIds", FieldValue.arrayRemove(petId))
            .addOnFailureListener {
                // Adım 3: Hata olursa UI'ı eski haline geri döndür
                Log.e("FavoritesActivity", "Favori kaldırılamadı", it)
                Toast.makeText(this, "Bir hata oluştu, favori kaldırılamadı.", Toast.LENGTH_SHORT).show()
                
                // Silinen ilanı ve ID'yi geri ekle
                favoritePetIds.add(petId)
                favoritePetsList.add(petIndex, pet) 
                adapter.notifyItemInserted(petIndex) 
            }
    }
}
