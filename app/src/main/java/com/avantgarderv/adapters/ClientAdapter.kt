package com.avantgarderv.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.avantgarderv.R
import com.avantgarderv.data.Client
import com.avantgarderv.data.ClientWithDetails

class ClientAdapter(private val onClick: (Client) -> Unit) :
    RecyclerView.Adapter<ClientAdapter.ClientViewHolder>() {

    private var clientsWithDetails: List<ClientWithDetails> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_client, parent, false)
        return ClientViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        holder.bind(clientsWithDetails[position])
    }

    override fun getItemCount(): Int = clientsWithDetails.size

    fun submitList(newClients: List<ClientWithDetails>) {
        clientsWithDetails = newClients
        notifyDataSetChanged()
    }

    class ClientViewHolder(itemView: View, val onClick: (Client) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.clientNameTextView)
        private val phone: TextView = itemView.findViewById(R.id.clientPhoneTextView)
        private val info: TextView = itemView.findViewById(R.id.clientInfoTextView)
        private var currentClient: Client? = null

        init {
            itemView.setOnClickListener { currentClient?.let { onClick(it) } }
        }

        fun bind(clientDetails: ClientWithDetails) {
            currentClient = clientDetails.client
            name.text = "${clientDetails.client.firstName} ${clientDetails.client.lastName}"
            phone.text = clientDetails.client.cellPhone
            info.text = "RVs: ${clientDetails.rvCount}, Work Orders: ${clientDetails.workOrderCount}"
        }
    }
}