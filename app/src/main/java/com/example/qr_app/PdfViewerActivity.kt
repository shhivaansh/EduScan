package com.example.qr_app

import android.os.Bundle
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.barteksc.pdfviewer.PDFView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class PdfViewerActivity : AppCompatActivity() {

    private lateinit var pdfView: PDFView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Prevent screenshots/screen recording
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        setContentView(R.layout.activity_pdf_viewer)

        pdfView = findViewById(R.id.pdfView)
        progressBar = findViewById(R.id.progressBar)

        val pdfUrl = intent.getStringExtra("PDF_URL")
        if (pdfUrl.isNullOrBlank() || !pdfUrl.contains(".pdf", ignoreCase = true)) {
            Toast.makeText(this, "Invalid PDF URL", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        progressBar.visibility = android.view.View.VISIBLE

        // Use lifecycle-aware coroutine scope
        lifecycleScope.launch(Dispatchers.IO) {
            val file = downloadPdfFromUrl(pdfUrl)
            withContext(Dispatchers.Main) {
                progressBar.visibility = android.view.View.GONE
                if (file != null) {
                    pdfView.fromFile(file)
                        .enableSwipe(true)
                        .swipeHorizontal(false)
                        .enableDoubletap(true)
                        .load()
                } else {
                    Toast.makeText(this@PdfViewerActivity, "Failed to load PDF", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun downloadPdfFromUrl(urlString: String): File? {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return null
            }

            val file = File(cacheDir, "temp.pdf")
            connection.inputStream.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            connection?.disconnect()
        }
    }
}
