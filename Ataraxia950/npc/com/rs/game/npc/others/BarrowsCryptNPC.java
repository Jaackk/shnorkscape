package com.rs.game.npc.others;

import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.content.barrows.Barrows;

public class BarrowsCryptNPC extends NPC {

	private static final long serialVersionUID = 727196449892285674L;

	public BarrowsCryptNPC(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setForceAgressive(true);
	}
	
	@Override
	public void sendDeath(final Entity source) {
		super.sendDeath(source);
		if (source instanceof Player) {
			Player src = (Player) source;
			src.setBarrowsKillCount(src.getBarrowsKillCount() + 1);
			Barrows.updateInterface(src, true);
		}
	}

}
