package com.rs.game.player;
import com.rs.utils.EconomyPrices;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.controllers.Wilderness;
import com.rs.utils.Lend;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.items.ItemExaminesDataParser;
import com.rs.utils.data.parsers.items.ItemWeightsDataParser;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import com.rs.utils.EconomyPrices.AlchTier;
public final class Inventory implements Serializable {

    public static final int INVENTORY_INTERFACE = 1473, INVENTORY_INTERFACE_2 = 1474;
    
    private static final long serialVersionUID = 8842800123753277093L;
    public ItemsContainer<Item> items;
    private transient Player player;
    private transient double inventoryWeight;

    public Inventory() {
        items = new ItemsContainer<Item>(28, false);
    }

    public int getSpaceFor(int id, int amount) {
        ItemDefinitions def = ItemDefinitions.getItemDefinitions(id);
        if (def == null)
            return Integer.MAX_VALUE;
        if (def.isStackable()) {
            int existingAmount = getAmountOf(id);
            if (existingAmount > 0) {
                if (existingAmount + amount > 0) {
                    return 0;
                } else {
                    return Integer.MAX_VALUE;
                }
            }
            return 1;
        }
        return amount;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }


    public boolean hasSpaceFor(int id, int amount) {
        return getFreeSlots() >= getSpaceFor(id, amount);
    }
    public boolean contains(int itemId) {

        return items.contains(new Item(itemId, 1));
    }

    public boolean addCoins(Item item, boolean added) {
        if (item.getId() < 0 || item.getAmount() < 0 || !Utils.itemExists(item.getId())
                || !player.getControlerManager().canAddInventoryItem(item.getId(), item.getAmount()))
            return false;
        Item[] itemsBefore = items.getItemsCopy();
        if (added)
            player.getMoneyPouch().addMoneyMisc(item.getAmount());
        if (!added) {
            if (!items.add(item)) {
                Item t = new Item(item.getId(), items.getFreeSlots(), item.getCharges());
                t.setAttributes(item.getAttributes());
                items.add(t);
                player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
                refreshItems(itemsBefore);
                return false;
            }
        }
        refreshItems(itemsBefore);
        return true;
    }

    public void refreshConfigs(boolean init) {
        double w = 0;
        for (Item item : items.getItems()) {
            if (item == null)
                continue;
            w += ItemWeightsDataParser.getWeight(item, false);
        }
        inventoryWeight = w;
        player.getPackets().refreshWeight();
    }



    public void convertInventoryToGold() {
        Item[] itemsBefore = items.getItemsCopy();
        int totalGold = 0;

        for (int i = 0; i < items.getSize(); i++) {
            Item item = items.get(i);

            if (item == null || item.getId() == 995) // Skip null or already gold
                continue;

            int amount = item.getAmount();
            int valueEach;

            try {
                valueEach = EconomyPrices.getAlchCoins(item, AlchTier.TABLE); // Use your alch price formula
            } catch (Exception e) {
                continue; // If price calculation fails, skip
            }

            int totalItemGold = valueEach * amount;
            totalGold += totalItemGold;

            deleteItem(i, item); // Deletes by slot
        }

        if (totalGold > 0) {
            player.getMoneyPouch().addMoneyMisc(totalGold); // Add gold to pouch
            player.sendMessage("Your items have been converted into " + Utils.getFormattedNumber(totalGold) + " coins.");
        } else {
            player.sendMessage("No items could be converted to gold.");
        }

        refreshItems(itemsBefore);
    }




    public double getInventoryWeight() {
        return inventoryWeight;
    }

    public boolean addItem(int itemId, int amount, int charges, ConcurrentHashMap<TemporaryAttributes.Key, Object> attributes) {
        if (itemId < 0 || amount < 0 || !Utils.itemExists(itemId)
                || !player.getControlerManager().canAddInventoryItem(itemId, amount))
            return false;
        if (attributes != null) {
            Item t = new Item(itemId, amount, charges);
            t.setAttributes(attributes);
            return addItem(t);
        }
        int newAmount = addCoalBag(new Item(itemId, amount));
        if (newAmount == 0)
            return false;
        amount = newAmount;
        Item[] itemsBefore = items.getItemsCopy();
        int amount2 = getAmountOf(itemId);
        int amount3 = (Integer.MAX_VALUE - amount2);
        boolean coinsToPouch = true;
        if (itemId == 995) {
            if (player.getInventory().getAmountOf(995) + amount < 0) {
                player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
                return true;
            }
            if (!items.add(new Item(itemId, amount, charges))) {
                items.add(new Item(itemId, items.getFreeSlots(), charges));
                player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
                refreshItems(itemsBefore);
                return false;
            }
            refreshItems(itemsBefore);
            return true;
        }
        if (amount + amount2 < 0) {
            if (itemId == 995) {
                if (player.getMoneyPouch().getTotal() + amount < 0) {
                    player.sendMessage(Utils.getFormattedNumber(amount) + " coins has been added to the ground.");
                    World.updateGroundItem(new Item(995, amount, charges), player, player, 60, 0, false);
                    return true;
                }
                player.getMoneyPouch().addOverFlowMoney(amount);
                amount = 0;
                return true;
            }
            return false;
        }
        if (itemId == 995) {
            if (player.getMoneyPouch().getTotal() + amount < 0)
                coinsToPouch = false;
            if (player.getMoneyPouch().getTotal() + amount < Integer.MAX_VALUE && coinsToPouch
                    && !Wilderness.isAtWild(player)) {
                player.getMoneyPouch().addMoneyMisc(amount);
                return true;
            }
            return false;
        }
        if (itemId == 995 && player.getInventory().getAmountOf(995) + amount < 0) {
            player.getMoneyPouch().addMoneyMisc(amount);
            return true;
        }
        if (amount3 <= 0) {
            player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
            return false;
        }
        if (!items.add(new Item(itemId, amount, charges))) {
            items.add(new Item(itemId, items.getFreeSlots(), charges));
            player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
            refreshItems(itemsBefore);
            return false;
        }
        refreshItems(itemsBefore);
        return true;
    }
    
    public boolean addItemToInventory(int itemId, int amount) { 
        return addItemToInventory(itemId, amount, true);
    }
    
    public boolean addItemToInventory(int itemId, int amount, boolean sendMessage) {
        if (itemId < 0 || amount < 0 || !Utils.itemExists(itemId)
                || !player.getControlerManager().canAddInventoryItem(itemId, amount))
            return false;
        Item[] itemsBefore = items.getItemsCopy();
        if (!items.add(new Item(itemId, amount))) {
            items.add(new Item(itemId, items.getFreeSlots()));
            if (sendMessage)
            player.getPackets().sendGameMessage("Not enough space in your inventory.");
            refreshItems(itemsBefore);
            return false;
        }
        refreshItems(itemsBefore);
        return true;
    }
    
    public boolean addItemToInventory(Item item) { 
        return addItemToInventory(item, true);
    }
    
    public boolean addItemToInventory(Item item, boolean sendMessage) {
        if (item.getId() < 0 || item.getAmount() < 0 || !Utils.itemExists(item.getId())
                || !player.getControlerManager().canAddInventoryItem(item.getId(), item.getAmount()))
            return false;
        Item[] itemsBefore = items.getItemsCopy();
        if (!items.add(item)) {
            Item t = new Item(item.getId(), items.getFreeSlots(), item.getCharges());
            t.setAttributes(item.getAttributes());
            items.add(t);
            if(sendMessage)
            player.getPackets().sendGameMessage("Not enough space in your inventory.");
            refreshItems(itemsBefore);
            return false;
        }
        refreshItems(itemsBefore);
        return true;
    }
    
    public boolean addItem(int itemId, int amount) {
        return addItem(itemId, amount, true);
    }

    public boolean addItem(int itemId, int amount, boolean silentAddToCoalBag) {
        if (itemId < 0 || amount < 0 || !Utils.itemExists(itemId)
                || !player.getControlerManager().canAddInventoryItem(itemId, amount))
            return false;
        int newAmount = silentAddToCoalBag ? addCoalBag(new Item(itemId, amount)) : amount;
        if (newAmount == 0)
            return false;
        amount = newAmount;
        Item[] itemsBefore = items.getItemsCopy();
        int amount2 = getAmountOf(itemId);
        int amount3 = (Integer.MAX_VALUE - amount2);
        boolean coinsToPouch = true;
        if (itemId == 995) {
            if (player.getInventory().getAmountOf(995) + amount < 0) {
                player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
                return true;
            }
            if (!items.add(new Item(itemId, amount))) {
                items.add(new Item(itemId, items.getFreeSlots()));
                player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
                refreshItems(itemsBefore);
                return false;
            }
            refreshItems(itemsBefore);
            return true;
        }
        if (amount + amount2 < 0) {
            if (itemId == 995) {
                if (player.getMoneyPouch().getTotal() + amount < 0) {
                    player.sendMessage(Utils.getFormattedNumber(amount) + " coins has been added to the ground.");
                    World.updateGroundItem(new Item(995, amount), player, player, 60, 0, false);
                    return true;
                }
                player.getMoneyPouch().addOverFlowMoney(amount);
                amount = 0;
                return true;
            }
            return false;
        }
        if (itemId == 995) {
            if (player.getMoneyPouch().getTotal() + amount < 0)
                coinsToPouch = false;
            if (player.getMoneyPouch().getTotal() + amount < Integer.MAX_VALUE && coinsToPouch
                    && !Wilderness.isAtWild(player)) {
                player.getMoneyPouch().addMoneyMisc(amount);
                return true;
            }
            return false;
        }
        if (itemId == 995 && player.getInventory().getAmountOf(995) + amount < 0) {
            player.getMoneyPouch().addMoneyMisc(amount);
            return true;
        }
        if (amount3 <= 0) {
            player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
            return false;
        }
        if (!items.add(new Item(itemId, amount))) {
            items.add(new Item(itemId, items.getFreeSlots()));
            player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
            refreshItems(itemsBefore);
            return false;
        }
        refreshItems(itemsBefore);
        return true;
    }

    public void addItem(int itemId) {
        addItem(new Item(itemId, 1));
    }

    public boolean addItem(Item item) {
        if (item.getId() < 0 || item.getAmount() < 0 || !Utils.itemExists(item.getId())
                || !player.getControlerManager().canAddInventoryItem(item.getId(), item.getAmount()))
            return false;
        int newAmount = addCoalBag(item);
        if (newAmount == 0)
            return false;
        item.setAmount(newAmount);
        Item[] itemsBefore = items.getItemsCopy();
        if (!items.add(item)) {
            Item t = new Item(item.getId(), items.getFreeSlots(), item.getCharges());
            t.setAttributes(item.getAttributes());
            items.add(t);
            player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
            refreshItems(itemsBefore);
            return false;
        }
        refreshItems(itemsBefore);
        return true;
    }

    public boolean containsCoins(int amount) {
        if (player.getMoneyPouch().getTotal() >= amount)
            return true;
        return items.contains(new Item(995, amount));
    }

    public boolean containsItemInInventory(int itemId, int amount) {
        return items.contains(new Item(itemId, amount));
    }

    public boolean containsItem(int itemId, int ammount) {
        if (itemId == 453 && hasCoal(ammount))
            return true;
        if (itemId == 19675 && player.getEquipment().getPocketId() == 19675)
            return true;
        if (itemId == 18337 && player.getEquipment().getPocketId() == 18337)
            return true;
        return items.contains(new Item(itemId, ammount)) || player.getToolBelt().contains(itemId);
    }

    public boolean containsName(String name, int amount) {
        for (int i = 0; i < items.getSize(); i++) {
            String itemName = items.get(i).getName();
            if (itemName.equals("Coal") && hasCoal(amount))
                return true;
            if (itemName.contains(name))
                return true;
        }
        return false;
    }

    public boolean hasItemsAmountOne(int[] itemIds) {
        for (int i = 0; i < itemIds.length; i++) {
            int id = itemIds[i];
            int amount = 1;
            if (id == 453 && hasCoal(amount))
                return true;
            if (!items.contains(new Item(id, amount)))
                return false;
        }
        return true;
    }

    public boolean containsItems(int[] itemIds, int[] ammounts) {
        int size = itemIds.length > ammounts.length ? ammounts.length : itemIds.length;
        for (int i = 0; i < size; i++) {
            int id = itemIds[i];
            int amount = ammounts[i];
            if (id == 453 && hasCoal(amount))
                return true;
            if (!items.contains(new Item(id, amount)))
                return false;
        }
        return true;
    }

    public boolean containsItems(Item[] item) {
        for (int i = 0; i < item.length; i++) {
            if (item[i] == null)
                continue;
            if (item[i].getId() == 453 && hasCoal(item[i].getAmount()))
                return true;
            if (!items.contains(item[i]))
                return false;
        }
        return true;
    }

    public boolean containsOneItem(int... itemIds) {
        for (int itemId : itemIds) {
            if (itemId == 453 && hasCoal(1))
                return true;
            if (items.containsOne(new Item(itemId, 1)) || player.getToolBelt().contains(itemId))
                return true;
        }
        return false;
    }

    public void deleteCoins(int amount) {
        if (player.getMoneyPouch().getTotal() >= amount) {
            player.getMoneyPouch().removeMoneyMisc(amount);
            return;
        }
        Item[] itemsBefore = items.getItemsCopy();
        items.remove(new Item(995, amount));
        refreshItems(itemsBefore);
    }

    public boolean deleteOneItem(Item item) {
        if (!player.getControlerManager().canDeleteInventoryItem(item.getId(), item.getAmount())
                || !containsItem(item.getId(), item.getAmount()))
            return false;

        int newAmount = useCoalBag(item.getId(), item.getAmount());
        if (newAmount == 0) {
            return false;
        }
        Item[] itemsBefore = items.getItemsCopy();
        items.remove(new Item(item.getId(), newAmount).setAttributes(item.getAttributes()));
        refreshItems(itemsBefore);
        return true;
    }

    public void deleteItem(int itemId) {
        deleteItem(itemId, 1);
    }

    public void deleteItem(int itemId, int amount) { 
        deleteItem( itemId,  amount,  false); 
    }

    public void deleteItem(int itemId, int amount, boolean skipPouch) {
        if (!player.getControlerManager().canDeleteInventoryItem(itemId, amount))
            return;
        if (!skipPouch && itemId == 995 && player.getMoneyPouch().getTotal() >= amount) {
            player.getMoneyPouch().removeMoneyMisc(amount);
            return;
        }
        int newAmount = useCoalBag(itemId, amount);
        if (newAmount == 0) {
            return;
        }
        Item[] itemsBefore = items.getItemsCopy();
        items.remove(new Item(itemId, newAmount));
        refreshItems(itemsBefore);
    }

    /**
     * Deletes up to {@code Integer.MAX_VALUE} of an item.
     */
    public void deleteAllId(int itemId) {
        ItemDefinitions def = ItemDefinitions.getItemDefinitions(itemId);
        if (def.isStackable()) {
            forceDeleteId(itemId);
        } else {
            deleteItem(itemId, Integer.MAX_VALUE);
        }
    }

    // For stackable items.
    private void forceDeleteId(int itemId) {
        int loops = 0;
        do {
            if(loops> 28) {
                Logger.getGlobal().warn("forceDeleteId took way longer than usual to finish!");
                break;
            }
            deleteItem(itemId, Integer.MAX_VALUE);
            loops++;
        } while (player.getInventory().containsItem(itemId, 1));
    }

    public boolean hasCoal(int amount) {
        int coalAmount = 0;
        coalAmount += player.getCoal();
        coalAmount += getAmountOf(453);
        return coalAmount >= amount;
    }

    private int useCoalBag(int itemId, int amount) {
        if (itemId == 453 && player.hasCoalBag()) {
            if (amount > player.getCoal() && containsItem(453, amount - player.getCoal())) {
                amount -= player.getCoal();
                player.setCoal(0);
                player.sendMessage("You remove all the coal from your bag. It is now empty.");
                return amount;
            } else if (amount <= player.getCoal()) {
                player.removeCoal(amount);
                player.sendMessage("You remove " + amount + " coal from your coal bag.");
                return 0;
            }
        }
        return amount;
    }

    // Returns leftover amount.
    private int addCoalBag(Item item) {
        if (item.getId() == 453 && containsOneItem(18339)) {
            int canAdd = 100 - player.getCoal();
            if (canAdd <= 0) {
                player.sendMessage("Your coal bag is full!");
                return item.getAmount();
            }
            if (item.getAmount() > canAdd) {
                player.addCoal(canAdd);
                player.sendMessage("You add " + canAdd + " coal to your coal bag.");
                return item.getAmount() - canAdd;
            }
            player.addCoal(item.getAmount());
            player.sendMessage("You add " + item.getAmount() + " coal to your coal bag.");
            return 0;
        }
        return item.getAmount();
    }

    public void deleteItem(int slot, Item item) {
        if (!player.getControlerManager().canDeleteInventoryItem(item.getId(), item.getAmount()))
            return;
        Item[] itemsBefore = items.getItemsCopy();
        items.remove(slot, item);
        refreshItems(itemsBefore);
    }

    public void deleteItem(Item item) {
        if (!player.getControlerManager().canDeleteInventoryItem(item.getId(), item.getAmount()))
            return;

        int newAmount = useCoalBag(item.getId(), item.getAmount());
        if (newAmount == 0) {
            return;
        }
        Item[] itemsBefore = items.getItemsCopy();
        items.remove(new Item(item.getId(), newAmount, item.getCharges()).setAttributes(item.getAttributes()));
        refreshItems(itemsBefore);
    }

    public int getFreeSlots() {
        return items.getFreeSlots();
    }

    public int getAmountOf(int itemId) {
        return items.getNumberOf(itemId);
    }

    public int getAmountOf(Item item) {
        return items.getNumberOf(item);
    }

    public Item getItem(int slot) {
        return items.get(slot);
    }

    public ItemsContainer<Item> getItems() {
        return items;
    }

    public int getItemsContainerSize() {
        return items.getSize();
    }


    public boolean hasFreeSlots() {
        return items.getFreeSlot() != -1;
    }

    public void init() {
        player.getPackets().sendItems(93, items);
    }

    public void refresh() {
        player.getPackets().sendItems(93, items);
        refreshConfigs(true);
    }

    public void refresh(int... slots) {
        player.getPackets().sendUpdateItems(93, items, slots);
        refreshConfigs(false);
    }

    public void refreshItems(Item[] itemsBefore) {
        int[] changedSlots = new int[itemsBefore.length];
        int count = 0;
        for (int index = 0; index < itemsBefore.length; index++) {
            if (itemsBefore[index] != items.getItems()[index])
                changedSlots[count++] = index;
        }
        int[] finalChangedSlots = new int[count];
        System.arraycopy(changedSlots, 0, finalChangedSlots, 0, count);
        refresh(finalChangedSlots);
    }

    public boolean removeItems(Item... list) {
        for (Item item : list) {
            if (item == null)
                continue;
            deleteItem(item);
        }
        return true;
    }

    public void deleteInventoryItem(int itemId, int amount) {
        if (!player.getControlerManager().canDeleteInventoryItem(itemId, amount))
            return;
        if (itemId == 995 && player.getMoneyPouch().getTotal() >= amount) {
            player.getMoneyPouch().removeMoneyMisc(amount);
            return;
        }
        Item[] itemsBefore = items.getItemsCopy();
        items.remove(new Item(itemId, amount));
        refreshItems(itemsBefore);
    }

    public void reset() {
        Lend lend = LendingManager.getLend(player);
        if (lend != null) {
            Player lender = World.getPlayer(lend.getLendee());
            if (lender.getInventory().containsOneItem(lend.getItem().getDefinitions().getLendId()))
                LendingManager.unLend(lend);
        }
        items.reset();
        init(); // as all slots reseted better just send all again
    }


    public void sendExamine(int slotId) {
        if (slotId >= getItemsContainerSize())
            return;
        Item item = items.get(slotId);
        if (item == null)
            return;

        // 1. Send Item ID
        player.getPackets().sendGameMessage("<col=3366ff>Item ID: " + item.getId() + "</col>");

        // 2. Send Examine text
        player.getPackets().sendGameMessage(ItemExaminesDataParser.getExamine(item));

        // 3. Send GE Price if tradeable
        if (ItemConstants.isTradeable(item)) {
            player.getPackets().sendGameMessage("<col=00ff00>GE guide price: " + Utils.formatNumber(GrandExchange.getPrice(item.getId())) + " gp.</col>");
        }
    }



    /*
     * No refresh needed its client to who does it :p
     */
    public void switchItem(int fromSlot, int toSlot) {
        Item[] itemsBefore = items.getItemsCopy();
        Item fromItem = items.get(fromSlot);
        Item toItem = items.get(toSlot);
        items.set(fromSlot, toItem);
        items.set(toSlot, fromItem);
        refreshItems(itemsBefore);
    }
    
    public void unlockInventoryOptions(boolean menu) { 
        unlockInventoryOptions(menu, false);
    }
    
    public void unlockInventoryOptions(boolean menu, boolean shiftDrop) {
        player.getPackets().sendIComponentSettings(menu ? INVENTORY_INTERFACE_2 : INVENTORY_INTERFACE, menu ? 8 : 7, -1, -1, 2097152);
        player.getPackets().sendIComponentSettings(menu ? INVENTORY_INTERFACE_2 : INVENTORY_INTERFACE, menu ? 8 : 7, 0, 27, shiftDrop ? 2 : 15302030);
        player.getPackets().sendIComponentSettings(menu ? INVENTORY_INTERFACE_2 : INVENTORY_INTERFACE, 27, 0, 14, 1422);
        player.getPackets().sendIComponentSettings(menu ? INVENTORY_INTERFACE_2 : INVENTORY_INTERFACE, menu ? 45 : 1, 0, 5, 2099198);
        if (shiftDrop)
            player.getPackets().sendInterSetItemsOptionsScript(menu ? INVENTORY_INTERFACE_2 : INVENTORY_INTERFACE, menu ? 8 : 7, 93, 4, 7, "Drop");
    }

    public void set(int i, Item item) {
        items.set(i, item);
        refresh();
    }

    public int getNumberOf(int itemId) {
        return items.getNumberOf(itemId);
    }

    public boolean containsItem(Item item) {
        return items.contains(item);
    }

    public boolean addItemDrop(Item item) {
        return addItemDrop(item, new WorldTile(player));
    }

    public boolean addItemDrop(Item item, WorldTile tile) {
        if (item.getId() < 0 || item.getAmount() < 0 || !Utils.itemExists(item.getId())
                || !player.getControlerManager().canAddInventoryItem(item.getId(), item.getAmount()))
            return false;
        int newAmount = addCoalBag(new Item(item.getId(), item.getAmount()));
        if (newAmount == 0)
            return false;
        int amount = newAmount;
        if (item.getId() == 995) {
            player.getMoneyPouch().addMoney(amount, false);
            return true;
        }
        Item[] itemsBefore = items.getItemsCopy();
        if (!items.add(new Item(item.getId(), amount).setAttributes(item.getAttributes())))
            World.updateGroundItem(new Item(item.getId(), amount).setAttributes(item.getAttributes()), tile, player, 60, 0, false);
        else
            refreshItems(itemsBefore);
        return true;
    }

    public boolean addItemDrop(int itemId, int amount) {
        return addItemDrop(new Item(itemId, amount), new WorldTile(player));
    }

    public void addItemMoneyPouch(Item item) {
        if (item.getId() == 995) {
            player.addMoney(item.getAmount());
            return;
        }
        addItem(item);
    }

    public void replaceItem(int id, int amount, int slot) {
        Item item = items.get(slot);
        if (item == null)
            return;
        if (id == -1)
            items.set(slot, null);
        else {
            item.setId(id);
            item.setAmount(amount);
            item.setAttributes(null);
        }
        refresh(slot);
    }

    public double getInventoryValue() {
        double value = 0;
        for (int i = 0; i < getItemsContainerSize(); i++) {
            try {
                Item item = getItems().get(i);
                if (item.getId() == 995 || item.getId() == 8851 || item.getId() == 12852)
                    value += item.getAmount() / 1000000;
                else
                    value += ((GrandExchange.getPrice(item.getId()) / 1000000) * item.getAmount());
            } catch (NullPointerException e) {
            }
        }
        return value;
    }

    public boolean removeItemMoneyPouch(Item item) {
        if (item.getId() == 995)
            return player.getMoneyPouch().removeMoneyMisc(item.getAmount());
        return removeItems(item);
    }

    /**
     * This function deletes {@code amount} worth of coins, starting with the pouch and moving to the inventory
     * if there isn't enough in the pouch. Does not modify the coin pouch or inventory if there aren't enough
     * coins to delete.
     *
     * @return {@code false} if there weren't enough coins.
     */
    public boolean deleteAllCoins(int amount) {
        int pouchAmt = player.getMoneyPouchValue();
        if (pouchAmt >= amount) {
            player.getMoneyPouch().removeMoneyMisc(amount);
            return true;
        }
        int inventoryAmt = getAmountOf(995);
        if (pouchAmt + inventoryAmt < amount) {
            return false;
        }
        amount -= pouchAmt;
        player.getMoneyPouch().removeMoneyMisc(pouchAmt);
        player.getInventory().deleteItem(new Item(995, amount));
        refresh();
        return true;
    }

    public int getCoinsAmount() {
        int coins = items.getNumberOf(995) + player.getMoneyPouch().getTotal();
        return coins < 0 ? Integer.MAX_VALUE : coins;
    }

    public boolean isFull() {
        return getFreeSlots() == 0;
    }

    public int getItemSlot(Item item) {
        for (int i = 0; i < items.getItems().length; i++) {
            if (items.getItems()[i] != null && items.getItems()[i].getId() == item.getId()
                    && items.getItems()[i].getAttributes() == item.getAttributes())
                return i;
        }
        return -1;
    }
    public int getItemSlot(int id) {
        for (int i = 0; i < items.getItems().length; i++) {
            if (items.getItems()[i] != null && items.getItems()[i].getId() == id)
                return i;
        }
        return -1;
    }

    public Item getItemById(int itemId) {
        for (Item item : getItemArray()) {
            if(item == null) {
                continue;
            }
            if(item.getId() == itemId) {
                return item;
            }
        }
        return null;
    }

    public boolean containsItem(Item item, int amount) {
        return items.contains(new Item(item.getId(), amount, item.getCharges()).setAttributes(item.getAttributes()));
    }

    /**
     * <strong>Using this incorrectly can cause certain things in the item container to break.
     * To keep usages as safe as possible, do not modify the array directly unless you're aware of the repercussions.</strong>
     */
    public Item[] getItemArray() {
        return items.getItems();
    }

    public void forEach(Consumer<Item> itemAction) {
        for (Item item : items.getItems()) {
            if (item == null) {
                continue;
            }
            itemAction.accept(item);
        }
    }
}