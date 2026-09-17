package com.rs.game.activites;

import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.concurrent.TimeUnit;

public class HalloweenEvent {

	/**
	 * The Spawn area name to string.
	 */
	public static String spawnArea;

	/**
	 * The spawn area's location.
	 */
	public static int getX, getY;

	/**
	 * The random places int.
	 */
	public static int randomPlaces;

	/**
	 * Fixed task to spawn.
	 */
	public static void start() {
		CoresManager.getServiceProvider().scheduleRepeatingTask(() -> {
			final NPC jack = World.findNPC(15975);
			if (jack != null) {
				// World.sendWorldMessage("<img=7><shad=000000><col="+
				// World.colors[Utils.random(World.colors.length)]+">Halloween
				// Event: "
				// + "Jack-O-Lantern is at the " + getSpawnedArea() + "!",
				// false); //TODO
				return;
			}
			randomPlaces = Utils.random(12);
			switch (randomPlaces) {
			case 0:
				setSpawnedArea("Draynor Manor");
				getX = 3109;
				getY = 3349;
				break;
			case 1:
				setSpawnedArea("Draynor Village");
				getX = 3091;
				getY = 3259;
				break;
			case 2:
				setSpawnedArea("Graveyard of Shadows");
				getX = 3228;
				getY = 3683;
				break;
			case 3:
				setSpawnedArea("East Wilderness Ruins");
				getX = 3230;
				getY = 3740;
				break;
			case 4:
				setSpawnedArea("Wilderness Bandit Camp");
				getX = 3033;
				getY = 3699;
				break;
			case 5:
				setSpawnedArea("West Wilderness Ruins");
				getX = 2971;
				getY = 3697;
				break;
			case 6:
				setSpawnedArea("Forgotten Cemetery");
				getX = 2974;
				getY = 3753;
				break;
			case 7:
				setSpawnedArea("Demonic Ruins");
				getX = 3288;
				getY = 3884;
				break;
			case 8:
				setSpawnedArea("Lumbridge Cemetery");
				getX = 3245;
				getY = 3198;
				break;
			case 9:
				setSpawnedArea("Camelot Cemetery");
				getX = 2710;
				getY = 3462;
				break;
			case 10:
				setSpawnedArea("Ardougne Cemetery");
				getX = 2612;
				getY = 3301;
				break;
			case 11:
				setSpawnedArea("Lumbridge Swamp");
				getX = 3218;
				getY = 3178;
				break;
			case 12:
				setSpawnedArea("Port Sarim Cemetery");
				getX = 2998;
				getY = 3187;
				break;
			}
			// World.sendWorldMessage("<img=7><shad=000000><col="+
			// World.colors[Utils.random(World.colors.length)]+">Halloween
			// Event: "
			// + "Jack-O-Lantern has spawned at the " + getSpawnedArea() +
			// "!", false); //TODO
			World.spawnNPC(15975, new WorldTile(getX, getY, 0), -1, true);
		}, 26, 27, TimeUnit.MINUTES);
	}

	/**
	 * Gets the spawned area's name.
	 *
	 * @return the spawnArea to return.
	 */
	public static String getSpawnedArea() {
		return spawnArea;
	}

	/**
	 * Sets the spawned area's name.
	 *
	 * @param location
	 *            the Location to set.
	 */
	public static void setSpawnedArea(final String location) {
		spawnArea = location;
	}

	/**
	 * Gets the spawn location.
	 *
	 * @return the Location to get.
	 */
	public static WorldTile getSpawn() {
		return new WorldTile(getX, getY, 0);
	}

	/**
	 * Teleports the Jack-O-Lantern to a new position.
	 *
	 * @param npc
	 *            The jack-O-Lantern.
	 * @param tile
	 *            The WorldTile.
	 */
	public static void teleport(final NPC npc, final WorldTile tile) {
		npc.setNextAnimation(new Animation(8939));
		npc.setNextGraphics(new Graphics(1678));
		npc.setCantInteract(true);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				npc.setNextWorldTile(tile);
				npc.setNextAnimation(new Animation(8941));
				npc.setNextGraphics(new Graphics(1679));
				npc.setCantInteract(false);
			}
		}, 3);
	}
}