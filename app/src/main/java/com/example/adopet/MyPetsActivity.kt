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
    private val myPetList = mutableListOf<Pet>() // <-- Map'ten Pet listesine çevrildi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_pets)

        recyclerView = findViewById(R.id.recyclerMyPets)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Yeni ve güvenli adaptörümüzü kuruyoruz
        adapter = MyPetsAdapter(myPetList) { pet ->
            // Tıklanan ilanın ID'sini EditPetActivity'ye gönderiyoruz
            val intent = Intent(this, EditPetActivity::class.java)
            intent.putExtra("petId", pet.id) // <-- Artık güvenli bir şekilde ID'ye erişiyoruz
            startActivity(intent)
        }

        recyclerView.adapter = adapter

        loadMyPets()
    }

    private fun loadMyPets() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("pets")
            .whereEqualTo("ownerId", uid)
            // İlanları en yeniden eskiye doğru sıralıyoruz
            .orderBy("timestamp", Query.Direction.DESCENDING) 
            .get()
            .addOnSuccessListener { result ->
                myPetList.clear()
                // Gelen dökümanları doğrudan Pet nesnelerine çeviriyoruz
                val pets = result.toObjects(Pet::class.java)
                myPetList.addAll(pets)
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "İlanlar yüklenemedi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
