package com.avantgarderv

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.avantgarderv.adapters.CampsiteAdapter
import com.avantgarderv.adapters.RVAdapter
import com.avantgarderv.adapters.WorkOrderAdapter
import com.avantgarderv.data.Client
import com.avantgarderv.data.RVDatabase
import com.avantgarderv.databinding.ActivityClientDetailBinding

class ClientDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientDetailBinding
    private var currentClient: Client? = null

    private lateinit var ownedRvAdapter: RVAdapter
    private lateinit var workOrderAdapter: WorkOrderAdapter
    private lateinit var campsiteAdapter: CampsiteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val clientId = intent.getStringExtra("CLIENT_ID")
        if (clientId == null) {
            Toast.makeText(this, "Client ID not found.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        currentClient = RVDatabase.findClientById(clientId)
        if (currentClient == null) {
            Toast.makeText(this, "Client not found in database.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupRecyclerViews()
    }

    override fun onResume() {
        super.onResume()
        currentClient?.let {
            currentClient = RVDatabase.findClientById(it.id)
        }
        currentClient?.let {
            populateDetails(it)
            loadRelatedData(it.id)
        }
    }

    private fun populateDetails(client: Client) {
        binding.clientDetailName.text = "${client.firstName} ${client.lastName}"
        
        binding.clientDetailCell.text = "Cell: ${client.cellPhone}"
        
        binding.clientDetailLandline.text = "Landline: ${client.landlinePhone ?: "N/A"}"
        binding.clientDetailLandline.visibility = if (client.landlinePhone != null) View.VISIBLE else View.GONE
        
        binding.clientDetailEmail.text = client.email ?: "No email on file"
        binding.clientDetailEmail.visibility = if (client.email != null) View.VISIBLE else View.GONE
        
        val address = buildString {
            client.addressStreet?.let { append(it).append("\n") }
            client.addressCity?.let { append(it) }
            client.addressState?.let { append(", ").append(it) }
            client.addressZip?.let { append(" ").append(it) }
        }
        if (address.isNotBlank()) {
            binding.clientDetailAddress.text = address
            binding.addressCard.visibility = View.VISIBLE
        } else {
            binding.addressCard.visibility = View.GONE
        }

        binding.editClientButton.setOnClickListener {
            val intent = Intent(this, AddEditClientActivity::class.java)
            intent.putExtra("CLIENT_ID", client.id)
            startActivity(intent)
        }
    }

    private fun setupRecyclerViews() {
        ownedRvAdapter = RVAdapter { rv ->
            val intent = Intent(this, RVDetailActivity::class.java)
            intent.putExtra("VIN", rv.vin)
            startActivity(intent)
        }
        binding.ownedRvsRecyclerView.apply {
            adapter = ownedRvAdapter
            layoutManager = LinearLayoutManager(this@ClientDetailActivity)
        }

        workOrderAdapter = WorkOrderAdapter { wo ->
            val intent = Intent(this, WorkOrderDetailActivity::class.java)
            intent.putExtra("WORK_ORDER_ID", wo.id)
            startActivity(intent)
        }
        binding.clientWorkOrdersRecyclerView.apply {
            adapter = workOrderAdapter
            layoutManager = LinearLayoutManager(this@ClientDetailActivity)
        }
        
        campsiteAdapter = CampsiteAdapter(onEdit = {}, onDelete = {})
        binding.campsitesRecyclerView.apply {
            adapter = campsiteAdapter
            layoutManager = LinearLayoutManager(this@ClientDetailActivity)
        }
    }

    private fun loadRelatedData(clientId: String) {
        val ownedRvs = RVDatabase.findRVsByClientId(clientId)
        ownedRvAdapter.submitList(ownedRvs)

        val workOrders = RVDatabase.findWorkOrdersByClientId(clientId)
        workOrderAdapter.submitList(workOrders)
        
        val campsites = currentClient?.campsites ?: emptyList()
        if (campsites.isNotEmpty()) {
            campsiteAdapter.submitList(campsites)
            binding.campsitesCard.visibility = View.VISIBLE
        } else {
            binding.campsitesCard.visibility = View.GONE
        }
    }
}