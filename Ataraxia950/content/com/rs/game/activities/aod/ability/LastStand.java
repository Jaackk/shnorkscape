package com.rs.game.activities.aod.ability;

import com.rs.game.ForceTalk;
import com.rs.game.activities.aod.AngelOfDeath;
import com.rs.game.activities.aod.npc.AoDNex;
import com.rs.game.activities.aod.npc.Crystal;

/**
 * The last stand ability. AoD switches phase and spawns four crystals at all
 * corners, after which Nex becomes invulnerable until the crystals are
 * destroyed. Nex herself still keeps attacking players. Crystals can only be
 * attacked and killed in the same order the Praesul died.
 * 
 * @author Kris | 30. sept 2017 : 16:46.52
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server
 *      profile</a>}
 */
public final class LastStand extends AoDAbility {

	public LastStand(final AoDNex nex, final AngelOfDeath instance) {
		super(nex, instance);
	}

	@Override
	public void run() {
		nex.setNextForceTalk(new ForceTalk("Noooo... You can not defeat me..."));
		nex.setVulnerability(false);
		instance.getPlayers().forEach(p -> {
			if ((p.isUnderCombat() && p.getAttackedBy() instanceof AoDNex)
					|| (p.getTemporaryAttributtes().get("last_target") != null && p.getTemporaryAttributtes().get("last_target") instanceof AoDNex))
				p.getActionManager().forceStop();
		});
		nex.setHeal(true);
		instance.addCrystal(new Crystal(24016, instance.getWorldTile(2827, 1841), instance));
		instance.addCrystal(new Crystal(24017, instance.getWorldTile(2865, 1841), instance));
		instance.addCrystal(new Crystal(24018, instance.getWorldTile(2827, 1803), instance));
		instance.addCrystal(new Crystal(24019, instance.getWorldTile(2865, 1803), instance));
		stop();
	}

}
