package com.avantgarderv

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.avantgarderv.databinding.ActivityScannerBinding
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.regex.Pattern

class ScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScannerBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService
    private var isProcessing = false

    // Regex to validate a 17-character VIN. Excludes I, O, Q.
    private val vinPattern: Pattern = Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera()

        binding.manualEntryButton.setOnClickListener {
            showVinInputDialog()
        }
    }

    private fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
            }

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor) { imageProxy ->
                    if (!isProcessing) {
                        isProcessing = true
                        processImage(imageProxy)
                    }
                }
            }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalysis
            )
        } catch (exc: Exception) {
            Log.e("ScannerActivity", "Use case binding failed", exc)
            showToast("Failed to start camera.")
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanBarcodes(image, imageProxy)
        } else {
            // If mediaImage is null, close proxy and reset flag
            imageProxy.close()
            isProcessing = false
        }
    }

    private fun scanBarcodes(image: InputImage, imageProxy: ImageProxy) {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_CODE_39, Barcode.FORMAT_CODE_128, Barcode.FORMAT_DATA_MATRIX)
            .build()
        val scanner = BarcodeScanning.getClient(options)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue?.uppercase()
                    if (rawValue != null && vinPattern.matcher(rawValue).matches()) {
                        // Found a valid VIN, stop processing and return
                        onVinFound(rawValue)
                        return@addOnSuccessListener
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ScannerActivity", "Barcode scanning failed.", e)
            }
            .addOnCompleteListener {
                // This is crucial: always close the imageProxy to continue receiving frames
                imageProxy.close()
                isProcessing = false
            }
    }

    private fun onVinFound(vin: String) {
        // Ensure we don't finish activity multiple times
        if (!isFinishing) {
            Log.d("ScannerActivity", "VIN Found: $vin")
            val resultIntent = Intent()
            resultIntent.putExtra("SCAN_RESULT", vin)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun showVinInputDialog() {
        // Prevent dialog from showing if activity is closing
        if (isFinishing) return

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter VIN Manually")

        val input = EditText(this)
        input.setSingleLine()
        builder.setView(input)

        builder.setPositiveButton("Submit") { dialog, _ ->
            val vin = input.text.toString().trim().uppercase()
            if (vin.isNotEmpty()) {
                if (vinPattern.matcher(vin).matches()) {
                    onVinFound(vin)
                } else {
                    showToast("Invalid VIN format. Must be 17 characters.")
                }
            } else {
                showToast("VIN cannot be empty.")
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}