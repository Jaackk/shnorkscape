package com.rs.game.npc.godwars.armadyl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activites.GodWarsBosses;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.player.Player;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class KreeArra extends NPC {

	public KreeArra(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned, boolean hardMode) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setCapDamage(1000);
		setLureDelay(1000);
		setForceTargetDistance(64);
		setForceFollowClose(false);
		setIntelligentRouteFinder(true);
		this.hardMode = hardMode;
		whirlwinds = new NPC[10];
	}
	
	@Override
	public double getMeleePrayerMultiplier() {
		return hardMode ? 0.125 : 0;
	}
	
	@Override
	public double getRangePrayerMultiplier() {
		return hardMode ? 0.125 : 0;
	}
	
	@Override
	public double getMagePrayerMultiplier() {
		return hardMode ? 0.125 : 0;
	}
	
	private final NPC[] whirlwinds;
	private boolean allSpawned;
	private int attacks;
	
	public int getAttacks() {
		return attacks;
	}
	
	public void setAttacks(int amount) {
		this.attacks = amount;
	}
	
	public NPC[] getWhirlwinds() {
		return whirlwinds;
	}
	
	public void spawnWhirlwind() {
		if (allSpawned)
			return;
		boolean spawned = false;
		for (int i = 0; i < 10; i++) {
			if (whirlwinds[i] == null) {
				spawned = true;
				whirlwinds[i] = new WhirlWinds(Utils.random(2) == 1 ? 17097 : 17096, new WorldTile(this, 1), -1, true, true, this);
				whirlwinds[i].setNextAnimation(new Animation(15531));
				whirlwinds[i].lock(5000);
				break;
			}
		}
		if (!spawned)
			allSpawned = true;
	}
	
	private final boolean hardMode;
	
	public boolean isHardMode() {
		return hardMode;
	}

	@Override
	public ArrayList<Entity> getPossibleTargets() {
		ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
		for (int regionId : getMapRegionsIds()) {
			List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
			if (playerIndexes != null) {
				for (int npcIndex : playerIndexes) {
					Player player = World.getPlayers().get(npcIndex);
					if (player == null || player.isDead() || player.hasFinished() || !player.isRunning()
							|| !player.withinDistance(this, 64)
							|| ((!isAtMultiArea() || !player.isAtMultiArea()) && player.getAttackedBy() != this
									&& player.getAttackedByDelay() > System.currentTimeMillis())
							|| !clipedProjectile(player, false))
						continue;
					possibleTarget.add(player);
				}
			}
		}
		return possibleTarget;
	}

	@Override
	public void sendDeath(Entity source) {
		allSpawned = false;
		for (int i = 0; i < 10; i++) {
			if (whirlwinds[i] == null)
				continue;
			whirlwinds[i].finish();
			whirlwinds[i] = null;
		}
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
					setNextGraphics(new Graphics(3350));
				} else if (loop >= defs.getDeathDelay()) {
					if (source instanceof Player) {
						Player plr = (Player) source;
						if(plr.isGroupIronman()) {
							plr.gimTracker.incrementBpGained(2);
						}
						plr.getAchievements().updateProgress(1, AchievementList.KILL_250_GWD1_BOSSES);
						plr.getActivityTimersManager().finishBossTimer(KreeArra.this);
						ContractHandler.updateContract(plr, KreeArra.this);
					}
					drop();
					reset();
					setLocation(getRespawnTile());
					finish();
					setRespawnTask();
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}

	@Override
	public void setRespawnTask() {
		final NPC npc = this;
		// Particulary for Dominion Tower.
		if (!GodWarsBosses.isAtGodwars(npc))
			return;
		if (!hasFinished()) {
			reset();
			setLocation(getRespawnTile());
			finish();
		}
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				setFinished(false);
				World.addNPC(npc);
				npc.setLastRegionId(0);
				World.updateEntityRegion(npc);
				loadMapRegions();
				checkMultiArea();
				GodWarsBosses.respawnArmadylMinions();
			}
		}, getCombatDefinitions().getRespawnDelay());
	}
}