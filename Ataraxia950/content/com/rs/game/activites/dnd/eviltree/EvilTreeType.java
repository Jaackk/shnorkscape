package com.rs.game.activites.dnd.eviltree;

import java.util.concurrent.ThreadLocalRandom;

import com.google.common.collect.ImmutableList;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

/**
 * @author lare96 <http://github.com/lare96>
 */
public enum EvilTreeType {
    NORMAL(25, 1511, 250, 0.05, 0.25, 8_000, 14839, 11434, 11435, 11436, 1, 7, 1, 60.0, 50.5, 50.0, 100.0, 15.7),
    OAK(23, 1521, 225, 0.06, 0.26, 10_000, 14840, 11437, 11438, 11439, 15, 19, 15, 75.0, 100.4, 100.4, 125.0, 30.5),
    WILLOW(21, 1519, 200, 0.07, 0.30, 12_000, 14841, 11440, 11441, 11442, 30, 31, 30, 90.0, 150.2, 125.7, 150.1, 45.3),
    MAPLE(19, 1517, 175, 0.08, 0.35, 14_000, 14842, 11443, 11444, -1, 45, 43, 45,105.0, 200.3, 175.8, 175.1, 60.4),
    YEW(17, 1515, 150, 0.09, 0.37, 16_000, 14843, 11916, 11917, 11918, 60, 55, 60, 120.0, 250.5, 200.5, 200.5, 75.3),
    MAGIC(15, 1513, 125, 0.10, 0.39, 18_000, 14844, 11919, 11920, 11921, 75, 67, 75, 135.0, 300.5, 250.0, 225.0, 90.5),
    ELDER(13, 29556, 100, 0.11, 0.41, 20_000, 14845, 11922, 11923, 11924, 90, 79, 90, 150.0, 350.8, 275.5, 250.5, 105.2);

    public static final ImmutableList<EvilTreeType> ALL = ImmutableList.copyOf(values());
    public final int aliveId, hurtId, dyingId,  lootId, logId, minLogAmt, maxLogAmt;
    public final int woodcuttingLevel, nurtureFarmingLevel, rakeFarmingLevel, firemakingLevel;
    public final double nurtureXp, rakeXp, burnXp, treeXp, rootXp, deweedXp;
    public final int hitpoints;
    public final String formattedName;
    public final double minDmg;
    public final double maxDmg;
    public final int attackRate;

    EvilTreeType(int attackRate, int logId, int minLogAmt, double minDmg, double maxDmg, int hitpoints, int lootId, int aliveId, int hurtId, int dyingId,
                 int woodcuttingLevel, int nurtureFarmingLevel, int firemakingLevel, double nurtureXp,
                 double burnXp, double treeXp, double rootXp, double rakeXp) {
        this.attackRate = attackRate;
        this.logId = logId;
        this.minLogAmt = minLogAmt;
        maxLogAmt = (int) (minLogAmt * 1.50);
        this.minDmg = minDmg;
        this.maxDmg = maxDmg;
        this.hitpoints = hitpoints;
        this.lootId = lootId;
        this.aliveId = aliveId;
        this.hurtId = hurtId;
        this.dyingId = dyingId;
        this.woodcuttingLevel = woodcuttingLevel;
        this.nurtureFarmingLevel = nurtureFarmingLevel;
        this.firemakingLevel = firemakingLevel;
        this.nurtureXp = nurtureXp;
        this.burnXp = burnXp;
        this.treeXp = treeXp;
        this.rootXp = rootXp;
        this.rakeXp = rakeXp;
        deweedXp = nurtureXp * 2;
        rakeFarmingLevel = Math.max(1, nurtureFarmingLevel - 20);
        formattedName = Utils.capitalize(name().toLowerCase());
    }

    public static EvilTreeType forName(String name) {
        for (EvilTreeType type : ALL) {
            if (type.name().equalsIgnoreCase(name))
                return type;
        }
        return null;
    }

    public int getRewardsBonus() {
        int multiplier;
        switch (this) {
            case NORMAL:
                multiplier = 95;
                break;
            case OAK:
                multiplier = 110;
                break;
            case WILLOW:
                multiplier = 125;
                break;
            case MAPLE:
                multiplier = 140;
                break;
            case YEW:
                multiplier = 155;
                break;
            case MAGIC:
                multiplier = 170;
                break;
            case ELDER:
                multiplier = 185;
                break;
            default:
                throw new IllegalStateException("Invalid tree type.");

        }
        return multiplier;
    }

    public int getDustAmount() {
        int chance;
        switch (this) {
            case NORMAL:
                chance = 40;
                break;
            case OAK:
                chance = 50;
                break;
            case WILLOW:
                chance = 60;
                break;
            case MAPLE:
                chance = 75;
                break;
            case YEW:
                chance = 80;
                break;
            case MAGIC:
                chance = 90;
                break;
            case ELDER:
                chance = 100;
                break;
            default:
                throw new IllegalStateException("Invalid tree type.");

        }
        int minChance = chance / 2;
        if (minChance < 1) {
            minChance = 1;
        }
        if (minChance > chance) {
            chance = minChance;
        }
        return ThreadLocalRandom.current().nextInt(minChance, chance);
    }

    public int getHarvestChance(Player player) {
        int chance;
        switch (this) {
            case NORMAL:
                chance = 14;
                break;
            case OAK:
                chance = 13;
                break;
            case WILLOW:
                chance = 12;
                break;
            case MAPLE:
                chance = 11;
                break;
            case YEW:
                chance = 10;
                break;
            case MAGIC:
                chance = 9;
                break;
            case ELDER:
                chance = 8;
                break;
            default:
                throw new IllegalStateException("Invalid tree type.");

        }
        return player.getPerkManager().hasPerkActive(DonationPerk.TREE_HUNTER) ? (int) (chance * 0.75) : chance;
    }

    public static EvilTreeType getRandom() {
        return Utils.randomFrom(values());
    }

    	/* Thanks to Kris for these.
	SEERS_VILLAGE(new WorldTile(2757, 3425, 0), new WorldTile(2755, 3420, 0), new String[] { "close to a collection of yew trees.", "close to the village you call 'Seers'."}),
	APE_ATOLL(new WorldTile(2756, 2696, 0), new WorldTile(2761, 2697, 0), new String[] { "close to a dungeon entrance, and within a tropical jungle.", "on the southern coast of a tropical island." }),
	BRIMHAVEN_DUNGEON(new WorldTile(2739, 3158, 0), new WorldTile(2736, 3155, 0), new String[] { "close to a dungeon entrance, and within a tropical jungle." }),
	SEERS_VILLAGE_BANK(new WorldTile(2709, 3506, 0), new WorldTile(2704, 3507, 0), new String[] { "close to a large collection of willow trees.", "close to the village you call 'Seers'.", "north as the crow flies from Seers' Village." }),
	DRAYNOR_VILLAGE(new WorldTile(3096, 3227, 0), new WorldTile(3101, 3226, 0), new String[] { "close to a large collection of willow trees.", "close to the village you humans call 'Draynor'.", "due west of the town you call Lumbridge." }),
	LEGENDS_GUILD(new WorldTile(2727, 3331, 0), null, new String[] { "close to a large collection of willow trees.", "close to a mine on the outskirts of a city.", "close to the home of 'Legends'." }),
	TREE_GNOME_STRONGHOLD(new WorldTile(2401, 3431, 0), new WorldTile(2406, 3431, 0), new String[] { "close to a large collection of willow trees." }),
	FALADOR(new WorldTile(2925, 3372, 0), null, new String[] { "close to a mine on the outskirts of a city." }),
	MONASTERY(new WorldTile(3050, 3460, 0), new WorldTile(3048, 3456, 0), new String[] { "close to a Runecrafting altar." }),
	SHILO_VILLAGE(new WorldTile(2832, 3013, 0), new WorldTile(2828, 3012, 0), new String[] { "Close to a Runecrafting altar.", "on the island known as Karamja." }),
	SORCERESS_TOWER(new WorldTile(2721, 3414, 0), new WorldTile(2716, 3415, 0), new String[] { "close to the home of 'Legends'." }),
	NORTHERN_YANILLE(new WorldTile(2605, 3118, 0), new WorldTile(2602, 3115, 0), new String[] { "close to the town you call Yanille." }),
	YANILLE(new WorldTile(2521, 3105, 0), new WorldTile(2517, 3104, 0), new String[] { "close to the town you call Yanille.", "to the south of a tree gnome settlement." }),
	MCGRUBORS_WOODS(new WorldTile(2667, 3487, 0), new WorldTile(2664, 3486, 0), new String[] { "close to the village you call 'Seers'.", "north as the crow flies from the market of Ardougne." }),
	NORTHERN_DRAYNOR_VILLAGE(new WorldTile(3094, 3305, 0), new WorldTile(3090, 3308, 0), new String[] { "close to the village you humans call 'Draynor'." }),
	CASTLE_WARS(new WorldTile(2377, 3054, 0), new WorldTile(2373, 3054, 0), new String[] { "in a location with rare trees and ogres nearby." }),
	MOBILISING_ARMIES(new WorldTile(2467, 2844, 0), new WorldTile(2463, 2842, 0), new String[] { "in a location with rare trees and ogres nearby." }),
	TYRAS_CAMP(new WorldTile(2213, 3175, 0), new WorldTile(2218, 3173, 0), new String[] { "in the lands inhabited by elves." }),
	LLETYA(new WorldTile(2293, 3120, 0), new WorldTile(2293, 3120, 0), new String[] { "in the lands inhabited by elves." }),
	TOLNAS_RIFT(new WorldTile(3321, 3458, 0), new WorldTile(3319, 3454, 0), new String[] { "just outside of the city you call Varrock." }),
	VARROCK_PALACE(new WorldTile(3221, 3511, 0), new WorldTile(3218, 3513, 0), new String[] { "just outside of the city you call Varrock." }),
	RELLEKKA(new WorldTile(2703, 3636, 0), new WorldTile(2702, 3633, 0), new String[] { "north as the crow flies from Seers' Village." }),
	RANGING_GUILD(new WorldTile(2653, 3415, 0), null, new String[] { "north as the crow flies from the market of Ardougne." }),
	KHAZARI_JUNGLE(new WorldTile(2912, 2893, 0), new WorldTile(2908, 2891, 0), new String[] { "on the island known as Karamja.", "on the southern coast of a tropical island." }),
	OUTPOST(new WorldTile(2451, 3346, 0), new WorldTile(2447, 3346, 0), new String[] { "to the south of a tree gnome settlement." });

	private final WorldTile location;
	private final WorldTile leprechaunLocation;
	private final String[] hints;
	*/
}