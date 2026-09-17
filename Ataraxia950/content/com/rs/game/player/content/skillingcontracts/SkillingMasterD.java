package com.rs.game.player.content.skillingcontracts;

import com.rs.game.item.Item;
import com.rs.game.player.Skills;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class SkillingMasterD extends Dialogue {
    private static final int SHOP_CONTRACT_RESTRICTION = 50;
    boolean advancedMaster;
    int npc;

    @Override
    public void start() {
        advancedMaster = (boolean) parameters[0];
        npc = advancedMaster ? 219 : 943;
        boolean levelsRequired = player.getSkills().getLevelForXp(Skills.WOODCUTTING) >= 70 ||
                player.getSkills().getLevelForXp(Skills.FIREMAKING) >= 70;
        if (!player.evilTreeContracts && levelsRequired && player.evilTreeKc >= 100) {
            sendNPCDialogue(npc, ANGRY, "You've been slacking on your contracts lately.",
                    "The Evil Tree Hunter tells me you've been too busy killing Evil Trees...",
                    "If that's the case, I'll start assigning you Evil Tree contracts!");
            stage = -1;
            player.evilTreeContracts = true;
            player.sendMessage(Colors.RED + "Both skilling masters can now assign you Evil Tree contracts!");
        } else {
            realStart();
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                realStart();
                break;
            case 0:
                sendOptionsDialogue("Select an option.",
                        "Assign me a skilling contract.",
                        "Can you explain something to me?",
                        "Can I see the rewards?",
                        "Can I see the shop?",
                        "I have extra skilling backpacks...");
                stage = 1;
                break;
            case 1:
                if (componentId == OPTION_1) {
                    // Assign skilling contract.
                    player.getContracts().checkedAssignContract(advancedMaster, npc);
                    stage = 2;
                } else if (componentId == OPTION_2) {
                    // Explain something?
                    player.getDialogueManager().startDialogue("ExplainSomethingD", advancedMaster);
                } else if (componentId == OPTION_3) {
                    // Point shop.
                    player.getDialogueManager().startDialogue("SpendPointsD", advancedMaster);

                } else if (componentId == OPTION_4) {
                    if (player.getContracts().totalContracts < SHOP_CONTRACT_RESTRICTION) {
                        sendNPCDialogue(npc, ANGRY, "Only real skillers can see my shop. Do more contracts for me and prove that you're not worthless (need " + (SHOP_CONTRACT_RESTRICTION - player.getContracts().totalContracts) + " more).");
                        stage = 0;
                        return;
                    }

                    if (advancedMaster) {
                        if (!player.unlockedAdvancedSkillingShop) {
                            sendNPCDialogue(npc, GOOFY_LAUGH, "Bwahahahahah! Not until you pay your one-time service fee of " + SkillingContractManager.TICKETS_FOR_SHOP + " Skilling tickets!",
                                    "I don't care if you've already paid the other woman, I want my share too!");
                            stage = 5;
                        } else {
                            ShopsDataParser.openShop(player, 182);
                        }
                    } else {
                        if (!player.unlockedSkillingShop) {
                            sendNPCDialogue(npc, ANGRY, "Fine. You'll need to pay a one-time service fee of " + SkillingContractManager.TICKETS_FOR_SHOP + " Skilling tickets before I can show it to you.");
                            stage = 5;
                        } else {
                            ShopsDataParser.openShop(player, 181);
                        }
                    }
                } else if (componentId == OPTION_5) {
                    // Sell skilling backpacks for points.
                    int count = player.getInventory().getAmountOf(37694);
                    if (count > 0) {
                        sendNPCDialogue(npc, ANGRY, "Do you want to trade me all the skilling backpacks in your",
                                "inventory for " + SkillingContractManager.TICKETS_FOR_BACKPACK + " Skilling tickets each?");
                        stage = 3;
                    } else {
                        sendNPCDialogue(npc, ANGRY, "No you don't, you liar!");
                        stage = 0;
                    }
                }
                break;
            case 2:
                end();
                break;
            case 3:
                sendOptionsDialogue("Select an option.", "Yes", "No");
                stage = 4;
                break;
            case 4:
                int count = player.getInventory().getAmountOf(37694);
                if (componentId == OPTION_1) {
                    sendNPCDialogue(npc, ANGRY, "Done. Are you finished bothering me now?");

                    int tickets = count * SkillingContractManager.TICKETS_FOR_BACKPACK;
                    if (count > 0) {
                        player.getInventory().deleteItem(37694, count);
                        player.addItem(new Item(39922, tickets));
                    }
                } else if (componentId == OPTION_2) {
                    sendNPCDialogue(npc, ANGRY, "Why did you inquire in the first place? Idiot!");
                }
                stage = 0;
                break;
            case 5:
                sendOptionsDialogue("Select an option.", "Sure, I'll pay.", "Bite me.");
                stage = 6;
                break;
            case 6:
                if (componentId == OPTION_1) {
                    if (player.getContracts().removeTickets(SkillingContractManager.TICKETS_FOR_SHOP)) {
                        if (advancedMaster) {
                            player.unlockedAdvancedSkillingShop = true;
                            ShopsDataParser.openShop(player, 182);
                        } else {
                            player.unlockedSkillingShop = true;
                            ShopsDataParser.openShop(player, 181);
                        }
                    } else {
                        sendNPCDialogue(npc, ANGRY, "You don't have enough Skilling tickets, imbecile.");
                        stage = 0;
                    }
                } else if (componentId == OPTION_2) {
                    sendNPCDialogue(npc, ANGRY, "You wish.");
                    stage = 0;
                }
                break;
        }
    }

    @Override
    public void finish() {
        player.getInterfaceManager().closeChatBoxInterface();
    }

    private void realStart() {
        if (advancedMaster) {
            sendNPCDialogue(npc, ANGRY, "I'll give ya better contracts than the woman beside me. Do not waste my time.");
        } else {
            sendNPCDialogue(npc, ANGRY, "Make it quick, casual. I have much skilling to do.");
        }
        stage = 0;
    }
}