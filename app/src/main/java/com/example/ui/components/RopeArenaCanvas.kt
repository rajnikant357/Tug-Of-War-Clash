package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.game.TugGameSnapshot
import com.example.model.ArenaTheme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RopeArenaCanvas(
    snapshot: TugGameSnapshot,
    arenaTheme: ArenaTheme = ArenaTheme.STADIUM,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val waveAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        waveAnim.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1400, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val centerY = height * 0.5f

        // 1. Draw the full distinct Arena Ground & Background
        drawArenaGround(arenaTheme, width, height, waveAnim.value)

        // 2. Draw Center Hazard Zone (Chalk Circle / Mud Pool / Laser Beam / Lava Pit)
        drawHazardZone(arenaTheme, width, height, centerY, waveAnim.value)

        // 3. Draw Win Marker Thresholds
        val winOffsetPx = (width * 0.38f)
        val leftWinX = (width * 0.5f) - winOffsetPx
        val rightWinX = (width * 0.5f) + winOffsetPx

        // Boundary lines
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
        val leftMarkerColor = when (arenaTheme) {
            ArenaTheme.CYBERPUNK -> Color(0xFF00F0FF)
            ArenaTheme.VOLCANO -> Color(0xFFFF6D00)
            else -> Color(0xFF3B82F6)
        }
        val rightMarkerColor = when (arenaTheme) {
            ArenaTheme.CYBERPUNK -> Color(0xFFFF007F)
            ArenaTheme.VOLCANO -> Color(0xFFFF3D00)
            else -> Color(0xFFEF4444)
        }

        drawLine(
            color = leftMarkerColor.copy(alpha = 0.7f),
            start = Offset(leftWinX, centerY - 110f),
            end = Offset(leftWinX, centerY + 110f),
            strokeWidth = 3f,
            pathEffect = dashEffect
        )
        drawLine(
            color = rightMarkerColor.copy(alpha = 0.7f),
            start = Offset(rightWinX, centerY - 110f),
            end = Offset(rightWinX, centerY + 110f),
            strokeWidth = 3f,
            pathEffect = dashEffect
        )

        // 4. Calculate Rope Center Shift
        val ropeShiftPx = snapshot.ropePosition * winOffsetPx
        val ropeCenterFlagX = (width * 0.5f) + ropeShiftPx

        // Dynamic rope vibration / tension
        val tensionIntensity = (abs(snapshot.ropeVelocity) * 16f).coerceIn(0f, 6.5f)
        val vibration = if (snapshot.isGameActive) sin(waveAnim.value * 24f * PI.toFloat()) * tensionIntensity else 0f
        val actualRopeY = centerY + vibration

        // 5. Draw Theme-Specific Rope
        drawTugRope(
            theme = arenaTheme,
            width = width,
            centerY = actualRopeY,
            leftAnchorX = 40f,
            rightAnchorX = width - 40f,
            centerFlagX = ropeCenterFlagX,
            tension = abs(snapshot.ropeVelocity),
            animProgress = waveAnim.value
        )

        // 6. Draw Redesigned Characters / Tuggers with Hopping Animation (Blue on Left, Red on Right)
        drawTuggerTeam(
            isLeftTeam = true,
            teamColor = Color(0xFF2563EB),
            teamAccent = Color(0xFF60A5FA),
            centerX = (width * 0.20f) + (ropeShiftPx * 0.85f),
            centerY = centerY,
            ropeY = actualRopeY,
            tps = snapshot.player1.currentTps,
            isSurging = snapshot.player1.isSurging,
            animProgress = waveAnim.value,
            textMeasurer = textMeasurer,
            icon = snapshot.player1.teamIcon,
            arenaTheme = arenaTheme
        )

        drawTuggerTeam(
            isLeftTeam = false,
            teamColor = Color(0xFFDC2626),
            teamAccent = Color(0xFFF87171),
            centerX = (width * 0.80f) + (ropeShiftPx * 0.85f),
            centerY = centerY,
            ropeY = actualRopeY,
            tps = snapshot.player2.currentTps,
            isSurging = snapshot.player2.isSurging,
            animProgress = waveAnim.value,
            textMeasurer = textMeasurer,
            icon = snapshot.player2.teamIcon,
            arenaTheme = arenaTheme
        )

        // 7. Draw Tied Center Ribbon / Indicator Flag
        drawCenterRibbon(
            theme = arenaTheme,
            flagX = ropeCenterFlagX,
            flagY = actualRopeY,
            wave = waveAnim.value
        )

        // 8. Draw Balance Gauge / Percentage at Top
        drawBalanceIndicator(
            width = width,
            ropePos = snapshot.ropePosition,
            theme = arenaTheme,
            textMeasurer = textMeasurer
        )
    }
}

private fun DrawScope.drawArenaGround(
    theme: ArenaTheme,
    width: Float,
    height: Float,
    animProgress: Float
) {
    when (theme) {
        ArenaTheme.STADIUM -> {
            val stripeCount = 10
            val stripeHeight = height / stripeCount
            for (i in 0 until stripeCount) {
                val stripeColor = if (i % 2 == 0) Color(0xFF15532D) else Color(0xFF196336)
                drawRect(
                    color = stripeColor,
                    topLeft = Offset(0f, i * stripeHeight),
                    size = Size(width, stripeHeight)
                )
            }
            val chalkColor = Color(0x77FFFFFF)
            drawLine(chalkColor, Offset(30f, 0f), Offset(30f, height), strokeWidth = 3f)
            drawLine(chalkColor, Offset(width - 30f, 0f), Offset(width - 30f, height), strokeWidth = 3f)
            drawLine(chalkColor, Offset(0f, 20f), Offset(width, 20f), strokeWidth = 3f)
            drawLine(chalkColor, Offset(0f, height - 20f), Offset(width, height - 20f), strokeWidth = 3f)
        }

        ArenaTheme.MUD_PIT -> {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF2A170C), Color(0xFF3B2212), Color(0xFF1F1007))
                ),
                size = size
            )
            val mudPatchColor = Color(0xFF1A0E06)
            drawOval(mudPatchColor, Offset(width * 0.1f, height * 0.2f), Size(width * 0.35f, 60f))
            drawOval(mudPatchColor, Offset(width * 0.55f, height * 0.65f), Size(width * 0.35f, 70f))

            val logColor = Color(0xFF5C3A21)
            drawRect(logColor, Offset(15f, 0f), Size(12f, height))
            drawRect(logColor, Offset(width - 27f, 0f), Size(12f, height))
        }

        ArenaTheme.CYBERPUNK -> {
            drawRect(color = Color(0xFF070B14), size = size)

            val gridColor = Color(0x3300F0FF)
            val magentaColor = Color(0x22FF007F)
            val stepY = height / 8f
            for (i in 0..8) {
                val y = i * stepY
                drawLine(gridColor, Offset(0f, y), Offset(width, y), strokeWidth = 1.5f)
            }
            val stepX = width / 12f
            for (j in 0..12) {
                val x = j * stepX
                drawLine(if (j % 2 == 0) gridColor else magentaColor, Offset(x, 0f), Offset(x, height), strokeWidth = 1f)
            }

            drawLine(Color(0xFF00F0FF), Offset(10f, 10f), Offset(60f, 10f), strokeWidth = 2f)
            drawLine(Color(0xFF00F0FF), Offset(10f, 10f), Offset(10f, 60f), strokeWidth = 2f)
            drawLine(Color(0xFFFF007F), Offset(width - 10f, height - 10f), Offset(width - 60f, height - 10f), strokeWidth = 2f)
            drawLine(Color(0xFFFF007F), Offset(width - 10f, height - 10f), Offset(width - 10f, height - 60f), strokeWidth = 2f)
        }

        ArenaTheme.VOLCANO -> {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF280B07), Color(0xFF140403)),
                    center = Offset(width * 0.5f, height * 0.5f),
                    radius = width * 0.8f
                ),
                size = size
            )

            val crackColor = Color(0x88FF5722)
            val brightCrack = Color(0xCCFFB74D)

            val leftCrack = Path().apply {
                moveTo(width * 0.05f, height * 0.1f)
                lineTo(width * 0.18f, height * 0.35f)
                lineTo(width * 0.12f, height * 0.65f)
                lineTo(width * 0.25f, height * 0.9f)
            }
            drawPath(leftCrack, crackColor, style = Stroke(width = 3f))
            drawPath(leftCrack, brightCrack, style = Stroke(width = 1f))

            val rightCrack = Path().apply {
                moveTo(width * 0.95f, height * 0.15f)
                lineTo(width * 0.82f, height * 0.4f)
                lineTo(width * 0.88f, height * 0.7f)
                lineTo(width * 0.75f, height * 0.88f)
            }
            drawPath(rightCrack, crackColor, style = Stroke(width = 3f))
            drawPath(rightCrack, brightCrack, style = Stroke(width = 1f))
        }
    }
}

private fun DrawScope.drawHazardZone(
    theme: ArenaTheme,
    width: Float,
    height: Float,
    centerY: Float,
    animProgress: Float
) {
    val centerX = width * 0.5f
    val hazardWidth = width * 0.24f

    when (theme) {
        ArenaTheme.STADIUM -> {
            val chalkColor = Color(0xBBDDE1E6)
            drawLine(
                color = chalkColor,
                start = Offset(centerX, 20f),
                end = Offset(centerX, height - 20f),
                strokeWidth = 5f
            )
            drawCircle(
                color = chalkColor,
                radius = 42f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 4f)
            )
            drawCircle(
                color = chalkColor,
                radius = 5f,
                center = Offset(centerX, centerY)
            )
        }

        ArenaTheme.MUD_PIT -> {
            val mudRadius = hazardWidth * 0.75f
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF130903), Color(0xFF26140A), Color.Transparent),
                    center = Offset(centerX, centerY),
                    radius = mudRadius
                ),
                topLeft = Offset(centerX - mudRadius, centerY - 90f),
                size = Size(mudRadius * 2f, 180f)
            )

            val rippleScale = (sin(animProgress * 2 * PI.toFloat()) * 0.15f) + 0.85f
            drawOval(
                color = Color(0x554A2810),
                topLeft = Offset(centerX - (mudRadius * 0.5f * rippleScale), centerY - 30f),
                size = Size(mudRadius * rippleScale, 60f),
                style = Stroke(width = 3f)
            )
        }

        ArenaTheme.CYBERPUNK -> {
            val laserGlow = Color(0x6600F0FF)
            val laserCore = Color(0xFFE0FFFF)
            val magentaGlow = Color(0x66FF007F)

            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, laserGlow, magentaGlow, Color.Transparent),
                    startX = centerX - 30f,
                    endX = centerX + 30f
                ),
                topLeft = Offset(centerX - 30f, 0f),
                size = Size(60f, height)
            )

            drawLine(
                color = laserCore,
                start = Offset(centerX, 0f),
                end = Offset(centerX, height),
                strokeWidth = 3f
            )

            val sparkY1 = (animProgress * height)
            val sparkY2 = ((animProgress + 0.5f) % 1f) * height
            drawCircle(Color(0xFF00F0FF), radius = 6f, center = Offset(centerX + 6f * sin(animProgress * 10f), sparkY1))
            drawCircle(Color(0xFFFF007F), radius = 5f, center = Offset(centerX - 6f * cos(animProgress * 10f), sparkY2))
        }

        ArenaTheme.VOLCANO -> {
            val pulse = (sin(animProgress * 2 * PI.toFloat()) * 0.15f) + 0.85f
            val lavaRadius = hazardWidth * 0.7f

            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFD54F), Color(0xFFFF5722), Color(0xFFBF360C), Color.Transparent),
                    center = Offset(centerX, centerY),
                    radius = lavaRadius * pulse
                ),
                topLeft = Offset(centerX - lavaRadius, centerY - 95f),
                size = Size(lavaRadius * 2f, 190f)
            )

            val emberY = centerY - 60f + (animProgress * 120f)
            drawCircle(Color(0xFFFFEE58), radius = 4f, center = Offset(centerX - 15f, emberY))
            drawCircle(Color(0xFFFF7043), radius = 3f, center = Offset(centerX + 20f, height - emberY))
        }
    }
}

private fun DrawScope.drawTugRope(
    theme: ArenaTheme,
    width: Float,
    centerY: Float,
    leftAnchorX: Float,
    rightAnchorX: Float,
    centerFlagX: Float,
    tension: Float,
    animProgress: Float
) {
    val sagAmount = (18f - (tension * 8f)).coerceAtLeast(4f)
    val leftMidX = (leftAnchorX + centerFlagX) * 0.5f
    val rightMidX = (centerFlagX + rightAnchorX) * 0.5f

    val ropePath = Path().apply {
        moveTo(leftAnchorX, centerY)
        quadraticTo(leftMidX, centerY + sagAmount, centerFlagX, centerY)
        quadraticTo(rightMidX, centerY + sagAmount, rightAnchorX, centerY)
    }

    when (theme) {
        ArenaTheme.CYBERPUNK -> {
            drawPath(ropePath, color = Color(0x5500F0FF), style = Stroke(width = 18f, cap = StrokeCap.Round))
            drawPath(ropePath, color = Color(0xFF00E5FF), style = Stroke(width = 8f, cap = StrokeCap.Round))
            drawPath(ropePath, color = Color.White, style = Stroke(width = 3f, cap = StrokeCap.Round))
        }

        ArenaTheme.VOLCANO -> {
            drawPath(ropePath, color = Color(0x66FF3D00), style = Stroke(width = 16f))
            drawPath(ropePath, color = Color(0xFF374151), style = Stroke(width = 9f))
            drawPath(ropePath, color = Color(0xFFFF9100), style = Stroke(width = 3f))
        }

        ArenaTheme.MUD_PIT -> {
            drawPath(ropePath, color = Color(0xFF45220A), style = Stroke(width = 14f))
            drawPath(ropePath, color = Color(0xFF92400E), style = Stroke(width = 8f))
            drawPath(ropePath, color = Color(0xFFB45309), style = Stroke(width = 3f))
        }

        ArenaTheme.STADIUM -> {
            drawPath(ropePath, color = Color(0x44000000), style = Stroke(width = 16f))
            drawPath(ropePath, color = Color(0xFFD97706), style = Stroke(width = 10f))
            drawPath(ropePath, color = Color(0xFFFDE68A), style = Stroke(width = 4f))
        }
    }
}

private fun DrawScope.drawCenterRibbon(
    theme: ArenaTheme,
    flagX: Float,
    flagY: Float,
    wave: Float
) {
    val flutter = sin(wave * 2 * PI.toFloat()) * 8f

    val ribbonColor = when (theme) {
        ArenaTheme.CYBERPUNK -> Color(0xFFFF007F)
        ArenaTheme.VOLCANO -> Color(0xFFFF3D00)
        ArenaTheme.MUD_PIT -> Color(0xFFEA580C)
        ArenaTheme.STADIUM -> Color(0xFFDC2626)
    }

    drawCircle(color = ribbonColor, radius = 9f, center = Offset(flagX, flagY))
    drawCircle(color = Color.White, radius = 3f, center = Offset(flagX, flagY))

    val flagTail = Path().apply {
        moveTo(flagX - 4f, flagY)
        lineTo(flagX - 12f + flutter, flagY + 36f)
        lineTo(flagX + flutter, flagY + 28f)
        lineTo(flagX + 12f + flutter, flagY + 36f)
        lineTo(flagX + 4f, flagY)
        close()
    }
    drawPath(flagTail, color = ribbonColor)
}

/**
 * Redesigned Tug-of-War Squad with Dynamic Hopping Animation
 * Features:
 * - Duo squad: Anchor Tugger (back) & Captain Tugger (front)
 * - Dynamic parabolic hop motion responding to TPS cadence
 * - Deep lean-back pulling mechanics
 * - Hands clamped firmly onto the rope
 * - Animated ground shadows & kick-up dust particles
 * - Expressive straining face, headband, jersey and avatar badge
 */
private fun DrawScope.drawTuggerTeam(
    isLeftTeam: Boolean,
    teamColor: Color,
    teamAccent: Color,
    centerX: Float,
    centerY: Float,
    ropeY: Float,
    tps: Float,
    isSurging: Boolean,
    animProgress: Float,
    textMeasurer: TextMeasurer,
    icon: String,
    arenaTheme: ArenaTheme
) {
    val direction = if (isLeftTeam) -1f else 1f
    val isPulling = tps > 0.3f || isSurging

    // Hopping cadence & height
    val hopFrequency = if (isPulling) (4f + tps.coerceIn(0f, 14f) * 1.3f) else 1.8f
    val baseHopHeight = if (isSurging) 24f else if (isPulling) (11f + tps.coerceIn(0f, 10f) * 1.1f) else 4f

    // Surging power aura
    if (isSurging) {
        val pulse = (sin(animProgress * 16f) * 0.2f) + 1f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFBBF24).copy(alpha = 0.5f), Color.Transparent),
                center = Offset(centerX, centerY),
                radius = 65f * pulse
            ),
            radius = 65f * pulse,
            center = Offset(centerX, centerY)
        )
    }

    // 1. Back Tugger (Anchor - Heavyweight)
    val backOffset = 36f * direction
    val backPhase = (animProgress * hopFrequency + 0.3f) % 1f
    drawSingleTugger(
        isLeftTeam = isLeftTeam,
        teamColor = teamColor,
        teamAccent = teamAccent,
        baseX = centerX + backOffset,
        baseY = centerY + 24f,
        ropeY = ropeY,
        tps = tps,
        isSurging = isSurging,
        hopPhase = backPhase,
        hopHeight = baseHopHeight * 0.9f,
        isCaptain = false,
        icon = icon,
        textMeasurer = textMeasurer,
        arenaTheme = arenaTheme
    )

    // 2. Front Tugger (Captain - Lead)
    val frontOffset = -14f * direction
    val frontPhase = (animProgress * hopFrequency) % 1f
    drawSingleTugger(
        isLeftTeam = isLeftTeam,
        teamColor = teamColor,
        teamAccent = teamAccent,
        baseX = centerX + frontOffset,
        baseY = centerY + 28f,
        ropeY = ropeY,
        tps = tps,
        isSurging = isSurging,
        hopPhase = frontPhase,
        hopHeight = baseHopHeight,
        isCaptain = true,
        icon = icon,
        textMeasurer = textMeasurer,
        arenaTheme = arenaTheme
    )
}

/**
 * Draws an individual redesigned athletic tugger with hopping physics.
 */
private fun DrawScope.drawSingleTugger(
    isLeftTeam: Boolean,
    teamColor: Color,
    teamAccent: Color,
    baseX: Float,
    baseY: Float,
    ropeY: Float,
    tps: Float,
    isSurging: Boolean,
    hopPhase: Float,
    hopHeight: Float,
    isCaptain: Boolean,
    icon: String,
    textMeasurer: TextMeasurer,
    arenaTheme: ArenaTheme
) {
    val direction = if (isLeftTeam) -1f else 1f

    // Parabolic hopping arc: jump up and land down
    val hopArc = sin(hopPhase * PI.toFloat()).coerceAtLeast(0f)
    val hopY = -hopArc * hopHeight

    // Lean angle: tuggers pull backwards forcefully
    val leanAngle = direction * (-20f - (tps * 1.4f).coerceAtMost(16f))

    // Ground position for feet & shadow
    val groundY = baseY + 20f

    // Dynamic ground shadow that shrinks as character hops up
    val shadowWidth = (28f * (1f - hopArc * 0.35f)).coerceAtLeast(14f)
    val shadowAlpha = (0.35f * (1f - hopArc * 0.45f)).coerceAtLeast(0.12f)
    drawOval(
        color = Color.Black.copy(alpha = shadowAlpha),
        topLeft = Offset(baseX - (shadowWidth * 0.5f), groundY - 4f),
        size = Size(shadowWidth, 8f)
    )

    // Kick-up dust / turf particles under boots when hopping back
    if (hopArc > 0.25f && tps > 0.5f) {
        val particleColor = when (arenaTheme) {
            ArenaTheme.MUD_PIT -> Color(0xFF45220A)
            ArenaTheme.CYBERPUNK -> Color(0xFF00F0FF)
            ArenaTheme.VOLCANO -> Color(0xFFFF6D00)
            ArenaTheme.STADIUM -> Color(0xFFE2E8F0)
        }
        val kickDir = direction
        val pProgress = hopPhase
        drawCircle(
            color = particleColor.copy(alpha = (1f - pProgress).coerceIn(0f, 0.7f)),
            radius = 3.5f * (1f - pProgress * 0.4f),
            center = Offset(baseX + kickDir * (12f + pProgress * 16f), groundY - 3f - (pProgress * 10f))
        )
        drawCircle(
            color = particleColor.copy(alpha = (1f - pProgress * 1.2f).coerceIn(0f, 0.5f)),
            radius = 2.5f,
            center = Offset(baseX + kickDir * (6f + pProgress * 22f), groundY - 6f - (pProgress * 8f))
        )
    }

    // Flying sweat droplets from character when pulling hard
    if (tps > 3.5f || isSurging) {
        val sweatX = baseX - (direction * 12f)
        val sweatY = baseY + hopY - 26f
        val sweatProgress = ((hopPhase + 0.4f) % 1f)
        drawCircle(
            color = Color(0xFF67E8F9).copy(alpha = (1f - sweatProgress)),
            radius = 2.5f,
            center = Offset(sweatX - (direction * sweatProgress * 16f), sweatY - (sweatProgress * 12f))
        )
    }

    // Character body rooted at baseX, baseY + hopY
    val charY = baseY + hopY

    rotate(degrees = leanAngle, pivot = Offset(baseX, groundY)) {
        val skinColor = Color(0xFFFBBF24)

        // 1. LEGS & ATHLETIC CLEATS
        // Back braced leg
        val backLegEndX = baseX + (direction * 14f)
        val backLegEndY = groundY - 2f
        drawLine(
            color = Color(0xFF0F172A),
            start = Offset(baseX, charY + 6f),
            end = Offset(backLegEndX, backLegEndY),
            strokeWidth = 7f,
            cap = StrokeCap.Round
        )
        // Back shoe/cleat
        drawRoundRect(
            color = teamColor,
            topLeft = Offset(backLegEndX - 4f, backLegEndY - 4f),
            size = Size(10f, 6f),
            cornerRadius = CornerRadius(3f, 3f)
        )

        // Front pushing leg
        val frontLegKneeX = baseX - (direction * 8f)
        val frontLegKneeY = charY + 12f
        val frontLegFootX = baseX - (direction * 12f)
        val frontLegFootY = groundY - 2f
        drawLine(
            color = Color(0xFF0F172A),
            start = Offset(baseX, charY + 6f),
            end = Offset(frontLegKneeX, frontLegKneeY),
            strokeWidth = 7f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFF0F172A),
            start = Offset(frontLegKneeX, frontLegKneeY),
            end = Offset(frontLegFootX, frontLegFootY),
            strokeWidth = 7f,
            cap = StrokeCap.Round
        )
        // Front shoe/cleat
        drawRoundRect(
            color = teamColor,
            topLeft = Offset(frontLegFootX - 4f, frontLegFootY - 4f),
            size = Size(10f, 6f),
            cornerRadius = CornerRadius(3f, 3f)
        )

        // 2. ATHLETIC SHORTS
        drawRoundRect(
            color = Color(0xFF1E293B),
            topLeft = Offset(baseX - 9f, charY),
            size = Size(18f, 10f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        // Shorts trim in team accent
        drawLine(
            color = teamAccent,
            start = Offset(baseX - 7f, charY + 9f),
            end = Offset(baseX + 7f, charY + 9f),
            strokeWidth = 2f
        )

        // 3. MUSCULAR TORSO / ATHLETIC JERSEY
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(teamAccent, teamColor)
            ),
            topLeft = Offset(baseX - 8.5f, charY - 18f),
            size = Size(17f, 20f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        // Jersey athletic center stripe
        drawLine(
            color = Color.White.copy(alpha = 0.85f),
            start = Offset(baseX, charY - 17f),
            end = Offset(baseX, charY + 1f),
            strokeWidth = 2f
        )

        // 4. ARMS & HANDS CLAMPED ONTO THE ROPE
        val armShoulderY = charY - 12f
        val handTargetX = baseX - (direction * 18f)
        val handTargetY = charY - 4f

        // Back Arm
        drawLine(
            color = skinColor,
            start = Offset(baseX, armShoulderY),
            end = Offset(handTargetX + (direction * 4f), handTargetY),
            strokeWidth = 5.5f,
            cap = StrokeCap.Round
        )
        // Front Arm reaching forward to clamp the rope
        drawLine(
            color = skinColor,
            start = Offset(baseX, armShoulderY + 2f),
            end = Offset(handTargetX, handTargetY),
            strokeWidth = 5.5f,
            cap = StrokeCap.Round
        )

        // Team sweatband on wrists
        drawCircle(
            color = teamColor,
            radius = 3.5f,
            center = Offset(handTargetX + (direction * 2f), handTargetY)
        )

        // Clamping Hands / Gripping Glove
        drawCircle(
            color = Color.White,
            radius = 4f,
            center = Offset(handTargetX, handTargetY)
        )

        // 5. HEAD & EXPRESSIVE FACE
        val headCenterY = charY - 26f
        drawCircle(
            color = skinColor,
            radius = 10f,
            center = Offset(baseX, headCenterY)
        )

        // Athletic Headband in team color
        drawRoundRect(
            color = teamColor,
            topLeft = Offset(baseX - 10f, headCenterY - 6f),
            size = Size(20f, 5f),
            cornerRadius = CornerRadius(2f, 2f)
        )

        // Straining eye (focused toward opponent)
        val eyeX = baseX - (direction * 4f)
        drawCircle(
            color = Color.White,
            radius = 2.5f,
            center = Offset(eyeX, headCenterY + 1f)
        )
        drawCircle(
            color = Color.Black,
            radius = 1.3f,
            center = Offset(eyeX - (direction * 0.8f), headCenterY + 1f)
        )

        // Clenched straining teeth / mouth
        drawLine(
            color = Color(0xFF7F1D1D),
            start = Offset(eyeX - 1f, headCenterY + 6f),
            end = Offset(eyeX + 3f, headCenterY + 6f),
            strokeWidth = 1.5f,
            cap = StrokeCap.Round
        )
    }

    // 6. MASCOT AVATAR BADGE (Captain only)
    if (isCaptain) {
        val badgeY = charY - 48f
        val badgeBounce = sin(hopPhase * 2 * PI.toFloat()) * 3f
        val emojiResult = textMeasurer.measure(
            text = icon,
            style = TextStyle(fontSize = 17.sp)
        )
        drawText(
            textLayoutResult = emojiResult,
            topLeft = Offset(
                baseX - (emojiResult.size.width / 2f),
                badgeY + badgeBounce - (emojiResult.size.height / 2f)
            )
        )
    }
}

private fun DrawScope.drawBalanceIndicator(
    width: Float,
    ropePos: Float,
    theme: ArenaTheme,
    textMeasurer: TextMeasurer
) {
    val barWidth = width * 0.6f
    val barHeight = 8f
    val startX = (width - barWidth) / 2f
    val barY = 24f

    // Background track
    drawRoundRect(
        color = Color(0x66000000),
        topLeft = Offset(startX, barY),
        size = Size(barWidth, barHeight),
        cornerRadius = CornerRadius(4f, 4f)
    )

    // Center divider
    val centerX = width * 0.5f
    drawLine(
        color = Color.White.copy(alpha = 0.6f),
        start = Offset(centerX, barY - 4f),
        end = Offset(centerX, barY + barHeight + 4f),
        strokeWidth = 2f
    )

    // Dynamic marker (moves with rope position: -1 to +1)
    val markerX = centerX + (ropePos * (barWidth / 2f)).coerceIn(-(barWidth / 2f), (barWidth / 2f))
    val markerColor = when {
        ropePos < -0.1f -> Color(0xFF60A5FA)
        ropePos > 0.1f -> Color(0xFFF87171)
        else -> Color(0xFFFBBF24)
    }

    drawCircle(
        color = markerColor,
        radius = 8f,
        center = Offset(markerX, barY + (barHeight / 2f))
    )
    drawCircle(
        color = Color.White,
        radius = 3f,
        center = Offset(markerX, barY + (barHeight / 2f))
    )
}
