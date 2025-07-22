package com.avantgarderv

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avantgarderv.adapters.ImageAdapter
import com.avantgarderv.adapters.InspectionDefectAdapter
import com.avantgarderv.adapters.VideoAdapter
import com.avantgarderv.data.Inspection
import com.avantgarderv.data.InspectionItem
import com.avantgarderv.data.RVDatabase
import com.avantgarderv.databinding.ActivityInspectionDetailBinding
import java.util.Date
import java.util.UUID

class InspectionDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInspectionDetailBinding
    private var currentInspection: Inspection? = null
    private var currentVin: String? = null

    private lateinit var defectAdapter: InspectionDefectAdapter
    private val inspectionItems = mutableListOf<InspectionItem>()

    // --- FIX: Activity Result Launchers must be registered at the Activity level ---
    private lateinit var selectImagesLauncher: ActivityResultLauncher<String>
    private lateinit var selectVideosLauncher: ActivityResultLauncher<String>

    // --- FIX: Helper properties to link launchers to the active dialog's data ---
    private var currentDialogImageUris: MutableList<String>? = null
    private var currentDialogVideoUris: MutableList<String>? = null
    private var currentDialogImageAdapter: ImageAdapter? = null
    private var currentDialogVideoAdapter: VideoAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInspectionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- FIX: Initialize launchers in onCreate ---
        initializeLaunchers()

        setupRecyclerView()
        setupSpinner()

        currentVin = intent.getStringExtra("VIN")
        val inspectionId = intent.getStringExtra("INSPECTION_ID")

        if (inspectionId != null) {
            currentInspection = RVDatabase.findInspectionById(inspectionId)
            title = "Edit Inspection"
            currentInspection?.let { populateFields(it) }
        } else {
            title = "New Inspection"
        }

        if (currentVin == null && currentInspection == null) {
            Toast.makeText(this, "Error: VIN not found.", Toast.LENGTH_LONG).show()
            finish(); return
        }

        binding.addDefectItemButton.setOnClickListener {
            showAddEditDefectDialog(null)
        }
        binding.saveInspectionButton.setOnClickListener { saveInspection() }
    }

    private fun initializeLaunchers() {
        selectImagesLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            uris?.let {
                currentDialogImageUris?.let { list ->
                    it.forEach { uri -> contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                    list.addAll(it.map(Uri::toString))
                    currentDialogImageAdapter?.notifyDataSetChanged()
                }
            }
        }
        selectVideosLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            uris?.let {
                currentDialogVideoUris?.let { list ->
                    it.forEach { uri -> contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                    list.addAll(it.map(Uri::toString))
                    currentDialogVideoAdapter?.notifyDataSetChanged()
                }
            }
        }
    }

    private fun populateFields(inspection: Inspection) {
        binding.editInspectionTitle.setText(inspection.title)
        val inspectionTypes = resources.getStringArray(R.array.inspection_types)
        val typePosition = inspectionTypes.indexOf(inspection.inspectionType)
        if (typePosition >= 0) binding.spinnerInspectionType.setSelection(typePosition)

        inspectionItems.clear()
        inspectionItems.addAll(inspection.items)
        defectAdapter.submitList(inspectionItems)
    }

    private fun setupRecyclerView() {
        defectAdapter = InspectionDefectAdapter(
            onEditClick = { item -> showAddEditDefectDialog(item) },
            onDeleteClick = { item -> showDeleteConfirmationDialog(item) }
        )
        binding.defectsRecyclerView.apply {
            adapter = defectAdapter
            layoutManager = LinearLayoutManager(this@InspectionDetailActivity)
        }
    }

    private fun setupSpinner() {
        ArrayAdapter.createFromResource(
            this, R.array.inspection_types, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerInspectionType.adapter = adapter
        }
    }

    private fun showDeleteConfirmationDialog(item: InspectionItem) {
        AlertDialog.Builder(this)
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete this defect item: '${item.defectDescription}'?")
            .setPositiveButton("Delete") { _, _ ->
                val index = inspectionItems.indexOfFirst { it.id == item.id }
                if (index != -1) {
                    inspectionItems.removeAt(index)
                    defectAdapter.submitList(inspectionItems)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddEditDefectDialog(itemToEdit: InspectionItem?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_defect, null)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle(if (itemToEdit == null) "Add Defect Item" else "Edit Defect Item")
        val dialog = builder.create()

        // --- Dialog View References ---
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.editDefectDescription)
        val locationEditText = dialogView.findViewById<EditText>(R.id.editDefectLocation)
        val imagesRecyclerView = dialogView.findViewById<RecyclerView>(R.id.imagesRecyclerView)
        val videosRecyclerView = dialogView.findViewById<RecyclerView>(R.id.videosRecyclerView)
        val addImageButton = dialogView.findViewById<Button>(R.id.addImageButton)
        val addVideoButton = dialogView.findViewById<Button>(R.id.addVideoButton)

        // --- Data & Adapters for the Dialog ---
        val dialogImageUris = mutableListOf<String>()
        val dialogVideoUris = mutableListOf<String>()
        itemToEdit?.let {
            descriptionEditText.setText(it.defectDescription)
            locationEditText.setText(it.location)
            dialogImageUris.addAll(it.imageUris)
            dialogVideoUris.addAll(it.videoUris)
        }

        val imageAdapter = ImageAdapter(this, dialogImageUris) { pos ->
            dialogImageUris.removeAt(pos)
            currentDialogImageAdapter?.notifyItemRemoved(pos)
        }
        val videoAdapter = VideoAdapter(this, dialogVideoUris) { pos ->
            dialogVideoUris.removeAt(pos)
            currentDialogVideoAdapter?.notifyItemRemoved(pos)
        }

        // --- FIX: Hook up dialog data/adapters to the Activity's helper properties ---
        this.currentDialogImageUris = dialogImageUris
        this.currentDialogVideoUris = dialogVideoUris
        this.currentDialogImageAdapter = imageAdapter
        this.currentDialogVideoAdapter = videoAdapter

        imagesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        imagesRecyclerView.adapter = imageAdapter
        videosRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        videosRecyclerView.adapter = videoAdapter

        addImageButton.setOnClickListener { selectImagesLauncher.launch("image/*") }
        addVideoButton.setOnClickListener { selectVideosLauncher.launch("video/*") }

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Save") { _, _ ->
            val description = descriptionEditText.text.toString().trim()
            if (description.isEmpty()) {
                Toast.makeText(this, "Defect description cannot be empty.", Toast.LENGTH_SHORT).show()
                return@setButton
            }
            val newItem = InspectionItem(
                id = itemToEdit?.id ?: UUID.randomUUID().toString(),
                defectDescription = description,
                location = locationEditText.text.toString().trim(),
                imageUris = dialogImageUris,
                videoUris = dialogVideoUris
            )
            val existingIndex = inspectionItems.indexOfFirst { it.id == newItem.id }
            if (existingIndex != -1) {
                inspectionItems[existingIndex] = newItem
            } else {
                inspectionItems.add(newItem)
            }
            defectAdapter.submitList(inspectionItems)
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel") { d, _ -> d.cancel() }
        dialog.setOnDismissListener {
            // --- FIX: Cleanup to prevent memory leaks ---
            this.currentDialogImageUris = null
            this.currentDialogVideoUris = null
            this.currentDialogImageAdapter = null
            this.currentDialogVideoAdapter = null
        }
        dialog.show()
    }

    private fun saveInspection() {
        val title = binding.editInspectionTitle.text.toString().trim()
        if (title.isEmpty()) {
            Toast.makeText(this, "Inspection title cannot be empty.", Toast.LENGTH_SHORT).show()
            return
        }

        val inspectionToSave = Inspection(
            id = currentInspection?.id ?: UUID.randomUUID().toString(),
            vin = currentInspection?.vin ?: currentVin!!,
            title = title,
            inspectionType = binding.spinnerInspectionType.selectedItem.toString(),
            date = currentInspection?.date ?: Date().time,
            items = inspectionItems
        )
        RVDatabase.addOrUpdateInspection(inspectionToSave)

        Toast.makeText(this, "Inspection Saved!", Toast.LENGTH_SHORT).show()
        finish()
    }
}