package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.model.CommunityBattleEntity
import com.example.data.local.model.CommunityFriendEntity
import com.example.data.local.model.CommunityProfileEntity
import com.example.data.local.model.FriendshipStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface CommunityDao {

    // Profile
    @Query("SELECT * FROM community_profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<CommunityProfileEntity?>

    @Query("SELECT * FROM community_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): CommunityProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: CommunityProfileEntity)

    // Friends
    @Query("SELECT * FROM community_friends ORDER BY isOnline DESC, level DESC")
    fun getAllFriendsFlow(): Flow<List<CommunityFriendEntity>>

    @Query("SELECT * FROM community_friends WHERE status = :status ORDER BY isOnline DESC, level DESC")
    fun getFriendsByStatusFlow(status: FriendshipStatus): Flow<List<CommunityFriendEntity>>

    @Query("SELECT * FROM community_friends WHERE friendUsername = :username LIMIT 1")
    suspend fun getFriendByUsername(username: String): CommunityFriendEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: CommunityFriendEntity): Long

    @Update
    suspend fun updateFriend(friend: CommunityFriendEntity)

    @Delete
    suspend fun deleteFriend(friend: CommunityFriendEntity)

    // Battles
    @Query("SELECT * FROM community_battles ORDER BY timestampMillis DESC")
    fun getAllBattlesFlow(): Flow<List<CommunityBattleEntity>>

    @Query("SELECT * FROM community_battles ORDER BY timestampMillis DESC LIMIT :limit")
    suspend fun getRecentBattles(limit: Int = 20): List<CommunityBattleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBattle(battle: CommunityBattleEntity): Long
}
