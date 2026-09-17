package com.rs.game.player.dialogue.impl;

import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.activites.gim.GIM;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.hcim_news.HcimNewsManager;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.player.skills.SkillsListener;
import com.rs.utils.Colors;
import com.rs.utils.mysql.QueryExecutor;
import com.rs.utils.mysql.impl.News;

public final class LevelUp extends Dialogue {
    private final SkillsListener skillsListener = new SkillsListener();

    public static void switchFlash(Player player, int skill, boolean on) {
        int id = 0;
        if (skill == Skills.ATTACK)
            id = 3267;
        else if (skill == Skills.STRENGTH)
            id = 3268;
        else if (skill == Skills.DEFENCE)
            id = 3269;
        else if (skill == Skills.RANGE)
            id = 3270;
        else if (skill == Skills.PRAYER)
            id = 3271;
        else if (skill == Skills.MAGIC)
            id = 3272;
        else if (skill == Skills.HITPOINTS)
            id = 3273;
        else if (skill == Skills.AGILITY)
            id = 3274;
        else if (skill == Skills.HERBLORE)
            id = 3275;
        else if (skill == Skills.THIEVING)
            id = 3276;
        else if (skill == Skills.CRAFTING)
            id = 3277;
        else if (skill == Skills.FLETCHING)
            id = 3278;
        else if (skill == Skills.MINING)
            id = 3279;
        else if (skill == Skills.SMITHING)
            id = 3280;
        else if (skill == Skills.FISHING)
            id = 3281;
        else if (skill == Skills.COOKING)
            id = 3282;
        else if (skill == Skills.FIREMAKING)
            id = 3283;
        else if (skill == Skills.WOODCUTTING)
            id = 3284;
        else if (skill == Skills.RUNECRAFTING)
            id = 3285;
        else if (skill == Skills.SLAYER)
            id = 3286;
        else if (skill == Skills.FARMING)
            id = 3287;
        else if (skill == Skills.CONSTRUCTION)
            id = 3288;
        else if (skill == Skills.HUNTER)
            id = 3289;
        else if (skill == Skills.SUMMONING)
            id = 3290;
        else if (skill == Skills.DUNGEONEERING)
            id = 3291;
        else if (skill == Skills.DIVINATION)
            id = 20114;
        else if (skill == Skills.INVENTION)
            id = 30199;
        if (id != 0)
            player.getPackets().sendConfigByFile(id, on ? 1 : 0);
    }

    /**
     * Sends the Mastery cape World announcement.
     *
     * @param player The player.
     * @param skill The skill ID.
     */
    public static void send104m(Player player, int skill) {
        boolean isNovice = player.getXPMode().equals("Novice");
        player.getPackets().sendMusicEffect(321);
        player.setNextGraphics(new Graphics(1765));
        if (!isNovice)
            World.sendWorldMessage("<img=6>" + Colors.RED + "<shad=000000>News: " + player.getDisplayName()
                    + " has achieved level 120 " + Skills.SKILL_NAME[skill] + " on "
                    + player.getXPMode(), false);
        HcimNewsManager.getInstance().addNews(player,"<#player> achieved level 120 "+ Skills.SKILL_NAME[skill] +"!", 2);
                player.sendMessage(
                "<col=825200><shad=000000>Well done! You've achieved 104,273,167 XP in this skill! You can now purchase a Mastery Cape from the Wise Old Man.");
        if (!isNovice)
            QueryExecutor.submit(new News(player,
                    "<b><img src=\"../hiscores/incl/img/skill_icons/" + Skills.SKILL_NAME[skill] + "-icon.png\" "
                            + "width=17> " + player.getDisplayName() + " has achieved 104,273,167 "
                            + Skills.SKILL_NAME[skill] + " XP on " + player.getXPMode() + " mode."));
    }

    /**
     * Sends the 250m EXP milestone World announcement.
     *
     * @param player The player.
     * @param skill The skill ID.
     */
    public static void send250m(Player player, int skill) {
        boolean isNovice = player.getXPMode().equals("Novice");
        if (!isNovice) {
            player.getPackets().sendMusicEffect(320);
            player.setNextGraphics(new Graphics(1765));
            World.sendWorldMessage("<img=6>" + Colors.RED + "<shad=000000>News: " + player.getDisplayName()
                    + " has achieved 250,000,000 experience in the " + Skills.SKILL_NAME[skill] + " skill on "
                    + player.getXPMode() + " mode!", false);
            HcimNewsManager.getInstance().addNews(player,"<#player> achieved 250M XP in "+ Skills.SKILL_NAME[skill] +"!",2);
                    QueryExecutor.submit(new News(player,
                    "<b><img src=\"../hiscores/incl/img/skill_icons/" + Skills.SKILL_NAME[skill] + "-icon.png\" "
                            + "width=17> " + player.getDisplayName() + " has achieved 250,000,000 "
                            + Skills.SKILL_NAME[skill] + " XP on " + player.getXPMode() + " mode."));
        }
    }

    /**
     * Sends the 500m EXP milestone World announcement.
     *
     * @param player The player.
     * @param skill The skill ID.
     */
    public static void send500m(Player player, int skill) {
        boolean isNovice = player.getXPMode().equals("Novice");
        if (!isNovice) {
            player.getPackets().sendMusicEffect(320);
            player.setNextGraphics(new Graphics(1765));
            World.sendWorldMessage("<img=6>" + Colors.RED + "<shad=000000>News: " + player.getDisplayName()
                    + " has achieved 500,000,000 experience in the " + Skills.SKILL_NAME[skill] + " skill on "
                    + player.getXPMode() + " mode!", false);
            HcimNewsManager.getInstance().addNews(player,"<#player> achieved 500M XP in "+ Skills.SKILL_NAME[skill] +"!",2);
                    QueryExecutor.submit(new News(player,
                    "<b><img src=\"../hiscores/incl/img/skill_icons/" + Skills.SKILL_NAME[skill] + "-icon.png\" "
                            + "width=17> " + player.getDisplayName() + " has achieved 500,000,000 "
                            + Skills.SKILL_NAME[skill] + " XP on " + player.getXPMode() + " mode."));
        }
    }

    /**
     * Sends the 1000m EXP milestone World announcement.
     *
     * @param player The player.
     * @param skill The skill ID.
     */
    public static void send1000m(Player player, int skill) {
        boolean isNovice = player.getXPMode().equals("Novice");
        if(!isNovice) {
            player.getPackets().sendMusicEffect(320);
            player.setNextGraphics(new Graphics(1765));
            World.sendWorldMessage("<img=6>" + Colors.RED + "<shad=000000>News: " + player.getDisplayName()
                    + " has achieved 1,000,000,000 experience in the " + Skills.SKILL_NAME[skill] + " skill on "
                    + player.getXPMode() + " mode!", false);
            HcimNewsManager.getInstance().addNews(player,"<#player> achieved 1B XP in "+ Skills.SKILL_NAME[skill] +"!");
                    QueryExecutor.submit(new News(player,
                    "<b><img src=\"../hiscores/incl/img/skill_icons/" + Skills.SKILL_NAME[skill] + "-icon.png\" "
                            + "width=17> " + player.getDisplayName() + " has achieved 1,000,000,000 "
                            + Skills.SKILL_NAME[skill] + " XP on " + player.getXPMode() + " mode."));
        }
    }

    /**
     * Sends the 1500m EXP milestone World announcement.
     *
     * @param player The player.
     * @param skill The skill ID.
     */
    public static void send1500m(Player player, int skill) {
        boolean isNovice = player.getXPMode().equals("Novice");
        if (!isNovice) {
            player.getPackets().sendMusicEffect(320);
            player.setNextGraphics(new Graphics(1765));
            World.sendWorldMessage("<img=6>" + Colors.RED + "<shad=000000>News: " + player.getDisplayName()
                    + " has achieved 1,500,000,000 experience in the " + Skills.SKILL_NAME[skill] + " skill on "
                    + player.getXPMode() + " mode!", false);
            HcimNewsManager.getInstance().addNews(player,"<#player> achieved 1500M XP in "+ Skills.SKILL_NAME[skill] +"!",2);
                    QueryExecutor.submit(new News(player,
                    "<b><img src=\"../hiscores/incl/img/skill_icons/" + Skills.SKILL_NAME[skill] + "-icon.png\" "
                            + "width=17> " + player.getDisplayName() + " has achieved 1,500,000,000 "
                            + Skills.SKILL_NAME[skill] + " XP on " + player.getXPMode() + " mode."));
        }
    }

    public static void send2000m(Player player, int skill) {
        boolean isNovice = player.getXPMode().equals("Novice");
        if (!isNovice) {
            player.getPackets().sendMusicEffect(320);
            player.setNextGraphics(new Graphics(1765));
            World.sendWorldMessage("<img=6>" + Colors.RED + "<shad=000000>News: " + player.getDisplayName()
                    + " has achieved 2,000,000,000 experience in the " + Skills.SKILL_NAME[skill] + " skill on "
                    + player.getXPMode() + " mode!", false);
            HcimNewsManager.getInstance().addNews(player,"<#player> achieved 2B XP in "+ Skills.SKILL_NAME[skill] +"!",2);
                    QueryExecutor.submit(new News(player,
                    "<b><img src=\"../hiscores/incl/img/skill_icons/" + Skills.SKILL_NAME[skill] + "-icon.png\" "
                            + "width=17> " + player.getDisplayName() + " has achieved 2,000,000,000 "
                            + Skills.SKILL_NAME[skill] + " XP on " + player.getXPMode() + " mode."));
        }
    }

    @Override
    public void start() {
        int skill = (Integer) parameters[0];
        int level = player.getSkills().getLevelForXp(skill);
        player.getTemporaryAttributtes().put("leveledUp", skill);
        player.getTemporaryAttributtes().put("leveledUp[" + skill + "]", Boolean.TRUE);
        player.setNextGraphics(new Graphics(199));
        String name = Skills.SKILL_NAME[skill];
        player.getPackets().sendGlobalConfig(5188, skillsListener.getSpecialIconValue(skill, level));
        player.getPackets().sendConfigByFile(31168, skillsListener.getIconValue(skill));
        player.getPackets().sendConfigByFile(31169, level);
        skillsListener.listenForNewLevel(player, skill, level);
        player.setNextGraphics(new Graphics(199));
        if (level == 99 || level == 120)
            player.setNextGraphics(new Graphics(1765));
        player.sendMessage("You've just advanced a" + (name.startsWith("A") ? "n" : "") + " " + name + " level! "
                + "You have reached level " + level + ".");
//        player.getPackets().sendConfigByFile(4757, skillsListener.getIconValue(skill));
    }

    @Override
    public void run(int interfaceId, int componentId) {
        end();
    }

    @Override
    public void finish() {
    }
}