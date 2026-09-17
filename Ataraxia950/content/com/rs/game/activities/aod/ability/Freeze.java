package com.rs.game.activities.aod.ability;

import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.activities.aod.AngelOfDeath;
import com.rs.game.activities.aod.npc.AoDNex;
import com.rs.game.activities.aod.npc.Crystal;
import com.rs.game.player.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AoD special ability to freeze all players near the ice crystal. Gradually
 * freezes the players, until the freeze is complete, after which they're
 * stunned for five seconds and get a bleed damage of 200 for five stacks. Any
 * players caught without protect from magic when this happens will receive the
 * attack instantaneously.
 * 
 * @author Kris | 30. sept 2017 : 22:36.19
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server
 *      profile</a>}
 */
public final class Freeze extends AoDAbility {

	public Freeze(final AoDNex nex, final AngelOfDeath instance) {
		super(nex, instance);
	}

	private int ticks;
	private boolean finish;
	private final Crystal crystal = instance.getCrystal(24017);
	private final Map<Player, Integer> frozenPlayers = new ConcurrentHashMap<Player, Integer>();

	@Override
	public void run() {
		if (ticks == 0) {
			instance.getPlayers().forEach(p -> {
				if (p.withinDistance(instance.NORTH_EAST, 12)) {
					frozenPlayers.put(p, p.getPrayer().isMageProtecting() ? 1 : 10);
					p.getInterfaceManager().sendOverlay(1688, true);
					p.getPackets().sendIComponentTransparency(1688, (byte) 127);
					p.getPackets().sendPlayerMessage(1, 15263739, "<col=ff0000>You begin to feel the effects of the ice pillar.", true);
				}
			});
		} else {
			if (finish) {
				frozenPlayers.forEach((k, v) -> {
					if (v == 0) {
						frozenPlayers.remove(k);
						k.getInterfaceManager().closeOverlay(true);
					} else
						frozenPlayers.put(k, v - 1);
					k.getPackets().sendIComponentTransparency(1688, (byte) ((byte) 127 - (v * 10)));
				});
				if (frozenPlayers.isEmpty())
					stop();
			}
			if (!crystal.isDead() && !crystal.hasFinished()) {
				frozenPlayers.forEach((p, v) -> {
					if (p.withinDistance(instance.NORTH_EAST, 12)) {
						final int value = frozenPlayers.get(p);
						frozenPlayers.put(p, value + 1);
						if (value == 0) {
							frozenPlayers.remove(p);
							return;
						}
						if (value < 0) {
							if (value % 2 == 0)
								p.applyHit(new Hit(null, withinOthers(p) ? 300 : 200, HitLook.REGULAR_DAMAGE));
							return;
						} else if (value == 10) {
							frozenPlayers.put(p, -10);
							p.getInterfaceManager().closeOverlay(true);
							p.applyHit(new Hit(null, withinOthers(p) ? 300 : 200, HitLook.REGULAR_DAMAGE));
							p.addFreezeDelay(5000);
						} else
							p.getPackets().sendIComponentTransparency(1688, (byte) ((byte) 127 - (frozenPlayers.get(p) * 10)));
					} else {
						if (v == 1) {
							p.getInterfaceManager().closeOverlay(true);
							frozenPlayers.remove(p);
						} else {
							frozenPlayers.put(p, v - 1);
							p.getPackets().sendIComponentTransparency(1688, (byte) ((byte) 127 - (v * 10)));
						}
					}
				});
			} else if (!finish)
				finish = true;
		}
		ticks++;
	}

	public boolean withinOthers(final Player p) {
		for (Player op : instance.getPlayers())
			if (p.withinDistance(op, 3) && op != p)
				return true;
		return false;
	}

}
