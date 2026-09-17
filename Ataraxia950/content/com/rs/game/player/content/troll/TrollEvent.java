package com.rs.game.player.content.troll;

import com.rs.game.Region;
import com.rs.game.World;
import com.rs.game.player.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class TrollEvent {

	private final Player owner;
	private final Region region;
	private final List<Player> targets = new ArrayList<Player>();
	
	public TrollEvent(Player owner) {
		this.owner = owner;
		this.region = World.getRegion(owner.getRegionId());
		doEvent();
	}
	
	public void doEvent() {
		List<Integer> playerIndexes = getRegion().getPlayerIndexes();
		if (playerIndexes != null) {
			for (int playerIndex : playerIndexes) {
				Player player = World.getPlayers().get(playerIndex);
				if (player == null || player.isDead() || player.hasFinished() ||
						!player.isRunning() || player.getAppearence().isHidden())
					continue;
				targets.add(player);
			}
		}
		for (Player target : targets) {
			if (target != null && target != owner)
				effect(target);
		}
	}
	
	public abstract void effect(Player player);
	
	public Player getOwner() {
		return owner;
	}
	
	public Region getRegion() {
		return region;
	}
	
}
