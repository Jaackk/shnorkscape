package com.rs.game.player.dialogue.impl;

import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputIntegerEvent;
import com.rs.utils.Utils;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class FlowerGirlD extends Dialogue {
    public static boolean listen(Player player, int npcId) {
        if (npcId == 1378) {
            player.getDialogueManager().startDialogue("FlowerGirlD");
            return true;
        }
        return false;
    }

    public static void spawn() {
        NPC npc = new NPC(1378, new WorldTile(3168, 3305, 0), -1, false);
        npc.setDirection(Utils.getAngle(0, -1));
        npc.setRandomWalk(0);
    }

    private static boolean giveFlour(Player player, int amount) {
        int freeSlots = player.getInventory().getFreeSlots();
        if (freeSlots == 0) {
            player.sendMessage("You do not have enough space in your inventory.");
            return false;
        }
        if (amount > freeSlots) {
            amount = freeSlots;
        }
        player.getInventory().addItem(new Item(1933, amount));
        if (amount > 1) {
            player.sendMessage("You receive " + amount + " pots of flour.");
        } else {
            player.sendMessage("You receive 1 pot of flour.");
        }
        return true;
    }

    @Override
    public void start() {
        sendNPCDialogue(1378, CALM, "Free buckets of flour, courtesy of the Kingdom of Lumbridge!");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                sendOptionsDialogue("Select an option.",
                        "1",
                        "5",
                        "10",
                        "All",
                        "X");
                stage = 1;
                break;
            case 1:
                if (componentId == OPTION_1) {
                    if (giveFlour(player, 1)) {
                        sendNPCDialogue(1378, CALM, "There ya go. Come back anytime!");
                    } else {
                        sendNPCDialogue(1378, CALM, "You don't have any space for it darling.");
                    }
                    stage = 2;
                } else if (componentId == OPTION_2) {
                    if (giveFlour(player, 5)) {
                        sendNPCDialogue(1378, CALM, "There ya go. Come back anytime!");
                    } else {
                        sendNPCDialogue(1378, CALM, "You don't have any space for it darling.");
                    }
                    stage = 2;
                } else if (componentId == OPTION_3) {
                    if (giveFlour(player, 10)) {
                        sendNPCDialogue(1378, CALM, "There ya go. Come back anytime!");
                    } else {
                        sendNPCDialogue(1378, CALM, "You don't have any space for it darling.");
                    }
                    stage = 2;
                } else if (componentId == OPTION_4) {
                    if (giveFlour(player, Integer.MAX_VALUE)) {
                        sendNPCDialogue(1378, CALM, "There ya go. Come back anytime!");
                    } else {
                        sendNPCDialogue(1378, CALM, "You don't have any space for it darling.");
                    }
                    stage = 2;
                } else if (componentId == OPTION_5) {
                    player.sendInputInteger("How many pots of flour to grab?", new InputIntegerEvent() {
                        @Override
                        public void run(Player player) {
                            if (giveFlour(player, getInteger())) {
                                sendNPCDialogue(1378, CALM, "There ya go. Come back anytime!");
                            } else {
                                sendNPCDialogue(1378, CALM, "You don't have any space for it darling.");
                            }
                            stage = 2;
                        }
                    });
                }
                break;
            case 2:
                end();
                break;
        }
    }

    @Override
    public void finish() {
        player.getInterfaceManager().closeChatBoxInterface();
    }
}