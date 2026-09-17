package com.rs.game.map.bossInstance.impl.pz.npcs;

import com.rs.game.WorldTile;
import com.rs.game.map.bossInstance.impl.pz.InstancedNPC;
import com.rs.game.map.bossInstance.impl.pz.PZInstance;
import com.rs.game.npc.airut.Airut;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Nov 28, 2018.
 */
public class InstancedAirut extends Airut implements InstancedNPC {

	private static final long serialVersionUID = -1123959360002971348L;
    private final PZInstance area;
    
	public InstancedAirut(final int id, final WorldTile tile, PZInstance area) {
		super(id, tile);
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

}
