package com.rs.game.item;

import java.io.Serializable;

/**
 * Container class.
 *
 * @param <T>
 * @author Graham / edited by Dragonkk(Alex)
 */
public final class ItemsContainer<T extends Item> implements Serializable {

    private static final long serialVersionUID = 1099313426737026107L;

    private Item[] data;
    private boolean alwaysStackable = false;

    public ItemsContainer(int size, boolean alwaysStackable) {
        data = new Item[size];
        this.alwaysStackable = alwaysStackable;
    }

    public boolean isEmpty() {
        for (int i = 0; i < data.length; i++) {
            if (data[i] != null) {
                return false;
            }
        }
        return true;
    }

    public boolean add(T item) {
        return add(item, item.getDefinitions().isStackable() || item.getDefinitions().isNoted());
    }

    /** Same container insertion, with stack semantics supplied by a verified cache adapter. */
    public boolean add(T item, boolean stackable) {
        if (item.getAttributes() == null && (alwaysStackable || stackable)) {
            for (int i = 0; i < data.length; i++) {
                if (data[i] != null) {
                    if (data[i].getId() == item.getId() && item.getAttributes() == null) {
                        data[i] = new Item(data[i].getId(), data[i].getAmount() + item.getAmount(), data[i].getCharges() + item.getCharges());
                        return true;
                    }
                }
            }
        } else {
            if (item.getAmount() > 1) {
                if (freeSlots() >= item.getAmount()) {
                    for (int i = 0; i < item.getAmount(); i++) {
                        int index = freeSlot();
                        data[index] = new Item(item.getId(), 1, item.getCharges());
                    }
                    return true;
                } else {
                    return false;
                }
            }
        }
        int index = freeSlot();
        if (index == -1) {
            return false;
        }
        data[index] = item;
        return true;
    }

    @SuppressWarnings("unchecked")
    public void addAll(Item[] container) {
        for (int i = 0; i < container.length; i++) {
            Item item = container[i];
            if (item != null) {
                this.add((T) item);
            }
        }
    }

    public void addAll(ItemsContainer<T> container) {
        for (int i = 0; i < container.getSize(); i++) {
            T item = container.get(i);
            if (item != null) {
                this.add(item);
            }
        }
    }

    public ItemsContainer<Item> asItemContainer() {
        ItemsContainer<Item> c = new ItemsContainer<Item>(data.length, this.alwaysStackable);
        System.arraycopy(data, 0, c.data, 0, data.length);
        return c;
    }

    public void clear() {
        for (int i = 0; i < data.length; i++) {
            data[i] = null;
        }
    }

    public boolean contains(T item) {
        int amtOf = 0;
        for (Item aData : data) {
            if (aData != null) {
                if (aData.exactMatch(item)) {
                    amtOf += aData.getAmount();
                }
            }
        }
        return amtOf >= item.getAmount();
    }

    public boolean containsOne(T item) {
        for (Item aData : data) {
            if (aData != null) {
                if (aData.exactMatch(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean forceAdd(T item) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] == null) {
                data[i] = item;
                return true;
            }
        }
        return false;
    }

    public int freeSlot() {
        for (int i = 0; i < data.length; i++) {
            if (data[i] == null) {
                return i;
            }
        }
        return -1;
    }

    public int freeSlots() {
        int j = 0;
        for (Item aData : data) {
            if (aData == null) {
                j++;
            }
        }
        return j;
    }

    @SuppressWarnings("unchecked")
    public T get(int slot) {
        if (slot < 0 || slot >= data.length) {
            return null;
        }
        return (T) data[slot];
    }

    public int getFreeSlot() {
        for (int i = 0; i < data.length; i++) {
            if (data[i] == null) {
                return i;
            }
        }
        return -1;
    }

    public int getFreeSlots() {
        int s = 0;
        for (Item aData : data) {
            if (aData == null) {
                s++;
            }
        }
        return s;
    }

    public Item[] getItems() {
        return data;
    }

    public Item[] getItemsCopy() {
        Item[] newData = new Item[data.length];
        System.arraycopy(data, 0, newData, 0, newData.length);
        return newData;
    }

    public int getNumberOf(int item) {
        int count = 0;
        for (Item aData : data) {
            if (aData != null) {
                if (aData.getId() == item) {
                    count += aData.getAmount();
                }
            }
        }
        return count;
    }

    public int getNumberOf(Item item) {
        int count = 0;
        for (Item aData : data) {
            if (aData != null) {
                if (aData.exactMatch(item))
                    count += aData.getAmount();
            }
        }
        return count;
    }

    public int getSize() {
        return data.length;
    }

    public int getThisItemSlot(T item) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] != null) {
                if (data[i].exactMatch(item)) {
                    return i;
                }
            }
        }
        return getFreeSlot();
    }

    public int getUsedSlots() {
        int s = 0;
        for (Item aData : data) {
            if (aData != null) {
                s++;
            }
        }
        return s;
    }

    public boolean goesOverAmount(ItemsContainer<T> container) {
        for (int i = 0; i < container.getSize(); i++) {
            Item item = container.get(i);
            if (item != null) {
                if (getNumberOf(item) + item.getAmount() < 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasSpaceFor(ItemsContainer<T> container) {
        for (int i = 0; i < container.getSize(); i++) {
            T item = container.get(i);
            if (item != null) {
                if (!this.hasSpaceForItem(item)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean hasSpaceForItem(T item) {
        if (item.getAttributes() == null && (alwaysStackable || item.getDefinitions().isStackable() || item.getDefinitions().isNoted())) {
            for (Item aData : data) {
                if (aData != null) {
                    if (aData.getId() == item.getId()) {
                        return true;
                    }
                }
            }
        } else {
            if (item.getAmount() > 1) {
                return freeSlots() >= item.getAmount();
            }
        }
        int index = freeSlot();
        return index != -1;
    }

    public Item lookup(int id) {
        for (Item aData : data) {
            if (aData == null) {
                continue;
            }
            if (aData.getId() == id) {
                return aData;
            }
        }
        return null;
    }
    
    public int lookupSlot(Item item) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] == null) {
                continue;
            }
            if (data[i].exactMatch(item)) {
                return i;
            }
        }
        return -1;
    }
    
    public int lookupSlot(int id) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] == null) {
                continue;
            }
            if (data[i].getId() == id) {
                return i;
            }
        }
        return -1;
    }

    public int remove(int preferredSlot, Item item) {
        int removed = 0, toRemove = item.getAmount();
        if (data[preferredSlot] != null) {
            if (data[preferredSlot].exactMatch(item)) {
                int amt = data[preferredSlot].getAmount();
                if (amt > toRemove) {
                    removed += toRemove;
                    amt -= toRemove;
                    toRemove = 0;
                    // data[preferredSlot] = new
                    // Item(data[preferredSlot].getDefinition().getId(), amt);
                    Item t = new Item(data[preferredSlot].getId(), amt, data[preferredSlot].getCharges());
                    t.setAttributes(data[preferredSlot].getAttributes());
                    set2(preferredSlot, t);
                    return removed;
                } else {
                    removed += amt;
                    toRemove -= amt;
                    // data[preferredSlot] = null;
                    set(preferredSlot, null);
                }
            }
        }
        for (int i = 0; i < data.length; i++) {
            if (data[i] != null) {
                if (data[i].exactMatch(item)) {
                    int amt = data[i].getAmount();
                    if (amt > toRemove) {
                        removed += toRemove;
                        amt -= toRemove;
                        toRemove = 0;
                        // data[i] = new Item(data[i].getDefinition().getId(),
                        // amt);
                        Item t = new Item(data[i].getId(), amt, data[i].getCharges());
                        t.setAttributes(data[i].getAttributes());
                        set2(i, t);
                        return removed;
                    } else {
                        removed += amt;
                        toRemove -= amt;
                        // data[i] = null;
                        set(i, null);
                    }
                }
            }
        }
        return removed;
    }

    public int remove(T item) {
        int removed = 0, toRemove = item.getAmount();
        for (int i = 0; i < data.length; i++) {
            if (data[i] != null) {
                if (data[i].exactMatch(item)) {
                    int amt = data[i].getAmount();
                    if (amt > toRemove) {
                        removed += toRemove;
                        amt -= toRemove;
                        toRemove = 0;
                        data[i] = new Item(data[i].getId(), amt, item.getCharges());
                        data[i].setAttributes(item.getAttributes());
                        return removed;
                    } else {
                        removed += amt;
                        toRemove -= amt;
                        data[i] = null;
                    }
                }
            }
        }
        return removed;
    }

    public void removeAll(T item) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] != null) {
                if (data[i].exactMatch(item)) {
                    data[i] = null;
                }
            }
        }
    }

    public void reset() {
        data = new Item[data.length];
    }

    public void set(int slot, T item) {
        if (slot < 0 || slot >= data.length)
            return;
        data[slot] = item;
    }

    public void set2(int slot, Item item) {
        if (slot < 0 || slot >= data.length)
            return;
        data[slot] = item;
    }

    public void shift() {
        Item[] oldData = data;
        data = new Item[oldData.length];
        int ptr = 0;
        for (int i = 0; i < data.length; i++) {
            if (oldData[i] != null)
                data[ptr++] = oldData[i];
        }
    }

    public Item[] toArray() {
        return data;
    }

    public int getThisItemSlot(int itemId) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] != null) {
                if (data[i].getId() == itemId) {
                    return i;
                }
            }
        }
        return -1;
    }

    public Item[] getShiftedItem() {
        int ptr = 0;
        for (Item item : data) {
            if (item == null)
                continue;
            ptr++;
        }
        Item[] items = new Item[ptr];
        ptr = 0;
        for (Item item : data) {
            if (item == null)
                continue;
            items[ptr++] = item;
        }
        return items;
    }
}