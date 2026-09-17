package com.rs.game.activities.rots.effects;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.NewForceMovement;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activities.rots.npcs.RiseOfTheSixNPC;
import com.rs.game.activities.rots.npcs.VeracNPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author Kris | 3. sept 2017 : 23:34.32
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public class DeathCopter extends RoTSEffect {

	public DeathCopter(int duration, RiseOfTheSixNPC npc, Player player) {
		super(duration, npc, player);
	}

	/**
	 * The tile where the partner is going to surge to.
	 */
	private WorldTile surgeTile;
	
	/**
	 * The death copter NPC.
	 */
	private final VeracNPC veracs = (VeracNPC) npc;
	
	@Override
	public void start() {
		veracs.setRequest(true);
		WorldTasksManager.schedule(new WorldTask() {
			private int ticks;
			@Override
			public void run() {
				if (!veracs.isRequestingAssistance() || cancel() || ticks++ == 34) {
					veracs.setRequest(false);
					if (surgeTile == null)
						veracs.finishEffect();
					stop();
				}
			}
		}, 0, 0);
	}
	
	public final void answerRequest(final RiseOfTheSixNPC partner) {
		surgeTile = getSurgeWorldTile(partner);
		WorldTasksManager.schedule(new WorldTask() {
			private int ticks;
			@Override
			public void run() {
				if (cancel() || ticks == 15) {
					npc.setCannotMove(false);
					npc.finishEffect();
					npc.refreshSpecialDelay();
					npc.setNextRenderAnimation(npc.getDefinitions().getRenderAnimation());
					npc.setTarget(instance.generateRandomTarget(npc));
					npc.setNextAnimation(new Animation(-1));
					stop();
					return;
				} else if (ticks == 0) {
					partner.getCombat().reset();
					partner.setCannotMove(true);
					npc.getCombat().reset();
					npc.setCannotMove(true);
					partner.setNextAnimation(new Animation(18358));
					partner.setNextGraphics(new Graphics(3537, 5, 0));
					partner.setNextForceMovement(new NewForceMovement(partner, 0, surgeTile, 1, Utils.getFaceDirection(surgeTile.getX() - partner.getX(), surgeTile.getY() - partner.getY())));
				} else if (ticks == 1) {
					partner.setNextWorldTile(surgeTile);
					partner.setNextFaceWorldTile(new WorldTile(npc));
					npc.setNextFaceWorldTile(surgeTile);
				} else if (ticks == 2) {
					partner.setNextFaceWorldTile(new WorldTile(npc));//Doesn't always turn on the first tick.
					partner.setNextAnimation(new Animation(21929));
					npc.setNextAnimation(new Animation(21943));
				} else if (ticks == 3) {
					npc.setNextRenderAnimation(2988);
					npc.setCannotMove(false);
					npc.setTarget(instance.generateRandomTarget(npc));
					partner.setCannotMove(false);
					partner.setTarget(instance.generateRandomTarget(partner));
					partner.setNextAnimation(new Animation(-1));
				} else if (ticks > 3) {
					for (Player p : instance.getPlayers()) {
						if (p == null || p.hasFinished() || p.isDead())
							continue;
						if (p.withinDistance(npc, 1)) {
							final int damage = Utils.random(5) == 0 ? Utils.random(300, 500) : Utils.random(100, 250);
							p.applyHit(new Hit(npc, instance.withinShadowRealm() ? damage * 2 : damage, HitLook.MELEE_DAMAGE));
						}
					}
				}
				ticks++;
			}
		}, 0, 0);
	}
	
	/**
	 * Gets the tile to which the partner can surge to; Checks clipping, area boundaries & makes sure that
	 * the tile doesn't require surging through the other NPC.
	 * @return
	 */
	private final WorldTile getSurgeWorldTile(final RiseOfTheSixNPC partner) {
		WorldTile tile;
		final int dirs = Utils.getFaceDirection(npc.getX() - partner.getX(), npc.getY() - partner.getY()) / 2048;
		final WorldTile back = new WorldTile(npc.getX() + Utils.DIRS[dirs][0], npc.getY() + Utils.DIRS[dirs][1], npc.getPlane());
		final WorldTile minCorner = instance.getWorldTile(19, 10);
		final WorldTile maxCorner = instance.getWorldTile(48, 29);
		while (true) {
			final int random = Utils.random(8);
			tile = new WorldTile(npc.getX() + Utils.DIRS[random][0], npc.getY() + Utils.DIRS[random][1] , npc.getPlane());
			if (World.canMoveNPC(tile, 1) && !tile.matches(back) && tile.withinArea(minCorner.getX(), minCorner.getY(), maxCorner.getX(), maxCorner.getY())) 
				break;
		}
		return tile;
	}

}
