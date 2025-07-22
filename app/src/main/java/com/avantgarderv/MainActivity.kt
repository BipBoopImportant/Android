package com.avantgarderv

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.avantgarderv.data.RVDatabase
import com.avantgarderv.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.scanVinButton.setOnClickListener {
            showVinInputDialog()
        }

        binding.viewInventoryButton.setOnClickListener {
            startActivity(Intent(this, InventoryListActivity::class.java))
        }
    }

    private fun showVinInputDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter VIN")
        builder.setMessage("Simulating VIN scan (Barcode/OCR). Please enter a VIN manually.")

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("Submit") { dialog, _ ->
            val vin = input.text.toString().trim()
            if (vin.isNotEmpty()) {
                handleVin(vin)
            } else {
                showToast("VIN cannot be empty.")
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun handleVin(vin: String) {
        val existingRv = RVDatabase.findRVByVin(vin)
        if (existingRv != null) {
            // RV exists, show actions for existing RV
            showExistingRvActions(vin)
        } else {
            // RV is new, show actions for a new RV
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
                    0 -> Intent(this, RVDetailActivity::class.java) // Verify Details
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
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}