package com.rs.game.player.content.dungeoneering.rooms.puzzles;

import com.rs.game.WorldTile;
import com.rs.game.npc.dungeonnering.DungeonNPC;
import com.rs.game.player.content.dungeoneering.DungeonManager;
import com.rs.game.player.content.dungeoneering.rooms.PuzzleRoom;

public class FollowTheLeader extends PuzzleRoom {

	private static final int[][] LEADER_TILES = { { 7, 11, 2 }, { 3, 7, 1 }, { 11, 7, 3 }, { 3, 4, 1 }, { 11, 4, 3 } };
	
	private final Leader[] leaders = new Leader[manager.getParty().getSize()];
	
	@Override
	public void openRoom() {
		for (int i = 0; i < leaders.length; i++)
			leaders[i] = new Leader(12960, manager.getRotatedTile(reference, LEADER_TILES[i][0], LEADER_TILES[i][1]), manager, 0.0, LEADER_TILES[i][2], null);
		
	}
	
	private static class Leader extends DungeonNPC {

		private static final long serialVersionUID = -5481570086922472546L;

		public Leader(int id, WorldTile tile, DungeonManager manager, double multiplier, int direction, FollowTheLeader puzzle) {
			super(id, tile, manager, multiplier);
			this.direction = direction;
			this.puzzle = puzzle;
		}
		
		@SuppressWarnings("unused")
		private final FollowTheLeader puzzle;
		private final int direction;

		@Override
		public int getDirection() {
			return direction;
		}
	}
	
}
