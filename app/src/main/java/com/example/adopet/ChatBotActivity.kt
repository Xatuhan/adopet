package com.example.adopet

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ChatbotActivity : AppCompatActivity() {

    private lateinit var etUserMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var tvChat: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatbot)

        etUserMessage = findViewById(R.id.etUserMessage)
        btnSend = findViewById(R.id.btnSend)
        tvChat = findViewById(R.id.tvChat)

        btnSend.setOnClickListener {
            val msg = etUserMessage.text.toString().trim()
            if (msg.isNotEmpty()) {
                // Şimdilik sahte cevap
                val reply = "Chatbot (fake): Şimdilik backend yok ama '$msg' için kedi-köpek önereceğim :)"
                tvChat.text = "${tvChat.text}\n\nSen: $msg\n$reply"
                etUserMessage.setText("")
            }
        }
    }
}

