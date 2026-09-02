package com.example.domain.games

data class GameAchievementDef(
    val id: String,
    val titleEn: String,
    val titleAr: String,
    val descriptionEn: String,
    val descriptionAr: String,
    val iconEmoji: String,
    val xpBonus: Int
)

object GameAchievementsCatalog {
    val allAchievements: List<GameAchievementDef> = listOf(
        GameAchievementDef(
            "first_game",
            "First Step",
            "الخطوة الأولى",
            "Play your very first brain game.",
            "العب أول لعبة لتمرين عقلك.",
            "🧠",
            50
        ),
        GameAchievementDef(
            "daily_spark",
            "Daily Spark",
            "شرارة اليوم",
            "Complete today's daily challenge.",
            "أكمل تحدي اليوم المخصص.",
            "🔥",
            100
        ),
        GameAchievementDef(
            "lightning_reflex",
            "Lightning Reflex",
            "رد فعل خاطف",
            "Achieve a reaction time under 280ms in Reaction Test.",
            "سجل رد فعل أسرع من 280 ملي ثانية.",
            "⚡",
            120
        ),
        GameAchievementDef(
            "perfect_match",
            "Sharpshooter Memory",
            "ذاكرة فوتوغرافية",
            "Clear Memory Match with exceptional efficiency.",
            "أنهِ لعبة تطابق الذاكرة بأقل عدد حركات.",
            "🎯",
            100
        ),
        GameAchievementDef(
            "math_wizard",
            "Number Wizard",
            "ساحر الأرقام",
            "Score over 1,000 points in Number Sprint.",
            "سجل أكثر من 1000 نقطة في سباق الأرقام.",
            "🔢",
            100
        ),
        GameAchievementDef(
            "pattern_prodigy",
            "Pattern Prodigy",
            "عبقري الأنماط",
            "Solve 8 complex sequence patterns correctly in a row.",
            "حل 8 أنماط متتالية بدون أي خطأ.",
            "🧩",
            120
        ),
        GameAchievementDef(
            "word_smith",
            "Word Smith",
            "صائغ الكلمات",
            "Unscramble a hard or expert word without hints.",
            "رتب كلمة صعبة بدون استخدام أي تلميح.",
            "🔠",
            100
        ),
        GameAchievementDef(
            "hawk_eye",
            "Hawk Eye",
            "عين الصقر",
            "Spot 10 odd items with rapid accuracy.",
            "اكتشف 10 عناصر مختلفة بسرعة ودقة فائقة.",
            "👁️",
            100
        ),
        GameAchievementDef(
            "master_of_logic",
            "Master of Logic",
            "أستاذ المنطق",
            "Crack an Expert or Master puzzle in Logic Lock.",
            "حل لغز منطقي بمستوى خبير أو أستاذ.",
            "🔒",
            150
        ),
        GameAchievementDef(
            "color_mastermind",
            "Color Mastermind",
            "قاهر التشتت",
            "Reach a combo streak of 12 in Color & Word.",
            "حقق تتابع إجابات 12 في لعبة اللون والكلمة.",
            "🎨",
            120
        ),
        GameAchievementDef(
            "quick_mind",
            "Rapid Mind",
            "فكر فوري",
            "Score over 1,200 points in Quick Logic.",
            "سجل أكثر من 1200 نقطة في المنطق السريع.",
            "🚀",
            120
        ),
        GameAchievementDef(
            "expert_ascendant",
            "Expert Ascendant",
            "صاعد الخبراء",
            "Win any game on Expert difficulty.",
            "فز بأي لعبة في مستوى الخبير.",
            "👑",
            150
        ),
        GameAchievementDef(
            "master_of_all",
            "Grand Master",
            "الأستاذ الأعظم",
            "Win any game on Master difficulty.",
            "أكمل أي لعبة بمستوى الأستاذ الأعلى.",
            "💎",
            200
        ),
        GameAchievementDef(
            "record_breaker",
            "Record Breaker",
            "كاسر الأرقام",
            "Set a new personal record.",
            "حطم رقمك القياسي الشخصي السابق.",
            "🏆",
            80
        ),
        GameAchievementDef(
            "triple_record",
            "Relentless Growth",
            "تطور مستمر",
            "Set 3 new personal records across any games.",
            "حطم 3 أرقام قياسية شخصية مختلفة.",
            "🎖️",
            150
        ),
        GameAchievementDef(
            "streak_3",
            "3-Day Momentum",
            "زخم 3 أيام",
            "Maintain a 3-day daily game streak.",
            "حافظ على تتابع اللعب لمدة 3 أيام متتالية.",
            "🔥",
            100
        ),
        GameAchievementDef(
            "streak_7",
            "7-Day Mind Streak",
            "أسبوع العقل المتيقظ",
            "Maintain a 7-day daily game streak.",
            "حافظ على تتابع اللعب لـ 7 أيام كاملة.",
            "🌟",
            200
        ),
        GameAchievementDef(
            "streak_30",
            "30-Day Dedication",
            "ثبات 30 يوماً",
            "Reach a 30-day brain game streak.",
            "حقق تتابع لعب يومي لمدة 30 يوماً.",
            "💫",
            500
        ),
        GameAchievementDef(
            "games_10",
            "Decade Club",
            "نادي العشرة",
            "Play 10 total game sessions.",
            "العب 10 جولات تدريبية إجمالاً.",
            "🎮",
            80
        ),
        GameAchievementDef(
            "games_50",
            "Veteran Thinker",
            "مفكر متمرس",
            "Play 50 total game sessions.",
            "العب 50 جولة تدريبية إجمالاً.",
            "🏅",
            250
        ),
        GameAchievementDef(
            "games_100",
            "Centurion",
            "المئوي",
            "Play 100 total game sessions.",
            "أتمم 100 جولة تدريبية في يومك.",
            "🛡️",
            500
        ),
        GameAchievementDef(
            "level_5",
            "Quick Thinker",
            "مفكر سريع",
            "Reach Personal Brain Level 5.",
            "بلغ المستوى الخامس في تمرين الدماغ.",
            "⭐",
            100
        ),
        GameAchievementDef(
            "level_10",
            "Logic Explorer",
            "مستكشف المنطق",
            "Reach Personal Brain Level 10.",
            "بلغ المستوى العاشر في تمرين الدماغ.",
            "🔮",
            200
        ),
        GameAchievementDef(
            "jack_of_all_trades",
            "All-Round Mind",
            "العقل الشامل",
            "Play every single one of the 10 games at least once.",
            "جرب جميع الألعاب العشر في يومك مرة واحدة على الأقل.",
            "🌈",
            300
        )
    )

    fun getDef(id: String): GameAchievementDef? = allAchievements.firstOrNull { it.id == id }
}
