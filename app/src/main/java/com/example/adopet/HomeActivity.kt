package com.example.adopet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adopet.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: MyPetsAdapter
    private val allPetsList = mutableListOf<Pet>()
    private val favoritePetIds = mutableSetOf<String>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()
        
        loadUserFavorites { 
            fetchApprovedPets()
        }
    }

    private fun setupRecyclerView() {
        adapter = MyPetsAdapter(allPetsList, favoritePetIds,
            onItemClick = { pet ->
                val intent = Intent(this, PetDetailActivity::class.java)
                intent.putExtra("petId", pet.id)
                startActivity(intent)
            },
            onFavoriteClick = { pet ->
                toggleFavorite(pet)
            }
        )
        binding.recyclerHomePets.layoutManager = LinearLayoutManager(this)
        binding.recyclerHomePets.adapter = adapter
    }

    private fun setupListeners() {
        binding.fabAddPet.setOnClickListener {
            startActivity(Intent(this, AddPetActivity::class.java))
        }
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_favorites -> {
                    startActivity(Intent(this, FavoritesActivity::class.java))
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

    private fun loadUserFavorites(onComplete: () -> Unit) {
        if (currentUser == null) { 
            onComplete()
            return 
        }
        val userDocRef = db.collection("users").document(currentUser.uid)
        userDocRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    favoritePetIds.clear()
                    user?.favoritePetIds?.let { favoritePetIds.addAll(it) }
                } else {
                    // Eğer kullanıcının dokümanı yoksa, favori ekleyebilmesi için boş bir tane oluştur.
                    userDocRef.set(User(uid = currentUser.uid, email = currentUser.email ?: ""), SetOptions.merge())
                }
            }
            .addOnFailureListener { 
                 Log.e("HomeActivity", "Favoriler yüklenemedi", it)
            }
            .addOnCompleteListener { 
                onComplete()
            }
    }

    private fun fetchApprovedPets() {
        db.collection("pets").whereEqualTo("status", "approved").orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    allPetsList.clear()
                    allPetsList.addAll(snapshot.toObjects(Pet::class.java))
                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun toggleFavorite(pet: Pet) {
        if (currentUser == null) {
            Toast.makeText(this, "Favorilere eklemek için giriş yapmalısınız.", Toast.LENGTH_SHORT).show()
            return
        }
        
        val userDocRef = db.collection("users").document(currentUser.uid)
        val petId = pet.id
        val isCurrentlyFavorite = favoritePetIds.contains(petId)

        val petIndex = allPetsList.indexOfFirst { it.id == petId }
        if (petIndex == -1) return

        // Adım 1: UI'ı anında güncelle (İyimser Güncelleme)
        if (isCurrentlyFavorite) {
            favoritePetIds.remove(petId)
        } else {
            favoritePetIds.add(petId)
        }
        adapter.notifyItemChanged(petIndex)

        // Adım 2: Firestore'u arka planda doğru komutla güncelle
        val firestoreUpdate = if (isCurrentlyFavorite) {
            FieldValue.arrayRemove(petId) 
        } else {
            FieldValue.arrayUnion(petId)
        }

        // DÜZELTİLDİ: set(merge) yerine, FieldValue operasyonları için doğru olan update() metodu kullanıldı.
        userDocRef.update("favoritePetIds", firestoreUpdate)
            .addOnFailureListener {
                // Adım 3: Hata olursa UI'ı eski haline geri döndür
                Log.e("HomeActivity", "Favori güncellenemedi", it)
                Toast.makeText(this, "Bir hata oluştu, favori güncellenemedi.", Toast.LENGTH_SHORT).show()
                if (isCurrentlyFavorite) {
                    favoritePetIds.add(petId)
                } else {
                    favoritePetIds.remove(petId)
                }
                adapter.notifyItemChanged(petIndex)
            }
    }
}
