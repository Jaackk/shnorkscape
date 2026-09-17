package com.rs.game.player.client;

import com.rs.cache.filestore.store.Store;
import com.rs.Settings;
import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.utils.data.parsers.npcs.NPCDropsDataParser;
import com.rs.utils.data.parsers.npcs.NPCDropTableRolls;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.security.MessageDigest;

/** Adapts the existing 910 ordinary drop table to items validated against the selected 950 cache. */
public final class Native950NpcDrops {
    private Native950NpcDrops() { }
    // Reuse Native950CacheContent.itemCatalog's established coin pin. Its old Convert menu
    // became Add to pouch; that menu change does not invalidate the currency used by loot.
    private static final String COINS_SHA256 = "628c2fd1154e10ed7eb2cda75f31d9736d12636269323e6adab292bbffac45ff";
    private static Store metadataStore;
    private static final Map<Integer, ItemMetadata> metadata = new HashMap<Integer, ItemMetadata>();

    /** Caller supplies the legacy damage-credit owner and owns placement/lifetime, as NPC.drop does. */
    public static List<Item> roll(NPC npc, Player owner) {
        if (npc == null || owner == null || !npc.isNative950() || !owner.isNative950()
                || !Native950IdValidity.get().isSafe(Native950IdValidity.Kind.NPC, npc.getId()))
            return Collections.emptyList();
        return roll(NPCDropsDataParser.getDrops(npc.getId()), Settings.getDropQuantityRate(owner),
                Settings.DOUBLE_DROPS, Native950NpcDrops::metadata, NPCDropTableRolls.LEGACY_ROLLS);
    }

    static List<Item> roll(NPCDrop[] table, double multiplier, boolean doubleDrops,
            IntFunction<ItemMetadata> items, NPCDropTableRolls.Rolls random) {
        List<Item> result = new ArrayList<Item>();
        // Contract reductions, donation conversions and unrelated death callbacks remain with those systems.
        NPCDropTableRolls.roll(table, multiplier, 100,
                drop -> validRow(drop, doubleDrops) && items.apply(drop.getItemId()) != null,
                drop -> {
                    int amount = NPCDropTableRolls.amount(drop, random);
                    if (doubleDrops) amount *= 2;
                    if (amount > 0) result.add(new Item(drop.getItemId(), amount));
                }, random);
        return Collections.unmodifiableList(result);
    }

    private static boolean validRow(NPCDrop drop, boolean doubleDrops) {
        return Double.isFinite(drop.getRate()) && drop.getRate() >= 0 && drop.getRate() <= 100
                && drop.getMinAmount() >= 0 && drop.getMaxAmount() >= drop.getMinAmount()
                && drop.getMaxAmount() <= (doubleDrops ? Integer.MAX_VALUE / 2 : Integer.MAX_VALUE - 1);
    }

    /** Supplies current stack and note semantics for ground-item storage and pickup. */
    public static synchronized ItemMetadata metadata(int id) {
        if (Cache.STORE == null || !Cache.isFlatReadOnly())
            throw new IllegalStateException("Native950 drops require the selected read-only cache");
        if (metadataStore != Cache.STORE) { metadata.clear(); metadataStore = Cache.STORE; }
        if (!metadata.containsKey(id)) metadata.put(id, resolveMetadata(id,
                value -> Native950IdValidity.get().isSafe(Native950IdValidity.Kind.ITEM, value),
                value -> Cache.STORE.getIndexes()[19].getFile(value >>> 8, value & 255)));
        return metadata.get(id);
    }

    static ItemMetadata resolveMetadata(int id, IntPredicate safe, IntFunction<byte[]> files) {
        if (id < 1 || id > 65534) return null;
        try {
            byte[] bytes = files.apply(id);
            if (!safe.test(id) && !pinnedCoins(id, bytes)) return null;
            ItemDefinitions definition = bytes == null ? null : ItemDefinitions.decodeStrict947(id, bytes, null);
            if (definition == null || transformed(definition)) return null;
            if (definition.certTemplateId == -1) {
                if (!named(definition)) return null;
                return new ItemMetadata(id, definition.getName(), definition.isStackable(), false, id);
            }
            int baseId = definition.getCertId();
            if (baseId < 1 || baseId > 65534 || baseId == id || !safe.test(baseId)) return null;
            ItemDefinitions base = decode(baseId, files);
            // A noted table row remains that exact id. Never invent baseId+1 or use a stale 910 certificate.
            if (base == null || !named(base) || transformed(base) || base.certTemplateId != -1
                    || base.getCertId() != id || decode(definition.certTemplateId, files) == null) return null;
            return new ItemMetadata(id, base.getName(), true, true, baseId);
        } catch (RuntimeException invalid) { return null; }
    }
    static boolean pinnedCoins(int id, byte[] bytes) {
        if (id != 995 || bytes == null) return false;
        try {
            StringBuilder hex = new StringBuilder();
            for (byte value : MessageDigest.getInstance("SHA-256").digest(bytes))
                hex.append(String.format("%02x", value & 255));
            return COINS_SHA256.equals(hex.toString());
        } catch (java.security.NoSuchAlgorithmException missing) { throw new IllegalStateException(missing); }
    }
    private static ItemDefinitions decode(int id, IntFunction<byte[]> files) {
        if (id < 0 || id > 0xffffff) return null;
        byte[] bytes = files.apply(id);
        return bytes == null ? null : ItemDefinitions.decodeStrict947(id, bytes, null);
    }
    private static boolean transformed(ItemDefinitions definition) {
        return definition.lendTemplateId != -1 || definition.bindTemplateId != -1 || definition.shardTemplateId != -1;
    }
    private static boolean named(ItemDefinitions definition) {
        return definition.getName() != null && !definition.getName().trim().isEmpty()
                && !"null".equalsIgnoreCase(definition.getName());
    }
    public static final class ItemMetadata {
        public final int id, baseId;
        public final String name;
        public final boolean stackable, noted;
        ItemMetadata(int id, String name, boolean stackable, boolean noted, int baseId) {
            this.id = id; this.name = name; this.stackable = stackable; this.noted = noted; this.baseId = baseId;
        }
    }
}
