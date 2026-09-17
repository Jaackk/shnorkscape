package com.rs.game.npc.combat.impl;

import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.utils.Utils;

public class AcheronMammothCombat extends CombatScript {

	/**
	 * TODO: Redo the entire script.. It's garbage.
	 */
	
	@Override
	public int attack(NPC npc, Entity target) {
		/*final NPCCombatDefinition defs = npc.getCombatDefinitions();
		int random = Utils.random(20);
		try {
		if (random == 0) {
			if (target instanceof Player) {
				Player player = (Player) target;
				player.getPrayer().closeAllPrayers();
				player.sendMessage("The mammoth smashes through your prayer");
			}
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, 300, NPCCombatDefinition.MELEE, target)));
			return defs.getAttackDelay();
		} else if (random == 1) {
			npc.setNextForceTalk(new ForceTalk("Freedom!"));
			if (npc.getFreezeDelay() > 0)
				npc.setFreezeDelay(0);
			return defs.getAttackDelay();
		} else if (random == 2) {
			int count = Utils.random(3);
			npc.setNextSecondaryBar(new SecondaryBar(0, 220, 1, false));
			npc.setNextAnimation(new Animation(27820));
			CoresManager.fastExecutor.schedule(new TimerTask() {
				@Override
				public void run() {
					chargeAttack(npc, target);
					for (int i = 0; i < count; i++) {
						CoresManager.fastExecutor.schedule(new TimerTask() {
							@Override
							public void run() {
								chargeAttack(npc, target);
							}
						}, (i + 1) * 3500);
					}
				}

			}, 4200);
			return (10 * (count > 0 ? count : 1)) + 15;
		} else if (!target.withinDistance(npc, 4) || random == 3) {
			WorldTile landTile = new WorldTile(target.getX(), target.getY(), target.getPlane());
			WorldTasksManager.schedule(new WorldTask() {
				int loop;


				@Override
				public void run() {
					switch (loop) {
					case 0:
						npc.setNextAnimation(new Animation(27819));
						npc.setNextGraphics(new Graphics(5934));
						break;
					case 4:
						World.sendStillProjectile(landTile, target, 5935, 200, 0, 50, 0, 0, 0);

						CoresManager.fastExecutor.schedule(new TimerTask() {

							@Override
							public void run() {
								World.sendGraphics(npc, new Graphics(5936), landTile);
								if (target.getHash() == landTile.getHash())
									delayHit(npc, 0, target, getRangeHit(npc, getRandomMaxHit(npc, 300, NPCCombatDefinition.RANGE, target)));
								npc.setTarget(target);
								return;
							}
							
						}, 300);
						break;
					case 5:
						stop();
						return;
					}

					loop++;
				}

			}, 0, 1);
			return 10;
		} else {
			npc.setNextAnimation(new Animation(27818));
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, 350, NPCCombatDefinition.MELEE, target)));
		}
		
		npc.setNextAnimation(new Animation(defs.getAttackEmote()));
		} catch (Exception e) {
			Logger.getGlobal().catching(e);
		}
		return defs.getAttackDelay();*/
		return 0;
	}

	public void chargeAttack(NPC npc, Entity target) {
/*		try {
		npc.setCantInteract(true);
		npc.setTarget(null);
		npc.setRun(true);
		npc.setNextRenderAnimation(3646);
		WorldTile surgeTile, dragTile;
		int offsetX = (npc.getX() - target.getX());
		int offsetY = (npc.getY() - target.getY());
		int distanceFromTarget = Math.abs(offsetX) > Math.abs(offsetY) ? Math.abs(offsetX) : Math.abs(offsetY);
		int	x = (-(offsetX * 6) / distanceFromTarget < 1 ? 1 : distanceFromTarget);
		int	y = (-(offsetY * 6) / distanceFromTarget < 1 ? 1 : distanceFromTarget);
		dragTile = (x > 0 || y > 0) ? (x > y ? new WorldTile(target.getX() + 3, target.getY(), target.getPlane()) : new WorldTile(target.getX(), target.getY() + 3, target.getPlane())) : (x < y ? new WorldTile(target.getX() - 3, target.getY(), target.getPlane()) : new WorldTile(target.getX(), target.getY() - 3, target.getPlane()));
		surgeTile = new WorldTile(npc.getX() + x, npc.getY() + y, npc.getPlane());
		
		PathFinder.simpleWalkTo(npc, surgeTile);
		CoresManager.fastExecutor.schedule(new TimerTask() {
			@Override
			public void run() {
				npc.setNextGraphics(new Graphics(5933));
				if (target.withinDistance(npc, 3)) {
					target.applyHit(new Hit(npc, Utils.random(200), HitLook.REGULAR_DAMAGE));
					if (World.canMoveNPC(dragTile.getPlane(), dragTile.getX(), dragTile.getY(), 1))
						target.setNextWorldTile(new WorldTile(dragTile));
				}
			}

		}, 600);
		if (surgeTile != null) {
			CoresManager.fastExecutor.schedule(new TimerTask() {
				@Override
				public void run() {
					npc.setCantInteract(false);
					npc.setRun(false);
					npc.setNextRenderAnimation(3645);
					npc.setTarget(target);
				}

			}, 3000);
		}
		} catch (Exception e) {
			
			Logger.getGlobal().catching(e);
		}*/
	}

	public WorldTile getDragWorldTile(Entity target, NPC npc) {
		WorldTile tile;
		int dir = Utils.getDirectionBetweenTiles(npc, target);
		if (dir < 4097)
			tile = new WorldTile(target.getX() - 2, target.getY(), target.getPlane());
		else if (dir < 8192)
			tile = new WorldTile(target.getX(), target.getY() - 2, target.getPlane());
		else if (dir < 12288)
			tile = new WorldTile(target.getX(), target.getY() + 2, target.getPlane());
		else
			tile = new WorldTile(target.getX() + 2, target.getY(), target.getPlane());
		return tile;
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { 22007 };
	}

}
