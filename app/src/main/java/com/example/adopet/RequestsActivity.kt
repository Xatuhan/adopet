package com.example.adopet

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adopet.databinding.ActivityRequestsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class RequestsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRequestsBinding
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    private val incomingList = mutableListOf<AdoptionRequest>()
    private val outgoingList = mutableListOf<AdoptionRequest>()

    private lateinit var incomingAdapter: RequestsAdapter
    private lateinit var outgoingAdapter: RequestsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (currentUser == null) {
            finish()
            return
        }

        setupRecyclerViews()
        fetchAllMyRequests()
    }

    private fun setupRecyclerViews() {
        incomingAdapter = RequestsAdapter(incomingList, isIncoming = true,
            onAccept = { request -> acceptRequest(request) },
            onReject = { request -> rejectRequest(request) }
        )
        binding.recyclerIncomingRequests.layoutManager = LinearLayoutManager(this)
        binding.recyclerIncomingRequests.adapter = incomingAdapter

        outgoingAdapter = RequestsAdapter(outgoingList, isIncoming = false)
        binding.recyclerOutgoingRequests.layoutManager = LinearLayoutManager(this)
        binding.recyclerOutgoingRequests.adapter = outgoingAdapter
    }

    private fun fetchAllMyRequests() {
        db.collection("adoption_requests")
            .whereArrayContains("participantIds", currentUser!!.uid) // YENİ VE GÜVENLİ SORGULAMA
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(this, "Başvurular alınamadı: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                // Gelen ve giden başvuruları ayır
                incomingList.clear()
                outgoingList.clear()
                snapshot.toObjects(AdoptionRequest::class.java).forEach { request ->
                    if (request.ownerId == currentUser.uid) {
                        incomingList.add(request)
                    } else {
                        outgoingList.add(request)
                    }
                }
                incomingAdapter.notifyDataSetChanged()
                outgoingAdapter.notifyDataSetChanged()
            }
    }

    private fun rejectRequest(request: AdoptionRequest) {
        db.collection("adoption_requests").document(request.id).update("status", "rejected")
    }

    private fun acceptRequest(request: AdoptionRequest) {
        db.runBatch { batch ->
            val petRef = db.collection("pets").document(request.petId)
            batch.update(petRef, "status", "adopted")

            val acceptedRequestRef = db.collection("adoption_requests").document(request.id)
            batch.update(acceptedRequestRef, "status", "accepted")

            db.collection("adoption_requests")
                .whereEqualTo("petId", request.petId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener { otherRequests ->
                    for (doc in otherRequests) {
                        if (doc.id != request.id) {
                            batch.update(doc.reference, "status", "rejected")
                        }
                    }
                    batch.commit().addOnSuccessListener {
                        Toast.makeText(this, "Başvuru kabul edildi!", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
