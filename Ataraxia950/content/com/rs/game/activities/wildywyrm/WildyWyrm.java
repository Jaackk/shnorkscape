package com.rs.game.activities.wildywyrm;

import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.others.WildyWyrmNPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.util.concurrent.TimeUnit;

public class WildyWyrm {

	private static final WildyWyrm WYRM = new WildyWyrm();
	private static final WorldTile[] LOCATIONS = new WorldTile[] { new WorldTile(3366, 3927, 0), new WorldTile(3284, 3847, 0), new WorldTile(2959, 3821, 0) };
	private WildyWyrmNPC wyrmNPC;
	private int minutes, aliveTime, nextHit, location;
	
	public static final void InitiateWyrmSpawningSequence() {
		CoresManager.getServiceProvider().scheduleRepeatingTask(() -> {
			try {
			WYRM.minutes--;
			if (WYRM.wyrmNPC != null && !WYRM.wyrmNPC.isDead() && !WYRM.wyrmNPC.hasFinished()) {
				WYRM.aliveTime++;
				if (WYRM.aliveTime >= 60) {
					WYRM.wyrmNPC.sendDeath(null);
				}
			}
			if (WYRM.minutes <= 0) {
				WYRM.wyrmNPC = new WildyWyrmNPC(20629, getLocation(), -1, true, true);
				WYRM.minutes = Utils.random(60, 120);
				WYRM.aliveTime = 0;
				WYRM.nextHit = Utils.random(4);
				World.sendWorldMessage("<img=6><col=ffff00>News: The WildyWyrm has just spawned in the Wilderness!", false);
			}
		} catch (final Throwable e) {
			Logger.getGlobal().catching(e);
		}
		}, 0, 1, TimeUnit.MINUTES);
	}
	
	public String getCurrentHealth() {
		if (WYRM.wyrmNPC == null || WYRM.wyrmNPC.isDead() || WYRM.wyrmNPC.hasFinished()) {
			return WYRM.wyrmNPC == null ? "Not spawned" : "0";
		}
		return WYRM.wyrmNPC.getHitpoints() + "";
	}
	
	public String getNextAttack() {
		if (WYRM.wyrmNPC == null || WYRM.wyrmNPC.isDead() || WYRM.wyrmNPC.hasFinished()) {
			return "None";
		}
		return nextHit == 0 ? "Dragging" : nextHit == 1 ? "Melee" : nextHit == 2 ? "Ranged" : "Magic";
	}
	
	public String getTimeUntilNextSpawn() {
		if (WYRM.wyrmNPC == null) {
			return "Shortly";
		}
		return "~" +WYRM.minutes + " minutes";
	}
	
	public String getPlayersFighting() {
		if (WYRM.wyrmNPC == null || WYRM.wyrmNPC.isDead() || WYRM.wyrmNPC.hasFinished()) {
			return "None";
		}
		final int size = WYRM.wyrmNPC.getPossibleTargets().size();
		return size + " player" + (size == 1 ? "" : "s");
	}
	
	public int getBonus(final int type) {
		if (WYRM.wyrmNPC == null || WYRM.wyrmNPC.isDead() || WYRM.wyrmNPC.hasFinished()) {
			return 0;
		}
		switch(type) {
		case 0:
			return WYRM.wyrmNPC.getBonus(8);
		case 1:
			return WYRM.wyrmNPC.getBonus(0);
		case 2:
			return WYRM.wyrmNPC.getBonus(9);
		case 3:
			return WYRM.wyrmNPC.getBonus(0);
		case 4:
			return WYRM.wyrmNPC.getBonus(10);
			default:
				return WYRM.wyrmNPC.getBonus(0);
		}
	}
	
	public void handleBonuses(final Player player, final int componentId, final int value) {
		if (componentId < 75) {
			if (WYRM.wyrmNPC == null || WYRM.wyrmNPC.isDead() || WYRM.wyrmNPC.hasFinished()) {
				player.sendMessage("You cannot modify WildyWyrm's bonuses at this time.");
				return;
			}
			if (value < 0) {
				player.sendMessage("You cannot set bonuses to negative values.");
				return;
			}
			if (value > 5000) {
				player.sendMessage("You cannot set bonuses above 5000.");
				return;
			}
		} else {
			if (WYRM.wyrmNPC == null || WYRM.wyrmNPC.isDead() || WYRM.wyrmNPC.hasFinished()) {
				player.sendMessage("You cannot modify WildyWyrm's health at this time.");
				return;
			}
			if (value < 0) {
				player.sendMessage("You cannot heal WildyWyrm for negative values.");
				return;
			}
			if (WYRM.wyrmNPC.getHitpoints() >= 85000) {
				player.sendMessage("WildyWyrm is already at full health.");
				return;
			}
		}
		switch(componentId) {
		case 69:
			WYRM.wyrmNPC.setBonus(8, value);
			break;
		case 70:
			WYRM.wyrmNPC.setBonus(0, value);
			break;
		case 71:
			WYRM.wyrmNPC.setBonus(9, value);
			break;
		case 72:
			WYRM.wyrmNPC.setBonus(0, value);
			break;
		case 73:
			WYRM.wyrmNPC.setBonus(10, value);
			break;
		case 74:
			WYRM.wyrmNPC.setBonus(0, value);
			break;
			default:
				final int amount = value + WYRM.wyrmNPC.getHitpoints() > 85000 ? (85000 - WYRM.wyrmNPC.getHitpoints()) : value;
				WYRM.wyrmNPC.applyHit(new Hit(null, amount, HitLook.HEALED_DAMAGE));
				break;
		}
	}
	
	public void setAttackStyle(final Player player, final int style) {
		nextHit = style;
		player.getPackets().sendIComponentText(473, 11, WildyWyrm.getWildywyrm().getNextAttack());
		player.sendMessage("WildyWyrm's next attack set to: " + WildyWyrm.getWildywyrm().getNextAttack() + ".", true);
	}
	
	public void teleportToWildyWyrm(final Player player) {
		if (WYRM.wyrmNPC == null || WYRM.wyrmNPC.isDead() || WYRM.wyrmNPC.hasFinished()) {
			player.setNextWorldTile(LOCATIONS[WYRM.location]);
		} else {
			player.setNextWorldTile(new WorldTile(WYRM.wyrmNPC));
		}
		player.sendMessage("You've been teleported to WildyWyrm. It is currently " + getState().toLowerCase() + "</col>.");
		player.getControlerManager().startControler("Wilderness");
	}
	
	public void spawnWildyWyrm(final Player player) {
		if (WYRM.wyrmNPC != null && !WYRM.wyrmNPC.isDead() && !WYRM.wyrmNPC.hasFinished()) {
			player.sendMessage("You cannot spawn a new WildyWyrm until current one has died.");
			return;
		}
		WYRM.wyrmNPC = new WildyWyrmNPC(20629, getLocation(), -1, true, true);
		WYRM.minutes = Utils.random(60, 120);
		WYRM.aliveTime = 0;
		WYRM.nextHit = Utils.random(4);
		World.sendWorldMessage("<img=6><col=ffff00>News: The WildyWyrm has just spawned in the Wilderness!", false);
	}
	
	public String getWildyWyrmLocation() {
		if (WYRM.wyrmNPC == null || WYRM.wyrmNPC.isDead() || WYRM.wyrmNPC.hasFinished()) {
			return "None";
		}
		return WYRM.location == 0 ? "Wilderness Volcano" : WYRM.location == 1 ? "Demonic Ruins" : "Chaos Temple";
	}
	
	public String getState() {
		if (WYRM.wyrmNPC == null || WYRM.wyrmNPC.isDead() || WYRM.wyrmNPC.hasFinished()) {
			return "<col=ff0000>Dead";
		}
		return "<col=00ff00>Alive";
	}
	
	public void setAttack(final Player player, final int type, final int level) {
		if (WYRM.wyrmNPC == null || WYRM.wyrmNPC.isDead() || WYRM.wyrmNPC.hasFinished()) {
			player.sendMessage("WildyWyrm is currently dead.");
			return;
		}
		final int index = type == 0 ? 8 : type == 1 ? 9 : 10;
		final int currentLevel = WYRM.wyrmNPC.getBonus(index);
		WYRM.wyrmNPC.setBonus(index, level);
		player.sendMessage("WildyWyrm's " + (type == 0 ? "melee" : type == 1 ? "ranged" : "magic") + " attack bonus has been set from " + currentLevel + " to " + level + ".");
	}
	
	public void setDefence(final Player player, final int type, final int level) {
		if (WYRM.wyrmNPC == null || WYRM.wyrmNPC.isDead() || WYRM.wyrmNPC.hasFinished()) {
			player.sendMessage("WildyWyrm is currently dead.");
			return;
		}
		final int index = 0;
		final int currentLevel = WYRM.wyrmNPC.getBonus(index);
		WYRM.wyrmNPC.setBonus(index, level);
		player.sendMessage("WildyWyrm's " + (type == 0 ? "melee" : type == 1 ? "ranged" : "magic") + " defence bonus has been set from " + currentLevel + " to " + level + ".");
	}

	public void refreshNextHit() {
		if (Utils.random(15) == 0) {
			nextHit = 0;
		} else if (WYRM.wyrmNPC != null && WYRM.wyrmNPC.getCombat().getTarget() != null && WYRM.wyrmNPC.getCombat().getTarget().withinDistance(new WorldTile(WYRM.wyrmNPC.getCoordFaceX(WYRM.wyrmNPC.getSize()), WYRM.wyrmNPC.getCoordFaceY(WYRM.wyrmNPC.getSize()), WYRM.wyrmNPC.getPlane()), WYRM.wyrmNPC.getSize() - 2) && Utils.random(10) > 4) {
			nextHit = 1;
		} else if (Utils.random(2) == 0) {
			nextHit = 2;
		} else {
			nextHit = 3;
		}
	}
	
	public int getNextHit() {
		return nextHit;
	}
	
	public void healWyrm(final Player player, int amount) {
		if (WYRM.wyrmNPC == null || WYRM.wyrmNPC.isDead() || WYRM.wyrmNPC.hasFinished()) {
			player.sendMessage("WildyWyrm is currently dead and cannot be healed.");
			return;
		}
		final int maxHealth = WYRM.wyrmNPC.getMaxHitpoints();
		final int currentHealth = WYRM.wyrmNPC.getHitpoints();
		if (currentHealth + amount > maxHealth) {
			amount = maxHealth - currentHealth;
		}
		if (amount == 0) {
			player.sendMessage("WildyWyrm is already at maximum health.");
			return;
		}
		WYRM.wyrmNPC.heal(amount);
		player.sendMessage("You've healed WildyWyrm for " + amount + " hitpoints.");
	}
	
	public void provokeWyrm(final Player provoker) {
		final WildyWyrmNPC npc = WYRM.wyrmNPC;
		if (npc.getId() == 3334) {
			return;
		}
		npc.setCantInteract(true);
		provoker.lock();
		WorldTasksManager.schedule(new WorldTask() {
			int loop = 0;

			@Override
			public void run() {
				if (loop == 0) {
					provoker.setNextAnimation(new Animation(4278));
				} else if (loop == 1) {
					npc.setNextAnimation(new Animation(12795));
					npc.transformIntoNPC(3334);
					npc.setNextGraphics(new Graphics(2317));
				} else if (loop == 2) {
					npc.setTarget(provoker);
					npc.setAttackedBy(provoker);
					npc.setCantInteract(false);
					provoker.unlock();
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}
	
	public static final WorldTile getLocation() {
		WYRM.location = Utils.random(3);
		return LOCATIONS[WYRM.location];
	}
	
	public static final WildyWyrm getWildywyrm() {
		return WYRM;
	}
}
