package com.rs.game.npc.dragons;

import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;

@SuppressWarnings("serial")
public class KingBlackDragon extends NPC {

	public KingBlackDragon(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea,
			boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setLureDelay(0);
		setCapDamage(950);
	}

	public static boolean atKBD(WorldTile tile) {
		return (tile.getX() >= 2250 && tile.getX() <= 2292) && (tile.getY() >= 4675 && tile.getY() <= 4710);
	}
	
	@Override
	public void sendDeath(Entity source) {
		super.sendDeath(source);
		if (source instanceof Player) {
			Player plr = (Player) source;
			if (plr.isGroupIronman()) {
				plr.gimTracker.incrementBpGained(2);
			}
			plr.getActivityTimersManager().finishBossTimer(KingBlackDragon.this);
		}
	}
}