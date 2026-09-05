package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.local.AppDatabase
import com.example.data.local.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class RoomDatabaseTest {

    private lateinit var database: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun testTaskInsertAndRetrieve() = runBlocking {
        val task = TaskEntity(
            title = "Build APK for YAWMEK",
            category = TaskCategory.WORK,
            priority = Priority.HIGH,
            isCompleted = false,
            durationMinutes = 45
        )
        val insertedId = database.taskDao().insertTask(task)
        assertTrue(insertedId > 0)

        val allTasks = database.taskDao().getAllTasks().first()
        assertEquals(1, allTasks.size)
        assertEquals("Build APK for YAWMEK", allTasks[0].title)
        assertEquals(Priority.HIGH, allTasks[0].priority)
        assertEquals(45, allTasks[0].durationMinutes)
    }

    @Test
    fun testWalletAccountAndTransactions() = runBlocking {
        val account = WalletAccountEntity(
            name = "Emergency Fund",
            type = AccountType.SAVINGS,
            balanceMinor = 500000L,
            currency = "USD",
            iconName = "savings",
            colorHex = "#10B981"
        )
        val accId = database.walletAccountDao().insertAccount(account)
        assertTrue(accId > 0)

        val accounts = database.walletAccountDao().getAllActiveAccounts().first()
        assertEquals(1, accounts.size)
        assertEquals("Emergency Fund", accounts[0].name)
        assertEquals(500000L, accounts[0].balanceMinor)
    }

    @Test
    fun testHabitAndLogTracking() = runBlocking {
        val habit = HabitEntity(
            title = "Morning Exercise",
            targetDaysPerWeek = 5,
            iconKey = "check",
            colorHex = "#3B82F6"
        )
        val habitId = database.habitDao().insertHabit(habit)
        assertTrue(habitId > 0)

        val habits = database.habitDao().getAllActiveHabits().first()
        assertEquals(1, habits.size)
        assertEquals("Morning Exercise", habits[0].title)
        assertEquals(5, habits[0].targetDaysPerWeek)
    }

    @Test
    fun testRpgCharacterStorage() = runBlocking {
        val character = CharacterRpgEntity(
            id = 1,
            name = "Hero of Yawmek",
            level = 5,
            currentXp = 450L,
            totalEarnedXp = 1200L,
            availableSkillPoints = 2,
            statFocus = 4,
            statDiscipline = 5,
            statKnowledge = 3,
            statConsistency = 6,
            statPlanning = 4,
            statCourage = 3
        )
        database.rpgDao().insertOrUpdateCharacter(character)

        val loaded = database.rpgDao().getCharacter()
        assertNotNull(loaded)
        assertEquals("Hero of Yawmek", loaded?.name)
        assertEquals(5, loaded?.level)
        assertEquals(450L, loaded?.currentXp)
    }
}
