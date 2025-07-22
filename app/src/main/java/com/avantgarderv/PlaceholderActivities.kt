package com.avantgarderv

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// This file contains placeholder activities for features that would be built out further.

class ServiceAppointmentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val textView = TextView(this).apply {
            text = "Service Appointment Screen for VIN: ${intent.getStringExtra("VIN")}"
            textSize = 20f
            setPadding(50, 50, 50, 50)
        }
        setContentView(textView)
        Toast.makeText(this, "Service Appointment Feature Placeholder", Toast.LENGTH_LONG).show()
    }
}