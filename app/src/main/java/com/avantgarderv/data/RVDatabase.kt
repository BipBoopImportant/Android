package com.avantgarderv.data

import java.util.Date
import java.util.UUID

// Singleton acting as a centralized in-memory database.
object RVDatabase {

    private val rvs = mutableListOf<RV>()
    private val clients = mutableListOf<Client>()
    private val inspections = mutableListOf<Inspection>()
    private val workOrders = mutableListOf<WorkOrder>()
    private val parts = mutableListOf<Part>()
    private val serviceAppointments = mutableListOf<ServiceAppointment>()

    init {
        // Pre-populate with dummy data
        val johnsCampsites = mutableListOf(CampsiteInfo(UUID.randomUUID().toString(), "Ocean View Resort", "A-12"))
        clients.add(Client("C001", "John", "Doe", "555-1234", "555-4321", "john.doe@email.com", "123 Main St", "Anytown", "CA", "12345", johnsCampsites))
        clients.add(Client("C002", "Jane", "Smith", "555-5678", null, "jane.s@email.com", "456 Oak Ave", "Someville", "NY", "54321", mutableListOf()))
        
        rvs.add(RV("1ABC123XYZ789", "Forest River", "Sunseeker", 2023, 120000.0, 5000, "Sold", "A beautiful Class C motorhome.", ownerClientId = "C001"))
        rvs.add(RV("2DEF456ABC123", "Winnebago", "Vista", 2022, 95000.0, 12000, "In Stock", "Spacious and comfortable Class A."))
        rvs.add(RV("3GHI789DEF456", "Thor Motor Coach", "Majestic", 2024, 155000.0, 500, "Sold", "Top-of-the-line model.", ownerClientId = "C002"))
        rvs.add(RV("4JKL012GHI789", "Jayco", "Redhawk", 2021, 89000.0, 25000, "In Service", "Reliable and easy to drive.", ownerClientId = "C002"))

        val inspectionItems = mutableListOf(InspectionItem("ITEM001", "Tear in awning", "Exterior", mutableListOf(), mutableListOf()))
        inspections.add(Inspection("INSP001", "1ABC123XYZ789", "6-Month Check", "Warranty", Date().time, inspectionItems))

        val woItems = mutableListOf(
            WorkOrderItem("WOI001", "Diagnose refrigerator failure", "Alice", null, WorkOrderStatus.COMPLETED),
            WorkOrderItem("WOI003", "Replace faulty slide-out motor", "Bob", "Motor is seized.", WorkOrderStatus.IN_PROGRESS)
        )
        workOrders.add(WorkOrder("WO001", "4JKL012GHI789", "Fridge and Slide-out Repair", "C002", Date().time, woItems))
        workOrders.add(WorkOrder("WO002", "1ABC123XYZ789", "Awning Repair", "C001", Date().time, mutableListOf()))
        
        parts.add(Part("PL-001", "1/2-inch PEX Connector Fitting", "Plumbing", 50, 2.50, 0.75, "AquaFlow"))
        parts.add(Part("EL-105", "15 Amp Breaker Switch", "Electrical", 25, 12.00, 4.50, "SparkSafe"))

        serviceAppointments.add(ServiceAppointment("SA001", "3GHI789DEF456", "C002", Date().time, "Winterization Prep"))
    }

    // RV Methods
    fun getAllRVs(): List<RV> = rvs
    fun findRVByVin(vin: String): RV? = rvs.find { it.vin.equals(vin, ignoreCase = true) }
    fun findRVsByClientId(clientId: String): List<RV> = rvs.filter { it.ownerClientId == clientId }
    fun addOrUpdateRV(rv: RV) {
        val i = rvs.indexOfFirst { it.vin.equals(rv.vin, ignoreCase = true) }
        if (i != -1) { rvs[i] = rv } else { rvs.add(rv) }
    }

    // Client Methods
    fun getAllClients(): List<Client> = clients
    fun findClientById(id: String): Client? = clients.find { it.id == id }
    fun findClientByVin(vin: String): Client? {
        val rv = findRVByVin(vin)
        return rv?.ownerClientId?.let { findClientById(it) }
    }
    fun addOrUpdateClient(client: Client) {
        val i = clients.indexOfFirst { it.id == client.id }
        if (i != -1) { clients[i] = client } else { clients.add(client) }
    }
    
    // Inspection Methods
    fun getInspectionsByVin(vin: String): List<Inspection> = inspections.filter { it.vin.equals(vin, ignoreCase = true) }
    fun findInspectionById(id: String): Inspection? = inspections.find { it.id == id }
    fun addOrUpdateInspection(insp: Inspection) {
        val i = inspections.indexOfFirst { it.id == insp.id }
        if (i != -1) { inspections[i] = insp } else { inspections.add(insp) }
    }
    
    // Work Order Methods
    fun getWorkOrdersByVin(vin: String): List<WorkOrder> = workOrders.filter { it.vin.equals(vin, ignoreCase = true) }
    fun findWorkOrdersByClientId(clientId: String): List<WorkOrder> = workOrders.filter { it.clientId == clientId }
    fun findWorkOrderById(id: String): WorkOrder? = workOrders.find { it.id == id }
    fun addOrUpdateWorkOrder(wo: WorkOrder) {
        val i = workOrders.indexOfFirst { it.id == wo.id }
        if (i != -1) { workOrders[i] = wo } else { workOrders.add(wo) }
    }
    
    // Part Methods
    fun getAllParts(): List<Part> = parts
    fun findPartByNumber(partNumber: String): Part? = parts.find { it.partNumber.equals(partNumber, ignoreCase = true) }
    fun addOrUpdatePart(part: Part) {
        val i = parts.indexOfFirst { it.partNumber.equals(part.partNumber, ignoreCase = true) }
        if (i != -1) { parts[i] = part } else { parts.add(part) }
    }
    fun consumePartFromInventory(partNumber: String, quantityToConsume: Int): Boolean {
        findPartByNumber(partNumber)?.let { part ->
            if (part.inStockQuantity >= quantityToConsume) {
                part.inStockQuantity -= quantityToConsume
                return true
            }
        }
        return false
    }

    // Service Appointment Methods
    fun findServiceAppointmentsByClientId(clientId: String): List<ServiceAppointment> = serviceAppointments.filter { it.clientId == clientId }
}