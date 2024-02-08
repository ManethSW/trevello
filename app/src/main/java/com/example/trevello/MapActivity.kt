package com.example.trevello

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

// In your new MapActivity
class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        window.navigationBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorSecondary)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            try {
                // Check if the current theme is dark
                val isDarkTheme = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

                // If the current theme is dark, apply the dark map style
                if (isDarkTheme) {
                    val success = googleMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.dark_map_style))

                    if (!success) {
                        Log.e(ContentValues.TAG, "Style parsing failed.")
                    }
                }
            } catch (e: Resources.NotFoundException) {
                Log.e(ContentValues.TAG, "Can't find style. Error: ", e)
            }
        }
        mapFragment.getMapAsync(this)

        // Initialize the SDK
//        Places.initialize(applicationContext, getString(R.string.my_api_key))

        // Create a new Places client instance
//        val placesClient = Places.createClient(this)

        val autocompleteFragment = supportFragmentManager
            .findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val returnIntent = Intent()
                val location = Location("")
                location.latitude = place.latLng?.latitude ?: 0.0
                location.longitude = place.latLng?.longitude ?: 0.0
                returnIntent.putExtra("location", location)
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            }

            override fun onError(status: com.google.android.gms.common.api.Status) {
                // Handle the error
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMapClickListener { latLng ->
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(latLng))
            val returnIntent = Intent()
            val location = Location("")
            location.latitude = latLng.latitude
            location.longitude = latLng.longitude
            returnIntent.putExtra("location", location)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
    }
}