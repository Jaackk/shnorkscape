package com.rs.game.player.content.jujupotions.harmonypillar;

import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.Colors;
import com.rs.utils.Utils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author lare96 <http://github.com/lare96>
 */
@RequiredArgsConstructor
public final class HarmonyPillar implements Serializable {
    private static final long serialVersionUID = -8028439920231195143L;

    @Getter
    private final WorldTile tile;
    @Getter
    private HarmonyPillarState state = HarmonyPillarState.WEEDED;
    @Getter
    private double currentXp = 0.0;
    @Setter
    @Getter
    private int attunedTo = -1;
    int mossAmount;
    @Setter
    @Getter
    private LocalDateTime reattuneDate;
    @Getter
    @Setter
    private transient Player player;

    public void addXp(double xp) {
        if (state == HarmonyPillarState.CLEAR ||
                state == HarmonyPillarState.WEEDED ||
                state == HarmonyPillarState.GROWN) {
            return;
        }
        currentXp += xp;
        if (currentXp >= HarmonyPillarManager.GROWTH_XP_NEEDED) {
            int attunedToBefore = attunedTo;
            switch (state) {
                case GROWING_1:
                    setState(HarmonyPillarState.GROWING_2);
                    break;
                case GROWING_2:
                    setState(HarmonyPillarState.GROWING_3);
                    break;
                case GROWING_3:
                    setState(HarmonyPillarState.GROWING_4);
                    break;
                case GROWING_4:
                    setState(HarmonyPillarState.GROWN);
                    break;
            }

            String skillName = Skills.SKILL_NAME[attunedToBefore];
            switch (state) {
                case GROWING_1:
                case GROWING_2:
                case GROWING_3:
                case GROWING_4:
                    player.sendMessage(Colors.GREEN + "Your harmony pillar attuned to " + skillName + " has grown a little.");
                    break;
                case GROWN:
                    player.sendMessage(Colors.GREEN + "Your harmony pillar attuned to " + skillName + " is now ready for harvest.");
                    break;
            }

            currentXp = 0.0;
        }
    }

    public boolean shouldWeedsGrow() {
        return state == HarmonyPillarState.CLEAR &&
                reattuneDate != null &&
                LocalDateTime.now().isAfter(reattuneDate);
    }

    public boolean isAttuned() {
        return attunedTo >= 0;
    }

    public void spawn() {
        player.getPackets().addSpawnedObject(new WorldObject(getObjectId(), 10, 1, tile));
    }

    public void setState(HarmonyPillarState newState) {
        switch (newState) {
            case WEEDED: // Reset attune state.
                attunedTo = -1;
                currentXp = 0.0;
                reattuneDate = null;
                mossAmount = 0;
                break;
            case CLEAR: // Generate attuned state.
                Set<Integer> attuneSet = new HashSet<>();
                for (val pillar : player.harmonyPillars.getPillars().values()) {
                    if (pillar.isAttuned()) {
                        attuneSet.add(pillar.attunedTo);
                    }
                }
                for (int skillId : Skills.SKILL_IDS) {
                    boolean removed = attuneSet.remove(skillId);
                    if (!removed) {
                        attuneSet.add(skillId);
                    }
                }
                attunedTo = Utils.randomFrom(new ArrayList<>(attuneSet));
                reattuneDate = LocalDateTime.now().plusDays(1);
                currentXp = 0.0;
                mossAmount = 0;
                break;
            case GROWN: // Reset attune state, generate harmony moss.
                attunedTo = -1;
                reattuneDate = null;
                currentXp = 0.0;
                mossAmount = ThreadLocalRandom.current().nextInt(2, HarmonyPillarManager.MAX_HARMONY_MOSS);
                break;
        }
        state = newState;
        spawn();
    }

    private int getObjectId() {
        switch (state) {
            case WEEDED:
                return 94324;
            case CLEAR:
                return 94331;
            case GROWING_1:
                return 94332;
            case GROWING_2:
                return 94333;
            case GROWING_3:
                return 94334;
            case GROWING_4:
                return 94335;
            case GROWN:
                return 94336;
            default:
                throw new IllegalStateException("no state for harmony pillar");
        }
    }
}
