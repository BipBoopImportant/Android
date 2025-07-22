package com.avantgarderv

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.avantgarderv.data.RV
import com.avantgarderv.data.RVDatabase
import com.avantgarderv.databinding.ActivityAddEditRvBinding

class AddEditRVActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditRvBinding
    private var passedVin: String? = null
    private var currentRv: RV? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditRvBinding.inflate(layoutInflater)
        setContentView(binding.root)

        passedVin = intent.getStringExtra("VIN")

        if (passedVin != null) {
            currentRv = RVDatabase.findRVByVin(passedVin!!)
            if (currentRv != null) {
                // EDIT MODE
                title = "Edit RV"
                populateFields(currentRv!!)
                binding.editVin.isEnabled = false
            } else {
                // ADD FROM SCAN MODE
                title = "Add New RV"
                binding.editVin.setText(passedVin)
                binding.editVin.isEnabled = false
            }
        } else {
            // ADD FROM SCRATCH MODE
            title = "Add New RV"
        }

        binding.saveButton.setOnClickListener {
            saveRV()
        }
    }

    private fun populateFields(rv: RV) {
        binding.editVin.setText(rv.vin)
        binding.editMake.setText(rv.make)
        binding.editModel.setText(rv.model)
        binding.editYear.setText(rv.year.toString())
        binding.editPrice.setText(rv.price.toString())
        binding.editMileage.setText(rv.mileage.toString())
        binding.editDescription.setText(rv.description)
    }

    private fun saveRV() {
        val vin = binding.editVin.text.toString().trim()
        val make = binding.editMake.text.toString().trim()
        val model = binding.editModel.text.toString().trim()
        val year = binding.editYear.text.toString().toIntOrNull()
        val price = binding.editPrice.text.toString().toDoubleOrNull()
        val mileage = binding.editMileage.text.toString().toIntOrNull()
        val description = binding.editDescription.text.toString().trim()

        if (vin.isEmpty() || make.isEmpty() || model.isEmpty() || year == null || price == null || mileage == null) {
            Toast.makeText(this, "Please fill all fields correctly.", Toast.LENGTH_SHORT).show()
            return
        }

        // Create or update RV. If new, media URIs will be empty lists by default.
        val rvToSave = RV(
            vin = vin,
            make = make,
            model = model,
            year = year,
            price = price,
            mileage = mileage,
            description = description,
            status = currentRv?.status ?: "In Stock",
            imageUris = currentRv?.imageUris ?: mutableListOf(),
            videoUris = currentRv?.videoUris ?: mutableListOf()
        )
        RVDatabase.addOrUpdateRV(rvToSave)

        Toast.makeText(this, "RV Saved Successfully!", Toast.LENGTH_SHORT).show()
        finish()
    }
}