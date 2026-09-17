package com.rs.game.player.actions.slayer.elite;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;

/**
 * @author Kris | 3. okt 2018 : 15:01:48
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>
 */
public class Supercow extends EliteNPC {

	private static final long serialVersionUID = 7659891656773572115L;

	protected Supercow(final Player owner, final int id, final WorldTile tile) {
		super(owner, id, tile);
	}
	
	@Override
	protected String getVaryingName() {
		return "Supercow";
	}

}
