package com.example

import com.example.data.local.model.GameDifficulty
import com.example.data.local.model.GameId
import com.example.domain.games.GameAchievementsCatalog
import com.example.domain.games.engine.*
import org.junit.Assert.*
import org.junit.Test

class GameEnginesTest {

    @Test
    fun testAchievementsCatalog() {
        val catalog = GameAchievementsCatalog.allAchievements
        assertEquals(24, catalog.size)
        // Every achievement has unique ID
        val ids = catalog.map { it.id }.toSet()
        assertEquals(24, ids.size)
    }

    @Test
    fun testMemoryGridConfigAndSequence() {
        val easyConfig = MemoryGridEngine.getConfig(GameDifficulty.EASY)
        assertEquals(3, easyConfig.gridSize)
        assertEquals(3, easyConfig.initialSequenceLength)

        val hardConfig = MemoryGridEngine.getConfig(GameDifficulty.HARD)
        assertEquals(5, hardConfig.gridSize)
        assertEquals(5, hardConfig.initialSequenceLength)

        val sequence = MemoryGridEngine.generateSequence(easyConfig.gridSize, easyConfig.initialSequenceLength)
        assertEquals(3, sequence.size)
        sequence.forEach { tileIndex ->
            assertTrue(tileIndex in 0 until (easyConfig.gridSize * easyConfig.gridSize))
        }
    }

    @Test
    fun testNumberSprintQuestionGeneration() {
        val question = NumberSprintEngine.generateQuestion(GameDifficulty.EASY)
        assertTrue(question.options.contains(question.answer))
        assertEquals(4, question.options.size)
    }

    @Test
    fun testPatternMasterEngine() {
        val puzzle = PatternMasterEngine.generatePuzzle(GameDifficulty.EASY)
        assertTrue(puzzle.sequence.isNotEmpty())
        assertTrue(puzzle.options.contains(puzzle.answer))
        assertEquals(4, puzzle.options.size)
    }

    @Test
    fun testAllTenGamesDefined() {
        assertEquals(10, GameId.entries.size)
    }
}
