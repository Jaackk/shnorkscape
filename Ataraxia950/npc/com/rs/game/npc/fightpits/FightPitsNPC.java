package com.rs.game.npc.fightpits;

import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.WorldTile;
import com.rs.game.activites.FightPits;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class FightPitsNPC extends NPC {

	public FightPitsNPC(int id, WorldTile tile) {
		super(id, tile, -1, true, true);
		setForceMultiArea(true);
		setNoDistanceCheck(true);
	}

	@Override
	public ArrayList<Entity> getPossibleTargets() {
		ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
		for (Player player : FightPits.arena)
			possibleTarget.add(player);
		return possibleTarget;
	}

	@Override
	public void sendDeath(Entity source) {
		setNextGraphics(new Graphics(2924 + getSize()));
		super.sendDeath(source);
	}

}
