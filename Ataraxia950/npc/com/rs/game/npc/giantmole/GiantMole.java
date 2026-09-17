package com.rs.game.npc.giantmole;

import com.rs.Settings;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public final class GiantMole extends NPC {

	private static final long serialVersionUID = 7671275776566458039L;

	public GiantMole(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned, GiantMoleInstance instance) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		this.instance = instance;
		setForceTargetDistance(7);
		setLureDelay(1000);
		setForceAgressive(true);
		setForceMultiArea(true);
		setNoDistanceCheck(true);
		setIntelligentRouteFinder(true);
		stages = new boolean[5];
		stage = -1;
	}

	private final boolean[] stages;
	private boolean hardMode;
	private NPC[] moles;
	private int stage;
	private final GiantMoleInstance instance;

	public GiantMoleInstance getInstance() {
		return instance;
	}

	/**
	 * Animations: 22609 -> Coming from the ground 22610 -> "Stomping" on the player (Possibly melee attack) 22614 -> Digging into the ground 22615 ->
	 * Jumping into the ground 22618 -> Blocking 22619 -> Death animation, 2 ticks. 22620 -> Death animation, 1 tick. 22622 -> One paw stomp 22625 ->
	 * Angry scream 22627 -> Quick one-paw swipe (Possible melee attack)
	 */

	public final int getStage() {
		return stage;
	}

	public final NPC[] getMoles() {
		return moles;
	}

	public final boolean isHardMode() {
		return hardMode;
	}

	private final void switchStage() {
		List<Integer> possibleStages = new ArrayList<Integer>();
		for (int i = 0; i < stages.length; i++)
			if (!stages[i])
				possibleStages.add(i);
		stage = possibleStages.get(Utils.random(possibleStages.size()));
		stages[stage] = true;
	}

	private final int getStagesCompleted() {
		int completed = 0;
		for (boolean s : stages)
			if (s)
				completed++;
		return completed;
	}
	
	@Override
	public int getCapDamage() {
		return 750;
	}
	
	@Override
	public void handleIngoingHit(Hit hit) {
		if (Settings.USE_DAMAGE_CAP) {
            if (getCapDamage() != -1 && hit.getDamage() > getCapDamage()) {
                hit.setDamage(getCapDamage());
            }
        }
		if (hit.getSource() instanceof Player) {
			final Player player = (Player) hit.getSource();
			final int weaponId = player.getEquipment().getWeaponId();
            if (weaponId == 25202) {
            	player.sendMessage("Your dart is no match for the mole!");
                return;
            }
		}
		super.handleIngoingHit(hit);
	}

	@Override
	public void processNPC() {

		WorldTile[] locations = new WorldTile[] { instance.getWorldTile(17, 58), instance.getWorldTile(19, 19), instance.getWorldTile(56, 18), instance.getWorldTile(62, 57),
//				instance.getWorldTile(38,  51)//Spawn location
		};
		super.processNPC();
		if (!isDead() && !isCantInteract() && getHitpoints() <= 5000 - (getStagesCompleted() * 1000)) {
			setCantInteract(true);
			switchStage();
			getCombat().reset();
			int r = Utils.random(locations.length);
			setNextFaceWorldTile(locations[r]);
			WorldTasksManager.schedule(new WorldTask() {
				int ticks;

				@Override
				public void run() {
					if (ticks == 0) {
						setNextAnimation(new Animation(22614));
						setNextGraphics(new Graphics(4565));
					} else if (ticks == 2) {
						setNextWorldTile(locations[r]);
					} else if (ticks == 4) {
						setNextAnimation(new Animation(22609));
						setNextGraphics(new Graphics(4564));
					} else if (ticks == 5) {
						setCantInteract(false);
						stop();
					}
					ticks++;
				}
			}, 0, 1);
		}
	}
	
	@Override
	public void sendDeath(Entity source) {
		super.sendDeath(source);
		if (source instanceof Player) {
			Player plr = (Player) source;
			if (plr.isGroupIronman()) {
				plr.gimTracker.incrementBpGained(1);
			}
			plr.getActivityTimersManager().finishBossTimer(GiantMole.this);
		}
	}
}
