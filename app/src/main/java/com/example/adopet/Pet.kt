package com.example.adopet

import com.google.firebase.firestore.GeoPoint


data class Pet(
    val id: String = "",
    val ownerId: String = "",
    val petName: String = "",
    val type: String = "",
    val breed: String = "",
    val age: Int = 0,
    val weight: Double = 0.0,
    val city: String = "",
    val district: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val lat: Double? = null,
    val lng: Double? = null,
    var photoHash: String? = null,
    var status: String = "pending",
    val timestamp: Long = 0L
)
