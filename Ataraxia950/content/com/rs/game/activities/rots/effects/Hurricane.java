package com.rs.game.activities.rots.effects;

import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.activities.rots.npcs.RiseOfTheSixNPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author Kris | 3. sept 2017 : 23:34.57
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public class Hurricane extends RoTSEffect {

	public Hurricane(int duration, RiseOfTheSixNPC npc, Player player) {
		super(duration, npc, player);
	}

	@Override
	public void start() {
		WorldTasksManager.schedule(new WorldTask() {
			private int ticks;
			@Override
			public void run() {
				if (cancel() || ticks >= 10) {
					npc.finishEffect();
					npc.refreshSpecialDelay();
					npc.setNextGraphics(new Graphics(-1));
					npc.setNextRenderAnimation(npc.getDefinitions().getRenderAnimation());
					npc.setRun(true);
					stop();
					return;
				} else if (ticks == 0) {
					npc.setRun(false);
					npc.getCombat().addCombatDelay(10);
					npc.setNextRenderAnimation(2989);
				} else {
					npc.setNextGraphics(new Graphics(4415));
					for (Player p : instance.getPlayers()) {
						if (p == null || p.hasFinished() || p.isDead())
							continue;
						if (p.withinDistance(npc, 1)) {
							final int damage = Utils.random(200, 350);
							p.applyHit(new Hit(npc, instance.withinShadowRealm() ? damage * 2 : damage, HitLook.MELEE_DAMAGE));
						}
					}
				}
				ticks++;
			}
		}, 0, 0);
	}
}
