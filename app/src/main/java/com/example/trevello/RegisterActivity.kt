package com.example.trevello

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.util.concurrent.TimeUnit

class RegisterActivity : AppCompatActivity() {
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var llPhoneNumber: LinearLayout
    private lateinit var etOTP1: EditText
    private lateinit var etOTP2: EditText
    private lateinit var etOTP3: EditText
    private lateinit var etOTP4: EditText
    private lateinit var etOTP5: EditText
    private lateinit var etOTP6: EditText
    private lateinit var ivLock1: ImageView
    private lateinit var ivLock2: ImageView
    private lateinit var ivLock3: ImageView
    private lateinit var ivLock4: ImageView
    private lateinit var ivLock5: ImageView
    private lateinit var ivLock6: ImageView
    private lateinit var sendOtpButton: Button
    private lateinit var bBack: ImageButton
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var storedVerificationId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorSecondary)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth
        db = Firebase.firestore

        var isFullNameValid = false
        var isEmailValid = false
        var isPhoneNumberValid = false

        etFullName = findViewById(R.id.etFullName)
        etFullName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // This method is called to notify you that, within s, the count characters
                // beginning at start are about to be replaced by new text with length after.
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // This method is called to notify you that, within s, the count characters
                // beginning at start have just replaced old text that had length before.
            }

            override fun afterTextChanged(s: Editable) {
                if (isValid(s.toString())) {
                    etFullName.setBackgroundResource(com.example.trevello.R.drawable.input_box_valid)
                    isFullNameValid = true
                } else {
                    etFullName.setBackgroundResource(com.example.trevello.R.drawable.input_box_invalid)
                    isFullNameValid = false
                }
            }
        })

        etEmail = findViewById(R.id.etEmail)
        etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // This method is called to notify you that, within s, the count characters
                // beginning at start are about to be replaced by new text with length after.
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // This method is called to notify you that, within s, the count characters
                // beginning at start have just replaced old text that had length before.
            }

            override fun afterTextChanged(s: Editable) {
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(s.toString()).matches()) {
                    etEmail.setBackgroundResource(com.example.trevello.R.drawable.input_box_valid)
                    isEmailValid = true
                } else {
                    etEmail.setBackgroundResource(com.example.trevello.R.drawable.input_box_invalid)
                    isEmailValid = false
                }
            }
        })

        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        llPhoneNumber = findViewById(R.id.llPhoneNumber)
        etPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // This method is called to notify you that, within s, the count characters
                // beginning at start are about to be replaced by new text with length after.
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // This method is called to notify you that, within s, the count characters
                // beginning at start have just replaced old text that had length before.
            }

            override fun afterTextChanged(s: Editable) {
                if (s.length == 10) {
                    llPhoneNumber.setBackgroundResource(com.example.trevello.R.drawable.input_box_valid)
                    isPhoneNumberValid = true
                } else {
                    llPhoneNumber.setBackgroundResource(com.example.trevello.R.drawable.input_box_invalid)
                    isPhoneNumberValid = false
                }
            }
        })

        etOTP1 = findViewById(R.id.etOTP1)
        etOTP2 = findViewById(R.id.etOTP2)
        etOTP3 = findViewById(R.id.etOTP3)
        etOTP4 = findViewById(R.id.etOTP4)
        etOTP5 = findViewById(R.id.etOTP5)
        etOTP6 = findViewById(R.id.etOTP6)
        ivLock1 = findViewById(R.id.ivLock1)
        ivLock2 = findViewById(R.id.ivLock2)
        ivLock3 = findViewById(R.id.ivLock3)
        ivLock4 = findViewById(R.id.ivLock4)
        ivLock5 = findViewById(R.id.ivLock5)
        ivLock6 = findViewById(R.id.ivLock6)
        sendOtpButton = findViewById(R.id.bSendOTP)


        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                showSnackbar("Verification failed: ${e.message}")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(verificationId, token)
                storedVerificationId = verificationId
            }
        }

        sendOtpButton.setOnClickListener {
            if (isFullNameValid && isEmailValid && isPhoneNumberValid) {
                val userPhoneNumber = etPhoneNumber.text.toString().trimStart('0')
                val phoneNumber = "+94$userPhoneNumber"
                db.collection("registered_phone_numbers")
                    .document(phoneNumber)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            showSnackbar("Phone number already in use.")
                        } else {
                            val options = PhoneAuthOptions.newBuilder(auth)
                                .setPhoneNumber(phoneNumber)       // Phone number to verify
                                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                .setActivity(this)                 // Activity (for callback binding)
                                .setCallbacks(callbacks)           // OnVerificationStateChangedCallbacks
                                .build()
                            PhoneAuthProvider.verifyPhoneNumber(options)
                            enableOTPInputs()
                            handler = Handler(Looper.getMainLooper())
                            runnable = Runnable {
                                resetInputsAndButton()
                                showSnackbar("OTP verification timed out. Please try again.")
                            }
                            handler.postDelayed(runnable, 60000)
                            showSnackbar("OTP sent to number $phoneNumber")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Error checking phone number in collection", exception)
                    }
            } else {
                showSnackbar("Please enter valid details")
            }
        }

        val otpFields = listOf(etOTP1, etOTP2, etOTP3, etOTP4, etOTP5, etOTP6)
        otpFields.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable) {
                    // If the length of input is 1, move to the next field
                    if (s.length == 1) {
                        if (index < otpFields.size - 1) {
                            otpFields[index + 1].requestFocus()
                        } else {
                            // Close the keyboard when the last field is filled
                            val imm =
                                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(editText.windowToken, 0)

                            // If the last OTP field is filled, call the function
                            if (index == otpFields.size - 1) {
                                onOtpCompleted()
                            }
                        }
                    }
                }
            })

            editText.setOnKeyListener { _, keyCode, event ->
                // If the field is empty and the backspace key is pressed, move to the previous field
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN && editText.text.isEmpty()) {
                    if (index > 0) {
                        otpFields[index - 1].requestFocus()
                    }
                }
                false
            }
        }

        bBack = findViewById(R.id.bBack)
        bBack.setOnClickListener {
            super.onBackPressed()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    val currentUser = auth.currentUser
                    // Store user data in Firestore
                    val userPhoneNumber = etPhoneNumber.text.toString().trimStart('0')
                    val phoneNumber = "+94$userPhoneNumber"
                    if (currentUser != null) {
                        val user = hashMapOf(
                            "full_name" to etFullName.text.toString(),
                            "email" to etEmail.text.toString(),
                            "phone_number" to phoneNumber
                        )
                        db.collection("users")
                            .document(currentUser.uid)
                            .set(user)
                            .addOnSuccessListener {
                                Log.d(TAG, "DocumentSnapshot added with ID: ${currentUser.uid}")
                                db.collection("registered_phone_numbers")
                                    .document(phoneNumber)
                                    .set(hashMapOf("phone_number" to phoneNumber))
                                    .addOnSuccessListener {
                                        Log.d(
                                            TAG,
                                            "Phone number added to registered_phone_numbers collection."
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w(TAG, "Error adding phone number to collection", e)
                                    }
                                showSnackbar("OTP verification successful. Redirecting...")
                                val intent = android.content.Intent(this, LoginActivity::class.java)
                                val options = android.app.ActivityOptions.makeCustomAnimation(
                                    this,
                                    R.anim.slide_in_right,
                                    R.anim.slide_out_left
                                ).toBundle()
                                startActivity(intent, options)
                                showSnackbar("Please Login to continue")
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error adding document", e)
                            }
                    } else {
                        showSnackbar("Phone number update failed. Please try again.")
                        etOTP1.setText("")
                        etOTP2.setText("")
                        etOTP3.setText("")
                        etOTP4.setText("")
                        etOTP5.setText("")
                        etOTP6.setText("")
                        etOTP1.requestFocus()
                    }
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        showSnackbar("OTP verification failed. Please try again.")
                        etOTP1.setText("")
                        etOTP2.setText("")
                        etOTP3.setText("")
                        etOTP4.setText("")
                        etOTP5.setText("")
                        etOTP6.setText("")
                        etOTP1.requestFocus()
                    } else {
                        showSnackbar("An error occurred. Please try again.")
                        etOTP1.setText("")
                        etOTP2.setText("")
                        etOTP3.setText("")
                        etOTP4.setText("")
                        etOTP5.setText("")
                        etOTP6.setText("")
                        etOTP1.requestFocus()
                    }
                }
            }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun isValid(text: String): Boolean {
        val words = text.trim().split(" ")
        return words.size >= 2 && words.all { it.matches(Regex("[a-zA-Z]+")) }
    }

    private fun resetInputsAndButton() {
        etOTP1.setText("")
        etOTP2.setText("")
        etOTP3.setText("")
        etOTP4.setText("")
        etOTP5.setText("")
        etOTP6.setText("")
        etOTP1.hint = ""
        etOTP2.hint = ""
        etOTP3.hint = ""
        etOTP4.hint = ""
        etOTP5.hint = ""
        etOTP6.hint = ""
        ivLock1.visibility = ImageButton.VISIBLE
        ivLock2.visibility = ImageButton.VISIBLE
        ivLock3.visibility = ImageButton.VISIBLE
        ivLock4.visibility = ImageButton.VISIBLE
        ivLock5.visibility = ImageButton.VISIBLE
        ivLock6.visibility = ImageButton.VISIBLE
        etOTP1.isEnabled = false
        etOTP2.isEnabled = false
        etOTP3.isEnabled = false
        etOTP4.isEnabled = false
        etOTP5.isEnabled = false
        etOTP6.isEnabled = false
        sendOtpButton.isEnabled = true
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

    private fun enableOTPInputs() {
        etOTP1.isEnabled = true
        etOTP2.isEnabled = true
        etOTP3.isEnabled = true
        etOTP4.isEnabled = true
        etOTP5.isEnabled = true
        etOTP6.isEnabled = true
        sendOtpButton.isEnabled = false
        ivLock1.visibility = ImageButton.GONE
        ivLock2.visibility = ImageButton.GONE
        ivLock3.visibility = ImageButton.GONE
        ivLock4.visibility = ImageButton.GONE
        ivLock5.visibility = ImageButton.GONE
        ivLock6.visibility = ImageButton.GONE
        etOTP1.hint = "•"
        etOTP2.hint = "•"
        etOTP3.hint = "•"
        etOTP4.hint = "•"
        etOTP5.hint = "•"
        etOTP6.hint = "•"
    }

    private fun onOtpCompleted() {
        val otp =
            etOTP1.text.toString() + etOTP2.text.toString() + etOTP3.text.toString() + etOTP4.text.toString() + etOTP5.text.toString() + etOTP6.text.toString()
        Log.d(TAG, "onOtpCompleted: $otp")
        if (storedVerificationId != null) {
            val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, otp)
            signInWithPhoneAuthCredential(credential)
        } else {
            showSnackbar("An error occurred. Please try again.")
        }
    }
}