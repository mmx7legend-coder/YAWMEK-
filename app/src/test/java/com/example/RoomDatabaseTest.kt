package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoomDatabaseTest {

    private lateinit var database: AppDatabase

    @Before
    fun initDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun testTaskInsertAndRetrieve() = runBlocking {
        val task = TaskEntity(
            title = "Build APK for YAWMEK",
            category = Category.WORK,
            priority = Priority.HIGH,
            isCompleted = false,
            estimatedMinutes = 45,
            isDailyFrog = true
        )
        val insertedId = database.taskDao().insertTask(task)
        assertTrue(insertedId > 0)

        val allTasks = database.taskDao().getAllTasks().first()
        assertEquals(1, allTasks.size)
        assertEquals("Build APK for YAWMEK", allTasks[0].title)
        assertTrue(allTasks[0].isDailyFrog)
        assertEquals(Priority.HIGH, allTasks[0].priority)
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

        val accounts = database.walletAccountDao().getAllAccounts().first()
        assertEquals(1, accounts.size)
        assertEquals("Emergency Fund", accounts[0].name)
        assertEquals(500000L, accounts[0].balanceMinor)
    }

    @Test
    fun testHabitAndLogTracking() = runBlocking {
        val habit = HabitEntity(
            title = "Morning Exercise",
            targetDaysPerWeek = 5,
            icon = "directions_run",
            color = "#3B82F6",
            streak = 3
        )
        val habitId = database.habitDao().insertHabit(habit)
        assertTrue(habitId > 0)

        val habits = database.habitDao().getAllHabits().first()
        assertEquals(1, habits.size)
        assertEquals("Morning Exercise", habits[0].title)
        assertEquals(3, habits[0].streak)
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

        val loaded = database.rpgDao().getCharacterOnce()
        assertNotNull(loaded)
        assertEquals("Hero of Yawmek", loaded?.name)
        assertEquals(5, loaded?.level)
        assertEquals(450L, loaded?.currentXp)
    }
}
