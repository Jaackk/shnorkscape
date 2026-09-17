package com.rs.game.player.content.packs.portable;

import com.rs.game.WorldObject;
import com.rs.game.player.Player;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Oct 9, 2018.
 */
public interface Portable {

	void handleObjectClick1(Player player, WorldObject object);
	
	void handleObjectClick2(Player player, WorldObject object);
	
	void handleObjectClick3(Player player, WorldObject object);
	
	void handleObjectClick4(Player player, WorldObject object);

}
