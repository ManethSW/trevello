package com.example.trevello

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.location.Geocoder
import android.location.Location
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.Locale
import java.util.UUID

class AddEntryActivity : AppCompatActivity() {
    private val CAMERA_REQUEST_CODE = 2000
    private lateinit var llCurrentLocation: LinearLayout
    private lateinit var llSetLocation: LinearLayout
    private lateinit var llTitleInput: LinearLayout
    private lateinit var etTitle: TextView
    private lateinit var llDescriptionInput: LinearLayout
    private lateinit var etDescription: TextView
    private lateinit var llUploadImages: LinearLayout
    private lateinit var imageCarousel: ViewPager2
    private lateinit var llAdd: LinearLayout
    private val images: MutableList<Bitmap> = mutableListOf()
    private val imageCarouselAdapter = ImageCarouselAdapter(images)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var location: Location? = null
    private var address: String? = null
    private var title: String? = null
    private var description: String? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val storage = FirebaseStorage.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorSecondary)
        setContentView(R.layout.activity_add_entry)

        llCurrentLocation = findViewById(R.id.llCurrentLocation)
        llSetLocation = findViewById(R.id.llSetLocation)
        val viewCurrentLocation = llCurrentLocation.getChildAt(2) as LinearLayout
        val viewSetLocation = llSetLocation.getChildAt(2) as LinearLayout
        llTitleInput = findViewById(R.id.llTitleInput)
        etTitle = findViewById(R.id.etTitle)
        llDescriptionInput = findViewById(R.id.llDescriptionInput)
        etDescription = findViewById(R.id.etDescription)
        llUploadImages = findViewById(R.id.llUploadImages)
        llAdd = findViewById(R.id.llAdd)
        imageCarousel = findViewById(R.id.imageCarousel)
        imageCarousel.adapter = imageCarouselAdapter
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        auth = Firebase.auth
        db = Firebase.firestore

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.menu_add
        }
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.menu_add -> {
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

        llCurrentLocation.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION
                )
            } else {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        location?.let {
                            this@AddEntryActivity.location = location
                            val geocoder = Geocoder(this, Locale.getDefault())
                            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            val cityName = addresses?.get(0)?.locality
                            val streetName = addresses?.get(0)?.thoroughfare
                            val fullAddress = addresses?.get(0)?.getAddressLine(0) // Get the full address

                            address = ""
                            if (streetName != null && cityName != null) {
                                address = "$streetName, $cityName"
                            } else if (streetName != null) {
                                address = streetName
                            } else if (cityName != null) {
                                address = cityName
                            } else if (fullAddress != null) {
                                address = fullAddress
                            }

                            val textView = llCurrentLocation.getChildAt(1) as TextView
                            val truncatedAddress = if (address!!.length > 30) {
                                address!!.substring(0, 30) + "..."
                            } else {
                                address
                            }
                            textView.text = truncatedAddress
                            viewCurrentLocation.getChildAt(0).background =
                                ContextCompat.getDrawable(this, R.drawable.green_circle)
                            llCurrentLocation.background =  ContextCompat.getDrawable(this, R.drawable.input_box_valid)
                            llSetLocation.background =  ContextCompat.getDrawable(this, R.drawable.input_box)
                            viewSetLocation.getChildAt(0).background = null
                            val textViewSetLocation = llSetLocation.getChildAt(1) as TextView
                            textViewSetLocation.text = "Set Location"
                        }
                    }
            }
        }

        llSetLocation.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivityForResult(intent, REQUEST_LOCATION)
        }

        // Store the value of the title when data is changed
        etTitle.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                title = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Store the value of the description when data is changed
        etDescription.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                description = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        llUploadImages.setOnClickListener {
            // Inflate the custom layout
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_layout, null)

            // Create an AlertDialog.Builder and set the view
            val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            builder.setView(dialogView)

            // Create the AlertDialog
            val alertDialog = builder.create()

            // Get the custom AlertDialog buttons and set their onClickListeners
            val btnTakePhoto = dialogView.findViewById<Button>(R.id.btnTakePhoto)
            val btnChooseFromGallery = dialogView.findViewById<Button>(R.id.btnChooseFromGallery)

            btnTakePhoto.setOnClickListener {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
                alertDialog.dismiss() // Close the dialog
            }

            btnChooseFromGallery.setOnClickListener {
                // Handle "Select Files" option
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "image/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
                startActivityForResult(intent, REQUEST_IMAGE_PICK)
                alertDialog.dismiss() // Close the dialog
            }

            // Show the AlertDialog
            alertDialog.show()
        }

        llAdd.setOnClickListener {
            if (location == null || address.isNullOrEmpty() || title.isNullOrEmpty() || description.isNullOrEmpty() || images.isEmpty()) {
                showSnackbar("Please fill in all the fields and upload at least one image.")
            } else {
                showSnackbar("Uploading your new entry...")

                val imageUrls = mutableListOf<String>()
                images.forEachIndexed { index, bitmap ->
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val data = baos.toByteArray()

                    val storageRef = storage.reference
                    val imagesRef = storageRef.child("images/${UUID.randomUUID()}.jpg")

                    val uploadTask = imagesRef.putBytes(data)
                    uploadTask.addOnFailureListener {
                        showSnackbar("Failed to upload image.")
                    }.addOnSuccessListener {
                        imagesRef.downloadUrl.addOnSuccessListener { uri ->
                            imageUrls.add(uri.toString())
                            if (imageUrls.size == images.size) {
                                val entry = hashMapOf(
                                    "location" to GeoPoint(location!!.latitude, location!!.longitude),
                                    "address" to address,
                                    "title" to title,
                                    "description" to description,
                                    "images" to imageUrls
                                )
                                val uid = auth.currentUser?.uid
                                db.collection("users").document(uid!!).collection("entries")
                                    .add(entry)
                                    .addOnSuccessListener {
                                        showSnackbar("Entry added successfully.")
                                        val intent = Intent(this, HomeActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                    .addOnFailureListener {
                                        showSnackbar("Error adding entry.")
                                    }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_LOCATION) {
            if (resultCode == Activity.RESULT_OK) {
                location = data?.getParcelableExtra<Location>("location")
                val geocoder = Geocoder(this, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location!!.latitude, location!!.longitude, 1)
                val cityName = addresses?.get(0)?.locality
                val streetName = addresses?.get(0)?.thoroughfare
                val fullAddress = addresses?.get(0)?.getAddressLine(0)
                address = ""
                if (streetName != null && cityName != null) {
                    address = "$streetName, $cityName"
                } else if (streetName != null) {
                    address = streetName
                } else if (cityName != null) {
                    address = cityName
                } else if (fullAddress != null) {
                    address = fullAddress
                }
                val textView = llSetLocation.getChildAt(1) as TextView
                val truncatedAddress = if (address!!.length > 50) {
                    address!!.substring(0, 50) + "..."
                } else {
                    address
                }
                textView.text = truncatedAddress
                val viewSetLocation = llSetLocation.getChildAt(2) as LinearLayout
                viewSetLocation.getChildAt(0).background =
                    ContextCompat.getDrawable(this, R.drawable.green_circle)
                llSetLocation.background =  ContextCompat.getDrawable(this, R.drawable.input_box_valid)
                llCurrentLocation.background =  ContextCompat.getDrawable(this, R.drawable.input_box)
                val viewCurrentLocation = llCurrentLocation.getChildAt(2) as LinearLayout
                viewCurrentLocation.getChildAt(0).background = null
                val textViewCurrentLocation = llCurrentLocation.getChildAt(1) as TextView
                textViewCurrentLocation.text = "Current Location"
            }
        }

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            val clipData = data.clipData
            val newImages: MutableList<Bitmap> = mutableListOf()
            val tvNoImages: TextView = findViewById(R.id.tvNoImages)
            if (clipData != null) {
                // Calculate the total number of images (already uploaded + newly selected)
                val totalImages = images.size + clipData.itemCount
                // Limit the user to upload up to 3 images only
                val count = if (totalImages > 10) 10 - images.size else clipData.itemCount
                for (i in 0 until count) {
                    val uri = clipData.getItemAt(i).uri
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                    newImages.add(bitmap)
                }
                if (totalImages > 10) {
                    // Show a message to the user
                    showSnackbar("You can only upload up to 10 images.")
                } else {
                    images.addAll(newImages)
                    imageCarouselAdapter.notifyDataSetChanged()
                }
            } else {
                data.data?.let { uri ->
                    if (images.size < 10) {
                        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                        images.add(bitmap)
                        imageCarouselAdapter.notifyDataSetChanged()
                    } else {
                        // Show a message to the user
                        showSnackbar("You can only upload up to 10 images.")
                    }
                }
            }
            if (images.isEmpty()) {
                tvNoImages.visibility = View.VISIBLE
            } else {
                tvNoImages.visibility = View.GONE
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

    companion object {
        const val MY_PERMISSIONS_REQUEST_LOCATION = 99
        const val REQUEST_LOCATION = 1
        const val REQUEST_IMAGE_PICK = 2
    }
}