package com.avantgarderv

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.avantgarderv.adapters.ImageAdapter
import com.avantgarderv.adapters.VideoAdapter
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

    private val selectImagesLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        handleSelectedMedia(uris, "image")
    }

    private val selectVideosLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        handleSelectedMedia(uris, "video")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRvDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val vin = intent.getStringExtra("VIN")
        if (vin == null) {
            finish()
            return
        }
        currentRv = RVDatabase.findRVByVin(vin)

        setupUI()
    }

    override fun onResume() {
        super.onResume()
        currentRv?.let { populateDetails(it) }
    }

    private fun setupUI() {
        binding.editButton.setOnClickListener {
            val intent = Intent(this, AddEditRVActivity::class.java)
            intent.putExtra("VIN", currentRv?.vin)
            startActivity(intent)
        }

        binding.addImageButton.setOnClickListener {
            selectImagesLauncher.launch("image/*")
        }

        binding.addVideoButton.setOnClickListener {
            selectVideosLauncher.launch("video/*")
        }

        setupRecyclerViews()
    }

    private fun setupRecyclerViews() {
        // Image RecyclerView
        binding.imagesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        imageAdapter = ImageAdapter(this, currentRv?.imageUris ?: emptyList())
        binding.imagesRecyclerView.adapter = imageAdapter

        // Video RecyclerView
        binding.videosRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        videoAdapter = VideoAdapter(this, currentRv?.videoUris ?: emptyList())
        binding.videosRecyclerView.adapter = videoAdapter
    }

    private fun populateDetails(rv: RV) {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
        val numberFormat = NumberFormat.getNumberInstance(Locale.US)

        binding.detailName.text = "${rv.year} ${rv.make} ${rv.model}"
        binding.detailVin.text = "VIN: ${rv.vin}"
        binding.detailStatus.text = "Status: ${rv.status}"
        binding.detailPrice.text = "Price: ${currencyFormat.format(rv.price)}"
        binding.detailMileage.text = "Mileage: ${numberFormat.format(rv.mileage)} mi"
        binding.detailDescription.text = rv.description.ifEmpty { "No description available." }

        // Refresh adapters
        refreshMediaAdapters()
    }
    
    private fun refreshMediaAdapters() {
        imageAdapter = ImageAdapter(this, currentRv?.imageUris ?: emptyList())
        binding.imagesRecyclerView.adapter = imageAdapter

        videoAdapter = VideoAdapter(this, currentRv?.videoUris ?: emptyList())
        binding.videosRecyclerView.adapter = videoAdapter
    }

    private fun handleSelectedMedia(uris: List<Uri>, type: String) {
        if (uris.isNotEmpty()) {
            val contentResolver = applicationContext.contentResolver
            uris.forEach { uri ->
                // Take persistent permissions so we can access the file later
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (type == "image") {
                    currentRv?.imageUris?.add(uri.toString())
                } else {
                    currentRv?.videoUris?.add(uri.toString())
                }
            }
            refreshMediaAdapters()
        }
    }
}