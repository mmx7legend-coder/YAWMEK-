package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Core Character RPG Profile
 * Starts very weak at Level 1 with 0 XP.
 * High levels require intentional, slow, consistent productivity.
 */
@Entity(tableName = "rpg_character")
data class CharacterRpgEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "المحارب (Seeker)",
    val level: Int = 1,
    val currentXp: Long = 0L,
    val totalEarnedXp: Long = 0L,
    val availableSkillPoints: Int = 0,

    // Six core productivity stats
    val statFocus: Int = 1,
    val statDiscipline: Int = 1,
    val statKnowledge: Int = 1,
    val statConsistency: Int = 1,
    val statPlanning: Int = 1,
    val statCourage: Int = 1,

    // Equipped gear IDs
    val equippedWeaponId: String = "starter_stick",
    val equippedArmorId: String = "ragged_cloth",
    val equippedHelmetId: String? = null,
    val equippedGlovesId: String? = null,
    val equippedBootsId: String? = null,
    val equippedCapeId: String? = null,
    val equippedAccessoryId: String? = null,
    val equippedFrameId: String = "frame_simple",
    val equippedBackgroundId: String = "bg_village",

    // Companions & Dragons
    val equippedCompanionId: String? = null,
    val dragonStage: DragonStage = DragonStage.NONE,
    val dragonAffinity: String = "NONE", // FIRE, CELESTIAL, NATURE, COSMIC
    val dragonProgressPoints: Int = 0,

    // Adventure World Progression
    val currentAdventureArea: AdventureArea = AdventureArea.VILLAGE,
    val areaMilestoneProgress: Int = 0,

    // Real Streak tracking
    val productiveStreakDays: Int = 0,
    val lastActiveDateEpochDay: Long = 0L,
    val avatarSkin: String = "🧙",
    val titleEn: String = "Apprentice Seeker",
    val titleAr: String = "المكافح المبتدئ"
)

enum class DragonStage(
    val levelRequired: Int,
    val titleEn: String,
    val titleAr: String,
    val iconEmoji: String,
    val descriptionEn: String,
    val descriptionAr: String
) {
    NONE(0, "No Dragon", "بدون تنين", "🥚", "Unlock a dragon by reaching Level 15 and mastering goals.", "افتح تنيناً بالوصول للمستوى 15 وإنجاز أهداف حقيقية."),
    EGG(15, "Mystic Dragon Egg", "بيضة تنين غامضة", "🥚", "An ancient dragon egg resonating with focus energy.", "بيضة تنين عتيقة تنبض بطاقة التركيز."),
    HATCHLING(25, "Dragon Hatchling", "فرخ تنين وليد", "🐣", "A newborn dragon following your daily schedule.", "تنين وليد يرافق خطواتك اليومية باجتهاد."),
    YOUNG(40, "Young Wyrm", "تنين يافع مجنح", "🦎", "A winged guardian breathing productive fire.", "حارس مجنح يطلق لهب الإرادة والإنجاز."),
    ADULT(60, "Adult Flame Drake", "تنين بالغ مهيب", "🐉", "A majestic beast of supreme habit mastery.", "كائن مهيب يشهد على شهور من الالتزام التام."),
    ANCIENT(85, "Ancient Realm Dragon", "تنين العوالم العتيق", "🐲", "The mythic dragon of lifelong self-mastery.", "التنين الأسطوري لقمة الاتزان وضبط النفس.")
}

enum class AdventureArea(
    val order: Int,
    val unlockLevel: Int,
    val titleEn: String,
    val titleAr: String,
    val descriptionEn: String,
    val descriptionAr: String,
    val iconEmoji: String
) {
    VILLAGE(1, 1, "Quiet Village", "قرية البدايات الهادئة", "Where humble productivity begins.", "حيث يبدأ الالتزام البسيط والتأسيس.", "🏡"),
    FOREST(2, 5, "Whispering Forest", "غابة التركيز الهامسة", "Overcome distractions in the wild woods.", "تجاوز المشتتات وسط أشجار العادات المتجذرة.", "🌲"),
    RUINS(3, 12, "Forgotten Ruins", "أطلال العزيمة المنسية", "Decipher the secrets of deep discipline.", "اكتشف أسرار الانضباط الراسخ من حضارات سابقة.", "🏛️"),
    MOUNTAIN(4, 22, "Peak of Persistence", "قمة المثابرة الشامخة", "Climb the heights of continuous habit streaks.", "تسلّق قمم الاستمرارية والتحديات المتتالية.", "🏔️"),
    CASTLE(5, 35, "Citadel of Will", "قلعة الإرادة الفولاذية", "Command your master goals with unwavering focus.", "تسيّد أهدافك الكبرى وحطّم العقبات العتيدة.", "🏰"),
    DRAGON_VALLEY(6, 50, "Dragon Valley", "وادي التنانين الأسطوري", "Walk among ancient beasts of supreme mastery.", "سر وسط الكائنات الخارقة بعد شهور من الإنجاز.", "🌋"),
    ANCIENT_REALM(7, 75, "Ancient Realm", "عالم الحكمة الأبدي", "The pinnacle of ultimate productivity and balance.", "ذروة الاتزان الحياتي والإنتاجية الأسطورية.", "✨")
}

enum class ItemRarity(val labelEn: String, val labelAr: String, val colorHex: String) {
    COMMON("Common", "شائع", "#94A3B8"),
    UNCOMMON("Uncommon", "غير مألوف", "#10B981"),
    RARE("Rare", "نادر", "#38BDF8"),
    EPIC("Epic", "ملحمي", "#A855F7"),
    LEGENDARY("Legendary", "أسطوري", "#F59E0B"),
    MYTHIC("Mythic", "خرافي", "#EF4444")
}

enum class EquipmentSlot(val titleEn: String, val titleAr: String) {
    WEAPON("Weapon", "سلاح"),
    ARMOR("Armor", "درع"),
    HELMET("Helmet", "خوذة"),
    GLOVES("Gloves", "قفازات"),
    BOOTS("Boots", "أحذية"),
    CAPE("Cape", "وشاح"),
    ACCESSORY("Accessory", "إكسسوار"),
    FRAME("Avatar Frame", "إطار الأفاتار"),
    BACKGROUND("Background", "خلفية المشهد")
}

@Entity(tableName = "rpg_equipment_items")
data class EquipmentItemEntity(
    @PrimaryKey val id: String,
    val slot: EquipmentSlot,
    val rarity: ItemRarity,
    val nameEn: String,
    val nameAr: String,
    val descriptionEn: String,
    val descriptionAr: String,
    val iconEmoji: String,
    val requiredLevel: Int = 1,
    val xpStoreCost: Long = 0L,
    val isPurchased: Boolean = false,
    val statBoostDescription: String = "",
    val bonusStatFocus: Int = 0,
    val bonusStatDiscipline: Int = 0,
    val bonusStatKnowledge: Int = 0,
    val bonusStatConsistency: Int = 0,
    val bonusStatPlanning: Int = 0,
    val bonusStatCourage: Int = 0
)

@Entity(tableName = "rpg_companions")
data class CompanionEntity(
    @PrimaryKey val id: String,
    val nameEn: String,
    val nameAr: String,
    val speciesEn: String,
    val speciesAr: String,
    val descriptionEn: String,
    val descriptionAr: String,
    val iconEmoji: String,
    val requiredLevel: Int,
    val xpCost: Long,
    val isUnlocked: Boolean = false,
    val reactionIdleAr: String,
    val reactionIdleEn: String,
    val reactionCelebrateAr: String,
    val reactionCelebrateEn: String
)

@Entity(tableName = "rpg_skills")
data class SkillNodeEntity(
    @PrimaryKey val id: String,
    val branch: SkillBranch,
    val tier: Int, // 1 to 5
    val nameEn: String,
    val nameAr: String,
    val descriptionEn: String,
    val descriptionAr: String,
    val iconEmoji: String,
    val requiredCharacterLevel: Int,
    val pointCost: Int = 1,
    val prerequisiteSkillId: String? = null,
    val isUnlocked: Boolean = false,
    val effectDescriptionAr: String,
    val effectDescriptionEn: String
)

enum class SkillBranch(val titleEn: String, val titleAr: String, val colorHex: String) {
    FOCUS_FLOW("Deep Focus", "التدفق العميق", "#38BDF8"),
    IRON_DISCIPLINE("Iron Discipline", "الانضباط الحديدي", "#F59E0B"),
    SCHOLAR_WISDOM("Scholar's Wisdom", "حكمة الباحث", "#10B981"),
    WARRIOR_COURAGE("Warrior's Courage", "شجاعة المحارب", "#EF4444")
}

@Entity(tableName = "rpg_quests")
data class RpgQuestEntity(
    @PrimaryKey val id: String,
    val isWeekly: Boolean = false,
    val titleEn: String,
    val titleAr: String,
    val descriptionEn: String,
    val descriptionAr: String,
    val iconEmoji: String,
    val targetCount: Int,
    val currentCount: Int = 0,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false,
    val rewardXp: Long,
    val rewardSkillPoints: Int = 0,
    val questType: RpgQuestType,
    val dateAssignedEpochDay: Long = 0L
)

enum class RpgQuestType {
    COMPLETE_IMPORTANT_TASKS,
    FINISH_FOCUS_SESSION,
    READ_SESSION,
    COMPLETE_HABITS,
    FINISH_TODAY_PLAN,
    WIN_BRAIN_GAME,
    LOG_DAILY_BUDGET
}

@Entity(tableName = "rpg_bosses")
data class BossChallengeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val relatedGoalId: Long? = null,
    val bossNameEn: String,
    val bossNameAr: String,
    val titleEn: String,
    val titleAr: String,
    val iconEmoji: String,
    val maxHp: Int,
    val currentHp: Int,
    val isDefeated: Boolean = false,
    val rewardXp: Long,
    val rewardTitleAr: String,
    val rewardTitleEn: String,
    val createdAtMillis: Long = System.currentTimeMillis()
)
