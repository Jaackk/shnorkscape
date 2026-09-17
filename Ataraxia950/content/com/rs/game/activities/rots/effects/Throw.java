package com.rs.game.activities.rots.effects;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.NewForceMovement;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activities.rots.npcs.AhrimNPC;
import com.rs.game.activities.rots.npcs.KarilNPC;
import com.rs.game.activities.rots.npcs.RiseOfTheSixNPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kris | 3. sept 2017 : 23:35.55
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public class Throw extends RoTSEffect {

	public Throw(int duration, RiseOfTheSixNPC npc, Player player) {
		super(duration, npc, player);
	}
	
	/**
	 * Entities under the arc during the initiation
	 */
	private final List<Player> targets = new ArrayList<Player>();
	
	/**
	 * The tile where the partner is going to surge to.
	 */
	private WorldTile surgeTile;
	
	/**
	 * The tile where the wight being helped is jumping to.
	 */
	private WorldTile backflip;
	
	/**
	 * The helping wight.
	 */
	private RiseOfTheSixNPC partner;
	
	@Override
	public void start() {
		partner = getAssistant();
		if (partner == null) {
			npc.finishEffect();
			return;
		}
		WorldTasksManager.schedule(new WorldTask() {
			private int ticks;
			@Override
			public void run() {
				if (cancel()) {
					partner.setCannotMove(false);
					npc.setCannotMove(false);
					npc.finishEffect();
					partner.setTarget(instance.generateRandomTarget(partner));
					npc.setTarget(instance.generateRandomTarget(npc));
					partner.setNextAnimation(new Animation(-1));
					npc.setNextAnimation(new Animation(-1));
					stop();
				} else if (ticks == 0) {
					partner.getCombat().reset();
					partner.setCannotMove(true);
					npc.getCombat().reset();
					npc.setCannotMove(true);
					partner.setNextAnimation(new Animation(18358));
					partner.setNextGraphics(new Graphics(3537, 5, 0));
					surgeTile = getSurgeWorldTile();
					partner.setNextForceMovement(new NewForceMovement(partner, 0, surgeTile, 1, Utils.getFaceDirection(surgeTile.getX() - partner.getX(), surgeTile.getY() - partner.getY())));
				} else if (ticks == 1) {
					partner.setNextWorldTile(surgeTile);
					partner.setNextFaceWorldTile(new WorldTile(npc));
					npc.setNextFaceWorldTile(surgeTile);
				} else if (ticks == 2) {
					partner.setNextFaceWorldTile(new WorldTile(npc));//Doesn't always turn on the first tick.
					partner.setNextAnimation(new Animation(21929));
					npc.setNextAnimation(new Animation(21930));
				} else if (ticks == 3) {
					npc.setNextForceMovement(new NewForceMovement(npc, 0, backflip, 2, Utils.getFaceDirection(npc.getX() - backflip.getX(), npc.getY() - backflip.getY())));
					final List<WorldTile> tiles = Utils.calculateLine(npc.getX(), npc.getY(), backflip.getX(), backflip.getY(), npc.getPlane());
					for (WorldTile t : tiles) {
						for (Player p : instance.getPlayers()) {
							if (p == null || p.isDead() || p.hasFinished())
								continue;
							if (p.getTileHash() == t.getTileHash())
								targets.add(p);
						}
					}
				} else if (ticks == 5) {
					targets.forEach(target -> target.applyHit(new Hit(npc, Utils.random(200, instance.withinShadowRealm() ? 440 : 220), HitLook.REGULAR_DAMAGE)));
					npc.setNextWorldTile(backflip);
					npc.refreshSpecialDelay();
					partner.setCannotMove(false);
					npc.setCannotMove(false);
					npc.finishEffect();
					partner.setTarget(instance.generateRandomTarget(partner));
					npc.setTarget(instance.generateRandomTarget(npc));
					stop();
				}
				ticks++;
			}
		}, 0, 0);
	}
	
	/**
	 * Gets a random possible assisting wight.
	 * @return assisting wight.
	 */
	private final RiseOfTheSixNPC getAssistant() {
		final WorldTile center = instance.getWorldTile(34, 20);
		final List<RiseOfTheSixNPC> wights = new ArrayList<RiseOfTheSixNPC>();
		for (RiseOfTheSixNPC wight : instance.getWights()) {
			if (wight == null || wight.isDead() || wight.hasFinished() || wight.getEffect() != null || wight instanceof KarilNPC || wight instanceof AhrimNPC)
				continue;
			if (npc.getX() > center.getX() && wight.getX() > center.getX() || npc.getX() < center.getX() && wight.getX() < center.getX())
				wights.add(wight);
		}
		if (wights.size() == 0)
			return null;
		return wights.get(Utils.random(wights.size()));
	}
	
	private final WorldTile getSurgeWorldTile() {
		WorldTile tile = null;
		final int dirs = Utils.getFaceDirection(npc.getX() - partner.getX(), npc.getY() - partner.getY()) / 2048;
		final WorldTile back = new WorldTile(npc.getX() + Utils.DIRS[dirs][0], npc.getY() + Utils.DIRS[dirs][1], npc.getPlane());
		final WorldTile minCorner = instance.getWorldTile(19, 10);
		final WorldTile maxCorner = instance.getWorldTile(48, 29);
		int count = 50;
		while (--count > 0) {
			final int random = Utils.random(8);
			tile = new WorldTile(npc.getX() + Utils.DIRS[random][0], npc.getY() + Utils.DIRS[random][1] , npc.getPlane());
			final int inverseRandom = random + 4 >= 8 ? random - 4 : random + 4;
			backflip = new WorldTile(npc.getX() + (Utils.DIRS[inverseRandom][0] * 3), npc.getY() + (Utils.DIRS[inverseRandom][1] * 3), npc.getPlane());
			if (World.canMoveNPC(tile, 1) && World.canMoveNPC(backflip, 1) && !tile.matches(back) && tile.withinArea(minCorner.getX(), minCorner.getY(), maxCorner.getX(), maxCorner.getY()))
				break;
		}
		return tile;
	}
}
