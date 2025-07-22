package com.avantgarderv

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.avantgarderv.adapters.RVAdapter
import com.avantgarderv.data.RVDatabase
import com.avantgarderv.databinding.ActivityInventoryListBinding

class InventoryListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventoryListBinding
    private lateinit var inventoryAdapter: RVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to the activity
        loadInventory()
    }

    private fun setupRecyclerView() {
        inventoryAdapter = RVAdapter { rv ->
            val intent = Intent(this, RVDetailActivity::class.java)
            intent.putExtra("VIN", rv.vin)
            startActivity(intent)
        }
        binding.inventoryRecyclerView.apply {
            adapter = inventoryAdapter
            layoutManager = LinearLayoutManager(this@InventoryListActivity)
        }
    }

    private fun loadInventory() {
        // Data is fetched directly from the in-memory "database"
        val rvList = RVDatabase.getAllRVs()
        inventoryAdapter.submitList(rvList)
    }
}