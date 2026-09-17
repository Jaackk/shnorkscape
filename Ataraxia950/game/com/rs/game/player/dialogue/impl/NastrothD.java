package com.rs.game.player.dialogue.impl;

import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.items.AncientArtefacts;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

/**
 * Handles Nastroth's Dialogue.
 *
 * @author Noel
 */
public class NastrothD extends Dialogue {

    /**
     * An Int representing the NPC's ID.
     */
    int npcId;

    @Override
    public void start() {
        npcId = (Integer) parameters[0];
        sendOptionsDialogue("Select an Option",
                "Sell ancient artefacts",
                "Change my game mode",
                "Reset my combat stats",
                "PK Point Shop",
                Colors.check(!player.hasClaimedStarterEquipment) + "Claim Starter Gear");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                switch (componentId) {
                    case OPTION_1:
                        AncientArtefacts.exchangeStatuettes(player);
                        break;
                    case OPTION_2:
                        if(player.isGroupIronman()) {
                            sendNPCDialogue(npcId, NORMAL, "Group Ironmen cannot change their game mode.");
                            stage = 100;
                        } else {
                            player.getDialogueManager().startDialogue("SwapGameMode");
                        }
                        break;
                    case OPTION_3:
                        sendPlayerDialogue(NORMAL, "Could you reset some of my combat skills?");
                        stage = 1;
                        break;
                    case OPTION_4:
                        sendPlayerDialogue(NORMAL, "Can I see the PK shop?");
                        stage = 4;
                        break;
                    case OPTION_5:
                        sendPlayerDialogue(player.hasClaimedStarterEquipment ? CALM_TALKING : NORMAL,  player.hasClaimedStarterEquipment ? "So uhh, could I have some more of that starter gear?" : "Could I claim my starter gear please?");
                        stage = 5;
                        break;
                }
                break;

            case 1:
                sendOptionsDialogue("Select a skill to reset", "Attack", "Defence", "Strength", "Constitution",
                        "More options..");
                stage = 2;
                break;

            case 2:
                switch (componentId) {
                    case OPTION_1:
                        sendNPCDialogue(npcId, NORMAL, "I've set your Attack level back to 1.");
                        player.getSkills().set(Skills.ATTACK, 1);
                        player.getSkills().setXp(Skills.ATTACK, 0);
                        stage = 99;
                        break;
                    case OPTION_2:
                        sendNPCDialogue(npcId, NORMAL, "I've set your Defence level back to 1.");
                        player.getSkills().set(Skills.DEFENCE, 1);
                        player.getSkills().setXp(Skills.DEFENCE, 0);
                        stage = 99;
                        break;
                    case OPTION_3:
                        sendNPCDialogue(npcId, NORMAL, "I've set your Strength level back to 1.");
                        player.getSkills().set(Skills.STRENGTH, 1);
                        player.getSkills().setXp(Skills.STRENGTH, 0);
                        stage = 99;
                        break;
                    case OPTION_4:
                        sendNPCDialogue(npcId, NORMAL, "I've set your Constitution level back to 10.");
                        player.getSkills().set(Skills.HITPOINTS, 10);
                        player.getSkills().setXp(Skills.HITPOINTS, 1154);
                        stage = 99;
                        break;
                    case OPTION_5:
                        sendOptionsDialogue("Select a skill to reset", "Ranged", "Prayer", "Magic", "Summoning",
                                "More options..");
                        stage = 3;
                        break;
                }
                break;

            case 3:
                switch (componentId) {
                    case OPTION_1:
                        sendNPCDialogue(npcId, NORMAL, "I've set your Ranged level back to 1.");
                        player.getSkills().set(Skills.RANGE, 1);
                        player.getSkills().setXp(Skills.RANGE, 0);
                        stage = 99;
                        break;
                    case OPTION_2:
                        sendNPCDialogue(npcId, NORMAL, "I've set your Prayer level back to 1.");
                        player.getSkills().set(Skills.PRAYER, 1);
                        player.getSkills().setXp(Skills.PRAYER, 0);
                        stage = 99;
                        break;
                    case OPTION_3:
                        sendNPCDialogue(npcId, NORMAL, "I've set your Magic level back to 1.");
                        player.getSkills().set(Skills.MAGIC, 1);
                        player.getSkills().setXp(Skills.MAGIC, 0);
                        stage = 99;
                        break;
                    case OPTION_4:
                        sendNPCDialogue(npcId, NORMAL, "I've set your Summoning level back to 1.");
                        player.getSkills().set(Skills.SUMMONING, 1);
                        player.getSkills().setXp(Skills.SUMMONING, 0);
                        stage = 99;
                        break;
                    case OPTION_5:
                        sendOptionsDialogue("Select a skill to reset", "Attack", "Defence", "Strength", "Constitution",
                                "More options..");
                        stage = 2;
                        break;
                }
                break;

            case 4:
                ShopsDataParser.openShop(player, 60);
                end();
                break;

            case 5:
                giveStarterEquipment();
                break;

            case 99:
                finish();
                break;

            case 100:
                end();
                break;
        }
    }

    private void giveStarterEquipment() {
        final int[] items = {9703, 9705, 15598, 36026, 36027, 36028, 36029, 36030};
        if (player.hasClaimedStarterEquipment) {
            sendNPCDialogue(npcId, MIDLY_ANGRY, "No, I said you may only claim it once.<br>Do not test my generosity.");
            stage = 100;
            return;
        }
        if (player.getInventory().getFreeSlots() >= items.length) {
            sendNPCDialogue(npcId, NORMAL, "Here you go.<br>Do not lose it, you may only claim it once.");
            stage = 100;
            for (int item : items) {
                player.addItem(new Item(item));
            }
            player.hasClaimedStarterEquipment = true;
            player.sendMessage("Nastroth hands you some equipment.", false);
        } else {
            sendNPCDialogue(npcId, CALM_TALKING, "I'm afraid you don't have enough inventory space to hold it all, bank some items then come speak to me again.");
            stage = 100;
        }
    }

    @Override
    public void finish() {
        player.getInterfaceManager().closeChatBoxInterface();
    }
}