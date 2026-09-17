package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Store;
import com.rs.cache.loaders.ItemDefinitions;
import java.security.MessageDigest;
import java.util.Arrays;

/** Reviewed cosmetic families use current-cache models and never borrow unverified 910 bonuses. */
public final class Native950CosmeticEquipment {
    private static final Entry[] ENTRIES = {
        new Entry(1038, "Red partyhat", "dbaa3e286ce8aa04b7af7b7fb3381c15d3aa99817d4d286e4ae89a430fdf624d"),
        new Entry(1040, "Yellow partyhat", "96273f9a7d4d73ee868516bf7c59e01393f44a4caf059b29a832e86f5eb8002a"),
        new Entry(1042, "Blue partyhat", "c560e7e98f856d8afb86c19177fb8a8b4339c7125c23ab0f2a71360f627e59ee"),
        new Entry(1044, "Green partyhat", "63c8c8758dcbf55982a7e885ac68895cbdac4958bf699ce391f648acd0219ae5"),
        new Entry(1046, "Purple partyhat", "8df197bdf9715b9fd08419144509149900557de0fa2db5722ea04eb441a0dcd9"),
        new Entry(1048, "White partyhat", "d2a7ad71df2da646c0b641ff4713fc684c7902ffba8ee88c0e53b67bc03a44ea")
    };
    private static final String[] OPTIONS = {null, "Wear", null, null, "drop"};
    private static Store verifiedStore;
    private Native950CosmeticEquipment() { }

    /**
     * Current-cache equipment with the complete reviewed neutral parameter contract.
     * IDs, names, models and cosmetic slots are not allow-listed. Unknown parameters
     * remain unsupported by classic combat rather than being silently treated as zero.
     */
    public static boolean isNeutralCosmeticEquipment(int id, int actualSlot) {
        if (Cache.STORE == null || !Cache.isFlatReadOnly()) return false;
        try {
            Native950EquipmentTypes.Type type = Native950EquipmentTypes.resolve(id);
            return type != null && type.slot == actualSlot
                    && neutralContract(Native950CacheItems.definition(id), actualSlot);
        } catch (RuntimeException malformed) { return false; }
    }

    static boolean neutralContract(ItemDefinitions d, int actualSlot) {
        return d != null && d.equipSlot == actualSlot && actualSlot >= 0 && actualSlot < 19
                && !d.noted && d.certTemplateId == -1 && d.shardTemplateId == -1
                && d.clientScriptData != null && d.clientScriptData.size() == 3
                && Integer.valueOf(7).equals(d.clientScriptData.get(2195))
                && Integer.valueOf(1).equals(d.clientScriptData.get(624))
                && Integer.valueOf(4191).equals(d.clientScriptData.get(537));
    }

    /** Only these pinned ordinary hats have a reviewed, effect-free equipment contract. */
    public static boolean isVerifiedCosmetic(int id) {
        return entry(id) != null && Cache.STORE != null && verifyCache();
    }

    static Native950ItemCatalog.Entry itemEntry(int id) {
        Entry entry = entry(id);
        return entry != null && isVerifiedCosmetic(id)
                ? new Native950ItemCatalog.Entry(id, entry.name, false, OPTIONS, 0, 2) : null;
    }

    public static synchronized boolean verifyCache() {
        if (Cache.STORE == null || !Cache.isFlatReadOnly())
            throw new IllegalStateException("950 cosmetic equipment requires the paired flat cache");
        if (verifiedStore == Cache.STORE) return true;
        boolean valid = true;
        for (Entry entry : ENTRIES) {
            byte[] raw = Cache.STORE.getIndexes()[19].getFile(entry.id >>> 8, entry.id & 255);
            boolean pinned = NativeCacheVerification.requireBinding("950 cosmetic equipment",
                    "19/" + (entry.id >>> 8) + "/" + (entry.id & 255), entry.hash, hash(raw));
            valid &= pinned;
            if (pinned) valid &= NativeCacheVerification.requireBinding("950 cosmetic equipment",
                    "item " + entry.id + " metadata", "reviewed ordinary head equipment",
                    matchesMetadata(entry.id, ItemDefinitions.decodeStrict947(entry.id, raw, null))
                            ? "reviewed ordinary head equipment" : "mismatched metadata");
        }
        // Both genders use native item model/recolour data. No synthetic appearance template.
        for (int model : new int[]{187, 363})
            valid &= NativeCacheVerification.requireBinding("950 cosmetic equipment", "47/" + model + "/0",
                    modelHash(model), hash(Cache.STORE.getIndexes()[47].getFile(model, 0)));
        if (valid) verifiedStore = Cache.STORE;
        return valid;
    }

    static boolean acceptsDefinition(int id, byte[] raw) {
        Entry entry = entry(id);
        return entry != null && entry.hash.equals(hash(raw))
                && matchesMetadata(id, ItemDefinitions.decodeStrict947(id, raw, null));
    }

    static boolean matchesMetadata(int id, ItemDefinitions d) {
        Entry entry = entry(id);
        return entry != null && d != null && d.getId() == id && entry.name.equals(d.name)
                && d.equipSlot == 0 && d.getEquipType() == -1 && d.getEquipType2() == -1
                && d.stackable == 0 && !d.noted && !d.lended
                && d.certTemplateId == -1 && d.lendTemplateId == -1 && d.bindTemplateId == -1
                && d.shardTemplateId == -1 && d.stackIds == null
                && Arrays.equals(OPTIONS, d.inventoryOptions)
                && d.getMaleWornModelId1() == 187 && d.getFemaleWornModelId1() == 363
                && d.getMaleWornModelId2() == -1 && d.getMaleWornModelId3() == -1
                && d.getFemaleWornModelId2() == -1 && d.getFemaleWornModelId3() == -1
                // The entire parameter set is reviewed: no requirement, charges, combat or item effect.
                && d.clientScriptData != null && d.clientScriptData.size() == 3
                && Integer.valueOf(7).equals(d.clientScriptData.get(2195))
                && Integer.valueOf(1).equals(d.clientScriptData.get(624))
                && Integer.valueOf(4191).equals(d.clientScriptData.get(537));
    }

    static boolean acceptsModel(int id, byte[] bytes) {
        return (id == 187 || id == 363) && modelHash(id).equals(hash(bytes));
    }
    private static String modelHash(int id) {
        if (id == 187) return "97cf524df651937fde980d6532a269cf4bf33037edc1e2acc1536981c9b7ef5d";
        if (id == 363) return "53668b4a4e2e21b122caf45c67d205d77e62401602f52a757d3986c7b105c21f";
        throw new IllegalArgumentException("Unreviewed cosmetic model " + id);
    }
    private static Entry entry(int id) {
        for (Entry entry : ENTRIES) if (entry.id == id) return entry;
        return null;
    }
    private static String hash(byte[] bytes) {
        if (bytes == null) return "missing";
        try {
            StringBuilder out = new StringBuilder();
            for (byte value : MessageDigest.getInstance("SHA-256").digest(bytes))
                out.append(String.format("%02x", value & 255));
            return out.toString();
        } catch (java.security.NoSuchAlgorithmException impossible) { throw new AssertionError(impossible); }
    }
    private static final class Entry {
        final int id; final String name, hash;
        Entry(int id, String name, String hash) { this.id=id; this.name=name; this.hash=hash; }
    }
}