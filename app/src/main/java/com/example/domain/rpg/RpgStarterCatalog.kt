package com.example.domain.rpg

import com.example.data.local.model.*

object RpgStarterCatalog {

    fun getDefaultCharacter(): CharacterRpgEntity {
        return CharacterRpgEntity(
            id = 1,
            name = "المكافح (Seeker)",
            level = 1,
            currentXp = 0L,
            totalEarnedXp = 0L,
            availableSkillPoints = 0,
            statFocus = 1,
            statDiscipline = 1,
            statKnowledge = 1,
            statConsistency = 1,
            statPlanning = 1,
            statCourage = 1,
            equippedWeaponId = "starter_stick",
            equippedArmorId = "ragged_cloth",
            equippedHelmetId = null,
            equippedGlovesId = null,
            equippedBootsId = null,
            equippedCapeId = null,
            equippedAccessoryId = null,
            equippedFrameId = "frame_simple",
            equippedBackgroundId = "bg_village",
            equippedCompanionId = null,
            dragonStage = DragonStage.NONE,
            dragonAffinity = "NONE",
            dragonProgressPoints = 0,
            currentAdventureArea = AdventureArea.VILLAGE,
            areaMilestoneProgress = 0,
            productiveStreakDays = 0,
            lastActiveDateEpochDay = 0L
        )
    }

    fun getEquipmentCatalog(): List<EquipmentItemEntity> {
        return listOf(
            // WEAPONS: From weak starter to legendary relics
            EquipmentItemEntity(
                id = "starter_stick",
                slot = EquipmentSlot.WEAPON,
                rarity = ItemRarity.COMMON,
                nameEn = "Weathered Wooden Staff",
                nameAr = "عصا خشبية بالية",
                descriptionEn = "A humble branch found on your first step into productivity.",
                descriptionAr = "غصن متواضع التقطه في بداية رحلتك مع الإنتاجية.",
                iconEmoji = "🦯",
                requiredLevel = 1,
                xpStoreCost = 0L,
                isPurchased = true,
                statBoostDescription = "+1 Focus",
                bonusStatFocus = 1
            ),
            EquipmentItemEntity(
                id = "bronze_quill_dagger",
                slot = EquipmentSlot.WEAPON,
                rarity = ItemRarity.UNCOMMON,
                nameEn = "Bronze Quill Dagger",
                nameAr = "خنجر الريشة البرونزية",
                descriptionEn = "Forged by writing down every daily thought and task.",
                descriptionAr = "شحذ بتدوين كل فكرة ومهمة يومية بدقة.",
                iconEmoji = "🗡️",
                requiredLevel = 3,
                xpStoreCost = 150L,
                isPurchased = false,
                statBoostDescription = "+2 Planning, +1 Focus",
                bonusStatPlanning = 2,
                bonusStatFocus = 1
            ),
            EquipmentItemEntity(
                id = "iron_focus_blade",
                slot = EquipmentSlot.WEAPON,
                rarity = ItemRarity.RARE,
                nameEn = "Iron Focus Blade",
                nameAr = "سيف التركيز الحديدي",
                descriptionEn = "Tempered in the fires of unbroken 25-minute Pomodoros.",
                descriptionAr = "صُقل في نيران جلسات البومودورو المكتملة دون انقطاع.",
                iconEmoji = "⚔️",
                requiredLevel = 8,
                xpStoreCost = 450L,
                isPurchased = false,
                statBoostDescription = "+4 Focus, +2 Discipline",
                bonusStatFocus = 4,
                bonusStatDiscipline = 2
            ),
            EquipmentItemEntity(
                id = "sapphire_runic_sword",
                slot = EquipmentSlot.WEAPON,
                rarity = ItemRarity.EPIC,
                nameEn = "Sapphire Runic Cleaver",
                nameAr = "سيف الياقوت الأزرق الروني",
                descriptionEn = "Vibrates with the energy of Midnight Sapphire discipline.",
                descriptionAr = "ينبض بطاقة الانضباط العميق وطمس المشتتات.",
                iconEmoji = "✨",
                requiredLevel = 18,
                xpStoreCost = 1200L,
                isPurchased = false,
                statBoostDescription = "+6 Discipline, +4 Courage",
                bonusStatDiscipline = 6,
                bonusStatCourage = 4
            ),
            EquipmentItemEntity(
                id = "chronos_sun_glaive",
                slot = EquipmentSlot.WEAPON,
                rarity = ItemRarity.LEGENDARY,
                nameEn = "Sun Glaive of Chronos",
                nameAr = "حربة كرونوس الشمسية",
                descriptionEn = "Bends the perceived flow of time for ultimate daily masterwork.",
                descriptionAr = "تطوّع جريان الزمن لتحقيق الإنجاز اليومي الأسطوري.",
                iconEmoji = "🔱",
                requiredLevel = 35,
                xpStoreCost = 3500L,
                isPurchased = false,
                statBoostDescription = "+10 Focus, +8 Consistency, +6 Planning",
                bonusStatFocus = 10,
                bonusStatConsistency = 8,
                bonusStatPlanning = 6
            ),

            // ARMOR: From ragged clothes to dragon-forged plate
            EquipmentItemEntity(
                id = "ragged_cloth",
                slot = EquipmentSlot.ARMOR,
                rarity = ItemRarity.COMMON,
                nameEn = "Ragged Cloth Tunic",
                nameAr = "سترة بالية بسيطة",
                descriptionEn = "Barely shields from cold mornings, but keeps you moving.",
                descriptionAr = "بالكاد تقي من برد الصباح، لكنها تذكرك بالبداية.",
                iconEmoji = "🥋",
                requiredLevel = 1,
                xpStoreCost = 0L,
                isPurchased = true,
                statBoostDescription = "+1 Discipline",
                bonusStatDiscipline = 1
            ),
            EquipmentItemEntity(
                id = "apprentice_leather_vest",
                slot = EquipmentSlot.ARMOR,
                rarity = ItemRarity.UNCOMMON,
                nameEn = "Apprentice Leather Vest",
                nameAr = "صدرية جلد المبتدئ",
                descriptionEn = "Stitched together with emerging habits and small wins.",
                descriptionAr = "حيكت من العادات الأولى والانتصارات الصغيرة المتراكمة.",
                iconEmoji = "🦺",
                requiredLevel = 4,
                xpStoreCost = 200L,
                isPurchased = false,
                statBoostDescription = "+2 Consistency, +1 Courage",
                bonusStatConsistency = 2,
                bonusStatCourage = 1
            ),
            EquipmentItemEntity(
                id = "steel_discipline_cuirass",
                slot = EquipmentSlot.ARMOR,
                rarity = ItemRarity.RARE,
                nameEn = "Steel Discipline Cuirass",
                nameAr = "درع الانضباط الفولاذي",
                descriptionEn = "Deflects procrastination impulses with ease.",
                descriptionAr = "يصد نزوات التسويف والتأجيل بكل حزم وقوة.",
                iconEmoji = "🛡️",
                requiredLevel = 10,
                xpStoreCost = 600L,
                isPurchased = false,
                statBoostDescription = "+5 Discipline, +3 Consistency",
                bonusStatDiscipline = 5,
                bonusStatConsistency = 3
            ),
            EquipmentItemEntity(
                id = "dragonscale_aegis",
                slot = EquipmentSlot.ARMOR,
                rarity = ItemRarity.LEGENDARY,
                nameEn = "Dragonscale Aegis",
                nameAr = "درع حراشف التنين الملكي",
                descriptionEn = "Emitted with golden ember aura earned over months of dedication.",
                descriptionAr = "يشع بهالة ذهبية اكتسبت عبر شهور من الالتزام والمثابرة.",
                iconEmoji = "🐉",
                requiredLevel = 45,
                xpStoreCost = 4500L,
                isPurchased = false,
                statBoostDescription = "+12 Consistency, +10 Courage, +8 Discipline",
                bonusStatConsistency = 12,
                bonusStatCourage = 10,
                bonusStatDiscipline = 8
            ),

            // HELMETS
            EquipmentItemEntity(
                id = "scholar_headband",
                slot = EquipmentSlot.HELMET,
                rarity = ItemRarity.COMMON,
                nameEn = "Scholar's Linen Band",
                nameAr = "عصابة رأس طالب الحكمة",
                descriptionEn = "Keeps sweat and wandering thoughts away from eyes.",
                descriptionAr = "تحجب تشتت الأفكار وتثبّت نظرك نحو الهدف.",
                iconEmoji = "🪢",
                requiredLevel = 2,
                xpStoreCost = 100L,
                isPurchased = false,
                statBoostDescription = "+1 Knowledge",
                bonusStatKnowledge = 1
            ),
            EquipmentItemEntity(
                id = "mind_fortress_helm",
                slot = EquipmentSlot.HELMET,
                rarity = ItemRarity.EPIC,
                nameEn = "Helm of the Mind Fortress",
                nameAr = "خوذة حصن العقل",
                descriptionEn = "Complete mental shield against digital notifications.",
                descriptionAr = "حصن ذهني كامل ضد إغراءات المقاطعة والإشعارات.",
                iconEmoji = "⛑️",
                requiredLevel = 20,
                xpStoreCost = 1500L,
                isPurchased = false,
                statBoostDescription = "+7 Focus, +5 Knowledge",
                bonusStatFocus = 7,
                bonusStatKnowledge = 5
            ),

            // CAPES
            EquipmentItemEntity(
                id = "wanderer_cloak",
                slot = EquipmentSlot.CAPE,
                rarity = ItemRarity.UNCOMMON,
                nameEn = "Wanderer's Cloak",
                nameAr = "عباءة المرتحل",
                descriptionEn = "Protects your vision on dark, slow productive days.",
                descriptionAr = "تحميك في الأيام الصعبة وتبقيك متمسكاً بمسارك.",
                iconEmoji = "🧣",
                requiredLevel = 5,
                xpStoreCost = 250L,
                isPurchased = false,
                statBoostDescription = "+2 Courage, +1 Consistency",
                bonusStatCourage = 2,
                bonusStatConsistency = 1
            ),
            EquipmentItemEntity(
                id = "aurora_stellar_mantle",
                slot = EquipmentSlot.CAPE,
                rarity = ItemRarity.MYTHIC,
                nameEn = "Aurora Stellar Mantle",
                nameAr = "وشاح الشفق الكوني",
                descriptionEn = "Floats with starlight, worn only by life architects.",
                descriptionAr = "ينسدل بنور النجوم، لا يرتديه إلا أصحاب البصيرة والإنجاز.",
                iconEmoji = "🌌",
                requiredLevel = 60,
                xpStoreCost = 7000L,
                isPurchased = false,
                statBoostDescription = "+15 to all six core stats",
                bonusStatFocus = 15,
                bonusStatDiscipline = 15,
                bonusStatKnowledge = 15,
                bonusStatConsistency = 15,
                bonusStatPlanning = 15,
                bonusStatCourage = 15
            ),

            // ACCESSORIES
            EquipmentItemEntity(
                id = "chronometer_ring",
                slot = EquipmentSlot.ACCESSORY,
                rarity = ItemRarity.RARE,
                nameEn = "Time-Keeper's Signet",
                nameAr = "خاتم حافظ الوقت",
                descriptionEn = "Reminds you every hour that life is finite and precious.",
                descriptionAr = "يذكرك كل ساعة بأن الوقت هو أثمن رأس مال تملكه.",
                iconEmoji = "💍",
                requiredLevel = 12,
                xpStoreCost = 800L,
                isPurchased = false,
                statBoostDescription = "+4 Planning, +3 Consistency",
                bonusStatPlanning = 4,
                bonusStatConsistency = 3
            )
        )
    }

    fun getCompanionCatalog(): List<CompanionEntity> {
        return listOf(
            CompanionEntity(
                id = "companion_owl",
                nameEn = "Sage Archimedes",
                nameAr = "البومة الحكيمة (حكيم)",
                speciesEn = "Silver Barn Owl",
                speciesAr = "بومة فضية باحثة",
                descriptionEn = "Quiet companion who thrives during late-night study and reading.",
                descriptionAr = "رفيق صامت يقف بجوارك في سكون الليل والقراءة المتعمقة.",
                iconEmoji = "🦉",
                requiredLevel = 10,
                xpCost = 700L,
                isUnlocked = false,
                reactionIdleAr = "حكيم يراقب تركيزك بإعجاب ويومئ برأسه هادئاً.",
                reactionIdleEn = "Sage blinks slowly, approving of your focused posture.",
                reactionCelebrateAr = "يرفرف حكيم بجناحيه فرحاً بإتمامك العمل النزيه!",
                reactionCelebrateEn = "Sage spreads wide wings, hooting in celebration!"
            ),
            CompanionEntity(
                id = "companion_fox",
                nameEn = "Ember the Swift",
                nameAr = "الثعلب الناري (فطن)",
                speciesEn = "Crimson Fox",
                speciesAr = "ثعلب قرمزي رشيق",
                descriptionEn = "Agile partner who hates procrastination and loves quick wins.",
                descriptionAr = "رفيق رشيق يمقت التسويف ويدفعك لإنجاز المهام السريعة.",
                iconEmoji = "🦊",
                requiredLevel = 20,
                xpCost = 1400L,
                isUnlocked = false,
                reactionIdleAr = "فطن يترقب مهمتك التالية بعينين متقدتين بالعزم.",
                reactionIdleEn = "Ember wags his bushy tail, eager for the next sprint.",
                reactionCelebrateAr = "يقفز فطن بحماس عارم ويلتف فرحاً بإنجازك!",
                reactionCelebrateEn = "Ember leaps through the air in pure excitement!"
            ),
            CompanionEntity(
                id = "companion_golem",
                nameEn = "Granite Guardian",
                nameAr = "حارس الجرانيت (صخر)",
                speciesEn = "Rune Golem",
                speciesAr = "غولم حجري عتيق",
                descriptionEn = "Unshakable construct that personifies iron habits and stoicism.",
                descriptionAr = "كيان حجري راسخ يجسد العادات الحديدية والثبات الصامد.",
                iconEmoji = "🗿",
                requiredLevel = 30,
                xpCost = 2500L,
                isUnlocked = false,
                reactionIdleAr = "صخر يقف ثابتاً كالطود، يمنحك شعوراً بالأمان والاستمرار.",
                reactionIdleEn = "Granite hums deeply, grounding your willpower.",
                reactionCelebrateAr = "يدق صخر صدره الحجري باعتزاز بصمودك المستمر!",
                reactionCelebrateEn = "Granite strikes the stone ground with triumphant pride!"
            ),
            CompanionEntity(
                id = "companion_phoenix",
                nameEn = "Solara the Reborn",
                nameAr = "طائر الفينيق (ضياء)",
                speciesEn = "Immortal Phoenix",
                speciesAr = "طائر العنقاء الأسطوري",
                descriptionEn = "Born from your ability to rise back up after difficult, missed days.",
                descriptionAr = "وُلد من قدرتك على النهوض مجدداً بعد أي كبوة أو يوم متعثر.",
                iconEmoji = "🦅",
                requiredLevel = 50,
                xpCost = 5000L,
                isUnlocked = false,
                reactionIdleAr = "ريش ضياء الذهبي يبدد الشك واليأس من قلبك تماماً.",
                reactionIdleEn = "Solara's warm aura melts away self-doubt and hesitation.",
                reactionCelebrateAr = "يشعل ضياء السماء بألوان الأمل والنهوض العظيم!",
                reactionCelebrateEn = "Solara bursts into radiant gold flames of rebirth!"
            )
        )
    }

    fun getSkillTreeCatalog(): List<SkillNodeEntity> {
        return listOf(
            // FOCUS FLOW BRANCH
            SkillNodeEntity(
                id = "skill_focus_1",
                branch = SkillBranch.FOCUS_FLOW,
                tier = 1,
                nameEn = "Calm Breath Initiation",
                nameAr = "تهدئة النفس والاستهلال",
                descriptionEn = "Reduces initial friction when starting a deep focus session.",
                descriptionAr = "يقلل مقاومة البدء ويخفف التردد في الدقائق الأولى من العمل.",
                iconEmoji = "🌬️",
                requiredCharacterLevel = 2,
                pointCost = 1,
                isUnlocked = false,
                effectDescriptionAr = "+5% تركيز إضافي عند بدء أي جلسة عمل.",
                effectDescriptionEn = "+5% focus clarity bonus on session start."
            ),
            SkillNodeEntity(
                id = "skill_focus_2",
                branch = SkillBranch.FOCUS_FLOW,
                tier = 2,
                nameEn = "Digital Silence Shield",
                nameAr = "درع الصمت الرقمي",
                descriptionEn = "Sharpens awareness against impulsive phone checking.",
                descriptionAr = "يشحذ الانتباه لمقاومة فتح الهاتف وتفقد وسائل التواصل.",
                iconEmoji = "🔕",
                requiredCharacterLevel = 6,
                pointCost = 1,
                prerequisiteSkillId = "skill_focus_1",
                isUnlocked = false,
                effectDescriptionAr = "تقليل قابلية التشتت بنسبة 15%.",
                effectDescriptionEn = "15% stronger resistance against distraction impulses."
            ),
            SkillNodeEntity(
                id = "skill_focus_3",
                branch = SkillBranch.FOCUS_FLOW,
                tier = 3,
                nameEn = "Hyperfocus Resonance",
                nameAr = "رنين التركيز الفائق",
                descriptionEn = "Unlocks uninterrupted momentum for sessions over 45 minutes.",
                descriptionAr = "يفتح باب التدفق الذهني المستمر للجلسات الطويلة فوق 45 دقيقة.",
                iconEmoji = "⚡",
                requiredCharacterLevel = 15,
                pointCost = 2,
                prerequisiteSkillId = "skill_focus_2",
                isUnlocked = false,
                effectDescriptionAr = "+20% مضاعفة الخبرة لجلسات التركيز العميقة.",
                effectDescriptionEn = "+20% XP bonus on deep focus sessions."
            ),

            // IRON DISCIPLINE BRANCH
            SkillNodeEntity(
                id = "skill_disc_1",
                branch = SkillBranch.IRON_DISCIPLINE,
                tier = 1,
                nameEn = "Morning Anchor",
                nameAr = "مرساة الصباح الباكر",
                descriptionEn = "Builds the resolve to start the primary task before noon.",
                descriptionAr = "يبني العزيمة لبدء المهمة الأهم قبل انتصاف النهار.",
                iconEmoji = "⚓",
                requiredCharacterLevel = 3,
                pointCost = 1,
                isUnlocked = false,
                effectDescriptionAr = "+10 خبرة عند إكمال أول مهمة في الصباح.",
                effectDescriptionEn = "+10 bonus XP on completing first morning task."
            ),
            SkillNodeEntity(
                id = "skill_disc_2",
                branch = SkillBranch.IRON_DISCIPLINE,
                tier = 2,
                nameEn = "Habit Lockout",
                nameAr = "تثبيت العادات الراسخ",
                descriptionEn = "Habit streaks become stronger and less vulnerable to disruption.",
                descriptionAr = "سلاسل العادات تصبح أشد صلابة وأقل عرضة للكسر.",
                iconEmoji = "🔒",
                requiredCharacterLevel = 8,
                pointCost = 1,
                prerequisiteSkillId = "skill_disc_1",
                isUnlocked = false,
                effectDescriptionAr = "+15% مكافأة استمرارية عند الوصول لسلسلة 7 أيام.",
                effectDescriptionEn = "+15% streak mastery multiplier at 7+ days."
            ),

            // SCHOLAR WISDOM BRANCH
            SkillNodeEntity(
                id = "skill_wis_1",
                branch = SkillBranch.SCHOLAR_WISDOM,
                tier = 1,
                nameEn = "Curious Mind",
                nameAr = "العقل الشغوف بالمعرفة",
                descriptionEn = "Rewards regular reading and structured note-taking.",
                descriptionAr = "يكافئ القراءة اليومية المنضبطة وتدوين الملاحظات المفيدة.",
                iconEmoji = "📖",
                requiredCharacterLevel = 2,
                pointCost = 1,
                isUnlocked = false,
                effectDescriptionAr = "+2 خبرة لكل دقيقة قراءة في مكتبة التطبيق.",
                effectDescriptionEn = "+2 XP per minute in dedicated reading mode."
            ),
            SkillNodeEntity(
                id = "skill_wis_2",
                branch = SkillBranch.SCHOLAR_WISDOM,
                tier = 2,
                nameEn = "Synthesis of Wisdom",
                nameAr = "دمج المعرفة والتطبيق",
                descriptionEn = "Translates reading reflections into actionable milestones.",
                descriptionAr = "يحوّل تأملات الكتب إلى خطوات تنفيذية ومحطات واضحة.",
                iconEmoji = "🧠",
                requiredCharacterLevel = 10,
                pointCost = 2,
                prerequisiteSkillId = "skill_wis_1",
                isUnlocked = false,
                effectDescriptionAr = "+30 خبرة عند تحويل فكرة كتاب إلى هدف حقيقي.",
                effectDescriptionEn = "+30 XP when connecting insights to real goals."
            ),

            // WARRIOR COURAGE BRANCH
            SkillNodeEntity(
                id = "skill_courage_1",
                branch = SkillBranch.WARRIOR_COURAGE,
                tier = 1,
                nameEn = "Tackle the Dreaded",
                nameAr = "مواجهة المهمة الأصعب (الضفدع)",
                descriptionEn = "Courage to begin the most dreaded task first thing.",
                descriptionAr = "الشجاعة للمبادرة بأصعب مهمة تؤجلها عادةً.",
                iconEmoji = "🦁",
                requiredCharacterLevel = 4,
                pointCost = 1,
                isUnlocked = false,
                effectDescriptionAr = "مضاعفة خبرة المهمة ذات الأولوية العالية عند إتمامها أولاً.",
                effectDescriptionEn = "2x XP for High-Priority tasks done first."
            )
        )
    }

    fun getDailyQuestsCatalog(): List<RpgQuestEntity> {
        val todayEpochDay = java.time.LocalDate.now().toEpochDay()
        return listOf(
            RpgQuestEntity(
                id = "quest_daily_frog",
                isWeekly = false,
                titleEn = "Slay the Mountain Task",
                titleAr = "قهر المهمة الكبرى",
                descriptionEn = "Complete 1 High-Priority task today.",
                descriptionAr = "أنجز مهمة واحدة عالية الأولوية اليوم.",
                iconEmoji = "🎯",
                targetCount = 1,
                currentCount = 0,
                isCompleted = false,
                isClaimed = false,
                rewardXp = 40L,
                rewardSkillPoints = 0,
                questType = RpgQuestType.COMPLETE_IMPORTANT_TASKS,
                dateAssignedEpochDay = todayEpochDay
            ),
            RpgQuestEntity(
                id = "quest_daily_focus",
                isWeekly = false,
                titleEn = "Deep Focus Ritual",
                titleAr = "طقس التركيز العميق",
                descriptionEn = "Complete at least 1 Focus session (20+ min).",
                descriptionAr = "أتمم جلسة تركيز واحدة لمدة 20 دقيقة على الأقل.",
                iconEmoji = "⏳",
                targetCount = 1,
                currentCount = 0,
                isCompleted = false,
                isClaimed = false,
                rewardXp = 35L,
                rewardSkillPoints = 0,
                questType = RpgQuestType.FINISH_FOCUS_SESSION,
                dateAssignedEpochDay = todayEpochDay
            ),
            RpgQuestEntity(
                id = "quest_daily_habits",
                isWeekly = false,
                titleEn = "Trio of Habits",
                titleAr = "ثلاثية العادات الراسخة",
                descriptionEn = "Check off 3 habits from your daily tracker.",
                descriptionAr = "أتمم 3 عادات من متتبع عاداتك اليومي.",
                iconEmoji = "🔁",
                targetCount = 3,
                currentCount = 0,
                isCompleted = false,
                isClaimed = false,
                rewardXp = 45L,
                rewardSkillPoints = 0,
                questType = RpgQuestType.COMPLETE_HABITS,
                dateAssignedEpochDay = todayEpochDay
            ),
            // Weekly Quest
            RpgQuestEntity(
                id = "quest_weekly_champion",
                isWeekly = true,
                titleEn = "Weekly Pillar of Consistency",
                titleAr = "عمود المثابرة الأسبوعي",
                descriptionEn = "Complete 15 meaningful tasks and 5 focus sessions this week.",
                descriptionAr = "أنجز 15 مهمة و5 جلسات تركيز خلال هذا الأسبوع.",
                iconEmoji = "🏆",
                targetCount = 20,
                currentCount = 0,
                isCompleted = false,
                isClaimed = false,
                rewardXp = 250L,
                rewardSkillPoints = 1,
                questType = RpgQuestType.FINISH_TODAY_PLAN,
                dateAssignedEpochDay = todayEpochDay
            )
        )
    }
}
