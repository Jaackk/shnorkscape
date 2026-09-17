package com.rs.game.npc.barrelchest;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.BarrelchestController;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

@SuppressWarnings("serial")
public class BarrelChest extends BarrelNPC {

	private final BarrelchestController controler;

	public BarrelChest(int id, WorldTile tile, BarrelchestController controler) {
		super(id, tile);
		this.controler = controler;
	}

	@Override
	public void processNPC() {
		super.processNPC();
	}

	@Override
	public void sendDeath(Entity source) {
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
					if(source instanceof Player) {
						Player plr = (Player) source;
						if (plr.isGroupIronman()) {
							plr.awardBcPoint += 0.5;
							if(plr.awardBcPoint >= 1.0) {
								plr.gimTracker.incrementBpGained(1);
								plr.awardBcPoint = 0.0;
							}
						}
					}
					reset();
					finish();
					controler.win();
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}

}