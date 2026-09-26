package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.AnimationDefinitions;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.cache.loaders.rs3.RS3GeneralRequirementMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Stream {@code npcanim2} (TOP50 round 5): the attack and block animation of a creature that
 * carries <b>no</b> weapon-family parameter 2816 - the creatures round 4's table cannot reach.
 *
 * <p>Round 4 sourced a creature's swing from NPC parameter 2816, the weapon-family struct, and
 * stopped where a creature does not carry one. The Varrock guards (5919, 5920, 9234) and the four
 * elemental wizards (2709-2712) - the creatures the user reported still standing rigid - are
 * exactly that case: their whole parameter map is stats, a stance and a BAS.</p>
 *
 * <h2>The route</h2>
 *
 * <p>A creature that wields a weapon is <b>drawn holding it</b>. The definition's {@code models}
 * array contains the very worn model an item definition publishes in {@code maleEquip1 /
 * femaleEquip1 / maleEquip2 / femaleEquip2}; that model maps back to the items that wear it, their
 * own parameter 686 names the weapon struct, and the struct's parameter 2914 (typed
 * {@code ANIMATION} by the cache's ParamType table) is the attack sequence with 2917 the block -
 * the same two struct fields a player's own equipped weapon of that struct resolves to. Varrock
 * guard 5919 is drawn with model 74312, worn in this cache by nothing except the two definitions
 * named "Bronze sword", whose struct 14922 publishes 37378/18292.</p>
 *
 * <p>A row ships only where the creature's models reach <b>exactly one</b> weapon family. Where the
 * creature also carries 2816 and the two routes disagree, the declared family wins and nothing is
 * shipped from the model: measured over the 250 definitions carrying both, 39 are ambiguous and the
 * drawn weapon agrees with the declared family on 183 of the remaining 211.</p>
 *
 * <h2>The render gate</h2>
 *
 * <p>{@link Native950NpcCombatAnimations#compatibleWithRender} refuses a legacy-frame combat
 * sequence on a creature whose BAS stand animation is a modern/animaya one. Every row here is
 * checked against it at verification time: an incompatible <b>attack</b> sequence fails startup
 * (no such row is shipped - Seren mage 22472 and Necromancer 22478 were refused for it) and an
 * incompatible <b>block</b> sequence is dropped for that creature, so it swings without flinching
 * rather than playing a sequence the client is known to mis-bind. Shnorkscape consults this table
 * only from the Developer Console's NPC preview (Stage B Phase 4); it does not feed live combat.</p>
 *
 * <p>Table: {@code resources/native950/npcanim2-drawn-950.tsv}.
 * Pins: {@code resources/native950/npcanim2-950.properties}.
 * Adopted from Artaven's decoder-and-wiki-evidenced table; the shipped table, pins and
 * verification below are unchanged from their source. See
 * docs/STAGE-B-PHASE4-NPC-ANIMATIONS-20260926.md.</p>
 */
public final class Native950NpcDrawnWeapons {

    private Native950NpcDrawnWeapons() { }

    /** Item parameter naming a weapon's struct - the field the shipped player weapon tables use. */
    public static final int PARAM_ITEM_STRUCT = 686;
    /** NPC parameter naming a declared weapon family; a class C creature must NOT carry it. */
    public static final int PARAM_WEAPON_STRUCT = 2816;
    /** Struct parameter holding the attack sequence; the cache types it ANIMATION. */
    public static final int PARAM_ATTACK_SEQUENCE = 2914;
    /** Struct parameter holding the block sequence; the cache types it ANIMATION. */
    public static final int PARAM_BLOCK_SEQUENCE = 2917;

    /** One shipped creature row. */
    public static final class Row {
        public final int npcId, struct, attackSequence, blockSequence, itemId, model;
        public final String npcName, itemName, evidence, route, wikiPage, wikiEvidence;
        Row(int npcId, String npcName, int struct, int attackSequence, int blockSequence, int itemId,
                String itemName, int model, String evidence, String route, String wikiPage, String wikiEvidence) {
            this.npcId = npcId; this.npcName = npcName; this.struct = struct;
            this.attackSequence = attackSequence; this.blockSequence = blockSequence;
            this.itemId = itemId; this.itemName = itemName; this.model = model;
            this.evidence = evidence; this.route = route;
            this.wikiPage = wikiPage; this.wikiEvidence = wikiEvidence;
        }
        /** True when the creature's own parameter 2816 must also name this struct. */
        public boolean declaresStruct() { return "param2816+drawn-model".equals(route); }
    }

    private static final Map<Integer, Row> ROWS;
    static {
        Map<Integer, Row> rows = new LinkedHashMap<Integer, Row>();
        try (InputStream in = Native950NpcDrawnWeapons.class
                .getResourceAsStream("/native950/npcanim2-drawn-950.tsv")) {
            if (in == null) throw new IllegalStateException("Missing 950 drawn-weapon animation table");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            for (String line; (line = reader.readLine()) != null; ) {
                if (line.isEmpty() || line.charAt(0) == '#') continue;
                String[] p = line.split("\t", -1);
                if (p.length != 12) throw new IllegalStateException("Malformed 950 drawn-weapon row: " + line);
                int npcId = Integer.parseInt(p[0]);
                int struct = Integer.parseInt(p[2]);
                int attack = Integer.parseInt(p[3]);
                int block = Integer.parseInt(p[4]);
                int itemId = Integer.parseInt(p[5]);
                int model = Integer.parseInt(p[7]);
                if (npcId <= 0 || struct <= 0 || attack < 0 || attack > 65535 || block < 0 || block > 65535
                        || itemId <= 0 || model < 0)
                    throw new IllegalStateException("Out of range 950 drawn-weapon row: " + line);
                if (!"A".equals(p[8]) && !"B".equals(p[8]) && !"C".equals(p[8]))
                    throw new IllegalStateException("Unknown 950 drawn-weapon evidence class: " + line);
                if (!"drawn-model".equals(p[9]) && !"param2816+drawn-model".equals(p[9]))
                    throw new IllegalStateException("Unknown 950 drawn-weapon route: " + line);
                if (p[1].isEmpty() || p[6].isEmpty() || p[10].isEmpty() || p[11].isEmpty())
                    throw new IllegalStateException("950 drawn-weapon row without its evidence: " + line);
                if (Native950NpcAttackAnimations.row(npcId) != null)
                    throw new IllegalStateException("950 creature " + npcId + " already has a round-4 animation row");
                if (rows.put(Integer.valueOf(npcId), new Row(npcId, p[1], struct, attack, block, itemId,
                        p[6], model, p[8], p[9], p[10], p[11])) != null)
                    throw new IllegalStateException("Duplicate 950 drawn-weapon row for " + npcId);
            }
        } catch (IOException broken) {
            throw new IllegalStateException(broken);
        }
        if (rows.isEmpty()) throw new IllegalStateException("Empty 950 drawn-weapon animation table");
        ROWS = Collections.unmodifiableMap(rows);
    }

    private static volatile Object verifiedStore;
    private static volatile Properties pins;
    /** Creatures whose BLOCK sequence the render gate refuses; rebuilt with every verification. */
    private static volatile Map<Integer, Boolean> blockAccepted = Collections.emptyMap();

    /** Every shipped row, in table order. */
    public static Map<Integer, Row> rows() { return ROWS; }

    /** The row for this creature, or null when its weapon is not published this way. */
    public static Row row(int npcId) { return ROWS.get(Integer.valueOf(npcId)); }

    /** The creature's published attack sequence, or -1 when no row verifies one. */
    public static int attackFor(int npcId) {
        Row row = ROWS.get(Integer.valueOf(npcId));
        return row == null ? -1 : row.attackSequence;
    }

    /**
     * The creature's published block sequence, or -1 when no row verifies one <b>or</b> when the
     * render gate refuses that sequence on this creature. The refusal is decided during
     * {@link #verifyCacheBindings()}; before that has run for the current cache the block is
     * withheld rather than guessed.
     */
    public static int blockFor(int npcId) {
        Row row = ROWS.get(Integer.valueOf(npcId));
        if (row == null) return -1;
        Boolean accepted = blockAccepted.get(Integer.valueOf(npcId));
        return Boolean.TRUE.equals(accepted) ? row.blockSequence : -1;
    }

    /**
     * Re-derives every row from the running cache: the creature's raw index-18 definition, its
     * name, that it is still DRAWN with the model this row names, that it declares (class B) or
     * does not declare (classes A and C) the weapon struct in parameter 2816; the carrier item's
     * raw index-19 definition, its name, that the item still wears that same model and that its own
     * parameter 686 is this struct; the struct's raw index-22 file and its 2914/2917; and that both
     * sequences decode strictly to a positive duration. Finally the attack sequence must clear
     * {@link Native950NpcCombatAnimations#compatibleWithRender} for this creature's BAS - a failure
     * there is a startup failure, not a silent downgrade - and the block sequence's verdict is
     * recorded so {@link #blockFor(int)} can withhold it.
     */
    public static synchronized void verifyCacheBindings() {
        if (Cache.STORE == null || !Cache.isFlatReadOnly())
            throw new IllegalStateException("950 drawn-weapon animations require the paired read-only cache");
        if (verifiedStore == Cache.STORE) return;
        if (pins == null) {
            Properties loaded = new Properties();
            try (InputStream in = Native950NpcDrawnWeapons.class
                    .getResourceAsStream("/native950/npcanim2-950.properties")) {
                if (in == null) throw new IllegalStateException("Missing npcanim2-950 pins");
                loaded.load(in);
            } catch (IOException broken) { throw new IllegalStateException(broken); }
            pins = loaded;
        }
        Map<Integer, Boolean> blocks = new HashMap<Integer, Boolean>();
        for (Row row : ROWS.values()) {
            pinned("npc." + row.npcId, Cache.STORE.getIndexes()[18].getFile(row.npcId >>> 7, row.npcId & 127),
                    "creature definition " + row.npcId);
            NPCDefinitions npc = NPCDefinitions.getNPCDefinitions(row.npcId);
            if (npc == null || npc.name == null || !npc.name.trim().equals(row.npcName))
                throw new IllegalStateException("Renamed 950 creature " + row.npcId);
            if (!wears(npc.models, row.model))
                throw new IllegalStateException("950 creature " + row.npcId + " is no longer drawn with model "
                        + row.model);
            Object declared = npc.clientScriptData == null ? null
                    : npc.clientScriptData.get(Integer.valueOf(PARAM_WEAPON_STRUCT));
            if (row.declaresStruct()) {
                if (!(declared instanceof Integer) || ((Integer) declared).intValue() != row.struct)
                    throw new IllegalStateException("950 creature " + row.npcId + " no longer declares weapon struct "
                            + row.struct + " in parameter " + PARAM_WEAPON_STRUCT + "; got " + declared);
            } else if (declared != null) {
                // The creature grew a declared family: that is the stronger source and this row's
                // whole reason for existing has gone. Refuse rather than keep the weaker one.
                throw new IllegalStateException("950 creature " + row.npcId
                        + " now declares its own weapon family " + declared + "; the drawn-model row is superseded");
            }

            pinned("item." + row.itemId, Cache.STORE.getIndexes()[19].getFile(row.itemId >>> 8, row.itemId & 255),
                    "carrier item " + row.itemId);
            ItemDefinitions item = ItemDefinitions.getItemDefinitions(row.itemId);
            if (item == null || item.getName() == null || !item.getName().trim().equals(row.itemName))
                throw new IllegalStateException("Renamed 950 carrier item " + row.itemId);
            if (!wears(new int[] { item.maleEquip1, item.femaleEquip1, item.maleEquip2, item.femaleEquip2 }, row.model))
                throw new IllegalStateException("950 item " + row.itemId + " no longer wears model " + row.model);
            Object itemStruct = item.clientScriptData == null ? null
                    : item.clientScriptData.get(Integer.valueOf(PARAM_ITEM_STRUCT));
            if (!(itemStruct instanceof Integer) || ((Integer) itemStruct).intValue() != row.struct)
                throw new IllegalStateException("950 carrier item " + row.itemId + " no longer names weapon struct "
                        + row.struct + "; got " + itemStruct);

            pinned("struct." + row.struct, Cache.STORE.getIndexes()[22].getFile(row.struct / 32, row.struct & 31),
                    "weapon struct " + row.struct);
            requireStructSequence(row.struct, PARAM_ATTACK_SEQUENCE, row.attackSequence);
            requireStructSequence(row.struct, PARAM_BLOCK_SEQUENCE, row.blockSequence);

            for (int sequence : new int[] { row.attackSequence, row.blockSequence }) {
                byte[] raw = pinned("seq." + sequence,
                        Cache.STORE.getIndexes()[20].getFile(sequence >>> 7, sequence & 127), "sequence " + sequence);
                if (durationCycles(sequence, raw) < 1)
                    throw new IllegalStateException("950 sequence " + sequence + " no longer decodes to a duration");
            }

            if (!Native950NpcCombatAnimations.compatibleWithRender(npc.renderEmote, row.attackSequence))
                throw new IllegalStateException("950 creature " + row.npcId + " render " + npc.renderEmote
                        + " cannot take attack sequence " + row.attackSequence);
            blocks.put(Integer.valueOf(row.npcId), Boolean.valueOf(
                    Native950NpcCombatAnimations.compatibleWithRender(npc.renderEmote, row.blockSequence)));
        }
        blockAccepted = Collections.unmodifiableMap(blocks);
        verifiedStore = Cache.STORE;
    }

    /** How many rows would play their block sequence against the running cache. */
    public static int acceptedBlockCount() {
        int accepted = 0;
        for (Boolean value : blockAccepted.values()) if (Boolean.TRUE.equals(value)) accepted++;
        return accepted;
    }

    private static boolean wears(int[] models, int model) {
        if (models == null) return false;
        for (int candidate : models) if (candidate == model) return true;
        return false;
    }

    private static void requireStructSequence(int struct, int parameter, int expected) {
        RS3GeneralRequirementMap map = RS3GeneralRequirementMap.getMap(struct);
        if (map == null || map.getValues() == null)
            throw new IllegalStateException("950 struct " + struct + " is absent");
        Object published = map.getValues().get(Long.valueOf(parameter));
        if (!(published instanceof Integer) || ((Integer) published).intValue() != expected)
            throw new IllegalStateException("950 struct " + struct + " parameter " + parameter
                    + " is not " + expected + "; got " + published);
    }

    /** Total 20 ms client cycles of a sequence, or -1 when it does not decode. */
    static int durationCycles(int sequence, byte[] raw) {
        if (raw == null) return -1;
        try {
            AnimationDefinitions decoded = AnimationDefinitions.decodeStrict947(sequence, raw, null);
            long cycles = decoded.modernInt26b;
            if (decoded.anIntArray2153 != null) for (int duration : decoded.anIntArray2153) cycles += duration;
            return cycles <= 0 || cycles > Integer.MAX_VALUE ? -1 : (int) cycles;
        } catch (RuntimeException undecodable) { return -1; }
    }

    private static byte[] pinned(String key, byte[] raw, String what) {
        String pin = pins.getProperty(key);
        if (pin == null) throw new IllegalStateException("Missing npcanim2-950 pin " + key);
        if (raw == null || !pin.equals(hash(raw)))
            throw new IllegalStateException("Changed 950 " + what);
        return raw;
    }

    private static String hash(byte[] data) {
        try {
            StringBuilder out = new StringBuilder(64);
            for (byte b : MessageDigest.getInstance("SHA-256").digest(data)) out.append(String.format("%02x", b & 255));
            return out.toString();
        } catch (java.security.NoSuchAlgorithmException impossible) { throw new AssertionError(impossible); }
    }
}
