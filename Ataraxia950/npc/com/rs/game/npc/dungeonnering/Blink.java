package com.rs.game.npc.dungeonnering;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.content.dungeoneering.DungeonManager;
import com.rs.game.player.content.dungeoneering.RoomReference;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class Blink extends DungeonBoss {

	private static final int[][] RUSH_COORDINATES = { { 2, 3, 13, 3 }, { 2, 6, 13, 6 }, { 2, 9, 13, 9 }, { 2, 12, 13, 12 }, { 3, 2, 3, 13 },
			{ 6, 2, 6, 13 }, { 9, 2, 9, 13 }, { 12, 2, 12, 13 }, };
	private static final int[] FAILURE_SOUNDS = new int[] { 3005, 3006, 3010, 3014, 3048, 2978 };
	private static final int[] RUSH_SOUNDS = { 2982, 2987, 2988, 2989, 2990, 2992, 2998, 3002, 3004, 3009, 3015, 3017, 3018, 3021, 3026,
			3027, 3031, 3042, 3043, 3047, 3049 };
	private static final String[] RUSH_MESSAGES = { "Grrrr...", "More t...tea Alice?", "Where...who?", "H..here it comes!",
			"See you all next year!", "", "", "", "Coo-coo-ca-choo!", "Ah! Grrrr...", "Aha! Huh? Ahaha!", "", "", "A face! A huuuge face!",
			"Aaahaahaha!", "C...can't catch me!", "A whole new world!", "Over here!", "There's no place like home.",
			"The...spire...doors...everywhere..." };

	private int rushCount, rushStage;
	private int[] selectedPath;
	private boolean inversedPath, specialRequired;
	private WorldTile toPath, activePillar;

	public Blink(final int id, final WorldTile tile, final DungeonManager manager, final RoomReference reference) {
		super(id, tile, manager, reference);
		setForceFollowClose(true);
		setForceAgressive(true);
		setRun(true);
		rushCount = 0;
		rushStage = 4;
	}

	@Override
	public void handleIngoingHit(final Hit hit) {
		if (rushCount == -1) {
			super.handleIngoingHit(hit);
		}
		if (getHitpoints() <= getMaxHitpoints() * (rushStage * .2125)) {
			rushStage--;
			rushCount = 0;
		}
	}

	@Override
	public double getMagePrayerMultiplier() {
		return 1.0;
	}

	@Override
	public double getRangePrayerMultiplier() {
		return 0.6;
	}

	@Override
	public double getMeleePrayerMultiplier() {
		return 0.8;
	}

	private void stopRushAttack() {
		resetWalkSteps();
		rushCount = -1;// stops the rush
		playSoundEffect(FAILURE_SOUNDS[Utils.random(FAILURE_SOUNDS.length)]);
		setNextForceTalk(new ForceTalk("Oof!"));
		setNextAnimation(new Animation(14946));
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				setSpecialRequired(true);
				setCantInteract(false);
			}
		});
	}
	
	@Override
	public ArrayList<Entity> getPossibleTargets(final boolean checkNPCs, final boolean checkPlayers) {
		final ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
		for (final int regionId : getMapRegionsIds()) {
			if (checkPlayers) {
				final List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
				if (playerIndexes != null) {
					for (final int playerIndex : playerIndexes) {
						final Player player = World.getPlayers().get(playerIndex);
						if (player == null || player.isDead() || player.hasFinished() || !player.isRunning()
								|| player.getPlane() != getPlane() || player.getAppearence().isHidden()
								|| !clipedProjectile(player, false) && player.getTileHash() != getMiddleWorldTile().getTileHash()) {
							continue;
						}
						possibleTarget.add(player);
					}
				}
			}
		}
		return possibleTarget;
	}

	@Override
	public boolean canMove(final int dir) {
		if (!hasActivePillar() || rushCount >= 0 && rushCount <= 11) {
			return true;
		}
		final int currentX = getX();
		final int currentY = getY();

		final int xOffset = Utils.DIRECTION_DELTA_X[dir];
		final int yOffset = Utils.DIRECTION_DELTA_Y[dir];

		if ((currentX == activePillar.getX() && currentY == activePillar.getY())
				|| ((xOffset + currentX) == activePillar.getX() && (yOffset + currentY) == activePillar.getY())) {
			stopRushAttack();
			return false;
		}
		return true;
	}
	
	@Override
	public void resetWalkSteps() {
		super.resetWalkSteps();
	}

	@Override
	public void processNPC() {
		super.processNPC();
		if (rushCount > -1) {
			if (getManager().isDestroyed() || isDead()) {
				return;
			}
			rushCount++;
			if (rushCount == 1) {
				getCombat().setTarget(null);
				resetWalkSteps();
				setNextFaceEntity(null);
				resetCombat();
				setTarget(null);
				setCantInteract(true);
			} else if (rushCount == 3) {
				setNextForceTalk(new ForceTalk("He saw me!"));
				playSoundEffect(3017);
			} else if (rushCount == 4) {
				setNextAnimation(new Animation(14994));
				setNextGraphics(new Graphics(2868));
			} else if (rushCount == 15 || rushCount == 5) {
				if (rushCount == 15) {
					rushCount = 5;
				}
				setNextNPCTransformation(1957);
			} else if (rushCount == 8) {
				setNextWorldTile(getNextPath());
			} else if (rushCount == 9) {
				setNextNPCTransformation(12865);
				toPath = getManager().getTile(getReference(), selectedPath[inversedPath ? 2 : 0], selectedPath[inversedPath ? 3 : 1]);
				addWalkSteps(toPath.getX(), toPath.getY(), -1, false);
			} else if (rushCount == 10) {
				final int index = Utils.random(RUSH_MESSAGES.length);
				setNextForceTalk(new ForceTalk(RUSH_MESSAGES[index]));
				playSoundEffect(RUSH_SOUNDS[index]);
			} else if (rushCount == 11) {
				setNextGraphics(new Graphics(2869));
				for (final Player player : getManager().getParty().getTeam()) {
					if (!getManager().getCurrentRoomReference(this).equals(getManager().getCurrentRoomReference(player))) {
						continue;
					}
					if (!Utils.isOnRange(player.getX(), player.getY(), 1, getX(), getY(), 1, 4)) {
						continue;
					}
					int damage = Utils.random(200, 600);
					if (player.getPrayer().isMageProtecting() || player.getPrayer().isRangeProtecting()) {
						damage *= .5D;
					}
					player.setNextGraphics(new Graphics(2854));
					player.applyHit(new Hit(this, damage, HitLook.REGULAR_DAMAGE, 35));

				}
			}
		}
	}

	public WorldTile getNextPath() {
		selectedPath = RUSH_COORDINATES[Utils.random(RUSH_COORDINATES.length)];
		inversedPath = Utils.random(2) == 0;
		return getManager().getTile(getReference(), selectedPath[inversedPath ? 0 : 2], selectedPath[inversedPath ? 1 : 3]);
	}

	public void raisePillar(final WorldObject selectedPillar) {
		final WorldObject newPillar = new WorldObject(selectedPillar);
		newPillar.setId(32196);// Our little secret :D
		activePillar = new WorldTile(selectedPillar);
		World.spawnObjectTemporary(newPillar, 2500, true, true);
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				activePillar = null;
			}
		}, 4);
	}

	public boolean hasActivePillar() {
		return activePillar != null;
	}

	public boolean isSpecialRequired() {
		return specialRequired;
	}

	public void setSpecialRequired(final boolean specialRequired) {
		this.specialRequired = specialRequired;
	}
}
