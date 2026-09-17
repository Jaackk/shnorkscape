package com.rs.game.map.bossInstance.impl.pz.npcs;

import com.rs.game.WorldTile;
import com.rs.game.map.bossInstance.impl.pz.InstancedNPC;
import com.rs.game.map.bossInstance.impl.pz.PZInstance;
import com.rs.game.npc.dragons.CelestialDragonB;

/**
 * @author Kris | 9. sept 2018 : 22:52:59
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>
 */
public class InstancedCelestialDragon extends CelestialDragonB implements InstancedNPC {

	private static final long serialVersionUID = 7103272140718583665L;
	private final PZInstance area;
	public InstancedCelestialDragon(final int id, final WorldTile tile,PZInstance area) {
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
