package com.rs.game.npc.camelwarrior;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/*
 * author Movee
 */

@SuppressWarnings("serial")
public class Mirage extends NPC {

	private CamelWarrior camel;
	private int targetIndex;
	public Entity camelTarget;
	
	public Mirage(CamelWarrior camel, int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, true);
		this.camel = camel;
		
		if(id != 22001) {
			setForceMultiAttacked(true);
			
			if(camel.savedTarget != null)
				getCombat().setTarget(camel.savedTarget);
		}
		
		targetIndex = -1;
	}
	
	public Mirage getCamel() {
		return camel;
	}

	public void setCamel(CamelWarrior camel) {
		this.camel = camel;
	}
	
	public Entity getCamelTarget() {
		return camelTarget;
	}

	public int getTargetIndex() {
		return targetIndex;
	}
	
	@Override
	public Hit handleOutgoingHit(Hit hit, Entity target) {
		if(id == 22003) {
			if(hit.getDamage() > 0) {
				camel.healMirages();
			}
		}
		return hit;
	}
	
	@Override
	public void sendDeath(Entity source) {
		if(id != 22001) {
			WorldTasksManager.schedule(new WorldTask() {
				int loop;
	
				@Override
				public void run() {
					if (loop == 0)
						setNextAnimation(new Animation(27788));
					else if (loop == 3) {
						reset();
						finish();
						stop();
					}
					loop++;
				}
			}, 0, 1);
			
			camel.removeMirage(this);
		} else {
			WorldTasksManager.schedule(new WorldTask() {
				int loop;
		
				@Override
				public void run() {
					if (loop == 0)
						setNextAnimation(new Animation(27810));
					else if (loop == 3) {
						reset();
						finish();
						stop();
					}
					loop++;
				}
			}, 0, 1);
				
		}
		
		
	}
	
	@Override
	public void handleIngoingHit(final Hit hit) {
		super.handleIngoingHit(hit);
	}
	
	
	@Override
	public void processNPC() {
		if(this != null || camel != null) {	
			Entity target = getCombat().getTarget();
			
			if(getTargetIndex() == -1) {
				
				
				if(target != null)
					targetIndex = target.getIndex();
			}
			
			if(target != null) {
				if(target.getIndex() != camel.getTargetIndex()) {
					targetIndex = target.getIndex();
				}	
			}
			
			if(getId() != 22001) {
				if(getCombat().getTarget() == null) {
					finish();
					reset();
					camel.setNextWorldTile(new WorldTile(camel.getX(), camel.getY(), 0));
					camel.resetMirages();		
					camel.resetCamel();
					//camel.setRespawnTask();
				}
			}
		}

		super.processNPC();
	}
	
}
