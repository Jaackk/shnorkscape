package com.rs.game.map.bossInstance.impl.pz.npcs;

import com.rs.game.WorldTile;
import com.rs.game.map.bossInstance.impl.pz.InstancedNPC;
import com.rs.game.map.bossInstance.impl.pz.PZInstance;
import com.rs.game.npc.others.DagannothKing;

/**
 * @author Kris | 9. sept 2018 : 22:48:14
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>
 */
public class InstancedDagannothKing extends DagannothKing implements InstancedNPC {

	private static final long serialVersionUID = 4307018246585406895L;
	private final PZInstance area;
	public InstancedDagannothKing(final int id, final WorldTile tile, PZInstance area) {
		super(id, tile, -1, true, false, null);
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
