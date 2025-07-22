package com.avantgarderv.data

// Singleton acting as a centralized in-memory database.
object RVDatabase {

    private val rvs = mutableListOf<RV>()
    private val clients = mutableListOf<Client>()

    init {
        // Pre-populate with dummy data for demonstration
        rvs.add(RV("1ABC123XYZ789", "Forest River", "Sunseeker", 2023, 120000.0, 5000, "In Stock", "A beautiful Class C motorhome, perfect for family trips. Well-maintained and recently serviced."))
        rvs.add(RV("2DEF456ABC123", "Winnebago", "Vista", 2022, 95000.0, 12000, "In Stock", "Spacious and comfortable Class A with all the amenities for a long-haul journey."))
        rvs.add(RV("3GHI789DEF456", "Thor Motor Coach", "Majestic", 2024, 155000.0, 500, "Sold", "Top-of-the-line model with luxury finishes throughout. A true home on wheels."))
        rvs.add(RV("4JKL012GHI789", "Jayco", "Redhawk", 2021, 89000.0, 25000, "In Service", "Reliable and easy to drive, this RV has seen many adventures and is ready for more."))

        clients.add(Client("C001", "John", "Doe", "555-1234"))
        clients.add(Client("C002", "Jane", "Smith", "555-5678"))
    }

    fun getAllRVs(): List<RV> {
        return rvs
    }

    fun findRVByVin(vin: String): RV? {
        return rvs.find { it.vin.equals(vin, ignoreCase = true) }
    }

    fun addOrUpdateRV(rv: RV) {
        val existingRv = findRVByVin(rv.vin)
        if (existingRv != null) {
            // Update existing
            existingRv.make = rv.make
            existingRv.model = rv.model
            existingRv.year = rv.year
            existingRv.price = rv.price
            existingRv.mileage = rv.mileage
            existingRv.status = rv.status
            existingRv.description = rv.description
            // Note: Media URIs are managed directly on the object in RVDetailActivity
        } else {
            // Add new
            rvs.add(rv)
        }
    }

    fun getAllClients(): List<Client> {
        return clients
    }

    fun findClientById(id: String): Client? {
        return clients.find { it.id == id }
    }

    fun addOrUpdateClient(client: Client) {
        val existingClient = findClientById(client.id)
        if (existingClient != null) {
            existingClient.firstName = client.firstName
            existingClient.lastName = client.lastName
            existingClient.phoneNumber = client.phoneNumber
        } else {
            clients.add(client)
        }
    }
}