package com.example.domain.community

import com.example.data.local.model.*
import kotlin.math.roundToInt
import kotlin.random.Random

data class BattleSimulationResult(
    val result: BattleResult,
    val myScore: Int,
    val opponentScore: Int,
    val ratingDelta: Int,
    val xpGained: Long,
    val coinsGained: Int,
    val summaryLog: String
)

object CommunityBattleEngine {

    fun simulateBattle(
        battleType: BattleType,
        myProfile: CommunityProfileEntity,
        myCharacter: CharacterRpgEntity?,
        myEquipment: List<EquipmentItemEntity>,
        myCompanions: List<CompanionEntity>,
        mySkills: List<SkillNodeEntity>,
        todayFocusMinutes: Int,
        todayTasksCompleted: Int,
        opponent: CommunityFriendEntity
    ): BattleSimulationResult {
        return when (battleType) {
            BattleType.RPG_ARENA -> simulateRpgArena(
                myProfile = myProfile,
                myCharacter = myCharacter,
                myEquipment = myEquipment,
                myCompanions = myCompanions,
                mySkills = mySkills,
                opponent = opponent
            )
            BattleType.FOCUS_SHOWDOWN -> simulateFocusShowdown(
                myProfile = myProfile,
                todayFocusMinutes = todayFocusMinutes,
                opponent = opponent
            )
            BattleType.TASK_RACE -> simulateTaskRace(
                myProfile = myProfile,
                todayTasksCompleted = todayTasksCompleted,
                opponent = opponent
            )
        }
    }

    private fun simulateRpgArena(
        myProfile: CommunityProfileEntity,
        myCharacter: CharacterRpgEntity?,
        myEquipment: List<EquipmentItemEntity>,
        myCompanions: List<CompanionEntity>,
        mySkills: List<SkillNodeEntity>,
        opponent: CommunityFriendEntity
    ): BattleSimulationResult {
        val myLevel = myCharacter?.level ?: 1
        val equippedIds = setOfNotNull(
            myCharacter?.equippedWeaponId,
            myCharacter?.equippedArmorId,
            myCharacter?.equippedHelmetId,
            myCharacter?.equippedGlovesId,
            myCharacter?.equippedBootsId,
            myCharacter?.equippedCapeId,
            myCharacter?.equippedAccessoryId
        )
        val equippedItems = myEquipment.filter { it.id in equippedIds }
        val gearAttack = equippedItems.sumOf { it.bonusStatFocus + it.bonusStatDiscipline + it.bonusStatCourage }
        val gearDefense = equippedItems.sumOf { it.bonusStatConsistency + it.bonusStatPlanning + it.bonusStatKnowledge }
        val isCompanionEquipped = myCompanions.any { it.id == myCharacter?.equippedCompanionId }
        val companionBuff = if (isCompanionEquipped) 10 else 0

        val baseAtk = (myCharacter?.statFocus ?: 1) + (myCharacter?.statDiscipline ?: 1) + (myCharacter?.statCourage ?: 1)
        val baseDef = (myCharacter?.statConsistency ?: 1) + (myCharacter?.statPlanning ?: 1) + (myCharacter?.statKnowledge ?: 1)

        val myAttack = (baseAtk + gearAttack) * (1.0 + companionBuff / 100.0)
        val myDefense = (baseDef + gearDefense) * (1.0 + companionBuff / 100.0)
        val myHp = 80 + (myLevel * 15) + ((myCharacter?.statConsistency ?: 1) * 5)

        // Opponent stats derived from level and rating
        val oppLevel = opponent.level.coerceAtLeast(1)
        val oppAttack = (oppLevel * 5.0 + 8.0) * (0.9 + Random.nextDouble() * 0.2)
        val oppDefense = (oppLevel * 4.0 + 6.0) * (0.9 + Random.nextDouble() * 0.2)
        val oppHp = 80 + oppLevel * 15

        // Turn simulation
        var currentMyHp = myHp.toDouble()
        var currentOppHp = oppHp.toDouble()
        val logs = StringBuilder()

        for (round in 1..4) {
            val myDmg = (myAttack * 1.2 - oppDefense * 0.5).coerceAtLeast(5.0) + Random.nextInt(0, 5)
            val oppDmg = (oppAttack * 1.1 - myDefense * 0.5).coerceAtLeast(4.0) + Random.nextInt(0, 5)

            currentOppHp -= myDmg
            currentMyHp -= oppDmg

            if (currentOppHp <= 0 || currentMyHp <= 0) break
        }

        val myFinalScore = currentMyHp.coerceAtLeast(0.0).roundToInt()
        val oppFinalScore = currentOppHp.coerceAtLeast(0.0).roundToInt()

        val isVictory = myFinalScore > oppFinalScore
        val isDraw = myFinalScore == oppFinalScore

        val result = if (isVictory) BattleResult.VICTORY else if (isDraw) BattleResult.DRAW else BattleResult.DEFEAT

        val (ratingDelta, xp, coins) = calculateRewards(result, myProfile.rating, opponent.rating)

        val log = if (isVictory) {
            "Triumphant victory! Landed decisive strikes dealing massive blows. +$ratingDelta Rating, +$xp XP, +$coins Gold."
        } else if (isDraw) {
            "Evenly matched duel! Both warriors stood firm with honor. +$ratingDelta Rating, +$xp XP."
        } else {
            "Narrow defeat! Opponent gained the upper hand this round. -$ratingDelta Rating."
        }

        return BattleSimulationResult(
            result = result,
            myScore = myFinalScore,
            opponentScore = oppFinalScore,
            ratingDelta = ratingDelta,
            xpGained = xp,
            coinsGained = coins,
            summaryLog = log
        )
    }

    private fun simulateFocusShowdown(
        myProfile: CommunityProfileEntity,
        todayFocusMinutes: Int,
        opponent: CommunityFriendEntity
    ): BattleSimulationResult {
        val myScore = (todayFocusMinutes * 2) + Random.nextInt(5, 20)
        val oppScore = (opponent.focusMinutes * 2) + Random.nextInt(5, 20)

        val isVictory = myScore > oppScore
        val isDraw = myScore == oppScore
        val result = if (isVictory) BattleResult.VICTORY else if (isDraw) BattleResult.DRAW else BattleResult.DEFEAT

        val (ratingDelta, xp, coins) = calculateRewards(result, myProfile.rating, opponent.rating)

        val log = if (isVictory) {
            "Focus Sprint Victory! Maintained laser concentration. +$ratingDelta Rating, +$xp XP, +$coins Gold."
        } else if (isDraw) {
            "Tied focus sprint! Equal dedication to the craft."
        } else {
            "Showdown conceded. Put in more deep work minutes to claim revenge!"
        }

        return BattleSimulationResult(
            result = result,
            myScore = myScore,
            opponentScore = oppScore,
            ratingDelta = ratingDelta,
            xpGained = xp,
            coinsGained = coins,
            summaryLog = log
        )
    }

    private fun simulateTaskRace(
        myProfile: CommunityProfileEntity,
        todayTasksCompleted: Int,
        opponent: CommunityFriendEntity
    ): BattleSimulationResult {
        val myScore = (todayTasksCompleted * 10) + Random.nextInt(0, 15)
        val oppScore = (opponent.tasksCompleted * 10) + Random.nextInt(5, 20)

        val isVictory = myScore > oppScore
        val isDraw = myScore == oppScore
        val result = if (isVictory) BattleResult.VICTORY else if (isDraw) BattleResult.DRAW else BattleResult.DEFEAT

        val (ratingDelta, xp, coins) = calculateRewards(result, myProfile.rating, opponent.rating)

        val log = if (isVictory) {
            "Task Blitz champion! Cleared high-priority goals rapidly. +$ratingDelta Rating, +$xp XP, +$coins Gold."
        } else if (isDraw) {
            "Dead heat! Equal productivity output recorded."
        } else {
            "Opponent sprinted through tasks faster today."
        }

        return BattleSimulationResult(
            result = result,
            myScore = myScore,
            opponentScore = oppScore,
            ratingDelta = ratingDelta,
            xpGained = xp,
            coinsGained = coins,
            summaryLog = log
        )
    }

    private fun calculateRewards(result: BattleResult, myRating: Int, oppRating: Int): Triple<Int, Long, Int> {
        val expected = 1.0 / (1.0 + Math.pow(10.0, (oppRating - myRating) / 400.0))
        val kFactor = 32

        return when (result) {
            BattleResult.VICTORY -> {
                val delta = ((1.0 - expected) * kFactor).roundToInt().coerceIn(12, 35)
                Triple(delta, 50L, 30)
            }
            BattleResult.DRAW -> {
                val delta = ((0.5 - expected) * kFactor).roundToInt().coerceIn(0, 10)
                Triple(delta, 25L, 15)
            }
            BattleResult.DEFEAT -> {
                val delta = ((0.0 - expected) * kFactor).roundToInt().coerceIn(-25, -10)
                Triple(delta, 10L, 5)
            }
        }
    }
}
