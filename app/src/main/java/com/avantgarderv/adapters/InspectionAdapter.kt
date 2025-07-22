package com.avantgarderv.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.avantgarderv.R
import com.avantgarderv.data.Inspection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InspectionAdapter(private val onClick: (Inspection) -> Unit) :
    RecyclerView.Adapter<InspectionAdapter.InspectionViewHolder>() {

    private var inspections: List<Inspection> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InspectionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_inspection, parent, false)
        return InspectionViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: InspectionViewHolder, position: Int) {
        holder.bind(inspections[position])
    }

    override fun getItemCount(): Int = inspections.size

    fun submitList(newInspections: List<Inspection>) {
        inspections = newInspections
        notifyDataSetChanged()
    }

    class InspectionViewHolder(itemView: View, val onClick: (Inspection) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.inspectionTitleTextView)
        private val typeTextView: TextView = itemView.findViewById(R.id.inspectionTypeTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.inspectionDateTextView)
        private var currentInspection: Inspection? = null

        init {
            itemView.setOnClickListener {
                currentInspection?.let { onClick(it) }
            }
        }

        fun bind(inspection: Inspection) {
            currentInspection = inspection
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            titleTextView.text = inspection.title
            typeTextView.text = "Type: ${inspection.inspectionType}"
            dateTextView.text = "Date: ${dateFormat.format(Date(inspection.date))}"
        }
    }
}