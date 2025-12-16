package com.example.adopet.admin

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object AdminGate {
    fun isCurrentUserAdmin(onResult: (Boolean) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) { onResult(false); return }

        FirebaseFirestore.getInstance()
            .collection("admins")
            .document(uid)
            .get()
            .addOnSuccessListener { snap -> onResult(snap.exists()) }
            .addOnFailureListener { onResult(false) }
    }
}
