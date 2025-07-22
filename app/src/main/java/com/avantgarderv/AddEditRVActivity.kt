package com.avantgarderv

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.avantgarderv.adapters.ImageAdapter
import com.avantgarderv.adapters.VideoAdapter
import com.avantgarderv.data.RV
import com.avantgarderv.data.RVDatabase
import com.avantgarderv.databinding.ActivityAddEditRvBinding

class AddEditRVActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditRvBinding
    private var currentRv: RV? = null

    private lateinit var imageAdapter: ImageAdapter
    private lateinit var videoAdapter: VideoAdapter

    private val imageUris = mutableListOf<String>()
    private val videoUris = mutableListOf<String>()

    private val selectImagesLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        handleSelectedMedia(uris, "image")
    }

    private val selectVideosLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        handleSelectedMedia(uris, "video")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditRvBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerViews()

        val passedVin = intent.getStringExtra("VIN")
        if (passedVin != null) {
            currentRv = RVDatabase.findRVByVin(passedVin)
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

        binding.saveButton.setOnClickListener { saveRV() }
        binding.addImageButton.setOnClickListener { selectImagesLauncher.launch("image/*") }
        binding.addVideoButton.setOnClickListener { selectVideosLauncher.launch("video/*") }
    }

    private fun populateFields(rv: RV) {
        binding.editVin.setText(rv.vin)
        binding.editMake.setText(rv.make)
        binding.editModel.setText(rv.model)
        binding.editYear.setText(rv.year.toString())
        binding.editPrice.setText(rv.price.toString())
        binding.editMileage.setText(rv.mileage.toString())
        binding.editDescription.setText(rv.description)
        
        imageUris.addAll(rv.imageUris)
        videoUris.addAll(rv.videoUris)
        refreshMediaAdapters()
    }

    private fun setupRecyclerViews() {
        binding.imagesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        imageAdapter = ImageAdapter(this, imageUris) { position -> showRemoveConfirmationDialog("image", position) }
        binding.imagesRecyclerView.adapter = imageAdapter

        binding.videosRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        videoAdapter = VideoAdapter(this, videoUris) { position -> showRemoveConfirmationDialog("video", position) }
        binding.videosRecyclerView.adapter = videoAdapter
    }
    
    private fun refreshMediaAdapters() {
        imageAdapter.notifyDataSetChanged()
        videoAdapter.notifyDataSetChanged()
    }

    private fun handleSelectedMedia(uris: List<Uri>, type: String) {
        if (uris.isNotEmpty()) {
            val contentResolver = applicationContext.contentResolver
            uris.forEach { uri ->
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (type == "image") {
                    imageUris.add(uri.toString())
                } else {
                    videoUris.add(uri.toString())
                }
            }
            refreshMediaAdapters()
        }
    }

    private fun showRemoveConfirmationDialog(type: String, position: Int) {
        val itemType = if (type == "image") "image" else "video"
        AlertDialog.Builder(this)
            .setTitle("Remove $itemType")
            .setMessage("Are you sure you want to remove this $itemType?")
            .setPositiveButton("Remove") { _, _ ->
                if (type == "image") {
                    imageUris.removeAt(position)
                } else {
                    videoUris.removeAt(position)
                }
                refreshMediaAdapters()
            }
            .setNegativeButton("Cancel", null)
            .show()
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

        val rvToSave = RV(
            vin = vin,
            make = make,
            model = model,
            year = year,
            price = price,
            mileage = mileage,
            description = description,
            status = currentRv?.status ?: "In Stock", // Preserve status if editing
            imageUris = imageUris,
            videoUris = videoUris
        )
        RVDatabase.addOrUpdateRV(rvToSave)

        Toast.makeText(this, "RV Saved Successfully!", Toast.LENGTH_SHORT).show()
        finish()
    }
}