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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Stream {@code npcanim} (TOP50 round 4): the per-creature <b>attack</b> and <b>block</b> animation,
 * as data.
 *
 * <p>Creatures on this server have swung silently since the port began. The whole of the creature
 * attack animation is {@code Native950MeleeCombat}'s {@code profile.attackAnim}, which the
 * catalogue copies out of the inherited 910-era {@code data/npcs/combatDefs.json}; 536 of the 842
 * placed creatures that have a row at all carry {@code -1} there, deliberately, because the earlier
 * passes refused to guess a sequence and no source published one. That is what the user saw: a dark
 * wizard damaging them while standing perfectly still.</p>
 *
 * <h2>Where this revision publishes it</h2>
 *
 * <p>The 950 cache publishes the <b>type</b> of every parameter id: cache index 2, group 11 is the
 * {@code ParamType} table and its opcode 101 carries the {@link
 * com.rs.cache.loaders.ParamTypeDefinitions.ScriptVarType} id - 6 is {@code SEQ} ("ANIMATION"),
 * 73 is {@code STRUCT}. Asking that table about every parameter on all 7,822 attackable NPC
 * definitions answers the sourcing question the revision's own way:</p>
 *
 * <ul>
 *   <li><b>NPC parameter 2816 is typed {@code STRUCT}</b> and is carried by 463 attackable
 *       definitions. The struct ids it names are the same weapon-family structs a player's own
 *       equipped weapon resolves to through its item parameter 686 (14921 dagger, 14923 sword,
 *       14929 battleaxe, 14932 halberd, 14933 maul, 14935 whip, 14937 staff, 14939/14940 bow,
 *       14941 crossbow).</li>
 *   <li><b>Struct parameter 2914 is typed {@code ANIMATION}</b>, with 2917 the block sequence -
 *       the same pair a player attacking with that weapon plays.</li>
 * </ul>
 *
 * <p>So a creature that carries 2816 is naming the weapon family it fights with, and that family's
 * struct publishes the sequence a <i>player</i> holding the same weapon already swings with. A row
 * ships only where the RS3 wiki independently names that weapon on the creature's own page and the
 * cache item of exactly that name resolves to a struct publishing the same two sequences.</p>
 *
 * <p><b>Dark wizards ship nothing.</b> All ten attackable ids - 172, 174, 3242, 3243, 3244, 3245,
 * 8871, <b>8872</b>, 8873 and 8874, 8872 being the one the live capture shows the user fighting at
 * the Varrock stone circle - carry no parameter typed {@code ANIMATION} and none typed
 * {@code STRUCT}; 172's group renders through BAS 2699 and 8871-8874 through BAS 2698, and both
 * publish only stand, walk and run; the wiki page publishes no animation id. A documented refusal,
 * not a guess. (Three <i>unplaced</i> definitions also named "Dark wizard" - 29292, 29293 "elder"
 * and 29294 "master" - do name struct 14938; nothing is shipped for them either, and the refusal
 * is about the dark wizards a player can meet, not about the name.)</p>
 *
 * <p>Table: {@code resources/native950/npcanim-attacks-950.tsv}.
 * Pins: {@code resources/native950/npcanim-950.properties}.
 * Adopted from Artaven's decoder-and-wiki-evidenced table (Stage B Phase 4); the shipped table,
 * pins and verification below are unchanged from their source. See
 * docs/STAGE-B-PHASE4-NPC-ANIMATIONS-20260926.md.</p>
 */
public final class Native950NpcAttackAnimations {

    private Native950NpcAttackAnimations() { }

    /** NPC parameter naming the creature's weapon struct; the cache types it STRUCT. */
    public static final int PARAM_WEAPON_STRUCT = 2816;
    /** Struct parameter holding the attack sequence; the cache types it ANIMATION. */
    public static final int PARAM_ATTACK_SEQUENCE = 2914;
    /** Struct parameter holding the block sequence; the cache types it ANIMATION. */
    public static final int PARAM_BLOCK_SEQUENCE = 2917;
    /** Item parameter naming a weapon's struct - the field the shipped player weapon tables use. */
    public static final int PARAM_ITEM_STRUCT = 686;

    /** One shipped creature row. */
    public static final class Row {
        public final int npcId, struct, attackSequence, blockSequence, itemId;
        public final String npcName, itemName, wikiPage, wikiEvidence;
        Row(int npcId, String npcName, int struct, int attackSequence, int blockSequence,
                int itemId, String itemName, String wikiPage, String wikiEvidence) {
            this.npcId = npcId; this.npcName = npcName; this.struct = struct;
            this.attackSequence = attackSequence; this.blockSequence = blockSequence;
            this.itemId = itemId; this.itemName = itemName;
            this.wikiPage = wikiPage; this.wikiEvidence = wikiEvidence;
        }
    }

    private static final Map<Integer, Row> ROWS;
    static {
        Map<Integer, Row> rows = new LinkedHashMap<Integer, Row>();
        try (InputStream in = Native950NpcAttackAnimations.class
                .getResourceAsStream("/native950/npcanim-attacks-950.tsv")) {
            if (in == null) throw new IllegalStateException("Missing 950 NPC attack animation table");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            for (String line; (line = reader.readLine()) != null; ) {
                if (line.isEmpty() || line.charAt(0) == '#') continue;
                String[] p = line.split("\t", -1);
                if (p.length != 9) throw new IllegalStateException("Malformed 950 NPC attack animation row: " + line);
                int npcId = Integer.parseInt(p[0]);
                int struct = Integer.parseInt(p[2]);
                int attack = Integer.parseInt(p[3]);
                int block = Integer.parseInt(p[4]);
                int itemId = Integer.parseInt(p[5]);
                if (npcId <= 0 || struct <= 0 || attack < 0 || attack > 65535 || block < 0 || block > 65535
                        || itemId <= 0)
                    throw new IllegalStateException("Out of range 950 NPC attack animation row: " + line);
                if (p[1].isEmpty() || p[6].isEmpty() || p[7].isEmpty() || p[8].isEmpty())
                    throw new IllegalStateException("950 NPC attack animation row without its evidence: " + line);
                if (rows.put(Integer.valueOf(npcId),
                        new Row(npcId, p[1], struct, attack, block, itemId, p[6], p[7], p[8])) != null)
                    throw new IllegalStateException("Duplicate 950 NPC attack animation row for " + npcId);
            }
        } catch (IOException broken) {
            throw new IllegalStateException(broken);
        }
        if (rows.isEmpty()) throw new IllegalStateException("Empty 950 NPC attack animation table");
        ROWS = Collections.unmodifiableMap(rows);
    }

    private static volatile Object verifiedStore;
    private static volatile Properties pins;

    /** Every shipped row, in table order. */
    public static Map<Integer, Row> rows() { return ROWS; }

    /** The row for this creature, or null when nothing publishes its animations. */
    public static Row row(int npcId) { return ROWS.get(Integer.valueOf(npcId)); }

    /**
     * The creature's published attack sequence, or -1 when no row verifies one. This is the
     * preview-only fallback in Shnorkscape; an existing authored preview attack takes precedence.
     */
    public static int attackFor(int npcId) {
        Row row = ROWS.get(Integer.valueOf(npcId));
        // Stream npcanim2 (round 5): a creature that declares no weapon family still publishes its
        // weapon through the model it is DRAWN holding. That table is consulted only where this one
        // has nothing, so a creature's own declared family always wins.
        return row == null ? Native950NpcDrawnWeapons.attackFor(npcId) : row.attackSequence;
    }

    /** The creature's published block sequence, or -1 when no row verifies one. */
    public static int blockFor(int npcId) {
        Row row = ROWS.get(Integer.valueOf(npcId));
        return row == null ? Native950NpcDrawnWeapons.blockFor(npcId) : row.blockSequence;
    }

    /**
     * Re-reads every definition the table depends on against the running cache: the creature's raw
     * index-18 definition and its name, that it still carries parameter 2816 naming this struct,
     * the struct's raw index-22 file and its 2914/2917, the carrier item's raw index-19 definition
     * and its name, that the item's own parameter 686 resolves to a struct publishing the same two
     * sequences, and that both sequences still decode strictly with a positive duration. If any of
     * them moved, the row has lost the evidence that put it there and the feature refuses to start
     * rather than play a sequence it can no longer justify.
     */
    public static synchronized void verifyCacheBindings() {
        if (Cache.STORE == null || !Cache.isFlatReadOnly())
            throw new IllegalStateException("950 NPC attack animations require the paired read-only cache");
        if (verifiedStore == Cache.STORE) return;
        if (pins == null) {
            Properties loaded = new Properties();
            try (InputStream in = Native950NpcAttackAnimations.class
                    .getResourceAsStream("/native950/npcanim-950.properties")) {
                if (in == null) throw new IllegalStateException("Missing npcanim-950 pins");
                loaded.load(in);
            } catch (IOException broken) { throw new IllegalStateException(broken); }
            pins = loaded;
        }
        for (Row row : ROWS.values()) {
            pinned("npc." + row.npcId, Cache.STORE.getIndexes()[18].getFile(row.npcId >>> 7, row.npcId & 127),
                    "creature definition " + row.npcId);
            NPCDefinitions npc = NPCDefinitions.getNPCDefinitions(row.npcId);
            if (npc == null || npc.name == null || !npc.name.trim().equals(row.npcName))
                throw new IllegalStateException("Renamed 950 creature " + row.npcId);
            Object pointer = npc.clientScriptData == null ? null
                    : npc.clientScriptData.get(Integer.valueOf(PARAM_WEAPON_STRUCT));
            if (!(pointer instanceof Integer) || ((Integer) pointer).intValue() != row.struct)
                throw new IllegalStateException("950 creature " + row.npcId + " no longer names weapon struct "
                        + row.struct + " in parameter " + PARAM_WEAPON_STRUCT + "; got " + pointer);

            pinned("struct." + row.struct, Cache.STORE.getIndexes()[22].getFile(row.struct / 32, row.struct & 31),
                    "weapon struct " + row.struct);
            requireSequences(row.struct, row.attackSequence, row.blockSequence);

            pinned("item." + row.itemId, Cache.STORE.getIndexes()[19].getFile(row.itemId >>> 8, row.itemId & 255),
                    "carrier item " + row.itemId);
            ItemDefinitions item = ItemDefinitions.getItemDefinitions(row.itemId);
            if (item == null || item.getName() == null || !item.getName().trim().equals(row.itemName))
                throw new IllegalStateException("Renamed 950 carrier item " + row.itemId);
            Object itemStruct = item.clientScriptData == null ? null
                    : item.clientScriptData.get(Integer.valueOf(PARAM_ITEM_STRUCT));
            if (!(itemStruct instanceof Integer))
                throw new IllegalStateException("950 carrier item " + row.itemId + " has no weapon struct");
            // The wiki's named weapon must publish the SAME pair. It is normally the creature's own
            // struct; Speedy Keith's strykebow is struct 14939 while the creature names 14940, and
            // both publish 37462/18295, which is why the pair is compared rather than the id.
            requireSequences(((Integer) itemStruct).intValue(), row.attackSequence, row.blockSequence);

            for (int sequence : new int[] {row.attackSequence, row.blockSequence}) {
                byte[] raw = pinned("seq." + sequence,
                        Cache.STORE.getIndexes()[20].getFile(sequence >>> 7, sequence & 127), "sequence " + sequence);
                if (durationCycles(sequence, raw) < 1)
                    throw new IllegalStateException("950 sequence " + sequence + " no longer decodes to a duration");
            }
        }
        verifiedStore = Cache.STORE;
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

    private static void requireSequences(int struct, int attack, int block) {
        RS3GeneralRequirementMap map = RS3GeneralRequirementMap.getMap(struct);
        if (map == null || map.getValues() == null)
            throw new IllegalStateException("950 struct " + struct + " is absent");
        Object published = map.getValues().get(Long.valueOf(PARAM_ATTACK_SEQUENCE));
        if (!(published instanceof Integer) || ((Integer) published).intValue() != attack)
            throw new IllegalStateException("950 struct " + struct + " parameter " + PARAM_ATTACK_SEQUENCE
                    + " is not " + attack + "; got " + published);
        Object blocking = map.getValues().get(Long.valueOf(PARAM_BLOCK_SEQUENCE));
        if (!(blocking instanceof Integer) || ((Integer) blocking).intValue() != block)
            throw new IllegalStateException("950 struct " + struct + " parameter " + PARAM_BLOCK_SEQUENCE
                    + " is not " + block + "; got " + blocking);
    }

    private static byte[] pinned(String key, byte[] raw, String what) {
        String pin = pins.getProperty(key);
        if (pin == null) throw new IllegalStateException("Missing npcanim-950 pin " + key);
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
