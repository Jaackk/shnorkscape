package com.rs.game.npc.combat.impl.gwd2;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.NewProjectile;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.gwd2.vindicta.Vindicta;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class VindictaCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 22459, 22460, 22461, 22462 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		Vindicta vindicta = (Vindicta) npc;
		final int phase = vindicta.getPhase();
		final int players = vindicta.getInstance().getPlayers().size();
		vindicta.nextPhase();
		if (vindicta.getPhase() < 0 || vindicta.getPhase() > (vindicta.getId() == 22460 || vindicta.getId() == 22462 ? 3 : (players > 1 ? 9 : 6)))
			vindicta.setPhase(0);
		if (npc.getId() == 22459 || npc.getId() == 22461) {
			if (!vindicta.performedHurricane()) {
				switch (phase) {
				case 2:
					vindicta.setPhase(0);
					vindicta.setHasPerformedHurricane();
					return hurricane(vindicta, target);
				default:
					return sliceAttack(vindicta, target);
				}
			} else {
				if (players > 1) {
					switch (phase) {
					case 3:
						return rangedAttack(vindicta, target);
					case 5:
						return dragonfireWall(vindicta, target);
					case 9:
						return hurricane(vindicta, target);
					default:
						return sliceAttack(vindicta, target);
					}
				} else {
					switch (phase) {
					case 2:
						return dragonfireWall(vindicta, target);
					case 6:
						return hurricane(vindicta, target);
					default:
						return sliceAttack(vindicta, target);
					}
				}
			}
		} else {
			switch (phase) {
			case 1:
				return rangedAttack(vindicta, target);
			case 3:
				return secondDragonfireWall(vindicta, target);
			default:
				return sliceAttack(vindicta, target);
			}
		}
	}

	/**
	 * Performs the basic auto-attack slice.
	 */
	private final int sliceAttack(Vindicta npc, Entity target) {
		npc.setNextAnimation(new Animation(npc.getId() == 22459 || npc.getId() == 22461 ? 28253 : 28273));
		delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, 150, NPCCombatDefinitionConstants.MELEE, target)));
		return 4;
	}

	private final int hurricane(Vindicta npc, Entity target) {
		npc.setCannotMove(true);
		npc.resetWalkSteps();
		WorldTasksManager.schedule(new WorldTask() {
			final WorldTile tile = new WorldTile(npc.getCoordFaceX(npc.getSize()), npc.getCoordFaceY(npc.getSize()), npc.getPlane());

			@Override
			public void run() {
				npc.setNextAnimation(new Animation(28256));
				npc.setNextGraphics(new Graphics(6111));
				npc.getInstance().getPlayers().forEach(p -> {
					if (p.getDistance(tile) < 3 && p.getTileHash() != tile.getTileHash())
						delayHit(npc, 0, target, getMeleeHit(npc, npc.getInstance().isHardMode() ? Utils.random(400, 500) : Utils.random(80, 125)));
					if (p.getFamiliar() != null && p.getFamiliar().getDistance(tile) < 3 && p.getFamiliar().getTileHash() != tile.getTileHash() && p.getFamiliar().getDefinitions().hasAttackOption())
						delayHit(npc, 0, p.getFamiliar(), getMeleeHit(npc, npc.getInstance().isHardMode() ? Utils.random(400, 500) : Utils.random(80, 125)));
				});
				npc.setCannotMove(false);
			}
		}, 0);
		return 6;
	}

	final boolean addFire(Vindicta npc, WorldTile target, boolean secondary) {
		if (!World.canMoveNPC(target.getPlane(), target.getX(), target.getY(), 1))
			return false;
		if (secondary && target.getDistance(new WorldTile(npc.getCoordFaceX(npc.getSize()), npc.getCoordFaceY(npc.getSize()), npc.getPlane())) <= 2)
			return false;
		final WorldTile corner = npc.getInstance().getWorldTile(0, 0);
		return target.getX() >= corner.getX() + 16 && target.getX() <= corner.getX() + 39 && target.getY() >= corner.getY() + 8 && target.getY() <= corner.getY() + 39;
	}

	private final WorldTile getCorner(Vindicta npc, Entity target) {
		final WorldTile zero = npc.getInstance().getWorldTile(0, 0);
		for (;;) {
			final WorldTile t = new WorldTile(zero.getX() + Vindicta.CORNERS[Utils.random(4)][0], zero.getY() + Vindicta.CORNERS[Utils.random(4)][1], zero.getPlane());
			if (t.getDistance(target) > 8)
				return t;
		}
	}

	private final int secondDragonfireWall(Vindicta npc, Entity target) {
		final WorldTile destination = getCorner(npc, target);
		npc.resetWalkSteps();
		npc.resetCombat();
		npc.setNextGraphics(new Graphics(6118));
		npc.setCannotMove(true);
		npc.setNextAnimation(new Animation(28275));
		npc.getTemporaryAttributtes().put("rangedDelay", Utils.currentTimeMillis() + 15000);
		WorldTasksManager.schedule(new WorldTask() {
			private int ticks;
			private WorldTile[] array;

			@Override
			public void run() {
				if (ticks == 2) {
					npc.resetWalkSteps();
					npc.getInstance().getPlayers().forEach(p -> p.stopAll());
					npc.setNextWorldTile(destination);
					npc.setNextFaceWorldTile(new WorldTile(target));
					npc.setNextGraphics(new Graphics(6118));
					npc.setNextAnimation(new Animation(28276));
				} else if (ticks == 5) {
					npc.setNextAnimationForce(new Animation(28274));
				} else if (ticks == 6) {
					final List<WorldTile> tiles = new ArrayList<WorldTile>();
					final WorldTile dest = new WorldTile(npc.getCoordFaceX(npc.getSize()), npc.getCoordFaceY(npc.getSize()), npc.getPlane());
					final WorldTile zero = npc.getInstance().getWorldTile(0, 0);
					final int minBoundaryX = zero.getX() + 15;
					final int minBoundaryY = zero.getY() + 7;
					final int startX = dest.getX();
					final int startY = dest.getY();
					final int maxBoundaryX = zero.getX() + 40;
					final int maxBoundaryY = zero.getY() + 40;
					float m = (float) (target.getY() - startY) / (float) (target.getX() - startX);
					final float intercept = ((float) target.getY() - m * (float) target.getX());
					float y = (float) target.getY();
					float x = (float) target.getX();
					if (x - startX == 0) {
						boolean down = y < startY;
						y = down ? minBoundaryY : maxBoundaryY;
					} else {
						boolean left = x < startX;
						for (; left ? x >= minBoundaryX : x <= maxBoundaryX; x = left ? x - 1 : x + 1) {
							if (m * x + intercept < maxBoundaryY)
								y = (m * x) + intercept;
							else
								break;
						}
					}
					final List<WorldTile> t = Utils.calculateLine(startX, startY, Math.round(x), Math.round(y), npc.getPlane());
					t.forEach(tile -> {
						if (addFire(npc, tile, true))
							tiles.add(tile);
					});
					npc.setCannotMove(false);
					array = tiles.toArray(new WorldTile[tiles.size()]);
					npc.addFires(array);
					npc.resetWalkSteps();
					npc.getCombat().setTarget(target);
				} else if (ticks == 55) {
					npc.removeFires(array);
					stop();
				}
				ticks++;
			}
		}, 0, 0);
		return 10;
	}

	private final int dragonfireWall(Vindicta npc, Entity target) {
		final int direction = Utils.random(16000);
		
		
		WorldTasksManager.schedule(new WorldTask() {
			private WorldTile dest;
			private int ticks;
			private WorldTile[] array;
			private final WorldTile[] freeSpots = new WorldTile[2];
			final int dir = (int) Math.ceil(direction / 2048);
			@Override
			public void run() {
				if (ticks == 0) {
					npc.setNextAnimation(new Animation(28259));
					World.sendGraphics(target, new Graphics(6114, 0, 0, dir), new WorldTile(target));
				} else if (ticks == 1) {
					final List<WorldTile> tiles = new ArrayList<WorldTile>();
					for (int i = -30; i < 30; i++) {
						dest = new WorldTile(target.getX() + (Utils.DIRS[dir][0] * i), target.getY() + (Utils.DIRS[dir][1] * i), target.getPlane());
						if (addFire(npc, dest, false))
							tiles.add(dest);
					}
					if (tiles.size() > 10) {
						int index = 0;
						for (;;) {
							index = Utils.random(2, tiles.size() - 2);
							if (target.getDistance(tiles.get(index)) < 3)
								continue;
							for (int i = 0; i < 2; i++) {
								freeSpots[i] = tiles.get(index);
								npc.addSafeTile(tiles.remove(index));
							}
							break;
						}
					}
					array = tiles.toArray(new WorldTile[tiles.size()]);
					npc.addFires(array);
					npc.getTemporaryAttributtes().put("rangedDelay", Utils.currentTimeMillis() + 5000);
				} else if (ticks == 50) {
					npc.removeFires(array);
					for (int i = 0; i < 2; i++) {
						if (freeSpots[i] != null)
							npc.removeSafeTile(freeSpots[i]);
					}
					stop();
				}
				ticks++;
			}
		}, 0, 0);
		return 4;
	}

	public static final int rangedAttack(Vindicta npc, Entity target) {
		final List<Entity> targets = new ArrayList<Entity>();
		npc.getInstance().getPlayers().forEach(p -> {
			if (npc.getInstance().getPlayers().size() == 1) {
				targets.add(p);
				if (p.getFamiliar() != null)
					targets.add(p.getFamiliar());
			} else if (!p.equals(target)) {
				targets.add(p);
				if (p.getFamiliar() != null)
					targets.add(p.getFamiliar());
			}
		});
		targets.forEach(t -> {
			t.setNextGraphics(new Graphics(6113));
			final NewProjectile projectile = new NewProjectile(new WorldTile(npc.getCoordFaceX(npc.getSize()), npc.getCoordFaceY(npc.getSize()), npc.getPlane()), t, 6116, npc.getId() == 22459 || npc.getId() == 22461 ? 50 : 80, 30, 55, 10, 30, 0);
			World.sendProjectile(projectile);
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					delayHit(npc, 0, t, getRangeHit(npc, npc.getInstance().isHardMode() ? Utils.random(80, 125) : Utils.random(80, 125)));
				}
			}, projectile.getTime() / 335);
		});
		return 4;
	}

}
