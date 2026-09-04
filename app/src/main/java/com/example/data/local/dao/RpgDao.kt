package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RpgDao {

    // Character
    @Query("SELECT * FROM rpg_character WHERE id = 1")
    fun getCharacterFlow(): Flow<CharacterRpgEntity?>

    @Query("SELECT * FROM rpg_character WHERE id = 1")
    suspend fun getCharacter(): CharacterRpgEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCharacter(character: CharacterRpgEntity)

    // Equipment Items
    @Query("SELECT * FROM rpg_equipment_items ORDER BY requiredLevel ASC, xpStoreCost ASC")
    fun getAllEquipmentItems(): Flow<List<EquipmentItemEntity>>

    @Query("SELECT * FROM rpg_equipment_items WHERE id = :itemId")
    suspend fun getEquipmentItemById(itemId: String): EquipmentItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipmentItems(items: List<EquipmentItemEntity>)

    @Update
    suspend fun updateEquipmentItem(item: EquipmentItemEntity)

    // Companions
    @Query("SELECT * FROM rpg_companions ORDER BY requiredLevel ASC")
    fun getAllCompanions(): Flow<List<CompanionEntity>>

    @Query("SELECT * FROM rpg_companions WHERE id = :companionId")
    suspend fun getCompanionById(companionId: String): CompanionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompanions(companions: List<CompanionEntity>)

    @Update
    suspend fun updateCompanion(companion: CompanionEntity)

    // Skills
    @Query("SELECT * FROM rpg_skills ORDER BY branch ASC, tier ASC")
    fun getAllSkills(): Flow<List<SkillNodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkills(skills: List<SkillNodeEntity>)

    @Update
    suspend fun updateSkill(skill: SkillNodeEntity)

    // Quests
    @Query("SELECT * FROM rpg_quests ORDER BY isCompleted ASC, isWeekly ASC")
    fun getAllQuests(): Flow<List<RpgQuestEntity>>

    @Query("SELECT * FROM rpg_quests WHERE id = :questId")
    suspend fun getQuestById(questId: String): RpgQuestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuests(quests: List<RpgQuestEntity>)

    @Update
    suspend fun updateQuest(quest: RpgQuestEntity)

    // Bosses
    @Query("SELECT * FROM rpg_bosses ORDER BY isDefeated ASC, id DESC")
    fun getAllBosses(): Flow<List<BossChallengeEntity>>

    @Query("SELECT * FROM rpg_bosses WHERE id = :bossId")
    suspend fun getBossById(bossId: Long): BossChallengeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoss(boss: BossChallengeEntity): Long

    @Update
    suspend fun updateBoss(boss: BossChallengeEntity)

    @Delete
    suspend fun deleteBoss(boss: BossChallengeEntity)
}
