package com.example.data.db

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OpportunityDao {
    @Query("SELECT * FROM opportunities ORDER BY dateTime ASC")
    fun getAll(): Flow<List<Opportunity>>

    @Query("SELECT * FROM opportunities WHERE id = :id")
    fun getById(id: Int): Flow<Opportunity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(opportunity: Opportunity): Long

    @Delete
    suspend fun delete(opportunity: Opportunity)

    @Query("SELECT * FROM opportunities WHERE posterEmail = :email ORDER BY id DESC")
    fun getByPoster(email: String): Flow<List<Opportunity>>
}

@Dao
interface RegistrationDao {
    @Query("SELECT * FROM registrations WHERE opportunityId = :opportunityId ORDER BY registeredAt DESC")
    fun getByOpportunity(opportunityId: Int): Flow<List<Registration>>

    @Query("SELECT * FROM registrations WHERE studentEmail = :email ORDER BY registeredAt DESC")
    fun getByStudent(email: String): Flow<List<Registration>>

    @Query("SELECT * FROM registrations ORDER BY registeredAt DESC")
    fun getAll(): Flow<List<Registration>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(registration: Registration): Long

    @Query("UPDATE registrations SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)

    @Query("DELETE FROM registrations WHERE id = :id")
    suspend fun delete(id: Int)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAll(): Flow<List<Notification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: Notification)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notifications")
    suspend fun clearAll()
}

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profiles LIMIT 1")
    fun getProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles LIMIT 1")
    suspend fun getProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)
}
