package com.example

import com.example.game.TugEngine
import com.example.model.AiDifficulty
import com.example.model.GameMode
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun tugEngine_initialState() {
    val engine = TugEngine(gameMode = GameMode.TWO_PLAYER_LOCAL)
    engine.startCountdown()
    assertTrue(engine.snapshot.isCountingDown)
    assertEquals(3, engine.snapshot.countdownNumber)
    assertEquals(0f, engine.snapshot.ropePosition, 0.001f)

    engine.startGame()
    assertTrue(engine.snapshot.isGameActive)
    assertFalse(engine.snapshot.isCountingDown)
  }

  @Test
  fun tugEngine_p1TappingPullsNegative() {
    val engine = TugEngine(gameMode = GameMode.TWO_PLAYER_LOCAL)
    engine.startGame()

    engine.registerPlayerTap(isPlayer1 = true)
    assertEquals(1, engine.snapshot.player1.tapsCount)
    assertTrue("Rope velocity should be negative after P1 tap", engine.snapshot.ropeVelocity < 0f)

    engine.updatePhysics(0.05f)
    assertTrue("Rope position should shift towards P1 (negative)", engine.snapshot.ropePosition < 0f)
  }

  @Test
  fun tugEngine_p2TappingPullsPositive() {
    val engine = TugEngine(gameMode = GameMode.TWO_PLAYER_LOCAL)
    engine.startGame()

    engine.registerPlayerTap(isPlayer1 = false)
    assertEquals(1, engine.snapshot.player2.tapsCount)
    assertTrue("Rope velocity should be positive after P2 tap", engine.snapshot.ropeVelocity > 0f)

    engine.updatePhysics(0.05f)
    assertTrue("Rope position should shift towards P2 (positive)", engine.snapshot.ropePosition > 0f)
  }
}

