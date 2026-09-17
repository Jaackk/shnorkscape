package com.rs.game.activities.aod.ability;

import com.rs.game.ForceTalk;
import com.rs.game.activities.aod.AngelOfDeath;
import com.rs.game.activities.aod.npc.AoDNex;

/**
 * The escalation ability - occurs after AoD has lost a certain amount of time.
 * Switches phase.
 * 
 * @author Kris | 30. sept 2017 : 16:44.49
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server
 *      profile</a>}
 */
public final class Escalation extends AoDAbility {

	public Escalation(final AoDNex nex, final AngelOfDeath instance) {
		super(nex, instance);
	}

	private int ticks;

	@Override
	public void run() {
		switch (ticks++) {
		case 0:
			nex.resetCombat();
			nex.setCannotMove(true);
			nex.setNextForceTalk(new ForceTalk("Understand this mortals, you know nothing of the power you meddle with."));
			break;
		case 5:
			nex.setNextForceTalk(new ForceTalk("Feel the uncontrollable nature of it overcome you."));
			break;
		case 10:
			nex.setNextForceTalk(new ForceTalk("These are the very elements of my creation.. This is everything... EVERYTHING!"));
			break;
		case 12:
			nex.setCannotMove(false);
			nex.setTarget(nex.getTargetedPlayer());
			stop();
			break;
		}
	}

}
