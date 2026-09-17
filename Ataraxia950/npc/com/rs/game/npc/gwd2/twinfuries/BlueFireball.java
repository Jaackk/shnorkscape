package com.rs.game.npc.gwd2.twinfuries;

import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.NewProjectile;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activities.instances.TwinFuriesInstance;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class BlueFireball extends TwinSpecialAttack {

	protected final TwinFuriesInstance instance;
	protected WorldTile tile;
	private WorldTile to;
	private boolean lockedOnTarget, cancelled;
	
	public BlueFireball(NPC npc, Entity entity) {
		super(npc, entity);
		instance = ((Avaryss) npc).getInstance();
	}
	
	public boolean isLockedOnTarget() {
		return lockedOnTarget;
	}
	
	public boolean isCancelled() {
		return cancelled;
	}
	
	@Override
	public void effect() {
		tile = instance.getWorldTile(Utils.random(21, 41), Utils.random(21, 41));
		WorldTasksManager.schedule(new WorldTask() {
			private int currentTicks;
			private int time;
			private int stage;
			@Override
			public void run() {
				if (currentTicks++ == time) {
					sendDirections(true);
					setNextLocation();
					sendDirections(false);
					if (currentTicks > 0) {
						instance.getPlayers().forEach(p -> {
							if (p.withinDistance(tile, 2) && !cancelled) {
								cancelled = true;
								WorldTasksManager.schedule(new WorldTask() {
									private int ticks, avaryss, nymora;
									@Override
									public void run() {
										if (ticks == 0) {
											final NewProjectile avaryssProjectile = new NewProjectile(tile, instance.getAvaryss(), 6147, 25, 25, 0, 35, 15, -2);
											World.sendProjectile(avaryssProjectile);
											avaryss = avaryssProjectile.getTime() / 335;
											final NewProjectile nymoraProjectile = new NewProjectile(tile, instance.getNymora(), 6147, 25, 25, 0, 35, 15, -2);
											World.sendProjectile(nymoraProjectile);
											nymora = nymoraProjectile.getTime() / 335;
										}
										if (ticks == avaryss) 
											instance.getAvaryss().applyHit(new Hit(entity, Utils.random(200, 680), HitLook.REGULAR_DAMAGE));
										if (ticks == nymora)
											instance.getNymora().applyHit(new Hit(entity, Utils.random(200, 680), HitLook.REGULAR_DAMAGE));
										if (ticks >= avaryss && ticks >= nymora)
											stop();
										ticks++;
									}
								}, 0, 0);
							}
						});
					}
					if (stage == 3 || cancelled) {
						if (stage == 3)
							lockedOnTarget = true;
						sendDirections(true);
						to = null;
						stop();
						return;
					}
					final NewProjectile projectile = new NewProjectile(tile, to, 6147, 25, 25, 0, 35, 15, 0);
					World.sendProjectile(projectile);
					tile = to;
					time += projectile.getTime() / 335;
					stage++;
				}
			}
		}, 0, 0);
	}
	
	private final void setNextLocation() {
		for (int i = 0; i < 100; i++)
			if (withinBoundaries(to = getNextTile()))
				break;
	}
	
	private final void sendDirections(boolean reset) {
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (to != null) {
					for (Player p : instance.getPlayers()) {
						p.getPackets().sendGraphics(new Graphics(reset ? -1 : 2789, 0, 0, 4), new WorldTile(to.getX(), to.getY() + 1, to.getPlane()));
						p.getPackets().sendGraphics(new Graphics(reset ? -1 : 2789, 0, 0, 0), new WorldTile(to.getX(), to.getY() - 1, to.getPlane()));
						p.getPackets().sendGraphics(new Graphics(reset ? -1 : 2789, 0, 0, 2), new WorldTile(to.getX() - 1, to.getY(), to.getPlane()));
						p.getPackets().sendGraphics(new Graphics(reset ? -1 : 2789, 0, 0, 6), new WorldTile(to.getX() + 1, to.getY(), to.getPlane()));
					}
				} else
					stop();
			}
		}, 0, 3);
	}
	
	private final WorldTile getNextTile() {
		return new WorldTile(tile.getX() + Utils.random(-3, 4), tile.getY() + Utils.random(-3, 4), tile.getPlane());
	}
	
	final boolean withinBoundaries(final WorldTile tile) {
		if (tile.getDistance(this.tile) != 3)
			return false;
		final WorldTile min = instance.getWorldTile(21, 21);
		final WorldTile max = instance.getWorldTile(40, 40);
		return tile.getX() >= min.getX() && tile.getY() >= min.getY() && tile.getX() <= max.getX() && tile.getY() <= max.getY();
	}

}
