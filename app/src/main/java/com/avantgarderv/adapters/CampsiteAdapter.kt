package com.avantgarderv.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.avantgarderv.R
import com.avantgarderv.data.CampsiteInfo

class CampsiteAdapter(
    private val onEdit: (CampsiteInfo) -> Unit,
    private val onDelete: (CampsiteInfo) -> Unit
) : RecyclerView.Adapter<CampsiteAdapter.CampsiteViewHolder>() {

    private var campsites: List<CampsiteInfo> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CampsiteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_campsite, parent, false)
        return CampsiteViewHolder(view, onEdit, onDelete)
    }

    override fun onBindViewHolder(holder: CampsiteViewHolder, position: Int) {
        holder.bind(campsites[position])
    }

    override fun getItemCount(): Int = campsites.size

    fun submitList(newList: List<CampsiteInfo>) {
        campsites = newList
        notifyDataSetChanged()
    }

    class CampsiteViewHolder(
        itemView: View,
        val onEdit: (CampsiteInfo) -> Unit,
        val onDelete: (CampsiteInfo) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val siteName: TextView = itemView.findViewById(R.id.campsiteNameTextView)
        private val lotNumber: TextView = itemView.findViewById(R.id.campsiteLotTextView)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteCampsiteButton)
        private var currentCampsite: CampsiteInfo? = null

        init {
            itemView.setOnClickListener { currentCampsite?.let { onEdit(it) } }
        }

        fun bind(campsite: CampsiteInfo) {
            currentCampsite = campsite
            siteName.text = campsite.siteName
            lotNumber.text = "Lot: ${campsite.lotNumber}"
            deleteButton.setOnClickListener { currentCampsite?.let { onDelete(it) } }
        }
    }
}