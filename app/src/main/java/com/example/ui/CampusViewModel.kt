package com.example.ui

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.MainActivity
import com.example.data.model.*
import com.example.data.repository.CampusRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CampusViewModel(
    application: Application,
    private val repository: CampusRepository
) : AndroidViewModel(application) {

    // Global UI Settings & States
    val isOrganizerMode = MutableStateFlow(false)
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All") // "All", "Event", "Competition", "Ambassador"
    val selectedTagFilter = MutableStateFlow<String?>(null)

    // Repository Flows
    val userProfile = repository.userProfileFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val allOpportunities = repository.allOpportunities.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allNotifications = repository.allNotifications.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allRegistrations = repository.getAllRegistrations().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered Opportunities Flow
    val filteredOpportunities = combine(
        allOpportunities,
        searchQuery,
        selectedCategory,
        selectedTagFilter
    ) { ops, query, category, tag ->
        ops.filter { op ->
            val matchesQuery = op.title.contains(query, ignoreCase = true) ||
                    op.description.contains(query, ignoreCase = true) ||
                    op.organization.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || op.category.equals(category, ignoreCase = true)
            val matchesTag = tag == null || op.tags.split(",").map { it.trim() }.contains(tag)
            matchesQuery && matchesCategory && matchesTag
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Personalized Recommendations Flow (Calculated based on User Profile Interests)
    val recommendedOpportunities = combine(
        allOpportunities,
        userProfile
    ) { ops, profile ->
        if (profile == null || profile.selectedInterests.isBlank()) {
            emptyList()
        } else {
            val userInterests = profile.selectedInterests.split(",").map { it.trim().lowercase() }.toSet()
            ops.map { op ->
                val opTags = op.tags.split(",").map { it.trim().lowercase() }.toSet()
                val score = opTags.intersect(userInterests).size
                Pair(op, score)
            }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { it.first }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Registrations of the current logged-in user (student)
    val studentRegistrations = combine(
        allRegistrations,
        userProfile
    ) { registrations, profile ->
        if (profile == null) {
            emptyList()
        } else {
            registrations.filter { it.studentEmail.equals(profile.email, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
        createNotificationChannel()
    }

    // --- Profile Operations ---
    fun saveProfile(name: String, email: String, university: String, major: String, interests: List<String>) {
        viewModelScope.launch {
            val profile = UserProfile(
                email = email.trim(),
                name = name.trim(),
                university = university.trim(),
                major = major.trim(),
                selectedInterests = interests.joinToString(",")
            )
            repository.saveProfile(profile)
            repository.sendSystemNotification(
                title = "Profile Configured! 🎯",
                message = "Welcome, ${profile.name}! Your interest dashboard is now set up for customized recommendation updates."
            )
        }
    }

    // --- Opportunity Operations ---
    fun createOpportunity(
        title: String,
        description: String,
        category: String,
        organization: String,
        dateTime: Long,
        location: String,
        deadline: Long,
        contactEmail: String,
        requirements: String,
        tags: List<String>
    ) {
        viewModelScope.launch {
            val profile = userProfile.value
            val posterEmail = profile?.email ?: "organizer@university.edu"
            val op = Opportunity(
                title = title.trim(),
                description = description.trim(),
                category = category,
                organization = organization.trim(),
                dateTime = dateTime,
                location = location.trim(),
                deadline = deadline,
                contactEmail = contactEmail.trim(),
                requirements = requirements.trim(),
                tags = tags.joinToString(","),
                posterEmail = posterEmail
            )
            repository.createOpportunity(op)
            triggerSystemPushNotification(
                title = "New Opportunity Live! 📢",
                message = "${op.organization} posted a new ${op.category}: \"${op.title}\"."
            )
        }
    }

    fun deleteOpportunity(opportunity: Opportunity) {
        viewModelScope.launch {
            repository.deleteOpportunity(opportunity)
        }
    }

    // --- Registration Operations ---
    fun registerForOpportunity(opportunity: Opportunity, name: String, email: String, university: String, major: String, gradYear: String) {
        viewModelScope.launch {
            val reg = Registration(
                opportunityId = opportunity.id,
                studentName = name.trim(),
                studentEmail = email.trim(),
                studentUniversity = university.trim(),
                studentMajor = major.trim(),
                gradYear = gradYear.trim(),
                status = "Registered"
            )
            repository.registerForOpportunity(reg, opportunity.title)
            triggerSystemPushNotification(
                title = "Registered Successfully! 🎉",
                message = "You are registered for \"${opportunity.title}\". We've notified the organizers!"
            )
        }
    }

    fun updateRegistrationStatus(registration: Registration, status: String, opportunityTitle: String) {
        viewModelScope.launch {
            repository.updateRegistrationStatus(
                registrationId = registration.id,
                status = status,
                studentEmail = registration.studentEmail,
                opportunityTitle = opportunityTitle
            )
            triggerSystemPushNotification(
                title = "Opportunity Status Update ✉️",
                message = "Your registration for \"$opportunityTitle\" is now: $status."
            )
        }
    }

    fun cancelRegistration(registrationId: Int) {
        viewModelScope.launch {
            repository.deleteRegistration(registrationId)
        }
    }

    // --- Notification Operations ---
    fun markNotificationsAsRead() {
        viewModelScope.launch {
            repository.markNotificationsAsRead()
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            repository.clearNotifications()
        }
    }

    // --- Push Notifications Engine (Simulated Real Notifications) ---
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "CampusHub Notifications"
            val descriptionText = "Triggers alerts when student events or competitions register changes"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("campushub_alerts", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getApplication<Application>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun triggerSystemPushNotification(title: String, message: String) {
        val context = getApplication<Application>()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, "campushub_alerts")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), builder.build())

        viewModelScope.launch {
            repository.sendSystemNotification(title, message)
        }
    }

    fun simulateUpcomingDeadlineAlert() {
        viewModelScope.launch {
            val ops = allOpportunities.value
            val firstOp = ops.firstOrNull()
            if (firstOp != null) {
                triggerSystemPushNotification(
                    title = "Deadline Warning! ⏳",
                    message = "The registration window for \"${firstOp.title}\" by ${firstOp.organization} is closing soon. Don't miss out!"
                )
            } else {
                triggerSystemPushNotification(
                    title = "Upcoming Opportunities! ⚡",
                    message = "Check out the latest campus roles and workshops on CampusHub and boost your resume."
                )
            }
        }
    }
}

class CampusViewModelFactory(
    private val application: Application,
    private val repository: CampusRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CampusViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CampusViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
