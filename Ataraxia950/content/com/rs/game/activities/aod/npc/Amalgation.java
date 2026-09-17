package com.rs.game.activities.aod.npc;

import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.activities.aod.AngelOfDeath;
import com.rs.game.hitbar.impl.HitBarTimer;
import com.rs.game.npc.NPC;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/**
 * An Amalgation NPC class. Only lasts 15 seconds, if it isn't defeated in time, it will die
 * and summon praesul with full health and bonuses. If it is defeated, the praesul will be weakened.
 * @author Kris | 30. sept 2017 : 17:11.17
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public final class Amalgation extends NPC {

	private static final long serialVersionUID = 7938993220565172226L;

	public Amalgation(final int id, final WorldTile tile, final AngelOfDeath instance) {
		super(id, tile, -1, true, true);
		this.instance = instance;
		getNextHitBars().add(new HitBarTimer(0));
		startSequence();
		setForceMultiArea(true);
		setCannotMove(true);
	}
	
	private final AngelOfDeath instance;
	
	@Override
	public void processNPC() {
		super.processNPC();
		addHitBars();
	}
	
	@Override
	public void sendDeath(final Entity source) {
		setCantInteract(true);
		WorldTasksManager.schedule(new WorldTask() {
			private int ticks;
			@Override
			public void run() {
				if (ticks++ == 0)
					finish();
				else {
					instance.spawnNPC(new PraesulMinion(getId() + 5, getPraesulLocation(), instance, false));
					stop();
				}
			}
		}, 1, 2);
	}

	private final void startSequence() {
		WorldTasksManager.schedule(new WorldTask() {
			private int ticks;
			@Override
			public void run() {
				if (isCantInteract()) {
					stop();
					return;
				}
				ticks++;
				if (ticks < 52)
					getNextHitBars().add(new HitBarTimer(ticks * 2));
				else if (ticks == 53) {
					finish();
					instance.spawnNPC(new PraesulMinion(getId() + 5, getPraesulLocation(), instance, true));
					stop();
					return;
				}
			}
		}, 0, 0);
	}
	
	private final WorldTile getPraesulLocation() {
		switch(getId()) {
		case 24005:
			return instance.getWorldTile(2858, 1834);
		case 24006:
			return instance.getWorldTile(2838, 1834);
		case 24007:
			return instance.getWorldTile(2838, 1814);
			default:
				return instance.getWorldTile(2858, 1814);
		}
	}

	@Override
	public boolean isIntelligentRouteFinder() {
		return true;
	}

	@Override
	public int getCapDamage() {
		return 1000;
	}

}
