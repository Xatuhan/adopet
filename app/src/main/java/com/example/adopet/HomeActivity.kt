package com.example.adopet

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adopet.databinding.ActivityHomeBinding
import com.google.android.gms.location.LocationServices
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var petAdapter: PetCardAdapter
    
    private val allPetsList = mutableListOf<Pet>()
    private var filteredPetsList = mutableListOf<Pet>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchWeatherInfoForCurrentUserLocation()
        } else {
            Toast.makeText(this, "Hava durumu için konum izni gerekli.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()
        
        fetchApprovedPets()
    }

    private fun setupRecyclerView() {
        petAdapter = PetCardAdapter(filteredPetsList) { pet ->
            val intent = Intent(this, PetDetailActivity::class.java)
            intent.putExtra("petId", pet.id)
            startActivity(intent)
        }
        binding.recyclerHomePets.layoutManager = LinearLayoutManager(this)
        binding.recyclerHomePets.adapter = petAdapter
    }

    private fun setupListeners() {
        binding.cardProfileIcon.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.cardChatbotIcon.setOnClickListener { // YENİ EKLENDİ
            startActivity(Intent(this, ChatbotActivity::class.java))
        }

        binding.fabAddPet.setOnClickListener {
            startActivity(Intent(this, AddPetActivity::class.java))
        }

        binding.chipGroupFilter.setOnCheckedChangeListener { group, checkedId ->
            val selectedChip = group.findViewById<Chip>(checkedId)
            if (selectedChip != null) {
                filterPets(selectedChip.text.toString())
            }
        }
        
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
             when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_favorites -> {
                    startActivity(Intent(this, FavoritesActivity::class.java))
                    true
                }
                R.id.navigation_map -> {
                    startActivity(Intent(this, MapActivity::class.java))
                    true
                }
                else -> false
            }
        }

        binding.cardWeatherIcon.setOnClickListener {
            checkLocationPermission()
        }
    }

    private fun fetchApprovedPets() {
        db.collection("pets").whereEqualTo("status", "approved").orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("HomeActivity", "Veri alınamadı", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    allPetsList.clear()
                    val pets = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Pet::class.java)?.apply {
                            id = doc.id
                        }
                    }
                    allPetsList.addAll(pets)
                    filterPets("Tümü") 
                }
            }
    }

    private fun filterPets(type: String) {
        filteredPetsList.clear()
        if (type == "Tümü") {
            filteredPetsList.addAll(allPetsList)
        } else {
            filteredPetsList.addAll(allPetsList.filter { it.type == type })
        }
        petAdapter.updateList(filteredPetsList)
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                fetchWeatherInfoForCurrentUserLocation()
            }
            else -> {
                locationPermissionRequest.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }
    }

    private fun fetchWeatherInfoForCurrentUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "Konum izni bulunamadı.", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val lat = location.latitude
                val lon = location.longitude
                fetchWeatherByLocation(lat, lon)
            } else {
                Toast.makeText(this, "Konum bilgisi alınamadı. Lütfen cihazınızın konum servislerinin açık olduğundan emin olun.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun fetchWeatherByLocation(lat: Double, lon: Double) {
        WeatherClient.api
            .getWeatherByLocation(lat, lon, BuildConfig.weather_Key)
            .enqueue(object : Callback<WeatherResponse> {

                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            showWeatherDialog(it)
                        }
                    } else {
                        Toast.makeText(
                            this@HomeActivity,
                            "Hava durumu alınamadı.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    Toast.makeText(
                        this@HomeActivity,
                        "Bağlantı hatası: ${t.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }


    private fun showWeatherDialog(weather: WeatherResponse) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_weather, null)
        val tvCity = dialogView.findViewById<TextView>(R.id.tvCityName)
        val tvTemp = dialogView.findViewById<TextView>(R.id.tvTemperature)
        val tvHumidity = dialogView.findViewById<TextView>(R.id.tvHumidity)

        tvCity.text = weather.name
        tvTemp.text = "${weather.main.temp.toInt()}°C - ${weather.weather.firstOrNull()?.description?.uppercase() ?: ""}"
        tvHumidity.text = "Nem: %${weather.main.humidity}"
        
        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Tamam", null)
            .create()
            .show()
    }
}
