package com.example.trevello

import android.app.ActivityOptions
import android.content.ContentValues
import android.content.Context
import android.content.Intent
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

class LoginActivity : AppCompatActivity() {
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
    private lateinit var llBack: LinearLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var storedVerificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorSecondary)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth
        var isPhoneNumberValid = false

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
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
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
            if (isPhoneNumberValid) {
                val userPhoneNumber = etPhoneNumber.text.toString().trimStart('0')
                val phoneNumber = "+94$userPhoneNumber"
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(callbacks)
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
                enableOTPInputs()

                // Start the timer when the OTP is sent
                handler = Handler(Looper.getMainLooper())
                runnable = Runnable {
                    resetInputsAndButton()
                    showSnackbar("OTP verification timed out. Please try again.")
                }
                handler.postDelayed(runnable, 60000)
                showSnackbar("OTP sent to number $phoneNumber")
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
                    if (s.length == 1) {
                        if (index < otpFields.size - 1) {
                            otpFields[index + 1].requestFocus()
                        } else {
                            val imm =
                                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(editText.windowToken, 0)

                            if (index == otpFields.size - 1) {
                                onOtpCompleted()
                            }
                        }
                    }
                }
            })

            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN && editText.text.isEmpty()) {
                    if (index > 0) {
                        otpFields[index - 1].requestFocus()
                    }
                }
                false
            }
        }

        llBack = findViewById(R.id.llBack)
        llBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(this, R.anim.slide_in_left, R.anim.slide_out_right).toBundle()
            startActivity(intent, options)
            Handler(Looper.getMainLooper()).postDelayed({
                finish()
            }, 500) // Delay finish() to allow the animation to complete. Adjust the delay time as needed.
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    val db = FirebaseFirestore.getInstance()
                    val docRef = db.collection("users").document(user!!.uid)
                    docRef.get()
                        .addOnSuccessListener { document ->
                            if (document != null && document.exists()) {
                                handler.removeCallbacks(runnable) // remove the scheduled task if OTP verification is successful
                                Log.d(ContentValues.TAG, "signInWithCredential:success")
                                showSnackbar("OTP verification successful. Redirecting...")
                                val intent = Intent(this, HomeActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                val options = ActivityOptions.makeCustomAnimation(this, R.anim.slide_in_right, R.anim.slide_out_left).toBundle()
                                startActivity(intent, options)
                            } else {
                                auth.currentUser?.delete()
                                showSnackbar("User does not exist. Please register first.")
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.d(ContentValues.TAG, "get failed with ", exception)
                            showSnackbar("Failed to check user existence. Please try again.")
                        }
                } else {
                    Log.w(ContentValues.TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        showSnackbar("Invalid OTP. Please try again.")
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

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
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
        val otp = etOTP1.text.toString() + etOTP2.text.toString() + etOTP3.text.toString() + etOTP4.text.toString() + etOTP5.text.toString() + etOTP6.text.toString()
        val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, otp)
        signInWithPhoneAuthCredential(credential)
    }
}