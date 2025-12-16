package com.example.adopet

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MyPetsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyPetsAdapter
    private val myPetList = mutableListOf<Pet>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_pets)

        recyclerView = findViewById(R.id.recyclerMyPets)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // DÜZELTİLDİ: Adaptörü yeni ve doğru parametrelerle kuruyoruz.
        adapter = MyPetsAdapter(
            myPetList,
            emptySet(), // Bu ekranda favori özelliği olmayacağı için boş set gönderiyoruz.
            onItemClick = { pet ->
                val intent = Intent(this, EditPetActivity::class.java)
                intent.putExtra("petId", pet.id)
                startActivity(intent)
            },
            onFavoriteClick = {} // Bu ekranda favori butonu bir şey yapmayacak.
        )

        recyclerView.adapter = adapter

        loadMyPets()
    }

    private fun loadMyPets() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("pets")
            .whereEqualTo("ownerId", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                myPetList.clear()
                val pets = result.toObjects(Pet::class.java)
                myPetList.addAll(pets)
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "İlanlar yüklenemedi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
