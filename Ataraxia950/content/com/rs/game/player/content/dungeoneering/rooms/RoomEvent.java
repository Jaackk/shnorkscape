package com.rs.game.player.content.dungeoneering.rooms;

import com.rs.game.player.content.dungeoneering.DungeonManager;
import com.rs.game.player.content.dungeoneering.RoomReference;

public interface RoomEvent {

	void openRoom(DungeonManager dungeon, RoomReference reference);
}
