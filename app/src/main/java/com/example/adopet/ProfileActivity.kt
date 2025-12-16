package com.example.adopet

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.adopet.utils.PinataUploader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    // Views
    private lateinit var imgProfile: ImageView
    private lateinit var tvEmail: TextView
    private lateinit var etName: EditText
    private lateinit var etSurname: EditText
    private lateinit var etCity: EditText
    private lateinit var etDistrict: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnChangePhoto: Button
    private lateinit var btnSave: Button
    private lateinit var btnMyRequests: Button
    private lateinit var btnChangePassword: Button
    private lateinit var btnDeactivate: Button
    private lateinit var btnLogout: Button

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            pickImageLauncher.launch("image/*")
        } else {
            Toast.makeText(this, "Galeriye erişim izni gerekli.", Toast.LENGTH_LONG).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { uploadProfilePicture(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            goToLogin()
            return
        }

        bindViews()
        setupListeners()
        loadProfile()
    }

    private fun bindViews() {
        imgProfile = findViewById(R.id.imgProfile)
        tvEmail = findViewById(R.id.tvEmail)
        etName = findViewById(R.id.etName)
        etSurname = findViewById(R.id.etSurname)
        etCity = findViewById(R.id.etCity)
        etDistrict = findViewById(R.id.etDistrict)
        etPhone = findViewById(R.id.etPhone)
        btnChangePhoto = findViewById(R.id.btnChangePhoto)
        btnSave = findViewById(R.id.btnSave)
        btnMyRequests = findViewById(R.id.btnMyRequests)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnDeactivate = findViewById(R.id.btnDeactivate)
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun setupListeners() {
        btnChangePhoto.setOnClickListener { checkPermissionAndOpenGallery() }
        btnSave.setOnClickListener { saveProfile() }
        btnMyRequests.setOnClickListener { startActivity(Intent(this, RequestsActivity::class.java)) }
        btnChangePassword.setOnClickListener { changePasswordDialog() }
        btnDeactivate.setOnClickListener { deactivateAccountDialog() }
        btnLogout.setOnClickListener { logout() }
    }

    private fun checkPermissionAndOpenGallery() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                pickImageLauncher.launch("image/*")
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun loadProfile() {
        val user = auth.currentUser ?: return
        tvEmail.text = user.email ?: ""

        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val userProfile = doc.toObject(User::class.java)
                    userProfile?.let {
                        etName.setText(it.name)
                        etSurname.setText(it.surname)
                        etCity.setText(it.city)
                        etDistrict.setText(it.district)
                        etPhone.setText(it.phone)
                        if (it.profileImageUrl.isNotEmpty()) {
                            Glide.with(this).load(it.profileImageUrl).circleCrop().into(imgProfile)
                        } else {
                            imgProfile.setImageResource(R.mipmap.ic_launcher_round)
                        }
                    }
                } else {
                    // Kullanıcı dokümanı yoksa, bu normal bir durum olabilir. 
                    // saveProfile fonksiyonu bu durumu ele alacaktır.
                    Log.w("ProfileActivity", "Kullanıcı dokümanı bulunamadı. Kaydet butonuna basıldığında oluşturulacak.")
                }
            }.addOnFailureListener { 
                Toast.makeText(this, "Profil yüklenemedi: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }
    
    private fun uploadProfilePicture(uri: Uri) {
        if (BuildConfig.PINATA_JWT.isNullOrEmpty() || BuildConfig.PINATA_JWT == "YOUR_PINATA_JWT_HERE") {
            Toast.makeText(this, "Pinata JWT anahtarı ayarlanmamış!", Toast.LENGTH_LONG).show()
            return
        }

        Toast.makeText(this, "Profil resmi yükleniyor...", Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.Main).launch {
            val uploader = PinataUploader(BuildConfig.PINATA_JWT)
            val cid = uploader.uploadImage(this@ProfileActivity, uri)

            if (cid == null) {
                Toast.makeText(this@ProfileActivity, "Fotoğraf yüklenemedi.", Toast.LENGTH_LONG).show()
                return@launch
            }

            val imageUrl = "https://gateway.pinata.cloud/ipfs/$cid"
            saveImageUrlToFirestore(imageUrl)
        }
    }

    private fun saveImageUrlToFirestore(imageUrl: String) {
        val user = auth.currentUser ?: return
        val data = mapOf("profileImageUrl" to imageUrl)

        // Her zaman set(merge) kullanarak doküman yoksa bile oluşturulmasını veya güncellenmesini sağla.
        db.collection("users").document(user.uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Profil resmi güncellendi.", Toast.LENGTH_SHORT).show()
                Glide.with(this).load(imageUrl).circleCrop().into(imgProfile)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "URL kaydedilemedi: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun saveProfile() {
        val user = auth.currentUser ?: return
        
        val profileData = mapOf(
            "uid" to user.uid,
            "email" to (user.email ?: ""),
            "name" to etName.text.toString().trim(),
            "surname" to etSurname.text.toString().trim(),
            "city" to etCity.text.toString().trim(),
            "district" to etDistrict.text.toString().trim(),
            "phone" to etPhone.text.toString().trim(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        // Her zaman set(merge) kullan. Bu, doküman yoksa oluşturur, varsa günceller.
        // Bu, "iki ayrı kişi" sorununu ve veri kaybını kesin olarak önler.
        db.collection("users").document(user.uid)
            .set(profileData, SetOptions.merge())
            .addOnSuccessListener { 
                Toast.makeText(this, "Profil güncellendi", Toast.LENGTH_SHORT).show() 
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Güncelleme hatası: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
    
    private fun changePasswordDialog() {
         val user = auth.currentUser ?: return
        val etNewPass = EditText(this).apply { hint = "Yeni şifre" }

        AlertDialog.Builder(this)
            .setTitle("Şifre Değiştir")
            .setView(etNewPass)
            .setPositiveButton("Kaydet") { _, _ ->
                val newPass = etNewPass.text.toString().trim()
                if (newPass.length < 6) {
                    Toast.makeText(this, "Şifre en az 6 karakter olmalı", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                user.updatePassword(newPass)
                    .addOnSuccessListener { Toast.makeText(this, "Şifre güncellendi", Toast.LENGTH_SHORT).show() }
                    .addOnFailureListener { Toast.makeText(this, "Şifre güncellenemedi: ${it.localizedMessage}", Toast.LENGTH_LONG).show() }
            }
            .setNegativeButton("İptal", null).show()
    }

    private fun deactivateAccountDialog() {
        AlertDialog.Builder(this)
            .setTitle("Hesabı Pasifleştir")
            .setMessage("Hesabınızı pasif hale getirmek istediğinize emin misiniz?")
            .setPositiveButton("Evet") { _, _ -> deactivateAccount() }
            .setNegativeButton("Hayır", null).show()
    }

    private fun deactivateAccount() {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).update("isActive", false)
            .addOnSuccessListener {
                Toast.makeText(this, "Hesabınız pasif hale getirildi", Toast.LENGTH_LONG).show()
                auth.signOut()
                goToLogin()
            }.addOnFailureListener { Toast.makeText(this, "İşlem başarısız: ${it.localizedMessage}", Toast.LENGTH_LONG).show() }
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
