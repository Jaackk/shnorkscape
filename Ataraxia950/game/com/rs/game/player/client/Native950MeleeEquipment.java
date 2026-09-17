package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Store;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.player.combat.rs2.ClassicBonuses;
import java.security.MessageDigest;

/** Two explicitly reviewed renamed kit items; does not relax the general ID validity table. */
public final class Native950MeleeEquipment {
    private static final Entry[] ENTRIES = {
        new Entry(1173, "Bronze square shield", "Bronze sq shield", 5, true,
                "97af6e94f54e0289f9b4c6ee7d6ad0430ff34025df9215bf87f383fe3da9f184",
                new ClassicBonuses(0, 0, 0, -6, -2, 5, 6, 4, 0, 5, 0, 0, 0, 0)),
        new Entry(1139, "Bronze med helm", "Bronze med helm", 0, false,
                "df1f9d31a32f0dac7ba94adccf8d0e01c9515c1681a351c41877a879562ca3a0",
                new ClassicBonuses(0, 0, 0, -3, -1, 3, 4, 2, -1, 3, 0, 0, 0, 0))
    };
    private static Store verifiedStore;
    private Native950MeleeEquipment() { }

    /** Cache-only startup preflight: never initializes the bonus database or resolves data paths. */
    public static synchronized boolean verifyCache() {
        if (Cache.STORE == null || !Cache.isFlatReadOnly())
            throw new IllegalStateException("950 melee kit requires the paired flat cache");
        if (verifiedStore == Cache.STORE) return true;
        boolean valid = true;
        for (Entry entry : ENTRIES) {
            byte[] raw = Cache.STORE.getIndexes()[19].getFile(entry.id >>> 8, entry.id & 255);
            boolean pinned = NativeCacheVerification.requireBinding("950 melee equipment kit",
                    "19/" + (entry.id >>> 8) + "/" + (entry.id & 255), entry.hash, hash(raw));
            valid &= pinned;
            if (pinned) valid &= NativeCacheVerification.requireBinding("950 melee equipment kit",
                    "item " + entry.id + " metadata", "expected name, slot and armour flags",
                    matchesMetadata(entry.id, ItemDefinitions.decodeStrict947(entry.id, raw, null))
                            ? "expected name, slot and armour flags" : "mismatched metadata");
        }
        if (valid) verifiedStore = Cache.STORE;
        return valid;
    }

    /** Additional combat admission only for these two pinned items; unknown IDs stay unaccepted. */
    public static boolean isVerifiedKitItem(int id) {
        return entry(id) != null && Cache.STORE != null && verifyCache();
    }

    /** Exact alias into the existing classic table, without accepting similarly named equipment. */
    public static String classicName(int id, String nativeName) {
        Entry entry = entry(id);
        return entry != null && entry.nativeName.equals(nativeName) ? entry.classicName : nativeName;
    }

    /** Separately check the actual loaded row; cache preflight deliberately never loads this data. */
    public static boolean hasExpectedClassicBonuses(int id, ClassicBonuses actual) {
        Entry entry = entry(id);
        if (entry == null || actual == null) return false;
        ClassicBonuses expected = entry.bonuses;
        return actual.stabAtk == expected.stabAtk && actual.slashAtk == expected.slashAtk
                && actual.crushAtk == expected.crushAtk && actual.magicAtk == expected.magicAtk
                && actual.rangeAtk == expected.rangeAtk && actual.stabDef == expected.stabDef
                && actual.slashDef == expected.slashDef && actual.crushDef == expected.crushDef
                && actual.magicDef == expected.magicDef && actual.rangeDef == expected.rangeDef
                && actual.strBonus == expected.strBonus && actual.rangeStrBonus == expected.rangeStrBonus
                && actual.magicDamagePercent == expected.magicDamagePercent && actual.prayerBonus == expected.prayerBonus;
    }

    // Pure seams share the exact runtime metadata check and allow literal-cache regression fixtures.
    static boolean acceptsDefinition(int id, byte[] raw) {
        Entry entry = entry(id);
        return entry != null && entry.hash.equals(hash(raw))
                && matchesMetadata(id, ItemDefinitions.decodeStrict947(id, raw, null));
    }
    static boolean matchesMetadata(int id, ItemDefinitions definition) {
        Entry entry = entry(id);
        return entry != null && definition != null && definition.getId() == id
                && entry.nativeName.equals(definition.getName()) && definition.getEquipSlot() == entry.slot
                && definition.isShield() == entry.shield && !definition.isMeleeTypeWeapon()
                && !definition.isRangeTypeWeapon() && !definition.isMagicTypeWeapon();
    }
    private static Entry entry(int id) {
        for (Entry entry : ENTRIES) if (entry.id == id) return entry;
        return null;
    }
    private static String hash(byte[] data) {
        if (data == null) return "missing";
        try {
            StringBuilder out = new StringBuilder();
            for (byte value : MessageDigest.getInstance("SHA-256").digest(data))
                out.append(String.format("%02x", value & 255));
            return out.toString();
        } catch (java.security.NoSuchAlgorithmException impossible) { throw new AssertionError(impossible); }
    }
    private static final class Entry {
        final int id, slot;
        final String nativeName, classicName, hash;
        final boolean shield;
        final ClassicBonuses bonuses;
        Entry(int id, String nativeName, String classicName, int slot, boolean shield, String hash, ClassicBonuses bonuses) {
            this.id = id; this.nativeName = nativeName; this.classicName = classicName;
            this.slot = slot; this.shield = shield; this.hash = hash; this.bonuses = bonuses;
        }
    }
}
