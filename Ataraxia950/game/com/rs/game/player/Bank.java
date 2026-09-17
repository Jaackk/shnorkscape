package com.rs.game.player;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.rs.Settings;
import com.rs.cache.loaders.BodyDefinitions;
import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.activites.dnd.eviltree.EvilTreeInstanceController;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.player.TemporaryAttributes.Key;
import com.rs.game.player.actions.invention.InventionData;
import com.rs.game.player.actions.summoning.Summoning;
import com.rs.game.player.actions.summoning.Summoning.Pouches;
import com.rs.game.player.content.InterfaceManager;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.TriskKeyBag;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.controllers.DungeonController;
import com.rs.game.player.controllers.bossInstance.BossInstanceController;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.packet.PacketRepository;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.items.ItemExaminesDataParser;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

public class Bank implements Serializable {
    // script_9850 contains varbits for all bank boosters
    private static final long serialVersionUID = 1551246756081236625L;
    public static final int MAX_BANK_SIZE = 600;
    // tab, items
    public Item[][] bankTabs;
    public transient Item[] lastContainerCopy;
    private transient int firstEmptyFreeSlot;
    private int lastX;
    private transient Player player;
    private int currentTab;
    private transient boolean withdrawNotes;
    private transient boolean insertItems;
    private int collectableItem;
    private String name;

    @SuppressWarnings("unused")
    private List<Integer> placeholders;

    public Bank() {
        name = "Bank of Runescape";
        bankTabs = new Item[1][0];
        tabsDetails = new TabDetails[0];
        bankPresets = new BankPreset[10];
        bobPreset = new BobPreset();
    }






    public Bank(String name) {
        this.name = name;
        bankTabs = new Item[1][0];
        tabsDetails = new TabDetails[0];
        bankPresets = new BankPreset[10];
        bobPreset = new BobPreset();
    }

    public void addItem(int id, int quantity, int charges, ConcurrentHashMap<TemporaryAttributes.Key, Object> attributes, boolean refresh) {
        addItem(id, quantity, charges, attributes, currentTab, refresh);
    }

    public void addItem(Item item, boolean refresh) {
        addItem(item.getId(), item.getAmount(), item.getCharges(), item.getAttributes(), refresh);
    }

    public void addItem(int id, int quantity, int charges, ConcurrentHashMap<TemporaryAttributes.Key, Object> attributes, int creationTab, boolean refresh) {
        if (id != 11640 && player.getControlerManager().getControler() instanceof DungeonController)
            return;
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(id);
        if (defs.isNoted() && defs.getCertId() != -1)
            id = defs.getCertId();
        int[] slotInfo = getItemSlot(id, attributes);
        if (creationTab == 0 && slotInfo == null)
            slotInfo = getEmptyItemSlot();
        if (slotInfo == null) {
            if (creationTab >= bankTabs.length)
                creationTab = bankTabs.length - 1;
            if (creationTab < 0) // fixed now, alex
                creationTab = 0;
            int slot = bankTabs[creationTab].length;
            Item[] tab = new Item[slot + 1];
            System.arraycopy(bankTabs[creationTab], 0, tab, 0, slot);
            tab[slot] = new Item(id, quantity, charges).setAttributes(attributes);
            bankTabs[creationTab] = tab;
            if (refresh)
                refreshTab(creationTab);
        } else {
            Item item = bankTabs[slotInfo[0]][slotInfo[1]];
            if (item == null) {
                bankTabs[slotInfo[0]][slotInfo[1]] = new Item(id, quantity, charges).setAttributes(attributes);
            } else {
                if (quantity + item.getAmount() < 0) {
                    return;
                }
                bankTabs[slotInfo[0]][slotInfo[1]] = new Item(item.getId(), item.getAmount() + quantity, charges).setAttributes(attributes);
            }
        }
        if (refresh) {
            refreshBankSize();
            refreshItems();
        }
    }

    public int addItems(Item[] items, boolean refresh) {
        int space = MAX_BANK_SIZE - getBankSize(false);
        if (space != 0) {
            space = (space < items.length ? space : items.length);
            for (int i = 0; i < space; i++) {
                if (items[i] == null)
                    continue;
                addItem(items[i], false);
            }
            if (refresh) {
                refreshTabs();
                refreshItems();
            }
        }
        return space;
    }


    public int addItems(Item[] items, boolean refresh, boolean aura) {
        int space = MAX_BANK_SIZE - getBankSize(false);
        if (space != 0) {
            space = (space < items.length ? space : items.length);
            for (int i = 0; i < space; i++) {
                if (items[i] == null || aura && items[i].getDefinitions().getEquipSlot() == Equipment.SLOT_AURA && player.getAuraManager().isActivated())
                    continue;
                addItem(items[i], false);
            }
            if (refresh) {
                refreshTabs();
                refreshItems();
            }
        }
        return space;
    }

    public void collapse(int tabId) {
        if (tabId == 0 || tabId >= bankTabs.length)
            return;
        Item[] items = bankTabs[tabId];
        for (Item item : items)
            removeItem(getItemSlot(item), item.getAmount(), false, Bank.DESTROY_ITEM);
        for (Item item : items)
            addItem(item.getId(), item.getAmount(), item.getCharges(), item.getAttributes(), 0, false);
        refreshTabs();
        refreshItems();
    }

    public void createTab() {
        int slot = bankTabs.length;
        if (slot >= 15)
            return;
        Item[][] tabs = new Item[slot + 1][];
        TabDetails[] newTabDetails = new TabDetails[slot];
        System.arraycopy(bankTabs, 0, tabs, 0, slot);
        System.arraycopy(this.tabsDetails, 0, newTabDetails, 0, slot - 1);
        tabs[slot] = new Item[0];
        newTabDetails[slot - 1] = new TabDetails(slot - 1, 0, 0);
        bankTabs = tabs;
        tabsDetails = newTabDetails;
    }

    public void depositAllBob(boolean banking) {
        Familiar familiar = player.getFamiliar();
        if (familiar == null || familiar.getBob() == null) {
            player.getPackets().sendMainInterfaceMessage(1, "You don't have a familiar.", true);
            return;
        }
        if (player.getFamiliar().getBob().getBeastItems().getFreeSlots() == 28) {
            player.getPackets().sendMainInterfaceMessage(1, "Your beast of burden is not carrying any items.", true);
            return;
        }
        int space = addItems(familiar.getBob().getBeastItems().getItems(), banking);
        if (space != 0) {
            for (int i = 0; i < space; i++)
                familiar.getBob().getBeastItems().set(i, null);
            familiar.getBob().sendInterItems();
        }
        if (space < familiar.getBob().getBeastItems().getSize()) {
            player.getPackets().sendMainInterfaceMessage(1, "Bank full. To make more room, sell, drop or withdraw something.", true);
            return;
        }
    }

    public void depositAllEquipment(boolean banking) {
        if (player.isNative950()) {
            com.rs.game.player.client.Native950Banking.depositEquipment(player, this, banking);
            return;
        }
        if (!player.getEquipment().wearingArmour()) {
            player.getPackets().sendMainInterfaceMessage(1, "Your body is already naked.", true);
            return;
        }
        Item[] items = player.getEquipment().getItems().getItems().clone();
        items[Equipment.SLOT_AURA] = null;
        int space = addItems(items, banking, true);
        if (space != 0) {
            for (int i = 0; i < space; i++) {
                if (i != Equipment.SLOT_AURA)
                    player.getEquipment().getItems().set(i, null);
            }
            player.getEquipment().init();
            player.getAppearence().generateAppearenceData();
        }
        if (space < player.getEquipment().getItems().getSize()) {
            player.getPackets().sendMainInterfaceMessage(1, "Bank full. To make more room, sell, drop or withdraw something.", true);
            return;
        }
    }

    public void depositAllInventory(boolean banking) {
        if (player.getInventory().getItems().isEmpty())
            return;
        int count = 0;
        for (int index = 0; index < 28; index++) {
            val item = player.getInventory().getItem(index);
            if (item != null && !depositItem(index, item.getAmount(), false)) {
                count++;
                continue;
            }
        }
        if (count > 1)
            player.getPackets().sendMainInterfaceMessage(1, "You could not bank all of your items.", true);
//        refreshTab(currentTab);
        refreshItems();
    }

    public boolean depositItem(int containerSlot, int quantity, boolean refresh) {
        return depositItem(containerSlot, quantity, refresh, 14);
    }

    public boolean depositItem(int containerSlot, int quantity, boolean refresh, int componentId) {
        if (player.isNative950()) return com.rs.game.player.client.Native950Banking.deposit(player, this, containerSlot, quantity, refresh, componentId);
        if (quantity < 1)
            return false;
        ItemsContainer<Item> container = componentId == 27 ? player.getEquipment().getItems() : componentId == 14 ? player.getInventory().getItems() : player.getFamiliar() != null && player.getFamiliar().getBob() != null ? player.getFamiliar().getBob().getBeastItems() : null;
        if (container == null || containerSlot < 0 || containerSlot >= container.getSize())
            return false;
        Item item = container.get(containerSlot);
        if (item == null)
            return false;
        if (item.getDefinitions().getEquipSlot() == Equipment.SLOT_AURA && componentId == 27)
            return false;
        String cantBankMessage = ItemConstants.isBankAble(player, item);
        if (cantBankMessage != null) {
            player.getPackets().sendMainInterfaceMessage(1, cantBankMessage, true);
            return false;
        }
        int amt = container.getNumberOf(item);
        if (amt < quantity)
            item = new Item(item.getId(), amt, item.getCharges()).setAttributes(item.getAttributes());
        else
            item = new Item(item.getId(), quantity, item.getCharges()).setAttributes(item.getAttributes());
        ItemDefinitions defs = item.getDefinitions();
        int originalId = item.getId();
        if (defs.isNoted() && defs.getCertId() != -1)
            item.setId(defs.getCertId());




        if (hitsPortableCap(item)) {
            player.getPackets().sendMainInterfaceMessage(1,
                    "Portable Bank can only hold " + PORTABLE_MAX_STACKS + " stacks.", true);
            return false;
        }





        Item bankedItem = getItemIncludingPlaceHolders(item);
        if (bankedItem != null) {
            if (((long) bankedItem.getAmount() + (long) item.getAmount()) > Integer.MAX_VALUE) {
                item.forceSetAmount(Integer.MAX_VALUE - bankedItem.getAmount());
                player.getPackets().sendMainInterfaceMessage(1, "Not enough space in your bank.", true);
                if (item.getAmount() == 0)
                    return false;
            }
        } else if (!hasBankSpace()) {
            player.getPackets().sendMainInterfaceMessage(1, "Not enough space in your bank.", true);
            return false;
        }
        if (item.getAmount() > 0) {
            container.remove(containerSlot, new Item(originalId, item.getAmount(), item.getCharges()).setAttributes(item.getAttributes()));
            addItem(item, refresh);
        }
        player.gimBank.addHistory("deposited", item);
        if (componentId == 14)
            player.getInventory().refresh();
        else if (componentId == 27) {
            player.getEquipment().refreshItemContainer();
            player.getAppearence().generateAppearenceData();
        } else {
            player.getFamiliar().getBob().sendInterItems();
        }
        return true;
    }

    public void depositLastAmount(int bankSlot, int componentId) {
        depositItem(bankSlot, lastX, true, componentId);
    }

    public void depositMoneyPouch(boolean banking) {
        if (player.getMoneyPouch().getTotal() == 0) {
            player.getPackets().sendMainInterfaceMessage(1, "Your money pouch is already empty.", true);
            return;
        }
        int coinsCount = player.getMoneyPouch().getTotal();
        boolean hasBankSpace = hasBankSpace();
        if (hasBankSpace) {
            if (MAX_BANK_SIZE - getBankSize(false) < player.getInventory().getItems().getSize()) {
                player.getPackets().sendMainInterfaceMessage(1, "Bank full. To make more room, sell, drop or withdraw something.", true);
                return;
            }
            for (Item item : getContainerCopy()) {
                if (item == null)
                    continue;
                if (item.getId() == 995) {
                    if (item.getAmount() + coinsCount < 0) {
                        coinsCount = Integer.MAX_VALUE - item.getAmount();
                    }
                    if (item.getAmount() == Integer.MAX_VALUE) {
                        coinsCount = 0;
                    }
                }
            }
            if (coinsCount == 0) {
                player.getPackets().sendMainInterfaceMessage(1, "Your bank is full of coins already.", true);
                return;
            }
            if (player.getMoneyPouch().removeMoneyMisc(coinsCount)) {
                addItems(new Item[] { new Item(995, coinsCount) }, true);
            }
        }
        forceRefreshBank(false);
    }

    public void forceRefreshBank(boolean shiftItems) {
        if (!shiftItems)
            player.getTemporaryAttributtes().put(Key.SKIP_BANK_SHIFT_ITEMS, Boolean.TRUE);
        player.getInterfaceManager().removeBankInterface();
        player.getBank().openBank();
        player.getTemporaryAttributtes().remove(Key.SKIP_BANK_SHIFT_ITEMS, Boolean.TRUE);
    }

    public void destroyTab(int slot) {
        if (slot == 0)
            return;
        int detailSlot = slot - 1;
        Item[][] tabs = new Item[bankTabs.length - 1][];
        System.arraycopy(bankTabs, 0, tabs, 0, slot);
        System.arraycopy(bankTabs, slot + 1, tabs, slot, bankTabs.length - slot - 1);
        TabDetails[] newTabs = new TabDetails[tabsDetails.length - 1];
        System.arraycopy(tabsDetails, 0, newTabs, 0, detailSlot);
        System.arraycopy(tabsDetails, detailSlot + 1, newTabs, detailSlot, tabsDetails.length - detailSlot - 1);
        int deleletedIndex = tabsDetails[detailSlot].getOriginalIndex();
        for (TabDetails tab : newTabs) {
            int deletedCount = 0;
            for (int i = 0; i < tab.getOriginalIndex() + 1; i++)
                if (i == deleletedIndex)
                    deletedCount++;
            tab.setOriginalIndex(tab.getOriginalIndex() - deletedCount);
        }
        bankTabs = tabs;
        this.tabsDetails = newTabs;
        if (currentTab != 0 && currentTab >= slot) {
            currentTab = 0;
            refreshViewingTab();
        }
    }

    public Item[] generateContainer() {
        Item[] container = new Item[getBankSize(true)];
        int count = 0;
        for (int slot = 1; slot < bankTabs.length; slot++) {
            System.arraycopy(bankTabs[slot], 0, container, count, bankTabs[slot].length);
            count += bankTabs[slot].length;
        }
        System.arraycopy(bankTabs[0], 0, container, count, bankTabs[0].length);
        firstEmptyFreeSlot = getNullItemSlot(container);
        return container;
    }

    public int getBankSize(boolean withEmpty) {
        if (!withEmpty)
            return getItemsLength(bankTabs);
        int size = 0;
        for (int i = 0; i < bankTabs.length; i++)
            size += bankTabs[i].length;
        return size;
    }

    public int getRemainingSlots() {
        return MAX_BANK_SIZE - getBankSize(false);
    }

    public int getTotalBankSize() {
        int size = 0;
        for (int i = 0; i < bankTabs.length; i++)
            size += bankTabs[i].length;
        return size;
    }

    public Item[] getContainerCopy() {
        if (lastContainerCopy == null)
            lastContainerCopy = generateContainer();
        return lastContainerCopy;
    }

    public Item getItemIncludingPlaceHolders(int id) {
        for (int slot = 0; slot < bankTabs.length; slot++) {
            for (Item item : bankTabs[slot])
                if (item != null && item.getId() == id && item.getAttributes() == null && item.getCharges() == 0)
                    return item;
        }
        return null;
    }

    public Item getItem(int id) {
        for (int slot = 0; slot < bankTabs.length; slot++) {
            for (Item item : bankTabs[slot])
                if (item != null && item.getId() == id && item.getAttributes() == null && item.getCharges() == 0 && item.getAmount() > 0)
                    return item;
        }
        return null;
    }

    public Item getItemIncludingPlaceHolders(Item t) {
        for (int slot = 0; slot < bankTabs.length; slot++) {
            for (Item item : bankTabs[slot])
                if (item != null && item.exactMatch(t))
                    return item;
        }
        return null;
    }

    public Item getItem(Item t) {
        for (int slot = 0; slot < bankTabs.length; slot++) {
            for (Item item : bankTabs[slot])
                if (item != null && item.exactMatch(t) && item.getAmount() > 0)
                    return item;
        }
        return null;
    }

    public int[] getItemSlotCheckIdOnly(int id) {
        for (int tab = 0; tab < bankTabs.length; tab++) {
            for (int slot = 0; slot < bankTabs[tab].length; slot++) {
                Item item = bankTabs[tab][slot];
                if (item != null && item.getId() == id && item.getAmount() > 0)
                    return new int[] { tab, slot };
            }
        }
        return null;
    }

    public int[] getItemSlot(int id, ConcurrentHashMap<Key, Object> attributes) {
        for (int tab = 0; tab < bankTabs.length; tab++) {
            for (int slot = 0; slot < bankTabs[tab].length; slot++) {
                Item item = bankTabs[tab][slot];
                if (item != null && item.getId() == id) {
                    if (attributes != null && item.getAttributes() == null)
                        continue;
                    if (attributes == null && item.getAttributes() != null)
                        continue;
                    if (attributes != null && !attributes.get(Key.ITEM_UUID).equals(item.getAttributes().get(Key.ITEM_UUID)))
                        continue;
                    return new int[] { tab, slot };
                }
            }
        }
        return null;
    }

    public int[] getEmptyItemSlot() {
        for (int tab = 0; tab < bankTabs.length; tab++) {
            for (int slot = 0; slot < bankTabs[tab].length; slot++) {
                if (bankTabs[tab][slot] == null)
                    return new int[] { tab, slot };
            }
        }
        return null;
    }

    public int[] getItemSlot(Item item) {
        if (item == null)
            return null;
        for (int tab = 0; tab < bankTabs.length; tab++) {
            for (int slot = 0; slot < bankTabs[tab].length; slot++)
                if (bankTabs[tab][slot] != null && bankTabs[tab][slot].exactMatch(item))
                    return new int[] { tab, slot };
        }
        return null;
    }

    public Item getItem(int[] slot) {
        if (slot == null)
            return null;
        return bankTabs[slot[0]][slot[1]];
    }

    public int getLastX() {
        if (lastX <= 0)
            lastX = 1;
        return lastX;
    }

    public void setLastX(int lastX) {
        if (lastX <= 0)
            lastX = 1;
        this.lastX = lastX;
        refreshLastX();
    }

    public int[] getRealSlot(int slot) {
        for (int tab = 1; tab < bankTabs.length; tab++) {
            if (slot >= bankTabs[tab].length)
                slot -= bankTabs[tab].length;
            else
                return new int[] { tab, slot };
        }
        if (slot >= bankTabs[0].length)
            return null;
        return new int[] { 0, slot };
    }

    public int getStartSlot(int tabId) {
        int slotId = 0;
        for (int tab = 1; tab < (tabId == 0 ? bankTabs.length : tabId); tab++)
            slotId += bankTabs[tab].length;

        return slotId;

    }







    // Put these inside Bank
    private static final int PORTABLE_INDEX = 10;   // your "Portable Bank" slot
    private static final int PORTABLE_MAX_STACKS = 10;






    /** Returns this bank's index within player.getBanks(), or -1 if unknown. */
    public int indexInPlayer() {
        if (player == null || player.getBanks() == null) return -1;
        List<Bank> banks = player.getBanks();
        for (int i = 0; i < banks.size(); i++) if (banks.get(i) == this) return i;
        return -1;
    }

    /** Count of occupied stacks (ignores nulls and placeholders with amount==0). */
    public int getStackCount(boolean includePlaceholders) {
        int count = 0;
        for (Item[] tab : bankTabs)
            for (Item it : tab)
                if (it != null && (includePlaceholders || it.getAmount() > 0))
                    count++;
        return count;
    }

    /** Are we at/over cap AND would this item create a new stack (not merge)? */
    private boolean hitsPortableCap(Item depositCandidate) {
        if (indexInPlayer() != PORTABLE_INDEX) return false;
        int stacks = getStackCount(false);                 // positive-amount stacks
        if (stacks < PORTABLE_MAX_STACKS) return false;    // under cap, ok
        // Allow deposit only if it MERGES with an existing positive stack:
        Item existing = getItem(depositCandidate);         // exactMatch + amount>0
        return existing == null;                           // null -> new stack -> block
    }










    public int getTabSize(int slot) {
        if (slot >= bankTabs.length)
            return 0;
        return bankTabs[slot].length;
    }

    public boolean hasBankSpace() {
        return getBankSize(false) < MAX_BANK_SIZE;
    }


    public void openBankTest() {
//        player.getPackets().sendHideIComponent(762, 124, false);
//        player.getPackets().sendHideIComponent(762, 125, false);
        openBank();
    }

    public void openBank() {
        if (player.inPzInstance || player.getControlerManager().getControler() instanceof EvilTreeInstanceController) {
            return;
        }
        player.stopAll(true, true, true);
        player.setNextAnimation(new Animation(-1));
        player.getActionManager().forceStop();
        if (player.isNative950()) {
            // The native adapter owns the selected-cache presentation. Keep the
            // original controller/action/interface lifecycle without910 UI writes.
            interactionTab = 0;
            currentFilter = 0;
            leavePlaceHolders = false;
            currentTab = 0;
            player.getInterfaceManager().sendBankInterface(517);
            return;
        }
        player.getPackets().sendGlobalConfig(6709, 0);
        refreshBob();
        player.getInterfaceManager().sendBankInterface(517);
        player.getPackets().sendExecuteScript(8420, 33882113, 33882402, -1, 33882403, getName(), 28241, 1017);
        withdrawNotes = false;
        refreshViewingTab();
        refreshTabs();
        unlockButtons();
        sendItems();
        refreshLastX();
        refreshBankSize();
        refreshDefaultInteractionAmount();
        refreshConfigPresetsTab();
        refreshCurrentFilter();
        refreshLeavePlaceHolders();
        refreshWithdrawNotes();
        refreshInteractionTab();
        refreshTabPosition();
        closeHiddenTabs();
        player.getPackets().sendConfigByFile(39433, 0);// close bank presets tab
        setSelectedPreset(0);
        resetErrorIndexes();
        refreshBankSize();
        if (!(this instanceof MetalBankStorage)) {
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    if (player.getInventory() != null) {
                        Item triskBag = player.getInventory().getItemById(48480);
                        if (triskBag != null) {
                            TriskKeyBag.withdrawToBank(player, triskBag);
                        }
                    }
                }
            }, 1);
        }
    }

    public void refreshBob() {
        int bobSize = player.getFamiliar() == null || player.getFamiliar().getBob() == null ? 0 : player.getFamiliar().getBOBSize();
        player.getPackets().sendGlobalConfig(6709, bobSize);
        if (interactionTab == 1 && bobSize <= 0)
            setInteractionTab(0);
    }

    public void closeHiddenTabs() {
        player.getPackets().sendHideIComponent(517, 286, true);
        player.getPackets().sendHideIComponent(517, 294, true);
        player.getPackets().sendHideIComponent(517, 200, true);
    }

    private void refreshBankSize() {
        player.getPackets().sendConfig(8971, getBankSize(true));
    }

    public void openDepositBox() {
        if (player.isUnderCombat(6)) {
            player.getPackets().sendMainInterfaceMessage(1, "You cannot open the deposit box 10 seconds after combat.", true);
            return;
        }
        player.getInterfaceManager().sendInterface(11);
        player.getInterfaceManager().sendLockGameTab(InterfaceManager.INVENTORY_TAB, true);
        player.getInterfaceManager().sendLockGameTab(InterfaceManager.EQUIPMENT_TAB, true);
        player.getPackets().sendExecuteScript(8420, 720913, 720916, 720914, 720919, "Bank of " + Settings.SERVER_NAME + " - Deposit Box", 21218, 1007);
        sendBoxInterItems();
        player.setCloseInterfacesEvent(new Runnable() {
            @Override
            public void run() {
                player.getInterfaceManager().sendLockGameTab(InterfaceManager.INVENTORY_TAB, false);
                player.getInterfaceManager().sendLockGameTab(InterfaceManager.EQUIPMENT_TAB, false);
            }
        });
    }

    public void openPlayerBank(Player victim) {
        if (victim == null)
            return;
//        player.getInterfaceManager().sendInterface(762);
//        player.getInterfaceManager().sendInventoryInterface(763);
//        player.getPackets().sendItems(95, victim.getBank().getContainerCopy());
//        refreshViewingTab();
//        refreshTabs();
//        unlockButtons();
    }

    public void refreshItems() {
        refreshItems(generateContainer(), getContainerCopy());
        refreshFirstEmptyFreeSlot();
    }

    public void refreshItems(int[] slots) {
        player.getPackets().sendUpdateItems(95, getContainerCopy(), slots);
    }

    public void refreshItems(Item[] itemsAfter, Item[] itemsBefore) {
        if (itemsBefore.length != itemsAfter.length) {
            lastContainerCopy = itemsAfter;
            sendItems();
            return;
        }
        int[] changedSlots = new int[itemsAfter.length];
        int count = 0;
        for (int index = 0; index < itemsAfter.length; index++) {
            if (itemsBefore[index] != itemsAfter[index])
                changedSlots[count++] = index;
        }
        int[] finalChangedSlots = new int[count];
        System.arraycopy(changedSlots, 0, finalChangedSlots, 0, count);
        lastContainerCopy = itemsAfter;
        refreshItems(finalChangedSlots);
    }

    public void withdrawLastAmount(int bankSlot) {
        withdrawItem(bankSlot, lastX);
    }

    public void refreshLastX() {
        if (lastX <= 0)
            lastX = 1;
        player.getPackets().sendConfig(111, lastX);
    }

    public void refreshTab(int slot) {
        refreshTab(slot, getTabSize(slot));
    }

    public void refreshTab(int slot, int size) {
        if (slot == 0)
            return;
        player.getPackets().sendConfigByFile(45143 + (slot - 1), size);
    }

    public void refreshTabs() {
        for (int slot = 1; slot < 15; slot++)
            refreshTab(slot);
        refreshTabIcons();
        refreshTabNames();
    }

    public void refreshViewingTab() {
        player.getPackets().sendConfigByFile(45141, currentTab + 1);
    }

    public void clearBank() {
        if (bankTabs != null) {
            for (int i = 1; i < bankTabs.length; i++)
                destroyTab(i);
            bankTabs[0] = new Item[0];
            refreshItems();
        }
    }

    public void removeItem(Item item) {
        if (bankTabs != null) {
            for (int i = 0; i < bankTabs.length; i++) {
                for (int i2 = 0; i2 < bankTabs[i].length; i2++) {
                    if (bankTabs[i][i2] != null && bankTabs[i][i2].getId() == item.getId() && bankTabs[i][i2].getCharges() == item.getCharges() && bankTabs[i][i2].getAttributes() == item.getAttributes()) {
                        this.removeItem(new int[] { i, i2 }, item.getAmount(), true, Bank.DESTROY_ITEM);
                        refreshItems();
                    }
                }
            }
        }
    }

    public boolean removeItem(int fakeSlot, int quantity, boolean refresh, int removeType) {
        return removeItem(getRealSlot(fakeSlot), quantity, refresh, removeType);
    }

    public static final int LEAVE_EMPTY_SLOT = 0, LEAVE_PLACE_HOLDER = 1, DESTROY_ITEM = 2;

    public boolean removeItem(int[] slot, int quantity, boolean refresh, int removeType) {
        if (slot == null)
            return false;
        Item item = bankTabs[slot[0]][slot[1]];
        boolean destroyed = false;
        if (quantity >= item.getAmount()) {
            if (removeType == LEAVE_PLACE_HOLDER)
                bankTabs[slot[0]][slot[1]] = new Item(item.getId(), 0, item.getCharges()).setAttributes(item.getAttributes());
            else if (removeType == LEAVE_EMPTY_SLOT)
                bankTabs[slot[0]][slot[1]] = null;
            else if (removeType == DESTROY_ITEM) {
                if (bankTabs[slot[0]].length == 1 && (bankTabs.length != 1)) {
                    if (slot[0] == 0) {
                        bankTabs[slot[0]] = new Item[0];
                    } else {
                        destroyTab(slot[0]);
                        destroyed = true;
                    }
                    if (refresh)
                        refreshTabs();
                } else {
                    Item[] tab = new Item[bankTabs[slot[0]].length - 1];
                    System.arraycopy(bankTabs[slot[0]], 0, tab, 0, slot[1]);
                    System.arraycopy(bankTabs[slot[0]], slot[1] + 1, tab, slot[1], bankTabs[slot[0]].length - slot[1] - 1);
                    bankTabs[slot[0]] = tab;
                    if (refresh)
                        refreshTab(slot[0]);
                }
            }
        } else
            bankTabs[slot[0]][slot[1]] = new Item(item.getId(), item.getAmount() - quantity, item.getCharges()).setAttributes(item.getAttributes());
        if (refresh) {
            refreshItems();
            refreshBankSize();
        }
        return destroyed;
    }

    public void sendBoxInterItems() {
        player.getPackets().sendInterSetItemsOptionsScript(11, 19, 93, 6, 5, "Deposit-1", "Deposit-5", "Deposit-10", "Deposit-All", "Deposit-X", "Examine");
        player.getPackets().sendUnlockIComponentOptionSlots(11, 19, 0, 27, 0, 1, 2, 3, 4, 5);
    }

    public void sendExamineBankItem(int fakeSlot) {
        int[] slot = getRealSlot(fakeSlot);
        if (slot == null)
            return;
        Item item = bankTabs[slot[0]][slot[1]];
        player.getPackets().sendInterfaceMessage(517, 184, 0, fakeSlot, item == null ? "When you deposit an item it will fill this slot." : ItemExaminesDataParser.getExamine(item));
        if (item != null)
            player.getPackets().sendGameMessage("Grand Exchange guide price: " + Utils.getFormattedNumber(GrandExchange.getPrice(item)) + ".");
    }

    public void sendExamineInteractionItem(int componentId, int slotId) {
        Item item = componentId == 27 ? player.getEquipment().getItem(slotId) : componentId == 14 ? player.getInventory().getItem(slotId) : player.getFamiliar() != null && player.getFamiliar().getBob() != null ? player.getFamiliar().getBob().getBeastItems().get(slotId) : null;
        if (item != null) {
            sendInteractionMessage(componentId, slotId, ItemExaminesDataParser.getExamine(item));
            if (item != null)
                player.getPackets().sendGameMessage("Grand Exchange guide price: " + Utils.getFormattedNumber(GrandExchange.getPrice(item)) + ".");
        }
    }

    public void sendInteractionMessage(int componentId, int slotId, String message) {
        player.getPackets().sendInterfaceMessage(517, componentId, message.contains("ff0000") ? 1 : 0, slotId, message);
    }

    public void sendItems() {
        player.getPackets().sendItems(95, getContainerCopy());
        refreshFirstEmptyFreeSlot();
    }

    public void setPlayer(Player player) {
        this.player = player;
        if (bankTabs == null || bankTabs.length == 0)
            bankTabs = new Item[1][0];
        if (tabsDetails == null) {
            tabsDetails = new TabDetails[bankTabs.length - 1];
            for (int i = 0; i < tabsDetails.length; i++)
                tabsDetails[i] = new TabDetails(i, 0, 0);
        }
        if (defaultInteractionAmount <= 0) {
            defaultInteractionAmount = 1;
            refreshDefaultInteractionAmount();
        }
        if (bankPresets == null)
            bankPresets = new BankPreset[10];
        if (bobPreset == null)
            bobPreset = new BobPreset();
    }

    public void switchInsertItems() {
        insertItems = !insertItems;
        player.getPackets().sendConfig(762, insertItems ? 1 : 0);
    }

    public void switchItem(int fromSlot, int toSlot, int fromComponentId, int toComponentId) {
        if (toComponentId == 151 || toComponentId == 147) {
            int toTab = toComponentId == 147 ? 0 : toSlot - 1;
            if (toTab < 0 || toTab > 15)
                return;
            if (bankTabs.length == toTab) {
                int[] fromRealSlot = getRealSlot(fromSlot);
                if (fromRealSlot == null)
                    return;
                Item item = getItem(fromRealSlot);
                if (item == null)
                    return;
                removeItem(fromSlot, item.getAmount(), false, Bank.DESTROY_ITEM);
                createTab();
                bankTabs[bankTabs.length - 1] = new Item[] { item };
                refreshTab(fromRealSlot[0]);
                refreshTab(toTab);
                refreshItems();
                refreshBankSize();
            } else if (bankTabs.length > toTab) {
                int[] fromRealSlot = getRealSlot(fromSlot);
                if (fromRealSlot == null)
                    return;
                if (toTab == fromRealSlot[0]) {
                    insertItem(fromSlot, toTab == 0 ? 0 : toTab + 1, fromComponentId, 187);
                    return;
                }
                Item item = getItem(fromRealSlot);
                if (item == null)
                    return;
                removeItem(fromSlot, item.getAmount(), true, Bank.DESTROY_ITEM);
                refreshTab(fromRealSlot[0]);
                addItem(item.getId(), item.getAmount(), item.getCharges(), item.getAttributes(), toTab, true);
                refreshBankSize();
            }
        }
    }

    public void switchItem(int fromSlot, int toSlot) {
        int[] fromRealSlot = getRealSlot(fromSlot);
        Item fromItem = getItem(fromRealSlot);
        if (fromItem == null)
            return;
        int[] toRealSlot = getRealSlot(toSlot);
        Item toItem = getItem(toRealSlot);
        bankTabs[fromRealSlot[0]][fromRealSlot[1]] = toItem;
        bankTabs[toRealSlot[0]][toRealSlot[1]] = fromItem;
        refreshTab(fromRealSlot[0]);
        if (fromRealSlot[0] != toRealSlot[0])
            refreshTab(toRealSlot[0]);
        refreshItems();
    }

    public void insertItem(int fromSlot, int toSlot, int fromComponentId, int toComponentId) {
        if (toComponentId == 187) {
            int toTabId = toSlot > 0 ? toSlot - 1 : toSlot;
            if (toTabId < 0 || toTabId > 14)
                return;
            int[] slot = getRealSlot(fromSlot);
            Item fromItem = getItem(slot);
            if (fromItem == null)
                return;
            if (toTabId == slot[0]) {
                if (slot[1] == bankTabs[toTabId].length - 1)
                    return;
                Item[] toTab = new Item[bankTabs[toTabId].length];
                System.arraycopy(bankTabs[slot[0]], 0, toTab, 0, slot[1]);
                for (int i = slot[1]; i < bankTabs[toTabId].length - 1; i++) {
                    toTab[i] = bankTabs[slot[0]][i + 1];
                }
                toTab[toTab.length - 1] = fromItem;
                bankTabs[toTabId] = toTab;
                refreshTab(toTabId);
                refreshItems();
                return;
            }
            Item[] toTab = new Item[bankTabs[toTabId].length + 1];
            System.arraycopy(bankTabs[toTabId], 0, toTab, 0, bankTabs[toTabId].length);
            toTab[toTab.length - 1] = fromItem;
            bankTabs[toTabId] = toTab;
            if (slot[0] != 0 && bankTabs[slot[0]].length == 1 && (bankTabs.length != 1)) {
                destroyTab(slot[0]);
                refreshTabs();
            } else {
                Item[] tab = new Item[bankTabs[slot[0]].length - 1];
                System.arraycopy(bankTabs[slot[0]], 0, tab, 0, slot[1]);
                System.arraycopy(bankTabs[slot[0]], slot[1] + 1, tab, slot[1], bankTabs[slot[0]].length - slot[1] - 1);
                bankTabs[slot[0]] = tab;
                refreshTab(slot[0]);
            }
            refreshTab(toTabId);
            refreshItems();
        } else {
            int[] slot = getRealSlot(fromSlot);
            int[] toRealSlot = getRealSlot(toSlot);
            Item fromItem = getItem(slot);
            if (fromItem == null)
                return;
            if (slot[0] == toRealSlot[0]) {
                if (toRealSlot[1] == slot[1])
                    return;
                Item[] toTab = new Item[bankTabs[toRealSlot[0]].length];
                if (slot[1] > toRealSlot[1]) {
                    System.arraycopy(bankTabs[toRealSlot[0]], 0, toTab, 0, toRealSlot[1]);
                    toTab[toRealSlot[1]] = fromItem;
                    for (int i = toRealSlot[1] + 1; i <= slot[1]; i++) {
                        toTab[i] = bankTabs[toRealSlot[0]][i - 1];
                    }
                    for (int i = slot[1] + 1; i < bankTabs[toRealSlot[0]].length; i++) {
                        toTab[i] = bankTabs[toRealSlot[0]][i];
                    }
                } else {
                    System.arraycopy(bankTabs[toRealSlot[0]], 0, toTab, 0, slot[1]);
                    for (int i = slot[1]; i < toRealSlot[1] - 1; i++) {
                        toTab[i] = bankTabs[toRealSlot[0]][i + 1];
                    }
                    toTab[toRealSlot[1] - 1] = fromItem;
                    for (int i = toRealSlot[1]; i < bankTabs[toRealSlot[0]].length; i++)
                        toTab[i] = bankTabs[toRealSlot[0]][i];
                }
                bankTabs[toRealSlot[0]] = toTab;
                refreshTab(toRealSlot[0]);
                refreshItems();
                return;
            }
            if (slot[0] != 0 && bankTabs[slot[0]].length == 1 && (bankTabs.length != 1)) {
                destroyTab(slot[0]);
                refreshTabs();
            } else {
                Item[] tab = new Item[bankTabs[slot[0]].length - 1];
                System.arraycopy(bankTabs[slot[0]], 0, tab, 0, slot[1]);
                System.arraycopy(bankTabs[slot[0]], slot[1] + 1, tab, slot[1], bankTabs[slot[0]].length - slot[1] - 1);
                bankTabs[slot[0]] = tab;
                refreshTab(slot[0]);
            }
            Item[] toTab = new Item[bankTabs[toRealSlot[0]].length + 1];
            System.arraycopy(bankTabs[toRealSlot[0]], 0, toTab, 0, toRealSlot[1]);
            System.arraycopy(bankTabs[toRealSlot[0]], toRealSlot[1], toTab, toRealSlot[1] + 1, bankTabs[toRealSlot[0]].length - toRealSlot[1]);
            toTab[toRealSlot[1]] = fromItem;
            bankTabs[toRealSlot[0]] = toTab;
            refreshTab(toRealSlot[0]);
            refreshItems();
        }
    }

    public void switchWithdrawNotes() {
        withdrawNotes = !withdrawNotes;
        refreshWithdrawNotes();
    }

    private void refreshWithdrawNotes() {
        player.getPackets().sendConfig(160, withdrawNotes ? 1 : 0);
        player.getPackets().sendExecuteScript(1487);
    }

    public void unlockButtons() {
        int interactionTabSize = Math.max(player.getInventory().getItems().getItems().length - 1, player.getFamiliar() != null ? (player.getFamiliar().getBOBSize() - 1) : 0);
        player.getPackets().sendIComponentSettings(517, 14, 0, interactionTabSize, 14682110);
        player.getPackets().sendUnlockIComponentOptionSlots(517, 27, 0, BodyDefinitions.getEquipmentContainerSize() - 1, 0, 6, 9);
        player.getPackets().sendUnlockIComponentOptionSlots(517, 33, 0, 32, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        player.getPackets().sendIComponentSettings(517, 184, 0, 1370, 11012094);
        player.getPackets().sendIComponentSettings(517, 199, 0, 1370, 2097152);
        player.getPackets().sendIComponentSettings(517, 207, 0, ClientScriptMap.getMap(15585).getSize(), 2);// custom tab icons

        player.getPackets().sendIComponentSettings(517, 151, 0, 15, 2097166);
        player.getPackets().sendIComponentSettings(517, 153, 0, 15, 2097152);
        player.getPackets().sendIComponentSettings(517, 152, 0, 15, 8388608);
        player.getPackets().sendIComponentSettings(517, 187, 0, 15, 2097152);
        player.getPackets().sendIComponentSettings(517, 188, 0, 15, 2097152);

        player.getPackets().sendIComponentSettings(517, 258, 0, 32, 2360322);
        player.getPackets().sendIComponentSettings(517, 268, 0, 19, 2098178);

        player.getPackets().sendIComponentSettings(517, 112, 0, 11, 14);
        player.getPackets().sendIComponentSettings(517, 245, 1, 11, 2359296);
        player.getPackets().sendIComponentSettings(517, 247, 1, 11, 14);
        player.getPackets().sendIComponentSettings(517, 251, 1, 11, 2);
        player.getPackets().sendIComponentSettings(517, 248, 1, 11, 2);
        player.getPackets().sendIComponentSettings(517, 249, 1, 11, 2);
        player.getPackets().sendIComponentSettings(517, 250, 1, 11, 2);
    }

    public boolean withdrawItemPlaceHolder(int bankSlot) {
        return withdrawItem(bankSlot, -1337);
    }

    public boolean withdrawDefaultAmount(int bankSlot) {
        if (currentFilter == 1) {
            Item item = getItem(getRealSlot(bankSlot));
            if (item == null)
                return false;
            if (item.getAmount() == 0) {
                removeItem(bankSlot, item.getAmount(), true, Bank.LEAVE_EMPTY_SLOT);
                return false;
            }
            refreshItems();
            player.getTemporaryAttributtes().put(Key.SKIP_BANK_SHIFT_ITEMS, Boolean.TRUE);
            sendConfirmationMessage(item.getId(), item.getAmount(), "Destroy Item", "Are you sure you want to destroy this item?", "Destroy", new Runnable() {

                @Override
                public void run() {
                    removeItem(bankSlot, item.getAmount(), true, Bank.DESTROY_ITEM);
                }
            });
            return false;
        }
        return withdrawItem(bankSlot, getDefaultInteractionAmount());
    }

    public boolean withdrawItem(int bankSlot, int quantity) {
        if (player.isNative950()) {
            if (interactionTab != 0 || leavePlaceHolders || quantity == -1337)
                return com.rs.game.player.client.Native950Banking.refuse(player, "That bank mode has not been enabled yet.");
            return com.rs.game.player.client.Native950Banking.withdraw(player, this, bankSlot, quantity);
        }
        if (interactionTab == 2 && wearBankItem(bankSlot, quantity))
            return false;
        if (interactionTab == 2) {
            player.getEquipment().refreshItemContainer();
            player.getAppearence().generateAppearenceData();
        }
        if (interactionTab == 1) {
            if (player.getFamiliar() == null || player.getFamiliar().getBob() == null) {
                refreshBob();
                refreshItems();
                refreshBankSize();
                return false;
            }
            player.getFamiliar().getBob().sendInterItems();
        }
        player.getInventory().refresh();
        boolean leavePlaceHolder = leavePlaceHolders || quantity == -1337;
        if (quantity == -1337)
            quantity = Integer.MAX_VALUE;
        if (quantity < 1)
            return false;
        Item item = getItem(getRealSlot(bankSlot));
        if (item == null)
            return false;
        if (item.getAmount() == 0) {
            removeItem(bankSlot, item.getAmount(), true, Bank.LEAVE_EMPTY_SLOT);
            return false;
        }
        if (item.getAmount() < quantity)
            item = new Item(item.getId(), item.getAmount(), item.getCharges()).setAttributes(item.getAttributes());
        else
            item = new Item(item.getId(), quantity, item.getCharges()).setAttributes(item.getAttributes());
        boolean noted = false;
        ItemDefinitions defs = item.getDefinitions();
        if (withdrawNotes) {
            if (!defs.isNoted() && defs.getCertId() != -1 && !(item.getId() > 14875 && item.getId() < 14893) && item.getAttributes() == null) {
                item.setId(defs.getCertId());
                noted = true;
            } else
                player.getPackets().sendMainInterfaceMessage(1, "You cannot withdraw this item as a note.", true);
        }
        if (interactionTab == 1) {
            if (!player.getFamiliar().getBob().canStoreItem(item, true)) {
                refreshItems();
                refreshBankSize();
                return false;
            }
        }
        ItemsContainer<Item> container = interactionTab == 1 ? player.getFamiliar().getBob().getBeastItems() : player.getInventory().getItems();
        if (container == null) {
            refreshItems();
            refreshBankSize();
            return false;
        }
        String containerName = interactionTab == 1 ? "Beast of Burden" : "Inventory";
        int amountInContainer = container.getNumberOf(item.getId());
        int withdrawAmount = item.getAmount();
        if (item.getId() == 995) {
            long coinsAmount = (long) player.getMoneyPouch().getTotal() + (long) amountInContainer;
            long maxCoinsAmount = (long) Integer.MAX_VALUE + (long) ((amountInContainer == 0 && player.getInventory().getFreeSlots() == 0) ? 0 : Integer.MAX_VALUE);
            if ((coinsAmount + (long) withdrawAmount) > maxCoinsAmount) {
                withdrawAmount = (int) (maxCoinsAmount - coinsAmount);
                player.getPackets().sendMainInterfaceMessage(1, "You can't hold anymore " + item.getName() + ".", true);
                if (withdrawAmount == 0)
                    return false;
            }
        } else {
            if ((noted || defs.isStackable()) && item.getAttributes() == null) {
                if (player.getInventory().getFreeSlots() == 0 && amountInContainer == 0) {
                    player.getPackets().sendMainInterfaceMessage(1, containerName + " full. To make more room, sell, drop or bank something.", true);
                    return false;
                }
                if (((long) amountInContainer + (long) withdrawAmount) > Integer.MAX_VALUE) {
                    withdrawAmount = Integer.MAX_VALUE - amountInContainer;
                    if (withdrawAmount > 0)
                        player.getPackets().sendMainInterfaceMessage(1, "You can't hold anymore " + item.getName() + ".", true);
                    if (withdrawAmount == 0) {
                        player.getPackets().sendMainInterfaceMessage(1, containerName + " full. To make more room, sell, drop or bank something.", true);
                        return false;
                    }
                }
            } else {
                int freeSlots = player.getInventory().getFreeSlots();
                if (freeSlots == 0) {
                    player.getPackets().sendMainInterfaceMessage(1, containerName + " full. To make more room, sell, drop or bank something.", true);
                    return false;
                }
                if (freeSlots < withdrawAmount) {
                    withdrawAmount = freeSlots;
                    player.getPackets().sendMainInterfaceMessage(1, containerName + " full. To make more room, sell, drop or bank something.", true);
                }
            }
        }
        if (withdrawAmount == 0)
            return false;
        removeItem(bankSlot, withdrawAmount, true, leavePlaceHolder ? Bank.LEAVE_PLACE_HOLDER : Bank.LEAVE_EMPTY_SLOT);
        player.gimBank.addHistory("withdrew", new Item(item.getId(), withdrawAmount, item.getCharges()).setAttributes(item.getAttributes()));
        if (item.getId() == 995) {
            int amountInPouch = player.getMoneyPouch().getTotal();
            int amountToPouch = ((long) amountInPouch + (long) withdrawAmount) > Integer.MAX_VALUE ? (Integer.MAX_VALUE - amountInPouch) : withdrawAmount;
            if (amountToPouch > 0)
                player.getMoneyPouch().addMoneyMisc(amountToPouch);
            withdrawAmount = withdrawAmount - amountToPouch;
        }
        if (withdrawAmount > 0)
            container.add(new Item(item.getId(), withdrawAmount, item.getCharges()).setAttributes(item.getAttributes()));
        if (interactionTab == 1)
            player.getFamiliar().getBob().sendInterItems();
        else
            player.getInventory().refresh();
        return true;
    }

    public boolean containsItem(Item t) {
        for (Bank bank : player.isNative950() ? java.util.Collections.singletonList(this) : player.getBanks()) {
            if (bank == null)
                continue;
            for (int i = 0; i < bank.bankTabs.length; i++) {
                for (Item item : bank.bankTabs[i]) {
                    if (item == null)
                        continue;
                    if (item.getId() == t.getId() && item.getAmount() >= t.getAmount() && item.getAttributes() == t.getAttributes() && item.getCharges() == t.getCharges())
                        return true;
                }
            }
        }
        return false;
    }

    public boolean containsItem(int itemId, int amount) {
        return containsItem(new Item(itemId, amount));
    }

    public boolean containsItemCurrentBank(int itemId, int amount) {
        for (int i = 0; i < bankTabs.length; i++) {
            for (Item item : bankTabs[i]) {
                if (item == null)
                    continue;
                if (item.getId() == itemId && item.getAmount() >= amount)
                    return true;
            }
        }
        return false;
    }

    public int getNumberOf(int itemId) {
        return getNumberOf(new Item(itemId, 1));
    }

    public int getNumberOf(Item t) {
        // Native saves own this active bank; the hydrated legacy multi-bank list is empty.
        if (player.isNative950() || player.getBanks() == null)
            for (int i = 0; i < bankTabs.length; i++) {
                for (Item item : bankTabs[i]) {
                    if (item == null)
                        continue;
                    if (item.exactMatch(t))
                        return item.getAmount();
                }
            }
        else
            for (Bank bank : player.getBanks()) {
                if (bank == null)
                    continue;
                for (int i = 0; i < bank.bankTabs.length; i++) {
                    for (Item item : bank.bankTabs[i]) {
                        if (item == null)
                            continue;
                        if (item.exactMatch(t))
                            return item.getAmount();
                    }
                }
            }
        return 0;
    }

    public void addCollectableItem(int collectableItem) {
        this.collectableItem = collectableItem;
    }

    public int getCollectableItem() {
        return collectableItem;
    }

    public boolean isInsertItems() {
        return insertItems;
    }

    public int getCurrentTab() {
        return currentTab;
    }

    public void setCurrentTab(int currentTab) {
        if (currentTab >= bankTabs.length || this.currentTab == currentTab)
            return;
        this.currentTab = currentTab;
        refreshViewingTab();
        shiftItems();
    }

    public boolean getWithdrawNotes() {
        return withdrawNotes;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        if (name == null)
            name = "Bank of Ataraxia";
        return name;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isFull() {
        return !hasBankSpace();
    }

    @Getter
    @Setter
    private transient boolean isInSearchMode;

    public void openSearchMode() {
//        isInSearchMode = true;
    }

    public void closeSearchMode() {
//        isInSearchMode = false;
//        player.getPackets().sendGlobalConfig(190, 1);
//        player.getPackets().sendConfigByFile(4893, 1);
//        player.getPackets().sendExecuteScript(1474);
    }

    private int defaultInteractionAmount = 1;// 1, 5, 10, all, x

    /** Native save bindings use the mode, preserving X independently of its value. */
    public int getNativeDefaultInteractionAmount() { return defaultInteractionAmount; }

    /** Packet-free hydration/control update; old saves retain the original defaults. */
    public void restoreNativePreferences(boolean notes, int savedX, int mode) {
        withdrawNotes = notes;
        lastX = savedX > 0 ? savedX : 1;
        defaultInteractionAmount = mode == 1 || mode == 5 || mode == 10 || mode == 11
                || mode == Integer.MAX_VALUE ? mode : 1;
    }

    public int getDefaultInteractionAmount() {
        if (defaultInteractionAmount <= 0)
            defaultInteractionAmount = 1;
        if (defaultInteractionAmount == 11) {
            return getLastX();
        }
        return defaultInteractionAmount;
    }

    public void setDefaultInteractionAmount(int defaultInteractionAmount) {
        this.defaultInteractionAmount = defaultInteractionAmount <= 0 ? 1 : defaultInteractionAmount;
        refreshDefaultInteractionAmount();
    }

    public void refreshDefaultInteractionAmount() {
        player.getPackets().sendConfigByFile(45189, defaultInteractionAmount == 1 ? 2 : defaultInteractionAmount == 5 ? 3 : defaultInteractionAmount == 10 ? 4 : defaultInteractionAmount == Integer.MAX_VALUE ? 7 : 5);
    }
    
    @Getter
    private boolean configPresetsTab;

    public void setConfigPresetsTab(boolean configPresetsTab) {
        this.configPresetsTab = configPresetsTab;
        refreshConfigPresetsTab();
    }

    public void refreshConfigPresetsTab() {
        player.getPackets().sendConfigByFile(45191, configPresetsTab ? 1 : 0);
    }

    private int currentFilter;

    public void setCurrentFilter(int currentFilter) {
        this.currentFilter = currentFilter;
        refreshCurrentFilter();
    }

    private void refreshCurrentFilter() {
        player.getPackets().sendConfigByFile(45140, currentFilter);
    }

    private boolean leavePlaceHolders;

    public void toggleLeavePlaceHolders() {
        leavePlaceHolders = !leavePlaceHolders;
        refreshLeavePlaceHolders();
    }

    public void refreshLeavePlaceHolders() {
        player.getPackets().sendConfigByFile(45190, leavePlaceHolders ? 1 : 0);
        player.getPackets().sendExecuteScript(8901);
    }

    public void removeAllPlaceHolders(boolean refreshItems) {
        for (int i = 0; i < bankTabs.length; i++) {
            for (Item item : bankTabs[i]) {
                if (item == null || item.getAmount() > 0)
                    continue;
                removeItem(getItemSlot(item), item.getAmount(), false, Bank.LEAVE_EMPTY_SLOT);
            }
        }
        if (refreshItems)
            refreshItems();
    }

    @Getter
    private int interactionTab;

    public void setInteractionTab(int interactionTab) {
        this.interactionTab = interactionTab;
        refreshInteractionTab();
    }

    public void refreshInteractionTab() {
        player.getPackets().sendConfigByFile(45139, interactionTab);
    }

    private boolean verticalTabs;

    public void toggleTabPosition() {
        verticalTabs = !verticalTabs;
        refreshTabPosition();
    }

    public void refreshTabPosition() {
        player.getPackets().sendConfigByFile(45192, verticalTabs ? 1 : 0);
    }

    public void init() {
        refreshLeavePlaceHolders();
        refreshTabPosition();
        refreshDefaultInteractionAmount();
        for (int i = 0; i < 5; i++)
            player.getVarsManager().sendVarBit(22153 + i, 1);// unlocks extra presets
        if (lastX <= 0)
            lastX = 1;
        refreshPresets();
        refreshLoadExactMatch();
    }

    public void ToggleEquipmentStats() {
        if (player.getInterfaceManager().getInterfaceParentId(1463) != -1) {
            player.getInterfaceManager().removeInterfaceByParent(517, 287);
            player.getPackets().sendHideIComponent(517, 286, true);
        } else {
            player.getInterfaceManager().setInterface(true, 517, 287, 1463);
            player.getPackets().sendIComponentText(1463, 21, "WORN EQUIPMENT STATS");
            player.getPackets().sendHideIComponent(517, 286, false);
        }
    }

    public boolean isEmpty(Item[][] items) {
        return getItemsLength(items) == 0;
    }

    public boolean isEmpty(Item[] items) {
        return getItemsLength(items) == 0;
    }

    public int getItemsLength(Item[][] items) {
        int count = 0;
        for (int i = 0; i < items.length; i++)
            count += getItemsLength(items[i]);
        return count;
    }

    public int getItemsLength(Item[] items) {
        int count = 0;
        for (Item item : items)
            if (item != null)
                count++;
        return count;
    }

    public boolean containsNullItems(Item[][] items) {
        for (int i = 0; i < items.length; i++)
            if (containsNullItems(items[i]))
                return true;
        return false;
    }

    public boolean containsNullItems(Item[] items) {
        for (Item item : items)
            if (item == null)
                return true;
        return false;
    }

    public int getNullItemSlot(Item[] items) {
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null)
                return i;
        }
        return -1;
    }

    public void shiftItems() {
        if (player.getTemporaryAttributtes().get(Key.SKIP_BANK_SHIFT_ITEMS) != null || !containsNullItems(bankTabs))
            return;
        Item[] itemsBefore = getContainerCopy();
        lastContainerCopy = null;
        List<Item[]> newTabs = new ArrayList<Item[]>();
        List<TabDetails> tabsList = new ArrayList<TabDetails>();
        int[] deletedTabs = new int[bankTabs.length];
        for (int i = 0; i < bankTabs.length; i++) {
            Item[] oldData = bankTabs[i];
            bankTabs[i] = new Item[getItemsLength(oldData)];
            int ptr = 0;
            for (int j = 0; j < oldData.length; j++) {
                if (oldData[j] != null)
                    bankTabs[i][ptr++] = oldData[j];
            }
            if (i == 0 || bankTabs[i].length >= 1) {
                newTabs.add(bankTabs[i]);
                if (i > 0)
                    tabsList.add(tabsDetails[i - 1]);
            } else
                deletedTabs[tabsDetails[i - 1].getOriginalIndex()] = 1;
        }
        for (TabDetails tab : tabsList) {
            int deletedCount = 0;
            for (int i = 0; i < tab.getOriginalIndex() + 1; i++)
                if (deletedTabs[i] == 1)
                    deletedCount++;
            tab.setOriginalIndex(tab.getOriginalIndex() - deletedCount);
        }
        bankTabs = newTabs.toArray(new Item[newTabs.size()][]);
        tabsDetails = tabsList.toArray(new TabDetails[tabsList.size()]);
        lastContainerCopy = generateContainer();
        refreshItems(lastContainerCopy, itemsBefore);
        refreshTabs();
        refreshBankSize();
    }

    private TabDetails[] tabsDetails;

    public void customiseTab(int tabIndex) {
        player.getTemporaryAttributtes().put(Key.BANK_CUSTOMISE_TAB_INDEX, tabIndex);
        player.getPackets().sendHideIComponent(517, 200, false);
    }

    public void closeCustomiseTab() {
        player.getTemporaryAttributtes().remove(Key.BANK_CUSTOMISE_TAB_INDEX);
        player.getPackets().sendHideIComponent(517, 200, true);
    }

    public void setTabIcon(int iconIndex) {
        Integer tabIndex = (Integer) player.getTemporaryAttributtes().get(Key.BANK_CUSTOMISE_TAB_INDEX);
        if (tabIndex == null || tabIndex >= tabsDetails.length || tabsDetails[tabIndex] == null)
            return;
        tabsDetails[tabIndex].setIconIndex(iconIndex);
        refreshTabIcon(tabIndex);
    }

    public void refreshTabIcons() {
        for (int i = 0; i < tabsDetails.length; i++)
            refreshTabIcon(i);
    }

    public void refreshTabIcon(int index) {
        player.getPackets().sendConfigByFile(45193 + index, tabsDetails[index].getIconIndex());
    }

    public void setTabName(int nameIndex) {
        Integer tabIndex = (Integer) player.getTemporaryAttributtes().get(Key.BANK_CUSTOMISE_TAB_INDEX);
        if (tabIndex == null || tabIndex >= tabsDetails.length || tabsDetails[tabIndex] == null)
            return;
        tabsDetails[tabIndex].setNameIndex(nameIndex);
        refreshTabName(tabIndex);
    }

    public void refreshTabNames() {
        for (int i = 0; i < tabsDetails.length; i++)
            refreshTabName(i);
    }

    public void refreshTabName(int index) {
        player.getPackets().sendConfigByFile(45207 + index, tabsDetails[index].getNameIndex());
    }

    public void switchTabIndexes(int fromSlot, int toSlot) {
        int fromTabId = fromSlot - 1;
        int toTabId = toSlot - 1;
        if (fromTabId >= bankTabs.length || fromTabId <= 0 || toTabId >= bankTabs.length || toTabId <= 0)
            return;
        Item[] fromTab = bankTabs[fromTabId];
        Item[] toTab = bankTabs[toTabId];
        TabDetails fromTabDetails = tabsDetails[fromTabId - 1];
        TabDetails toTabDetails = tabsDetails[toTabId - 1];
        bankTabs[fromTabId] = toTab;
        bankTabs[toTabId] = fromTab;
        tabsDetails[fromTabId - 1] = toTabDetails;
        tabsDetails[toTabId - 1] = fromTabDetails;
        refreshTabs();
        refreshItems();
    }

    public void insertTab(int fromSlot, int toSlot) {
        int fromTabId = fromSlot - 1;
        int toTabId = toSlot - 1;
        int fromTabDetailId = fromSlot - 2;
        int toTabDetailId = toSlot - 2;
        if (fromTabId >= bankTabs.length || fromTabId <= 0 || toTabId >= bankTabs.length || toTabId <= 0)
            return;
        if (fromTabId == toTabId)
            return;
        Item[] fromTab = bankTabs[fromTabId];
        Item[][] newBankTabs = new Item[bankTabs.length][0];
        if (fromTabId > toTabId) {
            System.arraycopy(bankTabs, 0, newBankTabs, 0, toTabId);
            newBankTabs[toTabId] = fromTab;
            for (int i = toTabId + 1; i <= fromTabId; i++) {
                newBankTabs[i] = bankTabs[i - 1];
            }
            for (int i = fromTabId + 1; i < bankTabs.length; i++) {
                newBankTabs[i] = bankTabs[i];
            }
        } else {
            System.arraycopy(bankTabs, 0, newBankTabs, 0, fromTabId);
            for (int i = fromTabId; i < toTabId - 1; i++) {
                newBankTabs[i] = bankTabs[i + 1];
            }
            newBankTabs[toTabId - 1] = fromTab;
            for (int i = toTabId; i < bankTabs.length; i++)
                newBankTabs[i] = bankTabs[i];
        }
        bankTabs = newBankTabs;
        TabDetails fromTabDetails = tabsDetails[fromTabDetailId];
        TabDetails[] newTabDetails = new TabDetails[tabsDetails.length];
        if (fromTabDetailId > toTabDetailId) {
            System.arraycopy(tabsDetails, 0, newTabDetails, 0, toTabDetailId);
            newTabDetails[toTabDetailId] = fromTabDetails;
            for (int i = toTabDetailId + 1; i <= fromTabDetailId; i++) {
                newTabDetails[i] = tabsDetails[i - 1];
            }
            for (int i = fromTabDetailId + 1; i < tabsDetails.length; i++) {
                newTabDetails[i] = tabsDetails[i];
            }
        } else {
            System.arraycopy(tabsDetails, 0, newTabDetails, 0, fromTabDetailId);
            for (int i = fromTabDetailId; i < toTabDetailId - 1; i++) {
                newTabDetails[i] = tabsDetails[i + 1];
            }
            newTabDetails[toTabDetailId - 1] = fromTabDetails;
            for (int i = toTabDetailId; i < tabsDetails.length; i++)
                newTabDetails[i] = tabsDetails[i];
        }
        tabsDetails = newTabDetails;
        if (currentTab == fromTabId)
            setCurrentTab(0);
        refreshTabs();
        refreshItems();
    }

    public void deleteTab(int slotId) {
        if (containsNullItems(bankTabs)) {
            shiftItems();
            player.getPackets().sendInterfaceMessage(517, 151, 1, slotId, "As you had empty slots in your bank this tab couldn't be removed. Please try again.");
            return;
        }
        sendConfirmDeleteTab(new Runnable() {

            @Override
            public void run() {
                collapse(slotId - 1);
            }
        });
    }

    public void resetTabsOrder() {
        sendConfirmResetTabOrder(new Runnable() {

            @Override
            public void run() {
                Item[] itemsBefore = getContainerCopy();
                Item[][] bankTabsCopy = new Item[bankTabs.length][0];
                TabDetails[] tabDetailsCopy = new TabDetails[tabsDetails.length];
                for (int i = 0; i < tabsDetails.length; i++) {
                    int originalIndex = tabsDetails[i].getOriginalIndex();
                    bankTabsCopy[originalIndex + 1] = bankTabs[i + 1];
                    tabDetailsCopy[originalIndex] = tabsDetails[i];
                }
                bankTabsCopy[0] = bankTabs[0];
                bankTabs = bankTabsCopy;
                tabsDetails = tabDetailsCopy;
                lastContainerCopy = generateContainer();
                refreshItems(lastContainerCopy, itemsBefore);
                refreshTabs();
                refreshBankSize();
            }
        });
    }

    public void sendConfirmDeleteTab(final Runnable onAccept) {
        sendConfirmationMessage(-1, -1, "Delete Tab", "Are you sure you wish to delete this tab?<br><br>All items in the tab will move to the untabbed section.", "Yes", onAccept);
    }

    public void sendConfirmResetTabOrder(final Runnable onAccept) {
        player.getTemporaryAttributtes().put(Key.SKIP_BANK_SHIFT_ITEMS, Boolean.TRUE);
        sendConfirmationMessage(-1, -1, "Reset Tab Order", "This will reset any re-ordered tabs back to their default order. Custom names and icons will not be changed.", "RESET", onAccept);
    }

    public void sendConfirmationMessage(final int itemId, final int amount, final String title, final String message, final String confirmOption, final Runnable onAccept) {
        if (player.gimBank.isOpen())
            return;
        player.getDialogueManager().startDialogue(new Dialogue() {

            @Override
            public void start() {
                player.getPackets().sendExecuteScript(3844, itemId, amount, title, message, confirmOption);
                player.getPackets().sendHideIComponent(517, 294, false);
            }

            @Override
            public void run(int interfaceId, int componentId) {
                if (componentId == 304)
                    onAccept.run();
                player.getInterfaceManager().removeBankInterface();
                player.getBank().openBank();
                player.getTemporaryAttributtes().remove(Key.SKIP_BANK_SHIFT_ITEMS);
            }

            @Override
            public void finish() {
                player.getTemporaryAttributtes().remove(Key.SKIP_BANK_SHIFT_ITEMS);
            }
        });
    }


    public static class TabDetails implements Serializable {

        private static final long serialVersionUID = 7175156452722832370L;
        @Getter
        @Setter
        private int originalIndex, nameIndex, iconIndex;

        public TabDetails(int originalIndex, int nameIndex, int iconIndex) {
            this.originalIndex = originalIndex;
            this.nameIndex = nameIndex;
            this.iconIndex = iconIndex;
        }
    }

    public abstract class DropDownMenuEvent {
        @Getter
        @Setter
        private int slotId;

        public abstract void run(Player player);
    }

    public void refreshFirstEmptyFreeSlot() {
        player.getPackets().sendConfig(8970, firstEmptyFreeSlot);
    }

    public void switchToInventory(int fromSlot, int toSlot) {
        int[] fromRealSlot = getRealSlot(fromSlot);
        Item bankItem = getItem(fromRealSlot);
        if (bankItem == null || bankItem.getAmount() == 0)
            return;
        ItemsContainer<Item> container = player.getInventory().getItems();
        if (toSlot < 0 || toSlot >= container.getSize())
            return;
        Item[] itemsBefore = container.getItemsCopy();
        Item withdrawItem = new Item(bankItem.getId(), bankItem.getAmount(), bankItem.getCharges()).setAttributes(bankItem.getAttributes());
        Item containerItem = container.get(toSlot);
        boolean noted = false;
        ItemDefinitions defs = withdrawItem.getDefinitions();
        if (withdrawNotes) {
            if (!defs.isNoted() && defs.getCertId() != -1 && !(withdrawItem.getId() > 14875 && withdrawItem.getId() < 14893) && withdrawItem.getAttributes() == null) {
                withdrawItem.setId(defs.getCertId());
                noted = true;
            } else
                player.getPackets().sendMainInterfaceMessage(1, "You cannot withdraw this item as a note.", true);
        }
        if (!withdrawItem.getDefinitions().isStackable() && !withdrawItem.getDefinitions().isNoted())
            withdrawItem.setAmount(1);
        int amountOfBankItemInContainer = container.getNumberOf(withdrawItem.getId());
        int withdrawAmount = withdrawItem.getAmount();
        if (withdrawItem.getId() == 995) {
            long coinsAmount = (long) player.getMoneyPouch().getTotal() + (long) amountOfBankItemInContainer;
            long maxCoinsAmount = (long) Integer.MAX_VALUE + (long) ((amountOfBankItemInContainer == 0 && container.getFreeSlots() == 0) ? 0 : Integer.MAX_VALUE);
            if ((coinsAmount + (long) withdrawAmount) > maxCoinsAmount) {
                withdrawAmount = (int) (maxCoinsAmount - coinsAmount);
                player.getPackets().sendMainInterfaceMessage(1, "You can't hold anymore " + withdrawItem.getName() + ".", true);
                if (withdrawAmount == 0)
                    return;
            }
        } else {
            if ((noted || defs.isStackable()) && withdrawItem.getAttributes() == null) {
                if (container.getFreeSlots() == 0 && amountOfBankItemInContainer == 0) {
                    player.getPackets().sendMainInterfaceMessage(1, "Inventory full. To make more room, sell, drop or bank something.", true);
                    return;
                }
                if (((long) amountOfBankItemInContainer + (long) withdrawAmount) > Integer.MAX_VALUE) {
                    withdrawAmount = Integer.MAX_VALUE - amountOfBankItemInContainer;
                    if (withdrawAmount > 0)
                        player.getPackets().sendMainInterfaceMessage(1, "You can't hold anymore " + withdrawItem.getName() + ".", true);
                    if (withdrawAmount == 0) {
                        player.getPackets().sendMainInterfaceMessage(1, "Inventory full. To make more room, sell, drop or bank something.", true);
                        return;
                    }
                }
            } else {
                int freeSlots = container.getFreeSlots();
                if (freeSlots == 0) {
                    player.getPackets().sendMainInterfaceMessage(1, "Inventory full. To make more room, sell, drop or bank something.", true);
                    return;
                }
                if (freeSlots < withdrawAmount) {
                    withdrawAmount = freeSlots;
                    player.getPackets().sendMainInterfaceMessage(1, "Inventory full. To make more room, sell, drop or bank something.", true);
                }
            }
        }
        if (withdrawAmount == 0)
            return;
        if (containerItem != null && !(amountOfBankItemInContainer > 0 && (withdrawItem.getDefinitions().isStackable() || withdrawItem.getDefinitions().isNoted()))) {
            Item depositItem = new Item(containerItem.getId(), containerItem.getAmount(), containerItem.getCharges()).setAttributes(containerItem.getAttributes());
            String cantBankMessage = ItemConstants.isBankAble(player, depositItem);
            if (cantBankMessage != null) {
                player.getPackets().sendMainInterfaceMessage(1, cantBankMessage, true);
                return;
            }
            ItemDefinitions defs2 = depositItem.getDefinitions();
            int originalId = depositItem.getId();
            if (defs2.isNoted() && defs2.getCertId() != -1)
                depositItem.setId(defs2.getCertId());
            Item bankedItem = getItemIncludingPlaceHolders(depositItem);
            if (bankedItem != null) {
                if (((long) bankedItem.getAmount() + (long) depositItem.getAmount()) > Integer.MAX_VALUE) {
                    player.getPackets().sendMainInterfaceMessage(1, "Not enough space in your bank.", true);
                    return;
                }
            } else if (!hasBankSpace()) {
                player.getPackets().sendMainInterfaceMessage(1, "Not enough space in your bank.", true);
                return;
            }
            if (depositItem.getAmount() != containerItem.getAmount()) {
                player.getPackets().sendMainInterfaceMessage(1, "Not enough space in your bank.", true);
                return;
            }
            if (depositItem.getAmount() > 0)
                container.remove(toSlot, new Item(originalId, depositItem.getAmount(), depositItem.getCharges()).setAttributes(depositItem.getAttributes()));
            removeItem(fromSlot, withdrawAmount, true, leavePlaceHolders ? Bank.LEAVE_PLACE_HOLDER : Bank.LEAVE_EMPTY_SLOT);
            if (withdrawItem.getId() == 995) {
                int amountInPouch = player.getMoneyPouch().getTotal();
                int amountToPouch = ((long) amountInPouch + (long) withdrawAmount) > Integer.MAX_VALUE ? (Integer.MAX_VALUE - amountInPouch) : withdrawAmount;
                if (amountToPouch > 0)
                    player.getMoneyPouch().addMoneyMisc(amountToPouch);
                withdrawAmount = withdrawAmount - amountToPouch;
            }
            player.gimBank.addHistory("withdrew", new Item(withdrawItem.getId(), withdrawAmount, withdrawItem.getCharges()).setAttributes(withdrawItem.getAttributes()));
            if (amountOfBankItemInContainer > 0 && (withdrawItem.getDefinitions().isStackable() || withdrawItem.getDefinitions().isNoted()))
                container.add(new Item(withdrawItem.getId(), withdrawAmount, withdrawItem.getCharges()).setAttributes(withdrawItem.getAttributes()));
            else
                container.set(toSlot, new Item(withdrawItem.getId(), withdrawAmount, withdrawItem.getCharges()).setAttributes(withdrawItem.getAttributes()));
            player.getInventory().refreshItems(itemsBefore);
            player.gimBank.addHistory("deposited", depositItem);
            bankedItem = getItemIncludingPlaceHolders(depositItem);
            if (bankedItem != null) {
                addItem(depositItem, true);
            } else {
                if (bankTabs[fromRealSlot[0]][fromRealSlot[1]] == null) {// switch items
                    bankTabs[fromRealSlot[0]][fromRealSlot[1]] = depositItem;
                } else {// insert inv item in next slot
                    int toTabId = fromRealSlot[0];
                    int toSlotId = fromRealSlot[1] + 1;
                    Item[] toTab = new Item[bankTabs[toTabId].length + 1];
                    System.arraycopy(bankTabs[toTabId], 0, toTab, 0, toSlotId);
                    System.arraycopy(bankTabs[toTabId], toSlotId, toTab, toSlotId + 1, bankTabs[toTabId].length - toSlotId);
                    toTab[toSlotId] = depositItem;
                    bankTabs[toTabId] = toTab;
                }
                refreshTab(fromRealSlot[0]);
                refreshItems();
                refreshBankSize();
            }
            return;
        }
        removeItem(fromSlot, withdrawAmount, true, leavePlaceHolders ? Bank.LEAVE_PLACE_HOLDER : Bank.LEAVE_EMPTY_SLOT);
        if (withdrawItem.getId() == 995) {
            int amountInPouch = player.getMoneyPouch().getTotal();
            int amountToPouch = ((long) amountInPouch + (long) withdrawAmount) > Integer.MAX_VALUE ? (Integer.MAX_VALUE - amountInPouch) : withdrawAmount;
            if (amountToPouch > 0)
                player.getMoneyPouch().addMoneyMisc(amountToPouch);
            withdrawAmount = withdrawAmount - amountToPouch;
        }
        player.gimBank.addHistory("withdrew", new Item(withdrawItem.getId(), withdrawAmount, withdrawItem.getCharges()).setAttributes(withdrawItem.getAttributes()));
        if (withdrawAmount > 0) {
            if (amountOfBankItemInContainer > 0 && (withdrawItem.getDefinitions().isStackable() || withdrawItem.getDefinitions().isNoted()))
                container.add(new Item(withdrawItem.getId(), withdrawAmount, withdrawItem.getCharges()).setAttributes(withdrawItem.getAttributes()));
            else
                container.set(toSlot, new Item(withdrawItem.getId(), withdrawAmount, withdrawItem.getCharges()).setAttributes(withdrawItem.getAttributes()));
        }
        player.getInventory().refreshItems(itemsBefore);
        if (interactionTab == 2) {
            player.getEquipment().refreshItemContainer();
            player.getAppearence().generateAppearenceData();
        }
    }

    public void switchToBank(int fromSlot, int toSlot, int toComponentId) {
        ItemsContainer<Item> container = player.getInventory().getItems();
        if (fromSlot >= container.getSize())
            return;
        Item containerItem = container.get(fromSlot);
        if (containerItem == null)
            return;
        Item[] itemsBefore = container.getItemsCopy();
        if (getItemIncludingPlaceHolders(containerItem) != null) {
            depositItem(fromSlot, containerItem.getAmount(), true);
            return;
        }
        Item depositItem = new Item(containerItem.getId(), containerItem.getAmount(), containerItem.getCharges()).setAttributes(containerItem.getAttributes());
        int[] toRealSlot = toComponentId != 184 ? null : getRealSlot(toSlot);
        Item bankItem = toComponentId != 184 ? null : getItem(toRealSlot);
        if (bankItem != null && bankItem.getAmount() == 0)
            return;
        String cantBankMessage = ItemConstants.isBankAble(player, depositItem);
        if (cantBankMessage != null) {
            player.getPackets().sendMainInterfaceMessage(1, cantBankMessage, true);
            return;
        }
        ItemDefinitions defs = depositItem.getDefinitions();
        int originalId = depositItem.getId();
        if (defs.isNoted() && defs.getCertId() != -1)
            depositItem.setId(defs.getCertId());
        if (bankItem != null && !hasBankSpace()) {
            player.getPackets().sendMainInterfaceMessage(1, "Not enough space in your bank.", true);
            return;
        }
        if (toComponentId != 184) {
            if (depositItem.getAmount() > 0)
                container.remove(fromSlot, new Item(originalId, depositItem.getAmount(), depositItem.getCharges()).setAttributes(depositItem.getAttributes()));
            if (toComponentId == 151 || toComponentId == 147) {
                int toTab = toComponentId == 147 ? 0 : toSlot - 1;
                if (toTab < 0 || toTab > 15)
                    return;
                if (bankTabs.length == toTab) {
                    createTab();
                    bankTabs[bankTabs.length - 1] = new Item[] { depositItem };
                    refreshTab(toTab);
                    refreshItems();
                    refreshBankSize();
                } else if (bankTabs.length > toTab) {
                    addItem(depositItem.getId(), depositItem.getAmount(), depositItem.getCharges(), depositItem.getAttributes(), toTab, true);
                    refreshBankSize();
                }
            } else if (toComponentId == 199) {
                int[] insertSlot = getRealSlot(toSlot);
                Item[] toTab = new Item[bankTabs[insertSlot[0]].length + 1];
                System.arraycopy(bankTabs[insertSlot[0]], 0, toTab, 0, insertSlot[1]);
                System.arraycopy(bankTabs[insertSlot[0]], insertSlot[1], toTab, insertSlot[1] + 1, bankTabs[insertSlot[0]].length - insertSlot[1]);
                toTab[insertSlot[1]] = depositItem;
                bankTabs[insertSlot[0]] = toTab;
                refreshTab(insertSlot[0]);
                refreshItems();
                refreshBankSize();
            } else {
                int toTabId = toComponentId == 187 ? (toSlot > 0 ? (toSlot - 1) : toSlot) : toSlot - 1;
                Item[] toTab = new Item[bankTabs[toTabId].length + 1];
                System.arraycopy(bankTabs[toTabId], 0, toTab, 0, bankTabs[toTabId].length);
                toTab[toTab.length - 1] = depositItem;
                bankTabs[toTabId] = toTab;
                refreshTab(toTabId);
                refreshItems();
                refreshBankSize();
            }
        } else {
            if (bankItem == null) {
                if (depositItem.getAmount() > 0)
                    container.remove(fromSlot, new Item(originalId, depositItem.getAmount(), depositItem.getCharges()).setAttributes(depositItem.getAttributes()));
                bankTabs[toRealSlot[0]][toRealSlot[1]] = depositItem;
                refreshTab(toRealSlot[0]);
                refreshItems();
                refreshBankSize();
            } else if (bankItem.getAmount() > 0) {
                Item item = new Item(bankItem.getId(), bankItem.getAmount(), bankItem.getCharges()).setAttributes(bankItem.getAttributes());
                boolean noted = false;
                ItemDefinitions defs2 = item.getDefinitions();
                if (withdrawNotes) {
                    if (!defs2.isNoted() && defs2.getCertId() != -1 && !(item.getId() > 14875 && item.getId() < 14893) && item.getAttributes() == null) {
                        item.setId(defs2.getCertId());
                        noted = true;
                    } else
                        player.getPackets().sendMainInterfaceMessage(1, "You cannot withdraw this item as a note.", true);
                }
                if (!item.getDefinitions().isStackable() && !item.getDefinitions().isNoted())
                    item.setAmount(1);
                int amountInInv = container.getNumberOf(item.getId());
                int withdrawAmount = item.getAmount();
                if (item.getId() == 995) {
                    long coinsAmount = (long) player.getMoneyPouch().getTotal() + (long) amountInInv;
                    long maxCoinsAmount = (long) Integer.MAX_VALUE + (long) ((amountInInv == 0 && container.getFreeSlots() == 0) ? 0 : Integer.MAX_VALUE);
                    if ((coinsAmount + (long) withdrawAmount) > maxCoinsAmount) {
                        withdrawAmount = (int) (maxCoinsAmount - coinsAmount);
                        player.getPackets().sendMainInterfaceMessage(1, "You can't hold anymore " + item.getName() + ".", true);
                        if (withdrawAmount == 0)
                            return;
                    }
                } else {
                    if ((noted || defs2.isStackable()) && item.getAttributes() == null) {
                        if (container.getFreeSlots() == 0 && amountInInv == 0) {
                            player.getPackets().sendMainInterfaceMessage(1, "Inventory full. To make more room, sell, drop or bank something.", true);
                            return;
                        }
                        if (((long) amountInInv + (long) withdrawAmount) > Integer.MAX_VALUE) {
                            withdrawAmount = Integer.MAX_VALUE - amountInInv;
                            if (withdrawAmount > 0)
                                player.getPackets().sendMainInterfaceMessage(1, "You can't hold anymore " + item.getName() + ".", true);
                            if (withdrawAmount == 0) {
                                player.getPackets().sendMainInterfaceMessage(1, "Inventory full. To make more room, sell, drop or bank something.", true);
                                return;
                            }
                        }
                    } else {
                        int freeSlots = container.getFreeSlots();
                        if (freeSlots == 0) {
                            player.getPackets().sendMainInterfaceMessage(1, "Inventory full. To make more room, sell, drop or bank something.", true);
                            return;
                        }
                        if (freeSlots < withdrawAmount) {
                            withdrawAmount = freeSlots;
                            player.getPackets().sendMainInterfaceMessage(1, "Inventory full. To make more room, sell, drop or bank something.", true);
                        }
                    }
                }
                if (withdrawAmount == 0)
                    return;
                if (depositItem.getAmount() > 0)
                    container.remove(fromSlot, new Item(originalId, depositItem.getAmount(), depositItem.getCharges()).setAttributes(depositItem.getAttributes()));
                removeItem(toSlot, withdrawAmount, true, leavePlaceHolders ? Bank.LEAVE_PLACE_HOLDER : Bank.LEAVE_EMPTY_SLOT);
                player.gimBank.addHistory("withdrew", new Item(item.getId(), withdrawAmount, item.getCharges()).setAttributes(item.getAttributes()));
                if (item.getId() == 995) {
                    int amountInPouch = player.getMoneyPouch().getTotal();
                    int amountToPouch = ((long) amountInPouch + (long) withdrawAmount) > Integer.MAX_VALUE ? (Integer.MAX_VALUE - amountInPouch) : withdrawAmount;
                    if (amountToPouch > 0)
                        player.getMoneyPouch().addMoneyMisc(amountToPouch);
                    withdrawAmount = withdrawAmount - amountToPouch;
                }
                if (withdrawAmount > 0) {
                    if (amountInInv > 0 && (item.getDefinitions().isStackable() || item.getDefinitions().isNoted()))
                        container.add(new Item(item.getId(), withdrawAmount, item.getCharges()).setAttributes(item.getAttributes()));
                    else
                        container.set(fromSlot, new Item(item.getId(), withdrawAmount, item.getCharges()).setAttributes(item.getAttributes()));
                }
                if (getItem(toRealSlot) != null) {// insert after
                    int toTabId = toRealSlot[0];
                    int toSlotId = toRealSlot[1] + 1;
                    Item[] toTab = new Item[bankTabs[toTabId].length + 1];
                    System.arraycopy(bankTabs[toTabId], 0, toTab, 0, toSlotId);
                    System.arraycopy(bankTabs[toTabId], toSlotId, toTab, toSlotId + 1, bankTabs[toTabId].length - toSlotId);
                    toTab[toSlotId] = depositItem;
                    bankTabs[toTabId] = toTab;
                } else
                    bankTabs[toRealSlot[0]][toRealSlot[1]] = depositItem;
                refreshTab(toRealSlot[0]);
                refreshItems();
                refreshBankSize();
            }
        }
        player.gimBank.addHistory("deposited", depositItem);
        player.getInventory().refreshItems(itemsBefore);
        if (interactionTab == 2) {
            player.getEquipment().refreshItemContainer();
            player.getAppearence().generateAppearenceData();
        }
    }

    public void doExtraBankAction(int slotId) {
        if (interactionTab == 2) {
            withdrawItem(slotId, 1);
            return;
        }
        int[] fromRealSlot = getRealSlot(slotId);
        Item item = getItem(fromRealSlot);
        if (item == null)
            return;
        if (item.getAmount() == 0)
            return;
        ItemDefinitions defs = item.getDefinitions();
        if (defs.isNoted() || defs.getExtraBankActionOption() == null)
            return;
        wearBankItem(slotId, 1);
    }

    public boolean wearBankItemPlaceHolder(int slotId) {
        return wearBankItem(slotId, -1337);
    }

    public boolean wearBankItem(int slotId, int quantity) {
        boolean leavePlaceHolder = leavePlaceHolders || quantity == -1337;
        if (quantity == -1337)
            quantity = Integer.MAX_VALUE;
        int[] fromRealSlot = getRealSlot(slotId);
        Item item = getItem(fromRealSlot);
        if (item == null)
            return false;
        if (item.getAmount() == 0) {
            removeItem(slotId, item.getAmount(), true, Bank.LEAVE_EMPTY_SLOT);
            player.getEquipment().refreshItemContainer();
            player.getAppearence().generateAppearenceData();
            return false;
        }
        ItemDefinitions defs = item.getDefinitions();
        if (defs.isNoted() || defs.getExtraBankActionOption() == null)
            return false;
        if (item.getAmount() < quantity)
            item = new Item(item.getId(), item.getAmount(), item.getCharges()).setAttributes(item.getAttributes());
        else
            item = new Item(item.getId(), quantity, item.getCharges()).setAttributes(item.getAttributes());
        if (!defs.isStackable() && item.getAmount() > 1)
            item.forceSetAmount(1);
        int equipSlot = defs.getEquipSlot();
        if (!player.getControlerManager().canEquip(equipSlot, item.getId())) {
            player.getEquipment().refreshItemContainer();
            player.getAppearence().generateAppearenceData();
            refreshItems();
            refreshBankSize();
            return false;
        }
        if (item != null && item.getDefinitions().getEquipSlot() == Equipment.SLOT_AURA) {
            player.getPackets().sendMainInterfaceMessage(1, "You can't wear this item, if its an aura drop it to unlock it in aura management interface.", true);
            player.getEquipment().refreshItemContainer();
            player.getAppearence().generateAppearenceData();
            refreshItems();
            refreshBankSize();
            return false;
        }
        if (!ItemConstants.canWear(item, player, true)) {
            player.getPackets().sendMainInterfaceMessage(1, "You don't have the requirements to equip this item.", true);
            player.getEquipment().refreshItemContainer();
            player.getAppearence().generateAppearenceData();
            refreshItems();
            refreshBankSize();
            return false;
        }
        HashMap<Integer, Integer> requiriments = item.getDefinitions().getWearingSkillRequiriments();
        boolean hasRequiriments = true;
        if (requiriments != null) {
            for (int skillId : requiriments.keySet()) {
                if (skillId >= Skills.SKILL_NAME.length || skillId < 0)
                    continue;
                int level = requiriments.get(skillId);
                if (level < 0 || level > 120)
                    continue;
                if (player.getSkills().getLevelForXp(skillId) < level) {
                    hasRequiriments = false;
                    break;
                }
            }
        }
        if (!hasRequiriments) {
            player.getPackets().sendMainInterfaceMessage(1, "You don't have the requirements to equip this item.", true);
            return false;
        }

        boolean twoHanded = defs.getEquipType() == 5;
        ItemsContainer<Item> container = player.getEquipment().getItems();
        int spacesNeeded = 0;
        int[] removeSlots = twoHanded ? new int[] { container.get(Equipment.SLOT_WEAPON) == null ? -1 : Equipment.SLOT_WEAPON, container.get(Equipment.SLOT_SHIELD) == null ? -1 : Equipment.SLOT_SHIELD } : equipSlot == Equipment.SLOT_SHIELD && container.get(Equipment.SLOT_WEAPON) != null && container.get(Equipment.SLOT_WEAPON).getDefinitions().getEquipType() == 5 ? new int[] { Equipment.SLOT_WEAPON } : new int[] { container.get(equipSlot) == null ? -1 : equipSlot };
        for (int slot : removeSlots) {
            if (slot == -1)
                continue;
            Item containerItem = container.get(slot);
            Item depositItem = new Item(containerItem.getId(), containerItem.getAmount(), containerItem.getCharges()).setAttributes(containerItem.getAttributes());
            String cantBankMessage = ItemConstants.isBankAble(player, depositItem);
            if (cantBankMessage != null) {
                player.getPackets().sendMainInterfaceMessage(1, cantBankMessage, true);
                return false;
            }
            if (getItemIncludingPlaceHolders(depositItem) == null)
                spacesNeeded++;
        }
        if (item.getAmount() >= getItemIncludingPlaceHolders(item).getAmount() && !leavePlaceHolder)
            spacesNeeded = spacesNeeded - 1 <= 0 ? 0 : spacesNeeded - 1;
        if (getRemainingSlots() < spacesNeeded) {
            player.getPackets().sendMainInterfaceMessage(1, "Not enough space in your bank.", true);
            return false;
        }
        Integer[] degradationData = item.getDefinitions().getItemDegradeData();
        if (!(item.getChargesData() != null || degradationData == null || degradationData.length != 2 || degradationData[1] != -1 || item.getDefinitions().getMaxCharges() <= 0)) {
            refreshItems();
            player.getEquipment().refreshItemContainer();
            player.getAppearence().generateAppearenceData();
            player.getTemporaryAttributtes().put(Key.SKIP_BANK_SHIFT_ITEMS, Boolean.TRUE);
            final Item newItem = item;
            sendConfirmationMessage(item.getId(), item.getAmount(), "Confirm Equip", "Equipping this armour will mean you can no longer trade it.<br>Are you sure you would like to?", "Yes", new Runnable() {

                @Override
                public void run() {
                    player.getChargesManagerNew().initChargesData(newItem);
                    sendWearItem(slotId, newItem, leavePlaceHolder, removeSlots);
                }
            });
            return true;
        }
        if (item.getId() == 37546 || item.getId() == 39648 || item.getId() == 39652) {
            Item originalItem = item;
            int orignalId = originalItem.getId();
            int augmentedId = defs.getAugmentedItemId();
            Item toGive = new Item(originalItem.getId());
            toGive.setChargesData(originalItem.getChargesData());
            toGive.setId(augmentedId);
            if (player.getInventionManager().getDivineCharges() <= 0 && !toGive.getDefinitions().usesChargesInside())
                toGive.setId(toGive.getDefinitions().getUnchargedItemId());
            toGive.setInventionData(new InventionData(0));
            toGive.getInventionData().setOriginalItemId(orignalId);
            if (toGive.getChargesData() != null)
                toGive.setChargesData(null);
            item = toGive;
        }
        sendWearItem(slotId, item, leavePlaceHolder, removeSlots);
        return true;
    }

    public void sendWearItem(int slotId, Item item, boolean leavePlaceHolder, int[] removeSlots) {
        int[] fromRealSlot = getRealSlot(slotId);
        int equipSlot = item.getDefinitions().getEquipSlot();
        ItemsContainer<Item> container = player.getEquipment().getItems();
        removeItem(slotId, item.getAmount(), true, leavePlaceHolder ? Bank.LEAVE_PLACE_HOLDER : Bank.LEAVE_EMPTY_SLOT);
        player.gimBank.addHistory("withdrew", new Item(item.getId(), item.getAmount(), item.getCharges()).setAttributes(item.getAttributes()));
        int count = 1;
        for (int slot : removeSlots) {
            if (slot == -1)
                continue;
            Item containerItem = container.get(slot);
            Item depositItem = new Item(containerItem.getId(), containerItem.getAmount(), containerItem.getCharges()).setAttributes(containerItem.getAttributes());
            if (item.getId() == depositItem.getId() && item.getDefinitions().isStackable())
                continue;
            container.set(slot, null);
            player.gimBank.addHistory("deposited", depositItem);
            Item bankedItem = getItemIncludingPlaceHolders(depositItem);
            if (bankedItem != null) {
                addItem(depositItem, false);
            } else {
                if (bankTabs[fromRealSlot[0]][fromRealSlot[1]] == null) {// switch items
                    bankTabs[fromRealSlot[0]][fromRealSlot[1]] = depositItem;
                } else {// insert inv item in next slot
                    int toTabId = fromRealSlot[0];
                    int toSlotId = fromRealSlot[1] + count;
                    Item[] toTab = new Item[bankTabs[toTabId].length + 1];
                    System.arraycopy(bankTabs[toTabId], 0, toTab, 0, toSlotId);
                    System.arraycopy(bankTabs[toTabId], toSlotId, toTab, toSlotId + 1, bankTabs[toTabId].length - toSlotId);
                    toTab[toSlotId] = depositItem;
                    bankTabs[toTabId] = toTab;
                    count++;
                }
            }
        }
        if (container.get(equipSlot) != null)
            container.get(equipSlot).setAmount(container.get(equipSlot).getAmount() + item.getAmount());
        else
            container.set(equipSlot, new Item(item.getId(), item.getAmount(), item.getCharges()).setAttributes(item.getAttributes()));
        player.getEquipment().refreshItemContainer();
        player.getAppearence().generateAppearenceData();
        refreshTab(fromRealSlot[0]);
        refreshItems();
        refreshBankSize();
        player.getPackets().sendSound(item.getDefinitions().getCSOpcode(118), 0, 1);
        if (player.getHitpoints() > (player.getMaxHitpoints() * 1.15)) {
            player.setHitpoints(player.getMaxHitpoints());
            player.refreshHitPoints();
        }
        if (equipSlot == Equipment.SLOT_WEAPON && item.getId() != 15486) {
            if (player.getPolDelay() > Utils.currentTimeMillis()) {
                player.setPolDelay(0);
                player.sendMessage("The power of the light fades. Your resistance to melee attacks return to normal.");
            }
        }
        if (equipSlot == Equipment.SLOT_WEAPON)
            player.resetDecimationEffect();
    }

    private BankPreset[] bankPresets;
    private BobPreset bobPreset;
    @Getter
    private transient int selectedPreset;
    @Getter
    private boolean loadExactMatch;

    public void openBankPresetsTab() {
        resetErrorIndexes();
        refreshSelectedPreset();
    }

    public void toggleLoadExactMatch() {
        loadExactMatch = !loadExactMatch;
        refreshLoadExactMatch();
    }

    private void refreshLoadExactMatch() {
        player.getPackets().sendConfigByFile(45228, loadExactMatch ? 1 : 0);
    }

    public void refreshPresets() {
        for (int i = 0; i < bankPresets.length; i++) {
            BankPreset preset = i >= bankPresets.length ? null : bankPresets[i];
            player.getPackets().sendConfigByFile(22189 + i, preset == null ? 0 : preset.getNameSlot());
            player.getPackets().sendConfigByFile(22158 + (i * 2), preset == null ? 0 : !preset.isIncludeInventoryItems() ? 1 : 0);
            player.getPackets().sendConfigByFile(22159 + (i * 2), preset == null ? 0 : !preset.isIncludeEquipmentItems() ? 1 : 0);
            player.getPackets().sendConfigByFile(26177 + i, preset == null ? 0 : preset.isIncludeBobPreset() ? 1 : 0);
            player.getPackets().sendItems(706 + (i * 2), preset == null ? new Item[28] : preset.getInventoryItems());
            player.getPackets().sendItems(707 + (i * 2), preset == null ? new Item[BodyDefinitions.getEquipmentContainerSize()] : preset.getEquipmentItems());
        }
        player.getPackets().sendConfigByFile(26187, bobPreset.isRemembersItems() ? 1 : 0);
        player.getPackets().sendItems(741, bobPreset.getItems());
    }

    public void setSelectedPreset(int selectedPreset) {
        if (this.selectedPreset != selectedPreset)
            resetErrorIndexes();
        this.selectedPreset = selectedPreset;
        refreshSelectedPreset();
    }

    public void refreshSelectedPreset() {
        player.getPackets().sendConfigByFile(22179, selectedPreset + 1);
        player.getPackets().sendHideIComponent(517, 279, selectedPreset == 10);
        if (selectedPreset == 10) {
            player.getPackets().sendConfigByFile(26188, bobPreset.isRemembersItems() ? 1 : 0);
            return;
        }
        BankPreset preset = selectedPreset >= bankPresets.length ? null : bankPresets[selectedPreset];
        player.getPackets().sendConfig(7751, preset == null ? 0 : preset.getNameSlot());
        player.getPackets().sendConfigByFile(22186, preset == null ? 0 : !preset.isIncludeInventoryItems() ? 1 : 0);
        player.getPackets().sendConfigByFile(22187, preset == null ? 0 : !preset.isIncludeEquipmentItems() ? 1 : 0);
        player.getPackets().sendConfigByFile(26188, preset == null ? 0 : preset.isIncludeBobPreset() ? 1 : 0);
    }

    public void togglePresetIncludeInventoryItems() {
        togglePresetIncludeInventoryItems(-1);
    }

    public void togglePresetIncludeInventoryItems(int slotId) {
        int index = slotId != -1 ? slotId - 1 : selectedPreset;
        if (index >= 10)
            return;
        BankPreset preset = index >= bankPresets.length ? null : bankPresets[index];
        if (preset == null)
            preset = bankPresets[index] = new BankPreset();
        preset.setIncludeInventoryItems(!preset.isIncludeInventoryItems());
        refreshPresets();
        refreshSelectedPreset();
    }

    public void togglePresetIncludeEquipmentItems() {
        togglePresetIncludeEquipmentItems(-1);
    }

    public void togglePresetIncludeEquipmentItems(int slotId) {
        int index = slotId != -1 ? slotId - 1 : selectedPreset;
        if (index >= 10)
            return;
        BankPreset preset = index >= bankPresets.length ? null : bankPresets[index];
        if (preset == null)
            preset = bankPresets[index] = new BankPreset();
        preset.setIncludeEquipmentItems(!preset.isIncludeEquipmentItems());
        refreshPresets();
        refreshSelectedPreset();
    }

    public void togglePresetFamiliar() {
        togglePresetFamiliar(-1);
    }

    public void togglePresetFamiliar(int slotId) {
        int index = slotId != -1 ? slotId - 1 : selectedPreset;
        if (index == 10) {
            BobPreset preset = bobPreset;
            preset.setRemembersItems(!preset.remembersItems);
            refreshPresets();
            refreshSelectedPreset();
            return;
        }
        BankPreset preset = index >= bankPresets.length ? null : bankPresets[index];
        if (preset == null)
            preset = bankPresets[index] = new BankPreset();
        preset.setIncludeBobPreset(!preset.isIncludeBobPreset());
        refreshPresets();
        refreshSelectedPreset();
    }

    public void setPresetName(int nameSlot) {
        if (selectedPreset == 10)
            return;
        BankPreset preset = selectedPreset >= bankPresets.length ? null : bankPresets[selectedPreset];
        if (preset == null)
            preset = bankPresets[selectedPreset] = new BankPreset();
        preset.setNameSlot(nameSlot);
        refreshPresets();
        refreshSelectedPreset();
    }

    public void overwritePreset() {
        if (selectedPreset == 10) {
            overwriteBoBPreset();
            refreshPresets();
            return;
        }
        BankPreset preset = selectedPreset >= bankPresets.length ? null : bankPresets[selectedPreset];
        if (preset == null)
            preset = bankPresets[selectedPreset] = new BankPreset();
        if (!preset.isIncludeInventoryItems())
            preset.setInventoryItems(new Item[28]);
        else {
            Item[] items = new Item[28];
            for (int i = 0; i < player.getInventory().getItems().getItems().length; i++) {
                Item item = player.getInventory().getItems().getItems()[i];
                if (item == null)
                    continue;
                items[i] = new Item(item.getId(), item.getAmount(), item.getCharges()).setAttributes(item.getAttributes());
                player.getPackets().sendGameMessage("Item added to " + preset.getName(selectedPreset + 1) + " (Backpack): " + item.getName(), true);
            }
            preset.setInventoryItems(items);
        }
        if (!preset.isIncludeEquipmentItems())
            preset.setEquipmentItems(new Item[BodyDefinitions.getEquipmentContainerSize()]);
        else {
            Item[] items = new Item[BodyDefinitions.getEquipmentContainerSize()];
            for (int i = 0; i < player.getEquipment().getItems().getItems().length; i++) {
                Item item = player.getEquipment().getItems().getItems()[i];
                if (item == null || i == Equipment.SLOT_AURA)
                    continue;
                items[i] = new Item(item.getId(), item.getAmount(), item.getCharges()).setAttributes(item.getAttributes());
                player.getPackets().sendGameMessage("Item added to " + preset.getName(selectedPreset + 1) + " (Equipment): " + item.getName(), true);
            }
            preset.setEquipmentItems(items);
        }
        if (preset.isIncludeBobPreset())
            overwriteBoBPreset();
        else
            player.getPackets().sendMainInterfaceMessage(0, "Your preset has been saved.", true);
        refreshPresets();
    }

    public void overwriteBoBPreset() {
        BobPreset preset = bobPreset;
        preset.setPouchId(player.getFamiliar() == null || player.getFamiliar().getPouch() == null || player.getFamiliar().getBob() == null || player.getFamiliar().getBob().canDepositOnly() ? 0 : player.getFamiliar().getPouch().getPouchId());
        if (preset.getPouchId() == 0 || player.getFamiliar() == null || player.getFamiliar().getPouch() == null || player.getFamiliar().getBob() == null || player.getFamiliar().getBob().canDepositOnly())
            preset.setItems(new Item[32]);
        else {
            Item[] items = new Item[32];
            for (int i = 0; i < player.getFamiliar().getBob().getBeastItems().getItems().length; i++) {
                Item item = player.getFamiliar().getBob().getBeastItems().getItems()[i];
                if (item == null)
                    continue;
                items[i] = new Item(item.getId(), item.getAmount(), item.getCharges()).setAttributes(item.getAttributes());
                player.getPackets().sendGameMessage("Item added to Beast of Burden Preset: " + item.getName(), true);
            }
            preset.setItems(items);
        }
        player.getPackets().sendMainInterfaceMessage(0, "Your preset has been saved.", true);
    }

    public static final int LOAD_ALL = 3, LOAD_INV_ONLY = 0, LOAD_EQUPMENT_ONLY = 1, LOAD_BOB_ONLY = 2;

    public void loadPreset(int type) {
        loadPreset(-1, type);
    }

    public void loadPreset(int index, int type) {
        boolean quick = index != -1;
        index = index == -1 ? selectedPreset : index;
        if (index == 10) {
            if (type != LOAD_ALL && type != LOAD_BOB_ONLY)
                return;
            boolean[] errorIndexes = loadBobPreset(true);
            if (errorIndexes == null)
                return;
            if (quick)
                player.closeInterfaces();
            else {
                int v = 0;
                for (int i = 0; i < errorIndexes.length; i++)
                    v |= (errorIndexes[i] ? 1 : 0) << i;
                player.getPackets().sendConfig(7753, v);
            }
            refreshBankSize();
            refreshItems();
            return;
        }
        BankPreset preset = index >= bankPresets.length ? null : bankPresets[index];
        if (preset == null)
            preset = bankPresets[index] = new BankPreset();
        if (((type == LOAD_ALL && preset.isIncludeInventoryItems()) || type == LOAD_INV_ONLY)) {
            if (!canDepositItemsToBank(player.getInventory().getItems().getItems())) {
                player.getPackets().sendMainInterfaceMessage(1, "You can't load a preset whilst you have some objects that you cannot bank with you.", true);
                return;
            }
        }
        if (((type == LOAD_ALL && preset.isIncludeEquipmentItems()) || type == LOAD_EQUPMENT_ONLY)) {
            if (!canDepositItemsToBank(player.getEquipment().getItems().getItems(), true)) {
                player.getPackets().sendMainInterfaceMessage(1, "You can't load a preset whilst you have some objects that you cannot bank with you.", true);
                return;
            }
        }
        player.getPackets().sendGameMessage("Your preset is being withdrawn. Please see below for any issues whilst trying to load it...", true);
        if (((type == LOAD_ALL && preset.isIncludeInventoryItems()) || type == LOAD_INV_ONLY)) {
            boolean[] errorIndexes = new boolean[28];
            for (int i = 0; i < player.getInventory().getItems().getItems().length; i++) {
                Item item = player.getInventory().getItems().getItems()[i];
                if (item != null) {
                    addItem(item, false);
                    player.getInventory().getItems().set(i, null);
                }
            }
            for (int i = 0; i < preset.getInventoryItems().length; i++) {
                Item item = preset.getInventoryItems()[i];
                if (item == null)
                    continue;
                int[] slot = loadExactMatch ? getItemSlot(item) : getItemSlotCheckIdOnly(item.getId());
                if (slot == null) {
                    errorIndexes[i] = true;
                    player.getPackets().sendGameMessage("<col=ff0000>Item could not be found: " + item.getName(), true);
                    continue;
                }
                Item bankedItem = getItem(slot);
                if (bankedItem.getAmount() == 0) {
                    errorIndexes[i] = true;
                    player.getPackets().sendGameMessage("<col=ff0000>Item could not be found: " + item.getName(), true);
                    continue;
                }
                if (item.getAmount() > bankedItem.getAmount()) {
                    errorIndexes[i] = true;
                    player.getPackets().sendGameMessage("<col=9b870c>Item could not be found in the quantity required: " + item.getName(), true);
                }
                int withdrawAmount = item.getAmount() > bankedItem.getAmount() ? bankedItem.getAmount() : item.getAmount();
                removeItem(slot, withdrawAmount, false, Bank.LEAVE_PLACE_HOLDER);
                player.getInventory().getItems().set(i, new Item(bankedItem.getId(), withdrawAmount, bankedItem.getCharges()).setAttributes(bankedItem.getAttributes()));
            }
            player.getInventory().refresh();
            int v = 0;
            for (int i = 0; i < errorIndexes.length; i++)
                v |= (errorIndexes[i] ? 1 : 0) << i;
            player.getPackets().sendConfig(7752, v);
            if (type == LOAD_INV_ONLY) {
                refreshBankSize();
                refreshItems();
                return;
            }
        }
        if (((type == LOAD_ALL && preset.isIncludeEquipmentItems()) || type == LOAD_EQUPMENT_ONLY)) {
            for (int i = 0; i < player.getEquipment().getItems().getItems().length; i++) {
                Item item = player.getEquipment().getItems().getItems()[i];
                if (item != null && item.getDefinitions().getEquipSlot() != Equipment.SLOT_AURA) {
                    addItem(item, false);
                    player.getEquipment().getItems().set(i, null);
                }
            }
            for (int i = 0; i < preset.getEquipmentItems().length; i++) {
                Item item = preset.getEquipmentItems()[i];
                if (item == null)
                    continue;
                int[] slot = loadExactMatch ? getItemSlot(item) : getItemSlotCheckIdOnly(item.getId());
                if (slot == null) {
                    player.getPackets().sendGameMessage("<col=ff0000>Item could not be found: " + item.getName(), true);
                    continue;
                }
                Item bankedItem = getItem(slot);
                if (bankedItem.getAmount() == 0) {
                    player.getPackets().sendGameMessage("<col=ff0000>Item could not be found: " + item.getName(), true);
                    continue;
                }
                if (item.getAmount() > bankedItem.getAmount()) {
                    player.getPackets().sendGameMessage("<col=9b870c>Item could not be found in the quantity required: " + item.getName(), true);
                }
                int withdrawAmount = item.getAmount() > bankedItem.getAmount() ? bankedItem.getAmount() : item.getAmount();
                removeItem(slot, withdrawAmount, false, Bank.LEAVE_PLACE_HOLDER);
                player.getEquipment().getItems().set(i, new Item(bankedItem.getId(), withdrawAmount, bankedItem.getCharges()).setAttributes(bankedItem.getAttributes()));
            }
            player.getEquipment().refreshItemContainer();
            player.getAppearence().generateAppearenceData();
            if (type == LOAD_EQUPMENT_ONLY) {
                refreshBankSize();
                refreshItems();
                return;
            }
        }
        if (type == LOAD_ALL && preset.isIncludeBobPreset())
            loadBobPreset(false);
        if (quick)
            player.closeInterfaces();
        refreshBankSize();
        refreshItems();
    }

    public void resetErrorIndexes() {
        player.getPackets().sendConfig(7752, 0);
        player.getPackets().sendConfig(7753, 0);
    }

    public boolean[] loadBobPreset(boolean sendStartItemsMessage) {
        BobPreset preset = bobPreset;
        if (preset.getPouchId() == 0) {
            player.getPackets().sendMainInterfaceMessage(1, "You don't have a beast of burden preset.", true);
            return null;
        }
        if (player.getFamiliar() != null && player.getFamiliar().getPouch().getPouchId() != preset.getPouchId()) {
            player.getPackets().sendMainInterfaceMessage(1, "You can't load beast of burden preset while having a different active familiar.", true);
            return null;
        }
        if (player.getFamiliar() != null && player.getFamiliar().getBob() != null && !canDepositItemsToBank(player.getFamiliar().getBob().getBeastItems().getItems())) {
            player.getPackets().sendMainInterfaceMessage(1, "You can't load beast of burden preset whilst it has items that cannot be banked.", true);
            return null;
        }
        Item pouch = getItem(preset.getPouchId());
        if (pouch == null && player.getFamiliar() == null) {
            player.getPackets().sendGameMessage("<col=ff0000>Item could not be found: " + ItemDefinitions.getItemDefinitions(preset.getPouchId()).getName(), true);
            return null;
        }
        if (pouch != null && player.getFamiliar() == null) {
            removeItem(getItemSlot(pouch), 1, false, Bank.LEAVE_PLACE_HOLDER);
            Pouches pouches = Pouches.forId(preset.getPouchId());
            Summoning.spawnFamiliar(player, pouches);
        }
        if (player.getFamiliar() == null)
            return null;
        boolean[] errorIndexes = new boolean[32];
        if (sendStartItemsMessage)
            player.getPackets().sendGameMessage("Your preset is being withdrawn. Please see below for any issues whilst trying to load it...", true);
        for (int i = 0; i < player.getFamiliar().getBob().getBeastItems().getItems().length; i++) {
            Item item = player.getFamiliar().getBob().getBeastItems().getItems()[i];
            if (item != null) {
                addItem(item, false);
                player.getFamiliar().getBob().getBeastItems().set(i, null);
            }
        }
        for (int i = 0; i < preset.getItems().length; i++) {
            Item item = preset.getItems()[i];
            if (item == null)
                continue;
            int[] slot = loadExactMatch ? getItemSlot(item) : getItemSlotCheckIdOnly(item.getId());
            if (slot == null) {
                errorIndexes[i] = true;
                player.getPackets().sendGameMessage("<col=ff0000>Item could not be found: " + item.getName(), true);
                continue;
            }
            Item bankedItem = getItem(slot);
            if (bankedItem.getAmount() == 0) {
                errorIndexes[i] = true;
                player.getPackets().sendGameMessage("<col=ff0000>Item could not be found: " + item.getName(), true);
                continue;
            }
            if (item.getAmount() > bankedItem.getAmount()) {
                errorIndexes[i] = true;
                player.getPackets().sendGameMessage("<col=9b870c>Item could not be found in the quantity required: " + item.getName(), true);
            }
            int withdrawAmount = item.getAmount() > bankedItem.getAmount() ? bankedItem.getAmount() : item.getAmount();
            removeItem(slot, withdrawAmount, false, Bank.LEAVE_PLACE_HOLDER);
            player.getFamiliar().getBob().getBeastItems().set(i, new Item(bankedItem.getId(), withdrawAmount, bankedItem.getCharges()).setAttributes(bankedItem.getAttributes()));
        }
        player.getFamiliar().getBob().sendInterItems();
        return errorIndexes;
    }

    public boolean canDepositItemsToBank(Item[] items) {
        return canDepositItemsToBank(items, false);
    }

    public boolean canDepositItemsToBank(Item[] items, boolean skipAuras) {
        for (Item item : items) {
            if (item == null || (skipAuras && item.getDefinitions().getEquipSlot() == Equipment.SLOT_AURA))
                continue;
            if (ItemConstants.isBankAble(player, item) != null)
                return false;
            Item bankedItem = getItemIncludingPlaceHolders(item);
            if (bankedItem == null && !hasBankSpace())
                return false;
            if (bankedItem != null) {
                if (((long) item.getAmount() + (long) bankedItem.getAmount()) > Integer.MAX_VALUE)
                    return false;
            }
        }
        return true;
    }

    public void handlePresetItemOptions(int componentId, int slotId, int packetId) {
        if (selectedPreset == 10) {
            BobPreset bobpreset = bobPreset;
            Item item = slotId < 0 || slotId >= bobpreset.getItems().length ? null : bobpreset.getItems()[slotId];
            if (item == null)
                return;
            if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
                bobpreset.getItems()[slotId] = null;
                refreshPresets();
                refreshSelectedPreset();
            } else {
                player.getPackets().sendGameMessage(ItemExaminesDataParser.getExamine(item));
            }
            return;
        }
        BankPreset preset = bankPresets[selectedPreset];
        if (preset == null)
            preset = bankPresets[selectedPreset] = new BankPreset();
        Item[] items = componentId == 258 ? preset.getInventoryItems() : preset.getEquipmentItems();
        Item item = slotId < 0 || slotId >= items.length ? null : items[slotId];
        if (item == null)
            return;
        if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
            items[slotId] = null;
            refreshPresets();
            refreshSelectedPreset();
        } else {
            player.getPackets().sendGameMessage(ItemExaminesDataParser.getExamine(item));
        }
    }

    public class BankPreset implements Serializable {

        private static final long serialVersionUID = -3798011943616908919L;

        private int nameSlot;
        private Item[] inventoryItems;
        private Item[] equipmentItems;
        private boolean includeInventoryItems;
        private boolean includeEquipmentItems;
        private boolean includeBobPreset;

        public BankPreset() {
            inventoryItems = new Item[28];
            equipmentItems = new Item[BodyDefinitions.getEquipmentContainerSize()];
            nameSlot = 0;
            includeInventoryItems = true;
            includeEquipmentItems = true;
            includeBobPreset = false;
        }

        public int getNameSlot() {
            return nameSlot;
        }

        public void setNameSlot(int nameSlot) {
            this.nameSlot = nameSlot;
        }

        public Item[] getInventoryItems() {
            return inventoryItems;
        }

        public void setInventoryItems(Item[] inventoryItems) {
            this.inventoryItems = inventoryItems;
        }

        public Item[] getEquipmentItems() {
            return equipmentItems;
        }

        public void setEquipmentItems(Item[] equipmentItems) {
            this.equipmentItems = equipmentItems;
        }

        public String getName(int id) {
            return nameSlot <= 0 ? "Preset " + id : ClientScriptMap.getMap(8657).getStringValue(nameSlot);
        }

        public boolean hasInventoryItems() {
            if (inventoryItems == null)
                return false;
            for (Item item : inventoryItems)
                if (item != null)
                    return true;
            return false;
        }

        public boolean hasEquipmentItems() {
            if (equipmentItems == null)
                return false;
            for (Item item : equipmentItems)
                if (item != null)
                    return true;
            return false;
        }

        public boolean isIncludeInventoryItems() {
            return includeInventoryItems;
        }

        public void setIncludeInventoryItems(boolean includeInventoryItems) {
            this.includeInventoryItems = includeInventoryItems;
        }

        public boolean isIncludeEquipmentItems() {
            return includeEquipmentItems;
        }

        public void setIncludeEquipmentItems(boolean includeEquipmentItems) {
            this.includeEquipmentItems = includeEquipmentItems;
        }

        public boolean isIncludeBobPreset() {
            return includeBobPreset;
        }

        public void setIncludeBobPreset(boolean includeBobPreset) {
            this.includeBobPreset = includeBobPreset;
        }

    }

    public class BobPreset implements Serializable {

        private static final long serialVersionUID = -5405392443234852530L;

        private int pouchId;
        private Item[] items;
        @Getter
        @Setter
        private boolean remembersItems;

        public BobPreset() {
            items = new Item[32];
            pouchId = 0;
            remembersItems = true;
        }

        public int getPouchId() {
            return pouchId;
        }

        public void setPouchId(int pouchId) {
            this.pouchId = pouchId;
        }

        public Item[] getItems() {
            return items;
        }

        public void setItems(Item[] items) {
            this.items = items;
        }

        public boolean hasItems() {
            if (items == null)
                return false;
            for (Item item : items)
                if (item != null)
                    return true;
            return false;
        }

    }

}
