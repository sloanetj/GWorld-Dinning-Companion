package com.example.maptest

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.core.text.isDigitsOnly
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var box1: CheckBox
    private lateinit var box2: CheckBox
    private lateinit var box3: CheckBox
    private lateinit var box4: CheckBox
    private lateinit var box5: CheckBox
    private lateinit var box6: CheckBox

    private lateinit var areaText: EditText
    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences: SharedPreferences = getSharedPreferences("SAVED_INFO", Context.MODE_PRIVATE)

        setTitle(R.string.title_activity_main)
        setContentView(R.layout.activity_main)

        areaText = findViewById(R.id.searchArea)
        button = findViewById((R.id.findRestaurantsButton))
        box1 = findViewById((R.id.checkBox))
        box2 = findViewById((R.id.checkBox2))
        box3 = findViewById((R.id.checkBox3))
        box4 = findViewById((R.id.checkBox4))
        box5 = findViewById((R.id.checkBox5))
        box6 = findViewById((R.id.checkBox6))


        areaText.setText(preferences.getString("SEARCH_AREA", ""))

        if (areaText.text.isEmpty()){
            areaText.setText("1500")
        }
        box1.isChecked = preferences.getBoolean("BOX1", false)
        box2.isChecked = preferences.getBoolean("BOX2", false)
        box3.isChecked = preferences.getBoolean("BOX3", false)
        box4.isChecked = preferences.getBoolean("BOX4", false)
        box5.isChecked = preferences.getBoolean("BOX5", false)
        box6.isChecked = preferences.getBoolean("BOX6", false)

        button.isEnabled = false
        // if the shared preferences already filled the area field, and if its validated, enable button
        if (areaText.text.isNotEmpty()){
            if (isValid(areaText.text.toString()))
                button.isEnabled = true
        }

        // text listener for area field. If there is a valid input, enable button
        areaText.addTextChangedListener(textWatcher)

        button.setOnClickListener {
            // input data
            val area= areaText.text.toString().trim()
            val b1 = checkBox.isChecked       // if checked, true
            val b2 = checkBox2.isChecked
            val b3 = checkBox3.isChecked
            val b4 = checkBox4.isChecked
            val b5 = checkBox5.isChecked
            val b6 = checkBox6.isChecked

            // put data into shared preferences
            val editor = preferences.edit()
            editor.putString("SEARCH_AREA", area)
            editor.putBoolean("BOX1", b1)
            editor.putBoolean("BOX2", b2)
            editor.putBoolean("BOX3", b3)
            editor.putBoolean("BOX4", b4)
            editor.putBoolean("BOX5", b5)
            editor.putBoolean("BOX6", b6)
            // apply changes
            editor.apply()


            val intent = Intent(this, MapsActivity::class.java)

            // give checkbox data to next activity
            intent.putExtra("BURGERS",       b1)
            intent.putExtra("PIZZA",         b2)
            intent.putExtra("CHINESE",       b3)
            intent.putExtra("SUSHI",         b4)
            intent.putExtra("INDIAN",        b5)
            intent.putExtra("SALAD",         b6)

            // give search area to next activity
            intent.putExtra("SEARCH_AREA", areaText.text.toString())
            startActivity(intent)
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {}
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(newString: CharSequence, start: Int, before: Int, count: Int) {
           val inputtedArea = areaText.text.toString().trim()
            button.isEnabled = isValid(inputtedArea)

        }
    }

    fun isValid(area: String):Boolean{
        if (area.isEmpty()){
            return false
        }
        if(!area.isDigitsOnly()){
            return false
        }
        val areaNum = area.toInt()
        if (areaNum <= 0){
            return false
        }

        return true

    }

}