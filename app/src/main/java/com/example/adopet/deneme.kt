package com.example.adopet

import android.content.Context
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

// 'petId' ve 'context' parametre olarak fonksiyona eklenir.
private fun updateFirestore(
    db: FirebaseFirestore,
    petId: String, // Hangi dokümanın güncelleneceğini belirtmek için.
    updatedData: HashMap<String, Any>,
    context: Context // Toast mesajı göstermek için.
) {
    // petId boş olmamalıdır.
    if (petId.isBlank()) {
        Toast.makeText(context, "Hata: Geçersiz ilan kimliği!", Toast.LENGTH_LONG).show()
        return
    }

    db.collection("pets").document(petId)
        // HashMap'i Map'e cast etmeye gerek kalmayabilir, ancak güvenli olması için kalabilir.
        .update(updatedData as Map<String, Any>)
        .addOnSuccessListener {
            Toast.makeText(context, "İlan güncellendi", Toast.LENGTH_SHORT).show()
            // finish() metodunu burada çağırmak yerine, bu fonksiyonu çağırdığınız Activity veya Fragment içinde çağırmalısınız.
        }
        .addOnFailureListener {
            Toast.makeText(context, "Hata: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
        }
}
