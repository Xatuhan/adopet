package com.example.adopet

data class Pet(
    val id: String = "",
    val ownerId: String = "",
    val petName: String = "",
    val type: String = "",
    val breed: String = "",
    val age: Int = 0,
    val weight: Double = 0.0,
    val city: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val lat: Double? = null,
    val lng: Double? = null
)
