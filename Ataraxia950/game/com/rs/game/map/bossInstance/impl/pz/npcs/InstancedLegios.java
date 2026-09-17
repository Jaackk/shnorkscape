package com.rs.game.map.bossInstance.impl.pz.npcs;

import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.map.bossInstance.impl.pz.InstancedNPC;
import com.rs.game.map.bossInstance.impl.pz.PZInstance;
import com.rs.game.npc.others.Legios;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Dec 12, 2018.
 */
public class InstancedLegios extends Legios implements InstancedNPC {

	/**
	 * 
	 */
	private static final long serialVersionUID = -618483098228450319L;
    private final PZInstance area;

	/**
	 * Constructs a new class.
	 * @param id
	 * @param tile
	 * @param mapAreaNameHash
	 * @param canBeAttackFromOutOfArea
	 */
	public InstancedLegios(final int id, final WorldTile tile, PZInstance area) {
		super(id, tile, null);
		setForceMultiArea(true);
		this.area = area;
	}
	
	@Override
	protected boolean isToleranceAffected() {
		return false;
	}


	@Override
	public void setRespawnTask() {
		InstancedNPC.super.setRespawnTask(area);
	}

    @Override
    public void spawnLineOfAscension(boolean vertical) {
        WorldTile center = getCombat().getTarget();
        WorldTile min = area.getTile(new WorldTile(3028, 6036, 0));
        WorldTile max = area.getTile(new WorldTile(3051, 6059, 0));
        for (int x = vertical ? min.getX() : center.getX(); x <= (vertical ? max.getX() : center.getX()); x++) {
            for (int y = !vertical ? min.getY() : center.getY(); y <= (!vertical ? max.getY() : center.getY()); y++) {
                WorldTile checkTile = new WorldTile(x, y, getPlane());
                WorldObject object = null;
                int rot = vertical ? 1 : 0;
                WorldObject existing = World.getObjectWithId(checkTile, 84675);
                if (existing != null && existing.getRotation() != rot) {
                    object = new WorldObject(84676, 4, 0, checkTile);
                } else {
                    object = new WorldObject(84675, 4, rot, checkTile);
                }
                if (World.containsObjectWithId(checkTile, 84676))
                    object = null;
                if (object != null) {
                    World.spawnObject(object);
                    getLineOfAscension().add(object);
                }
            }
        }
    }
	
	

}
