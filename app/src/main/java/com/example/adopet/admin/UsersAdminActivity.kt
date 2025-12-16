package com.example.adopet.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adopet.R
import com.example.adopet.databinding.ActivityAdminListBinding
import com.google.firebase.firestore.FirebaseFirestore

data class AppUser(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val city: String = "",
    val district: String = "",
    val isActive: Boolean = true
)

class UsersAdminActivity : AppCompatActivity() {

    private lateinit var vb: ActivityAdminListBinding
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val items = mutableListOf<AppUser>()
    private lateinit var adapter: UsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivityAdminListBinding.inflate(layoutInflater)
        setContentView(vb.root)

        vb.titleText.text = "Kullanıcılar"
        vb.recycler.layoutManager = LinearLayoutManager(this)

        adapter = UsersAdapter(
            items,
            onEdit = { showEditDialog(it) },
            onDeactivate = { setActive(it, false) },
            onActivate = { setActive(it, true) },
            onDeleteDoc = { deleteUserDoc(it) }
        )
        vb.recycler.adapter = adapter

        listen()
    }

    private fun listen() {
        db.collection("users")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    Toast.makeText(this, err.message, Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                items.clear()
                snap?.documents?.forEach { d ->
                    val x = AppUser(
                        uid = d.id,
                        fullName = d.getString("fullName") ?: "",
                        email = d.getString("email") ?: "",
                        phone = d.getString("phone") ?: "",
                        city = d.getString("city") ?: "",
                        district = d.getString("district") ?: "",
                        isActive = d.getBoolean("isActive") ?: true
                    )
                    items.add(x)
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun showEditDialog(u: AppUser) {
        val v = LayoutInflater.from(this).inflate(R.layout.dialog_edit_user, null)
        val etName = v.findViewById<EditText>(R.id.etFullName)
        val etEmail = v.findViewById<EditText>(R.id.etEmail)
        val etPhone = v.findViewById<EditText>(R.id.etPhone)
        val etCity = v.findViewById<EditText>(R.id.etCity)
        val etDistrict = v.findViewById<EditText>(R.id.etDistrict)

        etName.setText(u.fullName)
        etEmail.setText(u.email)
        etPhone.setText(u.phone)
        etCity.setText(u.city)
        etDistrict.setText(u.district)

        AlertDialog.Builder(this)
            .setTitle("Kullanıcı Düzenle")
            .setView(v)
            .setPositiveButton("Kaydet") { _, _ ->
                db.collection("users").document(u.uid).update(
                    mapOf(
                        "fullName" to etName.text.toString().trim(),
                        "email" to etEmail.text.toString().trim(),
                        "phone" to etPhone.text.toString().trim(),
                        "city" to etCity.text.toString().trim(),
                        "district" to etDistrict.text.toString().trim()
                    )
                ).addOnSuccessListener {
                    Toast.makeText(this, "Kaydedildi", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun setActive(u: AppUser, active: Boolean) {
        db.collection("users").document(u.uid).update("isActive", active)
            .addOnSuccessListener {
                Toast.makeText(this, if (active) "Aktif edildi" else "Pasif edildi", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
            }
    }

    private fun deleteUserDoc(u: AppUser) {
        AlertDialog.Builder(this)
            .setTitle("Kullanıcı Kaydı Silinsin mi?")
            .setMessage("Bu işlem Firestore 'users/${u.uid}' dokümanını siler. (Auth hesabını silmez)")
            .setPositiveButton("Sil") { _, _ ->
                db.collection("users").document(u.uid).delete()
                    .addOnSuccessListener { Toast.makeText(this, "Silindi", Toast.LENGTH_SHORT).show() }
                    .addOnFailureListener { Toast.makeText(this, it.message, Toast.LENGTH_LONG).show() }
            }
            .setNegativeButton("Vazgeç", null)
            .show()
    }
}
