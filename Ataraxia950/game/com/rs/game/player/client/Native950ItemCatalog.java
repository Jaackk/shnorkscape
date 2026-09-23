package com.rs.game.player.client;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Validated modern item metadata supplied by the selected cache adapter. */
public final class Native950ItemCatalog {
    public static final int MAX_ITEM_ID = 0xfffffe; // native container u24 encodes id + 1
    private final Map<Integer, Entry> entries;
    private final java.util.function.IntFunction<Entry> fallback;
    private final Map<Integer,Entry> resolved=new java.util.HashMap<>();

    public Native950ItemCatalog(Collection<Entry> entries) { this(entries, id -> null); }

    public Native950ItemCatalog(Collection<Entry> entries, java.util.function.IntFunction<Entry> fallback) {
        this.fallback=Objects.requireNonNull(fallback,"fallback");
        Map<Integer, Entry> copy = new LinkedHashMap<Integer, Entry>();
        for (Entry entry : Objects.requireNonNull(entries, "entries")) {
            if (copy.put(entry.id, entry) != null) throw new IllegalArgumentException("Duplicate item " + entry.id);
        }
        this.entries = Collections.unmodifiableMap(copy);
    }

    public synchronized Entry get(int id) {
        Entry entry=entries.get(id);
        if(entry!=null)return entry;
        if(!resolved.containsKey(id))resolved.put(id,fallback.apply(id));
        return resolved.get(id);
    }

    /** Resolve every item and equipment capability from the selected current cache. */
    public Native950ItemCatalog withLegacyDrops() {
        return new Native950ItemCatalog(Collections.<Entry>emptyList(), id -> {
            Entry current = Native950CacheItems.entry(id);
            if (current == null) return null;
            Native950EquipmentTypes.Type equipment = Native950EquipmentTypes.resolve(id);
            if (equipment == null) return current;
            int option = 0;
            for (int candidate = 1; candidate <= 5; candidate++)
                if (equipment.isWearOption(candidate)) { option = candidate; break; }
            return new Entry(id, current.name, current.stackMode, current.inventoryOptions, equipment.slot, option);
        });
    }

    Native950ItemCatalog withCurrentCache(java.util.function.IntFunction<Entry> currentCache) {
        // Resolve seeds as well as fallback items: even the pinned bronze kit and coins need
        // their current Drop/menu labels rather than the old list of implemented actions only.
        return new Native950ItemCatalog(Collections.<Entry>emptyList(), id -> {
            Entry current = currentCache.apply(id);
            if (current == null) return null;
            Entry skill = entries.get(id);
            if (skill == null) {
                skill = Native950CosmeticEquipment.itemEntry(id);
                if (skill == null) skill = Native950Woodcutting.itemEntry(id);
                if (skill == null) skill = Native950Firemaking.itemEntry(id);
                if (skill == null) skill = Native950Mining.itemEntry(id);
                Entry production = Native950Production.itemEntry(id);
                if (production != null) skill = mergeOptions(skill, production);
                Entry prayer = Native950Prayer.itemEntry(id);
                if (prayer != null) skill = mergeOptions(skill, prayer);
                for (Entry extra : new Entry[]{Native950Crafting.itemEntry(id), Native950Summoning.itemEntry(id),
                        Native950Runecrafting.itemEntry(id), Native950Divination.itemEntry(id), Native950Hunter.itemEntry(id), Native950Smithing.itemEntry(id)})
                    if (extra != null) skill = mergeOptions(skill, extra);
            }
            return withCacheOptions(skill, current);
        });
    }

    static Entry withCacheOptions(Entry verified, Entry current) {
        if (current == null) return null;
        // A Wield/Wear string is not proof that the appearance/equipment path supports this ID.
        boolean equip = verified != null && verified.id == current.id && verified.name.equals(current.name)
                && verified.stackable == current.stackable && verified.equipSlot >= 0
                && Objects.equals(verified.option(verified.equipOption), current.option(verified.equipOption));
        return new Entry(current.id, current.name, current.stackMode, current.inventoryOptions,
                equip ? verified.equipSlot : -1, equip ? verified.equipOption : 0);
    }
    private static Entry mergeOptions(Entry base,Entry extra) {
        if(base==null)return extra;
        String[] options=new String[5];
        for(int i=0;i<options.length;i++)options[i]=base.option(i+1)!=null?base.option(i+1):extra.option(i+1);
        return new Entry(base.id,base.name,base.stackable,options,
                base.equipSlot>=0?base.equipSlot:extra.equipSlot,base.equipSlot>=0?base.equipOption:extra.equipOption);
    }

    public static final class Entry {
        public final int id;
        public final String name;
        public final boolean stackable;
        /** Native0=ordinary,1=stackable,2=individual item state (e.g. augmented gear). */
        public final int stackMode;
        public final int equipSlot;
        public final int equipOption;
        private final String[] inventoryOptions;

        public Entry(int id, String name, boolean stackable, String[] inventoryOptions) {
            this(id, name, stackable, inventoryOptions, -1, 0);
        }

        public Entry(int id, String name, boolean stackable, String[] inventoryOptions,
                     int equipSlot, int equipOption) {
            this(id,name,stackable?1:0,inventoryOptions,equipSlot,equipOption);
        }
        public Entry(int id,String name,int stackMode,String[] inventoryOptions) {
            this(id,name,stackMode,inventoryOptions,-1,0);
        }
        public Entry(int id,String name,int stackMode,String[] inventoryOptions,int equipSlot,int equipOption) {
            if (id < 0 || id > MAX_ITEM_ID) throw new IllegalArgumentException("Item is outside the verified inventory wire range");
            if(stackMode<0||stackMode>2)throw new IllegalArgumentException("Unsupported native item stack mode");
            this.id = id;
            this.name = Objects.requireNonNull(name, "name");
            this.stackMode=stackMode;
            this.stackable = stackMode==1;
            this.inventoryOptions = Objects.requireNonNull(inventoryOptions, "inventoryOptions").clone();
            if (equipSlot == -1) {
                if (equipOption != 0) throw new IllegalArgumentException("Non-wearable item has an equip operation");
            } else {
                if (equipSlot < 0 || equipSlot >= 19)
                    throw new IllegalArgumentException("Unsupported equipment slot");
                String option = option(equipOption);
                if (equipOption < 1 || equipOption > 5 || !("Wear".equalsIgnoreCase(option) || "Wield".equalsIgnoreCase(option) || "Equip".equalsIgnoreCase(option)))
                    throw new IllegalArgumentException("Equip operation must name a verified Wear or Wield action");
            }
            this.equipSlot = equipSlot;
            this.equipOption = equipOption;
        }

        public String option(int oneBased) {
            return oneBased < 1 || oneBased > inventoryOptions.length ? null : inventoryOptions[oneBased - 1];
        }
    }
}
