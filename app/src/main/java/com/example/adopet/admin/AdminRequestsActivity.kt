package com.example.adopet.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adopet.AdoptionRequest
import com.example.adopet.RequestsAdapter
import com.example.adopet.databinding.ActivityAdminRequestsBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminRequestsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminRequestsBinding
    private val db = FirebaseFirestore.getInstance()

    private val pendingRequestsList = mutableListOf<AdoptionRequest>()
    private lateinit var requestsAdapter: RequestsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        fetchPendingRequests()
    }

    private fun setupRecyclerView() {
        requestsAdapter = RequestsAdapter(
            pendingRequestsList,
            isIncoming = true, // Admin view, so all are treat as incoming to show buttons
            onAccept = { request -> acceptRequest(request) },
            onReject = { request -> rejectRequest(request) }
        )
        binding.recyclerAdminRequests.layoutManager = LinearLayoutManager(this)
        binding.recyclerAdminRequests.adapter = requestsAdapter
    }

    private fun fetchPendingRequests() {
        db.collection("adoption_requests")
            .whereEqualTo("status", "pending")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(this, "Başvurular alınamadı: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                pendingRequestsList.clear()
                pendingRequestsList.addAll(snapshot.toObjects(AdoptionRequest::class.java))
                requestsAdapter.notifyDataSetChanged()
            }
    }

    private fun rejectRequest(request: AdoptionRequest) {
        db.collection("adoption_requests").document(request.id)
            .update("status", "rejected")
            .addOnSuccessListener {
                Toast.makeText(this, "Başvuru reddedildi.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun acceptRequest(request: AdoptionRequest) {
        val batch = db.batch()

        // 1. Update the pet's status to "adopted"
        val petRef = db.collection("pets").document(request.petId)
        batch.update(petRef, "status", "adopted")

        // 2. Update the accepted request's status to "accepted"
        val acceptedRequestRef = db.collection("adoption_requests").document(request.id)
        batch.update(acceptedRequestRef, "status", "accepted")

        // 3. Find and reject all other pending requests for the same pet
        db.collection("adoption_requests")
            .whereEqualTo("petId", request.petId)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { otherRequestsSnapshot ->
                for (document in otherRequestsSnapshot) {
                    // Exclude the one we just accepted
                    if (document.id != request.id) {
                        batch.update(document.reference, "status", "rejected")
                    }
                }
                
                // Commit the batch
                batch.commit()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Başvuru kabul edildi ve diğerleri reddedildi.", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "İşlem tamamlanamadı: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                // Fallback to commit only the first two operations if querying others fails
                batch.commit()
                Toast.makeText(this, "Ana başvuru kabul edildi, diğerleri kontrol edilemedi.", Toast.LENGTH_LONG).show()
            }
    }
}
