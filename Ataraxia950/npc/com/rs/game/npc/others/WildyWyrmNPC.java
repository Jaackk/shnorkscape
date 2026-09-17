package com.rs.game.npc.others;

import com.rs.Settings;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.Wilderness;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.npcs.NPCDropsDataParser;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;

import java.util.ArrayList;
import java.util.List;

public class WildyWyrmNPC extends NPC {

	private static final long serialVersionUID = -7862643614912376747L;

	public WildyWyrmNPC(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setCapDamage(1500);
	}

	@Override
	public void drop() {
		NPCDrop[] drops = NPCDropsDataParser.getDrops(getId());
		if (drops == null)
			return;
		NPCDrop[] possibleDrops = new NPCDrop[drops.length];
		if (getReceivedDamageSources().isEmpty())
			return;
		getReceivedDamage().forEach((contributor, v) -> {
			if (contributor != null && contributor instanceof Player) {
				Player killer = (Player) contributor;
				if (v > 10000) {
					increaseKillStatistics(killer, this.getName());
					int possibleDropsCount = 0;
					for (NPCDrop drop : drops) {
						if (drop.getRate() == 100)
							sendDrop(killer, drop, false);
						else {
							double rate = drop.getRate();
							double random = Utils.getRandomDouble(100);
							if (rate < 30)
								rate *= Settings.getDropQuantityRate(killer);
							if (random <= rate && random != 100 && random != 0)
								possibleDrops[possibleDropsCount++] = drop;
						}
					}
					if (possibleDropsCount > 0)
						sendDrop(killer, possibleDrops[Utils.getRandom(possibleDropsCount - 1)], false);
				}
			}
		});
	}
	
	@Override
	public ArrayList<Entity> getPossibleTargets() {
		ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
		for (int regionId : getMapRegionsIds()) {
				List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
				if (playerIndexes != null) {
					for (int playerIndex : playerIndexes) {
						Player player = World.getPlayers().get(playerIndex);
						if (player == null || player.isDead() || player.hasFinished() 
								|| !player.isRunning() || player.getAppearence().isHidden() 
								|| (!isForceMultiAttacked() && (!isAtMultiArea() 
								|| !player.isAtMultiArea()) && (player.getAttackedBy() != this && (player.getAttackedByDelay() > Utils.currentTimeMillis() 
								|| player.getFindTargetDelay() > Utils.currentTimeMillis()))) 
								|| !clipedProjectile(player, false) || (!isForceAgressive() && !Wilderness.isAtWild(this)))
							continue;
						possibleTarget.add(player);
					}
			}
		}
		return possibleTarget;
	}
	
	@Override
	public void setNPC(int id) {
		this.id = id;
	}
	
	@Override
	public double getMagePrayerMultiplier() {
		return 0.6;
	}
	
	@Override
	public double getMeleePrayerMultiplier() {
		return 0.6;
	}
	
	@Override
	public double getRangePrayerMultiplier() {
		return 0.6;
	}

}
