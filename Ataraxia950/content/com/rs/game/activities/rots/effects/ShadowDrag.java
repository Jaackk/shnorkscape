package com.rs.game.activities.rots.effects;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.activities.rots.npcs.RiseOfTheSixNPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/**
 * @author Kris | 3. sept 2017 : 23:35.32
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public class ShadowDrag extends RoTSEffect {

	public ShadowDrag(int duration, RiseOfTheSixNPC npc, Player player) {
		super(duration, npc, player);
	}

	@Override
	public void start() {
		WorldTasksManager.schedule(new WorldTask() {
			private int ticks;
			@Override
			public void run() {
				if (cancel()) {
					npc.finishEffect();
					stop();
					return;
				}
				if (ticks == 0) 
					instance.getPlayers().forEach(player -> player.sendMessage(instance.getShadowWight().getName() + " is preparing to drag everyone to shadow realm!"));
				else if (ticks == instance.getShadowDragTicks()) {
					instance.buildShadowRealm();
					WorldTasksManager.schedule(new WorldTask() {
						private int ticks;
						@Override
						public void run() {
							if (ticks == 0) {
								instance.getPlayers().forEach(p -> {
									p.lock(2);
									p.setNextAnimation(new Animation(21917, -1, -1, -1, -1, 0));
									p.setNextGraphics(new Graphics(4413));
									p.sendMessage(instance.getShadowWight().getName() + " forced everyone to shadow realm!");
								});
								for (int i = 0; i < instance.getWights().length; i++) {
									if (instance.getWights()[i].getEffect() != null)
										instance.getWights()[i].getEffect().setCancelled();
									instance.getWights()[i].resetWalkSteps();
									instance.getWights()[i].setTarget(null);
									instance.getWights()[i].setCantInteract(true);
									instance.getWights()[i].setNoDistanceCheck(true);
									instance.getWights()[i].setNextAnimation(new Animation(21917, -1, -1, -1, -1, 0));
									instance.getWights()[i].setNextGraphics(new Graphics(4413));
								}
							} else if (ticks == 1) {
								boolean dead = true;
								for (RiseOfTheSixNPC n : instance.getWights()) {
									if (n.isDead() || n.hasFinished())
										continue;
									dead = false;
								}
								if (dead) {
									stop();
									return;
								}
								instance.setShadowRealm(true);
								instance.getPlayers().forEach(p -> {
									p.setNextAnimation(new Animation(21915));
									p.setNextGraphics(new Graphics(4413));
									p.setNextWorldTile(instance.convertToShadowRealm(p));
								});
								for (int i = 0; i < instance.getWights().length; i++) {
									instance.getWights()[i].finishEffect();
									instance.getWights()[i].setCantInteract(false);
									instance.getWights()[i].setNextAnimation(new Animation(21915));
									instance.getWights()[i].setNextGraphics(new Graphics(4413));
									instance.getWights()[i].setNextWorldTile(instance.convertToShadowRealm(instance.getWights()[i]));
								}
								stop();
								return;
							}
							ticks++;
						}
					}, 0, 0);
					stop();
					return;
				}
				ticks++;
			}
		}, 0, 1);
	}

}
