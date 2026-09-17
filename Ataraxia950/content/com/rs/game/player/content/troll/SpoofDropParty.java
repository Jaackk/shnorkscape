package com.rs.game.player.content.troll;

import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

public class SpoofDropParty extends TrollEvent {

	public static final int[] NPCS = {
		19274, 19275, 19276, 19277, 19278, 19279, 17205
	};
	
	public SpoofDropParty(Player owner) {
		super(owner);
	}

	@Override
	public void effect(Player player) {
		for (int i=0; i < 3; i++) 
			World.spawnNPC(new NPC(NPCS[Utils.random(NPCS.length)], player, -1, false));
	}
	
}
