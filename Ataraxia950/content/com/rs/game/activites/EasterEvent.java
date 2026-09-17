package com.rs.game.activites;

import com.rs.cores.CoresManager;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.content.easter.EasterBunnyNPC;
import com.rs.game.player.content.easter.SickEasterBunnyNPC;
import com.rs.utils.Utils;

import java.util.concurrent.TimeUnit;

/**
 * Handles the extra Easter Event.
 *
 * @author Noel
 */
public class EasterEvent {

	public static int[] EGG = { 7928, 7929, 7930, 7931, 7932, 7933 };

	public static String droppedArea;

	public static int getX, getY, spawnedEgg;
	
	public static boolean eventStarted;

	public static void teleportBunny() {
		final int randomPlaces = Utils.random(21);
		if (randomPlaces == 20) {
			setDroppedArea("Forgotten Cemetry (Wilderness)");
			getX = 2980;
			getY = 3752;
		} else if (randomPlaces == 19) {
			setDroppedArea("Wilderness (Level 44)");
			getX = 3308;
			getY = 3827;
		} else if (randomPlaces == 18) {
			setDroppedArea("Karamja");
			getX = 2824;
			getY = 3179;
		} else if (randomPlaces == 17) {
			setDroppedArea("Catherby");
			getX = 2852;
			getY = 3430;
		} else if (randomPlaces == 16) {
			setDroppedArea("Lumbridge");
			getX = 3220;
			getY = 3227;
		} else if (randomPlaces == 15) {
			setDroppedArea("Draynor");
			getX = 3139;
			getY = 3263;
		} else if (randomPlaces == 14) {
			setDroppedArea("Seers' Village");
			getX = 2721;
			getY = 3485;
		} else if (randomPlaces == 13) {
			setDroppedArea("Edgeville");
			getX = 3051;
			getY = 3516;
		} else if (randomPlaces == 12) {
			setDroppedArea("Oo'glog");
			getX = 2487;
			getY = 2832;
		} else if (randomPlaces == 11) {
			setDroppedArea("Phoenix Lair");
			getX = 2303;
			getY = 3575;
		} else if (randomPlaces == 10) {
			setDroppedArea("Seers Village");
			getX = 2654;
			getY = 3415;
		} else if (randomPlaces == 9) {
			setDroppedArea("Varrock");
			getX = 3161;
			getY = 3390;
		} else if (randomPlaces == 8) {
			setDroppedArea("Falador");
			getX = 3030;
			getY = 3410;
		} else if (randomPlaces == 7) {
			setDroppedArea("Port Sarim");
			getX = 2956;
			getY = 3221;
		} else if (randomPlaces == 6) {
			setDroppedArea("Castle Wars");
			getX = 2490;
			getY = 3098;
		} else if (randomPlaces == 5) {
			setDroppedArea("Barbarian oupost");
			getX = 2535;
			getY = 3432;	
		} else if (randomPlaces == 4) {
			setDroppedArea("Tree Gnome Village");
			getX = 2519;
			getY = 3208;
		} else if (randomPlaces == 3) {
			setDroppedArea("Al Kharid");
			getX = 3313;
			getY = 3275;
		} else if (randomPlaces == 2) {
			setDroppedArea("Burthrope");
			getX = 2949;
			getY = 3438;
		} else if (randomPlaces == 1) {
			setDroppedArea("Bandit Camp");
			getX = 3148;
			getY = 2929;
		} else if (randomPlaces == 0) {
			setDroppedArea("Canifis");
			getX = 3539;
			getY = 3494;
		}
	}
	
	/**
	 * We init the auto-event.
	 */
	public static void initEvent() {
		eventStarted = true;
		teleportBunny();
		World.sendWorldMessage(
				"<img=7><col=E0246F>Easter Event: The easter bunny was last seen around " + getDroppedArea() + ".", false);
		
		new NPC(13651, new WorldTile(2307, 3186, 0), -1, false);
		new SickEasterBunnyNPC(15754, new WorldTile(2311, 3187, 0));
		
		World.spawnObject(new WorldObject(41467, 10, 10, 2311, 3179, 0));
		final NPC easterBunny = World.findNPC(15753);
		if(easterBunny == null) {
			new EasterBunnyNPC(15753, new WorldTile(getX, getY, 0));
		}

		
		CoresManager.getServiceProvider().scheduleRepeatingTask(() -> {
			// if (World.getPlayers().size() <= 3)
			// return;
			teleportBunny();
			//spawnedEgg = EGG[Utils.random(EGG.length)];
			World.sendWorldMessage(
					"<img=7><col=E0246F>Easter Event: The easter bunny was last seen around " + getDroppedArea() + ".", false);
			final NPC easterBunny1 = World.findNPC(15753);
			easterBunny1.setNextWorldTile(new WorldTile(getX, getY, 0));
			//World.addGroundItem(new Item(spawnedEgg, 1), getSpawn());
		}, Utils.random(600, 600), Utils.random(600, 600), TimeUnit.SECONDS);
	}

	public static String getDroppedArea() {
		return droppedArea;
	}

	public static void setDroppedArea(final String droppedLocation) {
		droppedArea = droppedLocation;
	}

	/**
	 * Gets the randomized spawn tile.
	 *
	 * @return The Worldtile.
	 */
	public static WorldTile getSpawn() {
		WorldTile centerTile = new WorldTile(getX, getY, 0);
		for (int trycount = 0; trycount < 10; trycount++) {
			centerTile = new WorldTile(centerTile, 15);
			if (World.canMoveNPC(centerTile.getPlane(), centerTile.getX(), centerTile.getY(), 1)) {
				break;
			}
		}
		return centerTile;
	}
}