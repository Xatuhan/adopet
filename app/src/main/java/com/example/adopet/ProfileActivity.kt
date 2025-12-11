package com.example.adopet

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var tvEmail: TextView
    private lateinit var etName: EditText
    private lateinit var etSurname: EditText
    private lateinit var etCity: EditText
    private lateinit var etDistrict: EditText
    private lateinit var etPhone: EditText

    private lateinit var btnSave: Button
    private lateinit var btnChangePassword: Button
    private lateinit var btnDeactivate: Button
    private lateinit var btnLogout: Button

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()

        tvEmail = findViewById(R.id.tvEmail)
        etName = findViewById(R.id.etName)
        etSurname = findViewById(R.id.etSurname)
        etCity = findViewById(R.id.etCity)
        etDistrict = findViewById(R.id.etDistrict)
        etPhone = findViewById(R.id.etPhone)

        btnSave = findViewById(R.id.btnSave)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnDeactivate = findViewById(R.id.btnDeactivate)
        btnLogout = findViewById(R.id.btnLogout)

        loadProfile()

        btnSave.setOnClickListener { saveProfile() }
        btnChangePassword.setOnClickListener { changePasswordDialog() }
        btnDeactivate.setOnClickListener { deactivateAccountDialog() }
        btnLogout.setOnClickListener { logout() }
    }

    private fun loadProfile() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Oturum bulunamadı", Toast.LENGTH_SHORT).show()
            goToLogin()
            return
        }

        tvEmail.text = user.email ?: ""

        val uid = user.uid

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    etName.setText(doc.getString("name") ?: "")
                    etSurname.setText(doc.getString("surname") ?: "")
                    etCity.setText(doc.getString("city") ?: "")
                    etDistrict.setText(doc.getString("district") ?: "")
                    etPhone.setText(doc.getString("phone") ?: "")
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Profil yüklenemedi: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun saveProfile() {
        val user = auth.currentUser ?: return
        val uid = user.uid

        val data = hashMapOf(
            "email" to (user.email ?: ""),
            "name" to etName.text.toString().trim(),
            "surname" to etSurname.text.toString().trim(),
            "city" to etCity.text.toString().trim(),
            "district" to etDistrict.text.toString().trim(),
            "phone" to etPhone.text.toString().trim(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        db.collection("users").document(uid)
            .set(data, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Profil güncellendi", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Güncelleme hatası: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun changePasswordDialog() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Oturum bulunamadı", Toast.LENGTH_SHORT).show()
            return
        }

        val etNewPass = EditText(this)
        etNewPass.hint = "Yeni şifre"

        AlertDialog.Builder(this)
            .setTitle("Şifre Değiştir")
            .setView(etNewPass)
            .setPositiveButton("Kaydet") { _: DialogInterface, _: Int ->
                val newPass = etNewPass.text.toString().trim()
                if (newPass.length < 6) {
                    Toast.makeText(this, "Şifre en az 6 karakter olmalı", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                user.updatePassword(newPass)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Şifre güncellendi", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Şifre güncellenemedi: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun deactivateAccountDialog() {
        AlertDialog.Builder(this)
            .setTitle("Hesabı Pasifleştir")
            .setMessage("Hesabınızı pasif hale getirmek istediğinize emin misiniz?")
            .setPositiveButton("Evet") { _, _ ->
                deactivateAccount()
            }
            .setNegativeButton("Hayır", null)
            .show()
    }

    private fun deactivateAccount() {
        val user = auth.currentUser ?: return
        val uid = user.uid

        db.collection("users").document(uid)
            .update("isActive", false)
            .addOnSuccessListener {
                Toast.makeText(this, "Hesabınız pasif hale getirildi", Toast.LENGTH_LONG).show()
                auth.signOut()
                goToLogin()
            }
            .addOnFailureListener {
                Toast.makeText(this, "İşlem başarısız: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun logout() {
        auth.signOut()
        goToLogin()
    }

    private fun goToLogin() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}
