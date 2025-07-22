package com.avantgarderv

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.avantgarderv.adapters.ImageAdapter
import com.avantgarderv.adapters.InspectionAdapter
import com.avantgarderv.adapters.VideoAdapter
import com.avantgarderv.adapters.WorkOrderAdapter
import com.avantgarderv.data.RV
import com.avantgarderv.data.RVDatabase
import com.avantgarderv.databinding.ActivityRvDetailBinding
import java.text.NumberFormat
import java.util.Locale

class RVDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRvDetailBinding
    private var currentRv: RV? = null

    private lateinit var imageAdapter: ImageAdapter
    private lateinit var videoAdapter: VideoAdapter
    private lateinit var inspectionAdapter: InspectionAdapter
    private lateinit var workOrderAdapter: WorkOrderAdapter

    // ... launchers remain the same ...
    private val selectImagesLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> -> handleSelectedMedia(uris, "image") }
    private val selectVideosLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> -> handleSelectedMedia(uris, "video") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRvDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val vin = intent.getStringExtra("VIN")
        if (vin == null) { finish(); return }
        currentRv = RVDatabase.findRVByVin(vin)

        setupUI()
        setupRecyclerViews()
    }

    override fun onResume() {
        super.onResume()
        val vin = currentRv?.vin
        if (vin != null) { currentRv = RVDatabase.findRVByVin(vin) }
        currentRv?.let {
            populateDetails(it)
            refreshMediaAdapters()
            refreshInspectionAdapter()
            refreshWorkOrderAdapter()
        }
    }

    private fun setupUI() {
        binding.editButton.setOnClickListener {
            val intent = Intent(this, AddEditRVActivity::class.java).putExtra("VIN", currentRv?.vin)
            startActivity(intent)
        }
        binding.addImageButton.setOnClickListener { selectImagesLauncher.launch("image/*") }
        binding.addVideoButton.setOnClickListener { selectVideosLauncher.launch("video/*") }
        binding.addInspectionButton.setOnClickListener {
            val intent = Intent(this, InspectionDetailActivity::class.java).putExtra("VIN", currentRv?.vin)
            startActivity(intent)
        }
        binding.addWorkOrderButton.setOnClickListener {
            val intent = Intent(this, WorkOrderDetailActivity::class.java).putExtra("VIN", currentRv?.vin)
            startActivity(intent)
        }
    }

    private fun setupRecyclerViews() {
        binding.imagesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.videosRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.inspectionsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.workOrdersRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun populateDetails(rv: RV) {
        // ... populating name, vin, etc. remains the same ...
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
        val numberFormat = NumberFormat.getNumberInstance(Locale.US)
        binding.detailName.text = "${rv.year} ${rv.make} ${rv.model}"
        binding.detailVin.text = "VIN: ${rv.vin}"
        binding.detailStatus.text = "Status: ${rv.status}"
        binding.detailPrice.text = "Price: ${currencyFormat.format(rv.price)}"
        binding.detailMileage.text = "Mileage: ${numberFormat.format(rv.mileage)} mi"
        binding.detailDescription.text = rv.description.ifEmpty { "No description available." }
    }
    
    // ... refreshMediaAdapters and refreshInspectionAdapter remain the same ...
    private fun refreshMediaAdapters() {
        imageAdapter = ImageAdapter(this, currentRv?.imageUris ?: emptyList()) { p -> showRemoveConfirmationDialog("image", p) }
        binding.imagesRecyclerView.adapter = imageAdapter
        videoAdapter = VideoAdapter(this, currentRv?.videoUris ?: emptyList()) { p -> showRemoveConfirmationDialog("video", p) }
        binding.videosRecyclerView.adapter = videoAdapter
    }
    private fun refreshInspectionAdapter() {
        val inspections = currentRv?.vin?.let { RVDatabase.getInspectionsByVin(it) } ?: emptyList()
        inspectionAdapter = InspectionAdapter { insp ->
            val intent = Intent(this, InspectionDetailActivity::class.java)
                .putExtra("VIN", insp.vin)
                .putExtra("INSPECTION_ID", insp.id)
            startActivity(intent)
        }
        binding.inspectionsRecyclerView.adapter = inspectionAdapter
        inspectionAdapter.submitList(inspections)
    }
    
    private fun refreshWorkOrderAdapter() {
        val workOrders = currentRv?.vin?.let { RVDatabase.getWorkOrdersByVin(it) } ?: emptyList()
        workOrderAdapter = WorkOrderAdapter { wo ->
            val intent = Intent(this, WorkOrderDetailActivity::class.java)
                .putExtra("VIN", wo.vin)
                .putExtra("WORK_ORDER_ID", wo.id)
            startActivity(intent)
        }
        binding.workOrdersRecyclerView.adapter = workOrderAdapter
        workOrderAdapter.submitList(workOrders)
    }
    
    // ... handleSelectedMedia and showRemoveConfirmationDialog remain the same ...
    private fun showRemoveConfirmationDialog(type: String, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Remove $type").setMessage("Are you sure you want to remove this $type?")
            .setPositiveButton("Remove") { _, _ ->
                if (type == "image") { currentRv?.imageUris?.removeAt(position) } else { currentRv?.videoUris?.removeAt(position) }
                refreshMediaAdapters()
            }.setNegativeButton("Cancel", null).show()
    }
    private fun handleSelectedMedia(uris: List<Uri>, type: String) {
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (type == "image") { currentRv?.imageUris?.add(uri.toString()) } else { currentRv?.videoUris?.add(uri.toString()) }
            }
            refreshMediaAdapters()
        }
    }
}