package com.rs.game.player.skills;

import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.activites.gim.GIM;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.hcim_news.HcimNewsManager;
import com.rs.game.player.content.InterfaceManager;
import com.rs.game.player.dialogue.impl.LevelUp;
import com.rs.utils.Colors;
import com.rs.utils.mysql.QueryExecutor;
import com.rs.utils.mysql.impl.News;

import lombok.val;

/**
 * A class that listens for actions to be done upon a certain level up. This
 * class also contains the actions themselves.
 * <p>
 * TODO transfer EXP gains here
 */
public class SkillsListener {
    private static final int WIDOWS_WAIL_MAX_LEVEL = 70;

    public void listenForNewLevel(Player player, int skill, int level) {
        listenForWidowsWailOnNewLevel(player, level);
        listenForMaxGraphics(player, level);
        listenForCongratulationsText(player, skill, level);
        listenForConfigs(player, skill);
        listenForMusicEffects(player, skill);
        listenForNews(player, skill);
    }

    public void listenForLogin(Player player) {
        listenForWidowsWail(player);
    }

    private void listenForWidowsWailOnNewLevel(Player player, int level) {
        if (level == WIDOWS_WAIL_MAX_LEVEL) {
            listenForWidowsWail(player);
        }
    }

    private void listenForWidowsWail(Player player) {
        if (player.getEquipment().cantWearWidowsWail()) {
            unequipWidowsWail(player);
        }
    }

    private void unequipWidowsWail(Player player) {
        val widowsWailId = 41383;
        if (player.getEquipment().containsOneItem(widowsWailId)) {
            player.getEquipment().deleteItem(widowsWailId, 1);
            player.addItem(new Item(widowsWailId, 1));
        }
    }

    private void listenForMaxGraphics(Player player, int level) {
        if (level == 99 || level == 120) {
            player.setNextGraphics(new Graphics(1765));
        }
    }

    private void listenForCongratulationsText(Player player, int skill, int level) {
//        String name = Skills.SKILL_NAME[skill];
//        if (level >= 80) {
//            player.getPackets().sendConfigByFile(4757, getIconValue(skill));
//            player.getInterfaceManager().sendChatBoxInterface(740);
//            player.getPackets().sendIComponentText(740, 0, "Congratulations, you have just advanced a" + (name.startsWith("A") ? "n" : "") + " " + name + " level!");
//            player.getPackets().sendIComponentText(740, 1, "You have now reached level " + level + ".");
//        }
    }

    private void listenForConfigs(Player player, int skill) {
        player.getInterfaceManager().setWindowInterface(InterfaceManager.LEVEL_UP_COMPONENT_ID, 1216);
        LevelUp.switchFlash(player, skill, true);
    }

    /**
     * The level-up jingle table is a 910 asset table, one entry per 910 stat, indexed by
     * raw stat id. It has 27 entries and there is no cache evidence for an Archaeology or
     * Necromancy level-up music effect, so stats 27 and 28 get no jingle rather than an
     * invented id - the same treatment {@code Skills.SHARDS} and {@code SKILL_MENU_COMPONENTS}
     * already get. Without the guard the widened model would throw
     * ArrayIndexOutOfBoundsException on the first Archaeology or Necromancy level-up.
     */
    private void listenForMusicEffects(Player player, int skill) {
        val skillLevelUpMusicEffects = new int[] { 30, 38, 66, 48, 58, 56, 52, 34, 70, 44, 42, 40, 36, 64, 54, 46, 28, 68, 61, 10, 60, 50, 32, 301, 417, 42, 42 };
        // TODO find real divination level up music effect
        if (skill < 0 || skill >= skillLevelUpMusicEffects.length) {
            return;
        }
        int musicEffect = skillLevelUpMusicEffects[skill];
        player.getPackets().sendMusicEffect(musicEffect);
    }

    private void listenForNews(Player player, int skill) {
        int staticLevel = player.skills.getLevelForXp(skill);
        if (staticLevel >= 99) {
            // "Maxed stats" means every stat at ITS OWN cache cap. The old rule here was the
            // 910 one - 120 for Dungeoneering, Invention and Slayer, 99 for everything else -
            // which the 947 stat definitions abolish: 15 skills moved off 99 (eight to 110,
            // seven to 120), so the old test would have called a character maxed while it was
            // 11 or 21 levels short in each of them. The loop covers all Skills.SKILL_COUNT
            // stats, Archaeology and Necromancy included, which is the same rule
            // DistinctionCape.isMaxed uses; until content awards experience in those two the
            // announcement cannot fire, and that is deliberate rather than a second rule.
            boolean reachedAll = true;
            for (int i = 0; i < Skills.SKILL_COUNT; i++) {
                if (player.getSkills().getLevelForXp(i) < Skills.getLevelCap(i)) {
                    reachedAll = false;
                    break;
                }
            }

            if (reachedAll) {
                World.sendWorldMessage("<img=6>" + Colors.RED + "<shad=000000>News: " + player.getDisplayName() + " has just achieved maxed stats on " + player.getXPMode() + " mode!", false);
                QueryExecutor.submit(new News(player, "<b><img src=\"../bin/images/news/maxed.png\" " + "width=17> " + player.getDisplayName() + " has just achieved maxed stats on " + player.getXPMode() + " mode."));
                HcimNewsManager.getInstance().addNews(player,"<#player> achieved maxed stats!");
            } else if (staticLevel == 120) {
                World.sendWorldMessage("<img=6>" + Colors.ORANGE + "<shad=000000>News: " + player.getDisplayName() + " has achieved 120 " + Skills.SKILL_NAME[skill] + " on " + player.getXPMode() + " mode!", false);
                QueryExecutor.submit(new News(player, "<b><img src=\"../hiscores/incl/img/skill_icons/" + Skills.SKILL_NAME[skill] + "-icon.png\" " + "width=17> " + player.getDisplayName() + " has achieved 120 " + Skills.SKILL_NAME[skill] + " on " + player.getXPMode() + " mode."));
                HcimNewsManager.getInstance().addNews(player,"<#player> achieved 120 " + Skills.SKILL_NAME[skill] + "!", 3);
            } else if (staticLevel == 99) {
                World.sendWorldMessage("<img=6>" + Colors.ORANGE + "<shad=000000>News: " + player.getDisplayName() + " has achieved 99 " + Skills.SKILL_NAME[skill] + " on " + player.getXPMode() + " mode!", false);
                QueryExecutor.submit(new News(player, "<b><img src=\"../hiscores/incl/img/skill_icons/" + Skills.SKILL_NAME[skill] + "-icon.png\" " + "width=17> " + player.getDisplayName() + " has achieved 99 " + Skills.SKILL_NAME[skill] + " on " + player.getXPMode() + " mode."));
                HcimNewsManager.getInstance().addNews(player,"<#player> achieved 99 " + Skills.SKILL_NAME[skill] + "!", 3);
            }
        }
    }

    public int getIconValue(int skill) {
        if (skill == Skills.ATTACK)
            return 1;
        if (skill == Skills.STRENGTH)
            return 2;
        if (skill == Skills.RANGE)
            return 3;
        if (skill == Skills.MAGIC)
            return 4;
        if (skill == Skills.DEFENCE)
            return 5;
        if (skill == Skills.HITPOINTS)
            return 6;
        if (skill == Skills.PRAYER)
            return 7;
        if (skill == Skills.AGILITY)
            return 8;
        if (skill == Skills.HERBLORE)
            return 9;
        if (skill == Skills.THIEVING)
            return 10;
        if (skill == Skills.CRAFTING)
            return 11;
        if (skill == Skills.RUNECRAFTING)
            return 12;
        if (skill == Skills.MINING)
            return 13;
        if (skill == Skills.SMITHING)
            return 14;
        if (skill == Skills.FISHING)
            return 15;
        if (skill == Skills.COOKING)
            return 16;
        if (skill == Skills.FIREMAKING)
            return 17;
        if (skill == Skills.WOODCUTTING)
            return 18;
        if (skill == Skills.FLETCHING)
            return 19;
        if (skill == Skills.SLAYER)
            return 20;
        if (skill == Skills.FARMING)
            return 21;
        if (skill == Skills.CONSTRUCTION)
            return 22;
        if (skill == Skills.SLAYER)
            return 23;
        if (skill == Skills.SUMMONING)
            return 24;
        if (skill == Skills.DUNGEONEERING)
            return 25;
        if (skill == Skills.DIVINATION)
            return 26;
        if (skill == Skills.INVENTION)
            return 27;
        return -1;
    }
    
    public int getSpecialIconValue(int skill, int level) {
        if (skill == Skills.ATTACK)
            return 8388609 + (8388608 * (level - 1));
        if (skill == Skills.STRENGTH)
            return 8388610 + (8388608 * (level - 1));
        if (skill == Skills.RANGE)
            return 8388611 + (8388608 * (level - 1));
        if (skill == Skills.MAGIC)
            return 8388612 + (8388608 * (level - 1));
        if (skill == Skills.DEFENCE)
            return 8388613 + (8388608 * (level- 1));
        if (skill == Skills.HITPOINTS)
            return 8388614 + (8388608 * (level - 1));
        if (skill == Skills.PRAYER)
            return 8388615 + (8388608 * (level - 1));
        if (skill == Skills.AGILITY)
            return 8388616 + (8388608 * (level - 1));
        if (skill == Skills.HERBLORE)
            return 8388617 + (8388608 * (level - 1));
        if (skill == Skills.THIEVING)
            return 8388618 + (8388608 * (level - 1));
        if (skill == Skills.CRAFTING)
            return 8388619 + (8388608 * (level - 1));
        if (skill == Skills.RUNECRAFTING)
            return 8388620 + (8388608 * (level - 1));
        if (skill == Skills.MINING)
            return 8388621 + (8388608 * (level - 1));
        if (skill == Skills.SMITHING)
            return 8388622 + (8388608 * (level - 1));
        if (skill == Skills.FISHING)
            return 8388623 + (8388608 * (level - 1));
        if (skill == Skills.COOKING)
            return 8388624 + (8388608 * (level - 1));
        if (skill == Skills.FIREMAKING)
            return 8388625 + (8388608 * (level - 1));
        if (skill == Skills.WOODCUTTING)
            return 8388626 + (8388608 * (level - 1));
        if (skill == Skills.FLETCHING)
            return 8388627 + (8388608 * (level - 1));
        if (skill == Skills.SLAYER)
            return 8388628 + (8388608 * (level - 1));
        if (skill == Skills.FARMING)
            return 8388629 + (8388608 * (level - 1));
        if (skill == Skills.CONSTRUCTION)
            return 8388630 + (8388608 * (level - 1));
        if (skill == Skills.HUNTER)
            return 8388631 + (8388608 * (level - 1));
        if (skill == Skills.SUMMONING)
            return 8388632 + (8388608 * (level - 1));
        else if (skill == Skills.DUNGEONEERING)
            return 8388633 + (8388608 * (level - 1));
        else if (skill == Skills.DIVINATION)
            return 8388634 + (8388608 * (level - 1));
        else if (skill == Skills.INVENTION) {
            return 8388635 + (8388608 * (level - 1));
        }
        return 27;
    }
}
