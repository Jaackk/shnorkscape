package com.rs.game.npc.combat.impl;

import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceMovement;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.NewProjectile;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activities.wildywyrm.WildyWyrm;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class WildyWyrmCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return (new Object[] { 3334 });
	}

	@Override
	public int attack(final NPC npc, final Entity target) {
		int hit = WildyWyrm.getWildywyrm().getNextHit();
		WildyWyrm.getWildywyrm().refreshNextHit();
		switch (hit) {
		case 0:
			sendDragAttack(npc);
			return 8;
		case 1:
			npc.setNextAnimation(new Animation(12791));
			delayHit(npc, 1, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getCombatDefinitions().getMaxHit(), NPCCombatDefinitionConstants.MELEE, target)));
			return 3;
		case 2:
			sendRangedAttack(npc, target);
			return 3;
			default:
				sendMagicAttack(npc, target);
				return 3;
		}
	}

	private void sendDragAttack(final NPC npc) {
		for (int i = 0; i < npc.getPossibleTargets().size(); i++) {
			Entity target = npc.getPossibleTargets().get(i);
			HashMap<Player, WorldTile> ts = new HashMap<Player, WorldTile>();
			if (target instanceof Player) {
				Player p = (Player) target;
				final WorldTile tile = new WorldTile(npc.getCoordFaceX(npc.getSize()), npc.getCoordFaceY(npc.getSize()), npc.getPlane());
				WorldTile t = new WorldTile(tile, 1);
				p.lock(1);
				p.setNextForceMovement(new ForceMovement(p, 0, t, 1, p.getDirection()));
				ts.put(p, t);
			}
			ts.forEach((p, t) -> p.setNextWorldTile(t));
		}
		
		npc.setCantInteract(true);
		npc.setNextAnimation(new Animation(12796));
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				npc.setNextNPCTransformation(20629);
			}
		});
		
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				npc.setNextNPCTransformation(3334);
				npc.setNextAnimation(new Animation(12795));
				npc.setCantInteract(false);
				for (int i = 0; i < npc.getPossibleTargets().size(); i++) {
					Entity target = npc.getPossibleTargets().get(i);
					if (target.withinDistance(new WorldTile(npc.getCoordFaceX(npc.getSize()), npc.getCoordFaceY(npc.getSize()), npc.getPlane()), 1))
						target.applyHit(new Hit(target, Utils.random(target.getHitpoints() / 2), HitLook.REGULAR_DAMAGE));
				}
			}
		}, 4);
	}

	private void sendRangedAttack(final NPC npc, final Entity target) {
		npc.setNextAnimation(new Animation(12794));
		NewProjectile projectile = new NewProjectile(new WorldTile(npc.getCoordFaceX(npc.getSize()), npc.getCoordFaceY(npc.getSize()), npc.getPlane()), target, 3934, 100, 16, 25, 16, 30, 0);
		World.sendProjectile(projectile);
		try {
			CoresManager.getServiceProvider().executeWithDelay(new Runnable() {
				@Override
				public void run() {
					target.setNextGraphics(new Graphics(3935));
					for (int i = 0; i < npc.getPossibleTargets().size(); i++) {
						Entity t = npc.getPossibleTargets().get(i);
						if (t == null)
							continue;
						if (t == target || t.withinDistance(target, 1))
							delayHit(npc, 0, t, getRangeHit(npc, getRandomMaxHit(npc, npc.getCombatDefinitions().getMaxHit() / 2, NPCCombatDefinitionConstants.RANGE, t)));
					}
				}
			}, projectile.getTime(), TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			Logger.getGlobal().catching(e);
			
		}
	}

	private void sendMagicAttack(final NPC npc, final Entity target) {
		WorldTile t = null;
		for (int i = 0; i < 10; i++) {
			t = new WorldTile(target, 3);
			if (World.isTileFree(t, 1))
				break;
		}
		if (t == null)
			return;
		npc.setNextAnimation(new Animation(12794));
		NewProjectile projectile = new NewProjectile(new WorldTile(npc.getCoordFaceX(npc.getSize()), npc.getCoordFaceY(npc.getSize()), npc.getPlane()), t, 3934, 100, 16, 25, 16, 30, 0);
		World.sendProjectile(projectile);
		try {
			CoresManager.getServiceProvider().executeWithDelay(new Runnable() {
				@Override
				public void run() {
					ArrayList<Entity> targets = npc.getPossibleTargets();
					if (targets.size() == 0)
						return;
					int size = Utils.random(targets.size());
					if (size == 0)
						size = 1;
					if (size > 3)
						size = Utils.random(1, 3);
					Entity[] t = new Entity[size];
					for (int i = 0; i < size; i++) {
						int index = Utils.random(targets.size());
						Entity target = targets.get(index);
						t[i] = target;
						targets.remove(index);
					}
					for (Entity entities : t) {
						World.sendGraphics(null, new Graphics(3935), new WorldTile(projectile.getTo()));
						NewProjectile p = new NewProjectile(projectile.getTo(), entities, 3934, 34, 16, 25, 16, 30, 0);
						World.sendProjectile(p);
						try {
							CoresManager.getServiceProvider().executeWithDelay(new Runnable() {

								@Override
								public void run() {
									delayHit(npc, 0, entities, getMagicHit(npc, getRandomMaxHit(npc, npc.getCombatDefinitions().getMaxHit() / 2, NPCCombatDefinitionConstants.MAGE, target)));
								}
								
							}, p.getTime(), TimeUnit.MILLISECONDS);
						} catch (Exception e) {
							Logger.getGlobal().catching(e);
						}
					}
				}

			}, projectile.getTime(), TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			Logger.getGlobal().catching(e);
		}
	}
}