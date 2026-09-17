package com.rs.game.player.content.agility;

import com.rs.game.player.Player;

/**
 * An agility object that can be succeded.
 */
public interface Succeedable {

	/**
	 * Defines success behaviour of an agility object.
	 * @param player the player interacting with the agility object
	 * @param start <tt>true</tt> if the interaction flow
	 *              is from start to end, <tt>false</tt> if
	 *              from end to start
     */
	void succeed(final Player player, boolean start);

	default void succeed(final Player player) {
		succeed(player, true);
	}

}
