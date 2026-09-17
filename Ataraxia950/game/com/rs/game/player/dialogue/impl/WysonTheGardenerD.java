package com.rs.game.player.dialogue.impl;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class WysonTheGardenerD extends Dialogue {

    public static void exchange(Player player) {
        int notedMoleSkinId = ItemDefinitions.getItemDefinitions(7418).certId;
        int notedMoleClawId = ItemDefinitions.getItemDefinitions(7416).certId;
        int moleSkinAmount = player.getInventory().getAmountOf(7418);
        int moleClawAmount = player.getInventory().getAmountOf(7416);
        int notedMoleSkinAmount = player.getInventory().getAmountOf(notedMoleSkinId);
        int notedMoleClawAmount = player.getInventory().getAmountOf(notedMoleClawId);

        if (moleSkinAmount > 0) {
            player.getInventory().deleteItem(7418, moleSkinAmount);
        }
        if (moleClawAmount > 0) {
            player.getInventory().deleteItem(7416, moleClawAmount);
        }
        if (notedMoleSkinAmount > 0) {
            player.getInventory().deleteItem(notedMoleSkinId, notedMoleSkinAmount);
        }
        if (notedMoleClawAmount > 0) {
            player.getInventory().deleteItem(notedMoleClawId, notedMoleClawAmount);
        }

        int amount = moleSkinAmount + moleClawAmount + notedMoleSkinAmount + notedMoleClawAmount;
        if (amount <= 0) {
            player.sendMessage("You have no mole bits to exchange.");
        } else if (amount == 1) {
            if (ThreadLocalRandom.current().nextBoolean()) {
                player.addItem(new Item(ItemDefinitions.getItemDefinitions(5073).certId));
            } else {
                player.addItem(new Item(ItemDefinitions.getItemDefinitions(5074).certId));
            }
            player.sendMessage("You have received " + Colors.RED + "1 bird's nest</col> for your mole bit.");
        } else {
            int seedAmount = ThreadLocalRandom.current().nextInt(0, amount);
            int jewelAmount = amount - seedAmount;
            player.addItem(new Item(ItemDefinitions.getItemDefinitions(5073).certId, seedAmount));
            player.addItem(new Item(ItemDefinitions.getItemDefinitions(5074).certId, jewelAmount));
            player.sendMessage("You have received " + Colors.RED + amount + " bird's nests</col> for your mole bits.");
        }
    }

    @Override
    public void start() {
        sendNPCDialogue(36, Dialogue.CALM, "Hiya! Would you like to trade me mole bits for birds nests?");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                sendOptionsDialogue("Select an option.",
                        "Sure!",
                        "No thanks.");
                stage = 1;
                break;
            case 1:
                if (componentId == OPTION_1) {
                    exchange(player);
                    end();
                } else if (componentId == OPTION_2) {
                    sendNPCDialogue(36, Dialogue.VERY_ANGRY, "Get lost then.");
                    stage = 2;
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