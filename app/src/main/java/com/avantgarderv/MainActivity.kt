package com.avantgarderv

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.avantgarderv.data.RVDatabase
import com.avantgarderv.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startScanner()
            } else {
                showToast("Camera permission is required to scan VINs.")
            }
        }

    private val scannerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val vin = result.data?.getStringExtra("SCAN_RESULT")
                if (!vin.isNullOrEmpty()) {
                    handleVin(vin)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.scanVinButton.setOnClickListener {
            checkCameraPermissionAndStartScanner()
        }

        binding.viewInventoryButton.setOnClickListener {
            startActivity(Intent(this, InventoryListActivity::class.java))
        }
    }

    private fun checkCameraPermissionAndStartScanner() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                startScanner()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showPermissionRationale()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startScanner() {
        scannerLauncher.launch(Intent(this, ScannerActivity::class.java))
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Camera Permission Needed")
            .setMessage("This app needs camera access to scan VIN barcodes and text. Please grant permission.")
            .setPositiveButton("OK") { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleVin(vin: String) {
        val existingRv = RVDatabase.findRVByVin(vin)
        if (existingRv != null) {
            showExistingRvActions(vin)
        } else {
            showNewRvActions(vin)
        }
    }

    private fun showNewRvActions(vin: String) {
        val actions = arrayOf("Add New RV to Inventory")
        AlertDialog.Builder(this)
            .setTitle("New VIN Detected: $vin")
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> { // Add New RV
                        val intent = Intent(this, AddEditRVActivity::class.java)
                        intent.putExtra("VIN", vin)
                        startActivity(intent)
                    }
                }
            }
            .show()
    }

    private fun showExistingRvActions(vin: String) {
        val actions = arrayOf(
            "Verify/View RV Details",
            "Start Inspection",
            "Create Service Appointment",
            "Create Work Order"
        )
        AlertDialog.Builder(this)
            .setTitle("Existing RV Found: $vin")
            .setItems(actions) { _, which ->
                val intent = when (which) {
                    0 -> Intent(this, RVDetailActivity::class.java)
                    1 -> Intent(this, InspectionActivity::class.java)
                    2 -> Intent(this, ServiceAppointmentActivity::class.java)
                    3 -> Intent(this, WorkOrderActivity::class.java)
                    else -> null
                }
                intent?.putExtra("VIN", vin)
                startActivity(intent)
            }
            .show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}