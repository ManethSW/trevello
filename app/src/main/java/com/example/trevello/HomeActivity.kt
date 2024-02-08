package com.example.trevello

import android.location.Geocoder
import java.util.Locale
import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.navigationBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorSecondary)
        setContentView(R.layout.activity_home)

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val uid = auth.currentUser?.uid
        val entriesRef = db.collection("users").document(uid!!).collection("entries")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            this.googleMap = googleMap
            try {
                // Check if the current theme is dark
                val isDarkTheme = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

                // If the current theme is dark, apply the dark map style
                if (isDarkTheme) {
                    val success = googleMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.dark_map_style))

                    if (!success) {
                        Log.e(TAG, "Style parsing failed.")
                    }
                }
            } catch (e: Resources.NotFoundException) {
                Log.e(TAG, "Can't find style. Error: ", e)
            }
        }
        mapFragment.getMapAsync(this)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.menu_home
        }
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    // Respond to navigation item 1 click
                    true
                }
                R.id.menu_activity -> {
                    // Respond to navigation item 2 click
                    true
                }
                R.id.menu_add -> {
                    val intent = Intent(this, AddEntryActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.menu_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Request location permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            startLocationUpdates(mapFragment)
        }

        entriesRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && googleMap != null) {
                // Clear existing markers
                googleMap!!.clear()

                // Iterate over the documents in the snapshot
                for (document in snapshot.documents) {
                    // Get the location and title of the entry
                    val location = document.getGeoPoint("location")
                    val title = document.getString("title")

                    // Log the location and title
                    Log.d(TAG, "Location: $location, Title: $title")

                    // Create a LatLng object from the location
                    val latLng = LatLng(location!!.latitude, location.longitude)

                    // Add a marker to the map at the LatLng position with the title
                    googleMap!!.addMarker(MarkerOptions().position(latLng).title(title))
                }
            } else {
                Log.d(TAG, "Current data: null")
            }
        }
    }

    private fun startLocationUpdates(mapFragment: SupportMapFragment) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                mapFragment.getMapAsync { googleMap ->
                    val currentLatLng = LatLng(location.latitude, location.longitude)
//                    googleMap.clear() // Clear old markers
                    googleMap.addMarker(MarkerOptions().position(currentLatLng).title("Current Location")) // Add a marker for current location
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f)) // Move and zoom the camera to current location

                    // Update the TextView with the current location
                    val tvCurrentLocation = findViewById<TextView>(R.id.tvCurrentLocation)

                    // Use Geocoder to get the location name
                    val geocoder = Geocoder(this@HomeActivity, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val cityName = addresses?.get(0)?.locality
                    val streetName = addresses?.get(0)?.thoroughfare
                    tvCurrentLocation.text = "$streetName, $cityName"
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment
                startLocationUpdates(mapFragment)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
    }
}