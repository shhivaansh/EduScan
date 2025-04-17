package com.example.qr_app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Prevent screenshots
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val signupButton = findViewById<Button>(R.id.signupButton)
        val roleRadioGroup = findViewById<RadioGroup>(R.id.roleRadioGroup)
        val progressBar = findViewById<ProgressBar>(R.id.signupProgressBar)

        signupButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            val selectedRoleId = roleRadioGroup.checkedRadioButtonId
            if (selectedRoleId == -1) {
                Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRole = findViewById<RadioButton>(selectedRoleId).text.toString().lowercase()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Show progress
                progressBar.visibility = View.VISIBLE
                signupButton.isEnabled = false

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val uid = auth.currentUser?.uid
                            if (uid != null) {
                                val userMap = hashMapOf(
                                    "email" to email,
                                    "role" to selectedRole
                                )

                                firestore.collection("users")
                                    .document(uid)
                                    .set(userMap)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this, LoginActivity::class.java))
                                        finish()
                                    }
                                    .addOnFailureListener {
                                        progressBar.visibility = View.GONE
                                        signupButton.isEnabled = true
                                        Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {
                            progressBar.visibility = View.GONE
                            signupButton.isEnabled = true
                            Toast.makeText(this, "Signup Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
