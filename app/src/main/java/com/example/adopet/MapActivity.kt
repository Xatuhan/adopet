package com.example.adopet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private lateinit var mMap: GoogleMap
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnInfoWindowClickListener(this) // Set the listener for info window clicks

        val turkey = LatLng(39.0, 35.0)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(turkey, 5f))

        fetchAndDisplayApprovedPets()
    }

    private fun fetchAndDisplayApprovedPets() {
        db.collection("pets")
            .whereEqualTo("status", "approved") // Only show approved pets
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val pet = document.toObject(Pet::class.java)

                    if (pet.latitude != 0.0 && pet.longitude != 0.0) {
                        val position = LatLng(pet.latitude, pet.longitude)
                        val marker = mMap.addMarker(
                            MarkerOptions()
                                .position(position)
                                .title(pet.petName)
                                .snippet(pet.type)
                        )
                        // Store the Pet ID in the marker's tag for later retrieval
                        marker?.tag = pet.id
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("MapActivity", "Dökümanları çekerken hata: ", exception)
            }
    }

    override fun onInfoWindowClick(marker: Marker) {
        val petId = marker.tag as? String
        if (petId == null) {
            Log.e("MapActivity", "Marker tag is null, cannot open detail activity.")
            return
        }


        val intent = Intent(this, PetDetailActivity::class.java)
        intent.putExtra("petId", petId)
        startActivity(intent)
    }
}
