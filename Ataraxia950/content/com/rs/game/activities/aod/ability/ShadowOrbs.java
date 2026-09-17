package com.rs.game.activities.aod.ability;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.WorldTile;
import com.rs.game.activities.aod.AngelOfDeath;
import com.rs.game.activities.aod.ShadowOrb;
import com.rs.game.activities.aod.npc.AoDNex;
import com.rs.game.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles the shadow orbs special ability. Nex sends three shadow orbs on all
 * players. Orbs will be cleared once they've all been planted. Orbs can't be
 * planted at a spot where another already exists.
 * 
 * @author Kris | 28. sept 2017 : 4:50.01
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server
 *      profile</a>}
 */
public final class ShadowOrbs extends AoDAbility {

	public ShadowOrbs(final AoDNex nex, final AngelOfDeath instance) {
		super(nex, instance);
	}

	private static final Animation EMPOWER = new Animation(30140);
	private static final Graphics HIT = new Graphics(6533);
	private int ticks;
	private final List<WorldTile> tiles = new ArrayList<WorldTile>();
	private final Map<Player, Integer> orbs = new HashMap<Player, Integer>();
	private boolean finish;

	@Override
	public void run() {
		if (ticks == 0) {
			nex.resetCombat();
			nex.setCannotMove(true);
			nex.sendMessage("Let the shadow engulf you. Give in to it.");
			nex.setNextAnimation(EMPOWER);
		} else if (ticks >= 6) {
			if (ticks == 6) {
				nex.setCannotMove(false);
				nex.setTarget(nex.getTargetedPlayer());
			}
			if (ticks % 2 == 0) {
				finish = true;
				instance.getPlayers().forEach(p -> {
					if (orbs.get(p) != null && orbs.get(p) == 3 || p.isDead() || p.hasFinished() || !instance.getPlayers().contains(p))
						return;
					for (ShadowOrb orb : instance.getShadowOrbs()) {
						if (orb.getTileHash() == p.getTileHash()) {
							finish = false;
							return;
						}
					}
					final WorldTile t = new WorldTile(p);
					orbs.put(p, orbs.get(p) != null ? (orbs.get(p) + 1) : 1);
					instance.sendGraphics(HIT, t);
					tiles.add(t);
					finish = false;
				});
			} else {
				tiles.forEach(t -> {
					instance.getShadowOrbs().forEach(orb -> {
						if (orb.getX() == t.getX() || orb.getY() == t.getY())
							orb.incrementAmount();
					});
					instance.getShadowOrbs().add(new ShadowOrb(t, instance));
				});
				tiles.clear();
				if (finish) {
					stop();
					return;
				}
			}
		}
		ticks++;
	}

}
