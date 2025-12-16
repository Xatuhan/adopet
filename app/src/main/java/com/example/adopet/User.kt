package com.example.adopet

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Uygulama genelinde kullanılacak tek ve standart kullanıcı veri modeli.
 */
data class User(
    // Temel Kimlik Bilgileri
    val uid: String = "",
    val email: String = "",

    // Profil Bilgileri
    val name: String = "",
    val surname: String = "",
    val phone: String = "",
    val city: String = "",
    val district: String = "",
    val profileImageUrl: String = "",

    // Uygulama İçi Veriler
    val favoritePetIds: List<String> = emptyList(),

    // Meta Veriler
    val isActive: Boolean = true,
    @ServerTimestamp val createdAt: Date? = null,
    @ServerTimestamp val updatedAt: Date? = null
)
