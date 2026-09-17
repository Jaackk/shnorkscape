package com.rs.game.player;

import java.util.concurrent.ConcurrentHashMap;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;

public final class MetalBankStorage extends Bank {

    private static final long serialVersionUID = -2943256137591492541L;
    private static final String NAME = "Metal Bank";

    private transient Player owner;

    public MetalBankStorage() {
        super(NAME);
    }

    @Override
    public void setPlayer(Player player) {
        super.setPlayer(player);
        owner = player;
        super.setName(NAME);
    }

    @Override
    public void setName(String name) {
        super.setName(NAME);
    }

    @Override
    public void addItem(Item item, boolean refresh) {
        if (canStore(item)) {
            super.addItem(item, refresh);
        }
    }

    @Override
    public void addItem(int id, int quantity, int charges, ConcurrentHashMap<TemporaryAttributes.Key, Object> attributes, boolean refresh) {
        if (canStore(id)) {
            super.addItem(id, quantity, charges, attributes, refresh);
        }
    }

    @Override
    public void addItem(int id, int quantity, int charges, ConcurrentHashMap<TemporaryAttributes.Key, Object> attributes, int creationTab, boolean refresh) {
        if (canStore(id)) {
            super.addItem(id, quantity, charges, attributes, creationTab, refresh);
        }
    }

    @Override
    public int addItems(Item[] items, boolean refresh) {
        int added = 0;
        for (Item item : items) {
            if (item == null || !canStore(item)) {
                continue;
            }
            super.addItem(item, false);
            added++;
        }
        if (refresh && added > 0) {
            refreshTabs();
            refreshItems();
        }
        return added;
    }

    @Override
    public int addItems(Item[] items, boolean refresh, boolean aura) {
        return addItems(items, refresh);
    }

    @Override
    public boolean depositItem(int containerSlot, int quantity, boolean refresh, int componentId) {
        Item item = getContainerItem(containerSlot, componentId);
        if (item == null) {
            return false;
        }
        if (!canStore(item)) {
            sendMetalOnlyMessage();
            refreshInteractionContainer(componentId);
            refreshMetalBankContainer();
            return false;
        }
        return super.depositItem(containerSlot, quantity, refresh, componentId);
    }

    @Override
    public void depositAllInventory(boolean banking) {
        if (owner == null || owner.getInventory().getItems().isEmpty()) {
            return;
        }
        boolean skippedNonMetal = false;
        for (int slot = 0; slot < 28; slot++) {
            Item item = owner.getInventory().getItem(slot);
            if (item == null) {
                continue;
            }
            if (!canStore(item)) {
                skippedNonMetal = true;
                continue;
            }
            super.depositItem(slot, item.getAmount(), false);
        }
        if (skippedNonMetal) {
            sendMetalOnlyMessage();
        }
        owner.getInventory().refresh();
        lastContainerCopy = null;
        refreshItems();
    }

    @Override
    public void depositAllEquipment(boolean banking) {
        sendMetalOnlyMessage();
    }

    @Override
    public void depositAllBob(boolean banking) {
        sendMetalOnlyMessage();
    }

    @Override
    public void depositMoneyPouch(boolean banking) {
        sendMetalOnlyMessage();
    }

    @Override
    public boolean canDepositItemsToBank(Item[] items) {
        return canDepositItemsToBank(items, false);
    }

    @Override
    public boolean canDepositItemsToBank(Item[] items, boolean skipAuras) {
        for (Item item : items) {
            if (item == null) {
                continue;
            }
            if (!canStore(item)) {
                return false;
            }
        }
        return super.canDepositItemsToBank(items, skipAuras);
    }

    private Item getContainerItem(int containerSlot, int componentId) {
        if (owner == null) {
            return null;
        }
        ItemsContainer<Item> container = componentId == 27 ? owner.getEquipment().getItems()
                : componentId == 14 ? owner.getInventory().getItems()
                : owner.getFamiliar() != null && owner.getFamiliar().getBob() != null ? owner.getFamiliar().getBob().getBeastItems()
                : null;
        if (container == null || containerSlot < 0 || containerSlot >= container.getSize()) {
            return null;
        }
        return container.get(containerSlot);
    }

    private void refreshInteractionContainer(int componentId) {
        if (owner == null) {
            return;
        }
        if (componentId == 14) {
            owner.getInventory().refresh();
        } else if (componentId == 27) {
            owner.getEquipment().refreshItemContainer();
            owner.getAppearence().generateAppearenceData();
        } else if (owner.getFamiliar() != null && owner.getFamiliar().getBob() != null) {
            owner.getFamiliar().getBob().sendInterItems();
        }
    }

    private void refreshMetalBankContainer() {
        lastContainerCopy = null;
        sendItems();
        refreshTabs();
        refreshItems();
    }

    private boolean canStore(Item item) {
        return item != null && canStore(item.getId());
    }

    private boolean canStore(int itemId) {
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(itemId);
        if (defs == null) {
            return false;
        }
        if (defs.isNoted() && defs.getCertId() != -1) {
            itemId = defs.getCertId();
        }
        return com.rs.game.player.content.MetalBank.isMetalBankItem(itemId);
    }

    private void sendMetalOnlyMessage() {
        if (owner != null) {
            owner.getPackets().sendMainInterfaceMessage(1, "Only ores and bars can be stored in the metal bank.", true);
        }
    }
}
