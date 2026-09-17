package com.rs.game.player.actions.woodcutting;

import com.google.common.collect.ImmutableList;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;

/**
 * @author lare96 <http://github.com/lare96>
 */
public enum AxeDef {
    // 2019 RS3 hatchet progression. Power is the roll value used by the
    // Woodcutting low/high chance formula, not an action delay.
    BRONZE(1351, 1, 879, 17091, 100, 1),
    IRON(1349, 1, 877, 17090, 200, 2),
    STEEL(1353, 6, 875, 17089, 300, 3),
    BLACK(1361, 11, 873, 17088, 350, 4),
    MITHRIL(1355, 21, 871, 17087, 400, 5),
    ADAMANT(1357, 31, 869, 17086, 500, 7),
    RUNE(1359, 41, 867, 17085, 600, 10),
    INFERNO(13661, 61, 10251, 17093, 700, 13),
    DRAGON(6739, 61, 2846, 17092, 700, 13),
    CRYSTAL(32645, 71, 25165, 25064, 800, 14);


    public static final ImmutableList<AxeDef> ALL = ImmutableList.copyOf(values());

    public final int itemId;
    public final int level;
    public final int emote;
    public final int specialEmote;
    public final int power;
    public final int time;

    AxeDef(int itemId, int level, int emote, int specialEmote, int power, int time) {
        this.itemId = itemId;
        this.level = level;
        this.emote = emote;
        this.specialEmote = specialEmote;
        this.power = power;
        this.time = time;
    }

    public static AxeDef get(Player player) {
        for (AxeDef axe : ALL.reverse()) {
            boolean hasAxe = player.getInventory().containsItem(axe.itemId, 1)
                    || player.getEquipment().has(Equipment.SLOT_WEAPON, axe.itemId)
                    || player.getToolBelt().contains(axe.itemId);
            if (hasAxe && player.getSkills().getLevelForXp(Skills.WOODCUTTING) >= axe.level) {
                return axe;
            }
        }
        return null;
    }
}
