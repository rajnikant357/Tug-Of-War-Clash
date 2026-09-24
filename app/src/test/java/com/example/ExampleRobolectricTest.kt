package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Tug of War Clash", appName)
  }

  @Test
  fun `verify app logo drawable resource exists`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val drawable = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.ic_app_logo)
    org.junit.Assert.assertNotNull("ic_app_logo drawable should exist", drawable)
  }

  @Test
  fun `verify pause and resume workflow in viewmodel`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.TugViewModel(app)
    vm.startMatch(com.example.model.GameMode.SOLO_VS_AI, com.example.model.AiDifficulty.EASY)

    assertEquals(false, vm.isPaused.value)

    vm.pauseGame()
    assertEquals(true, vm.isPaused.value)

    vm.resumeGame()
    assertEquals(false, vm.isPaused.value)

    vm.pauseGame()
    assertEquals(true, vm.isPaused.value)

    vm.restartCurrentMatch()
    assertEquals(false, vm.isPaused.value)
  }

  @Test
  fun `verify match history retains at most 10 matches in storage`() = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val database = com.example.data.db.AppDatabase.getInstance(app)
    val repository = com.example.data.GameRepository(database)
    repository.clearHistory()

    // Insert 15 match records
    for (i in 1..15) {
      repository.saveMatch(
        mode = "SOLO_VS_AI",
        winnerName = "Player 1",
        p1Name = "Player 1",
        p2Name = "AI $i",
        p1Taps = 50 + i,
        p2Taps = 40,
        p1MaxTps = 8.5f,
        p2MaxTps = 6.0f,
        durationSec = 20,
        isP1Winner = true
      )
    }

    val records = repository.matchRecords.first()
    assertEquals(10, records.size)
  }

  @Test
  fun `verify navigation to about screen in viewmodel`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.TugViewModel(app)

    vm.navigateTo(com.example.ui.Screen.Settings)
    assertEquals(com.example.ui.Screen.Settings, vm.currentScreen.value)

    vm.navigateTo(com.example.ui.Screen.About)
    assertEquals(com.example.ui.Screen.About, vm.currentScreen.value)

    vm.navigateTo(com.example.ui.Screen.Settings)
    assertEquals(com.example.ui.Screen.Settings, vm.currentScreen.value)
  }
}
