package com.example.domain.games.engine

import androidx.compose.ui.graphics.Color
import com.example.data.local.model.GameDifficulty
import kotlin.random.Random

// -------------------------------------------------------------
// 1. MEMORY GRID ENGINE
// -------------------------------------------------------------
data class MemoryGridConfig(
    val gridSize: Int, // 3, 4, 5, 6
    val initialSequenceLength: Int,
    val flashDurationMs: Long,
    val pauseBetweenFlashMs: Long
)

object MemoryGridEngine {
    fun getConfig(difficulty: GameDifficulty): MemoryGridConfig = when (difficulty) {
        GameDifficulty.EASY -> MemoryGridConfig(gridSize = 3, initialSequenceLength = 3, flashDurationMs = 550L, pauseBetweenFlashMs = 250L)
        GameDifficulty.MEDIUM -> MemoryGridConfig(gridSize = 4, initialSequenceLength = 4, flashDurationMs = 450L, pauseBetweenFlashMs = 200L)
        GameDifficulty.HARD -> MemoryGridConfig(gridSize = 5, initialSequenceLength = 5, flashDurationMs = 360L, pauseBetweenFlashMs = 170L)
        GameDifficulty.EXPERT -> MemoryGridConfig(gridSize = 5, initialSequenceLength = 6, flashDurationMs = 280L, pauseBetweenFlashMs = 140L)
        GameDifficulty.MASTER -> MemoryGridConfig(gridSize = 6, initialSequenceLength = 7, flashDurationMs = 220L, pauseBetweenFlashMs = 120L)
    }

    fun generateSequence(gridSize: Int, length: Int): List<Int> {
        val totalTiles = gridSize * gridSize
        val result = mutableListOf<Int>()
        var last = -1
        for (i in 0 until length) {
            var next: Int
            do {
                next = Random.nextInt(totalTiles)
            } while (next == last && totalTiles > 1)
            result.add(next)
            last = next
        }
        return result
    }

    fun calculateScore(sequenceLength: Int, difficulty: GameDifficulty, timeTakenSeconds: Int): Int {
        val base = sequenceLength * 120
        val speedBonus = maxOf(0, (30 - timeTakenSeconds) * 10)
        return ((base + speedBonus) * difficulty.multiplier).toInt()
    }
}

// -------------------------------------------------------------
// 2. NUMBER SPRINT ENGINE
// -------------------------------------------------------------
data class MathQuestion(
    val text: String,
    val answer: Int,
    val options: List<Int>,
    val explanation: String
)

object NumberSprintEngine {
    fun generateQuestion(difficulty: GameDifficulty): MathQuestion {
        val op = when (difficulty) {
            GameDifficulty.EASY -> listOf("+", "-").random()
            GameDifficulty.MEDIUM -> listOf("+", "-", "×").random()
            GameDifficulty.HARD -> listOf("+", "-", "×", "÷").random()
            GameDifficulty.EXPERT, GameDifficulty.MASTER -> listOf("+", "-", "×", "÷", "mixed").random()
        }

        var text = ""
        var answer = 0

        when (op) {
            "+" -> {
                val max = when (difficulty) {
                    GameDifficulty.EASY -> 25
                    GameDifficulty.MEDIUM -> 70
                    GameDifficulty.HARD -> 200
                    else -> 500
                }
                val a = Random.nextInt(5, max)
                val b = Random.nextInt(5, max)
                text = "$a + $b"
                answer = a + b
            }
            "-" -> {
                val max = when (difficulty) {
                    GameDifficulty.EASY -> 30
                    GameDifficulty.MEDIUM -> 90
                    GameDifficulty.HARD -> 250
                    else -> 600
                }
                val a = Random.nextInt(10, max)
                val b = Random.nextInt(1, a)
                text = "$a - $b"
                answer = a - b
            }
            "×" -> {
                val maxA = when (difficulty) {
                    GameDifficulty.MEDIUM -> 10
                    GameDifficulty.HARD -> 14
                    GameDifficulty.EXPERT -> 20
                    else -> 35
                }
                val maxB = when (difficulty) {
                    GameDifficulty.MEDIUM -> 9
                    GameDifficulty.HARD -> 12
                    else -> 15
                }
                val a = Random.nextInt(3, maxA)
                val b = Random.nextInt(2, maxB)
                text = "$a × $b"
                answer = a * b
            }
            "÷" -> {
                val divisor = Random.nextInt(2, if (difficulty >= GameDifficulty.EXPERT) 16 else 11)
                val quotient = Random.nextInt(3, if (difficulty >= GameDifficulty.EXPERT) 25 else 13)
                val dividend = divisor * quotient
                text = "$dividend ÷ $divisor"
                answer = quotient
            }
            else -> { // mixed
                val a = Random.nextInt(5, 20)
                val b = Random.nextInt(2, 6)
                val c = Random.nextInt(4, 25)
                text = "($a × $b) + $c"
                answer = (a * b) + c
            }
        }

        val distractors = mutableSetOf<Int>()
        val offsets = listOf(-2, -1, 1, 2, -10, 10, -5, 5, -3, 3)
        var tries = 0
        while (distractors.size < 3 && tries < 30) {
            tries++
            val dist = answer + offsets.random()
            if (dist != answer && dist >= 0) {
                distractors.add(dist)
            }
        }
        while (distractors.size < 3) {
            val dist = answer + (distractors.size + 1) * 3
            distractors.add(dist)
        }

        val options = (distractors + answer).shuffled()
        return MathQuestion(text = "$text = ?", answer = answer, options = options, explanation = "$text = $answer")
    }

    fun calculatePoints(combo: Int, difficulty: GameDifficulty, timeRemainingRatio: Float): Int {
        val base = 100
        val comboMult = 1.0f + (combo.coerceAtMost(10) * 0.15f)
        val speedMult = 0.5f + (timeRemainingRatio * 0.5f)
        return (base * comboMult * speedMult * difficulty.multiplier).toInt()
    }
}

// -------------------------------------------------------------
// 3. PATTERN MASTER ENGINE
// -------------------------------------------------------------
data class PatternPuzzle(
    val sequence: List<String>,
    val answer: String,
    val options: List<String>,
    val ruleExplanationEn: String,
    val ruleExplanationAr: String
)

object PatternMasterEngine {
    fun generatePuzzle(difficulty: GameDifficulty): PatternPuzzle {
        val type = Random.nextInt(5)
        return when (type) {
            0 -> {
                // Arithmetic progression
                val start = Random.nextInt(1, 20)
                val step = when (difficulty) {
                    GameDifficulty.EASY -> Random.nextInt(2, 6)
                    GameDifficulty.MEDIUM -> Random.nextInt(6, 15)
                    else -> Random.nextInt(12, 35)
                }
                val seq = (0..3).map { (start + it * step).toString() }
                val nextVal = start + 4 * step
                val distractors = listOf(nextVal - step, nextVal + step, nextVal + 2, nextVal - 3)
                    .filter { it != nextVal }.distinct().take(3)
                val options = (distractors + nextVal).shuffled().map { it.toString() }
                PatternPuzzle(
                    sequence = seq + "?",
                    answer = nextVal.toString(),
                    options = options,
                    ruleExplanationEn = "Rule: Add $step each step.",
                    ruleExplanationAr = "القاعدة: زيادة بمقدار $step في كل خطوة."
                )
            }
            1 -> {
                // Geometric progression
                val start = Random.nextInt(1, 4)
                val ratio = when (difficulty) {
                    GameDifficulty.EASY -> 2
                    GameDifficulty.MEDIUM -> 3
                    else -> Random.nextInt(2, 4)
                }
                val seq = (0..3).map { (start * Math.pow(ratio.toDouble(), it.toDouble()).toInt()).toString() }
                val nextVal = (start * Math.pow(ratio.toDouble(), 4.0)).toInt()
                val distractors = listOf(nextVal / ratio + 5, nextVal + ratio * 2, nextVal - ratio, nextVal * 2)
                    .filter { it != nextVal && it > 0 }.distinct().take(3)
                val options = (distractors + nextVal).shuffled().map { it.toString() }
                PatternPuzzle(
                    sequence = seq + "?",
                    answer = nextVal.toString(),
                    options = options,
                    ruleExplanationEn = "Rule: Multiply by $ratio each step.",
                    ruleExplanationAr = "القاعدة: ضرب في $ratio في كل خطوة."
                )
            }
            2 -> {
                // Quadratic / Increasing step
                val start = Random.nextInt(1, 6)
                var current = start
                val stepStart = Random.nextInt(2, 5)
                val seq = mutableListOf<String>()
                for (i in 0..3) {
                    seq.add(current.toString())
                    current += (stepStart + i * 2)
                }
                val nextVal = current
                val distractors = listOf(nextVal - 4, nextVal + 3, nextVal + 6).distinct()
                val options = (distractors + nextVal).shuffled().map { it.toString() }
                PatternPuzzle(
                    sequence = seq + "?",
                    answer = nextVal.toString(),
                    options = options,
                    ruleExplanationEn = "Rule: The step between numbers increases by 2 each time.",
                    ruleExplanationAr = "القاعدة: الزيادة بين الأعداد تتضاعف بمقدار 2 كل خطوة."
                )
            }
            3 -> {
                // Alternating +X, -Y
                val a = Random.nextInt(10, 30)
                val plus = Random.nextInt(5, 12)
                val minus = Random.nextInt(2, 5)
                val seqList = mutableListOf(a)
                seqList.add(seqList.last() + plus)
                seqList.add(seqList.last() - minus)
                seqList.add(seqList.last() + plus)
                val nextVal = seqList.last() - minus
                val distractors = listOf(nextVal + plus, nextVal - minus, nextVal + 2).distinct()
                val options = (distractors + nextVal).shuffled().map { it.toString() }
                PatternPuzzle(
                    sequence = seqList.map { it.toString() } + "?",
                    answer = nextVal.toString(),
                    options = options,
                    ruleExplanationEn = "Rule: Alternates between adding $plus and subtracting $minus.",
                    ruleExplanationAr = "القاعدة: إضافة $plus ثم طرح $minus بالتناوب."
                )
            }
            else -> {
                // Symbol pattern
                val symbols = listOf("▲", "■", "●", "◆", "★")
                val p1 = symbols[0]
                val p2 = symbols[1]
                val p3 = symbols[2]
                val seq = listOf(p1, p2, p3, p1, p2)
                val nextVal = p3
                val options = listOf(p1, p2, p3, symbols[3]).shuffled()
                PatternPuzzle(
                    sequence = seq + "?",
                    answer = nextVal,
                    options = options,
                    ruleExplanationEn = "Rule: Repeating symbol cycle ($p1, $p2, $p3).",
                    ruleExplanationAr = "القاعدة: دورة أشكال هندسية متكررة ($p1، $p2، $p3)."
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 4. REACTION TEST ENGINE
// -------------------------------------------------------------
enum class ReactionGrade(val labelEn: String, val labelAr: String, val colorHex: String) {
    EXCELLENT("Lightning Fast", "رد فعل خاطف", "#10B981"),
    FAST("Excellent Speed", "سرعة ممتازة", "#3B82F6"),
    AVERAGE("Average Reaction", "معدل طبيعي", "#F59E0B"),
    SLOW("Practice Needed", "واصل التمرين", "#EF4444")
}

object ReactionTestEngine {
    fun getRandomWaitDelayMs(): Long = Random.nextLong(1800L, 4500L)

    fun evaluateReaction(ms: Long): ReactionGrade = when {
        ms < 270L -> ReactionGrade.EXCELLENT
        ms < 350L -> ReactionGrade.FAST
        ms < 460L -> ReactionGrade.AVERAGE
        else -> ReactionGrade.SLOW
    }

    fun calculateScore(bestMs: Long, avgMs: Long, difficulty: GameDifficulty): Int {
        if (bestMs <= 0 || avgMs <= 0) return 0
        val baseScore = maxOf(0, (600 - avgMs.toInt()) * 4)
        val peakBonus = maxOf(0, (500 - bestMs.toInt()) * 2)
        return ((baseScore + peakBonus) * difficulty.multiplier).toInt()
    }
}

// -------------------------------------------------------------
// 5. MEMORY MATCH ENGINE
// -------------------------------------------------------------
data class MatchCard(
    val id: Int,
    val pairKey: String,
    val iconName: String,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false
)

object MemoryMatchEngine {
    private val iconSet = listOf(
        "psychology", "star", "bolt", "favorite", "rocket_launch",
        "lightbulb", "local_fire_department", "explore", "flag", "shield",
        "extension", "auto_awesome", "diamond", "lock", "pets"
    )

    fun getPairsCount(difficulty: GameDifficulty): Int = when (difficulty) {
        GameDifficulty.EASY -> 4 // 8 cards
        GameDifficulty.MEDIUM -> 6 // 12 cards
        GameDifficulty.HARD -> 10 // 20 cards
        GameDifficulty.EXPERT -> 12 // 24 cards
        GameDifficulty.MASTER -> 15 // 30 cards
    }

    fun generateCards(difficulty: GameDifficulty): List<MatchCard> {
        val pairsCount = getPairsCount(difficulty)
        val selectedIcons = iconSet.shuffled().take(pairsCount)
        val cards = mutableListOf<MatchCard>()
        var id = 0
        for (icon in selectedIcons) {
            cards.add(MatchCard(id = id++, pairKey = icon, iconName = icon))
            cards.add(MatchCard(id = id++, pairKey = icon, iconName = icon))
        }
        return cards.shuffled()
    }

    fun calculateScore(pairs: Int, moves: Int, timeSeconds: Int, difficulty: GameDifficulty): Int {
        val optimalMoves = pairs * 2
        val moveEfficiency = maxOf(0, (optimalMoves * 2 - moves) * 50)
        val timeEfficiency = maxOf(0, (120 - timeSeconds) * 10)
        val base = pairs * 200
        return ((base + moveEfficiency + timeEfficiency) * difficulty.multiplier).toInt()
    }
}

// -------------------------------------------------------------
// 6. WORD SCRAMBLE ENGINE
// -------------------------------------------------------------
data class ScrambleWord(
    val word: String,
    val hintEn: String,
    val hintAr: String,
    val difficulty: GameDifficulty
)

object WordScrambleEngine {
    private val englishWords = listOf(
        ScrambleWord("PLAN", "A set of decisions for the future", "مجموعة قرارات للمستقبل", GameDifficulty.EASY),
        ScrambleWord("TIME", "The most precious non-renewable asset", "أثمن مورد لا يتجدد", GameDifficulty.EASY),
        ScrambleWord("FOCUS", "Concentrating all mental energy", "توجيه كامل الطاقة الذهنية", GameDifficulty.EASY),
        ScrambleWord("SMART", "Specific, measurable, and sharp", "محدد وقابل للقياس", GameDifficulty.EASY),
        ScrambleWord("HABIT", "A routine repeated until automatic", "سلوك يتكرر حتى يصير تلقائياً", GameDifficulty.EASY),
        ScrambleWord("GOALS", "Target aspirations to accomplish", "طموحات تسعى لتحقيقها", GameDifficulty.MEDIUM),
        ScrambleWord("ENERGY", "Vitality needed for daily action", "الحيوية اللازمة للإنجاز", GameDifficulty.MEDIUM),
        ScrambleWord("CLARITY", "Clearness of thought and vision", "وضوح الرؤية والتفكير", GameDifficulty.MEDIUM),
        ScrambleWord("ROUTINE", "A sequence of structured actions", "تسلسل منظم للمهام اليومية", GameDifficulty.HARD),
        ScrambleWord("ACHIEVE", "Successfully reaching a milestone", "الوصول بنجاح إلى الهدف", GameDifficulty.HARD),
        ScrambleWord("BALANCE", "Equilibrium between life areas", "توازن سليم بين جوانب الحياة", GameDifficulty.HARD),
        ScrambleWord("MINDFUL", "Aware and present in the moment", "حاضر الذهن وواعي باللحظة", GameDifficulty.EXPERT),
        ScrambleWord("DISCIPLINE", "Consistency over fleeting impulse", "الالتزام فوق المشاعر المؤقتة", GameDifficulty.MASTER)
    )

    private val arabicWords = listOf(
        ScrambleWord("هدف", "طموح يسعى الإنسان لتحقيقه", "Target goal", GameDifficulty.EASY),
        ScrambleWord("وقت", "أثمن ما يملكه الإنسان في يومه", "Precious time", GameDifficulty.EASY),
        ScrambleWord("عقل", "جوهر التفكير والتدبر", "Intellect and mind", GameDifficulty.EASY),
        ScrambleWord("خطة", "رسم منظم لطريق الإنجاز", "Structured plan", GameDifficulty.EASY),
        ScrambleWord("تركيز", "حصر الانتباه في مهمة واحدة", "Deep focus", GameDifficulty.MEDIUM),
        ScrambleWord("إنجاز", "إتمام عمل ذي قيمة", "Accomplishment", GameDifficulty.MEDIUM),
        ScrambleWord("تخطيط", "تنظيم المهام قبل التنفيذ", "Organized planning", GameDifficulty.MEDIUM),
        ScrambleWord("معرفة", "نور الفكر وحصيلة التعلم", "Knowledge", GameDifficulty.HARD),
        ScrambleWord("انضباط", "الالتزام بالواجب دون تسويف", "Self discipline", GameDifficulty.HARD),
        ScrambleWord("استمرار", "سر النجاح في العادات", "Consistency", GameDifficulty.EXPERT),
        ScrambleWord("إنتاجية", "إنجاز المزيد بأعلى كفاءة", "Productivity", GameDifficulty.MASTER)
    )

    fun getWord(isArabic: Boolean, difficulty: GameDifficulty): ScrambleWord {
        val pool = if (isArabic) arabicWords else englishWords
        val matching = pool.filter { it.difficulty == difficulty }
        return if (matching.isNotEmpty()) matching.random() else pool.random()
    }

    fun scramble(word: String): String {
        if (word.length <= 2) return word
        var scrambled: String
        var attempts = 0
        do {
            scrambled = word.toList().shuffled().joinToString("")
            attempts++
        } while (scrambled == word && attempts < 20)
        return scrambled
    }

    fun calculateScore(wordLength: Int, timeSeconds: Int, hintsUsed: Int, difficulty: GameDifficulty): Int {
        val base = wordLength * 150
        val speedBonus = maxOf(0, (40 - timeSeconds) * 10)
        val hintPenalty = hintsUsed * 60
        return (maxOf(50, (base + speedBonus - hintPenalty)) * difficulty.multiplier).toInt()
    }
}

// -------------------------------------------------------------
// 7. ODD ONE OUT ENGINE
// -------------------------------------------------------------
data class OddOneOutRound(
    val items: List<OddItem>,
    val correctIndex: Int,
    val promptEn: String,
    val promptAr: String
)

data class OddItem(
    val id: Int,
    val text: String = "",
    val iconKey: String = "",
    val color: Color = Color.Unspecified
)

object OddOneOutEngine {
    fun generateRound(difficulty: GameDifficulty): OddOneOutRound {
        val gridSize = when (difficulty) {
            GameDifficulty.EASY -> 3 // 9 items
            GameDifficulty.MEDIUM -> 4 // 16 items
            GameDifficulty.HARD -> 4 // 16 items
            GameDifficulty.EXPERT -> 5 // 25 items
            GameDifficulty.MASTER -> 5 // 25 items
        }
        val count = gridSize * gridSize
        val correctIndex = Random.nextInt(count)
        val mode = Random.nextInt(3)

        val items = mutableListOf<OddItem>()

        when (mode) {
            0 -> {
                // Icon Difference
                val commonIcon = listOf("star", "favorite", "circle", "square").random()
                val oddIcon = listOf("bolt", "diamond", "change_history", "emergency").random()
                for (i in 0 until count) {
                    items.add(
                        OddItem(
                            id = i,
                            iconKey = if (i == correctIndex) oddIcon else commonIcon,
                            color = Color(0xFF3B82F6)
                        )
                    )
                }
                return OddOneOutRound(
                    items = items,
                    correctIndex = correctIndex,
                    promptEn = "Find the different symbol!",
                    promptAr = "ابحث عن الرمز المختلف!"
                )
            }
            1 -> {
                // Subtle Color Difference
                val baseRed = 59
                val baseGreen = 130
                val baseBlue = 246
                val delta = when (difficulty) {
                    GameDifficulty.EASY -> 60
                    GameDifficulty.MEDIUM -> 40
                    GameDifficulty.HARD -> 25
                    GameDifficulty.EXPERT -> 16
                    GameDifficulty.MASTER -> 10
                }
                val baseColor = Color(baseRed, baseGreen, baseBlue)
                val oddColor = Color(baseRed + delta, baseGreen + delta / 2, baseBlue - delta / 2)
                for (i in 0 until count) {
                    items.add(
                        OddItem(
                            id = i,
                            color = if (i == correctIndex) oddColor else baseColor
                        )
                    )
                }
                return OddOneOutRound(
                    items = items,
                    correctIndex = correctIndex,
                    promptEn = "Spot the slightly different shade!",
                    promptAr = "اكتشف المربع بدرجة اللون المختلفة!"
                )
            }
            else -> {
                // Number Property Difference (e.g. all even, 1 odd)
                val isBaseEven = Random.nextBoolean()
                for (i in 0 until count) {
                    val num = if (i == correctIndex) {
                        if (isBaseEven) (Random.nextInt(1, 45) * 2) + 1 else Random.nextInt(1, 45) * 2
                    } else {
                        if (isBaseEven) Random.nextInt(1, 45) * 2 else (Random.nextInt(1, 45) * 2) + 1
                    }
                    items.add(OddItem(id = i, text = num.toString(), color = Color(0xFF1E293B)))
                }
                val promptEn = if (isBaseEven) "Find the only ODD number!" else "Find the only EVEN number!"
                val promptAr = if (isBaseEven) "ابحث عن العدد الفردي الوحيد!" else "ابحث عن العدد الزوجي الوحيد!"
                return OddOneOutRound(items = items, correctIndex = correctIndex, promptEn = promptEn, promptAr = promptAr)
            }
        }
    }
}

// -------------------------------------------------------------
// 8. LOGIC LOCK ENGINE
// -------------------------------------------------------------
data class LogicLockPuzzle(
    val questionEn: String,
    val questionAr: String,
    val optionsEn: List<String>,
    val optionsAr: List<String>,
    val correctIndex: Int,
    val explanationEn: String,
    val explanationAr: String
)

object LogicLockEngine {
    private val puzzles = listOf(
        LogicLockPuzzle(
            questionEn = "A farmer has 17 sheep. All but 9 run away. How many sheep does the farmer have left?",
            questionAr = "مزارع يملك 17 خروفاً. هربت جميعها إلا 9 خراف. كم خروفاً بقي مع المزارع؟",
            optionsEn = listOf("8", "9", "17", "0"),
            optionsAr = listOf("8", "9", "17", "0"),
            correctIndex = 1,
            explanationEn = "\"All but 9\" means exactly 9 sheep remained on the farm.",
            explanationAr = "عبارة \"إلا 9\" تعني صراحة أن 9 خراف هي التي لم تهرب وبقيت."
        ),
        LogicLockPuzzle(
            questionEn = "Tarek is taller than Omar, but shorter than Kareem. Ziad is taller than Kareem. Who is the tallest?",
            questionAr = "طارق أطول من عمر، لكنه أقصر من كريم. وزياد أطول من كريم. من هو الأطول بينهم؟",
            optionsEn = listOf("Omar", "Tarek", "Kareem", "Ziad"),
            optionsAr = listOf("عمر", "طارق", "كريم", "زياد"),
            correctIndex = 3,
            explanationEn = "Ordering: Omar < Tarek < Kareem < Ziad. Thus Ziad is tallest.",
            explanationAr = "الترتيب بالترتيب: عمر < طارق < كريم < زياد. إذن زياد هو الأطول."
        ),
        LogicLockPuzzle(
            questionEn = "A clock shows 3:15. If both hands rotate 90 degrees clockwise, what time does the minute hand point to?",
            questionAr = "الساعة تشير إلى 3:15. إذا دار عقرب الدقائق 90 درجة باتجاه عقارب الساعة، فإلى أي دقيقة سيشير؟",
            optionsEn = listOf("Minute 15", "Minute 30", "Minute 45", "Minute 60"),
            optionsAr = listOf("الدقيقة 15", "الدقيقة 30", "الدقيقة 45", "الدقيقة 60"),
            correctIndex = 1,
            explanationEn = "90 degrees represents 1/4 of a circle (15 minutes). 15 + 15 = minute 30.",
            explanationAr = "90 درجة تمثل ربع دائرة (15 دقيقة). 15 + 15 = الدقيقة 30."
        ),
        LogicLockPuzzle(
            questionEn = "3-Digit Code Clues:\n• 682: One digit is correct and in the right place.\n• 614: One digit is correct but in the wrong place.\n• 206: Two digits are correct but in wrong places.\nWhat is the 3-digit code?",
            questionAr = "أدلة قفل ثلاثي:\n• 682: رقم واحد صحيح وفي مكانه الصحيح.\n• 614: رقم واحد صحيح ولكن في مكان خاطئ.\n• 206: رقمان صحيحان ولكن في أماكن خاطئة.\nما هو الرمز الصحيح؟",
            optionsEn = listOf("042", "062", "052", "602"),
            optionsAr = listOf("042", "062", "052", "602"),
            correctIndex = 0,
            explanationEn = "6 is eliminated. From 682, 2 is in 3rd place. From 206, 0 is first. From 614, 4 is second -> 042.",
            explanationAr = "استبعاد الرقم 6. من 682 الرقم 2 في الخانة الأخيرة، ومن 206 الرقم 0 أولاً، ومن 614 الرقم 4 ثانياً -> 042."
        ),
        LogicLockPuzzle(
            questionEn = "If 5 machines take 5 minutes to make 5 widgets, how many minutes do 100 machines take to make 100 widgets?",
            questionAr = "إذا كانت 5 آلات تحتاج 5 دقائق لصنع 5 قطع، فكم دقيقة تحتاج 100 آلة لصنع 100 قطعة؟",
            optionsEn = listOf("100 minutes", "5 minutes", "20 minutes", "1 minute"),
            optionsAr = listOf("100 دقيقة", "5 دقائق", "20 دقيقة", "دقيقة واحدة"),
            correctIndex = 1,
            explanationEn = "Each machine takes 5 minutes to make 1 widget. Thus 100 machines simultaneously make 100 widgets in 5 minutes.",
            explanationAr = "كل آلة واحدة تحتاج 5 دقائق لصنع قطعة واحدة. إذن 100 آلة تعمل معاً وتنتج 100 قطعة في نفس الـ 5 دقائق."
        )
    )

    fun getPuzzle(difficulty: GameDifficulty, roundIndex: Int): LogicLockPuzzle {
        return puzzles[roundIndex % puzzles.size]
    }
}

// -------------------------------------------------------------
// 9. COLOR & WORD (STROOP) ENGINE
// -------------------------------------------------------------
data class ColorWordItem(
    val wordTextEn: String,
    val wordTextAr: String,
    val inkColor: Color,
    val inkColorNameEn: String,
    val inkColorNameAr: String
)

data class ColorWordRound(
    val item: ColorWordItem,
    val instructionMode: StroopMode,
    val optionsEn: List<String>,
    val optionsAr: List<String>,
    val correctIndex: Int
)

enum class StroopMode(val promptEn: String, val promptAr: String) {
    MATCH_INK("Select the INK COLOR", "اختر لون الحبر الظاهر"),
    MATCH_WORD("Select the WRITTEN WORD", "اختر الكلمة المكتوبة")
}

object ColorAndWordEngine {
    private val colorCatalog = listOf(
        Triple("RED", "أحمر", Color(0xFFEF4444)),
        Triple("BLUE", "أزرق", Color(0xFF3B82F6)),
        Triple("GREEN", "أخضر", Color(0xFF10B981)),
        Triple("YELLOW", "أصفر", Color(0xFFF59E0B)),
        Triple("PURPLE", "بنفسجي", Color(0xFF8B5CF6))
    )

    fun generateRound(difficulty: GameDifficulty): ColorWordRound {
        val wordEntry = colorCatalog.random()
        var inkEntry = colorCatalog.random()
        if (difficulty >= GameDifficulty.MEDIUM && inkEntry == wordEntry) {
            inkEntry = colorCatalog.filter { it != wordEntry }.random()
        }

        val mode = if (difficulty == GameDifficulty.EASY) {
            StroopMode.MATCH_INK
        } else {
            if (Random.nextBoolean()) StroopMode.MATCH_INK else StroopMode.MATCH_WORD
        }

        val correctNameEn = if (mode == StroopMode.MATCH_INK) inkEntry.first else wordEntry.first
        val correctNameAr = if (mode == StroopMode.MATCH_INK) inkEntry.second else wordEntry.second

        val otherOptions = colorCatalog.filter { it.first != correctNameEn }.shuffled().take(3)
        val allEn = (otherOptions.map { it.first } + correctNameEn).shuffled()
        val correctIndex = allEn.indexOf(correctNameEn)
        val allAr = allEn.map { en -> colorCatalog.first { it.first == en }.second }

        val item = ColorWordItem(
            wordTextEn = wordEntry.first,
            wordTextAr = wordEntry.second,
            inkColor = inkEntry.third,
            inkColorNameEn = inkEntry.first,
            inkColorNameAr = inkEntry.second
        )

        return ColorWordRound(
            item = item,
            instructionMode = mode,
            optionsEn = allEn,
            optionsAr = allAr,
            correctIndex = correctIndex
        )
    }
}

// -------------------------------------------------------------
// 10. QUICK LOGIC ENGINE
// -------------------------------------------------------------
data class QuickLogicItem(
    val questionEn: String,
    val questionAr: String,
    val optionsEn: List<String>,
    val optionsAr: List<String>,
    val correctIndex: Int
)

object QuickLogicEngine {
    private val questions = listOf(
        QuickLogicItem(
            questionEn = "Which shape completes the relationship: Circle is to Sphere as Square is to...?",
            questionAr = "أي شكل يكمل التشبيه: الدائرة بالنسبة للمجسم كروي كالمربع بالنسبة لـ...؟",
            optionsEn = listOf("Cube", "Triangle", "Pyramid", "Rectangle"),
            optionsAr = listOf("المكعب", "المثلث", "الهرم", "المستطيل"),
            correctIndex = 0
        ),
        QuickLogicItem(
            questionEn = "If today is Wednesday, what day was it 4 days ago?",
            questionAr = "إذا كان اليوم الأربعاء، فماذا كان اليوم قبل 4 أيام؟",
            optionsEn = listOf("Saturday", "Sunday", "Monday", "Tuesday"),
            optionsAr = listOf("السبت", "الأحد", "الإثنين", "الثلاثاء"),
            correctIndex = 0
        ),
        QuickLogicItem(
            questionEn = "Which of the following does NOT belong in the group?",
            questionAr = "أي من الآتي لا ينتمي إلى نفس المجموعة؟",
            optionsEn = listOf("Apple", "Carrot", "Banana", "Grape"),
            optionsAr = listOf("التفاح", "الجزر", "الموز", "العنب"),
            correctIndex = 1
        ),
        QuickLogicItem(
            questionEn = "What is the missing number in the sequence: 3, 9, 27, 81, ...?",
            questionAr = "ما هو العدد المفقود في المتتالية: 3، 9، 27، 81، ...؟",
            optionsEn = listOf("162", "243", "324", "198"),
            optionsAr = listOf("162", "243", "324", "198"),
            correctIndex = 1
        ),
        QuickLogicItem(
            questionEn = "Book is to Reading as Fork is to...?",
            questionAr = "الكتاب بالنسبة للقراءة كالمعلقة أو الشوكة بالنسبة لـ...؟",
            optionsEn = listOf("Writing", "Eating", "Cooking", "Drinking"),
            optionsAr = listOf("الكتابة", "تناول الطعام", "الطهي", "الشرب"),
            correctIndex = 1
        )
    )

    fun getQuestion(round: Int): QuickLogicItem {
        return questions[round % questions.size]
    }
}
