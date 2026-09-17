package com.rs.game.npc.others;

import com.rs.game.Graphics;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("serial")
public class DonatorRunesphere extends NPC {
	
	@Getter @Setter private boolean active;
	private static final Graphics GLOW = new Graphics(3939);

	public DonatorRunesphere(int id, WorldTile tile) {
		super(id, tile, -1, true, true);
		setCannotMove(true);
	}
	
	@Override
	public void processNPC() {
		if(active)
			setNextGraphics(GLOW);
	}

}
