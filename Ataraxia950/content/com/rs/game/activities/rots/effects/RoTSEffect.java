package com.rs.game.activities.rots.effects;

import com.rs.game.activities.rots.RiseOfTheSix;
import com.rs.game.activities.rots.npcs.RiseOfTheSixNPC;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;

/**
 * Contains the special attacks of the wights.
 * @author Kris | 3. sept 2017 : 23:35.22
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public abstract class RoTSEffect {

	public RoTSEffect(int duration, RiseOfTheSixNPC npc, Player player) {
		this.duration = duration;
		this.npc = npc;
		this.player = player;
		this.instance = npc.getInstance();
	}
	
	protected int duration;
	protected RiseOfTheSixNPC npc;
	protected RiseOfTheSix instance;
	protected Player player;
	protected boolean cancelled;
	
	public abstract void start();
	
	public boolean cancel() {
		return cancelled || npc == null || npc.isDead() || npc.hasFinished() 
				|| player != null && player.isDead() || player != null && player.hasFinished()
				|| player != null && !instance.getPlayers().contains(player) || npc.getEffect() != this;
	}
	
	public void setCancelled() {
		cancelled = true;
	}
	
	public int getDuration() {
		return duration;
	}
	
	public NPC getNPC() {
		return npc;
	}
	
	public Player getPlayer() {
		return player;
	}
	
}
