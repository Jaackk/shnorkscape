
package com.rs.game.npc.Ed3boss1;

import java.util.ArrayList;
import java.util.List;

import com.rs.game.*;
import com.rs.game.npc.NPC;
import com.rs.game.npc.eds.EliteDungeonNPC;
import com.rs.game.npc.solak.Solak;
import com.rs.game.player.Player;
import com.rs.utils.MapAreas;
import com.rs.utils.Utils;

public class Crassianscout extends NPC {
	private transient Entity ctarget;
	public static int aggro_dis = 7;
	public WorldTile[] walkTiles;
	public Crassianscout(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);


		if (this.getId() == 26149 || this.getId() == 26157 || this.getId() == 26154 ||  this.getId() == 26158 || this.getId() == 26163
		|| this.getId() == 26165 ||  this.getId() == 26170 || this.getId() == 26164) {
			walkTiles = new WorldTile[2];
			boolean reverse = Utils.random(2) == 0;
			walkTiles[reverse ? 1 : 0] = tile.transform(-15, 0, 0);
			walkTiles[reverse ? 0 : 1] = tile.transform(+15, 0, 0);
		}

		setForceMultiAttacked(true);
		setForceAgressive(true);
		setForceMultiArea(true);
		setForceFollowClose(true);

		setIntelligentRouteFinder(true);
		setForceTargetDistance(10);
		setHitpoints(4200);
		getCombatDefinitions().setHitpoints(4200);
	}

	@Override
	public void processNPC() {
		super.processNPC();
		for (Entity targets : getPossibleTargets()) {
			setForceAgressive(targets.getHitpoints() < (targets.getMaxHitpoints() / 2));
		}
		if (getId() == 26149 || getId() == 26157 || getId() == 26154) {
			if (!isForceWalking()) {
				if (!isCantInteract()) {
					if (!checkAgressivity()) {
						if (getFreezeDelay() < Utils.currentTimeMillis()) {
							if (!hasWalkSteps() && (getWalkType() & NORMAL_WALK) != 0) {
								boolean can = false;
								for (int i = 0; i < 2; i++) {
									if (Math.random() * 1000.0 < 100.0) {
										can = true;
										break;
									}
								}
								if (can) {
									final int moveX = (int) Math.round(Math.random() * 10.0 - 5.0);
									final int moveY = (int) Math.round(Math.random() * 10.0 - 5.0);
									resetWalkSteps();
									if (getMapAreaNameHash() != -1) {
										if (!MapAreas.isAtArea(getMapAreaNameHash(), this)) {
											forceWalkRespawnTile();
											return;
										}
										addWalkSteps(getX() + moveX, getY() + moveY, 5, (getWalkType() & FLY_WALK) == 0);
									} else {
										addWalkSteps(getRespawnTile().getX() + moveX, getRespawnTile().getY() + moveY, 5, (getWalkType() & FLY_WALK) == 0);
									}
								}
							}
						}
					}
				}
			}
		}
	}
	@Override
	public boolean switchTarget() {
		return false;
	}

	@Override
	public boolean switchTarget(boolean force) {
		return false;
	}

	@Override
	public void setTarget(Entity entity) {

	}
	@Override
	public void spawn() {
		super.spawn();
		setForceMultiArea(true);

		setForceAgressive(true);
	}
	@Override
	public boolean checkAgressivity() {
		ArrayList<Entity> possibleTarget = getPossibleTargets();
		if (ctarget != null && !ctarget.isDead() && !ctarget.hasFinished()) {
			if (getCombat().getTarget() == null) {
				forceWalk = null;
				getCombat().setTarget(ctarget);
				setLastAttackedByTarget(Utils.currentTimeMillis());
				ctarget.setAttackedBy(ctarget);
				ctarget.setFindTargetDelay(Utils.currentTimeMillis() + 10000);
				return true;
			}
			forceWalk = null;
			return true;
		} else {
			ctarget = null;
		}
		for (Entity target : possibleTarget) {
			if (!(target instanceof Player))
				continue;
			if (Utils.isOnRange(this, target, aggro_dis)) {
				forceWalk = null;
				this.ctarget = target;
				getCombat().setTarget(target);
				setLastAttackedByTarget(Utils.currentTimeMillis());
				target.setAttackedBy(target);
				target.setFindTargetDelay(Utils.currentTimeMillis() + 10000);
				return true;
			}
		}
		return false;
	}



	@Override
	public void processHit(Hit hit) {
		super.processHit(hit);
		if (hit.getSource() != null && this.getCombat().getTarget() == null && hit.getSource() instanceof Player && ctarget == null) {
			forceWalk = null;
			ctarget = hit.getSource();
			getCombat().setTarget(ctarget);
			setLastAttackedByTarget(Utils.currentTimeMillis());
			ctarget.setAttackedBy(ctarget);
			ctarget.setFindTargetDelay(Utils.currentTimeMillis() + 6000);
		}
	}

	@Override
	public void setNextGraphics(Graphics nextGraphics) {
		if (getTemporaryAttributtes() == null || getTemporaryAttributtes().get("cantDoAnimationOrGFX") != null)
			return;
		super.setNextGraphics(nextGraphics);
	}

	@Override
	public void setNextFaceEntity(Entity entity) {
		if (getName().equalsIgnoreCase("Defence Pylon")) {
			super.setNextFaceEntity(null);
			return;
		}
		if (getTemporaryAttributtes() == null || getTemporaryAttributtes().get("cantDoAnimationOrGFX") != null)
			return;
		super.setNextFaceEntity(entity);
	}

	@Override
	public void setNextFaceWorldTile(WorldTile nextFaceWorldTile) {
		if (getName().equalsIgnoreCase("Defence Pylon")) {
			super.setNextFaceWorldTile(getRespawnTile().transform(0, -1, 0));
			return;
		}
		if (getTemporaryAttributtes() == null || getTemporaryAttributtes().get("cantDoAnimationOrGFX") != null)
			return;
		super.setNextFaceWorldTile(nextFaceWorldTile);
	}
	@Override
    public double getMeleePrayerMultiplier() {
		return 0.50;
    }
	
	
	@Override
	public double getMagePrayerMultiplier() {
		return 0.275;
	}
	
	@Override
    public double getRangePrayerMultiplier() {
		return 0.275;
    }
	public void checkReset() {
		int maxhp = getMaxHitpoints();
		if (maxhp > getHitpoints() && !isUnderCombat() && getPossibleTargets().isEmpty())
			setHitpoints(maxhp);
	}
	
	//test
	@Override
	public ArrayList<Entity> getPossibleTargets(final boolean checkNPCs, final boolean checkPlayers) {
		final ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
		for (final Player player : World.getPlayers()) {
			if (player == null || player.isDead() || player.hasFinished() || !player.isRunning() || player.getAppearence().isHidden()) {
				continue;
			}
			possibleTarget.add(player);
		}
		return possibleTarget;
	}
	@Override
	public ArrayList<Entity> getPossibleTargets() {
		return getPossibleTargets(false, true);
	}


	//test

}
