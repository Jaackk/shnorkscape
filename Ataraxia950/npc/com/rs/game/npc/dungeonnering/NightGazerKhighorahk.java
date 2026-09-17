package com.rs.game.npc.dungeonnering;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.content.dungeoneering.DungeonConstants;
import com.rs.game.player.content.dungeoneering.DungeonManager;
import com.rs.game.player.content.dungeoneering.RoomReference;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import lombok.val;

@SuppressWarnings("serial")
public final class NightGazerKhighorahk extends DungeonBoss {

	private boolean secondStage;
	private boolean usedSpecial;
	private int lightCount;

	public NightGazerKhighorahk(final int id, final WorldTile tile, final DungeonManager manager, final RoomReference reference) {
		super(id, tile, manager, reference);
		setCantFollowUnderCombat(true); //force cant walk
	}

	public boolean isSecondStage() {
		return secondStage;
	}

	@Override
	public void sendDeath(final Entity source) {
		if (!secondStage) {
			secondStage = true;
			setNextAnimation(new Animation(getCombatDefinitions().getDeathEmote()));
			setNextNPCTransformation(9739);
			setCombatLevel((int) (getCombatLevel() * 0.85)); //15% nerf
			setHitpoints(getMaxHitpoints());
			resetBonuses();
			
			val region = World.getRegion(this.getRegionId());
			val objects = region.getAllObjects();
			if (objects == null || lightCount == 4) {
				return;
			}
			for (val object : objects) {
				if (object == null || object.getId() != 49265) {
					continue;
				}
				val light = new WorldObject(object);
				light.setId(49266);
				World.spawnObject(light);
			}
			lightCount = 4;
			return;
		}
		super.sendDeath(source);
	}

	public boolean isUsedSpecial() {
		return usedSpecial;
	}

	public void setUsedSpecial(final boolean usedSpecial) {
		this.usedSpecial = usedSpecial;
	}

	@Override
	public void handleIngoingHit(final Hit hit) {
		if (!secondStage) {
			reduceHit(hit);
		}
		super.handleIngoingHit(hit);
	}

	public void reduceHit(final Hit hit) {
		if (hit.getLook() != HitLook.MELEE_DAMAGE && hit.getLook() != HitLook.RANGE_DAMAGE && hit.getLook() != HitLook.MAGIC_DAMAGE) {
			return;
		}
		hit.setDamage((int) (hit.getDamage() * lightCount * 0.25));
	}

	public void lightPillar(final Player player, final WorldObject object) {
		if (lightCount >= 4) {
			return;
		}
		if (!player.getInventory().containsItem(DungeonConstants.TINDERBOX, 1) && !player.getDungeoneeringToolbelt().containsTool(DungeonConstants.TINDERBOX)) {
			player.getPackets().sendGameMessage("You need a tinderbox to do this.");
			return;
		}
		player.setNextAnimation(new Animation(833));
		final WorldObject light = new WorldObject(object);
		light.setId(object.getId() + 1);

		World.spawnObject(light);
		lightCount++;

		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				try {
					if (secondStage) {
						return;
					}
					lightCount--;
					if (light != null) {
						World.removeObject(light);
					}
					for (final Entity target : getPossibleTargets()) {
						if (target == null) {
							continue;
						}
						if (target.withinDistance(light, 2)) {
							target.applyHit(new Hit(NightGazerKhighorahk.this, Utils.random((int) (target.getMaxHitpoints() * 0.25)) + 1, HitLook.REGULAR_DAMAGE));
							if (target instanceof Player) {
								((Player) target).getPackets().sendGameMessage("You are damaged by the shadows engulfing the pillar of light.");
							}
						}
					}
				} catch (final Throwable e) {
					Logger.getGlobal().catching(e);
				}
			}
			
		}, 49 - (getManager().getParty().getSize() * 5));
	}

	/*  @Override
	  public void sendDeath(final Entity source) {
	final NPCCombatDefinition defs = getCombatDefinitions();
	resetWalkSteps();
	getCombat().removeTarget();
	setNextAnimation(null);
	WorldTasksManager.schedule(new WorldTask() {
	    int loop;

	    @Override
	    public void run() {
		if (loop == 0) {
		    setNextAnimation(new Animation(defs.getDeathEmote()));
		} else if (loop >= defs.getDeathDelay()) {
		    if (source instanceof Player)
			((Player) source).getControlerManager().processNPCDeath(NightGazerKhighorahk.this);
		    drop();
		    reset();
		    if (source.getAttackedBy() == NightGazerKhighorahk.this) { //no need to wait after u kill
			source.setAttackedByDelay(0);
			source.setAttackedBy(null);
			source.setFindTargetDelay(0);
		    }
		    setCantInteract(true);
		    setNextNPCTransformation(9781);
		    stop();
		}
		loop++;
	    }
	}, 0, 1);
	getManager().openStairs(getReference());
	  }*/

}
