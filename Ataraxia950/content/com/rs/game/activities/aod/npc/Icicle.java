package com.rs.game.activities.aod.npc;

import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.activities.aod.ability.IcePrison;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;

/**
 * Handles the Icicle NPC that the player gets encased in during a certain ability.
 * Players must defeat the icicle before the timer runs out, otherwise
 * it will collapse and deal unblockable 5 stacks of 200 damage each to the targeted
 * player with 2 tick intervals.
 * @author Kris | 30. sept 2017 : 17:19.03
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public final class Icicle extends NPC {

	private static final long serialVersionUID = 2966451470009222044L;

	public Icicle(final Player player, final WorldTile tile) {
		super(24014, tile, -1, true, true);
		this.player = player;
		setForceMultiArea(true);
		setCannotMove(true);
	}

	@Override
	public boolean isIntelligentRouteFinder() {
		return true;
	}
	
	private final Player player;
	
	@Override
	public void processNPC() {
		super.processNPC();
		addHitBars();
	}
	
	@Override
	public void sendDeath(final Entity source) {
		finish();
		player.setNextAnimation(IcePrison.STANDING_UP);
		player.getInterfaceManager().closeOverlay(true);
		player.setFreezeDelay(0);
	}
	
	public Player getPlayer() {
		return player;
	}
	
	@Override
	public int getCapDamage() {
		return 1000;
	}

}
