package com.rs.game.activities.rots.effects;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.WorldTile;
import com.rs.game.activities.rots.npcs.RiseOfTheSixNPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/**
 * @author Kris | 3. sept 2017 : 23:34.38
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public class Empowerment extends RoTSEffect {

	public Empowerment(int duration, RiseOfTheSixNPC npc, Player player) {
		super(duration, npc, player);
	}

	@Override
	public void start() {
		WorldTasksManager.schedule(new WorldTask() {
			private int tick;
			@Override
			public void run() {
				if (tick == 0) {
					npc.setNextAnimation(new Animation(21917, -1, -1, -1, -1, 0));
					npc.setNextGraphics(new Graphics(4413));
				} else if (tick == 1) {
					WorldTile loc = npc.getX() < instance.getWorldTile(35, 19).getX() ? instance.getWorldTile(42, 19) : instance.getWorldTile(25, 19);
					npc.setNextWorldTile(new WorldTile(loc, 5));
					npc.setNextAnimation(new Animation(21915));
					npc.setNextGraphics(new Graphics(4413));
					npc.finishEffect();
					npc.refreshSpecialDelay();
					stop();
					return;
				}
				tick++;
			}
		}, 0, 0);
	}

}
