package com.rs.game.activities.aod.ability;

import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.NewProjectile;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activities.aod.AngelOfDeath;
import com.rs.game.activities.aod.npc.AoDNex;
import com.rs.game.hitbar.impl.HitBarTimer;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Elements instant kill ability for AoD. Sends four projectiles from all
 * corners to the center after certain amount of time has passed. If the
 * targeted player(s) aren't within the dome at the center, they will be
 * instantly killed.
 * 
 * @author Kris | 30. sept 2017 : 16:43.57
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server
 *      profile</a>}
 */
public final class ElementsInstantKill extends AoDAbility {

	public ElementsInstantKill(final AoDNex nex, final AngelOfDeath instance) {
		super(nex, instance);
	}

	public static final Graphics EXPLOSION = new Graphics(6529);
	private int ticks;
	private final List<Player> targets = new CopyOnWriteArrayList<Player>();
	private final List<WorldTile> tiles = new ArrayList<WorldTile>();

	@Override
	public void run() {
		if (targets.size() > 0) {
			targets.forEach(t -> {
				if (t.isDead() || t.hasFinished() || !instance.getPlayers().contains(t))
					targets.remove(t);
			});
		}
		if (ticks == 0) {
			instance.getAllPlayers().forEach(p -> p.getPackets().sendSpawnedObject(new WorldObject(100805, 10, 0, instance.getWorldTile(2846, 1822))));
			targets.add(instance.getPlayers().get(Utils.random(instance.getPlayers().size())));
			targets.forEach(t -> {
				t.getPackets().sendPlayerMessage(1, 15263739, "<col=ff0000>Nex has marked you to take the full force of the elements.", true);
				t.getNextHitBars().add(new HitBarTimer(0));
			});
		} else if (ticks < 19) {
			if (ticks == 17) {
				targets.forEach(t -> {
					t.getPackets().sendTestProjectile(new NewProjectile(instance.SOUTH_WEST, t, 6523, 100, 10, 110, 0));
					t.getPackets().sendTestProjectile(new NewProjectile(instance.SOUTH_EAST, t, 6525, 100, 10, 110, 0));
					t.getPackets().sendTestProjectile(new NewProjectile(instance.NORTH_WEST, t, 3371, 100, 10, 110, 0));
					t.getPackets().sendTestProjectile(new NewProjectile(instance.NORTH_EAST, t, 6524, 100, 10, 110, 0));
				});
			}
			targets.forEach(t -> t.getNextHitBars().add(new HitBarTimer((int) ((ticks * 6.66)))));
		} else if (ticks == 19) {
			final List<Player> toRemove = new ArrayList<Player>();
			targets.forEach(t -> {
				t.getPackets().sendGraphics(EXPLOSION, t);
				if (!t.withinDistance(instance.CENTER, 2)) {
					t.applyHit(new Hit(null, Integer.MAX_VALUE, HitLook.INSTANT_KILL_TYPE));
					toRemove.add(t);
				} else
					t.getPackets().sendPlayerMessage(1, 15263739, "<col=ff0000>The shield blocks most of the damage, but you feel the elements boiling up inside.", true);
			});
			targets.removeAll(toRemove);
		} else if (ticks == 20) {
			targets.forEach(t -> t.getNextHitBars().add(new HitBarTimer(0)));
		} else if (ticks < 36) {
			targets.forEach(t -> t.getNextHitBars().add(new HitBarTimer((int) (((ticks - 20) * 6.66)))));
		} else if (ticks == 38) {
			targets.forEach(t -> tiles.add(new WorldTile(t)));
		} else if (ticks == 40) {
			tiles.forEach(t -> instance.addBomb(t));
			stop();
			return;
		}
		ticks++;
	}

}
