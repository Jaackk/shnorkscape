package com.rs.game.player.content;

import com.rs.game.WorldTile;
import com.rs.game.activites.quest.AbstractQuest;
import com.rs.game.player.DataAnswerInterface;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.DungeonController;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles the custom AbstractQuest Tab.
 *
 * @author Noel
 */
public class QuestTab {

    public static Dialogue memberZone = new Dialogue() {

        @Override
        public void start() {
            sendOptionsDialogue("Where would you like to go?", "Members zone", "Platinum members zone", "Diamond members zone", "Boss portals", "Nevermind");
            stage = 0;
        }

        @Override
        public void run(int interfaceId, int componentId) {
            if (stage == 0) {
                switch (componentId) {
                    case OPTION_1:
                        if (player.getMoneySpent() < 20) {
                            player.getDialogueManager().startDialogue("SimpleMessage", "You need to have donated a total of at least $20 to access the Members zone.");
                        } else {
                            Magic.sendAncientTeleportSpell(player, 1, 0, new WorldTile(4382, 5919, 0));
                            finish();
                        }
                        break;
                    case OPTION_2:
                        if(player.isGroupIronman()) {
                            player.sendMessage("Group Ironmen cannot go here.");
                            return;
                        }
                        if (player.getMoneySpent() < 250) {
                            player.getDialogueManager().startDialogue("SimpleMessage", "You need to have donated a total of at least $250 to access the Platinum Members zone.");
                        } else {
                            Magic.sendAncientTeleportSpell(player, 1, 0, new WorldTile(3890, 6815, 0));
                            finish();
                        }
                        break;
                    case OPTION_3:
                        if(player.isGroupIronman()) {
                            player.sendMessage("Group Ironmen cannot go here.");
                            return;
                        }
                        if (player.getMoneySpent() < 500) {
                            player.getDialogueManager().startDialogue("SimpleMessage", "You need to have donated a total of at least $500 to access the Diamond Members zone.");
                        } else {
                            Magic.sendAncientTeleportSpell(player, 1, 0, new WorldTile(3554, 6048, 1));
                            finish();
                        }
                        break;
                    case OPTION_4:
                        if(player.isGroupIronman()) {
                            player.sendMessage("Group Ironmen cannot go here.");
                            return;
                        }
                        if (player.getMoneySpent() < 500) {
                            player.getDialogueManager().startDialogue("SimpleMessage", "You need to have donated a total of at least $500 to access the boss portals zone.");
                        } else {
                            Magic.sendAncientTeleportSpell(player, 1, 0, new WorldTile(2085, 4461, 0));
                            finish();
                        }
                        break;
                    case OPTION_5:
                        finish();
                        break;
                }
            }
        }

        @Override
        public void finish() {
            player.getInterfaceManager().closeChatBoxInterface();
        }

    };

    /**
     * Sends the tab.
     *
     * @param player The player.
     */
    public static void sendTab(Player player) {
//        player.getPackets().sendIComponentText(506, 0, Colors.PINK + Colors.SHAD + "Ataraxia");
//        player.getPackets().sendIComponentText(506, 2, Colors.WHITE + "Achievements");
//        if (player.isUsingTeleportInterface()) {
//            player.getPackets().sendIComponentText(506, 4, Colors.WHITE + "Teleports");
//            player.getPackets().sendIComponentText(506, 6, Colors.WHITE + "Quests");
//            player.getPackets().sendIComponentText(506, 8, "");
//            player.getPackets().sendIComponentText(506, 10, "");
//            player.getPackets().sendIComponentText(506, 12, "");
//            player.getPackets().sendIComponentText(506, 14, "");
//        } else {
//            player.getPackets().sendIComponentText(506, 4, Colors.WHITE + "Training");
//            player.getPackets().sendIComponentText(506, 6, Colors.WHITE + "Bosses");
//            player.getPackets().sendIComponentText(506, 8, Colors.WHITE + "Minigames");
//            player.getPackets().sendIComponentText(506, 10, Colors.WHITE + "PvP");
//            player.getPackets().sendIComponentText(506, 12, Colors.WHITE + "Skilling");
//            player.getPackets().sendIComponentText(506, 14, Colors.WHITE + "Quests");
//        }
    }

    /**
     * Handles the custom AbstractQuest Tabs buttons.
     *
     * @param player The player using the tab.
     * @param componentId The interfaces childId's.
     */
    public static void handleTab(Player player, int componentId) {
        if (player.getControlerManager().getControler() instanceof DungeonController) {
            player.sendMessage("You cannot teleport inside dungeoneering.");
            return;
        }
        switch (componentId) {
            case 2: /** Achievements **/
                AchievementInterface.open(player);
                break;
            case 4: /** Training or Teleports **/
                // InterfaceManager.setPlayerInterfaceSelected(3);
                // TrainingTeleports.sendInterface(player);
                if (player.isUsingTeleportInterface())
                    player.getTeleportInterface().sendInterface();
                    // erzfzef.open(player);
                else
                    player.getDialogueManager().startDialogue("TrainingTeleports");
                break;
            case 6: /** Bosses or Quests **/
                // InterfaceManager.setPlayerInterfaceSelected(1);
                // BossTeleports.sendInterface(player);
                if (player.isUsingTeleportInterface()) {
                    showQuests(player);
                } else
                    player.getDialogueManager().startDialogue("BossTeleports");
                break;
            case 8: /** Minigames **/
                // InterfaceManager.setPlayerInterfaceSelected(2);
                // MinigameTeleports.sendInterface(player);
                if (!player.isUsingTeleportInterface())
                    player.getDialogueManager().startDialogue("MinigameTeleports");
                break;
            case 10: /** PvP **/
                // InterfaceManager.setPlayerInterfaceSelected(4);
                // PvPTeleports.sendInterface(player);
                if (!player.isUsingTeleportInterface())
                    player.getDialogueManager().startDialogue("PkingTeleports");
                break;
            case 12: /** Skilling **/
                // InterfaceManager.setPlayerInterfaceSelected(5);
                // SkillingTeleportsD.sendInterface(player);
                if (!player.isUsingTeleportInterface())
                    player.getDialogueManager().startDialogue("SkillingTeleportsD");
                break;
            case 14: /** Quests **/
                showQuests(player);
                break;
            default:
                player.sendMessage("Unhandled interface button: " + componentId + "; report this to an Administrator!");
                break;
        }
    }

    public static void showQuests(Player player) {
        Map<String, Runnable> answers = new HashMap<>();
        for (Class<? extends AbstractQuest> questType : AbstractQuest.QUEST_TAB.values()) {
            val quest = player.quests.get(questType);
            String color;
            if (quest.getCurrentStage() == -1) {
                color = Colors.RED;
            } else if (quest.isCompleted()) {
                color = Colors.GREEN;
            } else {
                color = Colors.ORANGE;
            }
            answers.put(color + quest.name() + "</col>", () -> quest.displayInfo(player));
        }

        DataAnswerInterface answerInterface = new DataAnswerInterface("Quests", answers, () -> showQuests(player));
        answerInterface.show(player);
    }
}