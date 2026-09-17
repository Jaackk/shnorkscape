package com.rs.game.player.content.agility;

import com.rs.game.player.Player;

/**
 * An agility object that can be failed.
 */
public interface Failable {

	/**
	 * Defines failure behaviour of an agility object.
	 * @param player the player interacting with the agility object
	 * @param start <tt>true</tt> if the interaction flow
	 *              is from start to end, <tt>false</tt> if
	 *              from end to start
	 */
	void fail(final Player player, boolean start);

	default void fail(Player player) {
		fail(player, true);
	}

}
