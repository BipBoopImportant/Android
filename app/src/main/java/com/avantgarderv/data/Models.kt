package com.avantgarderv.data

import java.io.Serializable

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
    val videoUris: MutableList<String> = mutableListOf()
) : Serializable

data class Client(
    val id: String,
    var firstName: String,
    var lastName: String,
    var phoneNumber: String
) : Serializable

data class Inspection(
    val id: String,
    val vin: String,
    val date: Long,
    val notes: String
) : Serializable

data class ServiceAppointment(
    val id: String,
    val vin: String,
    val clientId: String,
    val date: Long,
    val reason: String
) : Serializable

data class WorkOrder(
    val id: String,
    val vin: String,
    val issueDescription: String,
    var cost: Double
) : Serializable