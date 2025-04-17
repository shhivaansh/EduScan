package com.example.qr_app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import android.graphics.Bitmap
import android.graphics.Color
import android.view.WindowManager
import android.view.View
import android.content.pm.PackageManager
import android.os.Build
import android.Manifest
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import java.util.UUID

class UploadActivity : AppCompatActivity() {

    private lateinit var selectPdfButton: Button
    private lateinit var uploadPdfButton: Button
    private lateinit var logoutButton: Button
    private lateinit var pdfNameTextView: TextView
    private lateinit var qrImageView: ImageView
    private lateinit var progressBar: ProgressBar

    private var selectedPdfUri: Uri? = null
    private var uploadedPdfId: String? = null

    private val storagePermissionCode = 101

    private val pdfPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedPdfUri = uri
                val fileName = uri.lastPathSegment?.split("/")?.last() ?: "PDF Selected"
                pdfNameTextView.text = fileName
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        setContentView(R.layout.activity_upload)

        selectPdfButton = findViewById(R.id.selectPdfButton)
        uploadPdfButton = findViewById(R.id.uploadPdfButton)
        logoutButton = findViewById(R.id.logoutButton)
        pdfNameTextView = findViewById(R.id.pdfNameTextView)
        qrImageView = findViewById(R.id.qrImageView)
        progressBar = findViewById(R.id.uploadProgressBar)

        requestStoragePermissionIfNeeded()

        selectPdfButton.setOnClickListener {
            if (hasStoragePermission()) {
                pickPdfFile()
            } else {
                requestStoragePermissionIfNeeded()
            }
        }

        uploadPdfButton.setOnClickListener {
            if (selectedPdfUri != null) {
                uploadPdfToFirebase(selectedPdfUri!!)
            } else {
                Toast.makeText(this, "Please select a PDF first.", Toast.LENGTH_SHORT).show()
            }
        }

        logoutButton.setOnClickListener {
            deleteUploadedPDFAndLogout()
        }
    }

    private fun requestStoragePermissionIfNeeded() {
        if (!hasStoragePermission()) {
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
            } else {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            ActivityCompat.requestPermissions(this, permissions, storagePermissionCode)
        }
    }

    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == storagePermissionCode) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Storage permission is required to select PDF", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun pickPdfFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        pdfPickerLauncher.launch(intent)
    }

    private fun uploadPdfToFirebase(pdfUri: Uri) {
        progressBar.visibility = View.VISIBLE
        uploadPdfButton.isEnabled = false

        val pdfId = UUID.randomUUID().toString()
        uploadedPdfId = pdfId

        val storageRef = FirebaseStorage.getInstance().reference
        val fileRef = storageRef.child("pdfs/$pdfId.pdf")

        val uploadTask = fileRef.putFile(pdfUri)

        uploadTask.addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()

                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("pdfMappings")
                    .document(pdfId)
                    .set(mapOf("url" to downloadUrl))
                    .addOnSuccessListener {
                        generateQrCode(pdfId)
                        Toast.makeText(this, "Upload successful!", Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.GONE
                        uploadPdfButton.isEnabled = true
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to store PDF mapping", Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.GONE
                        uploadPdfButton.isEnabled = true
                    }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
            uploadPdfButton.isEnabled = true
        }
    }

    private fun generateQrCode(text: String) {
        val writer = MultiFormatWriter()
        try {
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            qrImageView.setImageBitmap(bmp)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteUploadedPDFAndLogout() {
        val pdfId = uploadedPdfId ?: return
        val fileRef = FirebaseStorage.getInstance().reference.child("pdfs/$pdfId.pdf")

        fileRef.delete().addOnSuccessListener {
            FirebaseFirestore.getInstance().collection("pdfMappings")
                .document(pdfId).delete()

            Toast.makeText(this, "PDF deleted", Toast.LENGTH_SHORT).show()
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to delete PDF: ${it.message}", Toast.LENGTH_SHORT).show()
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
