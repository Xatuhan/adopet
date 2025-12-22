package com.example.adopet


data class AdoptionRequest(
    val id: String = "",
    val petId: String = "",
    val petName: String = "",
    val petImageUrl: String = "",
    val ownerId: String = "",
    val requesterId: String = "",
    val requesterName: String = "",
    val status: String = "pending",
    val timestamp: Long = 0L,
    val participantIds: List<String> = emptyList()
)
