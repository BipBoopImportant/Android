package com.avantgarderv.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.avantgarderv.R
import com.avantgarderv.data.*
import com.google.android.material.chip.Chip
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// Adapter for the list of Work Orders on the RV Detail screen
class WorkOrderAdapter(private val onClick: (WorkOrder) -> Unit) :
    RecyclerView.Adapter<WorkOrderAdapter.WorkOrderViewHolder>() {

    private var workOrders: List<WorkOrder> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkOrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_work_order, parent, false)
        return WorkOrderViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: WorkOrderViewHolder, position: Int) {
        holder.bind(workOrders[position])
    }

    override fun getItemCount(): Int = workOrders.size

    fun submitList(newList: List<WorkOrder>) {
        workOrders = newList
        notifyDataSetChanged()
    }

    class WorkOrderViewHolder(itemView: View, val onClick: (WorkOrder) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.woTitleTextView)
        private val date: TextView = itemView.findViewById(R.id.woDateTextView)
        private val statusChip: Chip = itemView.findViewById(R.id.woStatusChip)
        private var currentWorkOrder: WorkOrder? = null

        init { itemView.setOnClickListener { currentWorkOrder?.let { onClick(it) } } }

        fun bind(workOrder: WorkOrder) {
            currentWorkOrder = workOrder
            title.text = workOrder.title
            date.text = "Created: ${SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(workOrder.createdDate))}"

            val overallStatus = getOverallStatus(workOrder.items)
            statusChip.text = overallStatus.name.replace("_", " ")
            statusChip.setChipBackgroundColorResource(getStatusColor(overallStatus))
        }

        private fun getOverallStatus(items: List<WorkOrderItem>): WorkOrderStatus {
            if (items.any { it.status == WorkOrderStatus.IN_PROGRESS }) return WorkOrderStatus.IN_PROGRESS
            if (items.any { it.status == WorkOrderStatus.BLOCKED }) return WorkOrderStatus.BLOCKED
            if (items.any { it.status == WorkOrderStatus.PENDING }) return WorkOrderStatus.PENDING
            return if (items.isNotEmpty()) WorkOrderStatus.COMPLETED else WorkOrderStatus.PENDING
        }

        private fun getStatusColor(status: WorkOrderStatus): Int = when (status) {
            WorkOrderStatus.PENDING -> R.color.status_pending
            WorkOrderStatus.IN_PROGRESS -> R.color.status_in_progress
            WorkOrderStatus.BLOCKED -> R.color.status_blocked
            WorkOrderStatus.COMPLETED -> R.color.status_completed
        }
    }
}

// Adapter for the list of Tasks inside the Work Order Detail screen
class WorkOrderItemAdapter(
    private val context: Context,
    private val onEdit: (WorkOrderItem) -> Unit,
    private val onDelete: (WorkOrderItem) -> Unit,
    private val onStatusChange: (WorkOrderItem, WorkOrderStatus) -> Unit
) : RecyclerView.Adapter<WorkOrderItemAdapter.TaskViewHolder>() {

    private var items: List<WorkOrderItem> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_work_order_task, parent, false)
        return TaskViewHolder(view, onEdit, onDelete, onStatusChange)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(context, items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newList: List<WorkOrderItem>) {
        items = newList
        notifyDataSetChanged()
    }

    class TaskViewHolder(
        itemView: View,
        val onEdit: (WorkOrderItem) -> Unit,
        val onDelete: (WorkOrderItem) -> Unit,
        val onStatusChange: (WorkOrderItem, WorkOrderStatus) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val description: TextView = itemView.findViewById(R.id.taskDescriptionTextView)
        private val technician: TextView = itemView.findViewById(R.id.taskTechnicianTextView)
        private val notes: TextView = itemView.findViewById(R.id.taskNotesTextView)
        private val statusSpinner: Spinner = itemView.findViewById(R.id.taskStatusSpinner)
        private val editButton: ImageButton = itemView.findViewById(R.id.editTaskButton)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteTaskButton)
        private var currentItem: WorkOrderItem? = null
        private var isBinding = false

        fun bind(context: Context, item: WorkOrderItem) {
            isBinding = true
            currentItem = item
            description.text = item.description
            technician.text = "Tech: ${item.technician ?: "Unassigned"}"
            technician.visibility = if (item.technician != null) View.VISIBLE else View.GONE
            notes.text = "Notes: ${item.notes ?: "None"}"
            notes.visibility = if (item.notes != null) View.VISIBLE else View.GONE

            editButton.setOnClickListener { currentItem?.let { onEdit(it) } }
            deleteButton.setOnClickListener { currentItem?.let { onDelete(it) } }

            val statusAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, WorkOrderStatus.values().map { it.name.replace("_", " ") })
            statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            statusSpinner.adapter = statusAdapter
            statusSpinner.setSelection(item.status.ordinal, false)

            statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (!isBinding) {
                        currentItem?.let {
                            val newStatus = WorkOrderStatus.values()[position]
                            onStatusChange(it, newStatus)
                        }
                    }
                    isBinding = false
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }
}

// Adapter to show parts used in the task dialog
class PartUsageAdapter(
    private val onDelete: (PartUsage) -> Unit
) : RecyclerView.Adapter<PartUsageAdapter.PartUsageViewHolder>() {
    private var parts: List<PartUsage> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartUsageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_part_usage, parent, false)
        return PartUsageViewHolder(view, onDelete)
    }

    override fun onBindViewHolder(holder: PartUsageViewHolder, position: Int) {
        holder.bind(parts[position])
    }

    override fun getItemCount(): Int = parts.size

    fun submitList(newList: List<PartUsage>) {
        parts = newList
        notifyDataSetChanged()
    }

    class PartUsageViewHolder(itemView: View, val onDelete: (PartUsage) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val description: TextView = itemView.findViewById(R.id.partUsageDescription)
        private val quantity: TextView = itemView.findViewById(R.id.partUsageQuantity)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deletePartUsageButton)
        private var currentPartUsage: PartUsage? = null

        fun bind(partUsage: PartUsage) {
            currentPartUsage = partUsage
            val partInfo = RVDatabase.findPartByNumber(partUsage.partNumber)
            description.text = "${partUsage.partNumber}: ${partInfo?.description ?: "Unknown Part"}"
            quantity.text = "Qty: ${partUsage.quantityUsed}"
            deleteButton.setOnClickListener { currentPartUsage?.let { onDelete(it) } }
        }
    }
}

// Adapter for the part selection dialog
class PartSelectionAdapter(
    private val onSelect: (Part) -> Unit
) : RecyclerView.Adapter<PartSelectionAdapter.PartSelectionViewHolder>() {
    private var parts: List<Part> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartSelectionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_part, parent, false)
        return PartSelectionViewHolder(view, onSelect)
    }

    override fun onBindViewHolder(holder: PartSelectionViewHolder, position: Int) {
        holder.bind(parts[position])
    }

    override fun getItemCount(): Int = parts.size

    fun submitList(newList: List<Part>) {
        parts = newList
        notifyDataSetChanged()
    }

    class PartSelectionViewHolder(itemView: View, val onSelect: (Part) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val description: TextView = itemView.findViewById(R.id.partDescriptionTextView)
        private val partNumber: TextView = itemView.findViewById(R.id.partNumberTextView)
        private val quantity: TextView = itemView.findViewById(R.id.partQuantityTextView)
        private val price: TextView = itemView.findViewById(R.id.partPriceTextView)
        private var currentPart: Part? = null

        init {
            itemView.setOnClickListener { currentPart?.let { onSelect(it) } }
        }

        fun bind(part: Part) {
            currentPart = part
            description.text = part.description
            partNumber.text = "PN: ${part.partNumber}"
            quantity.text = "In Stock: ${part.inStockQuantity}"
            price.text = NumberFormat.getCurrencyInstance(Locale.US).format(part.price)
            (itemView.findViewById(R.id.partCategoryTextView) as TextView).visibility = View.GONE
        }
    }
}