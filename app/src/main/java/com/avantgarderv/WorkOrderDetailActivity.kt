package com.avantgarderv

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avantgarderv.adapters.PartSelectionAdapter
import com.avantgarderv.adapters.PartUsageAdapter
import com.avantgarderv.adapters.WorkOrderItemAdapter
import com.avantgarderv.data.*
import com.avantgarderv.databinding.ActivityWorkOrderDetailBinding
import java.util.*

class WorkOrderDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkOrderDetailBinding
    private lateinit var taskAdapter: WorkOrderItemAdapter
    private var currentWorkOrder: WorkOrder? = null
    private var currentVin: String? = null
    private val taskItems = mutableListOf<WorkOrderItem>()
    private val clients = RVDatabase.getAllClients()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupClientSpinner()

        currentVin = intent.getStringExtra("VIN")
        val workOrderId = intent.getStringExtra("WORK_ORDER_ID")

        if (workOrderId != null) {
            currentWorkOrder = RVDatabase.findWorkOrderById(workOrderId)
            title = "Edit Work Order"
            currentWorkOrder?.let { populateFields(it) }
        } else {
            title = "New Work Order"
        }

        if (currentVin == null && currentWorkOrder == null) {
            Toast.makeText(this, "Error: VIN not found.", Toast.LENGTH_LONG).show()
            finish(); return
        }

        binding.addTaskButton.setOnClickListener { showAddEditTaskDialog(null) }
        binding.saveWoButton.setOnClickListener { saveWorkOrder() }
    }
    
    private fun populateFields(workOrder: WorkOrder) {
        binding.editWoTitle.setText(workOrder.title)
        workOrder.clientId?.let { id ->
            val clientPos = clients.indexOfFirst { it.id == id }
            if (clientPos != -1) binding.spinnerClient.setSelection(clientPos + 1)
        }
        taskItems.clear()
        taskItems.addAll(workOrder.items.map { it.copy(partsUsed = it.partsUsed.toMutableList()) }) // Deep copy
        taskAdapter.submitList(taskItems)
    }

    private fun setupClientSpinner() {
        val clientNames = listOf("Unassigned") + clients.map { "${it.firstName} ${it.lastName}" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, clientNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerClient.adapter = adapter
    }

    private fun setupRecyclerView() {
        taskAdapter = WorkOrderItemAdapter(
            context = this,
            onEdit = { showAddEditTaskDialog(it) },
            onDelete = { showDeleteTaskDialog(it) },
            onStatusChange = { item, newStatus ->
                val index = taskItems.indexOfFirst { it.id == item.id }
                if (index != -1) {
                    val oldStatus = taskItems[index].status
                    taskItems[index].status = newStatus
                    // If task is completed, deduct inventory
                    if (newStatus == WorkOrderStatus.COMPLETED && oldStatus != WorkOrderStatus.COMPLETED) {
                        consumePartsForTask(taskItems[index])
                    }
                }
            }
        )
        binding.tasksRecyclerView.adapter = taskAdapter
        binding.tasksRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun consumePartsForTask(item: WorkOrderItem) {
        var allConsumed = true
        item.partsUsed.forEach { partUsage ->
            if (!RVDatabase.consumePartFromInventory(partUsage.partNumber, partUsage.quantityUsed)) {
                allConsumed = false
                Toast.makeText(this, "Failed to consume ${partUsage.quantityUsed} of ${partUsage.partNumber}: Not enough stock.", Toast.LENGTH_LONG).show()
            }
        }
        if (allConsumed && item.partsUsed.isNotEmpty()) {
            Toast.makeText(this, "Inventory updated for task: ${item.description}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showDeleteTaskDialog(item: WorkOrderItem) {
        AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Delete") { _, _ ->
                val index = taskItems.indexOfFirst { it.id == item.id }
                if (index != -1) {
                    taskItems.removeAt(index)
                    taskAdapter.submitList(taskItems)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showAddEditTaskDialog(item: WorkOrderItem?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_task, null)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.editTaskDescription)
        val technicianEditText = dialogView.findViewById<EditText>(R.id.editTaskTechnician)
        val notesEditText = dialogView.findViewById<EditText>(R.id.editTaskNotes)
        val partsUsedRecyclerView = dialogView.findViewById<RecyclerView>(R.id.partsUsedRecyclerView)
        val addPartButton = dialogView.findViewById<android.widget.Button>(R.id.addPartButton)

        val dialogParts = item?.partsUsed?.toMutableList() ?: mutableListOf()
        val partUsageAdapter = PartUsageAdapter { partUsage ->
            dialogParts.remove(partUsage)
            partsUsedRecyclerView.adapter?.notifyDataSetChanged()
        }
        partsUsedRecyclerView.adapter = partUsageAdapter
        partsUsedRecyclerView.layoutManager = LinearLayoutManager(this)
        partUsageAdapter.submitList(dialogParts)
        
        item?.let {
            descriptionEditText.setText(it.description)
            technicianEditText.setText(it.technician)
            notesEditText.setText(it.notes)
        }
        
        addPartButton.setOnClickListener { showPartSelectionDialog(dialogParts, partUsageAdapter) }

        AlertDialog.Builder(this)
            .setTitle(if (item == null) "Add Task" else "Edit Task")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val description = descriptionEditText.text.toString().trim()
                if(description.isEmpty()){
                    Toast.makeText(this, "Description is required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val task = item?.copy(
                    description = description,
                    technician = technicianEditText.text.toString().trim().takeIf { it.isNotEmpty() },
                    notes = notesEditText.text.toString().trim().takeIf { it.isNotEmpty() },
                    partsUsed = dialogParts
                ) ?: WorkOrderItem(
                    id = UUID.randomUUID().toString(),
                    description = description,
                    technician = technicianEditText.text.toString().trim().takeIf { it.isNotEmpty() },
                    notes = notesEditText.text.toString().trim().takeIf { it.isNotEmpty() },
                    partsUsed = dialogParts
                )
                
                val index = taskItems.indexOfFirst { it.id == task.id }
                if (index != -1) taskItems[index] = task else taskItems.add(task)
                taskAdapter.submitList(taskItems)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPartSelectionDialog(dialogParts: MutableList<PartUsage>, partUsageAdapter: PartUsageAdapter) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_part, null)
        val searchView = dialogView.findViewById<SearchView>(R.id.partSearchView)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.partSelectionRecyclerView)
        var allParts = RVDatabase.getAllParts()

        val dialog = AlertDialog.Builder(this).setTitle("Select Part").setView(dialogView).setNegativeButton("Cancel", null).create()

        val adapter = PartSelectionAdapter { part ->
            showQuantityInputDialog(part) { quantity ->
                val existing = dialogParts.find { it.partNumber == part.partNumber }
                if (existing != null) {
                    existing.quantityUsed += quantity
                } else {
                    dialogParts.add(PartUsage(part.partNumber, quantity))
                }
                partUsageAdapter.submitList(dialogParts)
                dialog.dismiss()
            }
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter.submitList(allParts)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                val filtered = if (newText.isNullOrBlank()) allParts else allParts.filter {
                    it.description.contains(newText, true) || it.partNumber.contains(newText, true)
                }
                adapter.submitList(filtered)
                return true
            }
        })
        dialog.show()
    }

    private fun showQuantityInputDialog(part: Part, onResult: (Int) -> Unit) {
        val editText = EditText(this)
        editText.inputType = InputType.TYPE_CLASS_NUMBER
        editText.hint = "Enter quantity"
        AlertDialog.Builder(this)
            .setTitle("Quantity for ${part.partNumber}")
            .setMessage("In Stock: ${part.inStockQuantity}")
            .setView(editText)
            .setPositiveButton("OK") { _, _ ->
                val quantity = editText.text.toString().toIntOrNull() ?: 0
                if (quantity > 0 && quantity <= part.inStockQuantity) {
                    onResult(quantity)
                } else {
                    Toast.makeText(this, "Invalid quantity or not enough stock.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveWorkOrder() {
        val title = binding.editWoTitle.text.toString().trim()
        if (title.isEmpty()) { Toast.makeText(this, "Work Order title cannot be empty.", Toast.LENGTH_SHORT).show(); return }

        val selectedClientId = if (binding.spinnerClient.selectedItemPosition > 0) clients[binding.spinnerClient.selectedItemPosition - 1].id else null

        val woToSave = WorkOrder(
            id = currentWorkOrder?.id ?: UUID.randomUUID().toString(),
            vin = currentWorkOrder?.vin ?: currentVin!!,
            title = title,
            clientId = selectedClientId,
            createdDate = currentWorkOrder?.createdDate ?: Date().time,
            items = taskItems
        )
        RVDatabase.addOrUpdateWorkOrder(woToSave)
        Toast.makeText(this, "Work Order Saved!", Toast.LENGTH_SHORT).show()
        finish()
    }
}