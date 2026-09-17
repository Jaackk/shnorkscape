package com.rs.game.npc.dragons;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/*
 * author Movee
 */

@SuppressWarnings("serial")
public class CelestialDragonB extends NPC {

	public boolean familiarTeleported;
	public WorldTile familiarTile = null;
	public Entity playerTarget = null;
	
	public CelestialDragonB(final int id, final WorldTile tile) {
		super(id, tile, -1, true, true);
	}
	
	@Override
	public void processNPC() {
		super.processNPC();
	}
	
	@Override
	public void handleIngoingHit(final Hit hit) {
		playerTarget = hit.getSource();
		handlePrayers(hit);
	}
	
	@Override
    public double getMeleePrayerMultiplier() {
		return 0.4;
    }
	
	
	@Override
	public double getMagePrayerMultiplier() {
		return 0.4;
	}
	
	@Override
    public double getRangePrayerMultiplier() {
		return 0.4;
    }
	
	
	@Override
	public void sendDeath(final Entity source) {
		if(playerTarget != null) {
			final Familiar familiar = ((Player) playerTarget).getFamiliar();
			
			if(familiarTile != null && familiar != null) {
				familiar.setNextWorldTile(familiarTile);
				familiar.setCantInteract(false);
				familiar.callBlocked = false;
			}
			
			playerTarget.setFreezeDelay(0);
			
		}
		
		final NPCCombatDefinition defs = getCombatDefinitions();

		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					setNextAnimation(new Animation(defs.getDeathEmote()));
				} else if (loop == 3) {
					drop();
					reset();
					getCombat().removeTarget();
					setLocation(getRespawnTile());
					finish();
					setRespawnTask();
					stop();
				}
				loop++;
			}
		}, 0, 1);
	
	}

}
