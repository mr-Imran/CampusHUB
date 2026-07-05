package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "opportunities")
data class Opportunity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String, // "Event", "Competition", "Ambassador"
    val organization: String,
    val dateTime: Long, // Epoch timestamp in ms
    val location: String, // "Virtual" or physical address
    val deadline: Long, // Registration deadline epoch timestamp
    val contactEmail: String,
    val requirements: String, // Comma-separated requirements
    val tags: String, // Comma-separated interests / tags
    val posterEmail: String, // Organizers email
    val imageUrl: String? = null
) : Serializable

@Entity(tableName = "registrations")
data class Registration(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val opportunityId: Int,
    val studentName: String,
    val studentEmail: String,
    val studentUniversity: String,
    val studentMajor: String,
    val gradYear: String,
    val registeredAt: Long = System.currentTimeMillis(),
    val status: String = "Registered" // "Registered", "Approved", "Waitlisted"
) : Serializable

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
) : Serializable

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val email: String,
    val name: String,
    val university: String,
    val major: String,
    val selectedInterests: String // Comma-separated tags, e.g., "Coding,Design,Leadership"
) : Serializable
