package com.rs.game.player;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.actions.divination.DivineObject;
import com.rs.game.player.content.Magic;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * <p>
 * Created on Sep 26, 2018.
 */
public class VisWaxManager implements Serializable {

    /**
     * The class serialization uid.
     */
    private static final long serialVersionUID = -1652884856648759841L;

    public static final int VISWAX_ITEM_ID = 32092;
    public static final int SKILLING_CONTRACT_PURCHASE_AMOUNT = 3;
    public static final int CO_OP_SKILLING_CONTRACT_PURCHASE_AMOUNT = 6;
    public static final int DIVINE_LOCATION_PURCHASE_AMOUNT = 15;
    public static final int WISDOM_AURA_RESET_PURCHASE_AMOUNT = 40;
    public static final int SKILLING_CONTRACT_POINTS_PURCHASE_AMOUNT = 30;
    public static final int SKILLING_TICKETS_BUY_AMT = 100;

    private transient Player player;
    private int quickTeleports;
    private int dailyResets;
    private boolean resetDivineLocation;

    public boolean purchaseWisdomAuraReset(int auraId) {

        Inventory inventory = player.getInventory();
        if (inventory == null)
            return cantPurchase("Your inventory is currently not available.");

        if (!inventory.containsItem(VISWAX_ITEM_ID, WISDOM_AURA_RESET_PURCHASE_AMOUNT))
            return cantPurchase("You do not have enough Vis Wax to make this purchase.");
        inventory.deleteItem(VISWAX_ITEM_ID, WISDOM_AURA_RESET_PURCHASE_AMOUNT);
        player.getAuraManager().cooldowns.remove(auraId);
        player.lastResetWisdomAura = LocalDateTime.now();
        return purchase(WISDOM_AURA_RESET_PURCHASE_AMOUNT + " Vis Wax was removed from your inventory.");
    }

    public boolean purchaseQuickTeleports(int chosenAmountOfWax) {
        if (quickTeleports >= 4000)
            return cantPurchase("You can't have any more than 4000 magical quick teleports at your disposal.");

        Inventory inventory = player.getInventory();
        if (inventory == null)
            return cantPurchase("Your inventory is currently not available.");

        int numberOfWaxInInventory = player.getInventory().getAmountOf(VISWAX_ITEM_ID);
        if (chosenAmountOfWax > numberOfWaxInInventory)
            chosenAmountOfWax = numberOfWaxInInventory;

        if (!inventory.containsItem(VISWAX_ITEM_ID, chosenAmountOfWax))
            return cantPurchase("You do not have enough to make this purchase.");

        int numberToGive = (chosenAmountOfWax * 10);
        if ((numberToGive + quickTeleports) > 4000)
            numberToGive = (4000 - quickTeleports);

        int algorithm = (int) Math.ceil(numberToGive / 10);
        if (algorithm <= 0)
            algorithm = 1;
        int amountToDelete = (algorithm);

        if (!inventory.containsItem(VISWAX_ITEM_ID, amountToDelete) || amountToDelete <= 0)
            return cantPurchase("You do not have enough to make this purchase.");

        quickTeleports += numberToGive;
        refreshQuickTeleportCharges();
        inventory.deleteItem(VISWAX_ITEM_ID, amountToDelete);
        return purchase(amountToDelete + " Vis Wax was removed from your inventory.");
    }

    public boolean purchaseCoOpContractReset() {
        Inventory inventory = player.getInventory();
        if (inventory == null)
            return cantPurchase("Your inventory is currently not available.");

        if (!inventory.containsItem(VISWAX_ITEM_ID, CO_OP_SKILLING_CONTRACT_PURCHASE_AMOUNT))
            return cantPurchase("You do not have enough to make this purchase. You need " + CO_OP_SKILLING_CONTRACT_PURCHASE_AMOUNT + " to buy a co-op skilling contract reset.");

        player.getContracts().resetCoOpContract(true);
        inventory.deleteItem(VISWAX_ITEM_ID, CO_OP_SKILLING_CONTRACT_PURCHASE_AMOUNT);
        return purchase(CO_OP_SKILLING_CONTRACT_PURCHASE_AMOUNT+ " Vis Wax was removed from your inventory.");
    }

    public boolean purchaseContractReset() {
        Inventory inventory = player.getInventory();
        if (inventory == null)
            return cantPurchase("Your inventory is currently not available.");

        if (!inventory.containsItem(VISWAX_ITEM_ID, SKILLING_CONTRACT_PURCHASE_AMOUNT))
            return cantPurchase("You do not have enough to make this purchase. You need " + SKILLING_CONTRACT_PURCHASE_AMOUNT + " to buy a skilling contract reset.");
player.skippedLastContract = true;
        player.getContracts().resetContract();
        inventory.deleteItem(VISWAX_ITEM_ID, SKILLING_CONTRACT_PURCHASE_AMOUNT);
        return purchase(SKILLING_CONTRACT_PURCHASE_AMOUNT + " Vis Wax was removed from your inventory.");
    }

    public boolean purchaseSkillingTickets() {
        Inventory inventory = player.getInventory();
        if (inventory == null)
            return cantPurchase("Your inventory is currently not available.");

        if (!inventory.containsItem(VISWAX_ITEM_ID, SKILLING_CONTRACT_POINTS_PURCHASE_AMOUNT))
            return cantPurchase("You do not have enough to make this purchase. You need " + SKILLING_CONTRACT_POINTS_PURCHASE_AMOUNT + " to buy "+SKILLING_TICKETS_BUY_AMT+" Skilling tickets.");

        player.addItem(new Item(39922, SKILLING_TICKETS_BUY_AMT));
        inventory.deleteItem(VISWAX_ITEM_ID, SKILLING_CONTRACT_POINTS_PURCHASE_AMOUNT);
        return purchase(SKILLING_CONTRACT_POINTS_PURCHASE_AMOUNT + " Vis Wax was removed from your inventory.");
    }

    public boolean purchaseDivinationDailyReset() {
        if (resetDivineLocation)
            return cantPurchase("You have already reset your Divination divine location today.");

        Inventory inventory = player.getInventory();
        if (inventory == null)
            return cantPurchase("Your inventory is currently not available.");

        if (!inventory.containsItem(VISWAX_ITEM_ID, DIVINE_LOCATION_PURCHASE_AMOUNT))
            return cantPurchase("You do not have enough to make this purchase. You need " + DIVINE_LOCATION_PURCHASE_AMOUNT + " to to reset your divination divine location.");

        DivineObject.resetGatherLimit(player, false);
        resetDivineLocation = true;

        inventory.deleteItem(VISWAX_ITEM_ID, DIVINE_LOCATION_PURCHASE_AMOUNT);
        return purchase(DIVINE_LOCATION_PURCHASE_AMOUNT + " Vis Wax was removed from your inventory.");
    }

    public void sendQuickTeleport(WorldTile tile) {
        if (player == null || tile == null)
            return;

        if (quickTeleports <= 0)
            return;

        quickTeleports--;
        Magic.sendNormalTeleportSpell(player, 0, 0, tile.transform(0, -1, 0), false);

        player.sendMessage("Oh no! You only have " + quickTeleports + " magical quick teleports left.");
        refreshQuickTeleportCharges();
    }

    private boolean cantPurchase(String messageToSend) {
        player.sendMessage(messageToSend);
        return false;
    }

    private boolean purchase(String soldMessage) {
        player.sendMessage(soldMessage);
        return true;
    }

    private String format(long time) {
        final int sec = (int) (time / 1000), h = sec / 3600, m = sec / 60 % 60, s = sec % 60;
        return (h < 1 ? "" : (h < 10 ? "0" + h : h) + "h:") + ((m < 1) && h < 1 ? "" : (m < 10 ? "0" + m : m) + "m:") + ((s < 1) && m < 1 ? "" : (s < 10 ? "0" + s + "s" : s + "s"));
    }

    /**
     * Gets the player.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Sets the player.
     *
     * @param player the player to set
     */
    public void setPlayer(Player player) {
        this.player = player;
    }

    /**
     * Gets the quickTeleports.
     *
     * @return the quickTeleports
     */
    public int getQuickTeleports() {
        return quickTeleports;
    }

    /**
     * Sets the quickTeleports.
     *
     * @param quickTeleports the quickTeleports to set
     */
    public void setQuickTeleports(int quickTeleports) {
        this.quickTeleports = quickTeleports;
        refreshQuickTeleportCharges();
    }

    /**
     * Gets the dailyResets.
     *
     * @return the dailyResets
     */
    public int getDailyResets() {
        return dailyResets;
    }

    /**
     * Sets the dailyResets.
     *
     * @param dailyResets the dailyResets to set
     */
    public void setDailyResets(int dailyResets) {
        this.dailyResets = dailyResets;
    }

    public boolean isResetDivineLocation() {
        return resetDivineLocation;
    }

    public void setResetDivineLocation(boolean resetDivineLocation) {
        this.resetDivineLocation = resetDivineLocation;
    }
    
    public void refreshQuickTeleportCharges() {
        player.getPackets().sendConfigByFile(28623, quickTeleports);
    }

}
