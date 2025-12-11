package com.example.adopet

import android.location.Geocoder
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

class MapSelectLocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_select_location)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // AddPetActivity'den gelen il / ilçe
        val city = intent.getStringExtra("city") ?: ""
        val district = intent.getStringExtra("district") ?: ""

        // Şöyle bir adres stringi oluştur: "Kadıköy, İstanbul, Türkiye" gibi
        val addressText = buildString {
            if (district.isNotEmpty()) append(district)
            if (city.isNotEmpty()) {
                if (isNotEmpty()) append(", ")
                append(city)
            }
            append(", Türkiye")
        }

        // Geocoder ile adresi lat/lng'e çevir
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val results = geocoder.getFromLocationName(addressText, 1)

            if (!results.isNullOrEmpty()) {
                val loc = results[0]
                val target = LatLng(loc.latitude, loc.longitude)

                // İlçeye zoom yap (13f civarı şehir/ilçe seviyesi için iyi)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(target, 13f))
            } else {
                // Bulamazsa fallback: Türkiye ortası vs.
                val turkey = LatLng(39.0, 35.0)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(turkey, 5f))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val turkey = LatLng(39.0, 35.0)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(turkey, 5f))
        }

        // Buradan sonra:
        // - Haritaya tıklanınca seçili konumu kaydedebilirsin
        // - Uzun basış ile marker koyup lat/lng alabilirsin
    }
}
