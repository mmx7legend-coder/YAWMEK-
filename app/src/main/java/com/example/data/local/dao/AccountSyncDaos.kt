package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts WHERE isLoggedIn = 1 LIMIT 1")
    fun getActiveAccountFlow(): Flow<UserAccountEntity?>

    @Query("SELECT * FROM user_accounts WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getActiveAccount(): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE email = :email LIMIT 1")
    suspend fun getAccountByEmail(email: String): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAccount(account: UserAccountEntity)

    @Query("UPDATE user_accounts SET isLoggedIn = 0")
    suspend fun markAllLoggedOut()

    @Query("DELETE FROM user_accounts WHERE id = :accountId")
    suspend fun deleteAccountById(accountId: String)

    @Query("DELETE FROM user_accounts")
    suspend fun clearAccounts()
}

@Dao
interface SyncMetadataDao {
    @Query("SELECT * FROM sync_metadata")
    fun getAllMetadataFlow(): Flow<List<SyncMetadataEntity>>

    @Query("SELECT * FROM sync_metadata WHERE syncStatus = 'PENDING'")
    suspend fun getPendingSyncItems(): List<SyncMetadataEntity>

    @Query("SELECT * FROM sync_metadata WHERE entityCompositeKey = :key LIMIT 1")
    suspend fun getMetadataByKey(key: String): SyncMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMetadata(metadata: SyncMetadataEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(metadataList: List<SyncMetadataEntity>)

    @Query("UPDATE sync_metadata SET syncStatus = :status WHERE entityCompositeKey = :key")
    suspend fun updateStatus(key: String, status: SyncStatus)

    @Query("DELETE FROM sync_metadata WHERE entityCompositeKey = :key")
    suspend fun deleteMetadataByKey(key: String)

    @Query("DELETE FROM sync_metadata")
    suspend fun clearAllMetadata()
}

@Dao
interface NotificationPreferencesDao {
    @Query("SELECT * FROM notification_preferences WHERE id = 1 LIMIT 1")
    fun getPreferencesFlow(): Flow<NotificationPreferencesEntity?>

    @Query("SELECT * FROM notification_preferences WHERE id = 1 LIMIT 1")
    suspend fun getPreferences(): NotificationPreferencesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePreferences(preferences: NotificationPreferencesEntity)
}

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY startedAtMillis DESC")
    fun getAllSessionsFlow(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE startedAtMillis >= :startMillis ORDER BY startedAtMillis DESC")
    fun getSessionsSince(startMillis: Long): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Delete
    suspend fun deleteSession(session: FocusSessionEntity)

    @Query("DELETE FROM focus_sessions")
    suspend fun clearAllSessions()
}
