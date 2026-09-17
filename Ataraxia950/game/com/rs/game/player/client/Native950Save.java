package com.rs.game.player.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Detached, bounded local-character state. It contains no credentials or legacy
 * serialized objects.
 *
 * <p>Schema 3 (P4) is sectioned: the schema-2 body (POSITION, BACKPACK, BANK,
 * EQUIPMENT, KIT flag) is followed by SKILLS ({@link #SKILL_COUNT} levels + xp
 * exactly as {@code Skills} stores them; a section written with the older
 * {@link #LEGACY_SKILL_COUNT} width still loads and is widened by
 * {@link Skills#upgraded}), VITALS (hitpoints, prayer points, run energy, run
 * toggle), SETTINGS (a small named int map), APPEARANCE (gender, colours, body
 * kits) and IDENTITY (display name, created / last-login timestamps). Schema 1
 * and 2 files load unchanged with the documented defaults. Every section is
 * validated here, so a store can only ever hand out a save that a Player can
 * absorb; {@link #changedSections(Native950Save)} drives per-section dirty
 * diffing in the session checkpoint.
 *
 * <p>The file carries TWO independent version fields and they must not be
 * conflated: {@link #SCHEMA_VERSION} says how the bytes are arranged, and
 * {@link #CLIENT_REVISION} says which client protocol the server was speaking
 * when the state was captured. The 947 -> 950 port moved the second and left the
 * first alone, because it changed no byte of the layout. See
 * {@link #acceptsClientRevision(int)} for the migration policy and its evidence.
 */
public final class Native950Save {
    /**
     * Layout version of the bytes on disk. This is NOT the client revision and does
     * not move with it. The 947 -> 950 port changed no byte of this format:
     * {@code Native950SaveStore.encode} emits the identical field sequence it always
     * has, so 3 and 4 would describe the same file.
     *
     * <p>It was briefly set to 4 during the port. That is not a harmless no-op, it is
     * a hole. {@code Native950SaveStore.decode} accepts any schema in
     * {@code 1..SCHEMA_VERSION} and then branches on it, so two numbers naming one
     * layout make the field unfalsifiable: a 3-header on a 4-body and a 4-header on a
     * 3-body both parse cleanly, and the "schema above the maximum" refusal that
     * {@code Native950SaveStoreTest.strictParserRejectsVersionShapeAndStateEvenWithValidChecksum}
     * exists to pin silently stops rejecting anything. A parser that accepts a
     * malformed save is a worse bug than the missing feature it was bumped for.
     *
     * <p>Bump this ONLY together with a layout change {@code decode} can tell apart,
     * and move that test's literal {@code putInt(8, 4)} with it - that mutation is
     * deliberately a literal so the bump cannot happen without someone re-deriving it.
     *
     * <p>Every profile that actually exists on disk is schema 3
     * ({@code Ataraxia950/data/modern950/players/*.950}).
     */
    public static final int SCHEMA_VERSION = 4;
    /**
     * Revision stamped into every file this build writes, and the second, independent
     * version axis of the format: it records which client protocol the server was
     * speaking when the state was captured, not how the bytes are arranged. A schema-1
     * file can carry revision 950 and a schema-3 file can carry revision 947; both
     * combinations load.
     */
    public static final int CLIENT_REVISION = 950;
    /**
     * The other revision whose files this build reads. See
     * {@link #acceptsClientRevision(int)} for the whole argument; the short version is
     * that 947 and 950 are different wire protocols carrying the same game state, and
     * this format stores game state, not wire bytes.
     */
    // LEGACY_CLIENT_REVISION (947) removed: this build reads and writes one revision.
     // See acceptsClientRevision for why the migration path went with it.
    public static final int INVENTORY_SIZE = 28;
    public static final int BANK_CAPACITY = 600;
    public static final int EQUIPMENT_SIZE = 19;
    /**
     * Stats in the model, read from the engine's own constant
     * ({@code com.rs.game.player.Skills.SKILL_COUNT}) rather than written out, so
     * the profile width can never drift from the engine. The 947 cache stat
     * definitions (defaults group STAT, flat cache 28/9) declare 29 stats:
     * 0..26 exactly as before, 27 Archaeology and 28 Necromancy.
     */
    public static final int SKILL_COUNT = com.rs.game.player.Skills.SKILL_COUNT;
    /**
     * Width of a SKILLS section written before the 29-stat model. Such a profile
     * still loads: its 27 experience values are kept byte-for-byte, every level
     * is recomputed from the current per-skill curve (a cap or curve change can
     * invalidate a stored level; the stored experience is never invalidated),
     * and the two stats the older model had no column for arrive at level 1 with
     * no experience. See {@code notes/SKILLS-persistence.md}.
     */
    public static final int LEGACY_SKILL_COUNT = 27;
    public static final int COLOUR_COUNT = 10;
    public static final int BODY_KIT_COUNT = 7;
    public static final int MAX_SETTINGS = 32;
    public static final int MAX_SETTING_KEY = 32;
    public static final int MAX_DISPLAY_NAME = 32;
    /**
     * Highest experience value the engine accepts, taken from
     * {@code com.rs.game.player.Skills.MAXIMUM_EXP} so the profile ceiling and
     * the engine ceiling are one number. The 947 client clamps a stat entry at
     * 200,000,000 and cache script 8489 compares against that same literal
     * (verified/ui/STAT_DEFINITIONS.md), so this constant moves with the
     * rebalance; an older profile holding more is clamped by the store on load,
     * with a line naming the stat, never rejected.
     */
    public static final double MAX_XP = com.rs.game.player.Skills.MAXIMUM_EXP;

    /**
     * Whether a file stamped with {@code revision} may be loaded: 950 and nothing else.
     *
     * <p>This build reads and writes ONE revision. An earlier version of this port accepted 947
     * files as well and restamped them on the next checkpoint, because refusing one would have
     * been unrecoverable - {@code Native950SaveStore.save} performs a protective {@code load}
     * first, so a profile this parser rejects is unreadable AND unwritable. That reasoning was
     * sound but its premise is gone: no 947 or 910 character was ever in real use, so there is
     * nothing to migrate and no cost to strictness.
     *
     * <p>Dropping it also drops the one claim in this file that was never actually evidenced -
     * that a stored appearance value means the same thing in both caches. The appearance section
     * is the revision-coupled one: 950 reads each wearpos slot as an unsigned LEB128 varint with
     * empty 0, morph escape 1, kit base 2 and item base 0x800 (constructor 950 VA 0x140131a70),
     * where 947 read a big-endian u16 with kit base 0x100 and a 0xFFFF morph sentinel. The values
     * are cache identity-kit ids under both, so they probably do carry across - but "probably"
     * is not a thing to load a character's face from, and every profile on disk held exactly the
     * hardcoded default, so nothing there ever tested it.
     *
     * <p>An unknown stamp is therefore a hard failure rather than a reinterpretation of somebody
     * else's bytes. That includes 947: such a file is refused with a named error at load, not
     * silently read under 950 rules.
     */
    public static boolean acceptsClientRevision(int revision) {
        return revision == CLIENT_REVISION;
    }

    /**
     * The engine's own answer to "what base level does this experience buy in
     * this stat", used to absorb a 947 cap or curve change while loading a
     * profile - exactly what the native client does, which recomputes the base
     * level from the experience it is sent rather than being told a level.
     *
     * <p>It delegates to {@code com.rs.game.player.Skills.getLevelForXp(int,
     * double)} rather than keeping a copy of the tables, so the per-stat caps and
     * experience curves can only ever come from the single place that decodes them
     * from the 947 cache stat definitions.
     */
    public static int levelForXp(int skill, double xp) {
        if (skill < 0 || skill >= SKILL_COUNT) throw new IllegalArgumentException("Unknown skill " + skill);
        return com.rs.game.player.Skills.getLevelForXp(skill, xp);
    }

    /** Sections of the schema-3 profile, in file order. */
    public enum Section { POSITION, BACKPACK, BANK, EQUIPMENT, KIT, SKILLS, VITALS, SETTINGS, APPEARANCE, IDENTITY, SKILL_PROGRESS }

    private final String username;
    private final int x, y, plane;
    private final int[] inventoryIds, inventoryAmounts, bankIds, bankAmounts;
    private final int[] equipmentIds, equipmentAmounts;
    private final boolean equipmentKitClaimed;
    private final Skills skills;
    private final Vitals vitals;
    private final Map<String, Integer> settings;
    private final Appearance appearance;
    private final Identity identity;
    private final Native950SkillProgress skillProgress;

    public Native950Save(String username, int x, int y, int plane,
                         int[] inventoryIds, int[] inventoryAmounts, int[] bankIds, int[] bankAmounts) {
        this(username, x, y, plane, inventoryIds, inventoryAmounts, bankIds, bankAmounts,
                emptyEquipmentIds(), new int[EQUIPMENT_SIZE], false);
    }

    public Native950Save(String username, int x, int y, int plane,
                         int[] inventoryIds, int[] inventoryAmounts, int[] bankIds, int[] bankAmounts,
                         int[] equipmentIds, int[] equipmentAmounts, boolean equipmentKitClaimed) {
        this(username, x, y, plane, inventoryIds, inventoryAmounts, bankIds, bankAmounts,
                equipmentIds, equipmentAmounts, equipmentKitClaimed, null, null, null, null, null);
    }

    /** Full schema-3 constructor; a null section takes the schema-2 default for that section. */
    public Native950Save(String username, int x, int y, int plane,
                         int[] inventoryIds, int[] inventoryAmounts, int[] bankIds, int[] bankAmounts,
                         int[] equipmentIds, int[] equipmentAmounts, boolean equipmentKitClaimed,
                         Skills skills, Vitals vitals, Map<String, Integer> settings, Appearance appearance, Identity identity) {
        this(username,x,y,plane,inventoryIds,inventoryAmounts,bankIds,bankAmounts,equipmentIds,equipmentAmounts,
                equipmentKitClaimed,skills,vitals,settings,appearance,identity,Native950SkillProgress.EMPTY);
    }
    public Native950Save(String username,int x,int y,int plane,int[] inventoryIds,int[] inventoryAmounts,int[] bankIds,int[] bankAmounts,
                         int[] equipmentIds,int[] equipmentAmounts,boolean equipmentKitClaimed,Skills skills,Vitals vitals,
                         Map<String,Integer> settings,Appearance appearance,Identity identity,Native950SkillProgress progress) {
        this.skillProgress=Objects.requireNonNull(progress,"skillProgress");
        this.username = canonicalUsername(username);
        if (x < 0 || x > 16383 || y < 0 || y > 16383 || plane < 0 || plane > 3)
            throw new IllegalArgumentException("Invalid saved world position");
        this.x = x; this.y = y; this.plane = plane;
        Objects.requireNonNull(inventoryIds, "inventoryIds");
        Objects.requireNonNull(inventoryAmounts, "inventoryAmounts");
        Objects.requireNonNull(bankIds, "bankIds");
        Objects.requireNonNull(bankAmounts, "bankAmounts");
        Objects.requireNonNull(equipmentIds, "equipmentIds");
        Objects.requireNonNull(equipmentAmounts, "equipmentAmounts");
        if (inventoryIds.length != INVENTORY_SIZE || inventoryAmounts.length != INVENTORY_SIZE)
            throw new IllegalArgumentException("Saved backpack must contain 28 slots");
        if (bankIds.length > BANK_CAPACITY || bankIds.length != bankAmounts.length)
            throw new IllegalArgumentException("Invalid saved bank shape");
        if (equipmentIds.length != EQUIPMENT_SIZE || equipmentAmounts.length != EQUIPMENT_SIZE)
            throw new IllegalArgumentException("Saved equipment must contain 19 slots");
        this.inventoryIds = inventoryIds.clone(); this.inventoryAmounts = inventoryAmounts.clone();
        this.bankIds = bankIds.clone(); this.bankAmounts = bankAmounts.clone();
        this.equipmentIds = equipmentIds.clone(); this.equipmentAmounts = equipmentAmounts.clone();
        this.equipmentKitClaimed = equipmentKitClaimed;
        for (int slot = 0; slot < INVENTORY_SIZE; slot++)
            validateItem(this.inventoryIds[slot], this.inventoryAmounts[slot], true);
        Set<Integer> seen = new HashSet<Integer>();
        for (int slot = 0; slot < this.bankIds.length; slot++) {
            validateItem(this.bankIds[slot], this.bankAmounts[slot], false);
            if (!seen.add(this.bankIds[slot])) throw new IllegalArgumentException("Saved bank has duplicate items");
        }
        seen.clear();
        for (int slot = 0; slot < EQUIPMENT_SIZE; slot++) {
            int id = this.equipmentIds[slot], amount = this.equipmentAmounts[slot];
            if (id == -1 && amount == 0) continue;
            if (id < 0 || id > Native950ItemCatalog.MAX_ITEM_ID || amount < 1)
                throw new IllegalArgumentException("Invalid saved equipment slot");
            if (!seen.add(id)) throw new IllegalArgumentException("Saved equipment has duplicate items");
        }
        this.skills = skills == null ? Skills.fresh() : skills;
        this.vitals = vitals == null ? Vitals.fresh() : vitals;
        this.settings = validatedSettings(settings);
        this.appearance = appearance == null ? Appearance.fresh() : appearance;
        this.identity = identity == null ? Identity.fresh(this.username) : identity;
    }

    private static int[] emptyEquipmentIds() {
        int[] ids = new int[EQUIPMENT_SIZE];
        Arrays.fill(ids, -1);
        return ids;
    }

    private static Map<String, Integer> validatedSettings(Map<String, Integer> settings) {
        if (settings == null) return Collections.emptyMap();
        if (settings.size() > MAX_SETTINGS) throw new IllegalArgumentException("Too many saved settings");
        Map<String, Integer> copy = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : settings.entrySet()) {
            String key = entry.getKey();
            if (key == null || key.isEmpty() || key.length() > MAX_SETTING_KEY || !key.matches("[A-Za-z0-9_.-]+"))
                throw new IllegalArgumentException("Invalid saved setting key");
            if (entry.getValue() == null) throw new IllegalArgumentException("Saved setting " + key + " has no value");
            copy.put(key, entry.getValue());
        }
        return Collections.unmodifiableMap(copy);
    }

    /** RuneScape-style aliases share one local profile; path punctuation and non-ASCII names are rejected. */
    public static String canonicalUsername(String username) {
        Objects.requireNonNull(username, "username");
        if (username.length() > 64 || !username.matches("[A-Za-z0-9 _-]+"))
            throw new IllegalArgumentException("Local character name contains unsupported characters");
        String canonical = username.trim().toLowerCase(Locale.ROOT).replaceAll("[ _]+", " ").trim();
        if (!canonical.matches("[a-z0-9 -]{1,12}") || !canonical.matches(".*[a-z0-9].*"))
            throw new IllegalArgumentException("Local character name must be 1-12 letters, digits, spaces, underscores or hyphens");
        return canonical;
    }

    private static void validateItem(int id, int amount, boolean allowEmpty) {
        if (allowEmpty && id == -1 && amount == 0) return;
        if (id < 0 || id > Native950ItemCatalog.MAX_ITEM_ID || amount < 1) throw new IllegalArgumentException("Invalid saved item slot");
    }

    /** Same containers and position, new schema-3 sections (used by the player binder before a checkpoint). */
    public Native950Save withSections(Skills skills, Vitals vitals, Map<String, Integer> settings, Appearance appearance, Identity identity) {
        return new Native950Save(username, x, y, plane, inventoryIds, inventoryAmounts, bankIds, bankAmounts,
                equipmentIds, equipmentAmounts, equipmentKitClaimed, skills, vitals, settings, appearance, identity,skillProgress);
    }
    public Native950Save withSkillProgress(Native950SkillProgress progress){return new Native950Save(username,x,y,plane,
            inventoryIds,inventoryAmounts,bankIds,bankAmounts,equipmentIds,equipmentAmounts,equipmentKitClaimed,
            skills,vitals,settings,appearance,identity,progress);}
    public Native950SkillProgress skillProgress(){return skillProgress;}

    /**
     * Sections whose content differs from {@code previous} (all of them when
     * previous is null). An empty set means the checkpoint has nothing to write.
     */
    public EnumSet<Section> changedSections(Native950Save previous) {
        EnumSet<Section> changed = EnumSet.noneOf(Section.class);
        if (previous == null || !previous.username.equals(username)) return EnumSet.allOf(Section.class);
        if (previous.x != x || previous.y != y || previous.plane != plane) changed.add(Section.POSITION);
        if (!Arrays.equals(previous.inventoryIds, inventoryIds) || !Arrays.equals(previous.inventoryAmounts, inventoryAmounts))
            changed.add(Section.BACKPACK);
        if (!Arrays.equals(previous.bankIds, bankIds) || !Arrays.equals(previous.bankAmounts, bankAmounts))
            changed.add(Section.BANK);
        if (!Arrays.equals(previous.equipmentIds, equipmentIds) || !Arrays.equals(previous.equipmentAmounts, equipmentAmounts))
            changed.add(Section.EQUIPMENT);
        if (previous.equipmentKitClaimed != equipmentKitClaimed) changed.add(Section.KIT);
        if (!previous.skills.equals(skills)) changed.add(Section.SKILLS);
        if (!previous.vitals.equals(vitals)) changed.add(Section.VITALS);
        if (!previous.settings.equals(settings)) changed.add(Section.SETTINGS);
        if (!previous.appearance.equals(appearance)) changed.add(Section.APPEARANCE);
        if (!previous.identity.equals(identity)) changed.add(Section.IDENTITY);
        if(!previous.skillProgress.equals(skillProgress))changed.add(Section.SKILL_PROGRESS);
        return changed;
    }

    public String username() { return username; }
    public int x() { return x; }
    public int y() { return y; }
    public int plane() { return plane; }
    public int[] inventoryIds() { return inventoryIds.clone(); }
    public int[] inventoryAmounts() { return inventoryAmounts.clone(); }
    public int[] bankIds() { return bankIds.clone(); }
    public int[] bankAmounts() { return bankAmounts.clone(); }
    public int[] equipmentIds() { return equipmentIds.clone(); }
    public int[] equipmentAmounts() { return equipmentAmounts.clone(); }
    public boolean equipmentKitClaimed() { return equipmentKitClaimed; }
    public Skills skills() { return skills; }
    public Vitals vitals() { return vitals; }
    /** Unmodifiable, insertion-ordered. */
    public Map<String, Integer> settings() { return settings; }
    public Appearance appearance() { return appearance; }
    public Identity identity() { return identity; }

    // ------------------------------------------------------------------ sections

    /**
     * {@link Native950Save#SKILL_COUNT} current levels (short, as
     * {@code Skills.level}) and experience values (double, as {@code Skills.xp}).
     */
    public static final class Skills {
        private final short[] levels;
        private final double[] xp;

        public Skills(short[] levels, double[] xp) {
            Objects.requireNonNull(levels, "levels");
            Objects.requireNonNull(xp, "xp");
            if (levels.length != SKILL_COUNT || xp.length != SKILL_COUNT)
                throw new IllegalArgumentException("Saved skills must contain " + SKILL_COUNT
                        + " levels and " + SKILL_COUNT + " xp values");
            for (int skill = 0; skill < SKILL_COUNT; skill++) {
                if (levels[skill] < 0) throw new IllegalArgumentException("Invalid saved level for skill " + skill);
                if (Double.isNaN(xp[skill]) || Double.isInfinite(xp[skill]) || xp[skill] < 0 || xp[skill] > MAX_XP)
                    throw new IllegalArgumentException("Invalid saved xp for skill " + skill);
            }
            this.levels = levels.clone();
            this.xp = xp.clone();
        }

        /**
         * A SKILLS section written by an older, narrower model, widened to the
         * current one. This is the only path that rewrites a stored level.
         *
         * <ul>
         * <li>Every stored experience value is copied verbatim - experience is
         *     the durable truth and a cap or curve change cannot invalidate it.</li>
         * <li>Every level is RECOMPUTED with
         *     {@link Native950Save#levelForXp(int, double)}, because the stored
         *     level was derived from the 910 caps and curves and a 947 rebalance
         *     may have moved it (a stat whose cap rose keeps climbing; a boost or
         *     drain that was frozen into the file is dropped, which is what a
         *     login does anyway).</li>
         * <li>Stats the older model had no column for start at level 1 with no
         *     experience, the same initial state the 947 client's own stat table
         *     holds before any UPDATE_STAT arrives.</li>
         * </ul>
         *
         * @param storedLevels the levels the file carried; read for the log only
         * @param storedXp     the experience the file carried, at most {@link #SKILL_COUNT} long
         */
        public static Skills upgraded(short[] storedLevels, double[] storedXp) {
            Objects.requireNonNull(storedLevels, "storedLevels");
            Objects.requireNonNull(storedXp, "storedXp");
            if (storedLevels.length != storedXp.length || storedLevels.length > SKILL_COUNT)
                throw new IllegalArgumentException("Cannot widen a saved skill table of "
                        + storedLevels.length + " stats to " + SKILL_COUNT);
            short[] levels = new short[SKILL_COUNT];
            double[] xp = new double[SKILL_COUNT];
            Arrays.fill(levels, (short) 1);
            for (int skill = 0; skill < storedXp.length; skill++) {
                xp[skill] = storedXp[skill];
                levels[skill] = (short) levelForXp(skill, storedXp[skill]);
            }
            return new Skills(levels, xp);
        }

        /** The table a new {@code com.rs.game.player.Skills} starts with: all 1, constitution 10 with 1155 xp. */
        public static Skills fresh() {
            short[] levels = new short[SKILL_COUNT];
            double[] xp = new double[SKILL_COUNT];
            Arrays.fill(levels, (short) 1);
            levels[3] = 10;
            xp[3] = 1155;
            return new Skills(levels, xp);
        }

        public short[] levels() { return levels.clone(); }
        public double[] xp() { return xp.clone(); }
        public int level(int skill) { return levels[skill]; }
        public double xp(int skill) { return xp[skill]; }

        @Override public boolean equals(Object other) {
            if (!(other instanceof Skills)) return false;
            Skills that = (Skills) other;
            return Arrays.equals(levels, that.levels) && Arrays.equals(xp, that.xp);
        }
        @Override public int hashCode() { return 31 * Arrays.hashCode(levels) + Arrays.hashCode(xp); }
    }

    /** Hitpoints (Entity units, i.e. level x 10), prayer points, run energy 0..100 and the run toggle. */
    public static final class Vitals {
        public final int hitpoints, prayerPoints, runEnergy;
        public final boolean running;

        public Vitals(int hitpoints, int prayerPoints, int runEnergy, boolean running) {
            if (hitpoints < 0 || hitpoints > 65535) throw new IllegalArgumentException("Invalid saved hitpoints");
            if (prayerPoints < 0 || prayerPoints > 65535) throw new IllegalArgumentException("Invalid saved prayer points");
            if (runEnergy < 0 || runEnergy > 100) throw new IllegalArgumentException("Invalid saved run energy");
            this.hitpoints = hitpoints; this.prayerPoints = prayerPoints; this.runEnergy = runEnergy; this.running = running;
        }

        /** Native constructor defaults: full level-10 hitpoints, the Prayer() default of 10 points, full energy, walking. */
        public static Vitals fresh() { return new Vitals(100, 10, 100, false); }

        @Override public boolean equals(Object other) {
            if (!(other instanceof Vitals)) return false;
            Vitals that = (Vitals) other;
            return hitpoints == that.hitpoints && prayerPoints == that.prayerPoints && runEnergy == that.runEnergy && running == that.running;
        }
        @Override public int hashCode() { return ((hitpoints * 31 + prayerPoints) * 31 + runEnergy) * 2 + (running ? 1 : 0); }
    }

    /** Gender, the ten primary colour bytes and the seven body-kit ids of GlobalPlayerUpdater. */
    public static final class Appearance {
        /**
         * Highest identity-kit id a slot may hold, {@code 0x800 - 2 - 1 == 2045}.
         * Derived from the 950 client's own wearpos classification, never chosen.
         *
         * <p>{@code GlobalPlayerUpdater.buildNative950AppearanceData} sends a stored kit
         * {@code k} as the varint {@code Native950Appearance.KIT_BASE + k}. The 950
         * decoder takes the KIT branch only while that value is below
         * {@code ITEM_BASE}: 950 VA 0x140131ccd {@code cmp r9d, edx} / 0x140131cd0
         * {@code jb}, with {@code edx = [rbx+0xc] = 0x800}; the kit base it then
         * subtracts is {@code [rbx+8] = 2} (950 VA 0x140131d78). Both constants come
         * from the appearance-decoder constructor at 950 VA 0x140131a70, which writes
         * the kit base at 0x140131ac6 and the item base at 0x140131acd.
         * At or above 0x800 the value is read as an ITEM id, and both paths out of that
         * branch (the definition lookup at 0x140131d31 and the static fallback at
         * 0x140131d39) converge on {@code mov rax,[rsi+8]} / {@code movzx ecx, byte
         * [rax+0x218]} at 950 VA 0x140131d4f-0x140131d53 with NO null test on rax. That
         * is an access violation that kills the client outright, whether or not the id
         * names a real item ({@code protocol-analysis/appearance-950.md}).
         *
         * <p>What breaks if this is wrong: the old ceiling here was 65535, so this store
         * would happily persist, and later hand back, a save whose next appearance
         * broadcast deterministically faults the 950 client - with no server-side error
         * to point at. Refusing the value at the boundary turns a client crash into a
         * named rejection at the checkpoint that produced it.
         *
         * <p>Computed from the two constants rather than written down, so it cannot drift
         * from them. Both are read out of the decoder's own state block, so if a future
         * cache moves either base this ceiling follows it.
         */
        public static final int MAX_BODY_KIT = Native950Appearance.ITEM_BASE - Native950Appearance.KIT_BASE - 1;

        public final boolean male;
        private final int[] colours, bodyKits;

        public Appearance(boolean male, int[] colours, int[] bodyKits) {
            Objects.requireNonNull(colours, "colours");
            Objects.requireNonNull(bodyKits, "bodyKits");
            if (colours.length != COLOUR_COUNT || bodyKits.length != BODY_KIT_COUNT)
                throw new IllegalArgumentException("Saved appearance must contain 10 colours and 7 body kits");
            for (int colour : colours) if (colour < 0 || colour > 255) throw new IllegalArgumentException("Invalid saved colour");
            for (int kit : bodyKits)
                if (kit < -1 || kit > MAX_BODY_KIT)
                    throw new IllegalArgumentException("Invalid saved body kit " + kit + ": a kit above "
                            + MAX_BODY_KIT + " reaches the 950 client as a wearpos value at or above its item base "
                            + "and faults it at 0x140131d53");
            this.male = male;
            this.colours = colours.clone();
            this.bodyKits = bodyKits.clone();
        }

        /** GlobalPlayerUpdater.male() defaults, the same values Native950Appearance's template uses. */
        public static Appearance fresh() {
            return new Appearance(true, new int[] {3, 16, 16, 0, 0, 0, 0, 0, 0, 0}, new int[] {3, 14, 18, 26, 34, 38, 42});
        }

        public int[] colours() { return colours.clone(); }
        public int[] bodyKits() { return bodyKits.clone(); }

        @Override public boolean equals(Object other) {
            if (!(other instanceof Appearance)) return false;
            Appearance that = (Appearance) other;
            return male == that.male && Arrays.equals(colours, that.colours) && Arrays.equals(bodyKits, that.bodyKits);
        }
        @Override public int hashCode() { return (Arrays.hashCode(colours) * 31 + Arrays.hashCode(bodyKits)) * 2 + (male ? 1 : 0); }
    }

    /** Display name plus creation and last-login timestamps (epoch millis, 0 = unknown). */
    public static final class Identity {
        public final String displayName;
        public final long created, lastLogin;

        public Identity(String displayName, long created, long lastLogin) {
            Objects.requireNonNull(displayName, "displayName");
            if (displayName.isEmpty() || displayName.length() > MAX_DISPLAY_NAME || !displayName.matches("[A-Za-z0-9 _-]+"))
                throw new IllegalArgumentException("Invalid saved display name");
            if (created < 0 || lastLogin < 0) throw new IllegalArgumentException("Invalid saved timestamp");
            this.displayName = displayName; this.created = created; this.lastLogin = lastLogin;
        }

        /** The canonical name as display name and unknown timestamps. */
        public static Identity fresh(String canonicalUsername) { return new Identity(canonicalUsername, 0, 0); }

        @Override public boolean equals(Object other) {
            if (!(other instanceof Identity)) return false;
            Identity that = (Identity) other;
            return displayName.equals(that.displayName) && created == that.created && lastLogin == that.lastLogin;
        }
        @Override public int hashCode() { return displayName.hashCode() * 31 + (int) (created ^ lastLogin); }
    }
}
