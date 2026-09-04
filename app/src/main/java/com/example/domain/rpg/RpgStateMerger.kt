package com.example.domain.rpg

import com.example.data.local.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class RpgCombinedState(
    val character: CharacterRpgEntity = CharacterRpgEntity(),
    val equipment: List<EquipmentItemEntity> = emptyList(),
    val companions: List<CompanionEntity> = emptyList(),
    val skills: List<SkillNodeEntity> = emptyList(),
    val quests: List<RpgQuestEntity> = emptyList(),
    val bosses: List<BossChallengeEntity> = emptyList()
)

object RpgStateMerger {
    fun merge(
        charFlow: Flow<CharacterRpgEntity?>,
        equipFlow: Flow<List<EquipmentItemEntity>>,
        compFlow: Flow<List<CompanionEntity>>,
        skillFlow: Flow<List<SkillNodeEntity>>,
        questFlow: Flow<List<RpgQuestEntity>>,
        bossFlow: Flow<List<BossChallengeEntity>>
    ): Flow<RpgCombinedState> {
        val flow1to3 = combine(charFlow, equipFlow, compFlow) { c, e, comp ->
            Triple(c ?: CharacterRpgEntity(), e, comp)
        }
        val flow4to6 = combine(skillFlow, questFlow, bossFlow) { s, q, b ->
            Triple(s, q, b)
        }
        return combine(flow1to3, flow4to6) { part1, part2 ->
            RpgCombinedState(
                character = part1.first,
                equipment = part1.second,
                companions = part1.third,
                skills = part2.first,
                quests = part2.second,
                bosses = part2.third
            )
        }
    }
}
