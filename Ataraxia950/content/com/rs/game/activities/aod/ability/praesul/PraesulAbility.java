package com.rs.game.activities.aod.ability.praesul;

import com.rs.game.activities.aod.AngelOfDeath;
import com.rs.game.activities.aod.npc.PraesulMinion;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;

/**
 * An abstract class for the Praesul abilities.
 * @author Kris | 30. sept 2017 : 17:06.12
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public abstract class PraesulAbility extends WorldTask {
	
	public PraesulAbility(final Player player, final PraesulMinion praesul, final AngelOfDeath instance) {
		this.player = player;
		this.praesul = praesul;
		this.instance = instance;
	}

	public static final int MAX = 112;
	protected PraesulMinion praesul;
	protected AngelOfDeath instance;
	protected Player player;
	protected boolean cancelled;
	public abstract int getDuration();
	public abstract int getAdrenaline();
	public abstract int getCooldown();

	public boolean cancel() {
		return cancelled || praesul == null || praesul.isDead() || praesul.hasFinished() || player != null && player.isDead() || player != null && player.hasFinished() || player != null && !instance.getPlayers().contains(player) || praesul.getAbility() != this;
	}

	public void setCancelled() {
		cancelled = true;
	}

	public Player getPlayer() {
		return player;
	}
}
