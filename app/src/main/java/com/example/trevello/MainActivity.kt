package com.example.trevello

import android.app.ActivityOptions
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class MainActivity : AppCompatActivity() {
    private val PREFS_NAME = "theme_prefs"
    private val KEY_THEME = "theme_key"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the saved theme from SharedPreferences
        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val savedThemeMode = sharedPreferences.getInt(KEY_THEME, -1)

        if (savedThemeMode == -1) {
            // If no theme is saved, set the default theme based on the device's night mode setting
            val defaultNightMode = when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> AppCompatDelegate.MODE_NIGHT_YES
                Configuration.UI_MODE_NIGHT_NO -> AppCompatDelegate.MODE_NIGHT_NO
                else -> AppCompatDelegate.MODE_NIGHT_NO
            }
            AppCompatDelegate.setDefaultNightMode(defaultNightMode)
        } else {
            // If a theme is saved, apply it
            AppCompatDelegate.setDefaultNightMode(savedThemeMode)
        }

        window.navigationBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorSecondary)
        setContentView(R.layout.activity_main)

        val createAccountButton = findViewById<Button>(R.id.bCreateAccount)
        val loginButton = findViewById<Button>(R.id.bLogin)

        createAccountButton.setOnClickListener {
            // Start the create account activity or implement create account logic here
            val intent = Intent(this, RegisterActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(this, R.anim.slide_in_right, R.anim.slide_out_left).toBundle()
            startActivity(intent, options)
        }

        loginButton.setOnClickListener {
            // Start the login activity or implement login logic here
            val intent = Intent(this, RegisterActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(this, R.anim.slide_in_right, R.anim.slide_out_left).toBundle()
            startActivity(intent, options)
        }

        val btnToggleTheme = findViewById<Button>(R.id.btnToggleTheme)
        btnToggleTheme.setOnClickListener {
            val mode = when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> AppCompatDelegate.MODE_NIGHT_NO
                Configuration.UI_MODE_NIGHT_NO -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_NO
            }

            // Save the new theme in SharedPreferences
            val editor = sharedPreferences.edit()
            editor.putInt(KEY_THEME, mode)
            editor.apply()

            // Apply the new theme
            AppCompatDelegate.setDefaultNightMode(mode)

            // Recreate all activities
            recreate()
        }
    }
}

// Code to toggle dark mode
//val btnToggleTheme = findViewById<Button>(R.id.btnToggleTheme)
//btnToggleTheme.setOnClickListener {
//    val mode = when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
//        Configuration.UI_MODE_NIGHT_YES -> AppCompatDelegate.MODE_NIGHT_NO
//        Configuration.UI_MODE_NIGHT_NO -> AppCompatDelegate.MODE_NIGHT_YES
//        else -> AppCompatDelegate.MODE_NIGHT_NO
//    }
//
//    // Save the new theme in SharedPreferences
//    val editor = sharedPreferences.edit()
//    editor.putInt(KEY_THEME, mode)
//    editor.apply()
//
//    // Apply the new theme
//    AppCompatDelegate.setDefaultNightMode(mode)
//
//    // Recreate all activities
//    recreate()
//}

// Code to setup maps
//class MainActivity : AppCompatActivity(), OnMapReadyCallback {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(this)
//    }
//
//    override fun onMapReady(googleMap: GoogleMap) {
//        googleMap.addMarker(MarkerOptions()
//            .position(LatLng(-34.0, 151.0))
//            .title("Sydney"))
//    }
//}