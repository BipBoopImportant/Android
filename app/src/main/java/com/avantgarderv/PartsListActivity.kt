package com.avantgarderv

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.avantgarderv.adapters.PartAdapter
import com.avantgarderv.data.Part
import com.avantgarderv.data.RVDatabase
import com.avantgarderv.databinding.ActivityPartsListBinding

class PartsListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPartsListBinding
    private lateinit var partAdapter: PartAdapter
    private var allParts: List<Part> = emptyList()

    private val scannerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val partNumber = result.data?.getStringExtra(ScannerActivity.SCAN_RESULT)
                if (!partNumber.isNullOrEmpty()) {
                    handleScannedPart(partNumber)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPartsListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Parts Inventory"

        setupRecyclerView()
        setupSearchView()

        binding.fabAddPart.setOnClickListener {
            startActivity(Intent(this, PartDetailActivity::class.java))
        }

        binding.fabScanPart.setOnClickListener {
            val intent = Intent(this, ScannerActivity::class.java)
            intent.putExtra(ScannerActivity.SCAN_MODE, ScannerActivity.MODE_PART)
            scannerLauncher.launch(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadParts()
    }

    private fun loadParts() {
        allParts = RVDatabase.getAllParts()
        filterParts(binding.searchView.query.toString())
    }

    private fun handleScannedPart(partNumber: String) {
        val part = RVDatabase.findPartByNumber(partNumber)
        if (part != null) {
            val intent = Intent(this, PartDetailActivity::class.java)
            intent.putExtra("PART_NUMBER", part.partNumber)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Part not found: $partNumber", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupRecyclerView() {
        partAdapter = PartAdapter { part ->
            val intent = Intent(this, PartDetailActivity::class.java)
            intent.putExtra("PART_NUMBER", part.partNumber)
            startActivity(intent)
        }
        binding.partsRecyclerView.apply {
            adapter = partAdapter
            layoutManager = LinearLayoutManager(this@PartsListActivity)
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterParts(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterParts(newText)
                return true
            }
        })
    }

    private fun filterParts(query: String?) {
        val filteredList = if (query.isNullOrBlank()) {
            allParts
        } else {
            val lowerCaseQuery = query.lowercase()
            allParts.filter {
                it.description.lowercase().contains(lowerCaseQuery) ||
                it.partNumber.lowercase().contains(lowerCaseQuery) ||
                it.category.lowercase().contains(lowerCaseQuery)
            }
        }
        partAdapter.submitList(filteredList)
    }
}