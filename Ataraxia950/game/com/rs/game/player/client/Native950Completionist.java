package com.rs.game.player.client;

import com.rs.game.player.Player;
import com.rs.game.player.Skills;

/** Local-development completion state limited to systems this project actually models. */
public final class Native950Completionist {
    public static final String SETTING = "completionist.granted";

    private Native950Completionist() { }

    public static void grant(Player player) { grant(player, true); }

    public static void restore(Player player) { grant(player, false); }

    private static void grant(Player player, boolean publish) {
        for (int skill = 0; skill < Skills.SKILL_COUNT; skill++) {
            player.getSkills().setXpWithoutRefresh(skill, Skills.MAXIMUM_EXP);
            player.getSkills().set(skill, Skills.getLevelForXp(skill, Skills.MAXIMUM_EXP));
        }
        player.setMax(true);
        player.setComp(true);
        player.setCompT(true);
        player.setCompletedFightCaves2();
        player.setCompletedFightKiln();
        player.setCompletedRfd();
        if (player.getAchievements() != null) player.getAchievements().quickFinish();
        if (player.getQuestManager() != null) player.getQuestManager().completeAllForLocalDevelopment();
        if (publish) Native950CombatProgression.grant(player);
        else Native950CombatProgression.restore(player);
        player.getSkills().init();
        player.refreshHitPoints();
        if (player.getPrayer() != null) player.getPrayer().refreshPrayerPoints();
    }
}
