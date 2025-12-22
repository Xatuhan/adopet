package com.example.adopet

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val surname: String = "",
    val phone: String = "",
    val city: String = "",
    val district: String = "",
    val profileImageUrl: String = "",
    val favoritePetIds: List<String> = emptyList(),

    // DÜZELTİLDİ: Firestore'daki 'active' alanı ile eşleştirildi.
    @get:PropertyName("active")
    val isActive: Boolean = true,

    // DÜZELTİLDİ: Çökmeyi önlemek için Timestamp ile uyumlu Date? kullanıldı.
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)
