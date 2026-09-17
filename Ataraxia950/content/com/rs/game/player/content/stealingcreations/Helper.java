package com.rs.game.player.content.stealingcreations;

import com.rs.game.ForceMovement;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;

public class Helper {

	public static int getFaceDirection(WorldTile faceTile, Player player) {
		if (player.getX() < faceTile.getX())
			return ForceMovement.EAST;
		else if (player.getX() > faceTile.getX())
			return ForceMovement.WEST;
		else if (player.getY() < faceTile.getY())
			return ForceMovement.NORTH;
		else if (player.getY() > faceTile.getY())
			return ForceMovement.SOUTH;
		else
			return 0;
	}
	
}
