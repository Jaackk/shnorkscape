package com.rs.game.activities.rots.effects;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activities.rots.npcs.KarilNPC;
import com.rs.game.activities.rots.npcs.RiseOfTheSixNPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author Kris | 3. sept 2017 : 23:34.27
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public class Bombard extends RoTSEffect {

	public Bombard(int duration, RiseOfTheSixNPC npc, Player player) {
		super(duration, npc, player);
	}
	
	@Override
	public void start() {
		final boolean leftSide = npc.getX() < instance.getMap().getTile("center").getX();
		WorldTasksManager.schedule(new WorldTask() {
			private int tick;
			private final boolean random = Utils.randomBool();
			final boolean reverse = Utils.randomBool();
			private final KarilNPC karils = (KarilNPC) npc;
			@Override
			public void run() {
				if (cancel()) {
					npc.finishEffect();
					npc.setNextWorldTile(new WorldTile(instance.getWorldTile(leftSide ? 22 : 42, 21), 3));
					npc.setTarget(instance.generateRandomTarget(npc));
					stop();
					return;
				} else if (tick == 0) {
					npc.setNextAnimation(new Animation(8939));
					npc.setNextGraphics(new Graphics(4416));
				} else if (tick == 2) {
					if (npc.getEffect() == null)
						return;
					WorldTile location;
					if (Utils.randomBool())
						location = instance.getWorldTile(!leftSide ? 33 : 34, 11);
					else 
						location = instance.getWorldTile(!leftSide ? 33 : 34, 29);
					for (Player p : instance.getPlayers()) {
						if (p == null)
							continue;
						final Entity target = (Entity) p.getTemporaryAttributtes().get("last_target");
						if (target == npc)
							p.getActionManager().forceStop();
					}
					npc.setNextWorldTile(location);
					npc.setNextGraphics(new Graphics(4416));
					npc.setNextAnimation(new Animation(8941));
				} else if (tick == 15) {
					npc.setNextAnimation(new Animation(8939));
					npc.setNextGraphics(new Graphics(4416));
				} else if (tick == 18) {
					npc.setNextWorldTile(new WorldTile(instance.getWorldTile(leftSide ? 22 : 42, 21), 3));
					npc.setNextAnimation(new Animation(8941));
					npc.setNextGraphics(new Graphics(4416));
				} else if (tick == 20) {
					npc.refreshSpecialDelay();
					npc.setTarget(instance.generateRandomTarget(npc));
					karils.finishEffect();
					stop();
					return;
				} else if (tick % 2 == 0) {
					final int height = reverse ? 12 + (tick - 2) :  34 - (tick + 2);
					if (leftSide)
						World.sendGraphics(npc, new Graphics(4410), instance.getWorldTile(random ? 23 : 28, height));
					else
						World.sendGraphics(npc, new Graphics(4410), instance.getWorldTile(random ? 39 : 44, height));
					for (Player p : instance.getPlayers()) {
						if (p == null || p.hasFinished() || p.isDead())
							continue;
						if (p.withinDistance(new WorldTile(leftSide ? instance.getWorldTile(random ? 23 : 28, height) : instance.getWorldTile(random ? 39 : 44, 14 + ((tick - 2)))), 2)) {
							p.applyHit(new Hit(npc, Utils.getRandom(instance.withinShadowRealm() ? 500 : 150), HitLook.REGULAR_DAMAGE));
							if (tick == 2)
								for (int i = 0; i < 2; i++)
									p.applyHit(new Hit(npc, Utils.getRandom(instance.withinShadowRealm() ? 500 : 150), HitLook.REGULAR_DAMAGE));
						}
					}
				}
				tick++;
			}
		}, 0, 0);
	}

}
