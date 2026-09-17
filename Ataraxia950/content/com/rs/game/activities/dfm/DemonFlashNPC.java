package com.rs.game.activities.dfm;

import com.rs.cores.CoresManager;
import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;

import java.util.concurrent.TimeUnit;

public class DemonFlashNPC extends NPC {

	private static final long serialVersionUID = -2173254072748472960L;

	public DemonFlashNPC(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
	}

	private int getAliveImpsAmount() {
		int amount = 0;
		for (NPC imps : DemonFlashMobs.getDemonFlashMobs().getImpHeralds()) {
			if (!imps.isDead() && !imps.hasFinished())
				amount++;
		}
		return amount;
	}

	private int getAliveLessersAmount() {
		int amount = 0;
		for (NPC lessers : DemonFlashMobs.getDemonFlashMobs().getLesserDemons()) {
			if (!lessers.isDead() && !lessers.hasFinished())
				amount++;
		}
		return amount;
	}

	private int getAliveBlacksAmount() {
		int amount = 0;
		for (NPC blacks : DemonFlashMobs.getDemonFlashMobs().getBlackDemons()) {
			if (!blacks.isDead() && !blacks.hasFinished())
				amount++;
		}
		return amount;
	}

	private void transformBoss() {
		DemonFlashMobs.getDemonFlashMobs().getBoss().transformIntoNPC(16732);
		CoresManager.getServiceProvider().executeWithDelay(new Runnable() {

			@Override
			public void run() {
				DemonFlashMobs.getDemonFlashMobs().getBoss().setName(DemonFlashMobs.getDemonFlashMobs().getName());
			}
			
		}, 1, TimeUnit.SECONDS);
	}

	@Override
	public void sendDeath(Entity source) {

		if (this.getId() == 16726 && getAliveImpsAmount() == 0) {
			if (getAliveLessersAmount() == 0) {
				if (getAliveBlacksAmount() == 0)
					transformBoss();
				else {
					for (NPC blacks : DemonFlashMobs.getDemonFlashMobs().getBlackDemons())
						blacks.transformIntoNPC(16730);
				}
			} else {
				for (NPC lessers : DemonFlashMobs.getDemonFlashMobs().getLesserDemons())
					lessers.transformIntoNPC(16728);
			}
		} else if (this.getId() == 16728 && getAliveImpsAmount() == 0 && getAliveLessersAmount() == 0) {
			if (getAliveBlacksAmount() == 0)
				transformBoss();
			else {
				for (NPC blacks : DemonFlashMobs.getDemonFlashMobs().getBlackDemons())
					blacks.transformIntoNPC(16730);
			}
		} else if (this.getId() == 16730 && getAliveImpsAmount() == 0 && getAliveLessersAmount() == 0 && getAliveBlacksAmount() == 0)
			transformBoss();
		super.sendDeath(source);
	}

}
