package com.avantgarderv

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.avantgarderv.data.RV
import com.avantgarderv.data.RVDatabase
import com.avantgarderv.databinding.ActivityRvDetailBinding
import java.text.NumberFormat
import java.util.Locale

class RVDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRvDetailBinding
    private var currentVin: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRvDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentVin = intent.getStringExtra("VIN")
        if (currentVin == null) {
            // Handle error, VIN is required
            finish()
            return
        }

        binding.editButton.setOnClickListener {
            val intent = Intent(this, AddEditRVActivity::class.java)
            intent.putExtra("VIN", currentVin)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data every time the activity is shown
        currentVin?.let {
            val rv = RVDatabase.findRVByVin(it)
            rv?.let { rvData -> populateDetails(rvData) }
        }
    }

    private fun populateDetails(rv: RV) {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
        val numberFormat = NumberFormat.getNumberInstance(Locale.US)

        binding.detailName.text = "${rv.year} ${rv.make} ${rv.model}"
        binding.detailVin.text = "VIN: ${rv.vin}"
        binding.detailMake.text = "Make: ${rv.make}"
        binding.detailModel.text = "Model: ${rv.model}"
        binding.detailYear.text = "Year: ${rv.year}"
        binding.detailStatus.text = "Status: ${rv.status}"
        binding.detailPrice.text = "Price: ${currencyFormat.format(rv.price)}"
        binding.detailMileage.text = "Mileage: ${numberFormat.format(rv.mileage)} mi"
    }
}