package com.rs.game.player.content.fistofguthix;

import com.rs.game.player.Player;

/**
 * The extra-utilities/components of the {@link FistOfGuthix}.
 * 
 * @author _Waterfiend <skype:alco-wahidi>
 *
 * Created in May 1, 2017 at 11:58:05 AM.
 */
class FOGUtil {

	/**
	 * Calculates the distance.
	 * 
	 * @param player
	 *            The player's distance.
	 * @return the computedDistance
	 */
	static int computeDistance(Player player) {
		final double y = Math.pow((Math.abs(FistOfGuthix.FOG_CENTER.getY() - player.getY())), 2);
		final double x = Math.pow((Math.abs(FistOfGuthix.FOG_CENTER.getX() - player.getX())), 2);
		final double distance = Math.sqrt(y + x);
		return (int) (distance + 0.5);
	}
}
