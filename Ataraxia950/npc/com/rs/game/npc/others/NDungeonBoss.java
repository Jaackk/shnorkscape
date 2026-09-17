package com.rs.game.npc.others;

import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.content.dungeoneering.Dungeoneering.Dungeon;

@SuppressWarnings("serial")
public class NDungeonBoss extends NPC {

	private final Dungeon dungeon;

	public NDungeonBoss(int id, WorldTile tile, Dungeon dungeon) {
		super(id, tile, dungeon.getDungeonBossRoomHash(), false, true);
		this.dungeon = dungeon;
		setForceMultiArea(true);
		setForceAgressive(true);
	}

	@Override
	public void sendDeath(Entity source) {
		super.sendDeath(source);
		dungeon.openStairs();
	}

	public Dungeon getDungeon() {
		return dungeon;
	}
}
