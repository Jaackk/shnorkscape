package com.rs.game.npc.vorago;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class StoneClone extends NPC {

	private final transient Vorago vorago;
	private final transient Player target;

	public StoneClone(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, Vorago vorago,
			Player target) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
		this.vorago = vorago;
		this.target = target;
		getCombat().setTarget(target);
		setForceMultiArea(true);
		setName("Stone " + target.getDisplayName());
		if (id == 17158)
			setForceFollowClose(true);
	}

	@Override
	public ArrayList<Entity> getPossibleTargets() {
		ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
		if (target != null && !target.isDead())
			possibleTarget.add(target);
		return possibleTarget;
	}

	@Override
	public boolean checkAgressivity() {
		ArrayList<Entity> possibleTarget = getPossibleTargets();
		if (!possibleTarget.isEmpty()) {
			Entity target = possibleTarget.get(Utils.random(possibleTarget.size()));
			setTarget(target);
			target.setAttackedBy(target);
			target.setFindTargetDelay(Utils.currentTimeMillis() + 10000);
			return true;
		}
		if (target == null || target.isDead())
			sendDeath(null);
		return false;
	}

	@Override
	public void sendDeath(final Entity source) {
		final NPCCombatDefinition defs = getCombatDefinitions();
		resetWalkSteps();
		getCombat().removeTarget();
		setNextAnimation(null);
		if (!isDead())
			setHitpoints(0);
		final int deathDelay = defs.getDeathDelay() - (getId() == 50 ? 2 : 1);
		StoneClone thisNPC = this;
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					setNextAnimation(new Animation(defs.getDeathEmote()));
				} else if (loop >= deathDelay) {
					if (source != null) {
						if (source instanceof Player)
							((Player) source).getControlerManager().processNPCDeath(thisNPC);
					}
					reset();
					finish();
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}

	public Player getTarget() {
		return target;
	}

	public Vorago getVorago() {
		return vorago;
	}
}
