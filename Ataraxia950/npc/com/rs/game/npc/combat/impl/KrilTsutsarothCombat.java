package com.rs.game.npc.combat.impl;

import com.rs.cache.loaders.NPCDefinitions;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.godwars.zammorak.KrilTsutsaroth;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class KrilTsutsarothCombat extends CombatScript {

	private int hitcount;
	private boolean spec;
	
	/**
	 * Animations: 
	 * 19851: Surge
	 * 19852: Slam
	 * 19853: Slice
	 * Render:
	 * 2827: Surge
	 * 2828: Slicing
	 */

	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		boolean hardMode = ((KrilTsutsaroth) npc).isHardMode();
		if (Utils.getRandom(4) == 0) {
			switch (Utils.getRandom(8)) {
			case 0:
				npc.setNextForceTalk(new ForceTalk("Attack them, you dogs!"));
				break;
			case 1:
				npc.setNextForceTalk(new ForceTalk("Forward!"));
				break;
			case 2:
				npc.setNextForceTalk(new ForceTalk("Death to Saradomin's dogs!"));
				break;
			case 3:
				npc.setNextForceTalk(new ForceTalk("Kill them, you cowards!"));
				break;
			case 4:
				npc.setNextForceTalk(new ForceTalk("The Dark One will have their souls!"));
				npc.playSound(3229, 2);
				break;
			case 5:
				npc.setNextForceTalk(new ForceTalk("Zamorak, curse them!"));
				break;
			case 6:
				npc.setNextForceTalk(new ForceTalk("Rend them limb from limb!"));
				break;
			case 7:
				npc.setNextForceTalk(new ForceTalk("No retreat!"));
				break;
			case 8:
				npc.setNextForceTalk(new ForceTalk("Flay them all!"));
				break;
			}
		}
		if (hardMode && Utils.getRandom(4) == 0) {
			int random = Utils.getRandom(3);
			if (random == 0) {
				npc.setNextForceTalk(new ForceTalk("Run, coward!"));
				npc.getPossibleTargets().forEach(t -> {
					if (t instanceof Player)
						((Player) t).sendMessage("<col=ff0000>K'ril prepares to charge.");
				});
				npc.setRun(true);
				WorldTile targetTile = new WorldTile(target);
				WorldTile npcTile = new WorldTile(npc);
				int x = npcTile.getX() - targetTile.getX();
				int y = npcTile.getY() - targetTile.getY();
				WorldTile toTile = new WorldTile(npc.getX() - (x * 10), npc.getY() - (y * 10), npc.getPlane());
				if (toTile.getX() > (npc.getRespawnTile().getX() + 8))
					toTile.setLocation(npc.getRespawnTile().getX() + 8, toTile.getY(), toTile.getPlane());
				else if (toTile.getX() < (npc.getRespawnTile().getX() - 8))
					toTile.setLocation(npc.getRespawnTile().getX() - 8, toTile.getY(), toTile.getPlane());
				if (toTile.getY() > (npc.getRespawnTile().getY() + 6)) 
					toTile.setLocation(toTile.getX(), npc.getRespawnTile().getY() + 6, toTile.getPlane());
				else if (toTile.getY() < (npc.getRespawnTile().getY() - 5)) 
					toTile.setLocation(toTile.getX(), npc.getRespawnTile().getY() - 5, toTile.getPlane());
				WorldTasksManager.schedule(new WorldTask() {
					int ticks;
					boolean end;
					@Override
					public void run() {
						if (ticks == 0) {
							npc.resetWalkSteps();
							npc.setTarget(null);
							npc.setCantInteract(true);
							npc.setNextRenderAnimation(2827);
							npc.setROTSForceWalk(toTile);
						} else {
							if (end || ticks >= 20 || npc.isDead() || !npc.hasWalkSteps()) {
								npc.applyHit(new Hit(null, Utils.random(50, 200), HitLook.REGULAR_DAMAGE));
								npc.setRun(false);
								npc.setTarget(target);
								npc.setCantInteract(false);
								stop();
								return;
							}
							if (!npc.hasROTSForceWalk()) {
								npc.setNextRenderAnimation(NPCDefinitions.getNPCDefinitions(6203).renderEmote);
								npc.setNextAnimationForce(new Animation(19852));
								end = true;
							} else {//TODO temp disable this part for the death tile and go from there in the future
								npc.getPossibleTargets().forEach(target -> {
									if (target.withinDistance(toTile, 3) && target.getRegionHash() == npc.getRegionHash())
										target.applyHit(new Hit(npc, Utils.random(100, 400), HitLook.REGULAR_DAMAGE));
								});
							}
						}
						ticks++;
					}
				}, 3, 1);
				return 10;
			} else if (random == 1) {
				npc.setNextForceTalk(new ForceTalk("Die in the name of Zamorak!"));
				npc.setNextRenderAnimation(2828);
				npc.getPossibleTargets().forEach(t -> {
					if (t instanceof Player)
						((Player) t).sendMessage("<col=ff0000>K'ril becomes enraged, swiping at you with wild but inaccurate strikes. You should try to avoid them.");
				});
				WorldTasksManager.schedule(new WorldTask() {
					int ticks;

					@Override
					public void run() {
						if (ticks == 9) {
							npc.setNextRenderAnimation(npc.getDefinitions().renderEmote);
							stop();
							return;
						}
						npc.setNextAnimationForce(new Animation(19853));
						npc.getPossibleTargets().forEach(t -> {
							if (t.withinDistance(target, 3) && npc.withinDistance(target, 3))
								t.applyHit(new Hit(npc, Utils.random(5) > 3 ? Utils.random(200) : Utils.random(100), HitLook.MELEE_DAMAGE));
						});
						ticks++;
					}
				}, 0, 1);
				return 10;
			} else if (random == 2) {
				WorldObject earthSpike = new WorldObject(83098, 10, 0, new WorldTile(target));
				npc.setNextForceTalk(new ForceTalk("You cannot stand against Zamorakian might!"));
				npc.getPossibleTargets().forEach(t -> {
					if (t instanceof Player)
						((Player) t).sendMessage("<col=ff0000>K'ril Smashes his blade into the ground, which bursts into spikes from the impact.");
				});
				npc.setNextAnimationForce(new Animation(19853));
				WorldTasksManager.schedule(new WorldTask() {
					int ticks;

					@Override
					public void run() {
						if (ticks == 1) 
							World.spawnObject(earthSpike);
						else if (ticks > 1) {
							npc.getPossibleTargets().forEach(t -> {
								if (t.getHash() == earthSpike.getHash())
									t.applyHit(new Hit(null, 99, HitLook.REGULAR_DAMAGE));
								if (npc.withinDistance(earthSpike, 1))
									npc.applyHit(new Hit(null, Utils.random(10, 50), HitLook.REGULAR_DAMAGE));
							});
							if (ticks == 5) {
								World.removeObject(earthSpike, true);
								stop();
								return;
							}
						}
						
						ticks++;
					}
				}, 0, 2);
				return 10;
			}
		}
		int attackStyle = Utils.getRandom(2);
		switch (attackStyle) {
		case 0:// magic flame attack
			npc.setNextAnimation(new Animation(14962));
			npc.setNextGraphics(new Graphics(1210));
			for (Entity t : npc.getPossibleTargets()) {
				delayHit(npc, 1, t, getMagicHit(npc, getRandomMaxHit(npc, 300, NPCCombatDefinitionConstants.MAGE, t)));
				World.sendProjectile(npc, t, 1211, 41, 16, 41, 35, 16, 0);
				if (Utils.getRandom(4) == 0)
					t.getPoison().makePoisoned(168);
			}
			break;
		case 1:// main attack
		case 2:// melee attack
			int damage = 400;// normal
			for (Entity e : npc.getPossibleTargets()) {
				if (!hardMode && e instanceof Player && hitcount >= 2 && ((Player) e).getPrayer().isMeleeProtecting()) {
					Player player = (Player) e;
					hitcount = -1;
					spec = false;
					damage = 470;
					npc.setNextForceTalk(new ForceTalk("YARRRRRRR!"));
					player.getPrayer().drainPrayer((Math.round(damage / 20)));
					player.setPrayerDelay(Utils.getRandom(5) + 5);
					player.getPackets().sendGameMessage(
							"K'ril Tsutsaroth slams through your protection prayer, leaving you feeling drained.");
				}
				npc.setNextAnimation(new Animation(spec ? 14963 : 14968));
				delayHit(npc, 0, e, getMeleeHit(npc, getRandomMaxHit(npc, damage, NPCCombatDefinitionConstants.MELEE, e)));
				spec = true;
			}
			break;
		}
		hitcount += 1;
		return defs.getAttackDelay();
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { 6203 };
	}
}
