package com.rs.game.player.dialogue.impl;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputIntegerEvent;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class NoteBankD extends Dialogue {

    private static void note(Player player, Item item) {
        player.getInventory().deleteItem(item);
        player.addItem(new Item(item.getDefinitions().certId, item.getAmount()));
    }

    private static void unnote(Player player, Item item) {
        int invSpace = player.getInventory().getFreeSlots();
        if (invSpace < item.getAmount()) {
            item.setAmount(invSpace);
        }
        player.getInventory().deleteItem(item);
        player.addItem(new Item(item.getDefinitions().certId, item.getAmount()));
    }

    public static boolean exchange(Player player, Item item) {
        int amount = player.getInventory().getAmountOf(item.getId());
        if (item.getAmount() > amount) {
            item.setAmount(amount);
        }
        if (item.getAmount() == 0 || item.getDefinitions().getCertId() == -1) {
            return false;
        }
        ItemDefinitions def = item.getDefinitions();
        if (def.isStackable() && !def.isNoted()) {
            player.sendMessage("Stackable items cannot be exchanged for bank notes.");
            return false;
        }
        if (def.isNoted()) {
            unnote(player, item);
        } else {
            note(player, item);
        }
        return true;
    }

    private int exchangeId = -1;
    private boolean toNote = false;

    @Override
    public void start() {
        exchangeId = (int) parameters[0];
        toNote = !ItemDefinitions.getItemDefinitions(exchangeId).isNoted();
        sendOptionsDialogue("How many to exchange?",
                "1", "5", "All", "X");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                if (componentId == OPTION_1) {
                    exchange(player, new Item(exchangeId));
                    end();
                } else if (componentId == OPTION_2) {
                    if (exchange(player, new Item(exchangeId, 5))) {
                        player.sendMessage("You exchange the " + (toNote ? "items for bank notes." : "bank notes for items."));
                    }
                    end();
                } else if (componentId == OPTION_3) {
                    if (exchange(player, new Item(exchangeId, player.getInventory().getAmountOf(exchangeId)))) {
                        player.sendMessage("You exchange the " + (toNote ? "items for bank notes." : "bank notes for items."));
                    }
                    end();
                } else if (componentId == OPTION_4) {
                    player.sendInputInteger("How many would you like to exchange?", new InputIntegerEvent() {
                        @Override
                        public void run(Player player) {
                            if (exchange(player, new Item(exchangeId, getInteger()))) {
                                player.sendMessage("You exchange the " + (toNote ? "items for bank notes." : "bank notes for items."));
                            }
                            end();
                        }
                    });
                }
                break;
        }
    }

    @Override
    public void finish() {
        exchangeId = -1;
        toNote = false;
        player.getInterfaceManager().closeChatBoxInterface();
    }
}
