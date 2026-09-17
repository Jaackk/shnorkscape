package com.rs.game.npc.gwd2.twinfuries;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.NewForceMovement;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class CeilingCollapse extends TwinSpecialAttack {

	private static final int[][] COLLAPSE_TILES = new int[][] { { 23, 23 }, { 28, 23 }, { 33, 23 }, { 38, 23 }, { 23, 28 }, { 28, 28 }, { 33, 28 }, { 38, 28 }, { 23, 33 }, { 28, 33 }, { 33, 33 }, { 38, 33 }, { 23, 38 }, { 28, 38 }, { 33, 38 }, { 38, 38 } };

	public CeilingCollapse(NPC npc, Entity entity) {
		super(npc, entity);
	}

	@Override
	public void effect() {
		Nymora nymora = (Nymora) npc;
		final int random = Utils.random(COLLAPSE_TILES.length);
		final WorldTile t = nymora.getInstance().getWorldTile(COLLAPSE_TILES[random][0] - 1, COLLAPSE_TILES[random][1] - 1);
		nymora.setCantInteract(true);
		nymora.setNextFaceWorldTile(t);
		nymora.setNextForceTalk(new ForceTalk("We will purge them all!"));
		nymora.setNextForceMovement(new NewForceMovement(nymora, 0, t, 1, nymora.getDirection()));
		nymora.setNextAnimation(new Animation(28509));
		WorldTasksManager.schedule(new WorldTask() {
			int ticks;

			@Override
			public void run() {
				if (npc.isDead() || npc.hasFinished() || nymora.getInstance().getPlayers().size() == 0) {
					stop();
					return;
				}
				if (ticks == 0) {
					nymora.setNextWorldTile(t);
					nymora.setNextFaceWorldTile(nymora.getInstance().getWorldTile(30, 31));
				} else if (ticks == 1) {
					nymora.setNextAnimation(new Animation(28517));
				} else if (ticks == 2)
					nymora.setNextAnimation(new Animation(28518));
				else if (ticks == 17) {
					nymora.setNextAnimation(new Animation(28519));
					nymora.setCantInteract(false);
				} else if (ticks == 18) {
					if (entity.getTileHash() == new WorldTile(nymora.getCoordFaceX(nymora.getSize()), nymora.getCoordFaceY(nymora.getSize()), nymora.getPlane()).getTileHash()) {
						if (!nymora.addWalkSteps(nymora.getX() - 1, nymora.getY(), 1))
							if (!nymora.addWalkSteps(nymora.getX() + 1, nymora.getY(), 1))
								if (!nymora.addWalkSteps(nymora.getX(), nymora.getY() + 1, 1))
									nymora.addWalkSteps(nymora.getX(), nymora.getY() - 1, 1);
					}
					stop();
					return;
				}
				if (ticks > 2 && ticks % 2 == 0)
					sendStalagmites(nymora, random);
				else if (ticks > 3 && ticks % 2 == 1) {
					final WorldTile tile = new WorldTile(nymora.getCoordFaceX(nymora.getSize()), nymora.getCoordFaceY(nymora.getSize()), nymora.getPlane());
					loop: for (Player nymoras : nymora.getInstance().getPlayers()) {
						for (int[] collapseTiles : COLLAPSE_TILES)
							if (nymoras.withinDistance(nymora.getInstance().getWorldTile(collapseTiles[0], collapseTiles[1]), 3) && nymoras.getDistance(tile) > 1) {
								nymoras.applyHit(new Hit(nymora, nymora.getInstance().isHardMode() ? Utils.random(100, 200) : Utils.random(50, 100), HitLook.RANGE_DAMAGE));
								continue loop;
							}
					}
				}
				ticks++;
			}
		}, 0, 0);
	}

	private final void sendStalagmites(final Nymora nymora, final int random) {
		for (int i = 0; i < COLLAPSE_TILES.length; i++) {
			if (i == random)
				continue;
			for (Player p : nymora.getInstance().getPlayers())
				p.getPackets().sendGraphics(new Graphics(6145), nymora.getInstance().getWorldTile(COLLAPSE_TILES[i][0], COLLAPSE_TILES[i][1]));
		}
	}

}
