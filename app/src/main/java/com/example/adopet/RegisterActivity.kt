package com.example.adopet

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    // DÜZELTİLDİ: Yeni arayüzdeki TÜM alanlar
    private lateinit var etName: TextInputEditText
    private lateinit var etSurname: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etCity: TextInputEditText
    private lateinit var btnRegister: Button
    private lateinit var btnGoLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        // DÜZELTİLDİ: Yeni arayüzdeki TÜM ID'lere göre bağlama
        etName = findViewById(R.id.etName)
        etSurname = findViewById(R.id.etSurname)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etPhone = findViewById(R.id.etPhone)
        etCity = findViewById(R.id.etCity)
        btnRegister = findViewById(R.id.btnRegister)
        btnGoLogin = findViewById(R.id.btnGoLogin)

        btnRegister.setOnClickListener { registerUser() }
        btnGoLogin.setOnClickListener { finish() }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun registerUser() {
        val name = etName.text.toString().trim()
        val surname = etSurname.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val city = etCity.text.toString().trim()

        if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Ad, Soyad, E-posta ve Şifre alanları zorunludur.", Toast.LENGTH_LONG).show()
            return
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Lütfen geçerli bir e-posta adresi girin.", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Şifre en az 6 karakter olmalıdır.", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user ?: return@addOnSuccessListener


                val newUser = User(
                    uid = firebaseUser.uid,
                    email = email,
                    name = name,
                    surname = surname,
                    phone = phone,
                    city = city,
                    district = "",
                    profileImageUrl = "",
                    favoritePetIds = emptyList(),
                    isActive = true
                )

                db.collection("users").document(firebaseUser.uid).set(newUser)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Hesap başarıyla oluşturuldu!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finishAffinity()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Profil bilgileri kaydedilemedi: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                if (e.localizedMessage?.contains("already in use") == true) {
                    Toast.makeText(this, "Bu e-posta adresi ile zaten bir hesap mevcut.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Kayıt hatası: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
    }
}
