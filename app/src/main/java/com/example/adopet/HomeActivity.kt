package com.example.adopet

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.adopet.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import android.widget.EditText
import android.widget.Toast
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 4. Hatalı olan ve `viewModel` referansı içeren satırları kaldırın.
        //    Artık tüm butonlara `binding` üzerinden erişeceksiniz.

        // `setOnClickListener`'ları `binding` üzerinden ayarlayın
        binding.btnAddPet.setOnClickListener {
            startActivity(Intent(this, AddPetActivity::class.java))
        }

        binding.btnMap.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }

        binding.btnChatbot.setOnClickListener {
            startActivity(Intent(this, ChatbotActivity::class.java))
        }
        binding.btnLogout.setOnClickListener {
            // Firebase'den çıkış yap
            FirebaseAuth.getInstance().signOut()
            // Ana ekrana yönlendir
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            }
        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        binding.btnMyPets.setOnClickListener {
            startActivity(Intent(this, MyPetsActivity::class.java))
        }


    }
}