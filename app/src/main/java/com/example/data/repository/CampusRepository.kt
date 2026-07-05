package com.example.data.repository

import com.example.data.db.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class CampusRepository(
    private val opportunityDao: OpportunityDao,
    private val registrationDao: RegistrationDao,
    private val notificationDao: NotificationDao,
    private val userDao: UserDao
) {
    val allOpportunities: Flow<List<Opportunity>> = opportunityDao.getAll()
    val allNotifications: Flow<List<Notification>> = notificationDao.getAll()
    val userProfileFlow: Flow<UserProfile?> = userDao.getProfileFlow()

    fun getOpportunityById(id: Int): Flow<Opportunity?> = opportunityDao.getById(id)

    fun getRegistrationsForOpportunity(opportunityId: Int): Flow<List<Registration>> =
        registrationDao.getByOpportunity(opportunityId)

    fun getRegistrationsForStudent(email: String): Flow<List<Registration>> =
        registrationDao.getByStudent(email)

    fun getAllRegistrations(): Flow<List<Registration>> = registrationDao.getAll()

    suspend fun getProfile(): UserProfile? = userDao.getProfile()

    suspend fun saveProfile(profile: UserProfile) {
        userDao.insertProfile(profile)
    }

    suspend fun createOpportunity(opportunity: Opportunity): Long {
        val id = opportunityDao.insert(opportunity)
        // Auto-generate notification for the community
        val categoryText = when (opportunity.category) {
            "Event" -> "New event"
            "Competition" -> "New competition"
            "Ambassador" -> "New ambassador program"
            else -> "New opportunity"
        }
        notificationDao.insert(
            Notification(
                title = "$categoryText Added!",
                message = "\"${opportunity.title}\" has been posted by ${opportunity.organization}. Register before the deadline!"
            )
        )
        return id
    }

    suspend fun registerForOpportunity(registration: Registration, opportunityTitle: String): Long {
        val id = registrationDao.insert(registration)
        // Dispatch local notification
        notificationDao.insert(
            Notification(
                title = "Registration Successful 🎉",
                message = "You have successfully registered for \"$opportunityTitle\". Your status is currently: ${registration.status}."
            )
        )
        return id
    }

    suspend fun updateRegistrationStatus(registrationId: Int, status: String, studentEmail: String, opportunityTitle: String) {
        registrationDao.updateStatus(registrationId, status)
        // Notify the student
        notificationDao.insert(
            Notification(
                title = "Registration Status Updated",
                message = "Your registration status for \"$opportunityTitle\" has been updated to: $status."
            )
        )
    }

    suspend fun deleteRegistration(id: Int) {
        registrationDao.delete(id)
    }

    suspend fun deleteOpportunity(opportunity: Opportunity) {
        opportunityDao.delete(opportunity)
    }

    suspend fun markNotificationsAsRead() {
        notificationDao.markAllAsRead()
    }

    suspend fun clearNotifications() {
        notificationDao.clearAll()
    }

    suspend fun sendSystemNotification(title: String, message: String) {
        notificationDao.insert(Notification(title = title, message = message))
    }

    suspend fun seedInitialDataIfEmpty() {
        val currentOps = allOpportunities.first()
        if (currentOps.isEmpty()) {
            val now = System.currentTimeMillis()
            val dayInMs = TimeUnit.DAYS.toMillis(1)

            val seedOps = listOf(
                Opportunity(
                    title = "Global Inter-University Hackathon 2026",
                    description = "Join over 500+ student developers worldwide to build solutions for real-world sustainability challenges. Top prizes include cash awards and exclusive mentorship programs from tech giants.",
                    category = "Competition",
                    organization = "Campus Developers Club",
                    dateTime = now + (15 * dayInMs),
                    location = "Main Engineering Auditorium",
                    deadline = now + (10 * dayInMs),
                    contactEmail = "hackathon@university.edu",
                    requirements = "Enrolled students, Team size of 2-4 members, Knowledge of basic coding / design",
                    tags = "Coding,Tech,Design",
                    posterEmail = "club-head@university.edu"
                ),
                Opportunity(
                    title = "Google Developer Student Clubs (GDSC) - Campus Lead",
                    description = "Looking for passionate leaders to start and grow developers communities on their campuses. As a GDSC Lead, you will host workshops, build cool projects, and connect students with Google's developer network.",
                    category = "Ambassador",
                    organization = "Google Developers",
                    dateTime = now + (30 * dayInMs),
                    location = "Virtual / Global",
                    deadline = now + (20 * dayInMs),
                    contactEmail = "gdsc-support@google.com",
                    requirements = "Passionate about technology, 1+ years left in university, Leadership experience is a plus",
                    tags = "Leadership,Tech,Marketing",
                    posterEmail = "lead-recruiter@google.com"
                ),
                Opportunity(
                    title = "National Business Plan & Pitch Competition",
                    description = "Pitch your startup idea to a panel of venture capitalists and angel investors. Win up to $10,000 in seed funding and a fast-track entry into the University Startup Incubator.",
                    category = "Competition",
                    organization = "School of Business",
                    dateTime = now + (25 * dayInMs),
                    location = "Business Center Building, 4th Floor",
                    deadline = now + (18 * dayInMs),
                    contactEmail = "pitch@university.edu",
                    requirements = "Submit a 5-page business plan draft in PDF, Open to all departments",
                    tags = "Business,Public Speaking",
                    posterEmail = "dean-business@university.edu"
                ),
                Opportunity(
                    title = "AI in Action: Hands-On Seminar",
                    description = "A practical seminar featuring engineering leads discussing real-world deployments of large language models and computer vision. Pizza and networking session included!",
                    category = "Event",
                    organization = "ACM Student Chapter",
                    dateTime = now + (4 * dayInMs),
                    location = "Seminar Hall C",
                    deadline = now + (3 * dayInMs),
                    contactEmail = "acm@university.edu",
                    requirements = "Open to all, bring your curiosity!",
                    tags = "Tech,Coding,Leadership",
                    posterEmail = "acm-treasurer@university.edu"
                ),
                Opportunity(
                    title = "GitHub Campus Ambassador Program",
                    description = "Empower your peers by bringing GitHub Education tools and resources to your campus. Host local developer events, represent GitHub on your campus, and unlock special benefits.",
                    category = "Ambassador",
                    organization = "GitHub Education",
                    dateTime = now + (40 * dayInMs),
                    location = "Virtual",
                    deadline = now + (35 * dayInMs),
                    contactEmail = "education@github.com",
                    requirements = "Active GitHub account, passion for open source, community organizer mindset",
                    tags = "Tech,Leadership,Marketing",
                    posterEmail = "github-edu@github.com"
                )
            )

            for (op in seedOps) {
                opportunityDao.insert(op)
            }

            // Insert initial greeting notification
            notificationDao.insert(
                Notification(
                    title = "Welcome to CampusHub! 🎒",
                    message = "Discover, register, and coordinate Events, Competitions, and Ambassador programs seamlessly. Toggle Organizer mode in the top bar to post your own!"
                )
            )
        }
    }
}
