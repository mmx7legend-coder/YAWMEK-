package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class GameDifficulty(val level: Int, val labelEn: String, val labelAr: String, val multiplier: Float) {
    EASY(1, "Easy", "سهل", 1.0f),
    MEDIUM(2, "Medium", "متوسط", 1.5f),
    HARD(3, "Hard", "صعب", 2.0f),
    EXPERT(4, "Expert", "خبير", 2.8f),
    MASTER(5, "Master", "أستاذ", 3.5f)
}

enum class GameCategory(val labelEn: String, val labelAr: String, val iconKey: String) {
    ALL("All", "الكل", "all"),
    MEMORY("Memory", "الذاكرة", "psychology"),
    LOGIC("Logic", "المنطق", "extension"),
    MATH("Math", "الحساب", "calculate"),
    REACTION("Reaction", "السرعة", "bolt"),
    WORDS("Words", "الكلمات", "translate"),
    FOCUS("Focus", "التركيز", "visibility")
}

enum class GameId(
    val idString: String,
    val titleEn: String,
    val titleAr: String,
    val descriptionEn: String,
    val descriptionAr: String,
    val category: GameCategory,
    val iconKey: String,
    val estimatedMinutes: Int
) {
    MEMORY_GRID(
        "memory_grid",
        "Memory Grid",
        "شبكة الذاكرة",
        "Memorize the flashing tile sequence and reproduce it flawlessly.",
        "تذكر تسلسل المربعات المضيئة وكررها بنفس الترتيب بدقة.",
        GameCategory.MEMORY,
        "grid_view",
        2
    ),
    NUMBER_SPRINT(
        "number_sprint",
        "Number Sprint",
        "سباق الأرقام",
        "Solve rapid mental arithmetic and build huge score multipliers.",
        "حل المسائل الحسابية بسرعة فائقة وضاعف نقاطك بتتابع الإجابات.",
        GameCategory.MATH,
        "calculate",
        2
    ),
    PATTERN_MASTER(
        "pattern_master",
        "Pattern Master",
        "سيد الأنماط",
        "Spot the rule in sequence progressions and predict the next item.",
        "اكتشف القاعدة الرياضية أو المنطقية وأكمل التسلسل الصحيح.",
        GameCategory.LOGIC,
        "schema",
        3
    ),
    REACTION_TEST(
        "reaction_test",
        "Reaction Test",
        "اختبار رد الفعل",
        "Wait for the screen to turn green and tap with lightning speed.",
        "انتظر حتى تتحول الشاشة للأخضر واضغط بأقصى سرعة ممكنة.",
        GameCategory.REACTION,
        "bolt",
        1
    ),
    MEMORY_MATCH(
        "memory_match",
        "Memory Match",
        "تطابق الذاكرة",
        "Flip hidden cards to discover matching pairs with minimum moves.",
        "اقلب البطاقات المخفية واكشف الأزواج المتطابقة بأقل عدد حركات.",
        GameCategory.MEMORY,
        "style",
        3
    ),
    WORD_SCRAMBLE(
        "word_scramble",
        "Word Scramble",
        "حروف وكلمات",
        "Unscramble shuffled letters to reveal the hidden word.",
        "رتب الحروف المبعثرة واكتشف الكلمة الصحيحة قبل انتهاء الوقت.",
        GameCategory.WORDS,
        "spellcheck",
        2
    ),
    ODD_ONE_OUT(
        "odd_one_out",
        "Odd One Out",
        "العنصر المختلف",
        "Scan the matrix and identify the single item that doesn't fit.",
        "امسح العناصر بنظرك واكتشف العنصر الوحيد المختلف بدقة وسرعة.",
        GameCategory.FOCUS,
        "visibility",
        2
    ),
    LOGIC_LOCK(
        "logic_lock",
        "Logic Lock",
        "قفل المنطق",
        "Deduce clues, crack the reasoning puzzle, and unlock the code.",
        "استنتج الأدلة وحل الألغاز المنطقية لفتح القفل ومعرفة السبب.",
        GameCategory.LOGIC,
        "lock",
        3
    ),
    COLOR_AND_WORD(
        "color_and_word",
        "Color & Word",
        "اللون والكلمة",
        "Overcome the Stroop effect by obeying the dynamic color rules.",
        "تغلب على تشتيت الألوان واتبع التعليمات المتغيرة بتركيز عالٍ.",
        GameCategory.FOCUS,
        "palette",
        2
    ),
    QUICK_LOGIC(
        "quick_logic",
        "Quick Logic",
        "المنطق السريع",
        "A rapid-fire series of visual, numeric, and conceptual logic puzzles.",
        "تحدٍ سريع ومتنوع من ألغاز الأشكال والأعداد والمنطق التتابعي.",
        GameCategory.LOGIC,
        "dynamic_feed",
        2
    );

    companion object {
        fun fromId(id: String): GameId = entries.firstOrNull { it.idString == id } ?: MEMORY_GRID
    }
}

@Entity(tableName = "game_stats")
data class GameStatsEntity(
    @PrimaryKey val gameId: String,
    val bestScore: Int = 0,
    val bestTimeMillis: Long = 0L,
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val favoriteDifficulty: GameDifficulty = GameDifficulty.MEDIUM,
    val lastPlayedMillis: Long = 0L,
    val totalScore: Long = 0L,
    val averageScore: Double = 0.0
)

@Entity(tableName = "game_sessions")
data class GameSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameId: String,
    val score: Int,
    val timeMillis: Long,
    val difficulty: GameDifficulty,
    val accuracy: Int, // 0 to 100
    val combo: Int,
    val xpEarned: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val isPersonalRecord: Boolean = false
)

@Entity(tableName = "game_achievements")
data class GameAchievementEntity(
    @PrimaryKey val achievementId: String,
    val unlockedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "game_daily_challenges")
data class GameDailyChallengeEntity(
    @PrimaryKey val dateEpochDay: Long,
    val gameId: String,
    val difficulty: GameDifficulty,
    val targetScore: Int,
    val rewardXp: Int = 150,
    val isCompleted: Boolean = false,
    val bestScore: Int = 0
)

@Entity(tableName = "game_preferences")
data class GamePreferencesEntity(
    @PrimaryKey val id: Int = 1,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val animationsEnabled: Boolean = true,
    val favoriteGameIds: String = "", // Comma separated
    val userLevel: Int = 1,
    val userXp: Int = 0,
    val gameStreak: Int = 0,
    val lastPlayedEpochDay: Long = 0L,
    val preferredDifficulty: GameDifficulty = GameDifficulty.MEDIUM
)
