package com.example.trevello

import android.app.ActivityOptions
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class MainActivity : AppCompatActivity() {
    private val PREFS_NAME = "theme_prefs"
    private val KEY_THEME = "theme_key"
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Check if user is signed
        val currentUser = auth.currentUser
        updateUI(currentUser)

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
            val intent = Intent(this, RegisterActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(this, R.anim.slide_in_right, R.anim.slide_out_left).toBundle()
            startActivity(intent, options)
            Handler(Looper.getMainLooper()).postDelayed({
                finish()
            }, 500)
        }

        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(this, R.anim.slide_in_right, R.anim.slide_out_left).toBundle()
            startActivity(intent, options)
            Handler(Looper.getMainLooper()).postDelayed({
                finish()
            }, 500)
        }
    }
    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}