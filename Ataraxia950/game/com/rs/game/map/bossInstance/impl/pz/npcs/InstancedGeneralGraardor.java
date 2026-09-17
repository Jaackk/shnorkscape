package com.rs.game.map.bossInstance.impl.pz.npcs;

import com.rs.game.WorldTile;
import com.rs.game.map.bossInstance.impl.pz.InstancedNPC;
import com.rs.game.map.bossInstance.impl.pz.PZInstance;
import com.rs.game.npc.godwars.bandos.GeneralGraardor;

/**
 * @author Kris | 9. sept 2018 : 21:49:24
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>
 */
public class InstancedGeneralGraardor extends GeneralGraardor implements InstancedNPC {

	private static final long serialVersionUID = 1458414356718941751L;
	private final PZInstance area;
	
	public InstancedGeneralGraardor(final int id, final WorldTile tile, PZInstance area) {
		super(id, tile, -1, true, false, false);
		setForceMultiArea(true);
		this.area = area;
	}

	@Override
	public boolean isHardMode() {
		return false;
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
