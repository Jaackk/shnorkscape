package com.rs.game.player.actions.summoning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.GeneralRequirementMap;
import com.rs.cache.loaders.InventionDefinitions;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.npc.others.DreadNip;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.RS3SkillsDialogue;
import com.rs.game.player.content.RS3SkillsDialogue.SkillDialogueResult;
import com.rs.game.player.controllers.Dungeoneering;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

public class Summoning {

    public enum Pouches {
        SPIRIT_WOLF(6830, 67, 12047, 1, 0.1D, 4.8D, 0x57e40L, 1, 12425), DREADFOWL(6825, 6825, 12043, 4, 0.1D, 9.3D, 0x3a980L, 1, 12445), SPIRIT_SPIDER(6841, 83, 12059, 8, 0.2D, 12.6D, 0xdbba0L, 2, 12428), THORNY_SNAIL(6807, 119, 12019, 13, 0.2D, 12.6D, 0xea600L, 2, 12459), GRANITE_CRAB(6796, 75, 12009, 16, 0.2D, 21.6D, 0x107ac0L, 2, 12533), SPIRIT_MOSQUITO(7332, 177, 12778, 17, 0.2D, 46.5D, 0xafc80L, 2, 12838), DESERT_WYRM(6832, 121, 12049, 18, 0.4D, 31.2D, 0x116520L, 1, 12460), SPIRIT_SCORPIAN(6838, 101, 12055, 19, 0.9D, 83.2D, 0xf9060L, 2, 12432), SPIRIT_TZ_KIH(7362, 179, 12808, 22, 1.1D, 96.8D, 0x107ac0L, 3, 12839), ALBINO_RAT(6848, 103, 12067, 23, 2.3D, 202.4D, 0x142440L, 3, 12430), SPIRIT_KALPHITE(6994, 99, 12063, 25, 2.5D, 220D, 0x142440L, 3, 12446), COMPOST_MOUNT(6872, 137, 12091, 28, 0.6D, 49.8D, 0x15f900L, 6, 12440), GIANT_CHINCHOMPA(7353, 165, 12800, 29, 2.5D, 255.2D, 0x1c61a0L, 1, 12834), VAMPYRE_BAT(6836, 71, 12053, 31, 1.6D, 136D, 0x1e3660L, 4, 12447), HONEY_BADGER(6846, 105, 12065, 32, 1.6D, 140.8D, 0x16e360L, 4, 12433), BEAVER(6808, 89, 12021, 33, 0.7D, 57.6D, 0x18b820L, 4, 12429), VOID_RAVAGER(7371, 157, 12818, 34, 0.7D, 59.6D, 0x18b820L, 4, 12443), VOID_SPINNER(7334, 157, 12780, 34, 0.7D, 59.6D, 0x18b820L, 4, 12443), VOID_TORCHER(7352, 157, 12798, 34, 0.7D, 59.6D, 0x560f40L, 4, 12443), VOID_SHIFTER(7367, 157, 12814, 34, 0.7D, 59.6D, 0x560f40L, 4, 12443), BRONZE_MINOTAUR(6854, 149, 12073, 36, 2.4D, 316.8D, 0x1b7740L, 9, 12461), BULL_ANT(6868, 91, 12087, 40, 0.6D, 52.8D, 0x1b7740L, 5, 12431), MACAW(6852, 73, 12071, 41, 0.8D, 72.4D, 0x1c61a0L, 5, 12422), EVIL_TURNIP(6834, 77, 12051, 42, 2.1D, 184.8D, 0x1b7740L, 5, 12448), SPIRIT_COCKATRICE(6876, 149, 12095, 43, 0.9D, 75.2D, 0x20f580L, 5, 12458), SPIRIT_GUTHATRICE(6878, 149, 12097, 43, 0.9D, 75.2D, 0x20f580L, 5, 12458), SPIRIT_SARATRICE(6880, 149, 12099, 43, 0.9D, 75.2D, 0x20f580L, 5, 12458), SPIRIT_ZAMATRICE(6882, 149, 12101, 43, 0.9D, 75.2D, 0x20f580L, 5, 12458), SPIRIT_PENGATRICE(6884, 149, 12103, 43, 0.9D, 75.2D, 0x20f580L, 5, 12458), SPIRIT_CORAXATRICE(6886, 149, 12105, 43, 0.9D, 75.2D, 0x20f580L, 5, 12458), SPIRIT_VULATRICE(6888, 149, 12107, 43, 0.9D, 75.2D, 0x20f580L, 5, 12458), IRON_MINOTAUR(6856, 149, 12075, 46, 4.6D, 404.8D, 0x21dfe0L, 9, 12462), PYRELORD(7378, 185, 12816, 46, 2.3D, 202.4D, 0x1d4c00L, 5, 12829), MAGPIE(6824, 81, 12041, 47, 0.9D, 83.2D, 0x1f20c0L, 5, 12426), BLOATED_LEECH(6844, 131, 12061, 49, 2.4D, 215.2D, 0x1f20c0L, 5, 12444), SPIRIT_TERRORBIRD(6795, 129, 12007, 52, 0.7D, 68.4D, 0x20f580L, 6, 12441), ABYSSAL_PARASITE(6819, 125, 12035, 54, 1.1D, 94.8D, 0x1b7740L, 6, 12454), SPIRIT_JELLY(6993, 123, 12027, 55, 5.5D, 484D, 0x275e20L, 6, 12453), STEEL_MINOTAUR(6858, 149, 12077, 56, 5.6D, 492.8D, 0x2a1d40L, 9, 12463), IBIS(6991, 85, 12531, 56, 1.1D, 98.8D, 0x22ca40L, 6, 12424), SPIRIT_KYATT(7364, 169, 12812, 57, 5.7D, 501.6D, 0x2cdc60L, 6, 12836), SPIRIT_LARUPIA(7366, 181, 12784, 57, 5.7D, 501.6D, 0x2cdc60L, 6, 12840), SPIRIT_GRAAHK(7338, 167, 12810, 57, 5.6D, 501.6D, 0x2cdc60L, 6, 12835), KARAMTHULU_OVERLOAD(6810, 135, 12023, 58, 5.8D, 510.4D, 0x284880L, 6, 12455), SMOKE_DEVIL(6866, 133, 12085, 61, 3.1D, 268D, 0x2bf200L, 7, 12468), ABYSSAL_LURKER(6821, 87, 12037, 62, 1.9D, 109.6D, 0x258960L, 7, 12427), SPIRIT_COBRA(6803, 115, 12015, 63, 3.1D, 276.8D, 0x334500L, 7, 12436), STRANGER_PLANT(6828, 141, 12045, 64, 3.2D, 281.6D, 0x2cdc60L, 7, 12467), MITHRIL_MINOTAUR(6860, 149, 12079, 66, 6.6D, 580.8D, 0x325aa0L, 9, 12464), BARKER_TOAD(6890, 107, 12123, 66, 1.0D, 87D, 0x75300L, 7, 12452), WAR_TORTOISE(6816, 117, 12031, 67, 0.7D, 58.6D, 0x275e20L, 7, 12439), BUNYIP(6814, 153, 12029, 68, 1.4D, 119.2D, 0x284880L, 7, 12438), FRUIT_BAT(6817, 79, 12033, 69, 1.4D, 121.2D, 0x2932e0L, 7, 12423), RAVENOUS_LOCUST(7374, 97, 12820, 70, 1.5D, 132D, 0x15f900L, 4, 12830), ARCTIC_BEAR(6840, 109, 12057, 71, 1.1D, 93.2D, 0x19a280L, 8, 12451), PHEONIX(8575, -1, 14623, 72, 3D, 301D, 0x1b7740L, 8, 14622), OBSIDIAN_GOLEM(7346, 173, 12792, 73, 7.3D, 642.4D, 0x325aa0L, 8, 12826), GRANITE_LOBSTER(6850, 93, 12069, 74, 3.7D, 325.6D, 0x2c8e40L, 8, 12449), PRAYING_MANTIS(6799, 95, 12011, 75, 3.6D, 329.6D, 0x3f2be0L, 8, 12450), FORGE_REGENT(7336, 187, 12782, 76, 1.5D, 134D, 0x2932e0L, 9, 12841), ADAMANT_MINOTAUR(6862, 149, 12081, 76, 8.6D, 668.8D, 0x3c6cc0L, 9, 12465), TALON_BEAST(7348, 143, 12794, 77, 3.8D, 1015.2D, 0x2cdc60L, 9, 12831), GIANT_ENT(6801, 139, 12013, 78, 1.6D, 136.8D, 0x2cdc60L, 8, 12457), FIRE_TITAN(7356, 159, 12802, 79, 7.9D, 695.2D, 0x38c340L, 9, 12824), MOSS_TITAN(7358, 159, 12804, 79, 7.9D, 695.2D, 0x38c340L, 9, 12824), ICE_TITAN(7360, 159, 12806, 79, 7.9D, 695.2D, 0x38c340L, 9, 12824), HYDRA(6812, 145, 12025, 80, 1.6D, 140.8D, 0x2cdc60L, 8, 12442), SPIRIT_DAGANNOTH(6805, 147, 12017, 83, 4.1D, 364.8D, 0x342f60L, 9, 12456), LAVA_TITAN(7342, 171, 12788, 83, 8.37D, 730.4D, 0x37d8e0L, 9, 12837), SWAMP_TITAN(7330, 155, 12776, 85, 4.2D, 373.6D, 0x334500L, 9, 12832), RUNE_MINOTAUR(6864, 149, 12083, 86, 8.6D, 756.8D, 0x8a3ea0L, 9, 12466), UNICORN_STALLION(6823, 113, 12039, 88, 1.8D, 154.4D, 0x317040L, 9, 12434), GEYSER_TITAN(7340, 161, 12786, 89, 8.9D, 783.2D, 0x3f2be0L, 10, 12833), WOLPERTINGER(6870, 151, 12089, 92, 4.6D, 404.8D, 0x38c340L, 10, 12437), ABYSSAL_TITAN(7350, 175, 12796, 93, 1.9D, 163.2D, 0x1d4c00L, 10, 12827), IRON_TITAN(7376, 183, 12822, 95, 8.6D, 417.6D, 0x36ee80L, 10, 12828), PACK_YAK(6874, 111, 12093, 96, 4.8D, 422.2D, 0x3519c0L, 10, 12435), STEEL_TITAN(7344, 163, 12790, 99, 4.9D, 435.2D, 0x3a9800L, 10, 12825), NIGHTMARE_MUSPAH(14912, -1, 31328, 81, 16, 145.3, 3480000, 10, 31332), BRAWLER_DEMON(20612, -1, 34137, 82, 0, 346.9, 1860000, 9, 34146), EXECUTIONER_DEMON(20614, -1, 34139, 82, 0, 346.9, 1860000, 9, 34146), DEACON_DEMON(20616, -1, 34141, 82, 0, 346.9, 1860000, 9, 34146), BLOOD_NIHIL(14957, -1, 31410, 87, 8.7, 355, 5400000, 10, 31380), SHADOW_NIHIL(14961, -1, 31412, 87, 8.7, 355, 5400000, 10, 31380), SMOKE_NIHIL(14953, -1, 31414, 87, 8.7, 355, 5400000, 10, 31380), ICE_NIHIL(14965, -1, 31416, 87, 8.7, 355, 5400000, 10, 31380), LIGHT_CREATURE(20306, -1, 32829, 88, 9, 771.6, 3600000, 10, 32832), PACK_MAMMOTH(22005, -1, 36060, 99, 10, 439.8, 3840000, 20, 36056),

        /**
         * Dungeoneering: int npcId, int configId, int pouchId, int level, double
         * useExp, double creationExp, long time, int spawnCost, int scrollId
         */
        CUB_BLOODRAGER(11106, -1, 17935, 1, 0.5, 5.0, 2700000, 1, 18027), LITTLE_BLOODRAGER(11108, -1, 17936, 11, 1, 19.5, 2700000, 2, 18028), NAIVE_BLOODRAGER(11110, -1, 17937, 21, 1.5, 43, 2700000, 3, 18029), KEEN_BLOODRAGER(11112, -1, 17938, 31, 2, 68.5, 2700000, 4, 18030), BRAVE_BLOODRAGER(11114, -1, 17939, 41, 2.5, 99.5, 2700000, 5, 18031), BRAH_BLOODRAGER(11116, -1, 17940, 51, 3, 157, 2700000, 6, 18032), NAABE_BLOODRAGER(11118, -1, 17941, 61, 3.5, 220, 2700000, 7, 18033), WISE_BLOODRAGER(11120, -1, 17942, 71, 4, 325, 2700000, 8, 18034), ADEPT_BLOODRAGER(11122, -1, 17943, 81, 4.5, 517.5, 2700000, 9, 18035), SACHEM_BLOODRAGER(11124, -1, 17944, 91, 5, 810, 2700000, 10, 18036), CUB_DEATHSLINGER(11206, -1, 17985, 2, 0.6, 5.7, 2700000, 1, 18037), LITTLE_DEATHSLINGER(11208, -1, 17986, 12, 1.1, 20.5, 2700000, 2, 18038), NAIVE_DEATHSLINGER(11210, -1, 17987, 22, 1.6, 44.4, 2700000, 3, 18039), KEEN_DEATHSLINGER(11212, -1, 17988, 32, 2.1, 70.4, 2700000, 4, 18040), BRAVE_DEATHSLINGER(11214, -1, 17989, 42, 2.6, 102, 2700000, 5, 18041), BRAH_DEATHSLINGER(11216, -1, 17990, 52, 3.1, 160.5, 2700000, 6, 18042), NAABE_DEATHSLINGER(11218, -1, 17991, 62, 3.6, 224.6, 2700000, 7, 18043), WISE_DEATHSLINGER(11220, -1, 17992, 72, 4.1, 330.8, 2700000, 8, 18044), ADEPT_DEATHSLINGER(11222, -1, 17993, 82, 4.6, 524.6, 2700000, 9, 18045), SACHEM_DEATHSLINGER(11224, -1, 17994, 92, 5.1, 818.5, 2700000, 10, 18046), CUB_STORMBRINGER(11126, -1, 17945, 3, 0.7, 6.4, 2700000, 1, 18047), LITTLE_STORMBRINGER(11128, -1, 17946, 13, 1.2, 21.5, 2700000, 2, 18048), NAIVE_STORMBRINGER(11130, -1, 17947, 23, 1.7, 45.8, 2700000, 3, 18049), KEEN_STORMBRINGER(11132, -1, 17948, 33, 2.2, 72.3, 2700000, 4, 18050), BRAVE_STORMBRINGER(11134, -1, 17949, 43, 2.7, 104.5, 2700000, 5, 18051), BRAH_STORMBRINGER(11136, -1, 17950, 53, 3.2, 164, 2700000, 6, 18052), NAABE_STORMBRINGER(11138, -1, 17951, 63, 3.7, 229.2, 2700000, 7, 18053), WISE_STORMBRINGER(11140, -1, 17952, 73, 4.2, 336.6, 2700000, 8, 18054), ADEPT_STORMBRINGER(11142, -1, 17953, 83, 4.7, 531.7, 2700000, 9, 18055), SACHEM_STORMBRINGER(11144, -1, 17954, 93, 5.2, 827, 2700000, 10, 18056), CUB_HOARDSTALKER(11146, -1, 17955, 5, 0.8, 7.1, 3600000, 1, 18057), LITTLE_HOARDSTALKER(11148, -1, 17956, 15, 1.3, 22.5, 3600000, 2, 18058), NAIVE_HOARDSTALKER(11150, -1, 17957, 25, 1.8, 47.2, 3600000, 3, 18059), KEEN_HOARDSTALKER(11152, -1, 17958, 35, 2.3, 74.2, 3600000, 4, 18060), BRAVE_HOARDSTALKER(11154, -1, 17959, 45, 2.8, 107, 3600000, 5, 18061), BRAH_HOARDSTALKER(11156, -1, 17960, 55, 3.3, 167.5, 3600000, 6, 18062), NAABE_HOARDSTALKER(11158, -1, 17961, 65, 3.8, 233.8, 3600000, 7, 18063), WISE_HOARDSTALKER(11160, -1, 17962, 75, 4.3, 342.4, 3600000, 8, 18064), ADEPT_HOARDSTALKER(11162, -1, 17963, 85, 5.8, 538.8, 3600000, 9, 18065), SACHEM_HOARDSTALKER(11164, -1, 17964, 95, 6.3, 835.5, 3600000, 10, 18066), CUB_WORLDBEARER(11186, -1, 17975, 7, 0.9, 7.8, 3600000, 1, 18067), LITTLE_WORLDBEARER(11188, -1, 17976, 17, 1.4, 23.5, 3600000, 2, 18068), NAIVE_WORLDBEARER(11190, -1, 17977, 27, 1.9, 48.6, 3600000, 3, 18069), KEEN_WORLDBEARER(11192, -1, 17978, 37, 2.4, 76.1, 3600000, 4, 18070), BRAVE_WORLDBEARER(11194, -1, 17979, 47, 2.9, 109.5, 3600000, 5, 18071), BRAH_WORLDBEARER(11196, -1, 17980, 57, 3.4, 171, 3600000, 6, 18072), BAANE_WORLDBEARER(11198, -1, 17981, 67, 3.9, 238.4, 3600000, 7, 18073), WISE_WORLDBEARER(11200, -1, 17982, 77, 4.4, 348.2, 3600000, 8, 18074), ADEPT_WORLDBEARER(11202, -1, 17983, 87, 4.9, 545.9, 3600000, 9, 18075), SACHEM_WORLDBEARER(11204, -1, 17984, 97, 5.4, 844, 3600000, 10, 18076), CUB_SKINWEAVER(11166, -1, 17965, 9, 1, 8.5, 1800000, 1, 18077), LITTLE_SKINWEAVER(11168, -1, 17966, 19, 1.5, 24.5, 1800000, 2, 18078), NAIVE_SKINWEAVER(11170, -1, 17967, 29, 2, 50, 1800000, 3, 18079), KEEN_SKINWEAVER(11172, -1, 17968, 39, 2.5, 78, 1800000, 4, 18080), BRAVE_SKINWEAVER(11174, -1, 17969, 49, 3, 112, 1800000, 5, 18081), BRAH_SKINWEAVER(11176, -1, 17970, 59, 3.5, 174.5, 1800000, 6, 18082), NAABE_SKINWEAVER(11178, -1, 17971, 69, 4, 243, 1800000, 7, 18083), WISE_SKINWEAVER(11180, -1, 17972, 79, 4.5, 354, 1800000, 8, 18084), ADEPT_SKINWEAVER(11182, -1, 17973, 89, 5, 553, 1800000, 9, 18085), SACHEM_SKINWEAVER(11184, -1, 17974, 99, 5.5, 852.5, 1800000, 10, 18086);

        private final int npcId;
        private final int pouchId;
        private final int level;
        private final int spawnCost;
        private final double useExp;
        private final double creationExp;
        private int configId;
        private final long time;
        private final int scrollId;

        Pouches(int npcId, int configId, int pouchId, int level, double useExp, double creationExp, long time, int spawnCost, int scrollId) {
            this.npcId = npcId;
            this.pouchId = pouchId;
            this.level = level;
            this.spawnCost = spawnCost;
            this.useExp = useExp;
            this.creationExp = creationExp;
            this.time = time;
            this.scrollId = scrollId;
        }

        private static final HashMap<Integer, Pouches> POUCHES = new HashMap<Integer, Pouches>();
        private static final HashMap<Integer, Pouches> POUCHESBYNPC = new HashMap<Integer, Pouches>();

        static {
            for (Pouches p : values()) {
                POUCHES.put(p.getPouchId(), p);
                POUCHESBYNPC.put(p.getNpcId(), p);
            }
        }

        public static Pouches forId(int pouchId) {
            return POUCHES.get(pouchId);
        }

        public static Pouches forNpcId(int npcId) {
            return POUCHESBYNPC.get(npcId);
        }

        public int getConfigId() {
            return configId;
        }

        public double getCreationExp() {
            return creationExp;
        }

        public int getLevel() {
            return level;
        }

        public int getNpcId() {
            return npcId;
        }

        public int getPouchId() {
            return pouchId;
        }

        public int getScrollId() {
            return scrollId;
        }

        public int getSpawnCost() {
            return spawnCost;
        }

        public long getTime(boolean perk) {
            return perk ? (long) (time + (time * 0.50)) : time;
        }

        public double getUseExp() {
            return useExp;
        }
    }

    public static Familiar createFamiliar(Player player, Pouches pouch) {
        String loc = "com.rs.game.npc.familiar.";
        try {
            Familiar fam = (Familiar) Class.forName(loc + NPCDefinitions.getNPCDefinitions(pouch.getNpcId()).name.replace(" ", "").replace("-", "")).getConstructor(new Class[] { Player.class, Pouches.class, WorldTile.class, Integer.TYPE, Boolean.TYPE }).newInstance(new Object[] { player, pouch, player, Integer.valueOf(-1), Boolean.valueOf(true) });
            if (fam != null) {
                return fam;
            }
        } catch (Throwable e) {
            Logger.getGlobal().error("Missing: " + NPCDefinitions.getNPCDefinitions(pouch.getNpcId()).name.replace(" ", "").replace("-", "") + " - NPC Id: " + pouch.getNpcId(), e);
        }
        return null;
    }
    
    public static void spawnFamiliar(Player player, Pouches pouch) { 
       spawnFamiliar(player, pouch, true);
    }
    
    public static void spawnFamiliar(Player player, Pouches pouch, boolean deleteItem) {
        if (player.getFamiliar() != null || player.getPet() != null) {
            player.getPackets().sendGameMessage("You already have a follower.");
            return;
        }
        if (!player.getControlerManager().canSummonFamiliar())
            return;
        if (player.getSkills().getLevelForXp(Skills.SUMMONING) < pouch.getLevel()) {
            player.getPackets().sendGameMessage("You need to have a summoning level of " + pouch.getLevel() + " to summon this.");
            return;
        }
        if (player.getSkills().getLevel(Skills.SUMMONING) < pouch.getSpawnCost()) {
            player.getPackets().sendGameMessage("You don't have enough summoning points to summon that familiar.");
            return;
        }
        final Familiar npc = createFamiliar(player, pouch);
        if (npc == null) {
            player.getPackets().sendGameMessage("This familiar is not added yet.");
            return;
        }
        if (deleteItem)
            player.getInventory().deleteItem(pouch.getPouchId(), 1);
        player.getSkills().drainSummoning(pouch.getSpawnCost());
        player.setFamiliar(npc);
    }

    public static int getRealItemId(int uitemId) {
        for (Entry<Long, Object> e : ClientScriptMap.getMap(1183).getValues().entrySet()) {
            if ((Integer) e.getValue() == uitemId) {
                return ClientScriptMap.getMap(1182).getIntValue(e.getKey());
            }
        }
        return uitemId;
    }

    public static boolean hasPouch(Player player) {
        for (Pouches pouch : Pouches.values())
            if (player.getInventory().containsOneItem(pouch.pouchId))
                return true;
        return false;
    }

    public static void openDreadnipInterface(Player player) {
        if (player.getControlerManager().getControler() instanceof Dungeoneering) {
            player.sendMessage("You can't use Dreadnips here.");
            return;
        }
        if (player.getControlerManager().getControler() instanceof Dungeoneering) {
            return;
        } else if (player.getTemporaryAttributtes().get("hasDN") != null) {
            return;
        }
        if (!player.getInventory().containsItem(22370, 1))
            return;
        Entity target = player.getCombatDefinitions().getCurrentTarget();
        if (target == null || target.isDead() || target.hasFinished()) {
            player.getPackets().sendMainInterfaceMessage(1, "You don't have a target.", true);
            return;
        }
        if (!(target instanceof NPC)) {
            player.getPackets().sendMainInterfaceMessage(1, "You can only use this on npc targets.", true);
            return;
        }
        attackDreadnipTarget((NPC) target, player);
    }

    public static void attackDreadnipTarget(NPC target, Player player) {
        if (target.isDead()) {
            player.getPackets().sendGameMessage("Your target is already dead!");
            return;
        } else if (player.getControlerManager().getControler() instanceof Dungeoneering) {
            return;
        } else if (player.getTemporaryAttributtes().get("hasDN") != null) {
            return;
        } else if (player.getFamiliar() != null && target == player.getFamiliar()) {
            return;
        } else if (target instanceof Familiar && !player.isCanPvp()) {
            return;
        } else if (player.getControlerManager() != null && !player.getControlerManager().canHit(target))
            return;
        if (!player.getInventory().containsItem(22370, 1))
            return;
        // TODO cannot attack other players' familiars
        player.getInventory().deleteItem(22370, 1);
        int id = player.selectedMeleeDreadnip ? 20458 : player.selectedMagicDreadnip ? 20460 : player.selectedRangedDreadnip ? 20459 : 20458;
        DreadNip npc = new DreadNip(player, id, player, -1, false);
        if (target == npc)
            return;
        player.getTemporaryAttributtes().put("hasDN", npc);
        npc.setTarget(target);
        npc.setNextAnimation(new Animation(14441));
    }

    /**
     * XP modifier by wearing items.
     *
     * @param player The player.
     * @return the XP modifier.
     */
    public static double shamanSuit(Player player) {
        double xpBoost = 1.0;
        if (player.getEquipment().getHatId() == 28995)
            xpBoost *= 1.01;
        if (player.getEquipment().getHatId() == 32778)
            xpBoost *= 1.05;
        if (player.getEquipment().getChestId() == 28996)
            xpBoost *= 1.01;
        if (player.getEquipment().getLegsId() == 28997)
            xpBoost *= 1.01;
        if (player.getEquipment().getBootsId() == 28999)
            xpBoost *= 1.01;
        if (player.getEquipment().getGlovesId() == 28998)
            xpBoost *= 1.01;
        if (player.getEquipment().getHatId() == 28995 && player.getEquipment().getChestId() == 28996 && player.getEquipment().getLegsId() == 28997 && player.getEquipment().getBootsId() == 28999 && player.getEquipment().getGlovesId() == 28998)
            xpBoost *= 1.01;
        if (player.getEquipment().getHatId() == 32278 && player.getEquipment().getChestId() == 28996 && player.getEquipment().getLegsId() == 28997 && player.getEquipment().getBootsId() == 28999 && player.getEquipment().getGlovesId() == 28998)
            xpBoost *= 1.05;
        return xpBoost;
    }

    public static void sendPouchInterface(Player player, WorldObject object) {
        player.getDialogueManager().startDialogue(new Dialogue() {

            @Override
            public void start() {
                RS3SkillsDialogue.sendSkillDialogueByProduce(player, getBestAvailableProduct(player));
            }

            @Override
            public void run(int interfaceId, int componentId) {
                SkillDialogueResult result = RS3SkillsDialogue.getResult(player, componentId == RS3SkillsDialogue.CONTINUE_OPTION);
                if (componentId == RS3SkillsDialogue.CONTINUE_OPTION) {
                    end();
                    int productId = result.getProduce();
                    int quantity = result.getQuantity();
                    InfuseData data = getInfuseData(productId);
                    if (data == null)
                        return;
                    boolean infusedAtleastOne = false;
                    double[] totalXps = new double[player.getSkills().getLevels().length];
                    int count = 0;
                    for (count = 0; count < quantity; count++) {
                        if (!checkAll(player, data, true))
                            break;
                        infusedAtleastOne = true;
                        for (int i = 0; i < data.requiredItems.length; i++) {
                            int itemId = data.getRequiredItems()[i][0];
                            int amount = data.getRequiredItems()[i][1];
                            player.getInventory().removeItemMoneyPouch(new Item(itemId, amount));
                        }
                        if (data.getReplaceableItems() != null)
                            for (Item item : data.getReplaceableItems())
                                if (player.getInventory().containsItem(item.getId(), item.getAmount())) {
                                    player.getInventory().deleteItem(item.getId(), item.getAmount());
                                    break;
                                }
                        for (int i = 0; i < data.getProductXp().length; i++) {
                            int skillId = data.getProductXp()[i][0];
                            double xp = ((double) data.getProductXp()[i][1]) / 10.0;
                            if (xp > 0)
                                totalXps[skillId] += xp;
                        }
                        Item item = new Item(data.productId, data.getQuantity());
                        player.getInventory().addItem(item);
                    }
                    player.setPouchesMade(player.getPouchesMade() + count);
                    if (count == 1)
                        player.getPackets().sendGameMessage("You infuse a " + ItemDefinitions.getItemDefinitions(data.productId).getName().toLowerCase() + "; pouches made: " + Colors.RED + Utils.getFormattedNumber(player.getPouchesMade()) + "</col>.", true);
                    else if (count > 0)
                        player.getPackets().sendGameMessage("You infuse some " + ItemDefinitions.getItemDefinitions(data.productId).getName().toLowerCase() + "es; pouches made: " + Colors.RED + Utils.getFormattedNumber(player.getPouchesMade()) + "</col>.", true);
                    if (infusedAtleastOne) {
                        player.faceObject(object);
                        player.setNextAnimation(new Animation(725));
                        player.setNextGraphics(new Graphics(1207));
                        for (int i = 0; i < totalXps.length; i++)
                            if (totalXps[i] > 0)
                                player.getSkills().addXp(i, totalXps[i]);
                    }
                }
            }

            @Override
            public void finish() {
            }
        });
    }

    public static int getBestAvailableProduct(Player player) {
        int selectedProductId = -1;
        int highestLevel = -1;
        for (Object o : ClientScriptMap.getMap(6932).getValues().values()) {
            for (Object o2 : ClientScriptMap.getMap((int) o).getValues().values()) {
                int productId = (int) o2;
                InfuseData data = getInfuseData(productId);
                if (data == null) {
                    System.out.println("error geting infuse data for productId=" + productId);
                    continue;
                }
                if (!checkAll(player, data, false))
                    continue;
                if (data.summoningLevelForPouch > highestLevel) {
                    selectedProductId = productId;
                    highestLevel = data.summoningLevelForPouch;
                    continue;
                }
            }
        }
        if (selectedProductId == -1) {
            return ClientScriptMap.getMap(ClientScriptMap.getMap(6932).getIntValue(0)).getIntValue(0);
        }
        return selectedProductId;
    }

    public static boolean checkAll(Player player, InfuseData data, boolean sendMessage) {
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(data.productId);
        String name = defs.getName();
        for (int i = 0; i < data.getRequirements().length; i++) {
            int type = data.getRequirements()[i][0];
            int amount = data.getRequirements()[i][1];
            if (type < 61) {
                int skillId = ClientScriptMap.getMap(681).getIntValue(type);
                int level = amount;
                if (player.getSkills().getLevelForXp(skillId) < level) {
                    if (sendMessage)
                        player.getPackets().sendGameMessage("You need a " + Skills.SKILL_NAME[skillId] + " level of " + level + " to " + ("infuse") + " " + name + ".");
                    return false;
                }
            }
        }
        for (int i = 0; i < data.getRequiredItems().length; i++) {
            int itemId = data.getRequiredItems()[i][0];
            int amount = data.getRequiredItems()[i][1];
            int materialIndex = data.getRequiredItems()[i][2];
            if (materialIndex == -1) {
                if (!player.getInventory().containsItemInInventory(itemId, amount)) {
                    if (sendMessage)
                        player.getPackets().sendGameMessage("You don't have enough " + ItemDefinitions.getItemDefinitions(itemId).getName() + " to " + ("infuse") + " " + name + ".");
                    return false;
                }
            } else {
                if (player.getInventionManager().getMaterials()[materialIndex] < amount) {
                    if (sendMessage)
                        player.getPackets().sendGameMessage("You don't have enough " + InventionDefinitions.getMaterialName(materialIndex) + " to " + ("infuse") + " " + name + ".");
                    return false;
                }
            }
        }
        Item[] replaceableItems = data.getReplaceableItems();
        if (replaceableItems != null) {
            boolean hasOne = false;
            for (Item item : replaceableItems)
                if (player.getInventory().containsItem(item.getId(), item.getAmount()))
                    hasOne = true;
            if (!hasOne) {
                if (sendMessage)
                    player.getPackets().sendGameMessage("You need to have any of the following items to " + ("infuse") + " " + name + ":", true);
                int count = 1;
                for (Item item : replaceableItems) {
                    if (sendMessage)
                        player.getPackets().sendGameMessage(count + ") " + item.getAmount() + " x " + item.getName() + ".", true);
                    count++;
                }
                return false;
            }
        }
        return true;
    }

    private static final ConcurrentHashMap<Integer, InfuseData> infuseData = new ConcurrentHashMap<Integer, InfuseData>();
    public static final int SKILLS_START_OPCODE = 2696, XPS_START_OPCODE = 2697, REUIRED_TYPE_START_OPCODE = 2640, REUIRED_AMOUT_START_OPCODE = 2645, ITEMS_START_OPCODE = 2655, AMOUNTS_START_OPCODE = 2665, MATERIALS_START_OPCODE = 5456, REPLACEABLE_ITEMS_MAP_OPCODE = 2675, REPLACEABLE_ITEMS_START_OPCODE = 5451, REPLACEABLE_AMOUNTS_START_OPCODE = 5471, BLUE_PRINT_OPCODE = 2646, CREATION_QUANTITY = 2653;

    public static InfuseData getInfuseData(int productId) {
        if (infuseData.containsKey(productId))
            return infuseData.get(productId);
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(productId);
        if (defs == null || defs.getName().equals("null"))
            return null;
        int summoningLevel = 0;
        List<int[]> requirements = new ArrayList<int[]>();
        for (int i = 0; i < 5; i++) {
            if (defs.getCSOpcode(REUIRED_TYPE_START_OPCODE + i) == 0)
                break;
            int type = defs.getCSOpcode(REUIRED_TYPE_START_OPCODE + i);
            int amount = defs.getCSOpcode(REUIRED_AMOUT_START_OPCODE + i);
            requirements.add(new int[] { type, amount });
            if (type < 61) {
                int skillId = ClientScriptMap.getMap(681).getIntValue(type);
                if (skillId == Skills.SUMMONING)
                    summoningLevel = amount;
            }
        }
        List<int[]> productXp = new ArrayList<int[]>();
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            if (defs.getCSOpcode(SKILLS_START_OPCODE + (i * 2)) == 0)
                break;
            int skillId = defs.getCSOpcode(SKILLS_START_OPCODE + (i * 2));
            skillId = ClientScriptMap.getMap(681).getIntValue(skillId);
            int xp = defs.getCSOpcode(XPS_START_OPCODE + (i * 2));
            productXp.add(new int[] { skillId, xp });
        }

        List<Item> replaceableItems = new ArrayList<Item>();
        for(int opcode = REPLACEABLE_ITEMS_MAP_OPCODE; opcode <= REPLACEABLE_ITEMS_MAP_OPCODE + 9;opcode++) {
            int replaceableItemsMap = defs.getCSOpcode(opcode);
        if (replaceableItemsMap != 0) {
            GeneralRequirementMap map = GeneralRequirementMap.getMap(replaceableItemsMap);
            if (map != null) {
                for (int i = 0; i < Integer.MAX_VALUE; i++) {
                    if (map.getIntValue(ITEMS_START_OPCODE + i) == 0 || ITEMS_START_OPCODE + i >= AMOUNTS_START_OPCODE)
                        break;
                    replaceableItems.add(new Item(map.getIntValue(ITEMS_START_OPCODE + i), map.getIntValue(AMOUNTS_START_OPCODE + i)));
                }
                for (int i = 0; i < Integer.MAX_VALUE; i++) {
                    if (map.getIntValue(REPLACEABLE_ITEMS_START_OPCODE + i) == 0)
                        break;
                    replaceableItems.add(new Item(map.getIntValue(REPLACEABLE_ITEMS_START_OPCODE + i), map.getIntValue(REPLACEABLE_AMOUNTS_START_OPCODE + i)));
                }
            }
        }
        }
        List<int[]> requiredItems = new ArrayList<int[]>();
        for (int i = 0; i <= 9; i++) {
            if (defs.getCSOpcode(ITEMS_START_OPCODE + i) == 0)
                break;
            int itemId = defs.getCSOpcode(ITEMS_START_OPCODE + i);
            int amount = defs.getCSOpcode(AMOUNTS_START_OPCODE + i);
            int materialId = itemId == 36365 ? defs.getCSOpcode(MATERIALS_START_OPCODE + i) : -1;
            if (materialId != -1)
                materialId = InventionDefinitions.getMaterialIndex(materialId);
            requiredItems.add(new int[] { itemId, amount, materialId });
        }
        int quantity = defs.getCSOpcode(CREATION_QUANTITY) == 0 ? 1 : defs.getCSOpcode(CREATION_QUANTITY);
        if (productId >= 41335 && productId <= 41545) {
            productId = 48269 + (productId - 41335);
        }
        for (int i = 0; i < requiredItems.size(); i++) {
            int[] item = requiredItems.get(i);
            Integer[] repairData = ItemDefinitions.getItemDefinitions(item[0]).getRepairData();
            if (repairData != null && repairData.length > 0) {
                item[0] = repairData[0];

            }
        }
        InfuseData data = new InfuseData(productId, quantity, requirements.toArray(new int[requirements.size()][2]), productXp.toArray(new int[productXp.size()][2]), requiredItems.toArray(new int[requiredItems.size()][3]), replaceableItems.isEmpty() ? null : replaceableItems.toArray(new Item[replaceableItems.size()]));
        data.summoningLevelForPouch = summoningLevel;
        infuseData.put(productId, data);
        return data;
    }

    public static class InfuseData {
        private final int[][] requirements;
        private final int[][] productXp;
        private final int[][] requiredItems;
        private final Item[] replaceableItems;
        private final int productId;
        private final int quantity;
        private int summoningLevelForPouch;

        public InfuseData(int productId, int quantity, int[][] requirements, int[][] productXp, int[][] requiredItems, Item[] replaceableItems) {
            this.productId = productId;
            this.quantity = quantity;
            this.requirements = requirements;
            this.productXp = productXp;
            this.requiredItems = requiredItems;
            this.replaceableItems = replaceableItems;
        }

        public int getProductId() {
            return productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public int[][] getRequirements() {
            return requirements;
        }

        public int[][] getProductXp() {
            return productXp;
        }

        public int[][] getRequiredItems() {
            return requiredItems;
        }

        public Item[] getReplaceableItems() {
            return replaceableItems;
        }

    }

}