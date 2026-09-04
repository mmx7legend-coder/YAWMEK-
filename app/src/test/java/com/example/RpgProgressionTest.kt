package com.example

import com.example.data.local.model.*
import com.example.domain.rpg.RpgProgressionCalculator
import com.example.domain.rpg.RpgStarterCatalog
import org.junit.Assert.*
import org.junit.Test

class RpgProgressionTest {

    @Test
    fun testStarterCharacterIsLevelOneAndWeak() {
        val character = RpgStarterCatalog.getDefaultCharacter()
        assertEquals(1, character.level)
        assertEquals(0L, character.currentXp)
        assertEquals(1, character.statFocus)
        assertEquals(1, character.statDiscipline)
        assertEquals(1, character.statKnowledge)
        assertEquals(1, character.statConsistency)
        assertEquals(1, character.statPlanning)
        assertEquals(1, character.statCourage)
        assertEquals("starter_stick", character.equippedWeaponId)
        assertEquals("ragged_cloth", character.equippedArmorId)
        assertNull(character.equippedHelmetId)
        assertNull(character.equippedCompanionId)
        assertEquals(DragonStage.NONE, character.dragonStage)
        assertEquals(AdventureArea.VILLAGE, character.currentAdventureArea)
    }

    @Test
    fun testSlowProgressionCurve() {
        val xpLvl1 = RpgProgressionCalculator.getXpRequiredForNextLevel(1)
        val xpLvl5 = RpgProgressionCalculator.getXpRequiredForNextLevel(5)
        val xpLvl20 = RpgProgressionCalculator.getXpRequiredForNextLevel(20)

        assertTrue("Level 1 requirement should be substantial", xpLvl1 >= 120L)
        assertTrue("Level 5 should require significantly more than level 1", xpLvl5 > xpLvl1 * 5)
        assertTrue("High levels must require intense long-term consistency", xpLvl20 > 10000L)
    }

    @Test
    fun testAntiFarmingTaskThrottling() {
        val normalXp = RpgProgressionCalculator.evaluateTaskXp(isHighPriority = false, completedTodayCount = 5)
        val throttledXp = RpgProgressionCalculator.evaluateTaskXp(isHighPriority = false, completedTodayCount = 20)

        assertEquals(18L, normalXp)
        assertTrue("Repeated actions in single day must experience steep diminishing returns", throttledXp < normalXp)
        assertEquals(2L, throttledXp)
    }

    @Test
    fun testDragonUnlocksAreLateGame() {
        val earlyDragon = RpgProgressionCalculator.computeDragonStage(level = 5, completedGoals = 0)
        val eggDragon = RpgProgressionCalculator.computeDragonStage(level = 15, completedGoals = 1)
        val adultDragon = RpgProgressionCalculator.computeDragonStage(level = 65, completedGoals = 12)

        assertEquals(DragonStage.NONE, earlyDragon)
        assertEquals(DragonStage.EGG, eggDragon)
        assertEquals(DragonStage.ADULT, adultDragon)
    }

    @Test
    fun testAreaProgressionLockedByLevel() {
        val areaLvl1 = RpgProgressionCalculator.computeAdventureArea(1)
        val areaLvl15 = RpgProgressionCalculator.computeAdventureArea(15)
        val areaLvl55 = RpgProgressionCalculator.computeAdventureArea(55)

        assertEquals(AdventureArea.VILLAGE, areaLvl1)
        assertEquals(AdventureArea.RUINS, areaLvl15)
        assertEquals(AdventureArea.DRAGON_VALLEY, areaLvl55)
    }
}
