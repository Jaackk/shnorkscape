package com.rs.game.activities.aod.ability;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.activities.aod.AngelOfDeath;
import com.rs.game.activities.aod.npc.AoDNex;
import com.rs.game.activities.aod.npc.Icicle;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The ice prison ability. AoD targets up to two people, who will then be frozen
 * inside an icicle. In order to free them, the other team-mates must defeat the
 * icicle(s) before the timer runs out on them. Failure to do so results in 5
 * stacks of 200 damage each on the frozen player(s), with small intervals.
 * 
 * @author Kris | 30. sept 2017 : 16:45.23
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server
 *      profile</a>}
 */
public final class IcePrison extends AoDAbility {

	public IcePrison(final AoDNex nex, final AngelOfDeath instance) {
		super(nex, instance);
	}

	private static final Graphics ICICLE = new Graphics(6536);
	private static final Animation DOWN = new Animation(10975);
	public static final Animation STANDING_UP = new Animation(11788);

	/**
	 * TODO: 100k hp if 1 people, 50k hp if two people.
	 */
	private int ticks;
	private final List<Player> targets = new CopyOnWriteArrayList<Player>();
	private final List<Icicle> icicles = new ArrayList<Icicle>();

	@Override
	public void run() {
		if (targets.size() > 0) {
			targets.forEach(t -> {
				if (t.isDead() || t.hasFinished() || !instance.getPlayers().contains(t))
					targets.remove(t);
			});
		}
		switch (ticks++) {
		case 0:
			nex.sendMessage("You... Stay...");
			for (int i = 0; i < 50; i++) {
				if (targets.size() == 2 || targets.size() == instance.getPlayers().size())
					break;
				final Player p = instance.getPlayers().get(Utils.random(instance.getPlayers().size()));
				if (targets.contains(p))
					continue;
				targets.add(p);
			}
			targets.forEach(p -> {
				p.getPackets().sendPlayerMessage(1, 15263739, "Ice begins to form around your feet, locking you in place!", true);
				p.addFreezeDelay(Integer.MAX_VALUE);
				p.getInterfaceManager().sendOverlay(1688, true);
				p.getPackets().sendIComponentTransparency(1688, (byte) 127);
			});
			break;
		case 1:
		case 2:
		case 3:
		case 4:
			targets.forEach(p -> p.getPackets().sendIComponentTransparency(1688, (byte) (127 - (ticks * 10))));
			break;
		case 5:
			nex.sendMessage("DOWN!");
			if (targets.size() != 0)
				nex.setNextFaceWorldTile(new WorldTile(targets.get(0)));
			targets.forEach(p -> {
				icicles.add(new Icicle(p, new WorldTile(p)));
				p.getPackets().sendPlayerMessage(1, 15263739, "An icicle falls from the ceiling pinning you in place.", true);
				p.getPackets().sendIComponentTransparency(1688, (byte) 0);
				p.setNextAnimation(DOWN);
				for (int x = p.getX() - 2; x < p.getX() + 3; x++) {
					for (int y = p.getY() - 2; y < p.getY() + 3; y++) {
						instance.sendGraphics(ICICLE, instance.getWorldTile(x, y));
						instance.getPlayers().forEach(player -> {
							if (!targets.contains(player) && player.withinDistance(p, 2)) {
								player.applyHit(new Hit(null, 500 / player.getDistance(p), HitLook.REGULAR_DAMAGE));
								player.addFreezeDelay(5000);
							}
						});
					}
				}
			});
			break;
		case 39:
			final List<Icicle> toRemove = new ArrayList<Icicle>();
			icicles.forEach(i -> {
				if (i.isDead() || i.hasFinished()) {
					toRemove.add(i);
					targets.remove(i.getPlayer());
				}
			});
			icicles.removeAll(toRemove);
			icicles.forEach(icicle -> icicle.finish());
			targets.forEach(target -> {
				target.setFreezeDelay(0);
				target.getInterfaceManager().closeOverlay(true);
				target.setNextAnimation(STANDING_UP);
			});
			if (icicles.isEmpty())
				stop();
			break;
		case 50:
			stop();
		case 40:
		case 42:
		case 44:
		case 46:
		case 48:
			targets.forEach(t -> {
				boolean close = false;
				loop: for (Player p : instance.getPlayers()) {
					if (p != t && p.withinDistance(t, 2)) {
						close = true;
						break loop;
					}
				}
				t.applyHit(new Hit(null, close ? 300 : 200, HitLook.REGULAR_DAMAGE));
			});
			return;
		}
	}

}
