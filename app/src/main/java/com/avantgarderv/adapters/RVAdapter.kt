package com.avantgarderv.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.avantgarderv.R
import com.avantgarderv.data.RV

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
        notifyDataSetChanged() // Simple refresh for this demonstration
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