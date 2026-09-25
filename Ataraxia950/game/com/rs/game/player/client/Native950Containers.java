package com.rs.game.player.client;

import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.player.Bank;
import com.rs.game.player.Player;
import java.util.Arrays;
import java.util.Objects;

/**
 * Modern container operations on Ataraxia's actual Inventory and Bank storage.
 * This deliberately avoids the old UI-emitting methods and unverified 910 item
 * definitions. Only ordinary items present in the selected modern catalog are
 * admitted; charges, notes, presets and additional tabs need their own port.
 */
public final class Native950Containers {
    public static final int INVENTORY_SIZE = 28;
    public static final int EQUIPMENT_SIZE = 19;
    private static final int[] EQUIPMENT_KIT = {1277, 1173, 1139};
    private final ItemsContainer<Item> inventory;
    private final ItemsContainer<Item> equipment;
    private final Bank bank;
    private final Player player;
    private final Native950ItemCatalog catalog;
    private final Thread owner;
    private boolean equipmentKitClaimed;

    public Native950Containers(Player player, Native950ItemCatalog catalog) {
        if (!player.isNative950()) throw new IllegalArgumentException("Native character required");
        this.player=player;
        this.inventory = player.getInventory().items;
        this.equipment = player.getEquipment().getItems();
        this.bank = player.getBank();
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.owner = Thread.currentThread();
        validateState();
    }

    /** Development supplies for a newly created, temporary local character. */
    public void seedStarterItems() {
        checkOwner();
        if (!inventory.isEmpty() || !equipment.isEmpty() || bank.bankTabs[0].length != 0) throw new IllegalStateException("Starter items require an empty character");
        requireType(995); requireType(1511); requireType(315);
        inventory.set(0, new Item(995, 1000));
        for (int slot = 1; slot <= 5; slot++) inventory.set(slot, new Item(1511, 1));
        for (int slot = 6; slot <= 10; slot++) inventory.set(slot, new Item(315, 1));
        validateState();
    }

    /** Restore only into a new character, after validating every item before commit. */
    public void restore(Native950Save save) {
        checkOwner(); validateState();
        if (!inventory.isEmpty() || !equipment.isEmpty() || equipmentKitClaimed || bank.bankTabs[0].length != 0)
            throw new IllegalStateException("Restore requires an empty character");
        Item[] nextInventory = restoredItems(save.inventoryIds(), save.inventoryAmounts(), true);
        Item[] nextBank = restoredItems(save.bankIds(), save.bankAmounts(), false);
        Item[] nextEquipment = restoredItems(save.equipmentIds(), save.equipmentAmounts(), true);
        for (int slot = 0; slot < nextEquipment.length; slot++) {
            Item item = nextEquipment[slot];
            if (item != null && (requireType(item.getId()).equipSlot != slot || (!requireType(item.getId()).stackable && item.getAmount() != 1)))
                throw new IllegalArgumentException("Saved equipment does not match its verified wear slot");
        }
        bank.bankTabs = new Item[][] {nextBank};
        for (int slot = 0; slot < nextInventory.length; slot++) inventory.set(slot, nextInventory[slot]);
        for (int slot = 0; slot < nextEquipment.length; slot++) equipment.set(slot, nextEquipment[slot]);
        equipmentKitClaimed = save.equipmentKitClaimed();
    }

    public Native950Save saveSnapshot(String username, int x, int y, int plane) {
        checkOwner(); validateState();
        Snapshot carried = inventorySnapshot();
        Snapshot stored = new Snapshot(bank.bankTabs[0], bank.bankTabs[0].length);
        Snapshot worn = equipmentSnapshot();
        return new Native950Save(username, x, y, plane, carried.ids, carried.amounts, stored.ids, stored.amounts,
                worn.ids, worn.amounts, equipmentKitClaimed);
    }

    /** The local banker grants this development kit once per saved character. */
    public Result claimEquipmentKit() {
        checkOwner(); validateState();
        if (equipmentKitClaimed) return Result.CLAIMED;
        for (int id : EQUIPMENT_KIT) {
            Native950ItemCatalog.Entry type = requireType(id);
            if (type.stackable || type.equipSlot < 0) throw new IllegalStateException("Unverified equipment kit item");
        }
        if (emptyCount(inventory.getItems()) < EQUIPMENT_KIT.length) return Result.KIT_FULL;
        Item[] nextInventory = copy(inventory.getItems());
        for (int id : EQUIPMENT_KIT) nextInventory[firstEmpty(nextInventory)] = new Item(id, 1);
        for (int slot = 0; slot < nextInventory.length; slot++) inventory.set(slot, nextInventory[slot]);
        equipmentKitClaimed = true;
        return Result.moved(EQUIPMENT_KIT.length);
    }

    /** Atomic inventory/equipment transaction, with cache conflict slots supplied by the wear service. */
    public Result equip(int inventorySlot, int expectedId) {
        EquipmentChange change = prepareEquip(inventorySlot, expectedId, new int[0]);
        return change.commit();
    }

    public Result unequip(int equipmentSlot, int expectedId) {
        return prepareUnequip(equipmentSlot, expectedId).commit();
    }

    /** Exact equipped ammunition debit, using the same staged identity checks as wearing. */
    boolean consumeEquipment(int slot,int expectedId,int amount) {
        checkOwner();validateState();
        if(slot<0||slot>=EQUIPMENT_SIZE||amount<1)return false;
        EquipmentChange change=new EquipmentChange();Item item=change.nextEquipment[slot];
        if(item==null||item.getId()!=expectedId||item.getAmount()<amount)return false;
        change.nextEquipment[slot]=item.getAmount()==amount?null:new Item(expectedId,item.getAmount()-amount);
        change.result=Result.moved(amount);return change.commit().moved==amount;
    }

    EquipmentChange prepareEquip(int inventorySlot, int expectedId, int[] conflicts) {
        checkOwner(); validateState();
        EquipmentChange change = new EquipmentChange();
        if (inventorySlot < 0 || inventorySlot >= INVENTORY_SIZE) return change.fail(Result.INVALID);
        Item source = inventory.get(inventorySlot);
        if (source == null || source.getId() != expectedId) return change.fail(Result.STALE);
        Native950ItemCatalog.Entry type = requireType(expectedId);
        if (type.equipSlot < 0) return change.fail(Result.NOT_EQUIPMENT);
        int destination = type.equipSlot;
        change.nextInventory[inventorySlot] = null;
        Item previous = change.nextEquipment[destination];
        int amount = source.getAmount();
        if (type.stackable && previous != null && previous.getId() == expectedId) {
            long combined = (long)amount + previous.getAmount();
            if (combined > Integer.MAX_VALUE) return change.fail(Result.FULL);
            amount = (int)combined;
        } else if (previous != null && !returnEquipment(change.nextInventory, previous, inventorySlot)) {
            return change.fail(Result.FULL);
        }
        change.nextEquipment[destination] = null;
        for (int slot : conflicts) {
            if (slot < 0 || slot >= EQUIPMENT_SIZE) return change.fail(Result.INVALID);
            if (slot == destination) continue;
            Item conflict = change.nextEquipment[slot];
            if (conflict != null && !returnEquipment(change.nextInventory, conflict, inventorySlot))
                return change.fail(Result.FULL);
            change.nextEquipment[slot] = null;
        }
        change.nextEquipment[destination] = new Item(expectedId, amount);
        change.result = Result.moved(source.getAmount());
        return change;
    }

    EquipmentChange prepareUnequip(int slot, int expectedId) {
        checkOwner(); validateState();
        EquipmentChange change = new EquipmentChange();
        if (slot < 0 || slot >= EQUIPMENT_SIZE) return change.fail(Result.INVALID);
        Item source = equipment.get(slot);
        if (source == null || source.getId() != expectedId) return change.fail(Result.STALE);
        if (!returnEquipment(change.nextInventory, source, -1)) return change.fail(Result.FULL);
        change.nextEquipment[slot] = null;
        change.result = Result.moved(source.getAmount());
        return change;
    }

    private boolean returnEquipment(Item[] carried, Item item, int preferred) {
        Native950ItemCatalog.Entry type = requireType(item.getId());
        int target = type.stackable ? indexOf(carried, item.getId()) : -1;
        if (target >= 0) {
            long combined = (long)carried[target].getAmount() + item.getAmount();
            if (combined > Integer.MAX_VALUE) return false;
            carried[target] = new Item(item.getId(), (int)combined);
            return true;
        }
        target = preferred >= 0 && carried[preferred] == null ? preferred : firstEmpty(carried);
        if (target < 0) return false;
        carried[target] = new Item(item.getId(), item.getAmount());
        return true;
    }

    /** Snapshot identity and quantity as well as value, so controller callbacks cannot race a staged exchange. */
    final class EquipmentChange {
        final Item[] originalInventory = inventory.getItems().clone(), originalEquipment = equipment.getItems().clone();
        final Item[] beforeInventory = copy(originalInventory), beforeEquipment = copy(originalEquipment);
        final Item[] nextInventory = copy(originalInventory), nextEquipment = copy(originalEquipment);
        Result result = Result.INVALID;
        private boolean committed;
        EquipmentChange fail(Result reason) { result = reason; return this; }
        Result commit() {
            checkOwner();
            if (committed) return Result.STALE;
            if (result.moved == 0) return result;
            if (!unchanged(inventory.getItems(), originalInventory, beforeInventory)
                    || !unchanged(equipment.getItems(), originalEquipment, beforeEquipment)) return Result.STALE;
            validateState();
            for (int slot = 0; slot < INVENTORY_SIZE; slot++) inventory.set(slot, nextInventory[slot]);
            for (int slot = 0; slot < EQUIPMENT_SIZE; slot++) equipment.set(slot, nextEquipment[slot]);
            committed = true;
            player.getNative950ActionBar().preferences.equipmentChanged(player,false);
            return result;
        }
    }

    private static boolean unchanged(Item[] current, Item[] identities, Item[] values) {
        for (int slot = 0; slot < current.length; slot++) {
            if (current[slot] != identities[slot]) return false;
            if (current[slot] != null && (current[slot].getId() != values[slot].getId()
                    || current[slot].getAmount() != values[slot].getAmount())) return false;
        }
        return true;
    }

    private Item[] restoredItems(int[] ids, int[] amounts, boolean backpack) {
        Item[] result = new Item[ids.length];
        java.util.Set<Integer> stacks = new java.util.HashSet<>();
        for (int slot = 0; slot < ids.length; slot++) {
            if (ids[slot] < 0) continue;
            Native950ItemCatalog.Entry type = requireType(ids[slot]);
            if (backpack && !type.stackable && amounts[slot] != 1)
                throw new IllegalArgumentException("Saved non-stackable item occupies more than one item per slot");
            if (backpack && type.stackable && !stacks.add(ids[slot]))
                throw new IllegalArgumentException("Saved stackable item appears in multiple backpack slots");
            result[slot] = new Item(ids[slot], amounts[slot]);
        }
        return result;
    }

    public Result deposit(int slot, int expectedId, int requested) {
        return deposit(slot, expectedId, requested, expectedId);
    }

    /** A validated certificate may deposit into its current-cache base item. */
    public Result deposit(int slot, int expectedId, int requested, int bankedId) {
        checkOwner(); validateState();
        requireType(bankedId);
        if (slot < 0 || slot >= INVENTORY_SIZE || requested <= 0) return Result.INVALID;
        Item source = inventory.get(slot);
        if (source == null || source.getId() != expectedId) return Result.STALE;
        Item[] nextBank = copy(bank.bankTabs[0]);
        int target = indexOf(nextBank, bankedId);
        if (target < 0 && nextBank.length == Bank.MAX_BANK_SIZE) return Result.FULL;
        int current = target < 0 ? 0 : nextBank[target].getAmount();
        int capacity = (int) Math.min((long) requested, (long) Integer.MAX_VALUE - current);
        if (capacity == 0) return Result.FULL;
        Item[] nextInventory = copy(inventory.getItems());
        // A quantity option applies to all copies of this item, including ordinary
        // non-stackable items distributed over separate backpack slots. Validate
        // the clicked slot first and consume it before the other matching slots.
        int moved = removeFromSlot(nextInventory, slot, capacity);
        for (int i = 0; i < nextInventory.length && moved < capacity; i++) {
            if (i != slot && nextInventory[i] != null && nextInventory[i].getId() == expectedId)
                moved += removeFromSlot(nextInventory, i, capacity - moved);
        }
        if (moved == 0) return Result.FULL;
        if (target < 0) { target = nextBank.length; nextBank = Arrays.copyOf(nextBank, target + 1); }
        nextBank[target] = new Item(bankedId, current + moved);
        // Every precondition and arithmetic operation is resolved before commit.
        bank.bankTabs = new Item[][] {nextBank};
        for (int i = 0; i < nextInventory.length; i++) inventory.set(i, nextInventory[i]);
        return Result.moved(moved);
    }

    /** Atomic developer grant; existing bank entries and their saved state are preserved. */
    Result receiveDeveloperBankItem(int id,int amount){
        checkOwner();validateState();requireType(id);
        if(amount<1)return Result.INVALID;
        Item[] next=copy(bank.bankTabs[0]);int at=indexOf(next,id);
        if(at<0&&next.length>=Bank.MAX_BANK_SIZE)return Result.FULL;
        int current=at<0?0:next[at].getAmount();
        if((long)current+amount>Integer.MAX_VALUE)return Result.FULL;
        if(at<0){at=next.length;next=Arrays.copyOf(next,at+1);next[at]=new Item(id,amount);}
        else next[at].setAmount(current+amount);
        bank.bankTabs=new Item[][]{next};return Result.moved(amount);
    }
    /** Exact read-only capacity preview used for native actor prediction and commit. */
    public int withdrawableAmount(int slot, int expectedId, int requested) {
        return withdrawableAmount(slot, expectedId, requested, expectedId);
    }

    /** Caller validates the current-cache certificate link; capacity belongs to the output identity. */
    public int withdrawableAmount(int slot, int expectedId, int requested, int withdrawnId) {
        checkOwner(); validateState();
        if (slot < 0 || slot >= bank.bankTabs[0].length || requested <= 0) return 0;
        Item source = bank.bankTabs[0][slot];
        if (source.getId() != expectedId) return 0;
        Native950ItemCatalog.Entry type = requireType(withdrawnId);
        int moved = Math.min(requested, source.getAmount());
        Item[] items = inventory.getItems();
        if (!type.stackable) return Math.min(moved, emptyCount(items));
        int target = indexOf(items, withdrawnId);
        if (target < 0) return firstEmpty(items) < 0 ? 0 : moved;
        return (int) Math.min((long) moved, (long) Integer.MAX_VALUE - items[target].getAmount());
    }

    public Result withdraw(int slot, int expectedId, int requested) {
        return withdraw(slot, expectedId, requested, expectedId);
    }

    /** Remove source bank units and publish the same quantity under a verified output identity atomically. */
    public Result withdraw(int slot, int expectedId, int requested, int withdrawnId) {
        int moved = withdrawableAmount(slot, expectedId, requested, withdrawnId);
        if (moved == 0) {
            if (slot < 0 || slot >= bank.bankTabs[0].length || requested <= 0) return Result.INVALID;
            if (bank.bankTabs[0][slot].getId() != expectedId) return Result.STALE;
            return Result.FULL;
        }
        Item source = bank.bankTabs[0][slot];
        Native950ItemCatalog.Entry type = requireType(withdrawnId);
        Item[] nextInventory = copy(inventory.getItems());
        if (type.stackable) {
            int target = indexOf(nextInventory, withdrawnId);
            if (target < 0) target = firstEmpty(nextInventory);
            int current = nextInventory[target] == null ? 0 : nextInventory[target].getAmount();
            nextInventory[target] = new Item(withdrawnId, current + moved);
        } else {
            for (int i = 0; i < moved; i++) nextInventory[firstEmpty(nextInventory)] = new Item(withdrawnId, 1);
        }
        Item[] nextBank = copy(bank.bankTabs[0]);
        if (moved == source.getAmount()) {
            Item[] compact = new Item[nextBank.length - 1];
            System.arraycopy(nextBank, 0, compact, 0, slot);
            System.arraycopy(nextBank, slot + 1, compact, slot, nextBank.length - slot - 1);
            nextBank = compact;
        } else nextBank[slot] = new Item(expectedId, source.getAmount() - moved);
        bank.bankTabs = new Item[][] {nextBank};
        for (int i = 0; i < nextInventory.length; i++) inventory.set(i, nextInventory[i]);
        return Result.moved(moved);
    }

    EquipmentBankChange prepareEquipmentDeposit() {
        checkOwner(); validateState(); return new EquipmentBankChange();
    }

    /** Eligible worn slots commit together; refused slots retain their identities and entire quantities. */
    final class EquipmentBankChange {
        final Item[] originalInventory = inventory.getItems().clone(), originalEquipment = equipment.getItems().clone();
        final Item[] originalBank = bank.bankTabs[0].clone();
        final Item[] beforeInventory = copy(originalInventory), beforeEquipment = copy(originalEquipment), beforeBank = copy(originalBank);
        final Item[] nextEquipment = originalEquipment.clone();
        Item[] nextBank = copy(originalBank);
        int movedSlots;
        private boolean committed;
        Result include(int slot) {
            checkOwner();
            if (committed || slot < 0 || slot >= EQUIPMENT_SIZE) return Result.INVALID;
            Item source = beforeEquipment[slot];
            if (source == null || nextEquipment[slot] == null) return Result.STALE;
            requireType(source.getId());
            int target = indexOf(nextBank, source.getId());
            if (target < 0 && nextBank.length >= Bank.MAX_BANK_SIZE) return Result.FULL;
            int existing = target < 0 ? 0 : nextBank[target].getAmount();
            long quantity = (long)existing + source.getAmount();
            if (quantity > Integer.MAX_VALUE) return Result.FULL;
            if (target < 0) { target = nextBank.length; nextBank = Arrays.copyOf(nextBank, target + 1); }
            nextBank[target] = new Item(source.getId(), (int)quantity);
            nextEquipment[slot] = null; movedSlots++;
            return Result.moved(source.getAmount());
        }
        Result commit() {
            checkOwner();
            if (committed) return Result.STALE;
            if (movedSlots == 0) return Result.FULL;
            if (bank.bankTabs.length != 1 || bank.bankTabs[0].length != originalBank.length
                    || !unchanged(inventory.getItems(),originalInventory,beforeInventory)
                    || !unchanged(equipment.getItems(),originalEquipment,beforeEquipment)
                    || !unchanged(bank.bankTabs[0],originalBank,beforeBank)) return Result.STALE;
            validateState();
            bank.bankTabs = new Item[][] {nextBank};
            for(int slot=0;slot<EQUIPMENT_SIZE;slot++) equipment.set(slot,nextEquipment[slot]);
            committed=true;return Result.moved(movedSlots);
        }
    }
    public int depositAll() {
        checkOwner(); validateState();
        Item[] before = copy(inventory.getItems());
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            Item item = inventory.get(slot);
            if (item != null) deposit(slot, item.getId(), Integer.MAX_VALUE);
        }
        int moved = 0;
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            Item after = inventory.get(slot);
            if (before[slot] != null && (after == null || after.getAmount() != before[slot].getAmount())) moved++;
        }
        return moved; // count of changed slots, avoiding a summed quantity overflow
    }

    /** Developer-only exchange: stage all displaced property before granting any generated item. */
    DeveloperLoadoutChange prepareDeveloperLoadout(Item[] carried,Item[] worn) {
        checkOwner();validateState();return new DeveloperLoadoutChange(carried,worn);
    }
    final class DeveloperLoadoutChange {
        final Item[][] originalTabs=bank.bankTabs;
        final Item[] originalInventory=inventory.getItems().clone(),originalEquipment=equipment.getItems().clone(),originalBank=bank.bankTabs[0].clone();
        final Item[] beforeInventory=copy(originalInventory),beforeEquipment=copy(originalEquipment),beforeBank=copy(originalBank);
        final Item[] nextInventory,nextEquipment;
        Item[] nextBank=copy(originalBank);
        Result result=Result.moved(1);
        boolean committed;
        DeveloperLoadoutChange(Item[] carried,Item[] worn){
            if(carried.length!=INVENTORY_SIZE||worn.length!=EQUIPMENT_SIZE)throw new IllegalArgumentException("Loadout shape");
            nextInventory=copy(carried);nextEquipment=copy(worn);
            for(int i=0;i<carried.length;i++)if(carried[i]!=null){
                validateItem(carried[i],"loadout inventory",i);
                if(!requireType(carried[i].getId()).stackable&&carried[i].getAmount()!=1)throw new IllegalArgumentException("Nonstackable loadout supply");
            }
            for(int i=0;i<worn.length;i++)if(worn[i]!=null){
                validateItem(worn[i],"loadout equipment",i);Native950ItemCatalog.Entry type=requireType(worn[i].getId());
                if(type.equipSlot!=i||!type.stackable&&worn[i].getAmount()!=1)throw new IllegalArgumentException("Loadout equipment slot");
            }
            for(Item[] source:new Item[][]{beforeInventory,beforeEquipment})for(Item item:source)if(item!=null){
                int target=indexOf(nextBank,item.getId());
                if(target<0){
                    if(nextBank.length>=Bank.MAX_BANK_SIZE){result=Result.FULL;return;}
                    target=nextBank.length;nextBank=Arrays.copyOf(nextBank,target+1);
                }
                long amount=(long)item.getAmount()+(nextBank[target]==null?0:nextBank[target].getAmount());
                if(amount>Integer.MAX_VALUE){result=Result.FULL;return;}
                nextBank[target]=new Item(item.getId(),(int)amount);
            }
        }
        Result commit(){
            checkOwner();if(committed)return Result.STALE;if(result.moved==0)return result;
            if(bank.bankTabs!=originalTabs||bank.bankTabs[0].length!=originalBank.length
                    ||!unchanged(bank.bankTabs[0],originalBank,beforeBank)
                    ||!unchanged(inventory.getItems(),originalInventory,beforeInventory)
                    ||!unchanged(equipment.getItems(),originalEquipment,beforeEquipment))return Result.STALE;
            validateState();
            bank.bankTabs=new Item[][]{nextBank};
            for(int i=0;i<INVENTORY_SIZE;i++)inventory.set(i,nextInventory[i]);
            for(int i=0;i<EQUIPMENT_SIZE;i++)equipment.set(i,nextEquipment[i]);
            committed=true;
            player.getNative950ActionBar().preferences.equipmentChanged(player,true);
            return result;
        }
    }

    public Result swap(int from, int sourceId, int to, int targetId) {
        checkOwner(); validateState();
        if (from < 0 || from >= INVENTORY_SIZE || to < 0 || to >= INVENTORY_SIZE || from == to) return Result.INVALID;
        Item source = inventory.get(from), target = inventory.get(to);
        if (source == null || source.getId() != sourceId || (target == null ? -1 : target.getId()) != targetId) return Result.STALE;
        inventory.set(from, target);
        inventory.set(to, source);
        return Result.moved(1);
    }

    /** Dry-run the same910 insertion used by loot and skill rewards. */
    public boolean canReceiveItem(Item item) {
        checkOwner(); validateState();
        if (item == null || item.getAmount() < 1 || item.getCharges() != 0 || item.getAttributes() != null || item.getInventionData() != null) return false;
        Native950ItemCatalog.Entry type = catalog.get(item.getId());
        if (type == null) return false;
        if (type.stackable) {
            int slot = indexOf(inventory.getItems(), item.getId());
            if (slot >= 0 && (long)inventory.get(slot).getAmount() + item.getAmount() > Integer.MAX_VALUE) return false;
        }
        return inventory.asItemContainer().add(new Item(item), type.stackable);
    }

    /** Consume all requested resources, or leave the original inventory untouched. */
    public boolean consumeItem(int id, int amount) {
        checkOwner(); validateState();
        if (amount < 1 || catalog.get(id) == null) return false;
        long available = 0;
        for (Item item : inventory.getItems()) if (item != null && item.getId() == id) available += item.getAmount();
        if (available < amount) return false;
        ItemsContainer<Item> staged = inventory.asItemContainer();
        staged.remove(new Item(id, amount));
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) inventory.set(slot, staged.get(slot));
        return true;
    }

    /** Use910 ItemsContainer.add on a staged copy; full bags/overflow never consume floor loot. */
    public Result receiveGroundItem(Item item) {
        checkOwner();validateState();
        if(item==null || item.getAmount()<1 || item.getCharges()!=0 || item.getAttributes()!=null)return Result.INVALID;
        Native950ItemCatalog.Entry type=catalog.get(item.getId());
        if(type==null)return Result.INVALID;
        if(type.stackable) {
            int slot=indexOf(inventory.getItems(),item.getId());
            if(slot>=0 && (long)inventory.get(slot).getAmount()+item.getAmount()>Integer.MAX_VALUE)return Result.FULL;
        }
        ItemsContainer<Item> staged=inventory.asItemContainer();
        if(!staged.add(new Item(item),type.stackable))return Result.FULL;
        for(int slot=0;slot<INVENTORY_SIZE;slot++)inventory.set(slot,staged.get(slot));
        return Result.moved(item.getAmount());
    }

    /** Stage all gathering rewards before changing any authoritative slot. */
    public boolean canReceiveItems(Item[] rewards) { return stageRewards(rewards) != null; }

    public boolean receiveItems(Item[] rewards) {
        ItemsContainer<Item> staged = stageRewards(rewards);
        if (staged == null) return false;
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) inventory.set(slot, staged.get(slot));
        return true;
    }

    private ItemsContainer<Item> stageRewards(Item[] rewards) {
        checkOwner(); validateState();
        if (rewards == null || rewards.length == 0) return null;
        ItemsContainer<Item> staged = inventory.asItemContainer();
        return stageOutputs(staged, rewards) ? staged : null;
    }

    /** Preview production against the same staged910 inventory used for the final commit. */
    public boolean canExchangeItems(Item[] consumed, Item[] produced) {
        return stageExchange(consumed, produced) != null;
    }

    /** Consume resources and add every product atomically, including a completely full backpack. */
    public boolean exchangeItems(Item[] consumed, Item[] produced) {
        ItemsContainer<Item> staged = stageExchange(consumed, produced);
        if (staged == null) return false;
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) inventory.set(slot, staged.get(slot));
        return true;
    }

    /** Exact source object, ID and quantity form the claim across controller callbacks. */
    boolean canExchangeSlot(int slot, Item expected, int expectedId, int expectedAmount,
                            int consumedAmount, Item[] produced) {
        return stageSlotExchange(slot, expected, expectedId, expectedAmount, consumedAmount, produced) != null;
    }

    boolean exchangeSlot(int slot, Item expected, int expectedId, int expectedAmount,
                         int consumedAmount, Item[] produced) {
        ItemsContainer<Item> staged = stageSlotExchange(slot, expected, expectedId, expectedAmount, consumedAmount, produced);
        if (staged == null) return false;
        for (int index = 0; index < INVENTORY_SIZE; index++) inventory.set(index, staged.get(index));
        return true;
    }

    private ItemsContainer<Item> stageSlotExchange(int slot, Item expected, int expectedId,
                                                   int expectedAmount, int consumedAmount, Item[] produced) {
        checkOwner();
        if (slot < 0 || slot >= INVENTORY_SIZE || expected == null || produced == null
                || inventory.get(slot) != expected || expected.getId() != expectedId
                || expected.getAmount() != expectedAmount || consumedAmount < 1 || consumedAmount > expectedAmount
                || !ordinaryResource(expected) || expected.getInventionData() != null) return null;
        validateState();
        ItemsContainer<Item> staged = inventory.asItemContainer();
        staged.set(slot, consumedAmount == expectedAmount ? null : new Item(expectedId, expectedAmount - consumedAmount));
        return stageOutputs(staged, produced) ? staged : null;
    }

    private ItemsContainer<Item> stageExchange(Item[] consumed, Item[] produced) {
        checkOwner();
        if (consumed == null || produced == null || consumed.length == 0) return null;
        // A requested ordinary resource must never match a modified instance by ID alone.
        // Refuse before validateState so skill previews return a reason rather than throwing.
        for(Item required:consumed){
            if(!ordinaryResource(required))return null;
            for(Item held:inventory.getItems())
                if(held!=null&&held.getId()==required.getId()&&!ordinaryResource(held))return null;
        }
        validateState();
        ItemsContainer<Item> staged = inventory.asItemContainer();
        for (Item item : consumed) {
            if (!ordinaryResource(item)) return null;
            long available = 0;
            for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
                Item held = staged.get(slot);
                if (held != null && held.getId() == item.getId()) available += held.getAmount();
            }
            if (available < item.getAmount()) return null;
            staged.remove(new Item(item));
        }
        return stageOutputs(staged, produced) ? staged : null;
    }

    private boolean stageOutputs(ItemsContainer<Item> staged, Item[] produced) {
        for (Item item : produced) {
            if (!ordinaryResource(item)) return false;
            Native950ItemCatalog.Entry type = catalog.get(item.getId());
            if (type.stackable) {
                long amount = item.getAmount();
                for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
                    Item held = staged.get(slot);
                    if (held != null && held.getId() == item.getId()) amount += held.getAmount();
                }
                if (amount > Integer.MAX_VALUE) return false;
            }
            if (!staged.add(new Item(item), type.stackable)) return false;
        }
        return true;
    }

    private boolean ordinaryResource(Item item) {
        return item != null && item.getId() >= 0 && item.getId() <= Native950ItemCatalog.MAX_ITEM_ID && item.getAmount() > 0
                && item.getCharges() == 0 && item.getAttributes() == null && item.getInventionData() == null && catalog.get(item.getId()) != null;
    }

    /** Exact-slot drop seam; called only by the owner-thread floor-item transaction. */
    boolean ownsInventory(Player player) {
        checkOwner();
        return player != null && inventory == player.getInventory().items;
    }

    boolean removeInventorySlot(int slot, Item expected, int expectedId, int expectedAmount) {
        checkOwner();
        if (slot < 0 || slot >= INVENTORY_SIZE || expected == null
                || inventory.get(slot) != expected || expected.getId() != expectedId || expected.getAmount() != expectedAmount) return false;
        if (!ordinaryResource(expected) || expected.getInventionData() != null) return false;
        validateState();
        inventory.set(slot, null);
        return true;
    }

    void restoreInventorySlot(int slot, Item original) {
        checkOwner();
        if (inventory.get(slot) != null) throw new IllegalStateException("Drop rollback slot is occupied");
        inventory.set(slot, original);
    }

    public Native950ItemCatalog.Entry itemType(int id) { checkOwner(); return catalog.get(id); }

    public Snapshot inventorySnapshot() { checkOwner(); return new Snapshot(inventory.getItems(), INVENTORY_SIZE); }
    public Snapshot bankSnapshot() { checkOwner(); return new Snapshot(bank.bankTabs[0], Bank.MAX_BANK_SIZE); }
    public Snapshot equipmentSnapshot() { checkOwner(); return new Snapshot(equipment.getItems(), EQUIPMENT_SIZE); }
    public boolean equipmentKitClaimed() { checkOwner(); return equipmentKitClaimed; }

    private void validateState() {
        if (inventory.getSize() != INVENTORY_SIZE || equipment.getSize() != EQUIPMENT_SIZE
                || bank.bankTabs.length != 1 || bank.bankTabs[0].length > Bank.MAX_BANK_SIZE)
            throw new IllegalStateException("Unexpected modern container shape");
        Item[] inventoryItems = inventory.getItems();
        for (int slot = 0; slot < inventoryItems.length; slot++) {
            Item item = inventoryItems[slot];
            if (item == null) continue;
            validateItem(item, "inventory", slot);
            if (!requireType(item.getId()).stackable && item.getAmount() != 1)
                throw new IllegalStateException("Non-stackable item occupies more than one item per backpack slot");
        }
        for (int slot = 0; slot < bank.bankTabs[0].length; slot++) {
            Item item = bank.bankTabs[0][slot];
            if (item == null) throw new IllegalStateException("Modern bank requires compact slots");
            validateItem(item, "bank", slot);
        }
        for (int slot = 0; slot < EQUIPMENT_SIZE; slot++) {
            Item item = equipment.get(slot);
            if (item == null) continue;
            validateItem(item, "equipment", slot);
            if ((!requireType(item.getId()).stackable && item.getAmount() != 1) || requireType(item.getId()).equipSlot != slot)
                throw new IllegalStateException("Equipment does not match its verified wear slot");
        }
    }

    private void validateItem(Item item, String container, int slot) {
        int id = item.getId();
        Native950ItemCatalog.Entry type = requireType(id);
        int amount = item.getAmount();
        int charges = item.getCharges();
        boolean attributes = item.getAttributes() != null;
        boolean invention = item.getInventionData() != null;
        if (amount < 1 || charges != 0 || attributes || invention) {
            String rule = amount < 1 ? "amount must be positive" : charges != 0 ? "charges are unsupported"
                    : attributes ? "item attributes are unsupported" : "invention metadata is unsupported";
            throw new IllegalStateException("Unsupported modern item state: container=" + container + " slot=" + slot
                    + " item=" + id + " (" + type.name + ") amount=" + amount + " charges=" + charges
                    + " attributes=" + attributes + " invention=" + invention + "; rule=" + rule);
        }
    }

    private Native950ItemCatalog.Entry requireType(int id) {
        Native950ItemCatalog.Entry type = catalog.get(id);
        if (type == null) throw new IllegalArgumentException("Unverified modern item " + id);
        return type;
    }

    private void checkOwner() {
        if (Thread.currentThread() != owner) throw new IllegalStateException("Containers must be used on their owning world thread");
    }

    private static Item[] copy(Item[] source) {
        Item[] result = new Item[source.length];
        for (int i = 0; i < source.length; i++) if (source[i] != null) result[i] = new Item(source[i].getId(), source[i].getAmount());
        return result;
    }
    private static int indexOf(Item[] items, int id) {
        for (int i = 0; i < items.length; i++) if (items[i] != null && items[i].getId() == id) return i;
        return -1;
    }
    private static int removeFromSlot(Item[] items, int slot, int requested) {
        Item item = items[slot];
        int moved = Math.min(item.getAmount(), requested);
        items[slot] = moved == item.getAmount() ? null : new Item(item.getId(), item.getAmount() - moved);
        return moved;
    }
    private static int firstEmpty(Item[] items) {
        for (int i = 0; i < items.length; i++) if (items[i] == null) return i;
        return -1;
    }
    private static int emptyCount(Item[] items) { int count = 0; for (Item item : items) if (item == null) count++; return count; }

    public static final class Snapshot {
        public final int[] ids, amounts;
        private Snapshot(Item[] items, int size) {
            ids = new int[size]; Arrays.fill(ids, -1); amounts = new int[size];
            for (int i = 0; i < items.length; i++) if (items[i] != null) { ids[i] = items[i].getId(); amounts[i] = items[i].getAmount(); }
        }
    }

    public static final class Result {
        public static final Result INVALID = new Result(0, "Invalid slot or quantity");
        public static final Result STALE = new Result(0, "The item in that slot has changed");
        public static final Result FULL = new Result(0, "The destination has no room for this item");
        public static final Result NOT_EQUIPMENT = new Result(0, "That item cannot be equipped yet");
        public static final Result CLAIMED = new Result(0, "You have already collected your equipment kit");
        public static final Result KIT_FULL = new Result(0, "Free three backpack slots to collect your equipment kit");
        public final int moved;
        public final String reason;
        private Result(int moved, String reason) { this.moved = moved; this.reason = reason; }
        private static Result moved(int amount) { return new Result(amount, null); }
    }
}
