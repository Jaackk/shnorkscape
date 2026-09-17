package com.rs.game.player.actions.slayer.elite;

import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.CombatScriptsHandler;
import com.rs.utils.Utils;

/**
 * @author Kris | 3. okt 2018 : 15:14:04
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>
 */
public class EliteDustDevilCombat extends CombatScript {

	@Override
	public int attack(final NPC npc, final Entity target) {
		if (Utils.random(20) == 0) {
			target.addFreezeDelay(3000, true);
		}
		return CombatScriptsHandler.DEFAULT_SCRIPT.attack(npc, target);
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { 21503 };
	}

}
