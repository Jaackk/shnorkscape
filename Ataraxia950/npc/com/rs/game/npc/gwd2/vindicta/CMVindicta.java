package com.rs.game.npc.gwd2.vindicta;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.activities.instances.VindictaInstance;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class CMVindicta extends Vindicta {

	private static final long serialVersionUID = -2739139160334939138L;

	public CMVindicta(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned, VindictaInstance instance) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned, instance);
	}
	
	@Override
	public void sendDeath(final Entity source) {
		if (getId() == 22462) {
			setCantInteract(true);
			resetWalkSteps();
			resetCombat();
			setPhase(0);
			setGorvekPhase(Utils.random(5, 10));
			setNextAnimationForce(new Animation(28524));
			reset();
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					heal(30000);
					setNextNPCTransformation(22463);
					setCantInteract(false);
				}
			},2);
			return;
		} else {
			super.sendDeath(source);
		}
	}

}
