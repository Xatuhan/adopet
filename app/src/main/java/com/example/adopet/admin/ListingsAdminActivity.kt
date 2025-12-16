package com.example.adopet.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adopet.Pet
import com.example.adopet.R
import com.example.adopet.databinding.ActivityAdminListBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ListingsAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminListBinding
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val allPetsList = mutableListOf<Pet>()
    private lateinit var adapter: ListingsAdapter

    // İlan durumları için bir liste
    private val statusOptions = listOf("pending_approval", "approved", "rejected", "adopted")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleText.text = "Tüm İlanları Yönet"

        // RecyclerView'ı yeni ve doğru adaptörle kuruyoruz
        adapter = ListingsAdapter(
            allPetsList,
            onEdit = { pet -> showEditDialog(pet) },
            onDelete = { pet -> showDeleteConfirmationDialog(pet) }
        )
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        fetchAllListings()
    }

    private fun fetchAllListings() {
        db.collection("pets")
            .orderBy("timestamp", Query.Direction.DESCENDING) // Tüm ilanları en yeniden eskiye sırala
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    Toast.makeText(this, err.message, Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                if (snap == null) return@addSnapshotListener

                allPetsList.clear()
                val pets = snap.toObjects(Pet::class.java)
                allPetsList.addAll(pets)
                adapter.notifyDataSetChanged()
            }
    }

    private fun showEditDialog(pet: Pet) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_admin_edit_pet, null)
        val etPetName = dialogView.findViewById<EditText>(R.id.etPetName)
        val spStatus = dialogView.findViewById<Spinner>(R.id.spStatus)

        // Mevcut bilgileri doldur
        etPetName.setText(pet.petName)
        
        // Spinner'ı ayarla
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statusOptions)
        spStatus.adapter = statusAdapter
        val currentStatusPosition = statusOptions.indexOf(pet.status)
        if (currentStatusPosition != -1) {
            spStatus.setSelection(currentStatusPosition)
        }

        AlertDialog.Builder(this)
            .setTitle("İlanı Düzenle")
            .setView(dialogView)
            .setPositiveButton("Kaydet") { _, _ ->
                val newName = etPetName.text.toString().trim()
                val newStatus = spStatus.selectedItem.toString()

                updatePet(pet.id, newName, newStatus)
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun updatePet(petId: String, newName: String, newStatus: String) {
        val updates = mapOf(
            "petName" to newName,
            "status" to newStatus
        )
        db.collection("pets").document(petId).update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "İlan güncellendi", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Güncelleme hatası: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showDeleteConfirmationDialog(pet: Pet) {
        AlertDialog.Builder(this)
            .setTitle("İlanı Sil")
            .setMessage("'${pet.petName}' adlı ilanı kalıcı olarak silmek istediğinize emin misiniz?")
            .setPositiveButton("Evet, Sil") { _, _ ->
                deletePet(pet.id)
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun deletePet(petId: String) {
        db.collection("pets").document(petId).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "İlan silindi", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Silme hatası: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
