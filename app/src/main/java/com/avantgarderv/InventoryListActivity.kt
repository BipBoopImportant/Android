package com.avantgarderv

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avantgarderv.data.RV
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
        // This simulates the fast caching requested
        val rvList = RVDatabase.getAllRVs()
        inventoryAdapter.submitList(rvList)
    }
}

class RVAdapter(private val onClick: (RV) -> Unit) :
    RecyclerView.Adapter<RVAdapter.RVViewHolder>() {

    private var rvs: List<RV> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RVViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_rv, parent, false)
        return RVViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: RVViewHolder, position: Int) {
        holder.bind(rvs[position])
    }

    override fun getItemCount(): Int = rvs.size

    fun submitList(newRvs: List<RV>) {
        rvs = newRvs
        notifyDataSetChanged() // Simple refresh for this procedural style
    }

    class RVViewHolder(itemView: View, val onClick: (RV) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.rvNameTextView)
        private val vinTextView: TextView = itemView.findViewById(R.id.rvVinTextView)
        private val statusTextView: TextView = itemView.findViewById(R.id.rvStatusTextView)
        private var currentRv: RV? = null

        init {
            itemView.setOnClickListener {
                currentRv?.let {
                    onClick(it)
                }
            }
        }

        fun bind(rv: RV) {
            currentRv = rv
            nameTextView.text = "${rv.year} ${rv.make} ${rv.model}"
            vinTextView.text = "VIN: ${rv.vin}"
            statusTextView.text = "Status: ${rv.status}"
        }
    }
}