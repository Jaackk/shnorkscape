package com.rs.game.activities.rots.effects;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceMovement;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.activities.rots.npcs.KarilNPC;
import com.rs.game.activities.rots.npcs.RiseOfTheSixNPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static com.rs.game.activities.rots.RiseOfTheSix.DIRS;

/**
 * @author Kris | 3. sept 2017 : 23:35.09
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public class LightningConductor extends RoTSEffect {

	public LightningConductor(int duration, RiseOfTheSixNPC npc, Player player) {
		super(duration, npc, player);
	}
	
	private int direction;
	private final List<Entity> targets = new ArrayList<Entity>();
	private final List<WorldTile> line = new ArrayList<WorldTile>();
	
	public final boolean getArenaSide(Entity entity) {
		return entity.getX() > instance.getWorldTile(34, 20).getX();
	}
	
	/**
	 * Gets the tile at which the NPC begins to do the firey special.
	 * If the opposite NPC is already doing the same special, it will not go under that NPC, but rather
	 * to the opposite side.
	 * @return tile
	 */
	private final WorldTile getTile() {
		WorldTile location;
		final boolean conflict = getArenaSide(npc) == getArenaSide(instance.getAhrims());
		if (conflict && instance.getAhrims().getEffect() instanceof TurretOfFire) {
			if (!getArenaSide(npc)) {
				if (instance.getAhrims().getDistance(instance.getWorldTile(25, 24)) > instance.getAhrims().getDistance(instance.getWorldTile(25, 16)))
					location = instance.getWorldTile(25, 24);
				else
					location = instance.getWorldTile(25, 16);
			} else {
				if (instance.getAhrims().getDistance(instance.getWorldTile(42, 24)) > instance.getAhrims().getDistance(instance.getWorldTile(42, 16)))
					location = instance.getWorldTile(42, 24);
				else
					location = instance.getWorldTile(42, 16);
			}
		} else
			location = instance.getNorthernTile(npc, getArenaSide(npc));
		return location;
	}

	@Override
	public void start() {
		npc.setTarget(null);
		npc.setCannotMove(true);
		final WorldTile tile = getTile();
		npc.setNextAnimation(new Animation(18358));
		npc.setNextGraphics(new Graphics(3537, 5, 0));
		npc.setNextForceMovement(new ForceMovement(npc, 0, tile, 1, ForceMovement.getDirection(Utils.getMoveDirection(npc.getX() - tile.getX(), npc.getY() - tile.getY()))));
		WorldTasksManager.schedule(new WorldTask() {
			private int stage;
			private final KarilNPC karils = instance.getKarils();
			private final boolean northern = npc.withinDistance(instance.getWorldTile(25, 24), 5) || npc.withinDistance(instance.getWorldTile(42, 24), 5);
			@Override
			public void run() {
				if (cancel() || stage == 19) {
					npc.setCannotMove(false);
					npc.setTarget(instance.generateRandomTarget(npc));
					npc.refreshSpecialDelay();
					karils.setCantSetGraphics(false);
					npc.setNextGraphics(new Graphics(-1));
					karils.setNextRenderAnimation(npc.getDefinitions().getRenderAnimation());
					karils.finishEffect();
					stop();
					return;
				} else if (stage == 0) {
					npc.setNextWorldTile(tile);
				} else if (stage == 1) {
					if (northern)
						direction = 8;
					karils.setNextAnimation(new Animation(21909));
					karils.setNextFaceWorldTile(new WorldTile(karils.getX(), northern ? (karils.getY() + 1) : (karils.getY() - 1), karils.getPlane()));
				} else if (stage > 2) {
					if (stage == 3) {
						karils.setNextRenderAnimation(2990);
						karils.setNextGraphics(new Graphics(4408));
						karils.setCantSetGraphics(true);
					}
					final int dir = direction >= 16 ? (direction - 16) : direction;
					direction++;
					if (direction == 16)
						direction = 16 - direction;
					final int dl = instance.getDirection(direction, northern ? 11 : 3);
					final int dr = instance.getDirection(direction, northern ? 3 : 11);
					line.clear();
					targets.clear();
					line.addAll(Utils.calculateLine(karils.getX() + DIRS[dl][0], karils.getY() + DIRS[dl][1], karils.getX() + DIRS[dr][0], karils.getY() + DIRS[dr][1], karils.getPlane()));
					karils.setNextFaceWorldTile(new WorldTile(karils.getX() + DIRS[dir][0], karils.getY() + DIRS[dir][1], karils.getPlane()));
					for (Player p : instance.getPlayers())
						if (p.withinDistance(npc, 2)) 
							targets.add(p);
					for (WorldTile t : line) {
						for (Player p : instance.getPlayers()) {
							if (!targets.contains(p) && p.withinDistance(t, 1))
								targets.add(p);
							if (p.getFamiliar() != null && !targets.contains(p.getFamiliar()) &&  p.getFamiliar().withinDistance(t, 1))
								targets.add(p.getFamiliar());
						}
					}
					for (Entity e : targets)
						e.applyHit(new Hit(null, instance.withinShadowRealm() ? Utils.random(300) : Utils.random(150), HitLook.REGULAR_DAMAGE));
				}
				stage++;
			}
		}, 0, 0);
	}
	
}
