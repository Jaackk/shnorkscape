package com.rs.game.player.content.skillingcontracts;

import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.InputIntegerEvent;

import java.util.List;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class BlockContractD extends Dialogue {
    boolean highLevel;
    int npc;

    @Override
    public void start() {
        highLevel = (boolean) parameters[0];
        npc = highLevel ? 219 : 943;
sendOriginal();
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 1:
                if (componentId == OPTION_1) {
                    displayInterface(Skills.AGILITY);
                } else if (componentId == OPTION_2) {
                    displayInterface(Skills.CONSTRUCTION);
                } else if (componentId == OPTION_3) {
                    displayInterface(Skills.COOKING);
                } else if (componentId == OPTION_4) {
                    displayInterface(Skills.CRAFTING);
                } else if (componentId == OPTION_5) {
                    sendOptionsDialogue("Choose the skill",
                            "Divination",
                            "Farming",
                            "Firemaking",
                            "Fishing",
                            Colors.RED + "Next...");
                    stage = 2;
                }
                break;
            case 2:
                if (componentId == OPTION_1) {
                    displayInterface(Skills.DIVINATION);
                } else if (componentId == OPTION_2) {
                    displayInterface(Skills.FARMING);
                } else if (componentId == OPTION_3) {
                    displayInterface(Skills.FIREMAKING);
                } else if (componentId == OPTION_4) {
                    displayInterface(Skills.FISHING);
                } else if (componentId == OPTION_5) {
                    sendOptionsDialogue("Choose the skill",
                            "Fletching",
                            "Herblore",
                            "Mining",
                            "Runecrafting",
                            Colors.RED + "Next...");
                    stage = 3;
                }
                break;
            case 3:
                if (componentId == OPTION_1) {
                    displayInterface(Skills.FLETCHING);
                } else if (componentId == OPTION_2) {
                    displayInterface(Skills.HERBLORE);
                } else if (componentId == OPTION_3) {
                    displayInterface(Skills.MINING);
                } else if (componentId == OPTION_4) {
                    displayInterface(Skills.RUNECRAFTING);
                } else if (componentId == OPTION_5) {
                    sendOptionsDialogue("Choose the skill",
                            "Smithing",
                            "Thieving",
                            "Woodcutting",
                            "Hunter",
                            Colors.RED + "Home...");
                    stage = 4;
                }
                break;
            case 4:
                if (componentId == OPTION_1) {
                    displayInterface(Skills.SMITHING);
                } else if (componentId == OPTION_2) {
                    displayInterface(Skills.THIEVING);
                } else if (componentId == OPTION_3) {
                    displayInterface(Skills.WOODCUTTING);
                } else if(componentId == OPTION_4) {
                    displayInterface(Skills.HUNTER);
                } else if (componentId == OPTION_5) {
                    sendOriginal();
                }
                break;
        }
    }

    @Override
    public void finish() {

    }

    private void sendOriginal() {
        sendOptionsDialogue("Choose the skill",
                "Agility",
                "Construction",
                "Cooking",
                "Crafting",
                Colors.RED + "Next...");
        stage = 1;
    }

    private void displayInterface(int skillId) {
        ContractList list = SkillingContractManager.ALL.get(skillId);
        List<SkillingContract> displayableContracts = list.getDisplayableContracts(highLevel);
        int linesNeeded = displayableContracts.size() + 3;
        int lineId = 1;
        player.getPackets().sendIComponentText(275, lineId++, Skills.SKILL_NAME[skillId] + " contracts");
        lineId += 8;
        player.getPackets().sendIComponentText(275, lineId++, "");
        player.getPackets().sendIComponentText(275, lineId++, "To block a contract, enter its identifier in the input box.");
        player.getPackets().sendIComponentText(275, lineId++, "");
        for (SkillingContract contract : displayableContracts) {
            String color = player.getContracts().getBlocks().containsEntry(skillId, contract.id) ? Colors.RED : Colors.GREEN;
            player.getPackets().sendIComponentText(275, lineId++, color + "ID: " + contract.id + ", " + contract.description.replace(" <amount> ", " "));
        }
        player.getPackets().sendRunScript(1207, linesNeeded);
        int linesLeft = 21 - lineId;
        if (linesNeeded < 11 && linesLeft > 0) {
            // Clear the rest of the lines.
            for (int loop = 0; loop < linesLeft; loop++) {
                player.getPackets().sendIComponentText(275, lineId++, "");
            }
        }
        player.sendInputInteger("Enter the ID", new InputIntegerEvent() {
            @Override
            public void run(Player player) {
                player.getInterfaceManager().closeScreenInterface();
                player.getDialogueManager().startDialogue("BlockContractSelectD", skillId, getInteger(), highLevel);
            }
        });
        player.getInterfaceManager().sendInterface(275);
    }
}