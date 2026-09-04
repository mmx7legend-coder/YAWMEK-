package com.example.domain.rpg

import com.example.data.local.model.*
import com.example.data.repository.YawmekRepository
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate

data class RpgActionResult(
    val character: CharacterRpgEntity,
    val xpEarned: Long,
    val leveledUp: Boolean,
    val newLevel: Int,
    val unlockedArea: AdventureArea?,
    val dragonEvolution: DragonStage?,
    val summaryMessageAr: String,
    val summaryMessageEn: String
)

class RpgProgressionManager(private val repository: YawmekRepository) {

    suspend fun initializeCatalogIfEmpty() {
        val existingChar = repository.getRpgCharacter()
        if (existingChar == null) {
            repository.saveRpgCharacter(RpgStarterCatalog.getDefaultCharacter())
            repository.saveEquipmentItems(RpgStarterCatalog.getEquipmentCatalog())
            repository.saveCompanions(RpgStarterCatalog.getCompanionCatalog())
            repository.saveSkills(RpgStarterCatalog.getSkillTreeCatalog())
            repository.saveQuests(RpgStarterCatalog.getDailyQuestsCatalog())
        }
    }

    /**
     * Awards real XP, handles slow leveling, stat increases, area progression, and quests
     */
    suspend fun awardProductivityXp(
        actionType: String,
        baseXp: Long,
        statKey: String? = null
    ): RpgActionResult {
        initializeCatalogIfEmpty()
        var char = repository.getRpgCharacter() ?: RpgStarterCatalog.getDefaultCharacter()

        val todayEpochDay = LocalDate.now().toEpochDay()
        val isConsecutive = char.lastActiveDateEpochDay == todayEpochDay - 1
        val newStreak = when {
            char.lastActiveDateEpochDay == todayEpochDay -> char.productiveStreakDays
            isConsecutive -> char.productiveStreakDays + 1
            else -> 1
        }

        var currentXp = char.currentXp + baseXp
        var totalXp = char.totalEarnedXp + baseXp
        var level = char.level
        var skillPoints = char.availableSkillPoints
        var leveledUp = false

        // Stat progression
        var focus = char.statFocus
        var discipline = char.statDiscipline
        var knowledge = char.statKnowledge
        var consistency = char.statConsistency
        var planning = char.statPlanning
        var courage = char.statCourage

        when (statKey) {
            "FOCUS" -> focus += 1
            "DISCIPLINE" -> discipline += 1
            "KNOWLEDGE" -> knowledge += 1
            "CONSISTENCY" -> consistency += 1
            "PLANNING" -> planning += 1
            "COURAGE" -> courage += 1
        }

        var xpNeeded = RpgProgressionCalculator.getXpRequiredForNextLevel(level)
        while (currentXp >= xpNeeded) {
            currentXp -= xpNeeded
            level += 1
            skillPoints += 1
            leveledUp = true
            // Baseline passive boost on level
            focus += 1
            discipline += 1
            consistency += 1
            xpNeeded = RpgProgressionCalculator.getXpRequiredForNextLevel(level)
        }

        // Check areas
        val newArea = RpgProgressionCalculator.computeAdventureArea(level)
        val areaUnlocked = if (newArea.order > char.currentAdventureArea.order) newArea else null

        // Check dragon
        val completedGoalsCount = repository.allGoals.firstOrNull()?.count { it.isCompleted } ?: 0
        val newDragonStage = RpgProgressionCalculator.computeDragonStage(level, completedGoalsCount)
        val dragonEvolved = if (newDragonStage.levelRequired > char.dragonStage.levelRequired) newDragonStage else null

        val updated = char.copy(
            level = level,
            currentXp = currentXp,
            totalEarnedXp = totalXp,
            availableSkillPoints = skillPoints,
            statFocus = focus,
            statDiscipline = discipline,
            statKnowledge = knowledge,
            statConsistency = consistency,
            statPlanning = planning,
            statCourage = courage,
            currentAdventureArea = newArea,
            dragonStage = newDragonStage,
            productiveStreakDays = newStreak,
            lastActiveDateEpochDay = todayEpochDay
        )

        repository.saveRpgCharacter(updated)

        // Progress matching quests
        progressQuestsForAction(actionType)

        val arMsg = if (leveledUp) "ارتقيت إلى المستوى $level! تم منحك نقطة مهارة ⭐" else "+$baseXp نقطة خبرة RPG"
        val enMsg = if (leveledUp) "Level Up! Reached Level $level (+1 Skill Point) ⭐" else "+$baseXp RPG XP"

        return RpgActionResult(
            character = updated,
            xpEarned = baseXp,
            leveledUp = leveledUp,
            newLevel = level,
            unlockedArea = areaUnlocked,
            dragonEvolution = dragonEvolved,
            summaryMessageAr = arMsg,
            summaryMessageEn = enMsg
        )
    }

    private suspend fun progressQuestsForAction(actionType: String) {
        val quests = repository.rpgQuests.firstOrNull() ?: return
        quests.forEach { quest ->
            if (!quest.isCompleted) {
                val matches = when (quest.questType) {
                    RpgQuestType.COMPLETE_IMPORTANT_TASKS -> actionType == "TASK_HIGH"
                    RpgQuestType.FINISH_FOCUS_SESSION -> actionType == "FOCUS"
                    RpgQuestType.COMPLETE_HABITS -> actionType == "HABIT"
                    RpgQuestType.FINISH_TODAY_PLAN -> actionType.startsWith("TASK")
                    RpgQuestType.WIN_BRAIN_GAME -> actionType == "GAME"
                    RpgQuestType.READ_SESSION -> actionType == "READ"
                    RpgQuestType.LOG_DAILY_BUDGET -> actionType == "EXPENSE"
                }
                if (matches) {
                    val nextCount = quest.currentCount + 1
                    val isDone = nextCount >= quest.targetCount
                    repository.updateQuest(quest.copy(currentCount = nextCount, isCompleted = isDone))
                }
            }
        }
    }

    suspend fun claimQuestReward(quest: RpgQuestEntity): Boolean {
        if (!quest.isCompleted || quest.isClaimed) return false
        repository.updateQuest(quest.copy(isClaimed = true))
        awardProductivityXp("QUEST", quest.rewardXp, "CONSISTENCY")
        if (quest.rewardSkillPoints > 0) {
            val char = repository.getRpgCharacter()
            if (char != null) {
                repository.saveRpgCharacter(char.copy(availableSkillPoints = char.availableSkillPoints + quest.rewardSkillPoints))
            }
        }
        return true
    }

    suspend fun purchaseStoreItem(item: EquipmentItemEntity): Boolean {
        val char = repository.getRpgCharacter() ?: return false
        if (item.isPurchased) return true
        if (char.level < item.requiredLevel) return false
        if (char.currentXp < item.xpStoreCost) return false

        // Deduct XP and grant item
        val updatedChar = char.copy(
            currentXp = char.currentXp - item.xpStoreCost,
            statFocus = char.statFocus + item.bonusStatFocus,
            statDiscipline = char.statDiscipline + item.bonusStatDiscipline,
            statKnowledge = char.statKnowledge + item.bonusStatKnowledge,
            statConsistency = char.statConsistency + item.bonusStatConsistency,
            statPlanning = char.statPlanning + item.bonusStatPlanning,
            statCourage = char.statCourage + item.bonusStatCourage
        )
        repository.saveRpgCharacter(updatedChar)
        repository.updateEquipmentItem(item.copy(isPurchased = true))
        return true
    }

    suspend fun equipItem(item: EquipmentItemEntity) {
        val char = repository.getRpgCharacter() ?: return
        val updated = when (item.slot) {
            EquipmentSlot.WEAPON -> char.copy(equippedWeaponId = item.id)
            EquipmentSlot.ARMOR -> char.copy(equippedArmorId = item.id)
            EquipmentSlot.HELMET -> char.copy(equippedHelmetId = item.id)
            EquipmentSlot.GLOVES -> char.copy(equippedGlovesId = item.id)
            EquipmentSlot.BOOTS -> char.copy(equippedBootsId = item.id)
            EquipmentSlot.CAPE -> char.copy(equippedCapeId = item.id)
            EquipmentSlot.ACCESSORY -> char.copy(equippedAccessoryId = item.id)
            EquipmentSlot.FRAME -> char.copy(equippedFrameId = item.id)
            EquipmentSlot.BACKGROUND -> char.copy(equippedBackgroundId = item.id)
        }
        repository.saveRpgCharacter(updated)
    }

    suspend fun unlockSkill(skill: SkillNodeEntity): Boolean {
        val char = repository.getRpgCharacter() ?: return false
        if (skill.isUnlocked) return true
        if (char.availableSkillPoints < skill.pointCost) return false
        if (char.level < skill.requiredCharacterLevel) return false

        repository.updateSkill(skill.copy(isUnlocked = true))
        repository.saveRpgCharacter(char.copy(availableSkillPoints = char.availableSkillPoints - skill.pointCost))
        return true
    }

    suspend fun damageBossWithGoal(bossId: Long, damageAmount: Int) {
        val boss = repository.rpgBosses.firstOrNull()?.find { it.id == bossId } ?: return
        val remainingHp = (boss.currentHp - damageAmount).coerceAtLeast(0)
        val defeated = remainingHp <= 0
        repository.updateBoss(boss.copy(currentHp = remainingHp, isDefeated = defeated))
        if (defeated) {
            awardProductivityXp("BOSS", boss.rewardXp, "COURAGE")
        }
    }
}
