package com.avantgarderv

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.avantgarderv.adapters.CampsiteAdapter
import com.avantgarderv.data.CampsiteInfo
import com.avantgarderv.data.Client
import com.avantgarderv.data.RVDatabase
import com.avantgarderv.databinding.ActivityAddEditClientBinding
import java.util.UUID

class AddEditClientActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditClientBinding
    private var currentClient: Client? = null
    private val campsites = mutableListOf<CampsiteInfo>()
    private lateinit var campsiteAdapter: CampsiteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        setupRecyclerView()

        val clientId = intent.getStringExtra("CLIENT_ID")
        if (clientId != null) {
            currentClient = RVDatabase.findClientById(clientId)
            supportActionBar?.title = "Edit Client"
            currentClient?.let { populateFields(it) }
        } else {
            supportActionBar?.title = "Add New Client"
        }

        binding.saveClientButton.setOnClickListener { saveClient() }
        binding.addCampsiteButton.setOnClickListener { showAddEditCampsiteDialog(null) }
    }

    private fun setupRecyclerView() {
        campsiteAdapter = CampsiteAdapter(
            onEdit = { showAddEditCampsiteDialog(it) },
            onDelete = { showDeleteCampsiteDialog(it) }
        )
        binding.campsitesRecyclerView.apply {
            adapter = campsiteAdapter
            layoutManager = LinearLayoutManager(this@AddEditClientActivity)
        }
    }

    private fun populateFields(client: Client) {
        binding.editFirstName.setText(client.firstName)
        binding.editLastName.setText(client.lastName)
        binding.editCellPhone.setText(client.cellPhone)
        binding.editLandline.setText(client.landlinePhone)
        binding.editEmail.setText(client.email)
        binding.editStreetAddress.setText(client.addressStreet)
        binding.editCity.setText(client.addressCity)
        binding.editState.setText(client.addressState)
        binding.editZip.setText(client.addressZip)
        campsites.clear()
        campsites.addAll(client.campsites)
        campsiteAdapter.submitList(campsites)
    }
    
    private fun showDeleteCampsiteDialog(campsite: CampsiteInfo) {
        AlertDialog.Builder(this)
            .setTitle("Delete Campsite")
            .setMessage("Are you sure you want to delete '${campsite.siteName} - Lot ${campsite.lotNumber}'?")
            .setPositiveButton("Delete") { _,_ ->
                val index = campsites.indexOfFirst { it.id == campsite.id }
                if (index != -1) {
                    campsites.removeAt(index)
                    campsiteAdapter.submitList(campsites)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddEditCampsiteDialog(campsite: CampsiteInfo?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_campsite, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.editCampsiteName)
        val lotEditText = dialogView.findViewById<EditText>(R.id.editLotNumber)

        campsite?.let {
            nameEditText.setText(it.siteName)
            lotEditText.setText(it.lotNumber)
        }
        
        AlertDialog.Builder(this)
            .setTitle(if (campsite == null) "Add Campsite" else "Edit Campsite")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = nameEditText.text.toString().trim()
                val lot = lotEditText.text.toString().trim()
                if (name.isEmpty() || lot.isEmpty()) {
                    Toast.makeText(this, "Site name and lot number are required.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                val newItem = CampsiteInfo(campsite?.id ?: UUID.randomUUID().toString(), name, lot)
                val existingIndex = campsites.indexOfFirst { it.id == newItem.id }
                if (existingIndex != -1) {
                    campsites[existingIndex] = newItem
                } else {
                    campsites.add(newItem)
                }
                campsiteAdapter.submitList(campsites)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveClient() {
        val firstName = binding.editFirstName.text.toString().trim()
        val lastName = binding.editLastName.text.toString().trim()
        val cell = binding.editCellPhone.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty() || cell.isEmpty()) {
            Toast.makeText(this, "First name, last name, and cell phone are required.", Toast.LENGTH_SHORT).show()
            return
        }

        val clientToSave = Client(
            id = currentClient?.id ?: UUID.randomUUID().toString(),
            firstName = firstName,
            lastName = lastName,
            cellPhone = cell,
            landlinePhone = binding.editLandline.text.toString().trim().takeIf { it.isNotEmpty() },
            email = binding.editEmail.text.toString().trim().takeIf { it.isNotEmpty() },
            addressStreet = binding.editStreetAddress.text.toString().trim().takeIf { it.isNotEmpty() },
            addressCity = binding.editCity.text.toString().trim().takeIf { it.isNotEmpty() },
            addressState = binding.editState.text.toString().trim().takeIf { it.isNotEmpty() },
            addressZip = binding.editZip.text.toString().trim().takeIf { it.isNotEmpty() },
            campsites = campsites
        )

        RVDatabase.addOrUpdateClient(clientToSave)
        Toast.makeText(this, "Client Saved Successfully!", Toast.LENGTH_SHORT).show()
        finish()
    }
}