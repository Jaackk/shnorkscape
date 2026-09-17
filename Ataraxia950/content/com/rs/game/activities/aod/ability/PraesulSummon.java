package com.rs.game.activities.aod.ability;

import com.rs.game.ForceTalk;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.NewProjectile;
import com.rs.game.WorldTile;
import com.rs.game.activities.aod.AngelOfDeath;
import com.rs.game.activities.aod.npc.Amalgation;
import com.rs.game.activities.aod.npc.AoDNex;

/**
 * AoD Praesul summoning ability. Occurs at 210k health. Sends projectiles from
 * all four corners to near the center of the arena, after which four
 * amalgations are spawned. As they spawn, an explosion occurs which will deal
 * 400 unblockable damage to anyone standing near it. Amalgations must be
 * defeated before the timer on them reaches full, in order to break the
 * connection between the Praesul and AoD, which will result in the Praesul
 * having only 15k health as opposed to 30k as well as slightly reduced stats.
 * 
 * @author Kris | 30. sept 2017 : 16:50.35
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server
 *      profile</a>}
 */
public final class PraesulSummon extends AoDAbility {

	public PraesulSummon(final AoDNex nex, final AngelOfDeath instance) {
		super(nex, instance);
	}

	final WorldTile SW = instance.getWorldTile(2843, 1819);
	final WorldTile SE = instance.getWorldTile(2853, 1819);
	final WorldTile NW = instance.getWorldTile(2843, 1829);
	final WorldTile NE = instance.getWorldTile(2853, 1829);
	/**
	 * Amalgations start as orange and go blue. TODO: Halved damage during
	 * cannotMove
	 */
	private int ticks;

	@Override
	public void run() {
		switch (ticks++) {
		case 0:
			nex.resetCombat();
			nex.setCannotMove(true);
			nex.setNextForceTalk(new ForceTalk("Hahahahaha, you've done well to me this far, but it ends here."));
			break;
		case 5:
			nex.setNextForceTalk(new ForceTalk("Come forth my Praesul, aid me."));
			break;
		case 10:
			nex.setNextForceTalk(new ForceTalk("Show these intruders the true meaning of pain."));
			break;
		case 12:
			nex.setCannotMove(false);
			nex.setTarget(nex.getTargetedPlayer());
			instance.sendProjectile(new NewProjectile(instance.SOUTH_WEST, SW, 6523, 20, 10));
			instance.sendProjectile(new NewProjectile(instance.SOUTH_EAST, SE, 6525, 20, 10));
			instance.sendProjectile(new NewProjectile(instance.NORTH_WEST, NW, 3371, 20, 10));
			instance.sendProjectile(new NewProjectile(instance.NORTH_EAST, NE, 6524, 20, 10));
			break;
		case 16:
			instance.sendGraphics(ElementsInstantKill.EXPLOSION, SW);
			instance.sendGraphics(ElementsInstantKill.EXPLOSION, SE);
			instance.sendGraphics(ElementsInstantKill.EXPLOSION, NW);
			instance.sendGraphics(ElementsInstantKill.EXPLOSION, NE);
			instance.spawnNPC(new Amalgation(24007, SW, instance));
			instance.spawnNPC(new Amalgation(24008, SE, instance));
			instance.spawnNPC(new Amalgation(24006, NW, instance));
			instance.spawnNPC(new Amalgation(24005, NE, instance));
			instance.getPlayers().forEach(p -> {
				p.getPackets().sendPlayerMessage(1, 15263739, "<col=ff0000>Nex brings forth powerful amalgations of each element.", true);
				if (p.withinDistance(SW, 3) || p.withinDistance(SE, 3) || p.withinDistance(NE, 3) || p.withinDistance(NW, 3))
					p.applyHit(new Hit(null, 400, HitLook.REGULAR_DAMAGE));
			});
			break;
		}
	}

}
