package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.slayer.RipperDemon;
import com.rs.game.player.actions.slayer.elite.SlasherDemon;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author Tom
 * @date April 19, 2017
 */

public class RipperDemonCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 21994, 21995 };
	}

	public void autoAttack(final NPC npc, final Entity target) {
		npc.setNextAnimation(new Animation(27769));
		npc.setNextGraphics(new Graphics(5914));
		final int random = Utils.random(1, 4);
		for (int i = 0; i < random; i++) {
			delayHit(npc, 0, target, getRegularHit(npc, Utils.random(50, 70)));
		}
	}

	public void shadowJump(final NPC npc, final Entity target) {
		final WorldTile coords = new WorldTile(target.getX() - Utils.random(2), target.getY(), target.getPlane());
		npc.setNextWorldTile(new WorldTile(5155, 7622, 0));
		npc.setNextAnimation(new Animation(27775));
		npc.setCantInteract(true);
		if (target.getX() >= 5132 && target.getX() <= 5168 && target.getY() >= 7570 && target.getY() <= 7596) {
			WorldTasksManager.schedule(new WorldTask() {
				int ticks = 0;

				@Override
				public void run() {
					switch (ticks) {
					case 2:
						World.sendGraphics(target, new Graphics(5918), coords);
						break;
					case 4:
						if (target.withinDistance(coords, 1)) {
							target.applyHit(new Hit(npc, target.getHitpoints(), HitLook.REGULAR_DAMAGE));
						}
						World.sendGraphics(target, new Graphics(5919), coords);
						npc.setNextWorldTile(coords);
						npc.setNextAnimation(new Animation(27776));
						break;
					case 5:
						npc.setNextGraphics(new Graphics(5919));
						npc.setNextWorldTile(new WorldTile(target.getX() - 1, target.getY(), target.getPlane()));
						npc.setCantInteract(false);
						npc.setForceAgressive(true);
						break;
					case 6:
						npc.setForceAgressive(false);
						stop();
						break;
					}
					ticks++;
				}
			}, 0, 1);
		}
	}

	@Override
	public int attack(final NPC npc, final Entity target) {
		if (npc instanceof SlasherDemon) {
			final SlasherDemon ripperDemon = (SlasherDemon) npc;
			ripperDemon.setRun(true);
			switch (ripperDemon.getPhase()) {
			case 0:
			case 1:
			case 2:
			case 3:
				autoAttack(npc, target);
				break;
			case 4:
				shadowJump(npc, target);
				break;
			}
			ripperDemon.nextPhase();
			if (ripperDemon.getPhase() < 0 || ripperDemon.getPhase() > 4) {
				ripperDemon.setPhase(0);
			}
			return npc.getAttackSpeed() + Utils.random(1, 3);
		}
		final RipperDemon ripperDemon = (RipperDemon) npc;
		ripperDemon.setRun(true);
		switch (ripperDemon.getPhase()) {
		case 0:
		case 1:
		case 2:
		case 3:
			autoAttack(npc, target);
			break;
		case 4:
			shadowJump(npc, target);
			break;
		}
		ripperDemon.nextPhase();
		if (ripperDemon.getPhase() < 0 || ripperDemon.getPhase() > 4) {
			ripperDemon.setPhase(0);
		}
		return npc.getAttackSpeed() + Utils.random(1, 3);
	}

}