package com.rs.game.player.dialogue.impl;

import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.InputIntegerEvent;
import com.rs.utils.LoggingSystem;

/**
 * @author Xenthium
 * soz lare :smug:
 */
public class AtaraxiaDollarsTransferD extends Dialogue {

    public static final int ATARAXIA_DOLLARS_ID = 41430;
    private Player receiver;
    private int amount;

    @Override
    public void start() {
        receiver = (Player) parameters[0];
        int dollars = player.getInventory().getAmountOf(ATARAXIA_DOLLARS_ID);
        if (canTransfer()) {
            player.sendInputInteger("How many Ataraxia Dollars would you like to transfer to " + receiver.getDisplayName() + "?<br><br>" + "(1 - " + dollars + ")", new InputIntegerEvent() {
                @Override
                public void run(Player player) {
                    amount = getInteger();
                    if (amount > dollars) {
                        amount = dollars;
                    }
                    if (amount < 1) {
                        return;
                    }
                    sendOptionsDialogue("Are you sure you want to transfer " + amount + " Ataraxia Dollar" + (amount > 1 ? "s" : "") + " to " + receiver.getDisplayName() + "?", "Yes", "No");
                }
            });
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if (componentId == OPTION_1) {
            if (canTransfer() && canCompleteTransfer()) {
                player.getInventory().deleteItem(new Item(ATARAXIA_DOLLARS_ID, amount));
                receiver.addItem(new Item(ATARAXIA_DOLLARS_ID, amount));
                player.sendMessage(Colors.YELLOW + "You have transferred " + amount + " Ataraxia Dollar" + (amount > 1 ? "s" : "") + " to " + receiver.getDisplayName() + ".", false);
                receiver.sendMessage(Colors.YELLOW + player.getDisplayName() + " has transferred you " + amount + " Ataraxia Dollar" + (amount > 1 ? "s!" : "!"), false);
                LoggingSystem.logAtaraxiaDollarTransfer(player, receiver, amount);
            }
        }
        end();
    }

    @Override
    public void finish() {}

    private boolean canTransfer() {
        boolean conditions = player.getControlerManager().getControler() == null && receiver.getControlerManager().getControler() == null
                && !player.isUnderCombat() && !receiver.isUnderCombat() && !player.isLocked() && !receiver.isLocked()
                && player.withinDistance(receiver.getLastWorldTile(), 4);
        if (receiver == null || !receiver.isRunning() || !World.containsPlayer(receiver.getUsername())) {
            player.sendMessage(Colors.RED + "It looks like something went wrong when attempting to do that, perhaps the player logged out?", false);
            return false;
        }
        if (player.isOwner()) {
            return true;
        }
        if (!conditions) {
            player.sendMessage(Colors.RED + "You may only transfer Ataraxia Dollars to players who're close to you, out of combat and not within any active minigame/npc/region controller.", false);
        }
        return conditions;
    }

    private boolean canCompleteTransfer() {
        boolean conditions = receiver.getInventory().getFreeSlots() >= 1 || receiver.getInventory().containsItem(new Item(ATARAXIA_DOLLARS_ID));
        if (!player.getInventory().containsItem(new Item(ATARAXIA_DOLLARS_ID, amount))) {
            return false;
        }
        if (!conditions) {
            player.sendMessage(Colors.RED + receiver.getDisplayName() + " doesn't have enough free inventory slots to receive your transfer.", false);
            receiver.sendMessage(Colors.RED + player.getDisplayName() + " just tried to transfer you some Ataraxia Dollars but you don't have enough free inventory slots to receive them!", false);
        }
        return conditions;
    }

}