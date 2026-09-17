package com.rs.game.activities.aod.npc;

import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activities.aod.AngelOfDeath;
import com.rs.game.npc.NPC;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/**
 * Handles the Crystal NPC. Four crystals are placed within Pillars of each corner and they must
 * be defeated in the same order as the respective Praesul were defeated in.
 * @author Kris | 30. sept 2017 : 17:18.06
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public final class Crystal extends NPC {
	
	private static final long serialVersionUID = -926187040193588095L;

	public Crystal(final int id, final WorldTile tile, final AngelOfDeath instance) {
		super(id, tile, -1, true, true);
		this.instance = instance;
		setForceMultiArea(true);
		setCannotMove(true);
		instance.spawnNPC(this);
	}

	@Override
	public boolean isIntelligentRouteFinder() {
		return true;
	}
	
	private final AngelOfDeath instance;

	private static final Graphics EXPLOSION = new Graphics(3802);
	
	@Override
	public void sendDeath(final Entity source) {
		WorldTasksManager.schedule(new WorldTask() {
			private int ticks;
			@Override
			public void run() {
				switch(ticks++) {
				case 0:
					finish();
					instance.finishOrder();
					final WorldTile tile = Crystal.this.getMiddleWorldTile();
					instance.sendGraphics(EXPLOSION, new WorldTile(tile.getX(), tile.getY(), tile.getPlane() + 1));
					if (instance.getAvailableCrystal() == -1)
						instance.getNex().setVulnerability(true);
					break;
				case 2:
					switch(getId()) {
					case 24018:
						World.spawnObject(new WorldObject(100820, 10, 0, instance.getWorldTile(2828, 1804)));
						break;
					case 24019:
						World.spawnObject(new WorldObject(100820, 10, 0, instance.getWorldTile(2866, 1804)));
						break;
					case 24016:
						instance.getNex().setHeal(false);
						World.spawnObject(new WorldObject(100821, 10, 0, instance.getWorldTile(2828, 1842)));
						break;
					default:
						World.spawnObject(new WorldObject(100822, 10, 0, instance.getWorldTile(2866, 1842)));
						break;
					}
					stop();
					return;
				}
			}
		}, 0, 0);
	}
	
	@Override
	public int getCapDamage() {
		return 1000;
	}
}
