package com.example.adopet.admin

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.adopet.R
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth

class AdminPanelActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_panel)

        findViewById<ImageView>(R.id.btnLogout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            finish()
        }


        findViewById<MaterialCardView>(R.id.cardListings).setOnClickListener {
            startActivity(Intent(this, ListingsAdminActivity::class.java))
        }


        findViewById<MaterialCardView>(R.id.cardUsers).setOnClickListener {
            startActivity(Intent(this, UsersAdminActivity::class.java))
        }


        findViewById<MaterialCardView>(R.id.cardRequests).setOnClickListener {
            startActivity(Intent(this, AdminRequestsActivity::class.java))
        }
    }
}
