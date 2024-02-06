package com.example.trevello

import android.R
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.activity.ComponentActivity
import com.hbb20.CountryCodePicker

class LoginActivity : ComponentActivity() {
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

        val ccpSelectCountry = findViewById<CountryCodePicker>(com.example.trevello.R.id.ccpSelectCountry)

        // Get the AutoCompleteTextView from the layout
//        val typesFilter = findViewById<AutoCompleteTextView>(R.id.types_filter)

// Create a list of items for the dropdown. This could be a static list or dynamic data from a web service.
        val items = listOf("+108", "+98", "+94")

// Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_dropdown_item, items)

// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

// Apply the adapter to the AutoCompleteTextView
//        typesFilter.setAdapter(adapter)
    }

    private fun isValid(text: String): Boolean {
        return if (text.isEmpty()) {
            false
        } else {
            text.matches(Regex("[a-zA-Z]+"))
        }
    }
}