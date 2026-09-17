package com.rs.game.activities.rots.effects;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.WorldTile;
import com.rs.game.activities.rots.npcs.RiseOfTheSixNPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author Kris | 3. sept 2017 : 23:35.37
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public class ShadowPits extends RoTSEffect {

	public ShadowPits(int duration, RiseOfTheSixNPC npc, Player player) {
		super(duration, npc, player);
	}

	@Override
	public void start() {
		final boolean leftSide = npc.getX() < instance.getMap().getTile("center").getX();
		final WorldTile location = Utils.randomBool() ? instance.getWorldTile(!leftSide ? 33 : 34, 11) : instance.getWorldTile(!leftSide ? 33 : 34, 29);
		WorldTasksManager.schedule(new WorldTask() {
			final WorldTile tile = new WorldTile(npc);
			private int tick;
			@Override
			public void run() {
				if (cancel() || tick == 7) {
					npc.setCannotMove(false);
					npc.setNextWorldTile(new WorldTile(instance.getWorldTile(leftSide ? 22 : 42, 21), 3));
					npc.setNextAnimation(new Animation(8941));
					npc.setNextGraphics(new Graphics(4416));
					if (cancel()) {
						npc.finishEffect();
						npc.refreshSpecialDelay();
						stop();
						return;
					}
				} else if (tick == 8) {
					npc.finishEffect();
					npc.refreshSpecialDelay();
					npc.setTarget(instance.generateRandomTarget(npc));
					stop();
					return;
				} else if (tick == 0) {
					npc.setCannotMove(true);
					npc.setNextAnimation(new Animation(8939));
					npc.setNextGraphics(new Graphics(4416));
				} else if (tick == 2) {
					for (Player p : instance.getPlayers()) {
						if (p == null)
							continue;
						final Entity target = (Entity) p.getTemporaryAttributtes().get("last_target");
						if (target == npc)
							p.getActionManager().forceStop();
					}
					npc.setNextWorldTile(location);
					npc.setNextAnimation(new Animation(8941));
					npc.setNextGraphics(new Graphics(4416));
				} else if (tick == 4) {
					instance.inflateOppositeSide(tile);
				} else if (tick == 5) {
					npc.setNextAnimation(new Animation(8939));
					npc.setNextGraphics(new Graphics(4416));
				}
				tick++;
			}
		}, 0, 0);
	}

}
