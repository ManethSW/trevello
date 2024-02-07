package com.example.trevello

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.request.target.Target
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.switchmaterial.SwitchMaterial

class ProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var imageView: ImageView
    private lateinit var layoutParams: LinearLayout.LayoutParams
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var ivTheme: ImageView
    private lateinit var tvTheme: TextView
    private lateinit var switchToggleTheme: SwitchMaterial
    private lateinit var ibLogout: ImageButton
    private val PREFS_NAME = "theme_prefs"
    private val KEY_THEME = "theme_key"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance()
        imageView = findViewById(R.id.ivProfile)
        layoutParams = imageView.layoutParams as LinearLayout.LayoutParams
        tvName = findViewById(R.id.tvFullName)
        tvEmail = findViewById(R.id.tvEmail)
        ivTheme = findViewById(R.id.ivTheme)
        tvTheme = findViewById(R.id.tvTheme)
        switchToggleTheme = findViewById(R.id.switchToggleTheme)
        ibLogout = findViewById(R.id.ibLogout)

        updateThemeUi(ivTheme, tvTheme)

        window.navigationBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorSecondary)

        fetchDataAndDisplay()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.menu_profile
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.menu_activity -> {
                    // Respond to navigation item 2 click
                    true
                }
                R.id.menu_add -> {
                    // Respond to navigation item 3 click
                    true
                }
                R.id.menu_profile -> {
                    // Respond to navigation item 4 click
                    true
                }
                else -> false
            }
        }

        // Retrieve the saved theme from SharedPreferences
        val savedThemeMode = sharedPreferences.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_NO)

        // Set the switch state based on the saved theme
        switchToggleTheme.isChecked = savedThemeMode == AppCompatDelegate.MODE_NIGHT_YES

        switchToggleTheme.setOnCheckedChangeListener { _, isChecked ->
            val mode = if (isChecked) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
            val editor = sharedPreferences.edit()
            editor.putInt(KEY_THEME, mode)
            editor.apply()
            AppCompatDelegate.setDefaultNightMode(mode)
            updateThemeUi(ivTheme, tvTheme)
        }

        ibLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun fetchDataAndDisplay() {
        val currentUser = auth.currentUser
        val db = FirebaseFirestore.getInstance()
        val docRef = currentUser?.let { db.collection("users").document(it.uid) }

        docRef?.get()?.addOnSuccessListener { document ->
            if (document != null) {
                val avatar = document.getString("avatar")
                val name = document.getString("full_name")
                val email = document.getString("email")
                updateUI(avatar, name, email)
            } else {
                Log.d("ProfileActivity", "No such document")
            }
        }?.addOnFailureListener { exception ->
            Log.d("ProfileActivity", "get failed with ", exception)
        }
    }

    private fun updateUI(avatar: String?, name: String?, email: String?) {
        if (avatar != null) {
            Glide.with(this)
                .load(avatar)
                .override(Target.SIZE_ORIGINAL)
                .circleCrop()
                .into(imageView)
            layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT
            layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT
        } else {
            Glide.with(this)
                .load(R.drawable.user)
                .circleCrop()
                .into(imageView)
            val density = resources.displayMetrics.density
            layoutParams.width = (24 * density).toInt()
            layoutParams.height = (24 * density).toInt()
        }
        imageView.layoutParams = layoutParams
        tvName.text = name
        tvEmail.text = email
    }

    private fun updateThemeUi(ivTheme: ImageView, tvTheme: TextView) {
        val isDarkTheme = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        if (isDarkTheme) {
            ivTheme.setImageResource(R.drawable.ic_light)
            tvTheme.text = "Enable Light Mode"
        } else {
            ivTheme.setImageResource(R.drawable.ic_night)
            tvTheme.text = "Enable Dark Mode"
        }
    }
}