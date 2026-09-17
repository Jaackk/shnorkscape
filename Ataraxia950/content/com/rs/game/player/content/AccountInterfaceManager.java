package com.rs.game.player.content;

import com.rs.game.WorldTile;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.content.titles.PlayerTitleHandler;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

/**
 * Handles everything related to the Account Manager.
 *
 * @author Noel.
 */
public class AccountInterfaceManager {

    /**
     * Holds spawn locations for cheap usage
     */

    public static WorldTile[] spawns = { new WorldTile(4128, 5848, 0), // New Home
            new WorldTile(2213, 3361, 1) // Prif
    };

    /**
     * Sends the actual interface with all available options.
     *
     * @param player The player to send the interface to.
     */
    public static void sendInterface(Player player) {
        if (!player.getInterfaceManager().containsInterface(1157)) {
            if (player.getInterfaceManager().containsScreenInter()) {
                player.sendMessage("Please finish or close what you are doing before trying to open this menu!");
                return;
            }
        }
        player.getInterfaceManager().sendInterface(1157);
        player.getPackets().sendIComponentText(1157, 92, "<col=FFFF00><shad=FFCC00>" + player.getDisplayName() + "'s Settings");
        player.getPackets().sendIComponentText(1157, 95, "");
        player.getPackets().sendIComponentText(1157, 33, "Setting");
        player.getPackets().sendIComponentText(1157, 34, "Toggle");

        player.getPackets().sendIComponentText(1157, 46, "");
        player.getPackets().sendIComponentText(1157, 47, "Appearance");
        player.getPackets().sendIComponentText(1157, 48, "Press to customize!");

        player.getPackets().sendIComponentText(1157, 49, "");
        player.getPackets().sendIComponentText(1157, 50, "Loyalty titles");
        player.getPackets().sendIComponentText(1157, 51, "Press to customize!");

        player.getPackets().sendIComponentText(1157, 52, "");
        player.getPackets().sendIComponentText(1157, 53, "Message filters");
        player.getPackets().sendIComponentText(1157, 54, "Press to customize!");

        player.getPackets().sendIComponentText(1157, 55, "");
        player.getPackets().sendIComponentText(1157, 56, "Misc settings");
        player.getPackets().sendIComponentText(1157, 57, "Press to customize!");

        player.getPackets().sendIComponentText(1157, 58, "");
        player.getPackets().sendIComponentText(1157, 59, "Spawn location");
        player.getPackets().sendIComponentText(1157, 60, Colors.RCYAN + Colors.SHAD + player.getHomeName() + "</col>");

        player.getPackets().sendIComponentText(1157, 61, "");
        player.getPackets().sendIComponentText(1157, 62, "Loot Beam");
        player.getPackets().sendIComponentText(1157, 63, (player.getLootBeamManager().isLootBeamsEnabled() ? Colors.GREEN + "Enabled (" + player.getLootBeamManager().getCurrentLootBeamType().toString() + ")" : Colors.RED + "Disabled") + ". Trigger price: " + Utils.getFormattedNumber(player.getLootBeamManager().getLootBeamMinimumValue()) + ".</col>");

        player.getPackets().sendIComponentText(1157, 64, "");
        player.getPackets().sendIComponentText(1157, 65, "Mac-lock");
        player.getPackets().sendIComponentText(1157, 66, (player.iplocked ? Colors.GREEN + "Enabled to : " + player.lockedwith : Colors.RED + "Disabled" + "</col>.") + " Press to customize");

        player.getPackets().sendIComponentText(1157, 67, "");
        player.getPackets().sendIComponentText(1157, 68, "Custom title");
        player.getPackets().sendIComponentText(1157, 69,  "Disabled");

        player.getPackets().sendIComponentText(1157, 70, "");
        player.getPackets().sendIComponentText(1157, 71, "Insignia Settings");
        player.getPackets().sendIComponentText(1157, 72, "Modify the Heart Insignia settings.");

        player.getPackets().sendIComponentText(1157, 74, "");
        player.getPackets().sendIComponentText(1157, 75, "Build Settings");
        player.getPackets().sendIComponentText(1157, 76, "Modify your client to your prefered play-style.");

        // player.getPackets().sendIComponentText(1157, 76, "");
    }

    /**
     * Handles the actual interfaces buttons.
     *
     * @param player      The players interface to handle.
     * @param componentId The players interface pressed component ID.
     */
    public static void handleInterface(Player player, int componentId) {
        player.getInterfaceManager().closeChatBoxInterface();
        if (componentId == 0)
            player.getDialogueManager().startDialogue("PlayerSettings");

        if (componentId == 1)
            PlayerTitleHandler.sendInterface(player);
        if (componentId == 7) {
            player.getPackets().sendMainInterfaceMessage(1, "This option is disabled.", true);
            return;
//            if (!player.isSupremeDonator() && !player.isOwner() && player.getRights() < 1 && !player.isSupport()) {
//                player.sendMessage("You need to be a Platinum member to change your title.");
//                return;
//            }
//            if (player.isTitleBanned()) {
//                player.sendMessage("You're currently banned from using custom titles.");
//                return;
//            }
//            
//            player.getDialogueManager().startDialogue("CustomTitleD");
        }

        if (componentId == 2) {
            player.getDialogueManager().startDialogue(new Dialogue() {

                @Override
                public void start() {
                    sendOptionsDialogue("What would you like to toggle?", "World messages: " + (player.isHidingWorldMessages() ? Colors.RED + "Disabled" : Colors.GREEN + "Enabled") + "</col>", "Yell messages: " + (player.isYellOff() ? Colors.RED + "Disabled" : Colors.GREEN + "Enabled") + "</col>", "Nevermind");
                    stage = 1;
                }

                @Override
                public void run(int interfaceId, int componentId) {
                    switch (stage) {
                    case 1:
                        switch (componentId) {
                        case OPTION_1:
                            if (!player.isHidingWorldMessages())
                                player.setHideWorldMessages(!player.isHidingWorldMessages());
                            else
                                player.setHideWorldMessages(!player.isHidingWorldMessages());
                            sendOptionsDialogue("What would you like to toggle?", "World messages: " + (player.isHidingWorldMessages() ? Colors.RED + "Disabled" : Colors.GREEN + "Enabled") + "</col>", "Yell messages: " + (player.isYellOff() ? Colors.RED + "Disabled" : Colors.GREEN + "Enabled") + "</col>", "Nevermind");
                            stage = 1;
                            break;
                        case OPTION_2:
                            if (!player.isYellOff())
                                player.setYellOff(!player.isYellOff());
                            else
                                player.setYellOff(!player.isYellOff());
                            sendOptionsDialogue("What would you like to toggle?", "World messages: " + (player.isHidingWorldMessages() ? Colors.RED + "Disabled" : Colors.GREEN + "Enabled") + "</col>", "Yell messages: " + (player.isYellOff() ? Colors.RED + "Disabled" : Colors.GREEN + "Enabled") + "</col>", "Nevermind");
                            stage = 1;
                            break;
                        case OPTION_3:
                            finish();
                            break;
                        }
                        break;
                    }
                }

                @Override
                public void finish() {
                    player.getInterfaceManager().closeChatBoxInterface();
                }
            });
        }

        if (componentId == 3) {
            player.getDialogueManager().startDialogue(new Dialogue() {

                @Override
                public void start() {
                    sendOptionsDialogue("What setting would you like to change?", "Show donator icon: " + (player.showIcon() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Skip empty warning: " + (player.getWarnEmpty() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Show bank list when opening bank: " + (player.promptList() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Random skilling events: " + (player.hasRandomEvent() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "More");
                    stage = 0;
                }

                @Override
                public void run(int interfaceId, int componentId) {
                    switch (stage) {
                    case 0:
                        switch (componentId) {
                        case OPTION_1: // show donator icon toggle
                            if (player.showIcon())
                                player.setIcon(false);
                            else {
                                if (player.getMoneySpent() >= 20)
                                    player.setIcon(true);
                                else {
                                    finish();
                                    player.sendMessage(Colors.RED + Colors.SHAD + "You must be a bronze donator or higher to do this!");
                                    break;
                                }
                            }
                            sendOptionsDialogue("What setting would you like to change?", "Show donator icon: " + (player.showIcon() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Skip empty warning: " + (player.getWarnEmpty() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Show bank list when opening bank: " + (player.promptList() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Random skilling events: " + (player.hasRandomEvent() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "More");
                            stage = 0;
                            break;
                        case OPTION_2: // show empty dialogue toggle
                            player.setWarnEmpty(!player.getWarnEmpty());
                            sendOptionsDialogue("What setting would you like to change?", "Show donator icon: " + (player.showIcon() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Skip empty warning: " + (player.getWarnEmpty() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Show bank list when opening bank: " + (player.promptList() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Random skilling events: " + (player.hasRandomEvent() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "More");
                            stage = 0;
                            break;
                        case OPTION_3: // toggle bank listing
                            player.setBankPrompt(!player.promptList());
                            sendOptionsDialogue("What setting would you like to change?", "Show donator icon: " + (player.showIcon() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Skip empty warning: " + (player.getWarnEmpty() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Show bank list when opening bank: " + (player.promptList() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Random skilling events: " + (player.hasRandomEvent() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "More");
                            stage = 0;
                            break;
                        case OPTION_4:
                            player.setRandomEvent(player.hasRandomEvent());
                            sendOptionsDialogue("What setting would you like to change?", "Show donator icon: " + (player.showIcon() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Skip empty warning: " + (player.getWarnEmpty() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Show bank list when opening bank: " + (player.promptList() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Random skilling events: " + (player.hasRandomEvent() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "More");
                            stage = 0;
                            break;
                        case OPTION_5:
                            stage = 1;
                            sendOptionsDialogue("What setting would you like to change?", "Teleport Interface: " + (player.isUsingTeleportInterface() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Legacy interfaces Skin: " + (player.hasLegacyInterfacesSkin() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Disassemble Warning: " + (!player.hasDisableDisassembleHighValueWarning() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "99 Gold Trim: " + (player.isGoldTrim99() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Back");
                            break;
                        }
                        break;
                    case 1:
                        switch (componentId) {
                        case OPTION_1:
                            player.setUsingTeleportInterface(!player.isUsingTeleportInterface());
                            sendOptionsDialogue("What setting would you like to change?", "Teleport Interface: " + (player.isUsingTeleportInterface() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Legacy interfaces Skin: " + (player.hasLegacyInterfacesSkin() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Disassemble Warning: " + (!player.hasDisableDisassembleHighValueWarning() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "99 Gold Trim: " + (player.isGoldTrim99() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Back");
                            QuestTab.sendTab(player);
                            break;
                        case OPTION_2:
                            player.toggleLegacyInterfacesSkin();
                            sendOptionsDialogue("What setting would you like to change?", "Teleport Interface: " + (player.isUsingTeleportInterface() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Legacy interfaces Skin: " + (player.hasLegacyInterfacesSkin() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Disassemble Warning: " + (!player.hasDisableDisassembleHighValueWarning() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "99 Gold Trim: " + (player.isGoldTrim99() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Back");
                            QuestTab.sendTab(player);
                            break;
                        case OPTION_3:
                            player.toggleDisableDisassembleHighValueWarning();
                            sendOptionsDialogue("What setting would you like to change?", "Teleport Interface: " + (player.isUsingTeleportInterface() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Legacy interfaces Skin: " + (player.hasLegacyInterfacesSkin() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Disassemble Warning: " + (!player.hasDisableDisassembleHighValueWarning() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "99 Gold Trim: " + (player.isGoldTrim99() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Back");
                            QuestTab.sendTab(player);
                            break;
                        case OPTION_4:
                            player.toggleGoldTrim99();
                            sendOptionsDialogue("What setting would you like to change?", "Teleport Interface: " + (player.isUsingTeleportInterface() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Legacy interfaces Skin: " + (player.hasLegacyInterfacesSkin() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Disassemble Warning: " + (!player.hasDisableDisassembleHighValueWarning() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "99 Gold Trim: " + (player.isGoldTrim99() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Back");
                            QuestTab.sendTab(player);
                            break;
                        case OPTION_5:
                            sendOptionsDialogue("What setting would you like to change?", "Show donator icon: " + (player.showIcon() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Skip empty warning: " + (player.getWarnEmpty() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Show bank list when opening bank: " + (player.promptList() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "Random skilling events: " + (player.hasRandomEvent() ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled"), "More");
                            stage = 0;
                            break;
                        }
                        break;
                    }
                }

                @Override
                public void finish() {
                    player.getInterfaceManager().closeChatBoxInterface();
                }

            });
        }

        if (componentId == 4) {
            player.getInterfaceManager().closeScreenInterface();
            player.getDialogueManager().startDialogue(new Dialogue() {

                @Override
                public void start() {
                    sendOptionsDialogue(Colors.RCYAN + "Where would you like to set your home?", Colors.GREEN + "Home/Donor Zone", (player.getPerkManager().hasPerkActive(DonationPerk.ELF__S_FRIEND) ? Colors.GREEN : Colors.RED) + "Prifddinas", "Nevermind");
                    stage = 1;
                }

                @Override
                public void run(int interfaceId, int componentId) {
                    switch (stage) {
                    case 1:
                        end();
                        if (componentId == OPTION_5)
                            break;
                        switch (componentId) {
                            case OPTION_1:
                                player.setHome(spawns[0], "Home/Donor Zone");
                                player.sendMessage(Colors.GREEN + Colors.SHAD + "Your respawn location has been set to Home/Donor Zone!", true);
                                break;
                            case OPTION_2:
                                if (player.getPerkManager().hasPerkActive(DonationPerk.ELF__S_FRIEND)) {
                                    player.setHome(spawns[1], "Prifddinas");
                                    player.sendMessage(Colors.GREEN + Colors.SHAD + "Your respawn location has been set to the Prifddinas", true);
                                } else
                                    player.sendMessage(Colors.RED + Colors.SHAD + "You must purchase the Elf's Friend perk!", false);
                                break;
                        }
                        break;
                    }
                }


                @Override
                public void finish() {
                    player.getInterfaceManager().closeChatBoxInterface();
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            AccountInterfaceManager.sendInterface(player);
                        }
                    }, 1);
                }

            });
            }

        if (componentId == 5)
            player.getDialogueManager().startDialogue("SetBeam");

        if (componentId == 6)
            player.getDialogueManager().startDialogue("setIplock");

        if (componentId == 8) {
            player.getInterfaceManager().closeScreenInterface();
            player.getDialogueManager().startDialogue("InsigniaSettingsD");
        }

        if (componentId == 9) {
            player.getDialogueManager().startDialogue("BuildSettings");
        }
    }
}