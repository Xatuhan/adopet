package com.example.adopet

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.adopet.databinding.ActivityMapSelectLocationBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale

class MapSelectLocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapSelectLocationBinding
    private lateinit var mMap: GoogleMap
    private var selectedLatLng: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapSelectLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnSaveLocation.setOnClickListener {
            saveLocation()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setupMap()
    }

    private fun setupMap() {
        // AddPetActivity'den gelen il / ilçe ile haritayı ortala
        centerMapBasedOnIntent()

        // Haritaya uzun basıldığında işaretçi ekle
        mMap.setOnMapLongClickListener { latLng ->
            selectedLatLng = latLng
            mMap.clear() // Önceki işaretçileri temizle
            mMap.addMarker(MarkerOptions().position(latLng).title("Seçilen Konum"))
            Toast.makeText(this, "Konum seçildi!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun centerMapBasedOnIntent() {
        val city = intent.getStringExtra("city") ?: ""
        val district = intent.getStringExtra("district") ?: ""

        val addressText = "$district, $city, Türkiye"

        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val results = geocoder.getFromLocationName(addressText, 1)

            if (!results.isNullOrEmpty()) {
                val loc = results[0]
                val target = LatLng(loc.latitude, loc.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(target, 13f))
            } else {
                val turkey = LatLng(39.0, 35.0)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(turkey, 5f))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val turkey = LatLng(39.0, 35.0)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(turkey, 5f))
        }
    }

    private fun saveLocation() {
        if (selectedLatLng == null) {
            Toast.makeText(this, "Lütfen haritaya uzun basarak bir konum seçin.", Toast.LENGTH_LONG).show()
            return
        }

        val resultIntent = Intent()
        resultIntent.putExtra("latitude", selectedLatLng!!.latitude)
        resultIntent.putExtra("longitude", selectedLatLng!!.longitude)
        setResult(Activity.RESULT_OK, resultIntent)
        finish() // Bu aktiviteyi kapat ve sonucu AddPetActivity'ye gönder
    }
}
