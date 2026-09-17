package com.rs.game.item;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.ItemsEquipIds;
import com.rs.game.player.ChargesManagerNew.ChargesData;
import com.rs.game.player.GlobalPlayerUpdater.ItemDye;
import com.rs.game.player.Player;
import com.rs.game.player.TemporaryAttributes;
import com.rs.game.player.TemporaryAttributes.Key;
import com.rs.game.player.actions.invention.InventionData;
import com.rs.game.player.actions.invention.InventionData.Gizmo;
import com.rs.game.player.actions.invention.InventionData.Perk;

import lombok.Getter;

/**
 * Represents a single item.
 */
public class Item implements Serializable {

    private static final long serialVersionUID = -6485003878697568087L;
    protected int amount;
    protected int charges;
    protected boolean placeholder;
    @Getter
    private transient ItemVarsManager varsManager;
    private int newId;
    @Deprecated
    private short id;
    @Deprecated
    private ConcurrentHashMap<Object, Object> attributes;

    private ConcurrentHashMap<TemporaryAttributes.Key, Object> newAttributes;

    public Item(int id) {
        this(id, 1);
        attributes = null;
        newAttributes = null;
    }

    public Item(int id, int amount) {
        this(id, amount, false, 0);
        attributes = null;
        newAttributes = null;
    }

    public Item(int id, int amount, boolean placeholder) {
        this(id, amount, true, 0, placeholder);
        attributes = null;
        newAttributes = null;
    }

    public Item(int id, int amount, int charges) {
        this(id, amount, false, charges);
        attributes = null;
    }

    public Item(int id, int amount, boolean amt0, int charges) {
        this(id, amount, amt0, charges, false);
        attributes = null;
        newAttributes = null;
    }

    public Item(int id, int amount, boolean amt0, int charges, boolean placeholder) {
        this.newId = id;
        this.amount = amount;
        this.charges = charges;
        this.placeholder = false;
        this.attributes = null;
        this.newAttributes = null;
    }

    public Item(Item item) {
        item.switchOldAttributes();
        this.newId = item.getId();
        this.amount = item.getAmount();
        this.charges = item.getCharges();
        this.attributes = null;
        this.newAttributes = item.getAttributes();
    }

    public void switchOldAttributes() {
        generateUUID();
        checkResetAttributes();
        if (attributes == null)
            return;
        newAttributes = new ConcurrentHashMap<TemporaryAttributes.Key, Object>();
        for (Entry<Object, Object> e : attributes.entrySet()) {
            if (!(e.getKey() instanceof TemporaryAttributes.Key))
                continue;
            newAttributes.put((TemporaryAttributes.Key) e.getKey(), e.getValue());
        }
        generateUUID();
        attributes = null;
    }

    @Override
    public Item clone() {
        switchOldAttributes();
        return new Item(newId, amount);
    }

    public int getAmount() {
        switchOldAttributes();
        return amount;
    }

    public int getCharges() {
        switchOldAttributes();
        return charges;
    }

    public void setCharges(int charges) {
        switchOldAttributes();
        if (this.charges + charges < 0 || charges > Integer.MAX_VALUE)
            return;
        this.charges = charges;
        if (charges == 0)
            newAttributes = null;
    }

    public void forceSetAmount(int amount) {
        switchOldAttributes();
        this.amount = amount;
    }

    public void setAmount(int amount) {
        switchOldAttributes();
        if (this.amount + amount < 0 || amount > Integer.MAX_VALUE)
            return;
        this.amount = amount;
    }

    public ItemDefinitions getDefinitions() {
        switchOldAttributes();
        return ItemDefinitions.getItemDefinitions(newId);
    }

    public int getId() {
        switchOldAttributes();
        return newId != 0 ? newId : id;
    }

    public void setId(int id) {
        switchOldAttributes();
        this.newId = id;
    }

    public String getName() {
        switchOldAttributes();
        return getDefinitions().getName();
    }

    // Lazy load for lowercase name, needed for drop catcher filtering.
    private transient String lowercaseName;

    public String getLowercaseName() {
        switchOldAttributes();
        if (lowercaseName == null)
            lowercaseName = getName().toLowerCase();
        return lowercaseName;
    }

    public int getEquipId() {
        switchOldAttributes();
        return ItemsEquipIds.getEquipId(newId);
    }

    public void initVarsManager(Player player) {
        switchOldAttributes();
        if (varsManager == null)
            varsManager = new ItemVarsManager();
        refreshVars(player);
    }

    public void refreshVars(Player player) {
        switchOldAttributes();
        if (getId() == 41083) {
            varsManager.setVarBit(37521, player.getInventionManager().getEmptyDivineChargesStored());
            varsManager.setVarBit(37522, player.getInventionManager().getDivineChargesStored());
            varsManager.setVarBit(37523, player.getInventionManager().getContainerChargesFilled());
            return;
        }
        InventionData inventionData = getInventionData();
        varsManager.setVarBit(30212, (int) (inventionData == null ? 0 : inventionData.getXp()));
        if (inventionData != null) {
            Perk[] perks = {inventionData.getGizmos()[0] == null ? null : inventionData.getGizmos()[0].getPerks()[0],
                    inventionData.getGizmos()[0] == null ? null : inventionData.getGizmos()[0].getPerks()[1],
                    inventionData.getGizmos()[1] == null ? null : inventionData.getGizmos()[1].getPerks()[0],
                    inventionData.getGizmos()[1] == null ? null : inventionData.getGizmos()[1].getPerks()[1],
            };
            for (int i = 0; i < perks.length; i++) {
                varsManager.setVarBit(30215 + (i * 2), perks[i] == null ? 0 : perks[i].getId());
                varsManager.setVarBit(30216 + (i * 2), perks[i] == null ? 0 : perks[i].getRank());
            }
        }
        int chargesUsed = getChargesData() == null ? 0 : (getChargesData().getMaxCharges() - getChargesData().getChargesLeft());
        varsManager.setVarBit(inventionData != null ? 32459 : 18550, chargesUsed);
    }

    public <T> Optional<T> getAttribute(Key key) {
        if (getAttributes() == null)
            setAttributes(new ConcurrentHashMap<>());
        Object value = getAttributesWithOutReset().get(key);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of((T) value);
    }

    public ConcurrentHashMap<TemporaryAttributes.Key, Object> getAttributes() {
        switchOldAttributes();
        return newAttributes;
    }
    
    public ConcurrentHashMap<TemporaryAttributes.Key, Object> getAttributesWithOutReset() {
        return newAttributes;
    }
    
    public Item setAttributes(ConcurrentHashMap<TemporaryAttributes.Key, Object> attributes) {
        switchOldAttributes();
        this.attributes = null;
        newAttributes = attributes;
        generateUUID();
        return this;
    }

    public InventionData getInventionData() {
        switchOldAttributes();
        if (newAttributes == null)
            return null;
        return (InventionData) newAttributes.get(Key.ITEM_INVENTION_DATA);
    }

    public void setInventionData(InventionData inventionData) {
        switchOldAttributes();
        if (newAttributes == null)
            newAttributes = new ConcurrentHashMap<TemporaryAttributes.Key, Object>();
        if (inventionData != null)
            newAttributes.put(Key.ITEM_INVENTION_DATA, inventionData);
        if (inventionData == null)
            newAttributes.remove(Key.ITEM_INVENTION_DATA);
        if (newAttributes != null && newAttributes.isEmpty())
            newAttributes = null;
        checkResetAttributes();
    }

    public boolean hasGizmo() {
        switchOldAttributes();
        if (this.getInventionData() == null || this.getInventionData().getGizmos() == null)
            return false;
        boolean hasGizmos = false;
        for (Gizmo gizmo : getInventionData().getGizmos()) {
            if (gizmo != null) {
                hasGizmos = true;
                break;
            }
        }
        return hasGizmos && getUnAugmentedItemId() == 0 && getDefinitions().getAugmentedItemId() == 0;
    }

    public int getUnAugmentedItemId() {
        switchOldAttributes();
        return getInventionData() != null ? getInventionData().getOriginalItemId() : 0;
    }

    @SuppressWarnings("unchecked")
    public ChargesData getChargesData() {
        switchOldAttributes();
        if (newAttributes == null)
            return null;
        Object attr = newAttributes.get(Key.ITEM_CHARGES_DATA);
        if (attr instanceof ChargesData) {
            return (ChargesData) attr;
        } else if (attr instanceof HashMap) {
            Map<String, Number> data = (HashMap<String, Number>) attr;
            Number originalId = data.get("orignalId");
            if (originalId == null) {
                originalId = data.get("originalId");
            }
            ChargesData chargesData = new ChargesData(originalId.intValue(),
                    data.get("wornId").intValue(),
                    data.get("brokenId").intValue(),
                    data.get("maxCharges").intValue(),
                    data.get("chargesLeft").intValue());
            newAttributes.put(Key.ITEM_CHARGES_DATA, chargesData);
            return chargesData;
        } else {
            return null;
        }
    }

    public void setChargesData(ChargesData data) {
        if (newAttributes == null)
            newAttributes = new ConcurrentHashMap<TemporaryAttributes.Key, Object>();
        if (data != null)
            newAttributes.put(Key.ITEM_CHARGES_DATA, data);
        if (data == null)
            newAttributes.remove(Key.ITEM_CHARGES_DATA);
        if (newAttributes != null && newAttributes.isEmpty())
            newAttributes = null;
        checkResetAttributes();
    }

    @Override
    public String toString() {
        switchOldAttributes();
        return "Item [id=" + newId + ", amount=" + amount + "]";
    }


    public ItemDye getDyeData() {
        switchOldAttributes();
        if (newAttributes == null)
            return null;
        return (ItemDye) newAttributes.get(Key.ITEM_DYE_DATA);
    }

    public void setDyeData(ItemDye itemDye) {
        switchOldAttributes();
        if (newAttributes == null)
            newAttributes = new ConcurrentHashMap<TemporaryAttributes.Key, Object>();
        if (itemDye != null)
            newAttributes.put(Key.ITEM_DYE_DATA, itemDye);
        if (itemDye == null)
            newAttributes.remove(Key.ITEM_DYE_DATA);
        if (newAttributes != null && newAttributes.isEmpty())
            newAttributes = null;
        checkResetAttributes();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + amount;
        result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
        result = prime * result + charges;
        result = prime * result + id;
        result = prime * result + ((newAttributes == null) ? 0 : newAttributes.hashCode());
        result = prime * result + newId;
        result = prime * result + (placeholder ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Item other = (Item) obj;
        generateUUID();
        other.generateUUID();
        if (amount != other.amount)
            return false;
        if (attributes == null) {
            if (other.attributes != null)
                return false;
        } else if (attributes != other.attributes)
            return false;
        else if (newAttributes != other.newAttributes)
            return false;
        if (charges != other.charges)
            return false;
        if (id != other.id)
            return false;
        return newId == other.newId;
    }
    
    
    public void generateUUID() {
        if (newAttributes != null && newAttributes.containsKey(Key.ITEM_UUID))
            return;
        if (newAttributes == null && charges == 0)
            return;
        UUID uuid = UUID.randomUUID();
        if (newAttributes == null)
            newAttributes = new ConcurrentHashMap<TemporaryAttributes.Key, Object>();
        newAttributes.put(Key.ITEM_UUID, uuid);
    }
    
    public boolean exactMatch(Item other) {
        generateUUID();
        other.generateUUID();
        if (id != other.id)
            return false;
        if (newId != other.newId)
            return false;
        if (attributes == null) {
            if (other.attributes != null)
                return false;
        }
        if (newAttributes == null) {
            if (other.newAttributes != null)
                return false;
        }
        if (newAttributes != null) {
            if (other.newAttributes == null)
                return false;
        }
        if (newAttributes != null) {
            return newAttributes.get(Key.ITEM_UUID).equals(other.newAttributes.get(Key.ITEM_UUID));
        }
        return true;
    }

    public int[] getCustomColor() {
        switchOldAttributes();
        if (newAttributes != null && newAttributes.get(Key.ITEM_CUSTOM_COLOUR) != null
                && !(newAttributes.get(Key.ITEM_CUSTOM_COLOUR) instanceof int[])) {
            @SuppressWarnings("unchecked")
            List<Double> coloursList = (ArrayList<Double>) newAttributes.get(Key.ITEM_CUSTOM_COLOUR);
            int[] customColours = new int[coloursList.size()];
            for (int i = 0; i < coloursList.size(); i++) {
                if (coloursList.get(i) == null)
                    continue;
                customColours[i] = coloursList.get(i).intValue();
            }
            newAttributes.put(Key.ITEM_CUSTOM_COLOUR, customColours);
        }
        if (newAttributes != null && newAttributes.get(Key.ITEM_CUSTOM_COLOUR) == null) {
            newAttributes.remove(Key.ITEM_CUSTOM_COLOUR);
            if (newAttributes != null && newAttributes.isEmpty())
                newAttributes = null;
        }
        return newAttributes == null ? null : (int[]) newAttributes.get(Key.ITEM_CUSTOM_COLOUR);
    }
    
    public void setCustomColor(int[] customColor) {
        switchOldAttributes();
        if (newAttributes == null)
            newAttributes = new ConcurrentHashMap<TemporaryAttributes.Key, Object>();
        newAttributes.put(Key.ITEM_CUSTOM_COLOUR, customColor);
        if (customColor == null)
            newAttributes.remove(Key.ITEM_CUSTOM_COLOUR);
        if (newAttributes != null && newAttributes.isEmpty())
            newAttributes = null;
        checkResetAttributes();
    }
    
    public void checkResetAttributes() {
        if (newAttributes == null)
            return;
        if (newAttributes.isEmpty())
            newAttributes = null;
        if (newAttributes.containsKey(Key.ITEM_UUID) && newAttributes.size() == 1 && charges == 0)
            newAttributes = null;
    }
}