package com.rs.game.activities.rots.effects;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activities.rots.npcs.RiseOfTheSixNPC;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/**
 * @author Kris | 3. sept 2017 : 23:35.15
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public class PortalDash extends RoTSEffect {

	public PortalDash(int duration, RiseOfTheSixNPC npc, Player player) {
		super(duration, npc, player);
	}

	private WorldTile bombCoords;
	private NPC[] bombs;
	
	private void setBombs(int ticks) {
		if (bombs == null)
			bombs = new NPC[6];
		bombs[(ticks - 1) / 10] = new NPC(18552, new WorldTile(bombCoords), -1, false, true);
	}

	@Override
	public void start() {
		instance.resetPortalDashTeleports();
		npc.setTarget(null);
		npc.setCannotMove(true);
		WorldTasksManager.schedule(new WorldTask() {
			private int ticks;
			@Override
			public void run() {
				if (ticks == 52) {
					npc.setCannotMove(false);
					npc.setTarget(instance.generateRandomTarget(npc));
				}
				if (cancel()) {
					npc.finishEffect();
					npc.setCannotMove(false);
					if (bombs != null) {
						for (NPC bomb : bombs) {
							if (bomb != null && !bomb.hasFinished())
								bomb.finish();
						}
					}
					stop();
					return;
				} else if (ticks % 10 == 0 && ticks < 51) {
					npc.setNextAnimation(new Animation(21917, -1, -1, -1, -1, 0));
					npc.setNextGraphics(new Graphics(4413));
					bombCoords = new WorldTile(npc);
				} else if ((ticks - 1) % 10 == 0 && ticks < 52) {
					setBombs(ticks);
					WorldTile toLocation = bombCoords.getX() < instance.getWorldTile(35, 19).getX() ? instance.getWorldTile(42, 20) : instance.getWorldTile(25, 20);
					for (Player p : instance.getPlayers()) {
						if (p == null)
							continue;
						final Entity target = (Entity) p.getTemporaryAttributtes().get("last_target");
						if (target == npc)
							p.getActionManager().forceStop();
					}
					npc.setNextWorldTile(new WorldTile(toLocation, 5));
					npc.setNextAnimation(new Animation(21915));
					npc.setNextGraphics(new Graphics(4413));
				} else if ((ticks + 1) % 10 == 0 && ticks < 60) {
					final NPC bomb = bombs[ticks == 9 ? 0 : ((ticks - 9) / 10)];
					if (bomb != null && !bomb.hasFinished() && !bomb.isLocked()) {
						World.sendGraphics(null, new Graphics(4412), new WorldTile(bomb));
						bomb.finish();
						for (Player p : instance.getPlayers()) {
							if (p.withinDistance(bomb, 8))
								p.applyHit(new Hit(null, 800 - (p.getDistance(bomb) * 40), HitLook.REGULAR_DAMAGE));
						}
					}
					if (ticks == 59) {
						instance.getKarils().finishEffect();
						npc.refreshSpecialDelay();
						stop();
					}
				}
				ticks++;
			}
		}, 0, 0);
	}

}
