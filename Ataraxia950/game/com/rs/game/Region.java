package com.rs.game;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.rs.Settings;
import com.rs.cache.Cache;
import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.ObjectData;
import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.cores.CoresManager;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.player.Player;
import com.rs.game.route.Flags;
import com.rs.network.io.InputStream;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.maps.CustomObjectSpawnsDataParser;
import com.rs.utils.data.parsers.npcs.NPCSpawnsDataParser;

import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import lombok.Getter;
import lombok.val;

public class Region {
    public static final int[] OBJECT_SLOTS = new int[] { 0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3 };
    public static final int OBJECT_SLOT_WALL = 0;
    public static final int OBJECT_SLOT_WALL_DECORATION = 1;
    public static final int OBJECT_SLOT_FLOOR = 2;
    public static final int OBJECT_SLOT_FLOOR_DECORATION = 3;

    protected int regionId;
    protected RegionMap map;
    protected RegionMap clipedOnlyMap;

    protected List<Integer> playersIndexes;
    protected List<Integer> npcsIndexes;
    protected List<WorldObject> spawnedObjects;
    protected List<WorldObject> removedOriginalObjects;
    protected List<Projectile> projectiles;
    // protected WorldObject[][][][] objects;
    @Getter
    protected Short2ObjectMap<WorldObject> objects;
    private List<FloorItem> groundItems;
    private volatile int loadMapStage;
    private boolean loadedNPCSpawns;
    private boolean loadedObjectSpawns;
    private boolean loadedItemSpawns;
    private int[] musicIds;
    private RuntimeException modernMapFailure;

    public Region(final int regionId) {
        this.regionId = regionId;
        spawnedObjects = new CopyOnWriteArrayList<WorldObject>();
        removedOriginalObjects = new CopyOnWriteArrayList<WorldObject>();
        projectiles = new CopyOnWriteArrayList<Projectile>();
        if (Cache.isFlatReadOnly()) musicIds = new int[0];
        else loadMusicIds();
        // indexes null by default cuz we dont want them on mem for regions that
        // players cant go in
    }

    public static final String getMusicName3(final int regionId) {
        switch (regionId) {
        case 13152: // crucible
            return "Steady";
        case 13151: // crucible
            return "Hunted";
        case 12895: // crucible
            return "Target";
        case 12896: // crucible
            return "I Can See You";
        case 11575: // burthope
            return "Spiritual";
        case 18512:
        case 18511:
        case 19024:
            return "Tzhaar City III";
        case 18255: // fight pits
            return "Tzhaar Supremacy III";
        case 14948:
            return "Dominion Lobby III";
        default:
            return null;
        }
    }

    public void quickLoad() {
        if (Cache.isFlatReadOnly()) { loadModernMap(); return; }
        if (getLoadMapStage() == 0) {
            setLoadMapStage(1);
            try {
                loadRegionMap();
                setLoadMapStage(2);
                if (!isLoadedObjectSpawns()) {
                    loadObjectSpawns();
                    setLoadedObjectSpawns(true);
                }
                if (!isLoadedNPCSpawns()) {
                    loadNPCSpawns();
                    setLoadedNPCSpawns(true);
                }
            } catch (final Throwable e) {
                Logger.getGlobal().catching(e);
            }
        }
    }

    public static final String getMusicName2(final int regionId) {
        switch (regionId) {
        case 13152: // crucible
            return "Steady";
        case 13151: // crucible
            return "Hunted";
        case 12895: // crucible
            return "Target";
        case 12896: // crucible
            return "I Can See You";
        case 11575: // burthope
            return "Spiritual";
        case 18512:
        case 18511:
        case 19024:
            return "Tzhaar City III";
        case 18255: // fight pits
            return "Tzhaar Supremacy III";
        case 14948:
            return "Dominion Lobby III";
        default:
            return null;
        }
    }

    public static final String getMusicName1(final int regionId) {
        switch (regionId) {
        case 9265: // ltdya
            return "Far Away";
        case 9009: // south east isafdar
            return "Forest";
        case 8001: // poison waste slayer dungeon
            return "Waste Defaced";
        case 9008: // poison waste
        case 9007:
        case 9264:
            return "Lost Soul";
        case 8496: // port tyras
        case 8497:
            return "Riverside";
        case 8752: // south of tyras camp
            return "Exposed";
        case 8753: // tyras camp
            return "Meridian";
        case 9010: // south east prifinas
            return "Crystal Castle";
        case 8498: // south west prifinas
            return "Everywhere";
        case 8754: // elf camp
            return "Woodland";
        case 9523: // arandar
        case 9267:
        case 9268:
            return "Overpass";
        case 10042: // waterbird island
            return "The Desolate Isle";
        case 9886:
            return "The Monsters Below";
        case 10142:
            return "Subterranea";
        case 10144:
            return "Corridors of Power";
        case 10400:
            return "Slither and Thither";
        case 10300:
            return "Etcetera";
        case 10044:
            return "Miscellania";
        case 7236:
        case 7748:
        case 7492:
            return "Xenophobe";
        case 11589:
            return "Dagannoth Dawn";
        case 10835:
            return "Dorgeshuun City";
        case 10834:
            return "Dorgeshuun Deep";
        case 12601:
            return "Wildwood";
        case 12093:
            return "Pirates of Peril";
        case 13874:
            return "Distant Land";
        case 13614:
            return "Desert Heat";
        case 13613:
            return "Over To Nardah";
        case 13872:
        case 13873:
            return "The Golem";
        case 13871:
            return "Kharidian Nights";
        case 10537: // pest control island
            return "Null and Void";
        case 6995: // ancient cavern
        case 6994:
            return "Barb Wire";
        case 6482: // kuradal dungeon
            return "Final Destination";
        case 9526: // gnome ball field
            return "Gnomeball";
        case 10034: // Battlefield north of Tree Gnome Village.,
            return "Attack I";
        case 10033: // Tree Gnome Village
            return "Emotion";
        case 11418: // Dwarven tunnel under Wolf Mountain
            return "Beyond";
        case 8774: // taverly slayer dungeon
            return "Taverley Lament";
        case 11576:
            return "Kingdom";
        case 11320:
            return "Tremble";
        case 12616: // tarns lair
            return "Undead Dungeon";
        case 10388:
            return "Cavern";
        case 12107:
            return "Into the Abyss";
        case 11164:
            return "The Slayer";
        case 10908:
        case 10907:
            return "Masquerade";
        case 4707:
        case 4451:
        case 5221:
        case 5220:
        case 5219:
        case 4453:
        case 4709:
            return "Hunting Dragons";
        case 12115:
            return "Dimension X";
        case 8527: // braindeath island
            return "Aye Car Rum Ba";
        case 8528: // braindeath mountain
            return "Blistering Barnacles";
        case 13206: // goblin mines under lumby
            return "The Lost Tribe";
        case 12949:
        case 12950:
            return "Cave of the Goblins";
        case 12948:
            return "The Power of Tears";
        case 11416: // dramen tree
            return "Underground";
        case 14638: // mosleharms
            return "In the Brine";
        case 14637:
        case 14894:
            return "Life's a Beach!";
        case 14494: // mosleharms cave
            return "Little Cave of Horrors";
        case 11673: // taverly dungeon musics
            return "Courage";
        case 11672:
            return "Dunjun";
        case 11417:
            return "Arabique";
        case 11671:
            return "Royale";
        case 13977:
            return "Stillness";
        case 13622:
            return "Morytania";
        case 13722:
            return "Mausoleum";
        case 10906:
            return "Twilight";
        case 12181: // Asgarnian Ice Dungeon's wyvern area
            return "Woe of the Wyvern";
        case 11925: // Asgarnian Ice Dungeon
            return "Starlight";
        case 13617: // abbey
            return "Citharede Requiem";
        case 13361: // desert verms
            return "Valerio's Song";
        case 13910: // The Tale of the Muspah cave entrance
        case 13654:
            return "Rest for the Weary";
        case 13656: // The Tale of the Muspah cave ice verms area
            return "The Muspah's Tomb";
        case 11057: // brimhaven and arroundd
            return "High Seas";
        case 10802:
            return "Jungly2";
        case 10801:
            return "Landlubber";
        case 11058:
            return "Jolly-R";
        case 10901: // brimhaven dungeon entrance
            return "Pathways";
        case 10645: // brimhaven dungeon
        case 10644:
        case 10900:
            return "7th Realm";
        case 11315: // crandor
        case 11314:
            return "The Shadow";
        case 11414: // karanja underground
        case 11413:
            return "Dangerous Road";
        case 7505: // strongholf of security war
            return "Dogs of War";
        case 8017: // strongholf of security famine
            return "Food for Thought";
        case 8530: // strongholf of security pestile
            return "Malady";
        case 9297: // strongholf of security death
            return "Dance of Death";
        case 10040:
            return "Lighthouse";
        case 10140: // inside lighthouse
            return "Out of the Deep";
        case 9797:
            return "Crystal Cave";
        case 9541:
            return "Faerie";
        case 11927: // gamers grotto
            return "Cave Background";
        case 14646:// Port Phasmatys
            return "The Other Side";
        case 14746:// Ectofuntus
            return "Phasmatys";
        case 14747:// Port Phasmatys brewery
            return "Brew Hoo Hoo";
        case 15967:// Runespan
            return "Runespan";
        case 15711:// Runespan
            return "Runearia";
        case 15710:// Runespan
            return "Runebreath";
        case 13152: // crucible
            return "Hunted";
        case 13151: // crucible
            return "Target";
        case 12895: // crucible
            return "I Can See You";
        case 12896: // crucible
            return "You Will Know Me";
        case 12597:
            return "Spirit";
        case 13109:
            return "Medieval";
        case 13110:
            return "Honkytonky Parade";
        case 10658:
            return "Espionage";
        case 13899: // water altar
            return "Zealot";
        case 10039:
            return "Legion";
        case 11319: // warriors guild
            return "Warriors' Guild";
        case 11575: // burthope
            return "Spiritual";
        case 11573: // taverley
            return "Taverley Ambience";
        case 7473:
            return "The Waiting Game";
        case 18512:
        case 18511:
        case 19024:
            return "Tzhaar City I";
        case 18255: // fight pits
            return "Tzhaar Supremacy I";
        case 14672:
        case 14671:
        case 14415:
        case 14416:
            return "Living Rock";
        case 11157: // Brimhaven Agility Arena
            return "Aztec";
        case 15446:
        case 15957:
        case 15958:
            return "Dead and Buried";
        case 12848:
            return "Arabian3";
        case 12954:
        case 12442:
        case 12441:
            return "Scape Cave";
        case 12185:
        case 11929:
            return "Dwarf Theme";
        case 12184:
            return "Workshop";
        case 6992:
        case 6993: // mole lair
            return "The Mad Mole";
        case 9776: // castle wars
            return "Melodrama";
        case 10029:
        case 10285:
            return "Jungle Hunt";
        case 14231: // barrows under
            return "Dangerous Way";
        case 12856: // chaos temple
            return "Faithless";
        case 13104:
        case 12847: // arround desert camp
        case 13359:
        case 13102:
            return "Desert Voyage";
        case 13103:
            return "Lonesome";
        case 12589: // granite mine
            return "The Desert";
        case 18517: // polipore dungeon
        case 18516:
        case 18773:
        case 18775:
        case 13407: // crucible entrance
        case 13360: // dominion tower outside
            return "";
        case 14948:
            return "Dominion Lobby I";
        case 11836: // lava maze near kbd entrance
            return "Attack III";
        case 11834: // forgotten cimitery
            return "Wilderness III";
        case 12091: // lava maze west
            return "Wilderness II";
        case 12092: // lava maze north
            return "Wild Side";
        case 9781:
            return "Gnome Village";
        case 11339: // air altar
            return "Serene";
        case 11083: // mind altar
            return "Miracle Dance";
        case 10827: // water altar
            return "Zealot";
        case 10571: // earth altar
            return "Down to Earth";
        case 10315: // fire altar
            return "Quest";
        case 8523: // cosmic altar
            return "Stratosphere";
        case 9035: // chaos altar
            return "Complication";
        case 8779: // death altar
            return "La Mort";
        case 10059: // body altar
            return "Heart and Mind";
        case 9803: // law altar
            return "Righteousness";
        case 9547: // nature altar
            return "Understanding";
        case 9804: // blood altar
            return "Bloodbath";
        case 13107:
            return "Arabian2";
        case 13105:
            return "Al Kharid";
        case 12342: // edge
            return "Forever";
        case 10806:
            return "Overture";
        case 10899:
            return "Karamja Jam";
        case 13623:
            return "The Terrible Tower";
        case 12374:
            return "The Route of All Evil";
        case 12630:
        case 12629:
            return "The Route of the Problem";
        case 12885:
        case 12886:
        case 13141:
        case 13142:
            return "The Route of All Evil";
        case 9802:
            return "Undead Dungeon";
        case 10809: // east rellekka
            return "Borderland";
        case 10553: // Rellekka
            return "Rellekka";
        case 10552: // south
            return "Saga";
        case 10296: // south west
            return "Lullaby";
        case 10828: // south east
            return "Legend";
        case 9275:
            return "Volcanic Vikings";
        case 11061:
        case 11317:
            return "Fishing";
        case 9551:
            return "TzHaar!";
        case 12345:
            return "Eruption";
        case 12089:
            return "Dark";
        case 12446:
        case 12445:
        case 13114:
            return "Wilderness I ";
        case 12343:
            return "Dangerous";
        case 14131:
            return "Dance of the Undead";
        case 11588:
            return "The Vacant Abyss";
        case 11844:
            return "Bane of Summer";
        case 13363: // duel arena hospital
            return "Shine";
        case 13362: // duel arena
            return "Duel Arena";
        case 12082: // port sarim
            return "Sea Shanty2";
        case 12081: // port sarim south
            return "Tomorrow";
        case 11602:
            return "Strength of Saradomin";
        case 12590:
            return "Bandit Camp";
        case 10329:
            return "The Sound of Guthix";
        case 9033:
            return "Attack V";
        // godwars
        case 11603:
            return "Zamorak Zoo";
        case 11346:
            return "Armadyl Alliance";
        case 11347:
            return "Armageddon";
        // black kngihts fortess
        case 12086:
            return "Knightmare";
        // tzaar
        case 9552:
            return "Fire and Brimstone";
        // kq
        case 13972:
            return "Insect Queen";
        // clan wars free for all:
        case 11094:
            return "Clan Wars";
        /*
         * tutorial island
         */
        case 12336:
            return "Newbie Melody";
        // dark warrior fortress
        case 12088:
            return "Army of Darkness";
        /*
         * darkmeyer
         */
        case 14644:
        case 14388:
            return "Darkmeyer";
        case 12183:
            return "Metalwork";
        /*
         * kalaboss
         */
        case 13626:
        case 13627:
        case 13882:
        case 13881:
            return "Daemonheim Entrance";
        /*
         * Lumbridge, falador and region.
         */
        case 11574: // heroes guild
            return "Splendour";
        case 12851:
            return "Autumn Voyage";
        case 12338: // draynor and market
            return "Unknown Land";
        case 12339: // draynor up
            return "Start";
        case 12340: // draynor mansion
            return "Spooky";
        case 12850: // lumbry castle
            return "Harmony";
        case 12849: // east lumbridge swamp
            return "Yesteryear";
        case 12593: // at Lumbridge Swamp.
            return "Book of Spells";
        case 12594: // on the path between Lumbridge and Draynor.
            return "Dream";
        case 12595: // at the Lumbridge windmill area.
            return "Flute Salad";
        case 12854: // at Varrock Palace.
            return "Adventure";
        case 12853: // at varrock center
            return "Garden";
        case 12852: // varock mages
            return "Expanse";
        case 13108:
            return "Still Night";
        case 12083:
            return "Wander";
        case 11828:
        case 12084:
            return "Fanfare";
        case 11829:
            return "Scape Soft";
        case 11577:
            return "Mad Eadgar";
        case 10293: // at the Fishing Guild.
            return "Mellow";
        case 11837: // wild agility course
            return "Scape Sad";
        case 13117: // rouges castle
            return "Regal";
        case 11824:
            return "Mudskipper Melody";
        case 11570:
            return "Wandar";
        case 12341:
            return "Barbarianims";
        case 12855:
            return "Crystal Sword";
        case 12344:
            return "Dark";
        case 12599:
            return "Doorways";
        case 12598:
            return "The Trade Parade";
        case 11318:
            return "Ice Melody";
        case 12600:
            return "Scape Wild";
        case 10032: // west yannile:
            return "Big Chords";
        case 10288: // east yanille
            return "Magic Dance";
        case 11826: // Rimmington
            return "Long Way Home";
        case 11825: // rimmigton coast
            return "Attention";
        case 11827: // north rimmigton
            return "Nightfall";
        /*
         * Camelot and region.
         */
        case 11062:
        case 10805:
            return "Camelot";
        case 10550:
            return "Talking Forest";
        case 10549:
            return "Lasting";
        case 10548:
            return "Wonderous";
        case 10547:
            return "Baroque";
        case 10291:
        case 10292:
            return "Knightly";
        case 11571: // crafting guild
            return "Miles Away";
        case 11595: // ess mine
            return "Rune Essence";
        case 10294:
            return "Theme";
        case 12349:
            return "Mage Arena";
        case 13365: // digsite
            return "Venture";
        case 13364: // exams center
            return "Medieval";
        case 13878: // canifis
            return "Village";
        case 13877: // canafis south
            return "Waterlogged";
        /*
         * Mobilies Armies.
         */
        case 9516:
            return "Command Centre";
        case 12596: // champions guild
            return "Greatness";
        case 10804: // legends guild
            return "Trinity";
        case 11601:
            return "Zaros Zeitgeist"; // zaros godwars
        default:
            return null;
        }
    }

    public static final int getMusicId(final String musicName) {
        if (musicName == null)
            return -1;
        if (musicName.equals(""))
            return -2;
        return (int) ClientScriptMap.getMap(1345).getKeyForValue(musicName);
    }

    public void checkLoadMap() {
        if (Cache.isFlatReadOnly()) { loadModernMap(); return; }
        if (getLoadMapStage() == 0) {
            setLoadMapStage(1);
            CoresManager.getServiceProvider().executeNow(() -> {
                try {
                    loadRegionMap();
                    setLoadMapStage(2);
                    if (!isLoadedObjectSpawns()) {
                        loadObjectSpawns();
                        setLoadedObjectSpawns(true);
                    }
                    if (!isLoadedNPCSpawns()) {
                        loadNPCSpawns();
                        setLoadedNPCSpawns(true);
                    }
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            });
        }
    }

    /**
     * M4: told once per region, the first time the native world finishes loading that
     * region's collision. The listener may not spawn anything from inside this call - map
     * loading is re-entrant (creating an NPC loads its own region) and happens under this
     * object's monitor - so the only contract is that it records the region and returns.
     */
    public interface Native950SpawnListener {
        void regionLoaded(int regionId);
    }

    private static volatile Native950SpawnListener native947SpawnListener;
    /**
     * The spawn generation this Region has already announced, or 0 for "never announced".
     * {@code World.regions} is a static map that is never cleared, so a Region object outlives
     * every login; a plain boolean would make the announcement once per JVM and the world
     * would stay permanently empty after the native world drops its NPC roster.
     */
    private int announcedNative950Generation;
    /** Bumped by {@link #rearmNative950Spawns()}; every Region re-announces once afterwards. */
    private static final java.util.concurrent.atomic.AtomicInteger NATIVE947_SPAWN_GENERATION =
            new java.util.concurrent.atomic.AtomicInteger(1);

    /**
     * Forgets every region's announcement, so the next time each one is touched it tells the
     * listener again. Called by the native world when it empties its NPC roster (the last
     * character left): the entities are gone, so the regions must be announceable again or
     * nothing would ever repopulate them.
     */
    public static void rearmNative950Spawns() {
        NATIVE947_SPAWN_GENERATION.incrementAndGet();
    }

    /** The current spawn generation; a Region announces once per generation. */
    public static int native947SpawnGeneration() {
        return NATIVE947_SPAWN_GENERATION.get();
    }

    /**
     * Installs the native spawn listener, or clears it with null. The legacy path never
     * consults it: {@code checkLoadMap} keeps calling {@code loadNPCSpawns} exactly as it
     * always has, and only {@link #loadModernMap} announces.
     */
    public static void setNative950SpawnListener(final Native950SpawnListener listener) {
        native947SpawnListener = listener;
    }

    public static Native950SpawnListener getNative950SpawnListener() {
        return native947SpawnListener;
    }

    /** Native migration runs map loading on its one authoritative world thread. */
    private synchronized void loadModernMap() {
        if (modernMapFailure != null) throw modernMapFailure;
        if (getLoadMapStage() != 0) {
            announceNative950Spawns();
            return;
        }
        setLoadMapStage(1);
        try {
            forceGetRegionMap();
            forceGetRegionMapClipedOnly();
            loadRegionMap();
            setLoadMapStage(2);
        } catch (RuntimeException failure) {
            // Stage 1 returns blocked masks. Never expose partially decoded collision.
            modernMapFailure = new IllegalStateException("Modern region " + regionId + " failed to load; movement blocked", failure);
            throw modernMapFailure;
        }
        announceNative950Spawns();
    }

    /**
     * Announces this region once, and only after stage 2: a spawn placed against stage-1
     * masks would be tested for collision against blocked tiles. The announcement is not
     * the spawn - the listener queues the region and the world thread drains it between
     * ticks, which keeps entity creation out of this monitor.
     */
    private void announceNative950Spawns() {
        int generation = NATIVE947_SPAWN_GENERATION.get();
        if (announcedNative950Generation == generation || getLoadMapStage() != 2) return;
        final Native950SpawnListener listener = native947SpawnListener;
        if (listener == null) return;
        announcedNative950Generation = generation;
        listener.regionLoaded(regionId);
    }

    private void loadNPCSpawns() {
        NPCSpawnsDataParser.loadNPCSpawns(regionId);
    }

    private void loadObjectSpawns() {
        CustomObjectSpawnsDataParser.loadObjectSpawns(regionId);
    }

    /**
     * Unload's map from memory.
     */
    public void unloadMap() {
        if (getLoadMapStage() == 2 && (playersIndexes == null || playersIndexes.isEmpty()) && (npcsIndexes == null || npcsIndexes.isEmpty())) {
            objects = null;
            map = null;
            setLoadMapStage(0);
        }
    }

    public RegionMap forceGetRegionMapClipedOnly() {
        if (clipedOnlyMap == null) {
            clipedOnlyMap = new RegionMap(regionId, true);
        }
        return clipedOnlyMap;
    }

    public RegionMap forceGetRegionMap() {
        if (map == null) {
            map = new RegionMap(regionId, false);
        }
        return map;
    }

    public RegionMap getRegionMap() {
        return map;
    }

    public int getMask(final int plane, final int localX, final int localY) {
        if (map == null || getLoadMapStage() != 2) {
            return -1; // cliped tile
        }
        if (plane < 0 || localX < 0 || localY < 0) {
            return -1;
        }
        return map.getMasks()[plane][localX][localY];
    }

    public int getMaskClipedOnly(final int plane, final int localX, final int localY) {
        if (clipedOnlyMap == null || getLoadMapStage() != 2) {
            return -1; // cliped tile
        }
        if (plane < 0 || localX < 0 || localY < 0) {
            return -1;
        }
        return clipedOnlyMap.getMasks()[plane][localX][localY];
    }

    public void setMask(final int plane, final int localX, final int localY, final int mask) {
        if (map == null || getLoadMapStage() != 2) {
            return; // cliped tile
        }
        if (localX >= 64 || localY >= 64 || localX < 0 || localY < 0) {
            final WorldTile tile = new WorldTile(map.getRegionX() + localX, map.getRegionY() + localY, plane);
            final int regionId = tile.getRegionId();
            final int newRegionX = (regionId >> 8) * 64;
            final int newRegionY = (regionId & 0xff) * 64;
            World.getRegion(tile.getRegionId()).setMask(plane, tile.getX() - newRegionX, tile.getY() - newRegionY, mask);
            return;
        }
        map.setMask(plane, localX, localY, mask);
    }

    public void clip(final WorldObject object, final int x, final int y) {
        if (object.getId() == -1) // dont clip or noclip with id -1
            return;
        if (map == null) {
            map = new RegionMap(regionId, false);
        }
        if (clipedOnlyMap == null) {
            clipedOnlyMap = new RegionMap(regionId, true);
        }
        if (!Cache.isFlatReadOnly() && object.getId() == 8967) {
            return;
        }
        final int plane = object.getPlane();
        final int type = object.getType();
        final int rotation = object.getRotation();
        if (x < 0 || y < 0 || x >= map.getMasks()[plane].length || y >= map.getMasks()[plane][x].length) {
            return;
        }
        final ObjectDefinitions objectDefinition = ObjectDefinitions.getObjectDefinitions(object.getId());
        if (type == 22 ? objectDefinition.getClipType() != 1 : objectDefinition.getClipType() == 0) {
            return;
        }
        if (type >= 0 && type <= 3) {
            if (!objectDefinition.ignoreClipOnAlternativeRoute) {
                map.addWall(plane, x, y, type, rotation, objectDefinition.isProjectileCliped(), !objectDefinition.ignoreClipOnAlternativeRoute);
            }
            if (objectDefinition.isProjectileCliped()) {
                clipedOnlyMap.addWall(plane, x, y, type, rotation, objectDefinition.isProjectileCliped(), !objectDefinition.ignoreClipOnAlternativeRoute);
            }
        } else if (type >= 9 && type <= 21) {
            int sizeX;
            int sizeY;
            if (rotation != 1 && rotation != 3) {
                sizeX = objectDefinition.getSizeX();
                sizeY = objectDefinition.getSizeY();
            } else {
                sizeX = objectDefinition.getSizeY();
                sizeY = objectDefinition.getSizeX();
            }
            map.addObject(plane, x, y, sizeX, sizeY, objectDefinition.isProjectileCliped(), !objectDefinition.ignoreClipOnAlternativeRoute);
            if (objectDefinition.isProjectileCliped()) {
                clipedOnlyMap.addObject(plane, x, y, sizeX, sizeY, objectDefinition.isProjectileCliped(), !objectDefinition.ignoreClipOnAlternativeRoute);
            }
        } else if (type == 22) {
            map.addFloor(plane, x, y); // dont ever fucking think about removing
            // it..., some floor deco objects DOES
            // BLOCK WALKING
        }
    }

    public void unclip(final int plane, final int x, final int y) {
        if (map == null) {
            map = new RegionMap(regionId, false);
        }
        if (clipedOnlyMap == null) {
            clipedOnlyMap = new RegionMap(regionId, true);
        }
        map.setMask(plane, x, y, 0);
    }

    public void clip(final int plane, final int x, final int y) {
        if (map == null) {
            map = new RegionMap(regionId, false);
        }
        if (clipedOnlyMap == null) {
            clipedOnlyMap = new RegionMap(regionId, true);
        }
        map.setMask(plane, x, y, 131328);
    }

    public void unclip(final WorldObject object, final int x, final int y) {
        if (map == null) {
            map = new RegionMap(regionId, false);
        }
        if (clipedOnlyMap == null) {
            clipedOnlyMap = new RegionMap(regionId, true);
        }
        final int plane = object.getPlane();
        final int type = object.getType();
        final int rotation = object.getRotation();
        if (x < 0 || y < 0 || x >= map.getMasks()[plane].length || y >= map.getMasks()[plane][x].length) {
            return;
        }
        final ObjectDefinitions objectDefinition = ObjectDefinitions.getObjectDefinitions(object.getId()); // load
        // here
        if (type == 22 ? objectDefinition.getClipType() != 1 : objectDefinition.getClipType() == 0) {
            return;
        }
        if (type >= 0 && type <= 3) {
            map.removeWall(plane, x, y, type, rotation, objectDefinition.isProjectileCliped(), !objectDefinition.ignoreClipOnAlternativeRoute);
            if (objectDefinition.isProjectileCliped()) {
                clipedOnlyMap.removeWall(plane, x, y, type, rotation, objectDefinition.isProjectileCliped(), !objectDefinition.ignoreClipOnAlternativeRoute);
            }
        } else if (type >= 9 && type <= 21) {
            int sizeX;
            int sizeY;
            if (rotation != 1 && rotation != 3) {
                sizeX = objectDefinition.getSizeX();
                sizeY = objectDefinition.getSizeY();
            } else {
                sizeX = objectDefinition.getSizeY();
                sizeY = objectDefinition.getSizeX();
            }
            map.removeObject(plane, x, y, sizeX, sizeY, objectDefinition.isProjectileCliped(), !objectDefinition.ignoreClipOnAlternativeRoute);
            if (objectDefinition.isProjectileCliped()) {
                clipedOnlyMap.removeObject(plane, x, y, sizeX, sizeY, objectDefinition.isProjectileCliped(), !objectDefinition.ignoreClipOnAlternativeRoute);
            }
        } else if (type == 22) {
            map.removeFloor(plane, x, y);
        }
    }

    public void spawnObject(WorldObject object, final int plane, final int localX, final int localY, final boolean original) {
        if (objects == null) {
            objects = new Short2ObjectOpenHashMap<WorldObject>();
        }

        final int slot = OBJECT_SLOTS[object.getType()];
        final short hash = (short) (localX | localY << 6 | slot << 12 | plane << 14);
        if (original) {
            objects.put(hash, object);
            clip(object, localX, localY);
        } else {
            final WorldObject spawned = getSpawnedObjectWithSlot(plane, localX, localY, slot);
            if (spawned != null) {
                spawnedObjects.remove(spawned);
                unclip(spawned, localX, localY);
            }
            final WorldObject removed = getRemovedObjectWithSlot(plane, localX, localY, slot);
            if (removed != null) {
                object = removed;
                removedOriginalObjects.remove(object);
            } else if (objects.get(hash) != object) {
                spawnedObjects.add(object);
                val obj = objects.get(hash);
                if (obj != null) {
                    unclip(obj, localX, localY);
                }
            } else if (spawned == null) {
                if (Settings.DEBUG) {
                    Logger.getGlobal().info("Requested object to spawn is already spawned.(Shouldnt happen)");
                }
                return;
            }
            clip(object, localX, localY);
            for (final Player p2 : World.getPlayers()) {
                if (p2 == null || p2.hasFinished() || !p2.getMapRegionsIds().contains(regionId)) {
                    continue;
                }
                p2.getPackets().sendSpawnedObject(object);
            }
        }
    }

    public void spawnObject(final Player player, WorldObject object, final int plane, final int localX, final int localY, final boolean original) {
        if (objects == null) {
            objects = new Short2ObjectOpenHashMap<WorldObject>();
        }
        final int slot = OBJECT_SLOTS[object.getType()];
        final short hash = (short) (localX | localY << 6 | slot << 12 | plane << 14);
        if (original) {
            objects.put(hash, object);
            clip(object, localX, localY);
        } else {
            final WorldObject spawned = getSpawnedObjectWithSlot(plane, localX, localY, slot);
            if (spawned != null) {
                spawnedObjects.remove(spawned);
                unclip(spawned, localX, localY);
            }
            final WorldObject removed = getRemovedObjectWithSlot(plane, localX, localY, slot);
            if (removed != null) {
                object = removed;
                removedOriginalObjects.remove(object);
            } else if (objects.get(hash) != object) {
                spawnedObjects.add(object);
                val obj = objects.get(hash);
                if (obj != null) {
                    unclip(obj, localX, localY);
                }
            } else if (spawned == null) {
                if (Settings.DEBUG) {
                    Logger.getGlobal().info("Requested object to spawn is already spawned.(Shouldnt happen)");
                }
                return;
            }
            clip(object, localX, localY);
            if (player == null || player.hasFinished() || !player.getMapRegionsIds().contains(regionId)) {
                return;
            }
            player.getPackets().sendSpawnedObject(object);
        }
    }

    public void removeObject(WorldObject object, final int plane, final int localX, final int localY) {
        if (objects == null) {
            objects = new Short2ObjectOpenHashMap<WorldObject>();
        }
        final int slot = OBJECT_SLOTS[object.getType()];
        final short hash = (short) (localX | localY << 6 | slot << 12 | plane << 14);
        final WorldObject removed = getRemovedObjectWithSlot(plane, localX, localY, slot);

        if (removed != null) {
            removedOriginalObjects.remove(object);
            clip(removed, localX, localY);
        }
        WorldObject original = null;
        // found non original object on this slot. removing it since we
        // replacing with real one or none if none
        final WorldObject spawned = getSpawnedObjectWithSlot(plane, localX, localY, slot);
        if (spawned != null) {
            object = spawned;
            spawnedObjects.remove(object);
            unclip(object, localX, localY);
            if (objects.get(hash) != null) {// original
                // unclips non original to clip original above
                val obj = objects.get(hash);
                clip(obj, localX, localY);
                original = obj;
            }
            // found original object on this slot. removing it since requested
        } else if (objects.get(hash) != null && objects.get(hash).equals(object)) { // removes
            // original
            unclip(object, localX, localY);
            removedOriginalObjects.add(object);
        } else {
            if (Settings.DEBUG) {
                Logger.getGlobal().info("Requested object to remove wasnt found.(Shouldnt happen "+object.getId()+")");
            }
            return;
        }
        for (final Player p2 : World.getPlayers()) {
            if (p2 == null || p2.hasFinished() || !p2.getMapRegionsIds().contains(regionId)) {
                continue;
            }
            if (original != null) {
                p2.getPackets().sendSpawnedObject(original);
            } else {
                p2.getPackets().sendDestroyObject(object);
            }
        }

    }

    public void removeObject(WorldObject object, final int plane, final int localX, final int localY, final boolean removeClip) {
        if (objects == null) {
            objects = new Short2ObjectOpenHashMap<WorldObject>();
        }
        final int slot = OBJECT_SLOTS[object.getType()];
        final short hash = (short) (localX | localY << 6 | slot << 12 | plane << 14);
        final WorldObject removed = getRemovedObjectWithSlot(plane, localX, localY, slot);
        if (removed != null) {
            removedOriginalObjects.remove(object);
            if (removeClip) {
                clip(removed, localX, localY);
            }
        }
        WorldObject original = null;
        final WorldObject spawned = getSpawnedObjectWithSlot(plane, localX, localY, slot);
        if (spawned != null) {
            object = spawned;
            spawnedObjects.remove(object);
            if (removeClip) {
                unclip(object, localX, localY);
            }
            if (objects.get(hash) != null) {// original
                // unclips non original to clip original above
                val obj = objects.get(hash);
                clip(obj, localX, localY);
                original = obj;
            }
            // found original object on this slot. removing it since requested
        } else if (objects.get(hash) != null && objects.get(hash).equals(object)) {
            if (removeClip) {
                unclip(object, localX, localY);
            }
            removedOriginalObjects.add(object);
        } else {
            if (Settings.DEBUG) {
                Logger.getGlobal().info("Requested object to remove wasnt found.(Shouldnt happen)");
            }
            return;
        }
        for (final Player p2 : World.getPlayers()) {
            if (p2 == null || p2.hasFinished() || !p2.getMapRegionsIds().contains(regionId)) {
                continue;
            }
            if (original != null) {
                p2.getPackets().sendSpawnedObject(original);
            } else {
                p2.getPackets().sendDestroyObject(object);
            }
        }

    }

    public void removeObject(final Player player, WorldObject object, final int plane, final int localX, final int localY) {
        if (objects == null) {
            objects = new Short2ObjectOpenHashMap<WorldObject>();
        }
        final int slot = OBJECT_SLOTS[object.getType()];
        final short hash = (short) (localX | localY << 6 | slot << 12 | plane << 14);
        final WorldObject removed = getRemovedObjectWithSlot(plane, localX, localY, slot);
        if (removed != null) {
            removedOriginalObjects.remove(object);
            clip(removed, localX, localY);
        }
        WorldObject original = null;
        final WorldObject spawned = getSpawnedObjectWithSlot(plane, localX, localY, slot);
        if (spawned != null) {
            object = spawned;
            spawnedObjects.remove(object);
            unclip(object, localX, localY);
            if (objects.get(hash) != null) {
                val obj = objects.get(hash);
                clip(obj, localX, localY);
                original = obj;
            }
        } else if (objects.get(hash) != null && objects.get(hash).equals(object)) {
            unclip(object, localX, localY);
            removedOriginalObjects.add(object);
        } else {
            if (Settings.DEBUG) {
                Logger.getGlobal().info("Requested object to remove wasnt found.(Shouldnt happen)");
            }
            return;
        }

        if (player == null || player.hasFinished()) {
            return;
        }
        if (!player.getMapRegionsIds().contains(regionId)) {
            Logger.getGlobal().info("WE STOP REMOVING OMG THIS FUCKER REGION");
            return;
        }
        if (original != null) {
            player.getPackets().sendSpawnedObject(original);
        } else {
            player.getPackets().sendDestroyObject(object);
        }

    }

    public void removeObject(final Player player, WorldObject object, final int plane, final int localX, final int localY, final boolean removeClip) {
        if (objects == null) {
            objects = new Short2ObjectOpenHashMap<WorldObject>();
        }
        final int slot = OBJECT_SLOTS[object.getType()];
        final short hash = (short) (localX | localY << 6 | slot << 12 | plane << 14);
        final WorldObject removed = getRemovedObjectWithSlot(plane, localX, localY, slot);
        if (removed != null) {
            removedOriginalObjects.remove(object);
            if (removeClip) {
                clip(removed, localX, localY);
            }
        }
        WorldObject original = null;
        final WorldObject spawned = getSpawnedObjectWithSlot(plane, localX, localY, slot);
        if (spawned != null) {
            object = spawned;
            spawnedObjects.remove(object);
            if (removeClip) {
                unclip(object, localX, localY);
            }
            if (objects.get(hash) != null) {
                val obj = objects.get(hash);
                if (removeClip) {
                    clip(obj, localX, localY);
                }
                original = obj;
            }
        } else if (objects.get(hash) != null && objects.get(hash).equals(object)) {
            if (removeClip) {
                unclip(object, localX, localY);
            }
            removedOriginalObjects.add(object);
        } else {
            if (Settings.DEBUG) {
                Logger.getGlobal().info("Requested object to remove wasnt found.(Shouldnt happen)");
            }
            return;
        }

        if (player == null || player.hasFinished() || !player.getMapRegionsIds().contains(regionId)) {
            return;
        }
        if (original != null) {
            player.getPackets().sendSpawnedObject(original);
        } else {
            player.getPackets().sendDestroyObject(object);
        }

    }

    public WorldObject getStandartObject(final int plane, final int x, final int y) {
        return getObjectWithSlot(plane, x, y, OBJECT_SLOT_FLOOR);
    }

    public WorldObject getObjectWithType(final int plane, final int x, final int y, final int type) {
        final WorldObject object = getObjectWithSlot(plane, x, y, OBJECT_SLOTS[type]);
        return object != null && object.getType() == type ? object : null;
    }

    public WorldObject getObjectWithSlot(final int plane, final int x, final int y, final int slot) {
        if (objects == null) {
            return null;
        }
        final WorldObject o = getSpawnedObjectWithSlot(plane, x, y, slot);
        if (o == null) {
            if (getRemovedObjectWithSlot(plane, x, y, slot) != null) {
                return null;
            }
            final short hash = (short) (x | y << 6 | slot << 12 | plane << 14);
            return objects.get(hash);
        }
        return o;
    }

    public WorldObject getSpawnedObjectWithSlot(final int plane, final int x, final int y, final int slot) {
        for (final WorldObject object : spawnedObjects) {
            if (object.getXInRegion() == x && object.getYInRegion() == y && object.getPlane() == plane && OBJECT_SLOTS[object.getType()] == slot) {
                return object;
            }
        }
        return null;
    }

    public WorldObject getRemovedObjectWithSlot(final int plane, final int x, final int y, final int slot) {
        for (final WorldObject object : removedOriginalObjects) {
            if (object.getXInRegion() == x && object.getYInRegion() == y && object.getPlane() == plane && OBJECT_SLOTS[object.getType()] == slot) {
                return object;
            }
        }
        return null;
    }

    public WorldObject[] getAllObjects(final int plane, final int x, final int y) {
        if (objects == null) {
            return null;
        }
        val objects = new ArrayList<WorldObject>();
        for (int i = 0; i < 4; i++) {
            final short hash = (short) (x | y << 6 | i << 12 | plane << 14);
            final WorldObject object = this.objects.get(hash);
            if (object != null) {
                objects.add(object);
            }
        }
        return objects.size() == 0 ? null : objects.toArray(new WorldObject[objects.size()]);
    }

    public List<WorldObject> getAllObjects() {
        if (objects == null) {
            return null;
        }
        return new ArrayList<WorldObject>(objects.values());
        /*
         * final List<WorldObject> list = new ArrayList<WorldObject>(); for (int z = 0;
         * z < 4; z++) { for (int x = 0; x < 64; x++) { for (int y = 0; y < 64; y++) {
         * if (objects[z][x][y] == null) { continue; } for (final WorldObject o :
         * objects[z][x][y]) { if (o != null) { list.add(o); } } } } } return list;
         */
    }

    public boolean containsObjectWithId(final int plane, final int x, final int y, final int id) {
        final WorldObject object = getObjectWithId(plane, x, y, id);
        return object != null && object.getId() == id;
    }

    public WorldObject getObjectWithId(final int plane, final int x, final int y, final int id) {
        if (objects == null) {
            return null;
        }
        for (final WorldObject object : removedOriginalObjects) {
            if (object.getId() == id && object.getXInRegion() == x && object.getYInRegion() == y && object.getPlane() == plane) {
                return null;
            }
        }
        for (int i = 0; i < 4; i++) {
            final short hash = (short) (x | y << 6 | i << 12 | plane << 14);
            final WorldObject object = objects.get(hash);
            if (object != null && object.getId() == id) {
                final WorldObject spawned = getSpawnedObjectWithSlot(plane, x, y, OBJECT_SLOTS[object.getType()]);
                return spawned == null ? object : spawned;// Previously: return spawned == null ? object : null;
            }
        }
        for (final WorldObject object : spawnedObjects) {
            if (object.getXInRegion() == x && object.getYInRegion() == y && object.getPlane() == plane && object.getId() == id) {
                return object;
            }
        }
        return null;
    }

    public List<WorldObject> getSpawnedObjects() {
        return spawnedObjects;
    }

    public List<WorldObject> getRemovedOriginalObjects() {
        return removedOriginalObjects;
    }

    public void loadRegionMap() {
        final int regionX = (regionId >> 8);
        final int regionY = (regionId & 0xff);
        final int archiveId = Utils.getMapArchiveId(regionX, regionY);

        if (Cache.isFlatReadOnly()) {
            final com.rs.cache.filestore.store.Index index = Cache.STORE.getIndexes()[5];
            loadNative950RegionMap(index.fileExists(archiveId, 3), index.fileExists(archiveId, 0),
                    () -> index.getFile(archiveId, 3), () -> index.getFile(archiveId, 0));
            return;
        }

        // 1. If the archive doesn't exist in the index, stop immediately
        if (archiveId == -1) {
            return;
        }

        // 2. Safely grab the settings data (Index 5, File 3)
        final byte[] mapSettingsData = Cache.STORE.getIndexes()[5].getFile(archiveId, 3);

        // 3. CRITICAL: Check null BEFORE passing to loadMapSettings
        if (mapSettingsData == null) {
            if (Cache.isFlatReadOnly()) throw new IllegalStateException("Missing map settings group " + archiveId);
            return;
        }

        // Now it is safe to process
        final byte[][][] mapSettings = loadMapSettings(mapSettingsData);

        // 4. Safely grab the objects data (Index 5, File 0)
        final byte[] objectsData = Cache.STORE.getIndexes()[5].getFile(archiveId, 0);
        if (objectsData != null) {
            loadMapObjects(objectsData, regionX, regionY, mapSettings);
        } else if (Cache.isFlatReadOnly()) {
            throw new IllegalStateException("Missing map objects group " + archiveId);
        }
    }

    /** Native scene borders can contain metadata only (e.g. group9771/file5 beside Air altar).
     * Only reference-table absence of BOTH world files makes a void square. A declared file
     * that cannot be read, locations without terrain, or corrupt payload still fails closed.
     */
    void loadNative950RegionMap(final boolean hasSettings, final boolean hasObjects,
            final java.util.function.Supplier<byte[]> readSettings,
            final java.util.function.Supplier<byte[]> readObjects) {
        if (!hasSettings && !hasObjects) {
            final int[][][] movement = forceGetRegionMap().getMasks();
            final int[][][] projectiles = forceGetRegionMapClipedOnly().getMasks();
            for (int plane = 0; plane < 4; plane++) for (int x = 0; x < 64; x++) {
                java.util.Arrays.fill(movement[plane][x], -1);
                java.util.Arrays.fill(projectiles[plane][x], -1);
            }
            return;
        }
        final int archive = Utils.getMapArchiveId(regionId >> 8, regionId & 255);
        if (!hasSettings)
            throw new IllegalStateException("Native map locations without terrain group " + archive);
        final byte[] settings = readSettings.get(), objectsData = hasObjects ? readObjects.get() : null;
        if (settings == null || (hasObjects && objectsData == null))
            throw new IllegalStateException("Declared native map world file unavailable group " + archive);
        final byte[][][] mapSettings = loadMapSettings(settings);
        if (hasObjects) loadMapObjects(objectsData, regionId >> 8, regionId & 255, mapSettings);
    }

    public byte[][][] loadMapSettings(final byte[] data) {
        byte[][][] mapSettings;
        if (data != null) {
            mapSettings = new byte[4][64][64];
            final InputStream stream = new InputStream(data, Cache.isFlatReadOnly());
            if (Cache.isFlatReadOnly()) {
                // RS3 >=936 tile format: jagx/version1 prefix and 16-bit heights.
                // Schema: skillbert/rsmv src/opcodes/mapsquare_tiles.jsonc.
                if (stream.readInt() != 0x6a616778 || stream.readUnsignedByte() != 1)
                    throw new IllegalArgumentException("Unsupported modern map settings header");
            }
            for (int plane = 0; plane < 4; plane++) {
                for (int x = 0; x < 64; x++) {
                    for (int y = 0; y < 64; y++) {

                        final int value = stream.readUnsignedByte();
                        if (Cache.isFlatReadOnly() && (value & ~15) != 0)
                            throw new IllegalArgumentException("Unsupported map setting flags " + value + " at " + (stream.getOffset() - 1));
                        if ((value & 0x1) != 0) {
                            stream.readUnsignedByte();
                            int overlayId = (short) stream.readUnsignedSmart();
                            if (!Cache.isFlatReadOnly() && regionId == 16475) {
                                if (overlayId == 112)
                                    forceSetMask(plane, x, y, Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ_BLOCKSWALK_ALTERNATIVE);
                            }
                        }
                        if ((value & 0x2) != 0) {
                            mapSettings[plane][x][y] = (byte) stream.readByte();

                        }
                        if ((value & 0x4) != 0) {
                            stream.readUnsignedSmart(); // setted to 30

                        }
                        if ((value & 0x8) != 0) {
                            if (Cache.isFlatReadOnly()) stream.readUnsignedShort();
                            else stream.readUnsignedByte();

                        }
                    }
                }
            }
            if (Cache.isFlatReadOnly()) {
                stream.skip(8); // Membership-area bitmap after exactly 16384 collision tiles.
                // Remaining environment/lighting records are rendering data. Their
                // container is CRC-validated; they are not inputs to collision.
            }
            for (int plane = 0; plane < 4; plane++) {
                for (int x = 0; x < 64; x++) {
                    for (int y = 0; y < 64; y++) {
                        if ((mapSettings[plane][x][y] & 0x1) == 1) {
                            int realPlane = plane;
                            if ((mapSettings[1][x][y] & 2) == 2) {
                                realPlane--;
                            }
                            if (realPlane >= 0) {
                                forceGetRegionMap().addFloor(realPlane, x, y);
                            }
                        }
                    }
                }
            }
        } else {
            mapSettings = null;
            for (int plane = 0; plane < 4; plane++) {
                for (int x = 0; x < 64; x++) {
                    for (int y = 0; y < 64; y++) {
                        forceGetRegionMap().addFloor(plane, x, y);
                    }
                }
            }
        }
        return mapSettings;
    }

    public void loadMapObjects(final byte[] data, final int regionX, final int regionY, final byte[][][] mapSettings) {
        final InputStream landStream = new InputStream(data, Cache.isFlatReadOnly());
        int objectId = -1;
        int incr;
        while ((incr = landStream.readSmart2()) != 0) {
            objectId += incr;
            int location = 0;
            int incr2;
            while ((incr2 = landStream.readUnsignedSmart()) != 0) {
                location += incr2 - 1;
                final int localX = (location >> 6 & 0x3f);
                final int localY = (location & 0x3f);
                final int plane = location >> 12;
                final int objectDataOffset = landStream.getOffset();
                ObjectData objectData = new ObjectData(landStream);
                final int type = objectData.type;
                final int rotation = objectData.rotation;
                if (Cache.isFlatReadOnly() && (type < 0 || type >= OBJECT_SLOTS.length || plane < 0 || plane >= 4))
                    throw new IllegalArgumentException("Invalid modern map object " + objectId + " type/plane " + type + "/" + plane);
                if (localX < 0 || localX >= 64 || localY < 0 || localY >= 64) {
                    continue;
                }
                int objectPlane = plane;
                if (mapSettings != null && (mapSettings[1][localX][localY] & 2) == 2) {
                    objectPlane--;
                }
                if (objectPlane < 0 || objectPlane >= 4 || plane < 0 || plane >= 4) {
                    continue;
                }
                WorldObject mapObject = new WorldObject(objectId, type, rotation, localX + regionX * 64, localY + regionY * 64, objectPlane);
                if (Cache.isFlatReadOnly() && objectData.aClass448_8204 != null)
                    mapObject.setNative950MapTransform(java.util.Arrays.copyOfRange(data, objectDataOffset + 1, landStream.getOffset()));
                spawnObject(mapObject, objectPlane, localX, localY, true);
            }
        }
        if (Cache.isFlatReadOnly() && landStream.getRemaining() != 0)
            throw new IllegalArgumentException("Trailing map object bytes at " + landStream.getOffset());
    }

    public int getRotation(final int plane, final int x, final int y) {
        return 0;
    }

    /**
     * Get's ground item with specific id on the specific location in this region.
     */
    public FloorItem getGroundItem(final int id, final WorldTile tile, final Player player) {
        if (groundItems == null) {
            return null;
        }
        for (final FloorItem item : groundItems) {
            if ((item.isInvisible()) && (item.hasOwner() && !player.getUsername().equals(item.getOwner()))) {
                continue;
            }
            if (item.getId() == id && tile.getX() == item.getTile().getX() && tile.getY() == item.getTile().getY() && tile.getPlane() == item.getTile().getPlane()) {
                return item;
            }
        }
        return null;
    }

    /**
     * Return's list of ground items that are currently loaded. List may be null if
     * there's no ground items. Modifying given list is prohibited.
     *
     * @return
     */
    public List<FloorItem> getGroundItems() {
        return groundItems;
    }

    /**
     * Return's list of ground items that are currently loaded. This method ensures
     * that returned list is not null. Modifying given list is prohibited.
     *
     * @return
     */
    public List<FloorItem> getGroundItemsSafe() {
        if (groundItems == null) {
            groundItems = new CopyOnWriteArrayList<FloorItem>();
        }
        return groundItems;
    }

    public List<Integer> getPlayerIndexes() {
        return playersIndexes;
    }

    public int getPlayerCount() {
        return playersIndexes == null ? 0 : playersIndexes.size();
    }

    public List<Integer> getNPCsIndexes() {
        return npcsIndexes;
    }

    public void addPlayerIndex(final int index) {
        // creates list if doesnt exist
        if (playersIndexes == null) {
            playersIndexes = new CopyOnWriteArrayList<Integer>();
        }
        playersIndexes.add(index);
    }

    public void addNPCIndex(final int index) {
        // creates list if doesnt exist
        if (npcsIndexes == null) {
            npcsIndexes = new CopyOnWriteArrayList<Integer>();
        }
        npcsIndexes.add(index);
    }

    public void removePlayerIndex(final Integer index) {
        if (playersIndexes == null) {
            return;
        }
        playersIndexes.remove(index);
    }

    public boolean removeNPCIndex(final Object index) {
        if (npcsIndexes == null) {
            return false;
        }
        return npcsIndexes.remove(index);
    }

    public void loadMusicIds() {
        String[] musicNames = getMusicNames(regionId);
        if (musicNames.length == 0)
            return;
        musicIds = new int[musicNames.length];
        for (int i = 0; i < musicNames.length; i++)
            musicIds[i] = getMusicId(musicNames[i]);
    }

    public int getRandomMusicId() {
        if (musicIds == null || musicIds.length == 0)
            return -1;
        return musicIds[Utils.random(musicIds.length)];
    }
    
    public int[] getMusicIds() {
        return musicIds;
    }
    
    public int getLoadMapStage() {
        return loadMapStage;
    }

    public void setLoadMapStage(final int loadMapStage) {
        this.loadMapStage = loadMapStage;
    }

    public boolean isLoadedObjectSpawns() {
        return loadedObjectSpawns;
    }

    public void setLoadedObjectSpawns(final boolean loadedObjectSpawns) {
        this.loadedObjectSpawns = loadedObjectSpawns;
    }

    public boolean isLoadedNPCSpawns() {
        return loadedNPCSpawns;
    }

    public void setLoadedNPCSpawns(final boolean loadedNPCSpawns) {
        this.loadedNPCSpawns = loadedNPCSpawns;
    }

    public int getRegionId() {
        return regionId;
    }

    public boolean isLoadedItemSpawns() {
        return loadedItemSpawns;
    }

    public void setLoadedItemSpawns(final boolean loadedItemSpawns) {
        this.loadedItemSpawns = loadedItemSpawns;
    }

    public WorldObject getRealObject(final int plane, final int x, final int y, final int type) {
        final short hash = (short) (x | y << 6 | OBJECT_SLOTS[type] << 12 | plane << 14);

        return objects.get(hash);
    }

    public boolean addProjectile(final Projectile projectile) {
        return projectiles.add(projectile);
    }

    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    public void removeProjectiles() {
        projectiles.clear();
    }

    public void forceSetMask(int plane, int localX, int localY, int mask) {
        if (map == null)
            map = new RegionMap(regionId, false);
        if (localX >= 64 || localY >= 64 || localX < 0 || localY < 0) {
            WorldTile tile = new WorldTile(map.getRegionX() + localX, map.getRegionY() + localY, plane);
            int regionId = tile.getRegionId();
            int newRegionX = (regionId >> 8) * 64;
            int newRegionY = (regionId & 0xff) * 64;
            World.getRegion(tile.getRegionId()).setMask(plane, tile.getX() - newRegionX, tile.getY() - newRegionY, mask);
            return;
        }
        map.setMask(plane, localX, localY, mask);
    }
    
    public static final String[] getMusicNames(int regionId) {

        List<String> names = new ArrayList<String>();

        switch (regionId) {
        case 11801: // exiled kalphite lair
            names.add("Coleoptera");
            break;
        case 11578:
            names.add("Frostbite");
            break;
        case 15173:
            names.add("Faces Obscura");
            break;
        case 14917:
            names.add("Dying Light");
            break;
        case 14916:
            names.add("Cliffhanger");
            break;
        case 15172:
            names.add("Empyrean Citadel");
            break;
        case 16738: // staff zone custom
            names.add("End Song");
            break;
        case 9011:
            names.add("Elven Elite");
            break;
        case 8244:
        case 8499:
        case 8500:
        case 8501:
        case 8755:
        case 8756:
        case 8757:
        case 9012:
        case 9013:
            names.add("Elven Daffodil");
            names.add("Elven Dhalia");
            names.add("Elven Heart");
            names.add("Elven Holly");
            names.add("Elven Sunlight");
            names.add("Elven Nightshade");
            names.add("Elven Sunrise");
            names.add("Elven Sunset");
            names.add("Elven Voice");
            names.add("Elven Rose");
            names.add("Elven Snapegrass");
            names.add("Elven Bluebell");
            names.add("Elven Lily");
            names.add("The Twilight Twain");
            names.add("Henceward!");
            names.add("Baxtorian's Hollow");
            names.add("Among Tirannwn Trees");
            break;
        case 9266: // Underground Pass exit
            names.add("Breeze");
            names.add("Elven Mist");
            break;
        }

        String name1 = getMusicName1(regionId);
        if (name1 != null)
            names.add(name1);
        String name2 = getMusicName2(regionId);
        if (name2 != null)
            names.add(name2);
        String name3 = getMusicName3(regionId);
        if (name3 != null)
            names.add(name3);

        return names.toArray(new String[names.size()]);

    }

}
