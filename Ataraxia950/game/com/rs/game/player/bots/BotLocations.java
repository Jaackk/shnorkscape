package com.rs.game.player.bots;

import com.rs.game.WorldTile;
import com.rs.utils.Utils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Curated catalog of world locations bot scripts can travel to. Coordinates are
 * lifted from 2009scape's Adventurer bot data and verified against this server's
 * region layout. Treat any unverified location as a hint — bots should fall back
 * gracefully if they get stuck on arrival.
 */
public final class BotLocations {

    private BotLocations() {
    }

    // Cities -----------------------------------------------------------------

    public static final WorldTile LUMBRIDGE = new WorldTile(3222, 3219, 0);
    public static final WorldTile VARROCK = new WorldTile(3213, 3428, 0);
    public static final WorldTile FALADOR = new WorldTile(2965, 3380, 0);
    public static final WorldTile DRAYNOR = new WorldTile(3080, 3250, 0);
    public static final WorldTile EDGEVILLE = new WorldTile(3088, 3486, 0);
    public static final WorldTile ARDOUGNE = new WorldTile(2662, 3304, 0);
    public static final WorldTile YANILLE = new WorldTile(2615, 3104, 0);
    public static final WorldTile CATHERBY = new WorldTile(2809, 3435, 0);
    public static final WorldTile SEERS = new WorldTile(2726, 3485, 0);
    public static final WorldTile RIMMINGTON = new WorldTile(2977, 3239, 0);
    public static final WorldTile KARAMJA = new WorldTile(2849, 3033, 0);
    public static final WorldTile AL_KHARID = new WorldTile(3297, 3219, 0);
    public static final WorldTile BARBARIAN_VILLAGE = new WorldTile(3082, 3422, 0);
    public static final WorldTile TAVERLEY = new WorldTile(2909, 3436, 0);
    public static final WorldTile BURTHORPE = new WorldTile(2885, 3540, 0);

    public static final WorldTile[] CITIES = {
            LUMBRIDGE, VARROCK, FALADOR, DRAYNOR, EDGEVILLE,
            ARDOUGNE, YANILLE, CATHERBY, SEERS, RIMMINGTON,
            AL_KHARID, BARBARIAN_VILLAGE, TAVERLEY
    };

    // Points of interest -----------------------------------------------------

    public static final WorldTile MINING_GUILD = new WorldTile(3046, 9740, 0);
    public static final WorldTile COAL_TRUCKS = new WorldTile(2581, 3481, 0);
    public static final WorldTile MAGIC_TREES = new WorldTile(2285, 3146, 0);
    public static final WorldTile GEM_ROCKS = new WorldTile(2825, 2997, 0);
    public static final WorldTile TREE_GNOME_STRONGHOLD = new WorldTile(2437, 3441, 0);
    public static final WorldTile TEAK_FOREST = new WorldTile(2334, 3048, 0);
    public static final WorldTile FALADOR_PARTY_ROOM = new WorldTile(3052, 3377, 0);
    public static final WorldTile WIZARD_TOWER = new WorldTile(3104, 3162, 0);
    public static final WorldTile CHAMPIONS_GUILD = new WorldTile(3192, 3357, 0);
    public static final WorldTile COOKS_GUILD = new WorldTile(3147, 3450, 0);
    public static final WorldTile WARRIORS_GUILD = new WorldTile(2884, 3543, 0);
    public static final WorldTile CRAFTING_GUILD = new WorldTile(2933, 3286, 0);

    public static final WorldTile[] POIS = {
            MINING_GUILD, COAL_TRUCKS, MAGIC_TREES, GEM_ROCKS, TREE_GNOME_STRONGHOLD,
            TEAK_FOREST, FALADOR_PARTY_ROOM, WIZARD_TOWER, CHAMPIONS_GUILD,
            COOKS_GUILD, WARRIORS_GUILD, CRAFTING_GUILD
    };

    // Grand Exchange ---------------------------------------------------------
    // Walked booth ring samples from /mypos, ordered clockwise around the GE.
    // Outer perimeter trees: SW(3144,3472) SE(3182,3472) NE(3190,3502) NW(3143,3509)

    public static final WorldTile GE_CENTER = new WorldTile(3164, 3492, 0);
    /** Maximum outward distance for bots queueing near the inner booth ring. */
    public static final int GE_RING_CLOSE_DISTANCE = 2;
    /** Maximum outward distance for the thinning GE plaza crowd. */
    public static final int GE_RING_OUTER_DISTANCE = 10;

    /** Booth-perimeter samples used as a closed polygonal ring. */
    public static final WorldTile[] GE_INNER_RING = {
            new WorldTile(3165, 3484, 0),
            new WorldTile(3168, 3485, 0),
            new WorldTile(3170, 3486, 0),
            new WorldTile(3172, 3489, 0),
            new WorldTile(3172, 3492, 0),
            new WorldTile(3171, 3495, 0),
            new WorldTile(3170, 3497, 0),
            new WorldTile(3168, 3498, 0),
            new WorldTile(3164, 3499, 0),
            new WorldTile(3161, 3498, 0),
            new WorldTile(3159, 3497, 0),
            new WorldTile(3157, 3494, 0),
            new WorldTile(3157, 3491, 0),
            new WorldTile(3158, 3488, 0),
            new WorldTile(3159, 3486, 0)
    };

    public static final WorldTile[] GE_BOOTH_CORNERS = GE_INNER_RING;

    // Helpers ----------------------------------------------------------------

    public static WorldTile randomCity() {
        return CITIES[Utils.random(CITIES.length)];
    }

    public static WorldTile randomPOI() {
        return POIS[Utils.random(POIS.length)];
    }

    /**
     * Picks any wanderable destination, weighted toward cities since they are
     * where most player activity is. ~75% city, 25% POI.
     */
    public static WorldTile randomDestination() {
        return ThreadLocalRandom.current().nextInt(4) == 0 ? randomPOI() : randomCity();
    }

    public static WorldTile randomNear(WorldTile center, int radius) {
        if (center == null) {
            return null;
        }
        if (radius <= 0) {
            return new WorldTile(center);
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int dx = random.nextInt(-radius, radius + 1);
        int dy = random.nextInt(-radius, radius + 1);
        return center.transform(dx, dy, 0);
    }
}
