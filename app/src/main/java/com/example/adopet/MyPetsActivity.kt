package com.example.adopet

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyPetsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyPetsAdapter
    private val petList = mutableListOf<Map<String, Any>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_pets)

        recyclerView = findViewById(R.id.recyclerMyPets)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = MyPetsAdapter(petList) { pet ->
            val intent = Intent(this, EditPetActivity::class.java)
            intent.putExtra("petId", pet["petId"].toString())
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
            .get()
            .addOnSuccessListener { result ->
                petList.clear()
                for (doc in result) {
                    petList.add(doc.data)
                }
                adapter.notifyDataSetChanged()
            }
    }
}
