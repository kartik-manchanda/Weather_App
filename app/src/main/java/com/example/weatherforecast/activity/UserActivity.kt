package com.example.weatherforecast.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.weatherforecast.R
import kotlinx.android.synthetic.main.activity_user.view.*

class UserActivity : AppCompatActivity() {

    lateinit var etUser: EditText
    lateinit var btnContinue: Button
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        etUser = findViewById(R.id.etUser)
        btnContinue = findViewById(R.id.btnContinue)
        sharedPreferences =
            getSharedPreferences(getString(R.string.preference_location), Context.MODE_PRIVATE)

        if (sharedPreferences.getBoolean("isLogged", false)) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnContinue.setOnClickListener {
            if (etUser.text.toString().isNullOrEmpty()) {
                etUser.error = "Field Required"
            } else {
                sharedPreferences.edit().putBoolean("isLogged", true).apply()
                sharedPreferences.edit().putString("UserName", etUser.text.toString()).apply()
                Toast.makeText(
                    this,
                    "Welcome ${sharedPreferences.getString("UserName", "User")}",
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

    }
}
