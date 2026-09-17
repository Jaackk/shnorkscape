package com.rs.game.player.content.distinctioncape;

import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.InterfaceManager;
import com.rs.game.player.content.RuneCrafting;
import com.rs.network.packet.PacketDispatcher;
import com.rs.utils.Utils;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * <p>
 * Created on Nov 7, 2018.
 */
public class DistinctionCapeInterface {

    public static final int INTERFACE_ID = 125;
    private static final String C = "<col=00ff00>Complete";
    private static final String IC = "<col=ff0000>Incomplete";

    public static void open(Player player) {
        player.setSelectedDistinctionCape(Cape.MAX_CAPE);
        PacketDispatcher packets = player.getPackets();
        packets.sendIComponentText(INTERFACE_ID, 0, "");
        packets.sendIComponentText(INTERFACE_ID, 12, "Distinction Cape Requirements");
        // the tasks list.
        for (int component = 70; component <= 168; component++)
            player.getPackets().sendHideIComponent(INTERFACE_ID, component, true);
        for (int i=0;i<3;i++)
        player.getPackets().sendHideIComponent(INTERFACE_ID, 173+i, true);
        sendCapeShowcase(player, false);
        sendCapeName(player, false);
        sendTasks(player);
        player.getInterfaceManager().sendInterface(INTERFACE_ID);
    }

    private static void sendCapeShowcase(Player player, boolean hide) {
        player.getPackets().sendHideIComponent(INTERFACE_ID, 27, hide);
        if (!hide) {
            player.getPackets().sendIComponentModel(INTERFACE_ID, 27, player.getSelectedDistinctionCape().modelId);
            player.getPackets().sendExecuteScript(17045, InterfaceManager.getComponentUId(INTERFACE_ID, 27), player.getSelectedDistinctionCape().modelId);
        }
    }

    private static void sendCapeName(Player player, boolean hide) {
        if (hide)
            player.getPackets().sendIComponentText(INTERFACE_ID, 43, "");
        else
            player.getPackets().sendIComponentText(INTERFACE_ID, 43, indent(player.getSelectedDistinctionCape().name(), (22 - player.getSelectedDistinctionCape().name().length()), true));
    }

    public static void handleButtons(Player player, int buttonId) {
        if (buttonId == 170 || buttonId == 171) {
            // the requirements list.
            for (int component = 70; component <= 168; component++)
                player.getPackets().sendHideIComponent(INTERFACE_ID, component, true);
            for (int i=0;i<3;i++)
                player.getPackets().sendHideIComponent(INTERFACE_ID, 173+i, true);
            player.setSelectedDistinctionCape(buttonId == 170 ? Cape.getNextCape(player.getSelectedDistinctionCape()) : Cape.getPreviousCape(player.getSelectedDistinctionCape()));
            sendCapeShowcase(player, false);
            sendCapeName(player, false);
            sendTasks(player);
        }
    }

    private static String indent(String string, int spaces, boolean format) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i <= spaces; i++)
            builder.append(" ");
        return (format ? Utils.formatString(builder + string) : (builder + string));
    }

    private static void sendTasks(Player player) {
        int component = 68;
        switch (player.getSelectedDistinctionCape()) {
            case COMPLETIONIST_CAPE:
                sendIndividualTask(player, "Unlock Max Cape", DistinctionCape.isMaxed(player) ? C : IC, DistinctionCape.isMaxed(player), component += 4);
                sendIndividualTask(player, "Slayer", player.getSkills().getLevelForXp(Skills.SLAYER) >= 120 ? C + " (" + player.getSkills().getLevelForXp(Skills.SLAYER) + "/120)" : IC + " (" + player.getSkills().getLevelForXp(Skills.SLAYER) + "/120)", player.getSkills().getLevelForXp(Skills.SLAYER) >= 120, component += 4);
                sendIndividualTask(player, "Dungeoneering", player.getSkills().getLevelForXp(Skills.DUNGEONEERING) >= 120 ? C + " (" + player.getSkills().getLevelForXp(Skills.DUNGEONEERING) + "/120)" : IC + " (" + player.getSkills().getLevelForXp(Skills.DUNGEONEERING) + "/120)", player.getSkills().getLevelForXp(Skills.DUNGEONEERING) >= 120, component += 4);
                sendIndividualTask(player, "Invention", player.getSkills().getLevelForXp(Skills.INVENTION) >= 120 ? C + " (" + player.getSkills().getLevelForXp(Skills.INVENTION) + "/120)" : IC + " (" + player.getSkills().getLevelForXp(Skills.INVENTION) + "/120)", player.getSkills().getLevelForXp(Skills.INVENTION) >= 120, component += 4);
                sendIndividualTask(player, "Defeat the Queen Black Dragon", player.isKilledQueenBlackDragon() ? C : IC, player.isKilledQueenBlackDragon(), component += 4);
                sendIndividualTask(player, "Complete Fight Caves", player.isCompletedFightCaves() ? C : IC, player.isCompletedFightCaves(), component += 4);
                sendIndividualTask(player, "Defeat the Culinaromancer", player.isKilledCulinaromancer() ? C : IC, player.isKilledCulinaromancer(), component += 4);
                sendIndividualTask(player, "Complete Fight Kiln", player.isCompletedFightKiln() ? C : IC, player.isCompletedFightKiln(), component += 4);
                sendIndividualTask(player, "Finish all easy and medium tasks", player.getAchievements().hasCompletionistTasksDone() ? C : IC, player.getAchievements().hasCompletionistTasksDone(), component += 4);
               // boolean allQuestsComplete = player.quests.isAllCompleted();
               // sendIndividualTask(player, "Complete all quests", allQuestsComplete ? C : IC, allQuestsComplete, component += 4);
                break;
            case COMPLETIONIST_CAPE_T:
                sendIndividualTask(player, "Unlock Completionist's Cape", DistinctionCape.isWorthyCompCape(player) ? C : IC, DistinctionCape.isWorthyCompCape(player), component += 4);
                sendIndividualTask(player, "Mine ores", getCompletionString(player.getOresMined(), 5000), player.getOresMined() >= 5000, component += 4);
                sendIndividualTask(player, "Smelt bars", getCompletionString(player.getSmithingActions(), 5000), player.getSmithingActions() >= 5000, component += 4);
                sendIndividualTask(player, "Chop logs", getCompletionString(player.getLogsChopped(), 5000), player.getLogsChopped() >= 5000, component += 4);
                sendIndividualTask(player, "Burn logs", getCompletionString(player.getLogsBurned(), 5000), player.getLogsBurned() >= 5000, component += 4);
                sendIndividualTask(player, "Sacrifice bones", getCompletionString(player.getBonesOffered(), 5000), player.getBonesOffered() >= 5000, component += 4);
                sendIndividualTask(player, "Create potions", getCompletionString(player.getPotionsMade(), 5000), player.getPotionsMade() >= 5000, component += 4);
                sendIndividualTask(player, "Steal", getCompletionString(player.getTimesStolen(), 5000), player.getTimesStolen() >= 5000, component += 4);
                sendIndividualTask(player, "Craft objects", getCompletionString(player.getItemsMade(), 5000), player.getItemsMade() >= 5000, component += 4);
                sendIndividualTask(player, "Fletch objects", getCompletionString(player.getItemsFletched(), 5000), player.getItemsFletched() >= 5000, component += 4);
                sendIndividualTask(player, "Catch creatures", getCompletionString(player.getCreaturesCaught(), 5000), player.getCreaturesCaught() >= 5000, component += 4);
                sendIndividualTask(player, "Catch fish", getCompletionString(player.getFishCaught(), 5000), player.getFishCaught() >= 5000, component += 4);
                sendIndividualTask(player, "Cook food", getCompletionString(player.getFoodCooked(), 5000), player.getFoodCooked() >= 5000, component += 4);
                sendIndividualTask(player, "Harvest products", getCompletionString(player.getProduceGathered(), 5000), player.getProduceGathered() >= 5000, component += 4);
                sendIndividualTask(player, "Infuse pouches", getCompletionString(player.getPouchesMade(), 2500), player.getPouchesMade() >= 2500, component += 4);
                sendIndividualTask(player, "Run laps", getCompletionString(player.getLapsRan(), 1000), player.getLapsRan() >= 1000, component += 4);
                sendIndividualTask(player, "Collect memories", getCompletionString(player.getMemoriesCollected(), 5000), player.getMemoriesCollected() >= 5000, component += 4);
                sendIndividualTask(player, "Craft runes", getCompletionString(player.getRunesMade(), 5000 * RuneCrafting.BASE_RC_MULTIPLIER), player.getRunesMade() >= 5000 * RuneCrafting.BASE_RC_MULTIPLIER, component += 4);
                sendIndividualTask(player, "Slay Demon Flash Mobs", getCompletionString(player.demonFlashMobsKills, 5), player.demonFlashMobsKills >= 5, component += 4);
                sendIndividualTask(player, "Reach floor 50 in Daemonheim", player.hasReachedFloor50() ? C : IC, player.hasReachedFloor50(), component += 4);
                sendIndividualTask(player, "Defeat bosses", getCompletionString(player.getBossKillcount(), 1000), player.getBossKillcount() >= 1000, component += 4);
                sendIndividualTask(player, "Slay WildyWyrms", player.getKillStatistics(111) >= 5 ? C + " (" + player.getKillStatistics(111) + "/5)" : IC + " (" + player.getKillStatistics(111) + "/5)", player.getKillStatistics(111) >= 5, component += 4);
                sendIndividualTask(player, "Obtain an enhanced fire cape", player.hasUnlockedEFC() ? C : IC, player.hasUnlockedEFC(), component += 4);
                sendIndividualTask(player, "Finish all achievement tasks", player.getAchievements().hasCompletionistTrimmedTasksDone() && player.getAchievements().hasCompletionistTasksDone() ? C : IC, player.getAchievements().hasCompletionistTrimmedTasksDone() && player.getAchievements().hasCompletionistTasksDone(), component += 4);
                break;
            case MAX_CAPE:
                // Each row reads "<skill>. Complete (level/cap)", where the cap is the
                // one the 947 cache's stat definition declares for that skill (28/9
                // opcode 1) - not a hardcoded 99, which was wrong for the 20 stats the
                // cache caps at 110 or 120.
                //
                // The interface physically has 26 task rows (72..172, step 4; anything
                // from 172 up is dropped by sendIndividualTask) plus the single wide row
                // at 175, and the model has 29 skills. So only the skills still BELOW
                // their cap are listed, newest requirement first come first served, and
                // row 175 always carries the roll-up. A player one skill short sees that
                // one skill; a fresh player sees the first 26 and the roll-up tells them
                // how many are really left.
                int listed = 0;
                for (int i = 0; i < Skills.SKILL_COUNT; i++) {
                    if (DistinctionCape.isAtCap(player, i))
                        continue;
                    if (listed++ == 26)
                        break;
                    int level = player.getSkills().getLevelForXp(i);
                    sendIndividualTask(player, Skills.SKILL_NAME[i],
                            IC + " (" + level + "/" + Skills.getLevelCap(i) + ")", false, component += 4);
                }
                int remaining = DistinctionCape.skillsBelowCap(player);
                sendIndividualTask(player, "Every skill at its level cap",
                        (remaining == 0 ? C : IC) + " (" + (Skills.SKILL_COUNT - remaining) + "/"
                                + Skills.SKILL_COUNT + ")", remaining == 0, 175);
                break;
        }

        if (component >= 168)
            return;
        player.getPackets().sendHideIComponent(INTERFACE_ID, component + 2, true);
    }

    private static void sendIndividualTask(Player player, String task, String completion, boolean progress, int component) {
        if (component == 175) {
            player.getPackets().sendHideIComponent(INTERFACE_ID, component, false);
            player.getPackets().sendHideIComponent(INTERFACE_ID, component - 1, false);
            player.getPackets().sendIComponentText(INTERFACE_ID, component, task + ". " + completion);
            player.getPackets().sendIComponentSprite(INTERFACE_ID, component - 1, progress ? 699 : 698);
            return;
        }
        if (component >= 172)
            return;
        player.getPackets().sendHideIComponent(INTERFACE_ID, component, false);
        player.getPackets().sendHideIComponent(INTERFACE_ID, component - 1, false);
        player.getPackets().sendHideIComponent(INTERFACE_ID, component + 1, false);
        player.getPackets().sendHideIComponent(INTERFACE_ID, component + 2, false);
        player.getPackets().sendIComponentText(INTERFACE_ID, component, task + ". " + completion);
        player.getPackets().sendIComponentSprite(INTERFACE_ID, component - 1, progress ? 699 : 698);
    }

    private static String getCompletionString(int current, int total) {
        String prefix = current >= total ? C : IC;
        return prefix + " (" + current + "/" + total + ")";
    }

    public enum Cape {
        // max cape
        MAX_CAPE(65300),

        // comp cape
        COMPLETIONIST_CAPE(65297),

        // comp cape t
        COMPLETIONIST_CAPE_T(65295);

        private final int modelId;

        Cape(int modelId) {
            this.modelId = modelId;
        }

        public static Cape getNextCape(Cape current) {
            if (current == null)
                return Cape.MAX_CAPE;

            for (Cape cape : Cape.values()) {
                if (cape == null)
                    continue;

                if (cape.ordinal() == current.ordinal() + 1)
                    return cape;
            }

            return Cape.MAX_CAPE;
        }

        public static Cape getPreviousCape(Cape current) {
            if (current == null)
                return Cape.MAX_CAPE;

            for (Cape cape : Cape.values()) {
                if (cape == null)
                    continue;

                if (cape.ordinal() == current.ordinal() - 1)
                    return cape;
            }

            return Cape.COMPLETIONIST_CAPE_T;
        }

        public int getModelId() {
            return modelId;
        }
    }

}
