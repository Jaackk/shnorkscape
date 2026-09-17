package com.rs.game.activities.dfm;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class DemonFlashMobs {

	private static final String[][] PRE_AND_SUFFIXES = new String[][] {
		{ "General", "Executioner", "Castellan", "Deacon" },
		{ "Blazing", "Corrupting", "Frostborn", "Glorious", "Infernal", "Obscured",
			"Pestilent", "Rending", "Shattering", "Terrifying" }
	};
 
 	private static final String[][] NAMES = new String[][] {
 		{ "Av'aar",  "Bra'k", "C'tcho", "Dr'end",  "Eflu'vya", "Fah'lth", "Gro'dd", 
 			"H'stur", "Ik'uur", "Jah'shu",  "Kla'tu", "Lim'uul", "Mord'uun", "Na'ash",
 			"Org'one", "Per'yl", "Qr'rel", "Ra'hasp", "Sud'er", "T'lon", "Urga'l",
 			"V'lac", "We'al", "X'ppe", "Ykr'ee", "Z'shuu" },
 		{ "Acidius", "Brutio", "Cluacael", "Detiel", "Effluxam", "Fractiel", "Grindus",
 			  "Haereticus", "Impeso", "Janio", "Klepto", "Lucius", "Morbiel", "Nullius",
 			  "Oblator", "Pupio", "Quietus", "Rectus", "Salumnos", "Thantio", "Ulcus",
 			   "Visony", "Wendio", "Xarkael", "Yahiel",  "Zonos" }
 	};
 	
 	private static final Object[][] LOCATIONS = new Object[][] {
 		{ "north of West Ardougne", new WorldTile(2541, 3355, 0) },
 		{ "north of Poison Waste", new WorldTile(2254, 3130, 0) },
 		{ "south of Gu'Tanoth", new WorldTile(2589, 2977, 0) },
 		{ "south of Seers Village", new WorldTile(2706, 3441, 0) },
 		{ "south-east of the Edgeville Monastery", new WorldTile(3067, 3469, 0) },
 		{ "in Lumbridge swamp", new WorldTile(3163, 3167, 0) },
 		{ "north-west of Menaphos", new WorldTile(3199, 2851, 0) },
 		{ "west of the Ruins of Uzer", new WorldTile(3435, 3105, 0) },
 		{ "south-west of Port Phasmatys", new WorldTile(3652, 3430, 0) },
 		{ "west of Digsite", new WorldTile(3312, 3401, 0) },
 		{ "north-west of Duel Arena", new WorldTile(3326, 3289, 0) },
 		{ "east of the Chaos Temple", new WorldTile(3284, 3599, 0) },
 		{ "west of the Boneyard", new WorldTile(3259, 3704, 0) },
 		{ "west of Daemonheim", new WorldTile(3356, 3663, 0) },
 		{ "in the Ruins", new WorldTile(3224, 3731, 0) },
 		{ "south of the Green dragons", new WorldTile(2987, 3593, 0) }
 	};
 	
 	/**
 	 * String name, boolean 'after name'
 	 */
 	public static final Object[][] TITLES = new Object[][] {
 		{ "<col=00ff00>General ", false, 121, 1103 },
 		{ "<col=990000>Executioner ", false, 117, 1102 },
 		{ "<col=996633>Castellan ", false, 108, 1100 },
 		{ "<col=ffcc00>Deacon ", false, 115, 1101 },
 		{ "<col=ff9900> the Blazing", true, 106, 1601 },
 		{ "<col=33cc33> the Corrupting", true, 110, 1602 },
 		{ "<col=00ffff> the Frostborn", true, 119, 1603 },
 		{ "<col=ffcc66> the Glorious", true, 123, 1604 },
 		{ "<col=b32d00> the Infernal", true, 125, 1605 },
 		{ "<col=a6a6a6> the Obscured", true, 131, 1606 },
 		{ "<col=00ff00> the Pestilent", true, 127, 1607 },
 		{ "<col=ee6600> the Rending", true, 129, 1608 },
 		{ "<col=00cccc> the Shattering", true, 2, 1609 },
 		{ "<col=999999> the Terrifying", true, 173, 1610 }
 	};
 	
 	public static String getTitle(int title) {
 		for (int i = 0; i < 14; i++) {
 			if ((int) TITLES[i][3] == title) 
 				return TITLES[i][0] + (title < 1600 ? "</col>" : "");
 		}
		return null;
 	}
 	
 	public static final int[] DEMON_SLAYER_PIECES = new int[] {
 			27157, 27159, 27161, 27163, 27165, 27168
 	};
 	
 	private static DemonFlashMobs dfm;
 	private HashMap<Player, Integer> rewardPlayers;
 	private String name, prefix, suffix, demonName, type;
 	private boolean wilderness;
 	private DemonFlashBoss boss;
 	private NPC[] blackDemons, lesserDemons, impHeralds;
 	private WorldTile location;
 	private String hint;
 	private int modelOffset;
 	
 	public String getPrefix() {
 		return prefix;
 	}
 	
 	public String getSuffix() {
 		return suffix;
 	}
 	
 	public HashMap<Player, Integer> getRewardPlayers() {
 		return rewardPlayers;
 	}
 	
 	public void addDamage(Player player, int damage) {
 		rewardPlayers.put(player, rewardPlayers.get(player) == null ? damage : rewardPlayers.get(player) + damage);
 	}
 	
 	public static DemonFlashMobs getDemonFlashMobs() {
 		return dfm;
 	}
 	
 	public DemonFlashBoss getBoss() {
 		return boss;
 	}
 	
 	public boolean isInWilderness() {
 		return wilderness;
 	}
 	
 	public NPC[] getImpHeralds() {
 		return impHeralds;
 	}
 	
 	public NPC[] getLesserDemons() {
 		return lesserDemons;
 	}
 	
 	public NPC[] getBlackDemons() {
 		return blackDemons;
 	}
 	
 	private String buildName() {
 		this.modelOffset = Utils.random(4);
 		this.prefix = PRE_AND_SUFFIXES[0][modelOffset];
 		this.suffix = PRE_AND_SUFFIXES[1][Utils.random(10)];
 		int typeId = Utils.random(2);
 		this.type = typeId == 0 ? "Zamorakian" : "Zarosian";
 		this.demonName = NAMES[typeId][Utils.random(NAMES[typeId].length)];
 		this.name = prefix + " " + demonName + " the " + suffix;
 		return name;
 	}
 	
 	private WorldTile setLocation() {
 		int random = Utils.random(LOCATIONS.length);
 		Object[] loc = LOCATIONS[random];
 		if (random > 10)
 			wilderness = true;
 		this.location = (WorldTile) loc[1];
 		this.hint = (String) loc[0];
 		return location;
 	}
 	
 	public boolean isMultiArea(WorldTile coords) {
 		if (getLocation() == null)
 			return false;
 		int minX = getLocation().getX() - 25;
 		int maxX = getLocation().getX() + 25;
 		int minY = getLocation().getY() - 25;
 		int maxY = getLocation().getY() + 25;
 		return getLocation() != null && coords.getX() >= minX && coords.getX() <= maxX && coords.getY() >= minY && coords.getY() <= maxY;
 	}
 	
 	public WorldTile getLocation() {
 		return location;
 	}
 	
 	public String getHint() {
 		return hint;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public static void init() {
 		CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
 			private int minutes;
			@Override
			public boolean repeat() {
				if (minutes == 0) {
					dfm = new DemonFlashMobs();
					dfm.buildName();
					dfm.rewardPlayers = new HashMap<Player, Integer>();
					dfm.boss = new DemonFlashBoss(16731, dfm.setLocation(), -1, true, true);
					dfm.boss.setChangedModels(83210 + dfm.modelOffset, 83215 + dfm.modelOffset, 83220 + dfm.modelOffset, 83225 + dfm.modelOffset);
					dfm.boss.setName(dfm.getName());
					dfm.boss.setRandomWalk(7);
					if (dfm.prefix.equals("General"))
						dfm.getBoss().setAttackBonuses(500);
					else if (dfm.prefix.equals("Castellan"))
						dfm.getBoss().setDefenceBonuses(500);
					dfm.boss.setHitpoints(20000);
					dfm.blackDemons = new NPC[] { new DemonFlashNPC(16729, new WorldTile(dfm.getLocation(), 4), -1, true, true), new DemonFlashNPC(16729, new WorldTile(dfm.getLocation(), 2), -1, true, true) };
					dfm.lesserDemons = new NPC[] { new DemonFlashNPC(16727, new WorldTile(dfm.getLocation(), 4), -1, true, true), new DemonFlashNPC(16727, new WorldTile(dfm.getLocation(), 3), -1, true, true), new DemonFlashNPC(16727, new WorldTile(dfm.getLocation(), 3), -1, true, true), new DemonFlashNPC(16727, new WorldTile(dfm.getLocation(), 3), -1, true, true) };
					dfm.impHeralds = new NPC[] { new DemonFlashNPC(16726, new WorldTile(dfm.getLocation(), 4), -1, true, true), new DemonFlashNPC(16726, new WorldTile(dfm.getLocation(), 4), -1, true, true), new DemonFlashNPC(16726, new WorldTile(dfm.getLocation(), 4), -1, true, true), new DemonFlashNPC(16726, new WorldTile(dfm.getLocation(), 4), -1, true, true), new DemonFlashNPC(16726, new WorldTile(dfm.getLocation(), 4), -1, true, true), new DemonFlashNPC(16726, new WorldTile(dfm.getLocation(), 4), -1, true, true), new DemonFlashNPC(16726, new WorldTile(dfm.getLocation(), 4), -1, true, true), new DemonFlashNPC(16726, new WorldTile(dfm.getLocation(), 4), -1, true, true) };
					//Doesn't allow one of the imps to glitch up at a nonaccessible location; Cheaphax cuz I cbf changing the code around for this little thing.
					for (NPC n : dfm.impHeralds) {
						if (n.getX() == 2257 && n.getY() == 3124)
							n.setNextWorldTile(new WorldTile(2256, 3125, 0));
					}
					for (int i = 0; i < 4; i++) {
						if (i < 2)
							dfm.blackDemons[i].setRandomWalk(7);
						dfm.lesserDemons[i].setRandomWalk(7);
					}
					//World.sendWorldMessage("<img=6><col=ffff00>News: " + dfm.type + " demonic energy has built up "+ dfm.hint + "!", false);
				} else if (minutes == 60) {
					minutes = -1;
					dfm.boss.finish();
					dfm.boss.reset();
					dfm.boss.resetReceivedDamage();
					for (int i = 0; i < 8; i++) {
						if (i < 2) {
							dfm.blackDemons[i].finish();
							dfm.blackDemons[i].reset();
						}
						if (i < 4) {
							dfm.lesserDemons[i].finish();
							dfm.lesserDemons[i].reset();
						}
						dfm.impHeralds[i].finish();
						dfm.impHeralds[i].reset();
					}
				}
				minutes++;
				return true;
			}
 			
 		}, 2, 1, TimeUnit.MINUTES);
 	}
 
}
