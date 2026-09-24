package com.example.model

enum class GameMode {
    TWO_PLAYER_LOCAL,
    SOLO_VS_AI,
    TOURNAMENT,
    TPS_PRACTICE
}

enum class AiDifficulty(val displayName: String, val baseTps: Float, val burstProbability: Float, val icon: String, val description: String) {
    EASY("Lazy Sloth", 3.4f, 0.05f, "🦥", "Slow, sleepy pulls with long pauses."),
    MEDIUM("Gym Rookie", 5.8f, 0.15f, "💪", "Steady rhythm with occasional bursts."),
    HARD("Iron Wolf", 8.2f, 0.30f, "🐺", "Aggressive puller, dangerous when losing."),
    TITAN("Mecha Titan", 10.5f, 0.45f, "🤖", "Relentless robotic cadence and heavy yanks."),
    CHAMPION("Tug Kong", 12.2f, 0.60f, "🦍", "Legendary king of the rope. Nearly unbeatable!")
}

enum class ArenaTheme(
    val displayName: String,
    val icon: String,
    val subtitle: String,
    val hazardTitle: String,
    val themeColorHex: Long
) {
    STADIUM("Grand Stadium", "🏟️", "Lush grass pitch with yard line markers", "Chalk Mark", 0xFF10B981),
    MUD_PIT("Swamp Mud Pit", "🪵", "Deep muddy quagmire with splashing muck", "Mud Quagmire", 0xFF92400E),
    CYBERPUNK("Neon Grid", "⚡", "Glowing synthwave cyber-grid & laser hazard", "Laser Barrier", 0xFF06B6D4),
    VOLCANO("Lava Gorge", "🌋", "Molten volcanic crags & boiling magma pit", "Molten Lava", 0xFFEF4444)
}

enum class TournamentStage(
    val stageNumber: Int,
    val bossName: String,
    val difficulty: AiDifficulty,
    val arenaTitle: String,
    val defaultArena: ArenaTheme
) {
    STAGE_1(1, "Sloth Sammy", AiDifficulty.EASY, "Sunny Park Lawn", ArenaTheme.STADIUM),
    STAGE_2(2, "Bicep Bob", AiDifficulty.MEDIUM, "Beach Boardwalk", ArenaTheme.MUD_PIT),
    STAGE_3(3, "Fang Shadow", AiDifficulty.HARD, "Muddy Swamp Pit", ArenaTheme.MUD_PIT),
    STAGE_4(4, "Cyber 9000", AiDifficulty.TITAN, "Neon Cyber Ring", ArenaTheme.CYBERPUNK),
    STAGE_5(5, "King Kong Tug", AiDifficulty.CHAMPION, "Volcano Peak", ArenaTheme.VOLCANO)
}

data class PlayerState(
    val name: String = "Player 1",
    val colorHex: Long = 0xFF2563EB, // Blue
    val tapsCount: Int = 0,
    val currentTps: Float = 0f,
    val peakTps: Float = 0f,
    val surgeMeter: Float = 0f, // 0.0 to 1.0
    val isSurging: Boolean = false,
    val lastTapTime: Long = 0L,
    val teamIcon: String = "🔵"
)
