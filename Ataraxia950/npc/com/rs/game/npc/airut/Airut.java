package com.rs.game.npc.airut;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/*
 * author Movee
 */

@SuppressWarnings("serial")
public class Airut extends NPC {

	int tick = 0;
	int hitCheck = 0;
	int stage = 0;
	public Entity target;
	
	public boolean BerserkMode;
	
	public Airut(int id, WorldTile tile) {
		super(id, tile, -1, true, true);
	}
	
	@Override
	public void processNPC() {
		if(isDead() || isCantInteract())
			return;
		
		if(!BerserkMode) {
			if(tick < 50) { 
				tick++;
				//setNextForceTalk(new ForceTalk("Tick: " + tick));
			} else {
				switch(getId()) {
					case 18621:
						hitCheck = 0;
						transformIntoNPC(18622);
						break;
					case 18622:
						hitCheck = 0;
						transformIntoNPC(18621);
						break;
				}
				
				tick = 0;
			}
		} 
		
		super.processNPC();
	}
	
	@Override
    public double getMeleePrayerMultiplier() {
		return 0.7;
    }
	
	
	@Override
	public double getMagePrayerMultiplier() {
		return 0.7;
	}
	
	@Override
    public double getRangePrayerMultiplier() {
		return 0.7;
    }
	
	@Override
	public void sendDeath(Entity source) {
		final NPCCombatDefinition defs = getCombatDefinitions();

		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0)
					setNextAnimation(new Animation(defs.getDeathEmote()));
				else if (loop == 3) {
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
