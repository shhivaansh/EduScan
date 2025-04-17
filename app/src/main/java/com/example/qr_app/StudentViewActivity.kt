package com.example.qr_app

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StudentViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        setContentView(R.layout.activity_student_view)

        val textView = findViewById<TextView>(R.id.studentText)
        textView.text = "Welcome, Student!\nHere you can scan/view teacher's PDFs via QR."

        val scanQrButton = findViewById<Button>(R.id.scanQrButton)
        scanQrButton.setOnClickListener {
            startActivity(Intent(this, QrScannerActivity::class.java))
        }
    }
}
