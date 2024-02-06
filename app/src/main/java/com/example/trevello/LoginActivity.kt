package com.example.trevello

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.navigationBarColor = Color.parseColor("#111418")
        window.statusBarColor = Color.parseColor("#111418")
        setContentView(com.example.trevello.R.layout.activity_login)

        val etFirstName = findViewById<EditText>(com.example.trevello.R.id.etFirstName)
        etFirstName.addTextChangedListener(object : TextWatcher {
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
                    etFirstName.setBackgroundResource(com.example.trevello.R.drawable.input_box_valid)
                } else {
                    etFirstName.setBackgroundResource(com.example.trevello.R.drawable.input_box_invalid)
                }
            }
        })
    }

    private fun isValid(text: String): Boolean {
        return if (text.isEmpty()) {
            false
        } else {
            text.matches(Regex("[a-zA-Z]+"))
        }
    }
}