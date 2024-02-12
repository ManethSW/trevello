package com.example.trevello

import MarkerInfoDialogFragment
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale


class HomeActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var tvCurrentLocation: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var googleMap: GoogleMap? = null
    private var currentLocation: Location? = null

    companion object {
        private const val TAG = "HomeActivity"
    }

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
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            currentLocation = location
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            this.googleMap = googleMap
            try {
                val isDarkTheme =
                    resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

                if (isDarkTheme) {
                    googleMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.dark_map_style
                        )
                    )
                }
            } catch (e: Resources.NotFoundException) {
                showSnackbar("Error: Can't find style. Error: $e")
            }

            if (checkLocationPermission()) {
                startLocationUpdates()
            } else {
                requestLocationPermission()
            }

            entriesRef.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    showSnackbar("Error getting entries")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    for (document in snapshot.documents) {
                        val location = document.getGeoPoint("location")
                        val title = document.getString("title")

                        val latLng = LatLng(location!!.latitude, location.longitude)

                        val marker = googleMap.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title(title)
                        )
                        marker?.setIcon(BitmapFromVector(this, R.drawable.ic_entry_marker)!!)

                        if (marker != null) {
                            marker.tag = document.id
                        }
                    }

                    googleMap.setOnMarkerClickListener { marker ->
                        val tag = marker.tag
                        if (tag == null) {
                            return@setOnMarkerClickListener true
                        } else {
                            val documentId = tag as String
                            entriesRef.document(documentId).get().addOnSuccessListener { document ->
                                val title = document.getString("title")
                                val location =
                                    document.getString("address")
                                val description = document.getString("description")
                                val images =
                                    document.get("images") as ArrayList<String>
                                val dialog = MarkerInfoDialogFragment.newInstance(
                                    title!!,
                                    location!!,
                                    description!!,
                                    images
                                )
                                dialog.show(
                                    supportFragmentManager,
                                    "MarkerInfoDialogFragment"
                                )
                            }
                            val cameraUpdate =
                                CameraUpdateFactory.newLatLngZoom(marker.position, 12f)
                            googleMap.moveCamera(cameraUpdate)
                        }
                        true
                    }
                }
            }
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.menu_home
        }
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
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
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                currentLocation = location
                val currentLatLng = LatLng(it.latitude, it.longitude)
                googleMap!!.addMarker(
                    MarkerOptions()
                        .position(currentLatLng)
                        .title("Current Location")
                )?.setIcon(
                    BitmapFromVector(this, R.drawable.ic_current_marker)!!
                )

                googleMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        currentLatLng,
                        12f
                    )
                )

                tvCurrentLocation = findViewById(R.id.tvCurrentLocation)
                val geocoder = Geocoder(this@HomeActivity, Locale.getDefault())
                val addresses =
                    geocoder.getFromLocation(it.latitude, it.longitude, 1)
                val cityName = addresses?.get(0)?.locality
                val streetName = addresses?.get(0)?.thoroughfare
                val finalAddress = "$streetName, $cityName"
                tvCurrentLocation.text = finalAddress
            }
        }
    }

    private fun showSnackbar(message: String) {
        val snackbar =
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val params = snackbar.view.layoutParams as FrameLayout.LayoutParams

        params.gravity = Gravity.TOP
        snackbar.view.layoutParams = params
        snackbar.show()
        snackbar.view.postDelayed({ snackbar.dismiss() }, 4000)
    }

    private fun BitmapFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        // below line is use to generate a drawable.
        val vectorDrawable = ContextCompat.getDrawable(
            context, vectorResId
        )
        vectorDrawable!!.setBounds(
            0, 0, vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun checkLocationPermission() =
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
    }
}