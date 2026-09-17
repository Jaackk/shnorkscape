package com.rs.game.player.content;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.player.Bank;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public final class MetalBank {

    private MetalBank() {

    }

    public static void depositInventory(Player player) {
        int deposited = 0;
        for (int slot = 0; slot < player.getInventory().getItems().getSize(); slot++) {
            Item item = player.getInventory().getItem(slot);
            if (item == null || !isMetalBankItem(getUnnotedId(item.getId()))) {
                continue;
            }
            player.addMetalBankItem(item.getId(), item.getAmount());
            player.getInventory().deleteItem(slot, item);
            deposited += item.getAmount();
        }
        if (deposited == 0) {
            player.sendMessage("You do not have any ores or bars to deposit into your metal bank.");
        } else {
            player.sendMessage("Deposited " + Utils.getFormattedNumber(deposited) + " ores and bars into your metal bank.");
        }
    }

    public static void show(Player player) {
        open(player);
    }

    public static void openWithdraw(Player player) {
        open(player);
    }

    public static void open(Player player) {
        Bank previousBank = player.getBank();
        if (player.getInterfaceManager().containsBankInterface()) {
            player.getInterfaceManager().removeBankInterface();
        }
        player.setMetalBankOpenBank(previousBank);
        player.getBank().lastContainerCopy = null;
        player.getBank().openBank();
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                Bank bank = player.getBank();
                if (bank != player.getMetalBankStorage() || !player.getInterfaceManager().containsBankInterface()) {
                    return;
                }
                bank.lastContainerCopy = null;
                bank.sendItems();
                bank.refreshTabs();
                bank.refreshItems();
            }
        }, 1);
    }

    public static void withdraw(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("Usage: ::withdrawmetal <item id> <amount>");
            return;
        }
        int itemId;
        int amount;
        try {
            itemId = Integer.parseInt(args[1]);
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage("Usage: ::withdrawmetal <item id> <amount>");
            return;
        }
        withdraw(player, itemId, amount);
    }

    public static void withdraw(Player player, int itemId, int requestedAmount) {
        if (!isMetalBankItem(getUnnotedId(itemId))) {
            player.sendMessage("That item cannot be stored in the metal bank.");
            return;
        }
        itemId = getUnnotedId(itemId);
        int stored = player.getMetalBankAmount(itemId);
        if (stored <= 0) {
            player.sendMessage("You do not have any " + getItemName(itemId).toLowerCase() + " in your metal bank.");
            return;
        }
        int amount = Math.min(requestedAmount, stored);
        if (amount <= 0) {
            player.sendMessage("Please enter an amount greater than zero.");
            return;
        }
        amount = getInventorySpaceAdjustedAmount(player, itemId, amount);
        if (amount <= 0) {
            player.sendMessage("You do not have enough free inventory space.");
            return;
        }
        int removed = player.removeMetalBankItem(itemId, amount);
        if (removed <= 0) {
            player.sendMessage("You do not have any " + getItemName(itemId).toLowerCase() + " in your metal bank.");
            return;
        }
        if (!player.getInventory().addItem(itemId, removed, false)) {
            player.addMetalBankItem(itemId, removed);
            player.sendMessage("You do not have enough free inventory space.");
            return;
        }
        player.sendMessage("Withdrew " + Utils.getFormattedNumber(removed) + " x " + getItemName(itemId).toLowerCase() + " from your metal bank.");
    }

    public static boolean isMetalBankItem(int itemId) {
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(itemId);
        if (defs == null) {
            return false;
        }
        String name = defs.getName().toLowerCase();
        return name.endsWith(" bar")
                || name.endsWith(" ore")
                || name.equals("coal")
                || name.equals("luminite")
                || name.equals("drakolith")
                || name.equals("necrite")
                || name.equals("phasmatite")
                || name.equals("banite")
                || name.endsWith(" animica");
    }

    private static int getInventorySpaceAdjustedAmount(Player player, int itemId, int amount) {
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(itemId);
        if (defs == null) {
            return 0;
        }
        if (!defs.isStackable() && !defs.isNoted()) {
            return Math.min(amount, player.getInventory().getFreeSlots());
        }
        if (!player.getInventory().containsItem(itemId, 1) && player.getInventory().getFreeSlots() == 0) {
            return 0;
        }
        return amount;
    }

    private static String getItemName(int itemId) {
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(itemId);
        return defs == null ? "Item " + itemId : defs.getName();
    }

    private static int getUnnotedId(int itemId) {
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(itemId);
        if (defs != null && defs.isNoted() && defs.getCertId() != -1) {
            return defs.getCertId();
        }
        return itemId;
    }
}
