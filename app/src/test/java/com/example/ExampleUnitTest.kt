package com.example

import com.example.data.local.model.FocusSessionEntity
import com.example.data.local.model.FocusTimerMode
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testFocusTimerModes() {
    assertEquals(25, FocusTimerMode.POMODORO_25.defaultMinutes)
    assertEquals(50, FocusTimerMode.DEEP_50.defaultMinutes)
    assertEquals(5, FocusTimerMode.BREAK_5.defaultMinutes)
    assertEquals(15, FocusTimerMode.BREAK_15.defaultMinutes)
    assertEquals(15, FocusTimerMode.CUSTOM.defaultMinutes)
  }

  @Test
  fun testFocusSessionEntityCalculation() {
    val session = FocusSessionEntity(
      taskId = 123L,
      taskTitle = "Design Architecture",
      durationMinutes = 50,
      actualSeconds = 3000,
      mode = FocusTimerMode.DEEP_50,
      isCompleted = true,
      completedAtMillis = System.currentTimeMillis(),
      soundMode = "RAIN"
    )

    assertEquals(50, session.durationMinutes)
    assertEquals(3000, session.actualSeconds)
    assertEquals(50, session.actualSeconds / 60)
    assertTrue(session.isCompleted)
    assertEquals("RAIN", session.soundMode)
    assertEquals(FocusTimerMode.DEEP_50, session.mode)
  }
}

