package com.rs.game.activites.pest;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.pest.Splatter;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

@SuppressWarnings("serial")
public class PestControlObject extends WorldObject {

	private int hitsAmount;
	private boolean beingRepaired;

	public PestControlObject(WorldObject object) {
		super(object);
	}

	public void destroyObject(NPC npc) {
		if (!(npc instanceof Splatter)) {
			NPCCombatDefinition defs = npc.getCombatDefinitions();
			npc.faceObject(this);
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
		}
		hitsAmount++;
		if (hitsAmount >= 2) {
			hitsAmount = 0;
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					boolean gate = getId() >= 91332 && getId() <= 91339;
					int maxId = (getId() >= 91332 && getId() <= 91335) ? 91335
							: (getId() >= 91336 && getId() <= 91339) ? 91339
									: (getId() == 14224 || getId() == 14227) ? 14227
											: (getId() == 14225 || getId() == 14228) ? 14228 : 14229;
					int increament = (gate ? 1 : 3);
					setId(getId() + increament > maxId ? maxId : getId() + increament);
					World.spawnObject(PestControlObject.this);
					World.unclipTile(PestControlObject.this);
				}
			});
			return;
		}
		return;
	}

	public boolean isBeingRepaired() {
		return beingRepaired;
	}

	public void setBeingRepaired(boolean beingRepaired) {
		this.beingRepaired = beingRepaired;
	}

}
