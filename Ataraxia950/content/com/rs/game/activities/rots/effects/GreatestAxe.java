package com.rs.game.activities.rots.effects;

import com.rs.game.Animation;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.activities.rots.npcs.RiseOfTheSixNPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/**
 * @author Kris | 3. sept 2017 : 23:34.46
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public class GreatestAxe extends RoTSEffect {

	public GreatestAxe(int duration, RiseOfTheSixNPC npc, Player player) {
		super(duration, npc, player);
	}

	@Override
	public void start() {
		WorldTasksManager.schedule(new WorldTask() {
			private int tick;
			@Override
			public void run() {
				if (cancel() || tick == duration) {
					npc.setCannotMove(false);
					npc.setTarget(instance.generateRandomTarget(npc));
					npc.finishEffect();
					npc.refreshSpecialDelay();
					npc.setNextGraphics(new Graphics(-1));
					npc.setNextAnimation(new Animation(-1));
					npc.setCantFollowUnderCombat(false);
					npc.setCantDoDefenceEmote(false);
					stop();
					return;
				} else if (tick == 0) {
					npc.setCantDoDefenceEmote(true);
					npc.setNextForceTalk(new ForceTalk("Give me everything!"));
					World.sendGraphics(npc, new Graphics(4406, 0, 4, 0), npc);
					npc.setNextAnimation(new Animation(21940));
					npc.setCantFollowUnderCombat(true);
					npc.setTarget(null);
					npc.setCannotMove(true);
				}
				tick++;
			}
		}, 0, 0);

	}

}
