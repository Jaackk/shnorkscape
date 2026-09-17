package com.rs.game.player.content;

import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

import java.io.Serializable;

public class GemstoneArmour implements Serializable {

	private static final long serialVersionUID = 1756188594142318211L;
	
	private transient Player player;
	private final int[] charges;
	private int attuned;
	private transient long lastEffect, hydrixSpecial;
	
	private static final Graphics GFX = new Graphics(94);
	
	public GemstoneArmour() {
		charges = new int[3];
		attuned = -1;
	}
	
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public void setAttuned(int type) {
		attuned = type;
	}
	
	public void addCharges(int type, int amount) {
		charges[type] += amount;
	}
	
	public int getCharges(int type) {
		return charges[type];
	}
	
	public int getAttuned() {
		return attuned;
	}
	
	public long getTime() {
		return lastEffect;
	}
	
	public boolean useDragonfireSpecial() {
		if (attuned != 0)
			return false;
		if (Utils.currentTimeMillis() < lastEffect)
			return false;
		if (charges[0] == 0)
			return false;
		final int delay = getDelay();
		if (delay == 0)
			return false;
		charges[0]--;
		player.setNextGraphics(GFX);
		lastEffect = Utils.currentTimeMillis() + delay;
		return true;
	}
	
	public void useOnyxSpecial(final int damage) {
		if (attuned != 1)
			return;
		if (Utils.currentTimeMillis() < lastEffect)
			return;
		if (charges[1] == 0)
			return;
		final int delay = getDelay();
		if (delay == 0)
			return;
		charges[1]--;
		player.setNextGraphics(GFX);
		lastEffect = Utils.currentTimeMillis() + delay;
		player.heal((int) (damage * 0.25));
	}
	
	public void useHydrixSpecial() {
		if (attuned != 2)
			return;
		if (Utils.currentTimeMillis() < hydrixSpecial) {
			player.getCombatDefinitions().restoreSpecialAttack(1);
			return;
		}
		if (Utils.currentTimeMillis() < lastEffect)
			return;
		if (charges[2] == 0)
			return;
		final int delay = getDelay();
		if (delay == 0)
			return;
		charges[2]--;
		player.setNextGraphics(GFX);
		lastEffect = Utils.currentTimeMillis() + delay;
		hydrixSpecial = Utils.currentTimeMillis() + 15000;
		player.getCombatDefinitions().restoreSpecialAttack(1);
	}
	
	public int getDelay() {
		int pieces = 0;
		for (Item i : player.getEquipment().getItems().getItems()) {
			if (i == null)
				continue;
			if (i.getId() >= 39893 && i.getId() <= 39901)
				pieces++;
		}
		switch(pieces) {
		case 5:
			return 17000;
		case 4:
			return 20000;
		case 3:
			return 23000;
			default:
				return 0;
		}
	}
	
}
