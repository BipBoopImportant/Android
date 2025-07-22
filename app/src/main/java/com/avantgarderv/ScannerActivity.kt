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

    companion object {
        const val SCAN_MODE = "SCAN_MODE"
        const val SCAN_RESULT = "SCAN_RESULT"
        const val MODE_VIN = "VIN"
        const val MODE_PART = "PART"
    }

    private lateinit var binding: ActivityScannerBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService
    private var isProcessing = false
    private var scanMode: String = MODE_VIN // Default to VIN

    // Regex to validate a 17-character VIN. Excludes I, O, Q.
    private val vinPattern: Pattern = Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        scanMode = intent.getStringExtra(SCAN_MODE) ?: MODE_VIN
        setupUIForMode()

        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera()

        binding.manualEntryButton.setOnClickListener {
            showManualInputDialog()
        }
    }

    private fun setupUIForMode() {
        when (scanMode) {
            MODE_VIN -> {
                binding.scannerPrompt.text = "Point camera at VIN Barcode"
                binding.manualEntryButton.text = "Enter VIN Manually"
            }
            MODE_PART -> {
                binding.scannerPrompt.text = "Point camera at Part Barcode"
                binding.manualEntryButton.text = "Enter Part # Manually"
            }
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
            imageProxy.close()
            isProcessing = false
        }
    }

    private fun scanBarcodes(image: InputImage, imageProxy: ImageProxy) {
        // Scan for all supported formats. The app logic will validate the result.
        val options = BarcodeScannerOptions.Builder().build()
        val scanner = BarcodeScanning.getClient(options)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue
                    if (!rawValue.isNullOrEmpty() && isValid(rawValue)) {
                        onScanSuccess(rawValue.uppercase())
                        return@addOnSuccessListener // Found a valid code, stop processing
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ScannerActivity", "Barcode scanning failed.", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
                isProcessing = false
            }
    }

    private fun isValid(value: String): Boolean {
        return when (scanMode) {
            MODE_VIN -> vinPattern.matcher(value.uppercase()).matches()
            MODE_PART -> value.isNotEmpty() // Any non-empty barcode is a valid part number
            else -> false
        }
    }

    private fun onScanSuccess(result: String) {
        if (!isFinishing) {
            Log.d("ScannerActivity", "$scanMode Found: $result")
            val resultIntent = Intent()
            resultIntent.putExtra(SCAN_RESULT, result)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun showManualInputDialog() {
        if (isFinishing) return

        val title = if (scanMode == MODE_VIN) "Enter VIN Manually" else "Enter Part # Manually"
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)

        val input = EditText(this)
        input.setSingleLine()
        builder.setView(input)

        builder.setPositiveButton("Submit") { dialog, _ ->
            val value = input.text.toString().trim()
            if (isValid(value)) {
                onScanSuccess(value.uppercase())
            } else {
                val errorMsg = if (scanMode == MODE_VIN) "Invalid VIN format. Must be 17 characters." else "Part number cannot be empty."
                showToast(errorMsg)
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