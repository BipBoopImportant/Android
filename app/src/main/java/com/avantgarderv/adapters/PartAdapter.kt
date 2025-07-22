package com.avantgarderv.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.avantgarderv.R
import com.avantgarderv.data.Part
import java.text.NumberFormat
import java.util.Locale

class PartAdapter(private val onClick: (Part) -> Unit) :
    RecyclerView.Adapter<PartAdapter.PartViewHolder>() {

    private var parts: List<Part> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_part, parent, false)
        return PartViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: PartViewHolder, position: Int) {
        holder.bind(parts[position])
    }

    override fun getItemCount(): Int = parts.size

    fun submitList(newParts: List<Part>) {
        parts = newParts
        notifyDataSetChanged()
    }

    class PartViewHolder(itemView: View, val onClick: (Part) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val description: TextView = itemView.findViewById(R.id.partDescriptionTextView)
        private val partNumber: TextView = itemView.findViewById(R.id.partNumberTextView)
        private val category: TextView = itemView.findViewById(R.id.partCategoryTextView)
        private val quantity: TextView = itemView.findViewById(R.id.partQuantityTextView)
        private val price: TextView = itemView.findViewById(R.id.partPriceTextView)
        private var currentPart: Part? = null

        init {
            itemView.setOnClickListener { currentPart?.let { onClick(it) } }
        }

        fun bind(part: Part) {
            currentPart = part
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
            description.text = part.description
            partNumber.text = "PN: ${part.partNumber}"
            category.text = "Category: ${part.category}"
            quantity.text = "In Stock: ${part.inStockQuantity}"
            price.text = currencyFormat.format(part.price)
        }
    }
}