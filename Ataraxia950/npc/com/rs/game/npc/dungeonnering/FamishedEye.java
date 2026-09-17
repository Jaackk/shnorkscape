package com.rs.game.npc.dungeonnering;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.content.dungeoneering.DungeonManager;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class FamishedEye extends DungeonNPC {

	private final HitLook weakness;
	private final HitLook type;
	private int sleepCycles;
	private boolean firstHit;
	private final WorldGorgerShukarhazh boss;
	
	public FamishedEye(final WorldGorgerShukarhazh boss, int id, WorldTile tile, final DungeonManager manager, double multiplier) {
		super(id, tile, manager, multiplier);
		this.boss = boss;
		this.sleepCycles = -1;
		int rotationType = (id - 12436) / 15;
		weakness = findWeakness(rotationType);
		type = findType(rotationType);
		setCantFollowUnderCombat(true);
		setForceAgressive(true);
		setCantSetTargetAutoRelatio(true);
		int rotation = manager.getRoom(manager.getCurrentRoomReference(this)).getRotation() + rotationType;
		setDirection(Utils.getAngle(Utils.ROTATION_DIR_X[(rotation + 1) & 0x3], Utils.ROTATION_DIR_Y[(rotation + 1) & 0x3]));
	}

	@Override
	public boolean clipedProjectile(WorldTile tile, boolean checkClose, int size) {
		//because npc is under cliped data
		return getManager().isAtBossRoom(tile);
	}

	@Override
	public void setNextFaceEntity(Entity entity) {
		//this boss doesnt face
	}

	@Override
	public void handleIngoingHit(Hit hit) {
		if (weakness != hit.getLook())
			hit.setDamage((int) (hit.getDamage() * 0.2));
		super.handleIngoingHit(hit);
	}

	@Override
	public void processNPC() {
		super.processNPC();
		if (sleepCycles == 45) {
			resetSleepCycles();
		} else if (sleepCycles >= 0)
			sleepCycles++;
	}

	private void resetSleepCycles() {
		sleepCycles = -1;
		setCantInteract(false);
		setNextAnimation(new Animation(-1));
		setHitpoints(getMaxHitpoints());
		boss.refreshCapDamage();
		for (Entity t : getPossibleTargets())
			if (t instanceof Player)
				((Player) t).getPackets().sendGameMessage("The creature shifts, and one of it's many eyes opens.");
	}

	@Override
	public void drop() {

	}

	@Override
	public void sendDeath(Entity source) {
		sleepCycles++;//start the cycles
		boss.refreshCapDamage();
		setCantInteract(true);
		setNextAnimation(new Animation(14917));
		for (Entity t : getPossibleTargets())
			if (t instanceof Player)
				((Player) t).getPackets().sendGameMessage("The creature shifts, and one of it's many eyes closes.");
	}

	public boolean isInactive() {
		return sleepCycles != -1;
	}

	public WorldGorgerShukarhazh getBoss() {
		return boss;
	}
	
	public HitLook getType() {
		return type;
	}

	public HitLook getWeaknessStyle() {
		return weakness;
	}

	private HitLook findType(int rotation) {
		switch (rotation) {
		case 0: //mage
			return HitLook.MELEE_DAMAGE;
		case 1: //warrior
			return HitLook.MAGIC_DAMAGE;
		default: //range
			return HitLook.RANGE_DAMAGE;
		}
	}
	
	private HitLook findWeakness(int type) {
		switch (type) {
		case 0: //mage
			return HitLook.RANGE_DAMAGE; //range was mage 2
		case 1: //warrior
			return HitLook.MAGIC_DAMAGE; //mage was melee 0
		default: //range
			return HitLook.MELEE_DAMAGE; //melee was range 1
		}
	}

	public boolean isFirstHit() {
		return firstHit;
	}

	public void setFirstHit(boolean firstHit) {
		this.firstHit = firstHit;
	}
}
