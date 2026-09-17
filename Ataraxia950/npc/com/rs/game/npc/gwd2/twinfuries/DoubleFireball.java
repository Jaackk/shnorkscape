package com.rs.game.npc.gwd2.twinfuries;

import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class DoubleFireball extends RedFireball {
	
	public DoubleFireball(NPC npc, Entity entity) {
		super(npc, entity);
	}
	
	private RedFireball redBall;
	private BlueFireball blueBall;

	@Override
	public void effect() {
		redBall = new RedFireball(npc, entity);
		blueBall = new BlueFireball(npc, entity);
		WorldTasksManager.schedule(new WorldTask() {
			private int ticks;
			@Override
			public void run() {
				if (ticks++ == 0) {
					redBall.effect();
					blueBall.effect();
				}
				if (blueBall.isLockedOnTarget() && !blueBall.isCancelled())
					redBall.setForceTarget();
				if (blueBall.isCancelled())
					stop();
			}
		}, 0, 0);
	}

}
