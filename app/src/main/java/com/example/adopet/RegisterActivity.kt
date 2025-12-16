package com.example.adopet

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    // Tüm EditText alanlarını tanımla
    private lateinit var etName: EditText
    private lateinit var etSurname: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etPhone: EditText
    private lateinit var etCity: EditText
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        // Arayüz elemanlarını bağla
        etName = findViewById(R.id.etName)
        etSurname = findViewById(R.id.etSurname)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etPhone = findViewById(R.id.etPhone)
        etCity = findViewById(R.id.etCity)
        btnRegister = findViewById(R.id.btnRegister)

        btnRegister.setOnClickListener {
            registerUser()
        }
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

        // Gerekli alanların kontrolü
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

        // Adım 1: Firebase Authentication ile kullanıcı oluştur
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user
                if (firebaseUser == null) {
                    Toast.makeText(this, "Kullanıcı oluşturulamadı, lütfen tekrar deneyin.", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                // Adım 2: Standart User modelini kullanarak bir nesne oluştur
                val newUser = User(
                    uid = firebaseUser.uid,
                    email = email,
                    name = name,
                    surname = surname,
                    phone = phone,
                    city = city
                )

                // Adım 3: Bu nesneyi Firestore'a kaydet
                db.collection("users").document(firebaseUser.uid).set(newUser)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Hesap başarıyla oluşturuldu!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finishAffinity() // Tüm geçmiş aktiviteleri temizle
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
