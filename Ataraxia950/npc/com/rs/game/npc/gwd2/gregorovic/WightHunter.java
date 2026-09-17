package com.rs.game.npc.gwd2.gregorovic;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class WightHunter extends NPC {

	private static final long serialVersionUID = 8326085001650815464L;
	private final CMGregorovic gregorovic;
	
	@Override
	public int getMaxDistance() {
		return getId() == 22447 ? 1 : 7;
	}
	
	@Override
	public boolean canWalkNPC(int toX, int toY) {
		return true;
	}
	
	public WightHunter(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, CMGregorovic gregorovic) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, true);
		this.gregorovic = gregorovic;
		setForceTargetDistance(50);
		if (gregorovic.getInstance().getPlayers().size() > 0)
			getCombat().setTarget(gregorovic.getInstance().getPlayers().get(0));
		setForceMultiArea(true);
		setNextForceTalk(new ForceTalk("WEEEEEUUUUGGGGHHHH!"));
	}
	
	@Override
	public void applyHit(final Hit hit) {
		super.applyHit(hit);
		if (gregorovic.getHunters() != null) {
			gregorovic.getHunters().forEach(hunter -> {
				if (hunter != null && !hunter.isDead() && hunter != this && hunter.withinDistance(this, 2) && hit.getLook() != HitLook.REGULAR_DAMAGE)
					hunter.applyHit(new Hit(null, hit.getDamage() / 2, HitLook.REGULAR_DAMAGE));
			});
		}
	}

	@Override
	public void sendDeath(final Entity source) {
		final NPCCombatDefinition defs = getCombatDefinitions();
		resetWalkSteps();
		getCombat().removeTarget();
		if (source instanceof Player)
			source.deathResetCombat();
		setNextAnimation(null);
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0)
					setNextAnimation(new Animation(defs.getDeathEmote()));
				else if (loop == defs.getDeathDelay()) {
					drop();
					reset();
					setLocation(getRespawnTile());
					finish();
				} else if (loop == defs.getDeathDelay() + 1) {
					stop();
					gregorovic.checkHunter(getId());
				}
				loop++;
			}
		}, 0, 1);
	}
	
}
