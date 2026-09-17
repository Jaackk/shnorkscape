package com.rs.game.player.combat.rs2;

import java.util.HashMap;
import java.util.Map;

import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.Magic;

/**
 * Registry of pre-EOC spell max hits in old HP units.
 *
 * Each entry returns the classic RS2-era max damage for a spell, which the
 * RS2 magic max-hit pipeline then multiplies by the player's magic damage
 * prayer multiplier (Mystic Will/Lore/Might, Augury, etc.).
 *
 * Adding a new spell here is preferred to expanding a switch in
 * Rs2AtaraxiaNumerics. Lambda entries let level-scaled spells (Magic Dart,
 * god-spell Charge variants) compute against the live player.
 */
public final class Rs2SpellMaxHits {

    @FunctionalInterface
    public interface SpellMaxFunction {
        int compute(Player player);
    }

    private static final Map<Integer, SpellMaxFunction> NORMAL = new HashMap<>();
    private static final Map<Integer, SpellMaxFunction> ANCIENT = new HashMap<>();

    static {
        // Normal book - strike / bolt / blast / wave families plus utility
        normal(25, 2);   // Wind Strike
        normal(28, 4);   // Water Strike
        normal(30, 6);   // Earth Strike
        normal(32, 8);   // Fire Strike
        normal(34, 9);   // Wind Bolt
        normal(39, 10);  // Water Bolt
        normal(42, 11);  // Earth Bolt
        normal(45, 12);  // Fire Bolt
        normal(47, 15);  // Crumble Undead
        normal(49, 13);  // Wind Blast
        normal(52, 14);  // Water Blast
        normal(54, 25);  // Iban Blast
        normal(56, p -> 10 + visibleMagic(p) / 10); // Magic Dart - scales with level
        normal(58, 15);  // Earth Blast
        normal(63, 16);  // Fire Blast
        normal(66, 20);  // Saradomin Strike (uncharged)
        normal(67, 20);  // Claws of Guthix (uncharged)
        normal(68, 20);  // Flames of Zamorak (uncharged)
        normal(70, 17);  // Wind Wave
        normal(73, 18);  // Water Wave
        normal(77, 19);  // Earth Wave
        normal(80, 20);  // Fire Wave

        // Ancients book - rush / burst / blitz / barrage
        ancient(28, 13); // Smoke Rush
        ancient(32, 14); // Shadow Rush
        ancient(24, 15); // Blood Rush
        ancient(20, 17); // Ice Rush
        ancient(30, 17); // Smoke Burst
        ancient(34, 18); // Shadow Burst
        ancient(26, 21); // Blood Burst
        ancient(22, 22); // Ice Burst
        ancient(29, 23); // Smoke Blitz
        ancient(33, 24); // Shadow Blitz
        ancient(25, 25); // Blood Blitz
        ancient(21, 26); // Ice Blitz
        ancient(31, 27); // Smoke Barrage
        ancient(35, 28); // Shadow Barrage
        ancient(27, 29); // Blood Barrage
        ancient(23, 30); // Ice Barrage
    }

    private Rs2SpellMaxHits() {
    }

    /**
     * Returns the pre-EOC max hit (in old HP units) for the given spell, or -1
     * if the spell isn't registered. Callers should fall back to a derived
     * max hit (typically scaled from cache bonuses) when -1 is returned, so
     * post-2009 spells stay playable on the RS2 path.
     */
    public static int lookup(Player player, int spellId, int spellBook) {
        Map<Integer, SpellMaxFunction> book = bookMap(spellBook);
        if (book == null) {
            return -1;
        }
        SpellMaxFunction fn = book.get(spellId);
        return fn == null ? -1 : fn.compute(player);
    }

    /**
     * Lets external callers register a custom or post-2009 spell with an
     * authentic pre-EOC max hit. Useful for extension code that adds new
     * spells without modifying this file.
     */
    public static void register(int spellBook, int spellId, SpellMaxFunction fn) {
        Map<Integer, SpellMaxFunction> book = bookMap(spellBook);
        if (book != null) {
            book.put(spellId, fn);
        }
    }

    private static Map<Integer, SpellMaxFunction> bookMap(int spellBook) {
        if (spellBook == Magic.NORMAL_BOOK) {
            return NORMAL;
        }
        if (spellBook == Magic.ANCIENTS_BOOK) {
            return ANCIENT;
        }
        return null;
    }

    private static void normal(int spellId, int constantMax) {
        NORMAL.put(spellId, p -> constantMax);
    }

    private static void normal(int spellId, SpellMaxFunction fn) {
        NORMAL.put(spellId, fn);
    }

    private static void ancient(int spellId, int constantMax) {
        ANCIENT.put(spellId, p -> constantMax);
    }

    private static int visibleMagic(Player player) {
        return Math.max(1, player.getSkills().getLevel(Skills.MAGIC)
                + player.getAuraManager().getStatModifier(Skills.MAGIC));
    }
}
