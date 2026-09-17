package com.rs.game.player.content.interfaces.Starter;

import com.rs.Settings;
import com.rs.game.World;
import com.rs.game.activites.gim.DCreateGroup;
import com.rs.game.activites.gim.DJoinGroup;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.item.Item;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.content.FadingScreen;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.StarterMap;

/**
 * ataraxia-server
 * paolo 13/06/2019
 * #Shnek6969
 */
public class StarterInterface {

    public static final int INTERFACE_ID = 124;
    private static final int ITEM_KEY = 90;
    private static final int ITEM_CONTAINER = 46;
    private static final int[] EXP_MODE_BUTTONS = {37, 38, 39, 42};
    private static final int[] GAME_MODE_BUTTONS = {40, 41, 56, 59};
    private static final int[] GAME_MODE_TEXT = {30, 31, 55, 58};
    private static final int[] EXP_MODE_TEXT = {27, 28, 29, 32};
    //starter items for the server to display
    private static final Item[] starterItems = new Item[]{
            new Item(41383, 1),
            new Item(9703, 1),
            new Item(9705, 1),
            new Item(15598, 1),
            new Item(8013, 15),
            new Item(554, 500),
            new Item(558, 500),
            new Item(555, 500),
            new Item(557, 500),
            new Item(562, 250),
            new Item(560, 250),
            new Item(565, 200),
            new Item(386, 500),
//            new Item(2678, 1),
            new Item(33262, 1),
            new Item(36029),
            new Item(36030),
            new Item(36028),
            new Item(36027),
            new Item(36026),
            new Item(882, 100),
            new Item(2552),
            new Item(22302),
            new Item(1052)
    };

    /**
     * sends the interface to the player
     *
     * @param player
     */
    public static void sendInterface(Player player) {
        player.getInterfaceManager().sendInterface(INTERFACE_ID);
        sendExpModes(player);
        sendGameModes(player);
        player.getPackets().sendItems(ITEM_KEY, false, starterItems);
        player.getPackets().sendInterSetItemsOptionsScript(INTERFACE_ID, ITEM_CONTAINER, ITEM_KEY, 6, 6, "Examine");
        player.getPackets().sendUnlockIComponentOptionSlots(INTERFACE_ID, ITEM_CONTAINER, 0, 160, 0);
    }

    /**
     * sends all the possible modes to the interface
     *
     * @param player
     */
    private static void sendExpModes(Player player) {
        int componentIndex = 0;
        for (ExpMode mode : ExpMode.values()) {
            player.getPackets().sendIComponentText(INTERFACE_ID, EXP_MODE_TEXT[componentIndex], mode.getFullname());
            componentIndex++;
        }
    }

    /**
     * sends the games modes to the interface
     *
     * @param player
     */
    private static void sendGameModes(Player player) {
        int componentIndex = 0;
        for (GameMode mode : GameMode.values()) {
            player.getPackets().sendIComponentText(INTERFACE_ID, GAME_MODE_TEXT[componentIndex], mode.getFullName());
            componentIndex++;
        }
    }

    /**
     * sends the discription of the mode to the container
     *
     * @param player
     */
    private static void showInformation(Player player) {
        String description = "";
        GameMode mode = (GameMode) player.getTemporaryAttributtes().get("GameMode");
        ExpMode expMode = (ExpMode) player.getTemporaryAttributtes().get("ExpMode");
        if (mode != null)
            description += mode.getDescription();
        if (expMode != null)
            description += "<br>" + expMode.getDescription();
        player.getPackets().sendText(INTERFACE_ID, 43, description);
    }

    /**
     * handles the player clicking on the xp mode buttons
     *
     * @param player
     * @param componentId
     */
    public static void setClickedXPMode(Player player, int componentId) {

        for (int componentIndex : EXP_MODE_BUTTONS) { //hiding the others
            player.getPackets().sendHideIComponent(INTERFACE_ID, componentIndex, false);
        }
        player.getPackets().sendHideIComponent(INTERFACE_ID, componentId, true);
    }

    public static void setClickedGameMode(Player player, int componentId) {
        for (int componentIndex : GAME_MODE_BUTTONS) { //hiding the others
            player.getPackets().sendHideIComponent(INTERFACE_ID, componentIndex, false);
        }
        player.getPackets().sendHideIComponent(INTERFACE_ID, componentId, true);
        if (componentId == 56) { //if hc iron hide others
            for (int componentIndex : EXP_MODE_BUTTONS) { //hiding the others
                player.getPackets().sendHideIComponent(INTERFACE_ID, componentIndex, false);
            }
        }
    }

    /**
     * handles the buttonclicks
     *
     * @param player
     * @param componentId
     */
    public static void handleButtons(Player player, int componentId) {
        if (componentId == 45) {
            setGameMode(player);
            return;
        }
        final GameMode mode = (GameMode) player.getTemporaryAttributtes().get("GameMode");
        switch (componentId) {
            case 37:
                if (mode != null && mode == GameMode.HARDCODE_IRONMAN) {
                    player.getDialogueManager().startDialogue("SimpleMessage", "Hardcore ironmen have a fixed exp and drop rate, it's not possible to select a different game mode.");
                    return;
                }
                player.getTemporaryAttributtes().put("ExpMode", ExpMode.NOVICE);
                setClickedXPMode(player, componentId);
                showInformation(player);
                break;
            case 38:
                if (mode != null && mode == GameMode.HARDCODE_IRONMAN) {
                    player.getDialogueManager().startDialogue("SimpleMessage", "Hardcore ironmen have a fixed exp and drop rate, it's not possible to select a different game mode.");
                    return;
                }
                player.getTemporaryAttributtes().put("ExpMode", ExpMode.INTERMEDIATE);
                setClickedXPMode(player, componentId);
                showInformation(player);
                break;
            case 39:
                if (mode != null && mode == GameMode.HARDCODE_IRONMAN) {
                    player.getDialogueManager().startDialogue("SimpleMessage", "Hardcore ironmen have a fixed exp and drop rate, it's not possible to select a different game mode.");
                    return;
                }
                player.getTemporaryAttributtes().put("ExpMode", ExpMode.EXPERT);
                setClickedXPMode(player, componentId);
                showInformation(player);
                break;
            case 42:
                if (mode != null && mode == GameMode.HARDCODE_IRONMAN) {
                    player.getDialogueManager().startDialogue("SimpleMessage", "Hardcore ironmen have a fixed exp and drop rate, it's not possible to select a different game mode.");
                    return;
                }
                player.getTemporaryAttributtes().put("ExpMode", ExpMode.LEGENDARY);
                setClickedXPMode(player, componentId);
                showInformation(player);
                break;
            case 40:
                setClickedGameMode(player, componentId);
                player.getTemporaryAttributtes().put("GameMode", GameMode.NORMALE);
                showInformation(player);
                break;
            case 41:
                setClickedGameMode(player, componentId);
                player.getTemporaryAttributtes().put("GameMode", GameMode.IRONMAN);
                showInformation(player);
                break;
            case 56:
                setClickedGameMode(player, componentId);
                player.getTemporaryAttributtes().put("GameMode", GameMode.HARDCODE_IRONMAN);
                showInformation(player);
                break;
            case 59:
                setClickedGameMode(player, componentId);
                player.getTemporaryAttributtes().put("GameMode", GameMode.GROUP_IRONMAN);
                showInformation(player);
                break;
        }
    }

    /**
     * sets the initial game mode of the player
     *
     * @param player
     */
    public static void setGameMode(Player player) {
        final ExpMode expMode = (ExpMode) player.getTemporaryAttributtes().get("ExpMode");
        final GameMode gameMode = (GameMode) player.getTemporaryAttributtes().get("GameMode");
        if (gameMode == null) {
            player.getDialogueManager().startDialogue("SimpleMessage", "Please select a game mode first.");
            return;
        }
        if (expMode == null && (gameMode != GameMode.HARDCODE_IRONMAN && gameMode != GameMode.GROUP_IRONMAN)) {
            player.getDialogueManager().startDialogue("SimpleMessage", "Please select an exp mode first.");
            return;
        }
        if (gameMode == GameMode.HARDCODE_IRONMAN) {
            player.setHCIronMan(true);
            completeTutorial(player);
            return;
        }

        if (gameMode == GameMode.GROUP_IRONMAN) {
            if (!GIM.ACTIVE) {
                Dialogue.sendSingleDialogue(player, "Group Ironman is disabled.");
                return;
            }
            player.getDialogueManager().startDialogue(new Dialogue() {
                @Override
                public void start() {
                    sendMenu();
                }

                @Override
                public void run(int interfaceId, int componentId) {
                    switch (stage) {
                        case 0:
                            if (componentId == OPTION_1) {
                                player.getDialogueManager().startDialogue(new DCreateGroup());
                            } else if (componentId == OPTION_2) {
                                sendDialogue("Are you sure you would like to create a groupless GIM account?",
                                        "Accounts with a 'groupless' status cannot use anything GIM related, and will not appear on any GIM leaderboards.",
                                        "They may join a group at any time.");
                                stage = 1;
                            } else if (componentId == OPTION_3) {
                                player.getDialogueManager().startDialogue(new DJoinGroup());

                            }
                            break;
                        case 1:
                            sendOptionsDialogue("Select an option.", "Yes", "No");
                            stage = 2;
                            break;
                        case 2:
                            if (componentId == OPTION_1) {
                                GIM.createGrouplessAccount(player);
                            } else if (componentId == OPTION_2) {
                                sendMenu();
                            }
                            break;
                    }

                }

                @Override
                public void finish() {
                }

                private void sendMenu() {
                    sendOptionsDialogue("Select an option.",
                            "Create a new group",
                            "Create a groupless GIM",
                            "Join an existing group");
                    stage = 0;
                }
            });
            return;
        }
        switch (expMode) {
            case NOVICE:
                if (gameMode == GameMode.IRONMAN)
                    player.setNoviceIronMan(true);
                else
                    player.setNovice(true);
                break;
            case EXPERT:
                if (gameMode == GameMode.IRONMAN)
                    player.setExpertIronMan(true);
                else
                    player.setExpert(true);
                break;
            case LEGENDARY:
                if (gameMode == GameMode.IRONMAN)
                    player.setIronMan(true);
                else
                    player.setLegendary(true);
                break;
            case INTERMEDIATE:
                if (gameMode == GameMode.IRONMAN)
                    player.setIntermediateIronMan(true);
                else
                    player.setIntermediate(true);
                break;
        }
        completeTutorial(player);

    }

    /**
     * Completes tutorial - hands out rewards.
     */
    public static void completeTutorial(Player player) {
        player.getHintIconsManager().removeUnsavedHintIcon();
        Dialogue.closeNoContinueDialogue(player);
        player.setCompleted();
        //player.setBonusXpTimer(12000);
        player.getAppearence().generateAppearenceData();
        player.getInterfaceManager().sendTaskSystem();
        player.getInterfaceManager().openGameTab(1);
        if (!canAddReward(player)) {
            player.sendMessage(
                    Colors.RED + "You did not receive your starter kit, you've already received it 2 times.");
            return;
        }
        player.getInventory().addItem(995, 100000);
        player.getEquipment().set(Equipment.SLOT_AMULET, new Item(1712));
        player.getEquipment().set(Equipment.SLOT_FEET, new Item(36029));
        player.getEquipment().set(Equipment.SLOT_HANDS, new Item(36030));
        player.getEquipment().set(Equipment.SLOT_LEGS, new Item(36028));
        player.getEquipment().set(Equipment.SLOT_CHEST, new Item(36027));
        player.getEquipment().set(Equipment.SLOT_HAT, new Item(36026));
        player.getEquipment().set(Equipment.SLOT_ARROWS, new Item(882, 100));
        player.getEquipment().set(Equipment.SLOT_RING, new Item(2552));
        player.getEquipment().set(Equipment.SLOT_CAPE, new Item(1052));
        player.getEquipment().refresh(Equipment.SLOT_AMULET, Equipment.SLOT_FEET, Equipment.SLOT_HANDS,
                Equipment.SLOT_HAT, Equipment.SLOT_CHEST, Equipment.SLOT_LEGS,
                Equipment.SLOT_AMULET, Equipment.SLOT_RING, Equipment.SLOT_AURA, Equipment.SLOT_CAPE,
                Equipment.SLOT_ARROWS);
        player.getInventory().addItem(new Item(22302));
        player.getInventory().addItem(new Item(9703, 1));
        player.getInventory().addItem(new Item(9705, 1));
        player.getInventory().addItem(new Item(15598, 1));
        player.getInventory().addItem(new Item(8013, 15));
        player.getInventory().addItem(new Item(554, 500));
        player.getInventory().addItem(new Item(558, 500));
        player.getInventory().addItem(new Item(555, 500));
        player.getInventory().addItem(new Item(557, 500));
        player.getInventory().addItem(new Item(562, 250));
        player.getInventory().addItem(new Item(560, 250));
        player.getInventory().addItem(new Item(565, 200));
        player.getInventory().addItem(new Item(386, 500));
        player.getInventory().addItem(new Item(2678, 1));
        player.getInventory().addItem(new Item(33262, 1));
        if (player.isIronMan() || player.isHCIronMan() || player.isLegendary()) {
            player.getInventory().addItem(new Item(41383, 1));
        }
        player.getEquipment().refreshConfigs(false);
        World.sendWorldMessage(Colors.RED + "<img=7>News:</col> Welcome: [" + player.getDisplayName() + "] - " + "mode: [" + player.getXPMode() + "] - to " + Settings.SERVER_NAME + "!", false);
        StarterMap.getSingleton().addIP(player.getIP());
        fade(player);
        // player.getPackets().sendBlackOut(5);
        player.getAppearence().generateAppearenceData();
        Magic.sendNormalTeleportSpell(player, 0, 0, player.getHomeTile());
        //player.getAccountPin().lock();
        ClueScrollDistributor.givePlayerClueScroll(player, 0, false);
    }

    private static boolean canAddReward(Player player) {
        int count = StarterMap.getSingleton().getCount(player.getIP());
        return count <= Settings.MAX_STARTER_COUNT;
    }

    public static void fade(final Player player) {
        final long time = FadingScreen.fade(player);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    FadingScreen.unfade(player, time, new Runnable() {
                        @Override
                        public void run() {
                            player.lock(3);
                        }
                    });
                } catch (Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }
        }, 5);
    }

}

