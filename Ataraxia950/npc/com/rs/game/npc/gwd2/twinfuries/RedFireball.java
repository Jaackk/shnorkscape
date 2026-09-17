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

public class RedFireball extends TwinSpecialAttack {

	protected final TwinFuriesInstance instance;
	protected WorldTile tile;
	private boolean forceTarget;
	private Entity target;
	
	public void setForceTarget() {
		forceTarget = true;
	}
	
	public RedFireball(NPC npc, Entity entity) {
		super(npc, entity);
		instance = ((Avaryss) npc).getInstance();
	}
	
	public RedFireball(NPC npc, Entity entity, Entity target) {
		super(npc, entity);
		instance = ((Avaryss) npc).getInstance();
		this.target = target;
	}
	
	private WorldTile to;
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
							p.getPackets().sendGraphics(new Graphics(6056), tile);
							if (p.withinDistance(tile, 2))
								p.applyHit(new Hit(npc, Utils.random(150, 680), HitLook.REGULAR_DAMAGE));
						});
					}
					if (forceTarget) {
						WorldTasksManager.schedule(new WorldTask() {
							private int ticks;
							private int impact;
							@Override
							public void run() {
								if (ticks == 0) {
									if (target == null || !target.withinDistance(instance.getAvaryss(), 50)) {
										if (instance.getPlayers().size() == 0) {
											stop();
											return;
										}
										target = instance.getPlayers().get(Utils.random(instance.getPlayers().size()));
									}
									final NewProjectile projectile = new NewProjectile(tile, target, 6151, 25, 25, 0, 35, 15, 0);
									World.sendProjectile(projectile);
									impact = projectile.getTime() / 335;
								}
								if (ticks == impact) {
									target.applyHit(new Hit(instance.getAvaryss(), Utils.random(200, 680), HitLook.REGULAR_DAMAGE));
									stop();
								}
								ticks++;
							}
							
						}, 0, 0);
					}
					if (stage >= 5 && target == null || forceTarget) {
						sendDirections(true);
						to = null;
						stop();
						return;
					}
					final NewProjectile projectile = new NewProjectile(tile, to, 6151, 25, 25, 0, 35, 15, 0);
					World.sendProjectile(projectile);
					tile = to;
					time += projectile.getTime() / 335;
					stage++;
				}
			}
		}, 0, 0);
	}
	
	private final void setNextLocation() {
		for (int i = 0; i < 100; i++) {
			if (withinBoundaries(to = getNextTile()))
				break;
		}
	}
	
	private final void sendDirections(boolean reset) {
		WorldTasksManager.schedule(new WorldTask() {
			private int ticks;
			@Override
			public void run() {
				if (to != null && ticks < 50) {
					for (Player p : instance.getPlayers()) {
						p.getPackets().sendGraphics(new Graphics(reset ? -1 : 2789, 0, 0, 4), new WorldTile(to.getX(), to.getY() + 1, to.getPlane()));
						p.getPackets().sendGraphics(new Graphics(reset ? -1 : 2789, 0, 0, 0), new WorldTile(to.getX(), to.getY() - 1, to.getPlane()));
						p.getPackets().sendGraphics(new Graphics(reset ? -1 : 2789, 0, 0, 2), new WorldTile(to.getX() - 1, to.getY(), to.getPlane()));
						p.getPackets().sendGraphics(new Graphics(reset ? -1 : 2789, 0, 0, 6), new WorldTile(to.getX() + 1, to.getY(), to.getPlane()));
					}
				} else
					stop();
				ticks++;
			}
		}, 0, 3);
	}
	
	final WorldTile getNextTile() {
		return new WorldTile(tile.getX() + Utils.random(-10, 10), tile.getY() + Utils.random(-10, 10), tile.getPlane());
	}
	
	final boolean withinBoundaries(final WorldTile tile) {
		final WorldTile min = instance.getWorldTile(21, 21);
		final WorldTile max = instance.getWorldTile(40, 40);
		return tile.getX() >= min.getX() && tile.getY() >= min.getY() && tile.getX() <= max.getX() && tile.getY() <= max.getY();
	}

}
