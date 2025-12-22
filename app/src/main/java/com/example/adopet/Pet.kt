package com.example.adopet



data class Pet(
    var id: String = "",
    val petName: String = "",
    val ownerId: String = "",
    val imageUrl: String = "",
    val type: String = "",
    val breed: String = "",
    val gender: String = "",
    val city: String = "",
    val district: String = "",
    val description: String = "",
    val age: Int = 0,
    val status: String = "",
    val timestamp: Long = 0L,

    val lat: Double = 0.0,
    val lng: Double = 0.0
)
