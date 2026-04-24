package com.apptest.ml1

import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.os.Bundle
import android.content.Intent

class HomeActivity : AppCompatActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val btnLogout = findViewById<Button>(R.id.btnLogout)

        btnLogout.setOnClickListener {
            // Return to the camera/login screen
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close HomeActivity
        }
    }

}