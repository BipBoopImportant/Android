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
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.avantgarderv.databinding.ActivityScannerBinding
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.regex.Pattern

class ScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScannerBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService
    private var isProcessing = false
    private var isBarcodeMode = true

    // Regex to validate a 17-character VIN. Excludes I, O, Q.
    private val vinPattern: Pattern = Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera()
        setupUI()
    }

    private fun setupUI() {
        binding.toggleScanMode.check(R.id.barcodeModeButton)
        binding.toggleScanMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isBarcodeMode = checkedId == R.id.barcodeModeButton
                binding.scannerPrompt.text = if (isBarcodeMode) {
                    "Point camera at VIN Barcode"
                } else {
                    "Point camera at written VIN"
                }
            }
        }

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
    private fun processImage(imageProxy: androidx.camera.core.ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            if (isBarcodeMode) {
                scanBarcodes(image, imageProxy)
            } else {
                recognizeText(image, imageProxy)
            }
        }
    }

    private fun scanBarcodes(image: InputImage, imageProxy: androidx.camera.core.ImageProxy) {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_CODE_39, Barcode.FORMAT_CODE_128, Barcode.FORMAT_QR_CODE)
            .build()
        val scanner = BarcodeScanning.getClient(options)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue
                    if (rawValue != null && vinPattern.matcher(rawValue).matches()) {
                        onVinFound(rawValue)
                        break // Found a valid VIN, stop processing
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

    private fun recognizeText(image: InputImage, imageProxy: androidx.camera.core.ImageProxy) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val fullText = visionText.text.replace("\\s+".toRegex(), "").uppercase()
                if (vinPattern.matcher(fullText).matches()) {
                    onVinFound(fullText)
                } else {
                    // Search for VIN within text blocks if full text fails
                    for (block in visionText.textBlocks) {
                        val blockText = block.text.replace("\\s+".toRegex(), "").uppercase()
                        if (vinPattern.matcher(blockText).matches()) {
                            onVinFound(blockText)
                            break
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ScannerActivity", "Text recognition failed.", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
                isProcessing = false
            }
    }

    private fun onVinFound(vin: String) {
        if (!isFinishing) { // Ensure activity is still active
            Log.d("ScannerActivity", "VIN Found: $vin")
            val resultIntent = Intent()
            resultIntent.putExtra("SCAN_RESULT", vin)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun showVinInputDialog() {
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