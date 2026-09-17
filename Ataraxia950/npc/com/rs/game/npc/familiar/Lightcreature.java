package com.rs.game.npc.familiar;

import com.rs.game.Graphics;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.actions.summoning.Summoning.Pouches;
import com.rs.utils.Utils;

public class Lightcreature extends Familiar {

	private static final long serialVersionUID = 2753794038834971169L;

	public Lightcreature(Player owner, Pouches pouch, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		super(owner, pouch, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
	}

	@Override
	public int getBOBSize() {
		return 0;
	}

	@Override
	public int getSpecialAmount() {
		return 10;
	}

	@Override
	public SpecialAttack getSpecialAttack() {
		return SpecialAttack.CLICK;
	}

	@Override
	public String getSpecialDescription() {
		return "Instant memory conversion chance increased to 50%.";
	}

	@Override
	public String getSpecialName() {
		return "Enlightenment";
	}

	@Override
	public boolean submitSpecial(Object object) {
		this.getOwner().setNextGraphics(new Graphics(1368));
		this.getOwner().enlightenment = Utils.currentTimeMillis() + 60 * 60 * 1000;
		this.getOwner().sendMessage("The enlightenment grants you a 50% instant memory conversion boost for six minutes.");
		return false;
	}

}
