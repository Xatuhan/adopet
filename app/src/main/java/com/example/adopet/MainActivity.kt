package com.example.adopet

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.adopet.admin.AdminGate
import com.example.adopet.admin.AdminPanelActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth


    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoRegister: TextView
    private lateinit var tvForgotPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoRegister = findViewById(R.id.btnGoRegister)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)

        if (auth.currentUser != null) {
            redirectUserBasedOnRole()
        }


        btnLogin.setOnClickListener { loginUser() }
        btnGoRegister.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }
        tvForgotPassword.setOnClickListener { showForgotPasswordDialog() }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
            return
        }
        if (!isValidEmail(email)){
            Toast.makeText(this, "Geçerli bir email adresi girin.", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Toast.makeText(this, "Giriş başarılı", Toast.LENGTH_SHORT).show()
                redirectUserBasedOnRole()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Giriş hatası: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Şifremi Unuttum")

        val view = layoutInflater.inflate(R.layout.dialog_forgot_password, null)
        val etEmailDialog = view.findViewById<EditText>(R.id.etEmailDialog)
        builder.setView(view)

        builder.setPositiveButton("Gönder") { _, _ ->
            val email = etEmailDialog.text.toString().trim()
            if (email.isNotEmpty() && isValidEmail(email)) {
                sendPasswordResetEmail(email)
            } else {
                Toast.makeText(this, "Lütfen geçerli bir e-posta adresi girin.", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("İptal", null)
        builder.create().show()
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Toast.makeText(this, "Şifre sıfırlama e-postası gönderildi.", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Hata: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun redirectUserBasedOnRole() {
        AdminGate.isCurrentUserAdmin { isAdmin ->
            if (isAdmin) {
                val intent = Intent(this, AdminPanelActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            }
            finish()
        }
    }
}
