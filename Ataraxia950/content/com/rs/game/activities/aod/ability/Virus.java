package com.rs.game.activities.aod.ability;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.activities.aod.AngelOfDeath;
import com.rs.game.activities.aod.npc.AoDNex;
import com.rs.game.player.Skills;
import com.rs.game.player.content.Pots;
import com.rs.utils.Utils;

/**
 * A special attack used by AoD during its last phase. Sends a poison attack to all the players.
 * The attack also drains players' skills as well as overload timers.
 * @author Kris | 30. sept 2017 : 21:46.51
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public final class Virus extends AoDAbility {

	public Virus(final AoDNex nex, final AngelOfDeath instance) {
		super(nex, instance);
	}

	private static final Animation ATTACK = new Animation(17414);
	private static final Graphics GFX = new Graphics(3375);
	private int ticks; 
	
	@Override
	public void run() {
		switch(ticks++) {
		case 0:
			nex.setNextAnimation(ATTACK);
			nex.setNextGraphics(GFX);
			nex.setPoisonous(45000);
			instance.getPlayers().forEach(p -> {
				p.getPackets().sendPlayerMessage(1, 15263739, "<col=ff0000>Nex infects you with a powerful virus!", true);
				Pots.resetOverLoadEffect(p);
				Pots.resetSupremeOverLoadEffect(p);
			});
			break;
		case 25:
			instance.getPlayers().forEach(p -> {
				p.getSkills().drainLevel(Skills.ATTACK, Utils.random(15));
				p.getSkills().drainLevel(Skills.STRENGTH, Utils.random(15));
				p.getSkills().drainLevel(Skills.MAGIC, Utils.random(15));
				p.getSkills().drainLevel(Skills.RANGE, Utils.random(15));
				p.getPackets().sendPlayerMessage(1, 15263739, "<col=ff0000>You feel the effects of the virus distort your vision!", true);
			});
			break;
		case 40:
			instance.getPlayers().forEach(p -> {
				p.getPackets().sendPlayerMessage(1, 15263739, "<col=ff0000>You feel the effects of the virus drain your prayer!", true);
				p.getPrayer().drainPrayer(500);
			});
			stop();
			break;
		case 45:
			instance.getPlayers().forEach(p -> p.getPackets().sendPlayerMessage(1, 15263739, "<col=ff0000>You feel the effects of the virus begin to dissipate!", true));
			stop();
			break;
		}
	}

}
