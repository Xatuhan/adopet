package com.example.adopet

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adopet.databinding.ActivityChatbotBinding

class ChatbotActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatbotBinding
    private val messageList = mutableListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatbotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupSendButton()

        addInitialBotMessage("Merhaba! Ben Adopet Tavsiye Botu. Evcil hayvan bakımıyla ilgili ne merak ediyorsun?")
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messageList)
        binding.recyclerViewChat.adapter = chatAdapter
        binding.recyclerViewChat.layoutManager = LinearLayoutManager(this)
    }

    private fun setupSendButton() {
        binding.btnSend.setOnClickListener {
            val userMessage = binding.etMessage.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                addUserMessage(userMessage)
                generateBotResponse(userMessage)
                binding.etMessage.text.clear()
            }
        }
    }

    private fun addInitialBotMessage(message: String) {
        messageList.add(ChatMessage(message, false))
        chatAdapter.notifyItemInserted(messageList.size - 1)
    }

    private fun addUserMessage(message: String) {
        messageList.add(ChatMessage(message, true))
        chatAdapter.notifyItemInserted(messageList.size - 1)
        binding.recyclerViewChat.scrollToPosition(messageList.size - 1)
    }

    private fun generateBotResponse(userMessage: String) {
        val response = getBotResponse(userMessage.lowercase())
        messageList.add(ChatMessage(response, false))
        chatAdapter.notifyItemInserted(messageList.size - 1)
        binding.recyclerViewChat.scrollToPosition(messageList.size - 1)
    }

    private fun getBotResponse(question: String): String {
        return when {
            question.contains("mama") || question.contains("beslenme") ->
                "Evcil hayvanınızın yaşına ve türüne uygun, kaliteli ve dengeli bir mama seçmek çok önemlidir. Veterinerinize danışarak en doğru mamayı bulabilirsiniz."
            question.contains("eğitim") || question.contains("tuvalet") ->
                "Tuvalet eğitimi sabır gerektirir. Özellikle yavru hayvanlarda, her doğru davranıştan sonra ödüllendirme (pozitif pekiştirme) en etkili yöntemdir."
            question.contains("oyun") || question.contains("oyuncak") ->
                "Oyun oynamak, evcil hayvanınızla bağ kurmanız ve enerjisini atması için harikadır. Türüne uygun, güvenli oyuncaklar tercih etmelisiniz."
            question.contains("tüy") || question.contains("bakım") ->
                "Tüy bakımı, özellikle uzun tüylü dostlarımız için önemlidir. Düzenli tarama, tüy dökülmesini azaltır ve cilt sağlığını korur."
            question.contains("aşı") || question.contains("sağlık") ->
                "Aşılar, evcil hayvanınızı birçok tehlikeli hastalıktan korur. Aşı takvimini takip etmek ve düzenli veteriner kontrollerini aksatmamak hayati önem taşır."
            question.contains("yalnız") || question.contains("evde") ->
                "Evcil hayvanınızı evde yalnız bırakırken, sıkılmaması için oyuncaklar ve rahat bir yatak sağlamak iyi bir fikirdir. Uzun süre yalnız kalacaksa, bir yakınınızdan yardım istemeyi düşünebilirsiniz."
            question.contains("teşekkür") || question.contains("sağ ol") ->
                "Rica ederim! Başka sorun olursa çekinme."
            else ->
                "Bu konuda tam bir bilgim yok, ancak bir veterinere danışmak en doğrusu olacaktır."
        }
    }
}
