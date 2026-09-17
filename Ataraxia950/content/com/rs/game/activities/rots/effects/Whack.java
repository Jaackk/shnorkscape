package com.rs.game.activities.rots.effects;

import com.rs.game.Animation;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.activities.rots.npcs.RiseOfTheSixNPC;
import com.rs.game.activities.rots.npcs.ToragNPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/**
 * @author Kris | 3. sept 2017 : 23:36.06
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public class Whack extends RoTSEffect {

	public Whack(int duration, RiseOfTheSixNPC npc, Player player) {
		super(duration, npc, player);
	}

	@Override
	public void start() {
		WorldTasksManager.schedule(new WorldTask() {
			private int loop;
			@Override
			public void run() {
				if (((ToragNPC) npc).getDamageTaken() >= 300 || cancel()) {
					if (!player.hasFinished()) {
						player.unlockROTS();
						player.getAppearence().setRenderEmote(-1);
					}
					if (!player.hasFinished() && !player.isDead())
						player.setNextAnimation(new Animation(21938));
					npc.refreshSpecialDelay();
					npc.finishEffect();
					if (!npc.hasFinished() && !npc.isDead()) {
						npc.setNextAnimation(new Animation(21936));
						npc.setNextRenderAnimation(2689);
						npc.setTarget(player);
					}
					((ToragNPC) npc).resetDamageTaken();
					player.setCantDoDefenceEmote(false);
					stop();
					return;
				}
				if (loop == 0) {
					npc.setNextAnimation(new Animation(21933));
					player.resetWalkSteps();
					player.setNextAnimation(new Animation(21934));
					player.setCantDoDefenceEmote(true);
					player.lockROTS();
					player.setTarget(null);
				} else if (loop == 2) {
					player.getAppearence().setRenderEmote(2985);
					npc.setNextRenderAnimation(2984);
				}
				if (loop > 3)
					player.applyHit(new Hit(npc, instance.withinShadowRealm() ? 100 : 50, HitLook.MELEE_DAMAGE));
				loop++;
			}
		}, 0, 0);

	}

}
