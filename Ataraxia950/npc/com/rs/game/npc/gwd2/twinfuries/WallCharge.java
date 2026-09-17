package com.rs.game.npc.gwd2.twinfuries;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceMovement;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.NewForceMovement;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.concurrent.TimeUnit;

import static com.rs.game.ForceMovement.EAST;
import static com.rs.game.ForceMovement.NORTH;
import static com.rs.game.ForceMovement.SOUTH;
import static com.rs.game.ForceMovement.WEST;

public class WallCharge extends TwinSpecialAttack {

	public WallCharge(NPC npc, Entity entity) {
		super(npc, entity);
		this.npc = (Avaryss) npc;
		target = entity;
	}

	private final Avaryss npc;
	private boolean flying, finished;
	private int distance;
	private WorldTile target;
	private NewForceMovement movement;
	private static final String[] WALL_CHARGE_MESSAGES = new String[] { "Ha ha ha!", "Keep them busy, sister!", "Think you can dodge me?" };

	private void fly(int direction) {
		WorldTasksManager.schedule(new WorldTask() {
			private int ticks;

			@Override
			public void run() {
				if (npc.isDead() || npc.hasFinished() || entity.isDead() || entity.hasFinished()) {
					stop();
					npc.setCantInteract(false);
					return;
				}
				if (ticks == 0) {
					movement = getForceMovement(direction, true);
					npc.setNextAnimation(new Animation(direction == SOUTH ? 28502 : 28506));
					npc.setNextForceMovement(movement);
				} else if (ticks == 1) {
					npc.setNextWorldTile(movement.getToSecondTile());
					movement = getForceMovement(direction, false);
					npc.setNextAnimation(new Animation(28503));
					npc.setNextFaceWorldTile(movement.getToSecondTile());
				} else if (ticks == 2) {
					distance = 0;
					flying = true;
					npc.setNextForceTalk(new ForceTalk(WALL_CHARGE_MESSAGES[Utils.random(3)]));
					npc.setNextForceMovement(movement);
					npc.setNextAnimation(new Animation(28504));
					npc.setNextGraphics(new Graphics(6146));
				} else if (ticks == 4) {
					flying = false;
					npc.setNextWorldTile(movement.getToSecondTile());
				} else if (ticks == 5) {
					if (direction == WEST)
						npc.setNextAnimation(new Animation(28505));
					else
						stop();
				} else if (ticks == 6) {
					npc.setCantInteract(false);
					npc.getCombat().setTarget(entity);
					stop();
				}
				ticks++;
			}
		}, 0, 0);
	}

	private NewForceMovement getForceMovement(final int direction, boolean start) {
		final int duration = start ? 1 : 3;
		switch (direction) {
		case SOUTH:
			return new NewForceMovement(npc, 0, npc.getInstance().getWorldTile(target.getXInRegion() - 1, start ? 21 : 38), duration, getDirection(direction));
		case NORTH:
			return new NewForceMovement(npc, 0, npc.getInstance().getWorldTile(target.getXInRegion() - 1, start ? 38 : 21), duration, getDirection(direction));
		case EAST:
			return new NewForceMovement(npc, 0, npc.getInstance().getWorldTile(start ? 38 : 21, target.getYInRegion() - 1), duration, getDirection(direction));
		default:
			return new NewForceMovement(npc, 0, npc.getInstance().getWorldTile(start ? 21 : 38, target.getYInRegion() - 1), duration, getDirection(direction));
		}
	}

	private final int getDirection(final int direction) {
		switch (direction) {
		case SOUTH:
			return ForceMovement.getDirection(NORTH);
		case NORTH:
			return ForceMovement.getDirection(SOUTH);
		case EAST:
			return ForceMovement.getDirection(WEST);
		default:
			return ForceMovement.getDirection(EAST);
		}
	}

	private final void checkTarget(final int ms) {
		if (ms % 50 == 0) {
			byte[] dirs = Utils.DIRS[npc.getDirection() / 2048];
			final WorldTile npcTile = new WorldTile(npc.getCoordFaceX(npc.getSize()) + (distance * dirs[0]), npc.getCoordFaceY(npc.getSize()) + (distance * dirs[1]), npc.getPlane());
			npc.getInstance().getPlayers().forEach(p -> {
				if (!p.isLocked() && p.getTileHash() == npcTile.getTileHash()) {
					target = new WorldTile(movement.getToSecondTile().getX() + 1, movement.getToSecondTile().getY() + 1, p.getPlane());
					p.stopAll();
					p.lock();
					final NewForceMovement movement = new NewForceMovement(p, 0, target, p.getDistance(target) * 9, npc.getDirection());
					movement.setForceMovementPrecise(true);
					p.setNextForceMovement(movement);
					p.setCantDoDefenceEmote(true);
					p.setNextAnimationForce(new Animation(19502));
					WorldTasksManager.schedule(new WorldTask() {
						@Override
						public void run() {
							p.setNextWorldTile(target);
							p.unlock();
							p.setCantDoDefenceEmote(false);
							p.applyHit(new Hit(npc, npc.getInstance().isHardMode() ? Utils.random(250, 400) : Utils.random(150, 200), HitLook.MELEE_DAMAGE));
							if (npc.getInstance().isHardMode()) {
								p.setNextGraphics(new Graphics(3911));
								p.getPrayer().closeAllPrayers();
							}

						}
					}, 2);
				}
			});
			distance++;
		}
	}

	@Override
	public void effect() {
		npc.setCantInteract(true);
		npc.setTarget(null);
		npc.getCombat().setTarget(null);
		CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
			private int ms, ticks;

			@Override
			public boolean repeat() {
				if (npc.isDead() || npc.hasFinished() || entity.isDead() || entity.hasFinished()) {
					npc.setCantInteract(false);
					return false;
				}
				if (finished)
					return false;
				if (flying)
					checkTarget(ms);
				if (ms == 0) {
					/**
					 * Needs to be done this way due to the synchronization of player/npc updating.
					 */
					WorldTasksManager.schedule(new WorldTask() {
						@Override
						public void run() {
							switch (ticks) {
							case 0:
								fly(SOUTH);
								break;
							case 1:
								fly(EAST);
								break;
							case 2:
								fly(NORTH);
								break;
							case 3:
								fly(WEST);
								break;
							case 4:
								finished = true;
								stop();
								return;
							}
							ticks++;
						}
					}, 0, 4);
				}
				ms++;
				return true;
			}
		}, 0, 1, TimeUnit.MILLISECONDS);
	}

}
