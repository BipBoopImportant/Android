package com.avantgarderv

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.avantgarderv.adapters.ClientAdapter
import com.avantgarderv.data.Client
import com.avantgarderv.data.ClientWithDetails
import com.avantgarderv.data.RVDatabase
import com.avantgarderv.databinding.ActivityClientListBinding

class ClientListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientListBinding
    private lateinit var clientAdapter: ClientAdapter
    private var allClientsWithDetails: List<ClientWithDetails> = emptyList()

    private val scannerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val vin = result.data?.getStringExtra(ScannerActivity.SCAN_RESULT)
                if (!vin.isNullOrEmpty()) {
                    handleScannedVin(vin)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Clients"

        setupRecyclerView()
        setupSearchView()

        binding.fabAddClient.setOnClickListener {
            startActivity(Intent(this, AddEditClientActivity::class.java))
        }

        binding.fabScanVinForClient.setOnClickListener {
            val intent = Intent(this, ScannerActivity::class.java)
            intent.putExtra(ScannerActivity.SCAN_MODE, ScannerActivity.MODE_VIN)
            scannerLauncher.launch(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadClients()
    }

    private fun loadClients() {
        // This is the correct, performant way to load the data.
        // We process it here, once, instead of multiple times in the adapter.
        val rawClients = RVDatabase.getAllClients()
        allClientsWithDetails = rawClients.map { client ->
            ClientWithDetails(
                client = client,
                rvCount = RVDatabase.findRVsByClientId(client.id).size,
                workOrderCount = RVDatabase.findWorkOrdersByClientId(client.id).size
            )
        }
        filterClients(binding.searchView.query.toString())
    }

    private fun handleScannedVin(vin: String) {
        val client = RVDatabase.findClientByVin(vin)
        if (client != null) {
            val intent = Intent(this, ClientDetailActivity::class.java)
            intent.putExtra("CLIENT_ID", client.id)
            startActivity(intent)
        } else {
            Toast.makeText(this, "No client found for VIN: $vin", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupRecyclerView() {
        clientAdapter = ClientAdapter { client ->
            val intent = Intent(this, ClientDetailActivity::class.java)
            intent.putExtra("CLIENT_ID", client.id)
            startActivity(intent)
        }
        binding.clientsRecyclerView.apply {
            adapter = clientAdapter
            layoutManager = LinearLayoutManager(this@ClientListActivity)
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterClients(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterClients(newText)
                return true
            }
        })
    }

    private fun filterClients(query: String?) {
        val filteredList = if (query.isNullOrBlank()) {
            allClientsWithDetails
        } else {
            val lowerCaseQuery = query.lowercase()
            allClientsWithDetails.filter {
                val client = it.client
                "${client.firstName} ${client.lastName}".lowercase().contains(lowerCaseQuery)
            }
        }
        clientAdapter.submitList(filteredList)
    }
}