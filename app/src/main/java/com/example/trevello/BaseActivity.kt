package com.example.trevello

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

open class BaseActivity : AppCompatActivity() {

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_base)
//
//        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
//        bottomNav.setOnNavigationItemSelectedListener(navListener)
//    }
//
//    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
//        var selectedFragment: Fragment? = null
//
//        when (item.itemId) {
//            R.id.nav_home -> selectedFragment = HomeFragment()
//            R.id.nav_profile -> selectedFragment = ProfileFragment()
//        }
//
//        if (selectedFragment != null) {
//            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, selectedFragment).commit()
//        }
//
//        true
//    }
}