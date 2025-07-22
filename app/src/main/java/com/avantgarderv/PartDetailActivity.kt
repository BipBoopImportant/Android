package com.avantgarderv

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.avantgarderv.data.Part
import com.avantgarderv.data.RVDatabase
import com.avantgarderv.databinding.ActivityPartDetailBinding

class PartDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPartDetailBinding
    private var currentPart: Part? = null

    private val scannerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val partNumber = result.data?.getStringExtra(ScannerActivity.SCAN_RESULT)
                if (!partNumber.isNullOrEmpty()) {
                    binding.editPartNumber.setText(partNumber)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPartDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val partNumber = intent.getStringExtra("PART_NUMBER")
        if (partNumber != null) {
            currentPart = RVDatabase.findPartByNumber(partNumber)
            title = "Edit Part"
            binding.editPartNumber.isEnabled = false
            binding.partNumberLayout.isEndIconVisible = false // Can't scan to change an existing part number
            currentPart?.let { populateFields(it) }
        } else {
            title = "Add New Part"
            binding.partNumberLayout.setEndIconOnClickListener {
                val intent = Intent(this, ScannerActivity::class.java)
                intent.putExtra(ScannerActivity.SCAN_MODE, ScannerActivity.MODE_PART)
                scannerLauncher.launch(intent)
            }
        }

        binding.savePartButton.setOnClickListener {
            savePart()
        }
    }

    private fun populateFields(part: Part) {
        binding.editPartNumber.setText(part.partNumber)
        binding.editPartDescription.setText(part.description)
        binding.editPartCategory.setText(part.category)
        binding.editPartSupplier.setText(part.supplier)
        binding.editPartQuantity.setText(part.inStockQuantity.toString())
        binding.editPartCost.setText(part.cost.toString())
        binding.editPartPrice.setText(part.price.toString())
    }

    private fun savePart() {
        val partNumber = binding.editPartNumber.text.toString().trim()
        val description = binding.editPartDescription.text.toString().trim()
        val category = binding.editPartCategory.text.toString().trim()
        val quantity = binding.editPartQuantity.text.toString().toIntOrNull()
        val cost = binding.editPartCost.text.toString().toDoubleOrNull()
        val price = binding.editPartPrice.text.toString().toDoubleOrNull()

        if (partNumber.isEmpty() || description.isEmpty() || category.isEmpty() || quantity == null || cost == null || price == null) {
            Toast.makeText(this, "Please fill all required fields correctly.", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentPart == null && RVDatabase.findPartByNumber(partNumber) != null) {
            Toast.makeText(this, "A part with this Part Number already exists.", Toast.LENGTH_LONG).show()
            return
        }

        val partToSave = Part(
            partNumber = partNumber,
            description = description,
            category = category,
            supplier = binding.editPartSupplier.text.toString().trim().takeIf { it.isNotEmpty() },
            inStockQuantity = quantity,
            cost = cost,
            price = price
        )

        RVDatabase.addOrUpdatePart(partToSave)
        Toast.makeText(this, "Part Saved Successfully!", Toast.LENGTH_SHORT).show()
        finish()
    }
}