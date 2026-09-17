package com.rs.game.activities.aod.ability;

import com.rs.game.activities.aod.AngelOfDeath;
import com.rs.game.activities.aod.npc.AoDNex;
import com.rs.game.npc.NPC;
import com.rs.game.tasks.WorldTask;

/**
 * An abstract class for Angel of Death abilities.
 * @author Kris | 30. sept 2017 : 16:43.11
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public abstract class AoDAbility extends WorldTask {

	public AoDAbility(final AoDNex nex, final AngelOfDeath instance) {
		this.nex = nex;
		this.instance = instance;
	}
	
	protected AoDNex nex;
	protected AngelOfDeath instance;
	protected boolean cancelled;
	
	public boolean cancel() {
		return cancelled || nex == null || nex.isDead() || nex.hasFinished()  || nex.getAbility() != this;
	}
	
	public void setCancelled() {
		cancelled = true;
	}
	
	public NPC getNPC() {
		return nex;
	}
	
}
