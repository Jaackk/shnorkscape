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
import com.rs.game.npc.others.CrystalShapeShifter;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author Tom
 * @date April 24, 2017
 */

public class CrystalShapeShifterCombat extends CombatScript {

	private final WorldTile[] bleedTiles = new WorldTile[2];

	@Override
	public Object[] getKeys() {
		return new Object[] { "Crystal Shapeshifter" };
	}

	public void combust(NPC npc, Entity target) {
		npc.setNextGraphics(new Graphics(5755));
		npc.setNextAnimation(new Animation(27155));
		bleedTiles[0] = new WorldTile(target.getX(), target.getY(), target.getPlane());
		int damage = Utils.random(25, 50);
		WorldTasksManager.schedule(new WorldTask() {
			int ticks = 0;

			public void run() {
				if (ticks == 0) {
					target.setNextGraphics(new Graphics(3574, 0, 130));
					target.applyHit(new Hit(npc, 1, HitLook.MAGIC_DAMAGE));
				} else if (ticks >= 1 && ticks <= 5) {
					boolean moved = bleedTiles[0].getX() != target.getX() || bleedTiles[0].getY() != target.getY()
							|| bleedTiles[0].getPlane() != target.getPlane();
					target.setNextGraphics(new Graphics(3574, 0, 130));
					delayHit(npc, 0, target, getMagicHit(npc, moved ? damage * 2 : damage));
				} else if (ticks >= 6) {
					npc.setNextGraphics(new Graphics(-1));
					stop();
				}
				ticks++;
			}
		}, 0, 1);
	}

	public void fragmentationShot(NPC npc, Entity target) {
		World.sendProjectile(npc, target, 5753, 42, 35, 150, 240, 0, 0);
		npc.setNextAnimation(new Animation(27151));
		bleedTiles[1] = new WorldTile(target.getX(), target.getY(), target.getPlane());
		int damage = Utils.random(25, 50);
		WorldTasksManager.schedule(new WorldTask() {
			int ticks = 0;

			public void run() {
				if (ticks == 1) {
					target.setNextGraphics(new Graphics(5754));
					target.setNextGraphics(new Graphics(3574, 0, 130));
					target.applyHit(new Hit(npc, 1, HitLook.RANGE_DAMAGE));
				} else if (ticks >= 2 && ticks <= 6) {
					boolean moved = bleedTiles[1].getX() != target.getX() || bleedTiles[1].getY() != target.getY()
							|| bleedTiles[1].getPlane() != target.getPlane();
					target.setNextGraphics(new Graphics(3574, 0, 130));
					delayHit(npc, 0, target, getRangeHit(npc, moved ? damage * 2 : damage));
				} else if (ticks >= 7) {
					npc.setNextGraphics(new Graphics(-1));
					stop();
				}
				ticks++;
			}
		}, 0, 1);
	}

	public void bombardment(NPC npc, Entity target) {
		World.sendProjectile(npc, target, 5753, 42, 35, 150, 240, 0, 0);
		npc.setNextAnimation(new Animation(27151));
		WorldTasksManager.schedule(new WorldTask() {
			int ticks = 0;

			public void run() {
				if (ticks == 1) {
					target.setNextGraphics(new Graphics(3525));
				} else if (ticks == 2)
					delayHit(npc, 0, target, getRangeHit(npc, Utils.random(100, 200)));
				else if (ticks == 3) 
					stop();
				ticks++;
			}
		}, 0, 1);
	}

	public void magicAutoAttack(NPC npc, Entity target) {
		npc.setNextGraphics(new Graphics(5755));
		npc.setNextAnimation(new Animation(27155));
		delayHit(npc, 2, target, getMagicHit(npc, Utils.random(30, 100)));
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				target.setNextGraphics(new Graphics(5756));
				npc.setNextGraphics(new Graphics(-1));
				target.setNextGraphics(new Graphics(-1));
				stop();
			}
		}, 2, 0);
	}

	public void rangedAutoAttack(NPC npc, Entity target) {
		World.sendProjectile(npc, target, 5753, 42, 35, 150, 240, 0, 0);
		npc.setNextAnimation(new Animation(27151));
		delayHit(npc, 2, target, getRangeHit(npc, Utils.random(30, 100)));
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				target.setNextGraphics(new Graphics(5754));
				npc.setNextGraphics(new Graphics(-1));
				stop();
			}
		}, 2, 0);
	}

	public void meleeAttack(NPC npc, Entity target) {
		npc.setNextAnimation(new Animation(27154));
		delayHit(npc, 0, target, getMeleeHit(npc, Utils.random(100, 150)));
		delayHit(npc, 2, target, getMeleeHit(npc, Utils.random(100, 150)));
	}

	@Override
	public int attack(NPC npc, Entity target) {
		CrystalShapeShifter shifter = (CrystalShapeShifter) npc;
		switch (shifter.getId()) {
		case 21630:
			switch (shifter.getPhase()) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
				meleeAttack(npc, target);
				break;
			}
			break;
		case 21631:
			switch (shifter.getPhase()) {
			case 0:
			case 1:
				rangedAutoAttack(npc, target);
				break;
			case 2:
				fragmentationShot(npc, target);
				break;
			case 3:
			case 4:
				rangedAutoAttack(npc, target);
				break;
			case 5:
				bombardment(npc, target);
				break;
			}
			break;
		case 21632:
			switch (shifter.getPhase()) {
			case 0:
			case 1:
			case 2:
				magicAutoAttack(npc, target);
				break;
			case 3:
				combust(npc, target);
				break;
			case 4:
			case 5:
				magicAutoAttack(npc, target);
				break;
			}
			break;
		}
		shifter.nextPhase();
		if (shifter.getPhase() < 0 || shifter.getPhase() > 5)
			shifter.setPhase(0);
		return npc.getAttackSpeed();
	}

}
