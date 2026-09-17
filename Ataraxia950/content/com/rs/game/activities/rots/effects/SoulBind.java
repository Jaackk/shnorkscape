package com.rs.game.activities.rots.effects;

import com.rs.game.Graphics;
import com.rs.game.WorldTile;
import com.rs.game.activities.rots.npcs.RiseOfTheSixNPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author Kris | 3. sept 2017 : 23:35.42
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public class SoulBind extends RoTSEffect {

	public SoulBind(int duration, RiseOfTheSixNPC npc, Player player) {
		super(duration, npc, player);
	}
	
	private WorldTile boundTile;
	
	private final void sendDirections(boolean reset) {
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (boundTile != null) {
					player.getPackets().sendGraphics(new Graphics(reset ? -1 : 2789, 0, 10, 4), new WorldTile(boundTile.getX(), boundTile.getY() + 1, boundTile.getPlane()));
					player.getPackets().sendGraphics(new Graphics(reset ? -1 : 2789, 0, 10, 0), new WorldTile(boundTile.getX(), boundTile.getY() - 1, boundTile.getPlane()));
					player.getPackets().sendGraphics(new Graphics(reset ? -1 : 2789, 0, 10, 2), new WorldTile(boundTile.getX() - 1, boundTile.getY(), boundTile.getPlane()));
					player.getPackets().sendGraphics(new Graphics(reset ? -1 : 2789, 0, 10, 6), new WorldTile(boundTile.getX() + 1, boundTile.getY(), boundTile.getPlane()));
				} else
					stop();
			}
		}, 0, 2);
	}

	@Override
	public void start() {
		if (instance.getSoulBoundPlayers().contains(player)) {
			npc.finishEffect();
			return;
		}
		WorldTasksManager.schedule(new WorldTask() {
			private int ticks;
			@Override
			public void run() {
				if (npc.isDead() || npc.hasFinished()) {
					npc.finishEffect();
					sendDirections(true);
					boundTile = null;
					instance.removeSoulBoundPlayer(player);
					stop();
					return;
				}
				if (ticks == 2)
					npc.finishEffect();
				if (ticks == 0) {
					instance.addSoulBoundPlayer(player);
					player.sendMessage(npc.getName() + " has bound your soul to a specific place!");
					final WorldTile loc = npc.getX() > instance.getWorldTile(35, 19).getX() ? instance.getWorldTile(42, 19) : instance.getWorldTile(25, 19);
					boundTile = new WorldTile(loc, 5);
					sendDirections(false);
				} else if (ticks < 100) {
					if (player.getHash() == boundTile.getHash()) {
						instance.removeSoulBoundPlayer(player);
						boundTile = null;
						sendDirections(true);
						stop();
						return;
					}
					player.getPrayer().drainPrayer(Utils.random(10, 50));
				} else if (ticks == 100) {
					instance.removeSoulBoundPlayer(player);
					boundTile = null;
					sendDirections(true);
					stop();
					return;
				}
				ticks++;
			}
		}, 0, 0);

	}

}
