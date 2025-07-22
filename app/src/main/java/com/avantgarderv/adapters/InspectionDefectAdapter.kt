package com.avantgarderv.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.avantgarderv.R
import com.avantgarderv.data.InspectionItem

class InspectionDefectAdapter(
    private val onEditClick: (InspectionItem) -> Unit,
    private val onDeleteClick: (InspectionItem) -> Unit
) : RecyclerView.Adapter<InspectionDefectAdapter.DefectViewHolder>() {

    private var items: List<InspectionItem> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefectViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_inspection_defect, parent, false)
        return DefectViewHolder(view, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: DefectViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<InspectionItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    class DefectViewHolder(
        itemView: View,
        val onEditClick: (InspectionItem) -> Unit,
        val onDeleteClick: (InspectionItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val descriptionTextView: TextView = itemView.findViewById(R.id.defectDescriptionTextView)
        private val locationTextView: TextView = itemView.findViewById(R.id.defectLocationTextView)
        private val mediaCountTextView: TextView = itemView.findViewById(R.id.defectMediaCountTextView)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteDefectButton)
        private var currentItem: InspectionItem? = null

        init {
            itemView.setOnClickListener {
                currentItem?.let { onEditClick(it) }
            }
        }

        fun bind(item: InspectionItem) {
            currentItem = item
            descriptionTextView.text = item.defectDescription
            locationTextView.text = "Location: ${item.location.ifEmpty { "N/A" }}"
            locationTextView.visibility = if (item.location.isNotEmpty()) View.VISIBLE else View.GONE

            val mediaText = "Photos: ${item.imageUris.size}, Videos: ${item.videoUris.size}"
            mediaCountTextView.text = mediaText

            deleteButton.setOnClickListener {
                currentItem?.let { onDeleteClick(it) }
            }
        }
    }
}