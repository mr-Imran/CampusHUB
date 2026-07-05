package com.example.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        Opportunity::class,
        Registration::class,
        Notification::class,
        UserProfile::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun opportunityDao(): OpportunityDao
    abstract fun registrationDao(): RegistrationDao
    abstract fun notificationDao(): NotificationDao
    abstract fun userDao(): UserDao
}
