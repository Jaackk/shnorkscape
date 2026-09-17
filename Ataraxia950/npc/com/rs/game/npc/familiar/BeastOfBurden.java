package com.rs.game.npc.familiar;

import java.io.Serializable;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.player.Player;
import com.rs.game.player.content.InterfaceManager;
import com.rs.game.player.content.ItemConstants;

public class BeastOfBurden implements Serializable {

    private static final int ITEMS_KEY = 530;

    /**
     *
     */
    private static final long serialVersionUID = -2090871604834210257L;

    private transient Player player;
    private transient Familiar familiar;

    private final ItemsContainer<Item> beastItems;

    public BeastOfBurden(int size) {
        beastItems = new ItemsContainer<Item>(size, false);
    }

    public void addItem(Item item) {
        if (!ItemConstants.isTradeable(item) && item.getId() != 23194 || item.getId() == 4049 || (familiar.canStoreEssOnly() && item.getId() != 1436 && item.getId() != 7936)) {
            player.sendMessage("You cannot store this item in this familiar.");
            return;
        }
        Item[] itemsBefore = beastItems.getItemsCopy();
        int freeSpace = beastItems.getFreeSlots();
        if (!item.getDefinitions().isStackable()) {
            if (freeSpace == 0) {
                player.sendMessage("Not enough space in your familiar inventory.");
                return;
            }

            if (freeSpace < item.getAmount()) {
                item.setAmount(freeSpace);
                player.sendMessage("Not enough space in your familiar inventory.");
            }
        } else {
            if (freeSpace == 0 && !beastItems.containsOne(item)) {
                player.sendMessage("Not enough space in your familiar inventory.");
                return;
            }
        }
        beastItems.add(item);
//        beastItems.shift();
        refreshItems(itemsBefore);
        player.setNextAnimation(new Animation(23314));
    }

    public void addItem(int slot, int amount) {
        Item item = player.getInventory().getItem(slot);
        if (item == null)
            return;
        if (!ItemConstants.isTradeable(item) || item.getId() == 4049 || (familiar.canStoreEssOnly() && item.getId() != 1436 && item.getId() != 7936) || item.getDefinitions().getValue() > 50000 || item.getDefinitions().isLended()) {
            player.getPackets().sendGameMessage("You cannot store this item.");
            return;
        }
        if (item.getDefinitions().isNoted()) {
            player.getPackets().sendGameMessage("You cannot store this item.");
            return;
        }
        Item[] itemsBefore = beastItems.getItemsCopy();
        int maxAmount = player.getInventory().getItems().getNumberOf(item);
        if (amount < maxAmount)
            item = new Item(item.getId(), amount);
        else
            item = new Item(item.getId(), maxAmount);
        int freeSpace = beastItems.getFreeSlots();
        if (!item.getDefinitions().isStackable()) {
            if (freeSpace == 0) {
                player.getPackets().sendGameMessage("Not enough space in your Familiar Inventory.");
                return;
            }

            if (freeSpace < item.getAmount()) {
                item.setAmount(freeSpace);
                player.getPackets().sendGameMessage("Not enough space in your Familiar Inventory.");
            }
        } else {
            if (freeSpace == 0 && !beastItems.containsOne(item)) {
                player.getPackets().sendGameMessage("Not enough space in your Familiar Inventory.");
                return;
            }
        }
        beastItems.add(item);
//        beastItems.shift();
        player.getInventory().deleteItem(slot, item);
        refreshItems(itemsBefore);
        player.setNextAnimation(new Animation(23314));
    }

    public boolean containsOneItem(int... itemIds) {
        for (int itemId : itemIds) {
            if (beastItems.containsOne(new Item(itemId, 1)))
                return true;
        }
        return false;
    }

    public void dropBob() {
        int size = familiar.getSize();
        WorldTile WorldTile = new WorldTile(familiar.getCoordFaceX(size), familiar.getCoordFaceY(size), familiar.getPlane());
        for (int i = 0; i < beastItems.getSize(); i++) {
            Item item = beastItems.get(i);
            if (item != null) {
                if (player.getRights() < 2 || player.getRights() >= 2 && !player.isOwner())
                    World.addGroundItem(item, WorldTile, player, false, -1);
            }
        }
        beastItems.reset();
    }

    public ItemsContainer<Item> getBeastItems() {
        return beastItems;
    }

    public void open() {
        player.getInterfaceManager().sendInterface(671);
        player.getVarsManager().sendVar(3923, 1);
        player.getVarsManager().forceSendVar(1784, familiar.getId());
        sendInterItems();
        sendOptions();
        player.getInterfaceManager().sendLockGameTab(InterfaceManager.INVENTORY_TAB, true);
        player.setCloseInterfacesEvent(new Runnable() {
            @Override
            public void run() {
                player.getInterfaceManager().sendLockGameTab(InterfaceManager.INVENTORY_TAB, false);
            }
        });
    }

    public void refresh(int... slots) {
        player.getPackets().sendUpdateItems(ITEMS_KEY, beastItems, slots);
    }

    public void refreshItems(Item[] itemsBefore) {
        int[] changedSlots = new int[itemsBefore.length];
        int count = 0;
        for (int index = 0; index < itemsBefore.length; index++) {
            Item item = beastItems.getItems()[index];
            if (itemsBefore[index] != item) {
                changedSlots[count++] = index;
            }

        }
        int[] finalChangedSlots = new int[count];
        System.arraycopy(changedSlots, 0, finalChangedSlots, 0, count);
        refresh(finalChangedSlots);
    }

    public void removeItem(int slot, int amount) {
        Item item = beastItems.get(slot);
        if (item == null)
            return;
        Item[] itemsBefore = beastItems.getItemsCopy();
        int maxAmount = beastItems.getNumberOf(item);
        if (amount < maxAmount)
            item = new Item(item.getId(), amount);
        else
            item = new Item(item.getId(), maxAmount);
        int freeSpace = player.getInventory().getFreeSlots();
        if (!item.getDefinitions().isStackable()) {
            if (freeSpace == 0) {
                player.getPackets().sendGameMessage("Not enough space in your inventory.");
                return;
            }
            if (freeSpace < item.getAmount()) {
                item.setAmount(freeSpace);
                player.getPackets().sendGameMessage("Not enough space in your inventory.");
            }
        } else {
            if (freeSpace == 0 && !player.getInventory().containsItem(item.getId(), 1)) {
                player.getPackets().sendGameMessage("Not enough space in your inventory.");
                return;
            }
        }
        beastItems.remove(slot, item);
//        beastItems.shift();
        player.getInventory().addItem(item);
        refreshItems(itemsBefore);
        player.setNextAnimation(new Animation(23314));
    }

    public void switchItem(int fromSlot, int toSlot) {
        Item[] itemsBefore = beastItems.getItemsCopy();
        Item fromItem = beastItems.get(fromSlot);
        Item toItem = beastItems.get(toSlot);
        beastItems.set(fromSlot, toItem);
        beastItems.set(toSlot, fromItem);
        refreshItems(itemsBefore);
    }

    public void sendInterItems() {
        player.getPackets().sendItems(ITEMS_KEY, beastItems);
        player.getPackets().sendItems(93, player.getInventory().getItems());
    }

    public void sendOptions() {
        player.getPackets().sendIComponentSettings(671, 27, 0, 32, 62);
        player.getPackets().sendIComponentSettings(671, 32, 0, 28, 62);
    }

    public void setEntitys(Player player, Familiar familiar) {
        this.player = player;
        this.familiar = familiar;
    }

    public void takeBob() {
        Item[] itemsBefore = beastItems.getItemsCopy();
        for (int i = 0; i < beastItems.getSize(); i++) {
            Item item = beastItems.get(i);
            if (item != null) {
                if (!player.getInventory().addItem(item))
                    break;
                beastItems.remove(i, item);
            }
        }
//        beastItems.shift();
        refreshItems(itemsBefore);
        player.setNextAnimation(new Animation(23314));
    }

    public void giveBob() {
        if (canDepositOnly()) {
            player.getPackets().sendGameMessage("You cannot store items in this familiar.");
            return;
        }
        Item[] inventoryItemsBefore = player.getInventory().getItems().getItemsCopy();
        Item[] itemsBefore = beastItems.getItemsCopy();
        boolean error = false;
        for (int i = 0; i < player.getInventory().getItems().getSize(); i++) {
            Item item = player.getInventory().getItems().get(i);
            if (item != null) {
                if (!ItemConstants.isTradeable(item) || item.getId() == 4049 || (familiar.canStoreEssOnly() && item.getId() != 1436 && item.getId() != 7936) || item.getDefinitions().getValue() > 50000 || item.getDefinitions().isLended()) {
                    error = true;
                    continue;
                }
                if (item.getDefinitions().isNoted()) {
                    error = true;
                    continue;
                }
                int freeSpace = beastItems.getFreeSlots();
                if (!item.getDefinitions().isStackable()) {
                    if (freeSpace == 0)
                        break;
                    if (freeSpace < item.getAmount())
                        item.setAmount(freeSpace);
                } else {
                    if (freeSpace == 0 && !beastItems.containsOne(item)) {
                        error = true;
                        break;
                    }
                }
                beastItems.add(item);
                player.getInventory().getItems().remove(i, item);
            }
        }
        if (error)
            player.getPackets().sendGameMessage("Couldn't store all of your items.");
//        beastItems.shift();
        player.setNextAnimation(new Animation(23314));
        refreshItems(itemsBefore);
        player.getInventory().refreshItems(inventoryItemsBefore);
    }

    public boolean canDepositOnly() {
        return familiar.getDefinitions().hasOption("withdraw"); // fix
    }
    
    public boolean canStoreItem(Item item) {
        return canStoreItem(item, true);
    }
    public boolean canStoreItem(Item item, boolean sendMessage) {
        if (!ItemConstants.isTradeable(item) && item.getId() != 23194 || item.getId() == 4049 || (familiar.canStoreEssOnly() && item.getId() != 1436 && item.getId() != 7936)) {
            if (sendMessage)
                player.sendMessage("You cannot store this item in this familiar.");
            return false;
        }
        if (canDepositOnly()) {
            player.getPackets().sendGameMessage("You cannot store items in this familiar.");
            return false;
        }
        if (!ItemConstants.isTradeable(item) || item.getId() == 4049 || (familiar.canStoreEssOnly() && item.getId() != 1436 && item.getId() != 7936) || item.getDefinitions().getValue() > 50000 || item.getDefinitions().isLended()) {
            if (sendMessage)
                player.sendMessage("You cannot store this item in this familiar.");
            return false;
        }
        if (item.getDefinitions().isNoted()) {
            if (sendMessage)
                player.sendMessage("You cannot store this item in this familiar.");
            return false;
        }
        return true;
    }
}
