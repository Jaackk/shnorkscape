package com.rs.game.player.combat.rs2;

import java.util.HashMap;
import java.util.Map;

import com.rs.game.player.content.Magic;

/**
 * Authentic late-RS2 rune costs, base XP, and level requirements for combat
 * spells.
 *
 * Ataraxia's GeneralRequirementMap cache doesn't populate the keys Magic.java
 * tries to read (2891 for XP, 2898-2912 for rune counts, 2807 for level) for
 * combat spells - the maps load with values == null, so the cache returns 0
 * for every key. Without this overlay every combat cast consumes no runes and
 * gives no XP.
 *
 * Values mirror 2009scape's spell-class definitions (modern/AirSpell.java,
 * WaterSpell.java, ancient/IceSpells.java, etc.), which are the same values
 * recorded on the OSRS wiki for late-RS2.
 *
 * Lookup is by (spell button id, spell book). Both books use overlapping
 * numeric ids for some spells (e.g. 28 is Water Strike in normal, Smoke Rush
 * in ancient), so the book parameter is required.
 */
public final class Rs2SpellOverride {

    public static final int AIR_RUNE = 556;
    public static final int WATER_RUNE = 555;
    public static final int EARTH_RUNE = 557;
    public static final int FIRE_RUNE = 554;
    public static final int MIND_RUNE = 558;
    public static final int CHAOS_RUNE = 562;
    public static final int DEATH_RUNE = 560;
    public static final int BLOOD_RUNE = 565;
    public static final int SOUL_RUNE = 566;
    public static final int NATURE_RUNE = 561;
    public static final int LAW_RUNE = 563;
    public static final int BODY_RUNE = 559;
    public static final int COSMIC_RUNE = 564;
    public static final int ASTRAL_RUNE = 9075;

    public static final class SpellData {
        public final int level;
        public final double xp;
        /** alternating runeId, amount pairs, ready to feed to Magic.checkRunes. */
        public final int[] runes;

        SpellData(int level, double xp, int... runes) {
            this.level = level;
            this.xp = xp;
            this.runes = runes;
        }
    }

    private static final Map<Integer, SpellData> NORMAL = new HashMap<>();
    private static final Map<Integer, SpellData> ANCIENT = new HashMap<>();

    static {
        // ---- STANDARD (MODERN) BOOK ----
        // Strikes: 1 mind rune
        normal(25, 1,  5.5,  AIR_RUNE, 1,   MIND_RUNE, 1);                          // Wind Strike
        normal(28, 5,  7.5,  WATER_RUNE, 1, AIR_RUNE, 1,  MIND_RUNE, 1);            // Water Strike
        normal(30, 9,  9.5,  EARTH_RUNE, 2, AIR_RUNE, 1,  MIND_RUNE, 1);            // Earth Strike
        normal(32, 13, 11.5, FIRE_RUNE, 3,  AIR_RUNE, 2,  MIND_RUNE, 1);            // Fire Strike

        // Bolts: 1 chaos rune
        normal(34, 17, 13.5, AIR_RUNE, 2,   CHAOS_RUNE, 1);                         // Wind Bolt
        normal(39, 23, 16.5, WATER_RUNE, 2, AIR_RUNE, 2,  CHAOS_RUNE, 1);           // Water Bolt
        normal(42, 29, 19.5, EARTH_RUNE, 3, AIR_RUNE, 2,  CHAOS_RUNE, 1);           // Earth Bolt
        normal(45, 35, 22.5, FIRE_RUNE, 4,  AIR_RUNE, 3,  CHAOS_RUNE, 1);           // Fire Bolt

        // Blasts: 1 death rune
        normal(49, 41, 25.5, AIR_RUNE, 3,   DEATH_RUNE, 1);                         // Wind Blast
        normal(52, 47, 28.5, WATER_RUNE, 3, AIR_RUNE, 3,  DEATH_RUNE, 1);           // Water Blast
        normal(58, 53, 31.5, EARTH_RUNE, 4, AIR_RUNE, 3,  DEATH_RUNE, 1);           // Earth Blast
        normal(63, 59, 34.5, FIRE_RUNE, 5,  AIR_RUNE, 4,  DEATH_RUNE, 1);           // Fire Blast

        // Waves: 1 blood rune
        normal(70, 62, 36.0, AIR_RUNE, 5,   BLOOD_RUNE, 1);                         // Wind Wave
        normal(73, 65, 37.5, WATER_RUNE, 7, AIR_RUNE, 5,  BLOOD_RUNE, 1);           // Water Wave
        normal(77, 70, 40.0, EARTH_RUNE, 7, AIR_RUNE, 5,  BLOOD_RUNE, 1);           // Earth Wave
        normal(80, 75, 42.5, FIRE_RUNE, 7,  AIR_RUNE, 5,  BLOOD_RUNE, 1);           // Fire Wave

        // Crumble Undead: 2 earth, 2 air, 1 chaos
        normal(47, 39, 24.5, EARTH_RUNE, 2, AIR_RUNE, 2,  CHAOS_RUNE, 1);           // Crumble Undead
        // Magic Dart (Slayer's staff supplies chaos): 4 mind, 1 death
        normal(56, 50, 30.0, MIND_RUNE, 4,  DEATH_RUNE, 1);                         // Magic Dart
        // Iban Blast (Iban's staff supplies the focus): 5 fire, 1 death
        normal(54, 50, 30.0, FIRE_RUNE, 5,  DEATH_RUNE, 1);                         // Iban Blast
        // God spells: 4 air, 2 blood, 2 fire (Saradomin Strike), 1 fire for the other two
        normal(66, 60, 35.0, AIR_RUNE, 4,   BLOOD_RUNE, 2, FIRE_RUNE, 2);           // Saradomin Strike
        normal(67, 60, 35.0, AIR_RUNE, 4,   BLOOD_RUNE, 2, FIRE_RUNE, 1);           // Claws of Guthix
        normal(68, 60, 35.0, AIR_RUNE, 4,   BLOOD_RUNE, 2, FIRE_RUNE, 1);           // Flames of Zamorak

        // ---- ANCIENT BOOK ----
        // Rushes
        ancient(20, 58, 34.0, DEATH_RUNE, 2, CHAOS_RUNE, 2, WATER_RUNE, 2);                       // Ice Rush
        ancient(24, 56, 33.0, BLOOD_RUNE, 1, DEATH_RUNE, 2, CHAOS_RUNE, 2);                       // Blood Rush
        ancient(28, 50, 30.0, DEATH_RUNE, 2, CHAOS_RUNE, 2, FIRE_RUNE, 1, AIR_RUNE, 1);            // Smoke Rush
        ancient(32, 52, 31.0, SOUL_RUNE, 1,  DEATH_RUNE, 2, CHAOS_RUNE, 2, AIR_RUNE, 1);           // Shadow Rush

        // Bursts
        ancient(22, 70, 40.0, DEATH_RUNE, 2, CHAOS_RUNE, 4, WATER_RUNE, 4);                       // Ice Burst
        ancient(26, 68, 39.0, BLOOD_RUNE, 2, DEATH_RUNE, 2, CHAOS_RUNE, 4);                       // Blood Burst
        ancient(30, 62, 36.0, DEATH_RUNE, 2, CHAOS_RUNE, 4, FIRE_RUNE, 2, AIR_RUNE, 2);            // Smoke Burst
        ancient(34, 64, 37.0, SOUL_RUNE, 2,  DEATH_RUNE, 2, CHAOS_RUNE, 4, AIR_RUNE, 1);           // Shadow Burst

        // Blitzes
        ancient(21, 82, 46.0, BLOOD_RUNE, 2, DEATH_RUNE, 2, WATER_RUNE, 3);                       // Ice Blitz
        ancient(25, 80, 45.0, BLOOD_RUNE, 4, DEATH_RUNE, 2);                                      // Blood Blitz
        ancient(29, 74, 42.0, BLOOD_RUNE, 2, DEATH_RUNE, 2, FIRE_RUNE, 2, AIR_RUNE, 2);            // Smoke Blitz
        ancient(33, 76, 43.0, SOUL_RUNE, 2,  BLOOD_RUNE, 2, DEATH_RUNE, 2, AIR_RUNE, 2);           // Shadow Blitz

        // Barrages
        ancient(23, 94, 52.0, BLOOD_RUNE, 2, DEATH_RUNE, 4, WATER_RUNE, 6);                       // Ice Barrage
        ancient(27, 92, 51.0, SOUL_RUNE, 1,  BLOOD_RUNE, 4, DEATH_RUNE, 4);                       // Blood Barrage
        ancient(31, 86, 48.0, BLOOD_RUNE, 2, DEATH_RUNE, 4, FIRE_RUNE, 4, AIR_RUNE, 4);            // Smoke Barrage
        ancient(35, 88, 48.0, SOUL_RUNE, 3,  BLOOD_RUNE, 2, DEATH_RUNE, 4, AIR_RUNE, 4);           // Shadow Barrage
    }

    private Rs2SpellOverride() {
    }

    private static void normal(int spellId, int level, double xp, int... runes) {
        NORMAL.put(spellId, new SpellData(level, xp, runes));
    }

    private static void ancient(int spellId, int level, double xp, int... runes) {
        ANCIENT.put(spellId, new SpellData(level, xp, runes));
    }

    /**
     * Returns the RS2 override for the given (spell button id, spell book) pair,
     * or null if the spell isn't registered. Lunars and other non-combat books
     * always return null so they keep using their existing flow.
     */
    public static SpellData find(int spellId, int spellBook) {
        if (spellBook == Magic.NORMAL_BOOK) {
            return NORMAL.get(spellId);
        }
        if (spellBook == Magic.ANCIENTS_BOOK) {
            return ANCIENT.get(spellId);
        }
        return null;
    }

    /**
     * External hook for adding or replacing spell data without touching this
     * file - useful for content modules that add new combat spells.
     */
    public static void register(int spellBook, int spellId, int level, double xp, int... runes) {
        SpellData data = new SpellData(level, xp, runes);
        if (spellBook == Magic.NORMAL_BOOK) {
            NORMAL.put(spellId, data);
        } else if (spellBook == Magic.ANCIENTS_BOOK) {
            ANCIENT.put(spellId, data);
        }
    }
}
