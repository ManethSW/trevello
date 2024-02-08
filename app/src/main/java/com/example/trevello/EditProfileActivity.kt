package com.example.trevello

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class EditProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var imageView: ImageView
    private lateinit var layoutParams: LinearLayout.LayoutParams
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var ibBack: ImageButton
    private lateinit var bSave: AppCompatButton
    private var isFullNameValid = false
    private var isEmailValid = false
    private var newAvatarUri: Uri? = null
    private val IMAGE_PICK_CODE = 1000
    private val CAMERA_REQUEST_CODE = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance()
        imageView = findViewById(R.id.ivProfile)
        layoutParams = imageView.layoutParams as LinearLayout.LayoutParams
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        ibBack = findViewById(R.id.ibBack)
        bSave = findViewById(R.id.bSave)
        val avatar = intent.getStringExtra("avatar")
        val name = intent.getStringExtra("full_name")
        val email = intent.getStringExtra("email")
        val phoneNumber = intent.getStringExtra("phone_no")
        Log.d(
            "EditProfileActivity",
            "Avatar: $avatar, Name: $name, Email: $email, Phone Number: $phoneNumber"
        )

        updateUI(avatar, name, email)

        window.navigationBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorSecondary)

        etFullName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                isFullNameValid = if (s.toString() == name) {
                    etFullName.setBackgroundResource(R.drawable.input_box)
                    false
                } else if (isValid(s.toString())) {
                    etFullName.setBackgroundResource(R.drawable.input_box_valid)
                    true
                } else {
                    etFullName.setBackgroundResource(R.drawable.input_box_invalid)
                    false
                }
            }
        })

        etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                isEmailValid = if (s.toString() == email) {
                    etEmail.setBackgroundResource(R.drawable.input_box)
                    false
                } else if (android.util.Patterns.EMAIL_ADDRESS.matcher(s.toString()).matches()) {
                    etEmail.setBackgroundResource(R.drawable.input_box_valid)
                    true
                } else {
                    etEmail.setBackgroundResource(R.drawable.input_box_invalid)
                    false
                }
            }
        })

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

        ibBack.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        imageView.setOnClickListener {
//            openImageChooser()
            openCamera()
        }

        bSave.setOnClickListener {
            val user = hashMapOf<String, String>()
            if (isFullNameValid) {
                user["full_name"] = etFullName.text.toString()
            }
            if (isEmailValid) {
                user["email"] = etEmail.text.toString()
            }

            newAvatarUri?.let {
                uploadImageToFirebase(it)
            }

            val db = FirebaseFirestore.getInstance()
            if (user.isNotEmpty()) {
                db.collection("users")
                    .document(auth.currentUser?.uid.toString())
                    .set(user, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d("EditProfileActivity", "DocumentSnapshot successfully written!")
                    }
                    .addOnFailureListener { e ->
                        Log.w("EditProfileActivity", "Error writing document", e)
                    }
            }

            navigateToProfile(newAvatarUri)
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
        etFullName.setText(name)
        etEmail.setText(email)
    }

    private fun isValid(text: String): Boolean {
        val words = text.trim().split(" ")
        return words.size >= 2 && words.all { it.matches(Regex("[a-zA-Z]+")) }
    }

    private fun navigateToProfile(newAvatarUri: Uri? = null) {
        val intent = Intent(this, ProfileActivity::class.java)
        newAvatarUri?.let {
            intent.putExtra("newAvatarUri", it.toString())
        }
        startActivity(intent)
        finish()
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            // Handle image pick result
            newAvatarUri = data.data
            newAvatarUri?.let {
                Glide.with(this)
                    .load(it)
                    .circleCrop()
                    .into(imageView)
            }
        } else if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // Handle camera result
            val bitmap = data.extras?.get("data") as Bitmap
            // Convert bitmap to Uri
            newAvatarUri = bitmapToUri(bitmap)
            newAvatarUri?.let {
                Glide.with(this)
                    .load(it)
                    .circleCrop()
                    .into(imageView)
            }
        }
    }

    private fun bitmapToUri(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Title", null)
        return Uri.parse(path)
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().getReference("avatars/${auth.currentUser?.uid}")
        storageRef.delete().addOnCompleteListener {
            storageRef.putFile(imageUri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users")
                            .document(auth.currentUser?.uid.toString())
                            .update("avatar", uri.toString())
                            .addOnSuccessListener {
                                Log.d("EditProfileActivity", "Avatar successfully updated!")
                            }
                            .addOnFailureListener { e ->
                                Log.w("EditProfileActivity", "Error updating avatar", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("EditProfileActivity", "Upload failed", e)
                }
        }
    }
}