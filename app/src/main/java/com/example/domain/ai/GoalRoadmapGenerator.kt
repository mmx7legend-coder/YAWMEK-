package com.example.domain.ai

import com.example.data.local.model.HabitFrequency
import com.example.data.local.model.Priority
import java.util.regex.Pattern

object GoalRoadmapGenerator {

    fun generateRoadmap(prompt: String, isArabic: Boolean = false): GoalRoadmapProposal {
        val lower = prompt.lowercase()

        // Extract days if mentioned (e.g., "in 60 days", "في 60 يوم", "30 days", "شهرين")
        val days = extractDays(prompt) ?: 60

        return when {
            lower.contains("python") || lower.contains("بايثون") || lower.contains("code") || lower.contains("برمجة") || lower.contains("developer") -> {
                generateCodingRoadmap(days, isArabic, "Python")
            }
            lower.contains("fitness") || lower.contains("gym") || lower.contains("جيم") || lower.contains("رياضة") || lower.contains("وزن") || lower.contains("run") || lower.contains("جري") -> {
                generateFitnessRoadmap(days, isArabic)
            }
            lower.contains("english") || lower.contains("إنجليزي") || lower.contains("language") || lower.contains("لغة") || lower.contains("ألماني") || lower.contains("spanish") -> {
                generateLanguageRoadmap(days, isArabic)
            }
            lower.contains("business") || lower.contains("مشروع") || lower.contains("متجر") || lower.contains("store") || lower.contains("freelance") || lower.contains("عمل حر") -> {
                generateBusinessRoadmap(days, isArabic)
            }
            else -> {
                generateGeneralRoadmap(prompt, days, isArabic)
            }
        }
    }

    private fun extractDays(prompt: String): Int? {
        val p = Pattern.compile("(\\d+)\\s*(days|day|يوم|ايام|أيام)")
        val m = p.matcher(prompt.lowercase())
        if (m.find()) {
            val num = m.group(1)?.toIntOrNull()
            if (num != null && num in 7..365) return num
        }
        if (prompt.contains("شهرين") || prompt.contains("2 months")) return 60
        if (prompt.contains("شهر") || prompt.contains("month") || prompt.contains("30")) return 30
        if (prompt.contains("3 months") || prompt.contains("٣ شهور") || prompt.contains("3 شهور")) return 90
        return null
    }

    private fun generateCodingRoadmap(days: Int, isArabic: Boolean, tech: String): GoalRoadmapProposal {
        val title = if (isArabic) "إتقان برمجة $tech وتطوير مشاريع عملية" else "Master $tech Programming & Build Real Projects"
        val desc = if (isArabic) "خطة متكاملة ومكثفة لتعلم $tech وتطبيقه على مشاريع حقيقية خلال $days يوماً."
        else "Comprehensive roadmap to build strong foundations and ship portfolio projects in $days days."

        val phases = listOf(
            RoadmapPhase(
                title = if (isArabic) "المرحلة الأولى: الأساسيات والمنطق البرمجي" else "Phase 1: Foundations & Core Syntax",
                dayRange = "Day 1 - Day ${days / 3}",
                description = if (isArabic) "فهم المتغيرات، الدوال، الحلقات التكرارية، وهياكل البيانات الأساسية."
                else "Variables, control flow, functions, and fundamental data structures."
            ),
            RoadmapPhase(
                title = if (isArabic) "المرحلة الثانية: البرمجة كائنية التوجه والمكتبات" else "Phase 2: OOP & Core Libraries",
                dayRange = "Day ${(days / 3) + 1} - Day ${(days * 2) / 3}",
                description = if (isArabic) "الفئات والوراثة، التعامل مع الملفات، ومعالجة البيانات."
                else "Object-Oriented design, modules, file I/O, and external packages."
            ),
            RoadmapPhase(
                title = if (isArabic) "المرحلة الثالثة: مشروع التخرج ومعرض الأعمال" else "Phase 3: Capstone Project & Portfolio",
                dayRange = "Day ${((days * 2) / 3) + 1} - Day $days",
                description = if (isArabic) "بناء مشروع متكامل من الصفر، رفع الكود على GitHub، وتجهيز السيرة الذاتية."
                else "Build end-to-end applications, commit to GitHub, and document your learnings."
            )
        )

        val milestones = listOf(
            RoadmapMilestone(
                title = if (isArabic) "إنهاء أساسيات المنطق البرمجي وكتابة ٥ برامج تفاعلية" else "Complete Syntax Foundations & 5 Mini-Scripts",
                targetDayOffset = days / 4,
                phaseTitle = phases[0].title
            ),
            RoadmapMilestone(
                title = if (isArabic) "إتقان OOP والتعامل مع واجهات البرمجة (APIs)" else "Master OOP & API Integration",
                targetDayOffset = days / 2,
                phaseTitle = phases[1].title
            ),
            RoadmapMilestone(
                title = if (isArabic) "إطلاق مشروع التخرج الكامل ونشره" else "Launch & Publish Full Capstone Project",
                targetDayOffset = days,
                phaseTitle = phases[2].title
            )
        )

        val tasks = listOf(
            RoadmapTask(
                title = if (isArabic) "تثبيت بيئة التطوير وتشغيل أول سكريبت $tech" else "Setup IDE & Run First $tech Program",
                durationMinutes = 45,
                priority = Priority.HIGH,
                subtasks = listOf(
                    if (isArabic) "تنزيل المحرر ومفسر اللغة" else "Install IDE & Compiler/Interpreter",
                    if (isArabic) "كتابة برنامج تفاعلي لاختبار المدخلات" else "Write interactive command-line test script",
                    if (isArabic) "إنشاء مستودع Git محلي" else "Initialize local Git repository"
                ),
                targetDayOffset = 1
            ),
            RoadmapTask(
                title = if (isArabic) "حل ٥ مسائل برمجية على القوائم والقواميس" else "Solve 5 Challenges on Lists & Dictionaries",
                durationMinutes = 40,
                priority = Priority.HIGH,
                subtasks = listOf(
                    if (isArabic) "فهم طرق التكرار والفرز" else "Understand iteration & sorting",
                    if (isArabic) "توثيق الحلول في ملاحظات يومك" else "Log key patterns in YAWMEK notes"
                ),
                targetDayOffset = 3
            ),
            RoadmapTask(
                title = if (isArabic) "تصميم كلاس لمشروع إدارة المهام أو جهات الاتصال" else "Design OOP Classes for CLI Project",
                durationMinutes = 60,
                priority = Priority.MEDIUM,
                subtasks = listOf(
                    if (isArabic) "تحديد الخصائص والتوابع" else "Define properties and methods",
                    if (isArabic) "معالجة الأخطاء والمدخلات غير الصحيحة" else "Handle exceptions and edge cases"
                ),
                targetDayOffset = 7
            )
        )

        val habitTitle = if (isArabic) "ممارسة البرمجة لمدة ٤٥ دقيقة يومياً" else "Daily 45-Min Focused Coding"
        val recovery = if (isArabic)
            "إذا فاتك يوم أو يومان، لا تيأس! قم بجلسة مراجعة مصغرة مدتها ٢٠ دقيقة لحل مسألة واحدة لتستعيد الزخم فوراً."
        else
            "If you miss 1-2 days, do not reset! Run a lightweight 20-minute recovery sprint with 1 mini-problem to restore momentum."

        return GoalRoadmapProposal(
            goalTitle = title,
            description = desc,
            category = "Education",
            totalDays = days,
            phases = phases,
            milestones = milestones,
            tasks = tasks,
            recommendedHabitTitle = habitTitle,
            recommendedHabitFrequency = HabitFrequency.DAILY,
            recoveryStrategy = recovery
        )
    }

    private fun generateFitnessRoadmap(days: Int, isArabic: Boolean): GoalRoadmapProposal {
        val title = if (isArabic) "بناء اللياقة البدنية والوصول لأفضل لياقة" else "Build Peak Physical Fitness & Athletic Stamina"
        val desc = if (isArabic) "خطة تدريب وتغذية متوازنة على مدار $days يوماً لزيادة النشاط والتحمل."
        else "Structured progressive training & nutrition roadmap over $days days."

        val phases = listOf(
            RoadmapPhase(
                title = if (isArabic) "المرحلة الأولى: تهيئة العضلات والتحمل الهوائي" else "Phase 1: Conditioning & Aerobic Base",
                dayRange = "Day 1 - Day ${days / 3}",
                description = if (isArabic) "تمارين الإحماء، المشي السريع أو الجري الخفيف، وتمارين وزن الجسم."
                else "Mobility, aerobic foundation, and fundamental bodyweight form."
            ),
            RoadmapPhase(
                title = if (isArabic) "المرحلة الثانية: التكثيف التدريجي وزيادة القوة" else "Phase 2: Progressive Overload & Strength",
                dayRange = "Day ${(days / 3) + 1} - Day ${(days * 2) / 3}",
                description = if (isArabic) "زيادة الأوزان وعدد التكرارات وتنظيم السعرات والبروتين."
                else "Increase intensity, track progression, and optimize nutrition."
            ),
            RoadmapPhase(
                title = if (isArabic) "المرحلة الثالثة: تثبيت العادات والوصول للمستوى المنشود" else "Phase 3: Peak Performance & Lifestyle Lock",
                dayRange = "Day ${((days * 2) / 3) + 1} - Day $days",
                description = if (isArabic) "تقييم القياسات البدنية وتحقيق أرقام قياسية شخصية."
                else "Final measurement check, personal best tests, and sustainable habits."
            )
        )

        val milestones = listOf(
            RoadmapMilestone(
                title = if (isArabic) "إتمام أسبوعين كاملين بدون تفويت أي تمرين" else "Complete 14 Days Flawless Workout Consistency",
                targetDayOffset = 14,
                phaseTitle = phases[0].title
            ),
            RoadmapMilestone(
                title = if (isArabic) "تحقيق زيادة بنسبة ٢٥٪ في قوة التحمل أو الجري لمسافة ٥ كم" else "Achieve 25% Increase in Stamina / 5K Run",
                targetDayOffset = days / 2,
                phaseTitle = phases[1].title
            ),
            RoadmapMilestone(
                title = if (isArabic) "تحقيق الوزن المستهدف ونسبة الدهون المثالية" else "Hit Target Body Composition & Weight Goal",
                targetDayOffset = days,
                phaseTitle = phases[2].title
            )
        )

        val tasks = listOf(
            RoadmapTask(
                title = if (isArabic) "تسجيل القياسات الأولية وتحديد جدول التمارين الأسبوعي" else "Record Baseline Metrics & Schedule Weekly Workouts",
                durationMinutes = 30,
                priority = Priority.HIGH,
                subtasks = listOf(
                    if (isArabic) "قياس الوزن ومحيط الخصر" else "Record weight and baseline dimensions",
                    if (isArabic) "تجهيز حقيبة الرياضة وملابس التدريب" else "Prepare athletic gear & water bottle",
                    if (isArabic) "تحديد أوقات ثابتة للتدريب في التقويم" else "Block workout slots in calendar"
                ),
                targetDayOffset = 1
            ),
            RoadmapTask(
                title = if (isArabic) "جلسة تمرين هوائي وإطالات كاملة" else "Cardio Foundation & Full Body Stretching",
                durationMinutes = 40,
                priority = Priority.MEDIUM,
                subtasks = listOf(
                    if (isArabic) "إحماء مفاصل ٥ دقائق" else "5-min dynamic warm up",
                    if (isArabic) "٣٠ دقيقة تمارين متوازنة" else "30-min steady-state cardio",
                    if (isArabic) "شرب كمية كافية من الماء" else "Hydrate & record in health log"
                ),
                targetDayOffset = 2
            )
        )

        return GoalRoadmapProposal(
            goalTitle = title,
            description = desc,
            category = "Health",
            totalDays = days,
            phases = phases,
            milestones = milestones,
            tasks = tasks,
            recommendedHabitTitle = if (isArabic) "تمرين رياضي أو مشي سريع ٣٠ دقيقة" else "Daily 30-Min Workout or Active Walk",
            recommendedHabitFrequency = HabitFrequency.DAILY,
            recoveryStrategy = if (isArabic)
                "إذا شعرت بالإجهاد أو فاتك تمرين، استبدله بـ ١٥ دقيقة مشي خفيف مع إطالات بدلاً من التوقف التام."
            else
                "If exhausted or missed a session, substitute with a 15-min gentle mobility walk rather than skipping entirely."
        )
    }

    private fun generateLanguageRoadmap(days: Int, isArabic: Boolean): GoalRoadmapProposal {
        val title = if (isArabic) "إتقان المحادثة والمفردات في لغة جديدة" else "Fluency & Conversation Mastery in New Language"
        val desc = if (isArabic) "خطة تدرجية تركز على الاستماع، التحدث، و ٥٠٠ كلمة شائعة في $days يوماً."
        else "Speaking-first immersion plan focused on core vocabulary and listening comprehension."

        val phases = listOf(
            RoadmapPhase(
                title = if (isArabic) "المرحلة الأولى: أهم ٣٠٠ كلمة والتركيب الأساسي للجمل" else "Phase 1: Core 300 Words & Sentence Framing",
                dayRange = "Day 1 - Day ${days / 3}",
                description = if (isArabic) "الضمائر، الأفعال الأساسية، وتركيب الجمل البسيطة." else "Pronouns, essential verbs, and daily phrasing."
            ),
            RoadmapPhase(
                title = if (isArabic) "المرحلة الثانية: الاستماع المكثف والمحادثات اليومية" else "Phase 2: Active Listening & Daily Dialogues",
                dayRange = "Day ${(days / 3) + 1} - Day ${(days * 2) / 3}",
                description = if (isArabic) "الاستماع لبودكاست تعليمي ومحاكاة محادثات حقيقية." else "Listen to podcasts, shadow native speakers, and practice dialogues."
            ),
            RoadmapPhase(
                title = if (isArabic) "المرحلة الثالثة: الطلاقة والتعبير التلقائي" else "Phase 3: Spontaneous Speech & Fluency",
                dayRange = "Day ${((days * 2) / 3) + 1} - Day $days",
                description = if (isArabic) "التحدث لمدة ١٥ دقيقة متواصلة بدون ترجمة ذهنية." else "15-min continuous conversation without mental translation."
            )
        )

        val milestones = listOf(
            RoadmapMilestone(
                title = if (isArabic) "حفظ وممارسة أول ٢٥٠ كلمة مع أمثلة عملية" else "Master First 250 Words with Context Sentences",
                targetDayOffset = days / 4,
                phaseTitle = phases[0].title
            ),
            RoadmapMilestone(
                title = if (isArabic) "إتمام محادثة تعريفية كاملة لمدة ١٠ دقائق" else "Conduct 10-Min Fluent Self-Introduction & Dialogue",
                targetDayOffset = days / 2,
                phaseTitle = phases[1].title
            ),
            RoadmapMilestone(
                title = if (isArabic) "فهم بودكاست كامل بمستوى متوسط بدون ترجمة" else "Understand Full Intermediate Podcast Without Subtitles",
                targetDayOffset = days,
                phaseTitle = phases[2].title
            )
        )

        val tasks = listOf(
            RoadmapTask(
                title = if (isArabic) "تحديد قائمة أهم ١٠٠ فعل وتطبيق بطاقات التكرار المتباعد" else "Study Top 100 Verbs with Spaced Repetition",
                durationMinutes = 30,
                priority = Priority.HIGH,
                subtasks = listOf(
                    if (isArabic) "تسجيل الكلمات في ملاحظات يومك" else "Add words to YAWMEK notes",
                    if (isArabic) "نطق كل فعل في جملة مفيدة" else "Pronounce each verb aloud in a sentence"
                ),
                targetDayOffset = 1
            )
        )

        return GoalRoadmapProposal(
            goalTitle = title,
            description = desc,
            category = "Education",
            totalDays = days,
            phases = phases,
            milestones = milestones,
            tasks = tasks,
            recommendedHabitTitle = if (isArabic) "مراجعة الكلمات والاستماع ٢٠ دقيقة يومياً" else "Daily 20-Min Listening & Vocab Review",
            recommendedHabitFrequency = HabitFrequency.DAILY,
            recoveryStrategy = if (isArabic) "استمع إلى بودكاست أو أغنية باللغة المستهدفة أثناء التنقل إذا ضاق وقتك."
            else "Listen to target language audio during transit if desk study time is short."
        )
    }

    private fun generateBusinessRoadmap(days: Int, isArabic: Boolean): GoalRoadmapProposal {
        val title = if (isArabic) "إطلاق فكرة مشروع أو عمل حر وتحقيق أول عميل" else "Launch Product or Freelance Service & Acquire First Client"
        val desc = if (isArabic) "خطة عمل واقعية تبدأ من التحقق من الفكرة وحتى التسويق والبيع في $days يوماً."
        else "Execution roadmap from value proposition to launch and customer acquisition."

        val phases = listOf(
            RoadmapPhase(
                title = if (isArabic) "المرحلة الأولى: تحديد القيمة والعميل المستهدف" else "Phase 1: Value Proposition & Customer Definition",
                dayRange = "Day 1 - Day ${days / 3}",
                description = if (isArabic) "دراسة المنافسين، تسعير العرض، وصياغة الرسالة التسويقية." else "Competitor research, offer pricing, and landing page outline."
            ),
            RoadmapPhase(
                title = if (isArabic) "المرحلة الثانية: بناء النموذج الأولي واختباره" else "Phase 2: Build MVP & Outreach Setup",
                dayRange = "Day ${(days / 3) + 1} - Day ${(days * 2) / 3}",
                description = if (isArabic) "تجهيز معرض الأعمال أو المنتج الأولي وإرسال أول عروض." else "Create initial portfolio/product demo and start direct outreach."
            ),
            RoadmapPhase(
                title = if (isArabic) "المرحلة الثالثة: التسويق واستقطاب المبيعات" else "Phase 3: Sales Engine & First Closing",
                dayRange = "Day ${((days * 2) / 3) + 1} - Day $days",
                description = if (isArabic) "إغلاق أول صفقة والحصول على تقييم عميل حقيقي." else "Close first customer and gather testimonial."
            )
        )

        val milestones = listOf(
            RoadmapMilestone(
                title = if (isArabic) "الانتهاء من صياغة العرض ومعرض الأعمال التجريبي" else "Finalize Core Offer & Portfolio Presentation",
                targetDayOffset = days / 4,
                phaseTitle = phases[0].title
            ),
            RoadmapMilestone(
                title = if (isArabic) "التواصل مع ٢٠ عميلاً محتملاً أو جهة مهتمة" else "Reach Out to 20 Qualified Leads or Prospects",
                targetDayOffset = days / 2,
                phaseTitle = phases[1].title
            ),
            RoadmapMilestone(
                title = if (isArabic) "تحقيق أول عملية بيع أو عقد عمل رسمي" else "Close First Paying Client / Launch Official Sales",
                targetDayOffset = days,
                phaseTitle = phases[2].title
            )
        )

        val tasks = listOf(
            RoadmapTask(
                title = if (isArabic) "كتابة وثيقة مواصفات العرض التجاري (One-Pager)" else "Draft 1-Page Offer Specification & Pricing",
                durationMinutes = 45,
                priority = Priority.HIGH,
                subtasks = listOf(
                    if (isArabic) "تحديد المشكلة والحل المقترح" else "Define core pain point and solution",
                    if (isArabic) "حساب التكاليف وهوامش الربح" else "Calculate operational cost & margin"
                ),
                targetDayOffset = 1
            )
        )

        return GoalRoadmapProposal(
            goalTitle = title,
            description = desc,
            category = "Work",
            totalDays = days,
            phases = phases,
            milestones = milestones,
            tasks = tasks,
            recommendedHabitTitle = if (isArabic) "تواصل تسويقي أو تحسين العرض ٣٠ دقيقة يومياً" else "Daily 30-Min Outreach & Offer Iteration",
            recommendedHabitFrequency = HabitFrequency.DAILY,
            recoveryStrategy = if (isArabic) "ركز على جودة التواصل مع عميل واحد محتمل يومياً حتى لو لم تكتمل كل الخطط."
            else "Focus on 1 direct prospect interaction daily even if operational plans stall."
        )
    }

    private fun generateGeneralRoadmap(prompt: String, days: Int, isArabic: Boolean): GoalRoadmapProposal {
        val cleanPrompt = prompt.replace(Regex("(?i)(i want to|عايز|أريد|خطة|plan|goal|roadmap)"), "").trim()
        val title = cleanPrompt.ifBlank { if (isArabic) "تحقيق الهدف الكبير" else "Master Major Milestone" }

        val phases = listOf(
            RoadmapPhase(
                title = if (isArabic) "المرحلة الأولى: التأسيس والتجهيز" else "Phase 1: Setup & Foundations",
                dayRange = "Day 1 - Day ${days / 3}",
                description = if (isArabic) "بناء المعرفة وتوفير الأدوات والبدء بالخطوات الأولى." else "Gather resources, establish routines, and complete first reps."
            ),
            RoadmapPhase(
                title = if (isArabic) "المرحلة الثانية: التنفيذ المكثف" else "Phase 2: Core Execution & Momentum",
                dayRange = "Day ${(days / 3) + 1} - Day ${(days * 2) / 3}",
                description = if (isArabic) "الاستمرارية اليومية والتغلب على المعوقات." else "Maintain consistency and overcome execution bottlenecks."
            ),
            RoadmapPhase(
                title = if (isArabic) "المرحلة الثالثة: الإتمام والتقييم النهائي" else "Phase 3: Final Push & Celebration",
                dayRange = "Day ${((days * 2) / 3) + 1} - Day $days",
                description = if (isArabic) "إنهاء المهام المتبقية وقياس الأثر النهائي." else "Complete remaining deliverables and lock in gains."
            )
        )

        val milestones = listOf(
            RoadmapMilestone(
                title = if (isArabic) "إنهاء المرحلة التأسيسية الأولى بنجاح" else "Complete Phase 1 Milestones",
                targetDayOffset = days / 3,
                phaseTitle = phases[0].title
            ),
            RoadmapMilestone(
                title = if (isArabic) "الوصول إلى منتصف الطريق بثقة واستقرار" else "Halfway Review & Pace Verification",
                targetDayOffset = days / 2,
                phaseTitle = phases[1].title
            ),
            RoadmapMilestone(
                title = if (isArabic) "تحقيق الهدف الكامل بنسبة ١٠٠٪" else "100% Goal Completion & Review",
                targetDayOffset = days,
                phaseTitle = phases[2].title
            )
        )

        val tasks = listOf(
            RoadmapTask(
                title = if (isArabic) "الخطوة الأولى: تفكيك المهمة وتحديد الموارد المطلوبة" else "First Action: Scope Deliverables & Required Tools",
                durationMinutes = 35,
                priority = Priority.HIGH,
                subtasks = listOf(
                    if (isArabic) "تحديد أول ٣ خطوات عملية" else "Identify first 3 concrete actions",
                    if (isArabic) "حجز وقت مخصص في الجدول اليومي" else "Block focus time in calendar"
                ),
                targetDayOffset = 1
            )
        )

        return GoalRoadmapProposal(
            goalTitle = title,
            description = if (isArabic) "خطة استراتيجية محكمة لتحقيق $title خلال $days يوماً." else "Strategic roadmap to accomplish $title within $days days.",
            category = "Personal",
            totalDays = days,
            phases = phases,
            milestones = milestones,
            tasks = tasks,
            recommendedHabitTitle = if (isArabic) "جلسة تركيز ٣٠ دقيقة موجهة للهدف" else "Daily 30-Min Focused Effort",
            recommendedHabitFrequency = HabitFrequency.DAILY,
            recoveryStrategy = if (isArabic) "قم بتقسيم الخطوات الكبيرة إلى مهام مصغرة لا تتجاوز ربع ساعة لتجاوز المماطلة."
            else "Break intimidating tasks down to 15-minute micro-actions to bypass procrastination."
        )
    }
}
