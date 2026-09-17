package com.rs.game.player.actions.divination;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * Handles Wisp NPC.
 *
 * @author Noel.
 */
public class Wisp extends NPC {

	private static final long serialVersionUID = -301831560348706545L;
	private int life;
	private final WispInfo info;
	private boolean isSpring;
	private boolean usedUp;

	public Wisp(int id, WorldTile tile) {
		super(id, tile, -1, true);
		this.info = WispInfo.forNpcId(id);
		isSpring = false;
		setRandomWalk(2);
	}

	@Override
	public void spawn() {
		super.spawn();
		setUsedUp(false);
		setSpring(false);
		setRandomWalk(1);
	}

	@Override
	public void sendDeath(Entity source) {
		resetWalkSteps();
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0)
					setNextAnimation(new Animation(21203)); // 21210 =
				// enriched
				// death
				else if (loop >= 1) {
					setUsedUp(true);
					setNextNPCTransformation(info.getNpcId());
					setRespawnTask();
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}

	@Override
	public void processNPC() {
		super.processNPC();
		if (isSpring()) {
			if (life > 0)
				life--;
			if (life <= 0 && !isDead()) {
				setHitpoints(0);
				sendDeath(this);
			}
		}
	}

	public void harvest(Player player) {
		if (!DivinationHarvest.checkAll(player, info))
			return;
		if (!isSpring()) {
			setNextNPCTransformation(info.getSpringNpcId());
			life = Utils.random(player.getPerkManager().hasPerkActive(DonationPerk.DEDICATED_DIVINATION) ? 120 : 90, 140);
			setLocked(true);
			setSpring(true);
		}
		player.getActionManager().setAction(new DivinationHarvest(player, new Object[] { this, info }));
	}

	public boolean isSpring() {
		return this.isSpring;
	}

	public void setSpring(boolean isSpring) {
		this.isSpring = isSpring;
	}

	public int getLife() {
		return life;
	}

	public void setLife(int life) {
		this.life = life;
	}

	public boolean isUsedUp() {
		return usedUp;
	}

	public void setUsedUp(boolean usedUp) {
		this.usedUp = usedUp;
	}
}
