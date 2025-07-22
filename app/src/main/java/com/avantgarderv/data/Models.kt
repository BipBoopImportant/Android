package com.avantgarderv.data

import java.io.Serializable

// --- CORE DATA MODELS ---

data class RV(
    val vin: String,
    var make: String,
    var model: String,
    var year: Int,
    var price: Double,
    var mileage: Int,
    var status: String = "In Stock",
    var description: String = "",
    val imageUris: MutableList<String> = mutableListOf(),
    val videoUris: MutableList<String> = mutableListOf(),
    var ownerClientId: String? = null // Link RV to a client
) : Serializable

data class CampsiteInfo(
    val id: String,
    var siteName: String,
    var lotNumber: String
) : Serializable

data class Client(
    val id: String,
    var firstName: String,
    var lastName: String,
    var cellPhone: String,
    var landlinePhone: String? = null,
    var email: String? = null,
    var addressStreet: String? = null,
    var addressCity: String? = null,
    var addressState: String? = null,
    var addressZip: String? = null,
    val campsites: MutableList<CampsiteInfo> = mutableListOf()
) : Serializable

/**
 * A data class specifically for displaying client info efficiently in a list.
 * It holds the client object and pre-calculated counts to avoid database lookups in the adapter.
 */
data class ClientWithDetails(
    val client: Client,
    val rvCount: Int,
    val workOrderCount: Int
)

// --- PART MODELS ---

data class Part(
    val partNumber: String, // Unique ID, like a SKU
    var description: String,
    var category: String,
    var inStockQuantity: Int,
    var price: Double,
    var cost: Double,
    var supplier: String? = null
) : Serializable

data class PartUsage(
    val partNumber: String,
    var quantityUsed: Int
) : Serializable

// --- INSPECTION MODELS ---

data class InspectionItem(
    val id: String,
    var defectDescription: String,
    var location: String,
    val imageUris: MutableList<String> = mutableListOf(),
    val videoUris: MutableList<String> = mutableListOf()
) : Serializable

data class Inspection(
    val id: String,
    val vin: String,
    var title: String,
    var inspectionType: String,
    val date: Long,
    val items: MutableList<InspectionItem> = mutableListOf()
) : Serializable

// --- WORK ORDER MODELS ---

enum class WorkOrderStatus {
    PENDING, IN_PROGRESS, BLOCKED, COMPLETED
}

data class WorkOrderItem(
    val id: String,
    var description: String,
    var technician: String? = null,
    var notes: String? = null,
    var status: WorkOrderStatus = WorkOrderStatus.PENDING,
    val partsUsed: MutableList<PartUsage> = mutableListOf()
) : Serializable

data class WorkOrder(
    val id: String,
    val vin: String,
    var title: String,
    var clientId: String? = null,
    val createdDate: Long,
    val items: MutableList<WorkOrderItem> = mutableListOf()
) : Serializable

// --- SERVICE APPOINTMENT MODEL ---

data class ServiceAppointment(
    val id: String,
    val vin: String,
    val clientId: String,
    val date: Long,
    val reason: String
) : Serializable