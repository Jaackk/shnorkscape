package com.rs.game.player.actions.slayer.elite;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;

/**
 * @author Kris | 3. okt 2018 : 11:40:37
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>
 */
public final class BasicEliteNPC extends EliteNPC {

	/**
	 * 21502 - abyssal demon.
	 */
	
	private static final long serialVersionUID = -4513380912079848503L;

	protected BasicEliteNPC(final Player owner, final int id, final WorldTile tile) {
		super(owner, id, tile);
	}

}
