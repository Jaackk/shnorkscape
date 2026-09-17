package com.rs.game.activities.aod.ability;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activities.aod.AngelOfDeath;
import com.rs.game.activities.aod.npc.AoDNex;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the mass shadow traps special ability of AoD Nex. Sends anywhere from 2 to 5 shadow traps per player.
 * @author Kris | 30. sept 2017 : 22:19.39
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public final class ShadowTraps extends AoDAbility {

	public ShadowTraps(final AoDNex nex, final AngelOfDeath instance) {
		super(nex, instance);
	}
	
	private static final Animation ANIMATION = new Animation(17407);
	private static final Graphics GFX = new Graphics(3362);
	private static final Graphics HIT = new Graphics(383);
	
	private int ticks;
	private final Map<Player, Integer> traps = new ConcurrentHashMap<Player, Integer>();
	private final Map<Player, Integer> existingTraps = new HashMap<Player, Integer>();
	private final List<WorldObject> trapObjects = new ArrayList<WorldObject>();
	
	@Override
	public void run() {
		traps.forEach((k, v) -> {
			if (k.isDead() || k.hasFinished() || !instance.getPlayers().contains(k))
				traps.remove(k);
		});
		existingTraps.forEach((k, v) -> {
			if (k.isDead() || k.hasFinished() || !instance.getPlayers().contains(k))
				existingTraps.remove(k);
		});
		if (ticks == 0) {
			nex.setNextAnimation(ANIMATION);
			nex.setNextGraphics(GFX);
			instance.getPlayers().forEach(p -> traps.put(p, Utils.random(2, 6)));
		} else if (ticks % 3 == 0) {
			trapObjects.forEach(t -> {
				if (World.containsObjectWithId(t, t.getId()))
					World.removeObject(t);
			});
			existingTraps.forEach((k, v) -> {
				instance.sendGraphics(HIT, new WorldTile(k.getX(), k.getY(), k.getPlane()));
				if (k.getTileHash() == v)
					k.applyHit(new Hit(nex, Utils.random(250, 480), HitLook.REGULAR_DAMAGE));
			});
			existingTraps.clear();
			traps.forEach((k, v) -> {
				final WorldObject t = new WorldObject(57261, 10, 0, k.getX(), k.getY(), k.getPlane());
				trapObjects.add(t);
				World.spawnObject(t);
				existingTraps.put(k, k.getTileHash());
				if (v > 1)
					traps.put(k, v - 1);
				else
					traps.remove(k);
			});
		} else if (ticks == 20) {
			stop();
			return;
		}
		ticks++;
	}

}
