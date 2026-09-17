package com.rs.game.player.actions.slayer.elite;

import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

/**
 * @author Kris | 3. okt 2018 : 14:56:01
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>
 */
public class EliteAnkou extends EliteNPC {

	private static final long serialVersionUID = -4244149987241503476L;

	protected EliteAnkou(final Player owner, final int id, final WorldTile tile) {
		super(owner, id, tile);
		setIntelligentRouteFinder(true);
	}

	private boolean healing;

	@Override
	public void applyHit(final Hit hit) {
		super.applyHit(hit);
		if (hit.getSource() == null) {
			return;
		}
		healing = hit.getDamage() <= 0;
	}

	@Override
	public void processNPC() {
		super.processNPC();
		if (healing) {
			if (getHitpoints() >= getMaxHitpoints()) {
				healing = false;
				return;
			}
			applyHit(new Hit(null, Utils.random(30, 50), HitLook.HEALED_DAMAGE));
		}
	}
}
