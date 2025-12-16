package com.example.adopet

data class AdoptionRequest(
    val id: String = "",
    val petId: String = "",
    val petName: String = "",
    val petImageUrl: String = "",
    val ownerId: String = "",
    val requesterId: String = "",
    val requesterName: String = "",
    val status: String = "pending", // pending, accepted, rejected
    val timestamp: Long = System.currentTimeMillis(),
    // YENİ EKLENEN ALAN: Sorguları basitleştirmek ve güvenliği artırmak için.
    val participantIds: List<String> = listOf()
)
