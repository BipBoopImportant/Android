package com.avantgarderv

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.avantgarderv.data.RV
import com.avantgarderv.data.RVDatabase
import com.avantgarderv.databinding.ActivityAddEditRvBinding

class AddEditRVActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditRvBinding
    private var existingVin: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditRvBinding.inflate(layoutInflater)
        setContentView(binding.root)

        existingVin = intent.getStringExtra("VIN")

        if (existingVin != null) {
            // This is an edit operation
            title = "Edit RV"
            val rv = RVDatabase.findRVByVin(existingVin!!)
            if (rv != null) {
                populateFields(rv)
            }
            binding.editVin.isEnabled = false // Don't allow editing VIN
        } else {
            // This is an add operation
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
    }

    private fun saveRV() {
        val vin = binding.editVin.text.toString().trim()
        val make = binding.editMake.text.toString().trim()
        val model = binding.editModel.text.toString().trim()
        val year = binding.editYear.text.toString().toIntOrNull()
        val price = binding.editPrice.text.toString().toDoubleOrNull()
        val mileage = binding.editMileage.text.toString().toIntOrNull()

        if (vin.isEmpty() || make.isEmpty() || model.isEmpty() || year == null || price == null || mileage == null) {
            Toast.makeText(this, "Please fill all fields correctly.", Toast.LENGTH_SHORT).show()
            return
        }

        val newRv = RV(vin, make, model, year, price, mileage)
        RVDatabase.addOrUpdateRV(newRv)

        Toast.makeText(this, "RV Saved Successfully!", Toast.LENGTH_SHORT).show()
        finish() // Close the activity and return to the previous one
    }
}