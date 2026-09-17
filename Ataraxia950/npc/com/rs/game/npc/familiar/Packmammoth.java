package com.rs.game.npc.familiar;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.actions.summoning.Summoning.Pouches;

public class Packmammoth extends Familiar {

	private static final long serialVersionUID = -5711658278098177504L;

	public Packmammoth(Player owner, Pouches pouch, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		super(owner, pouch, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
	}

	@Override
	public int getBOBSize() {
		return 30;
	}

	@Override
	public int getSpecialAmount() {
		return 20;
	}

	@Override
	public SpecialAttack getSpecialAttack() {
		return SpecialAttack.CLICK;
	}

	@Override
	public String getSpecialDescription() {
		return "Consumes any piece of food stored and heals you the health it gains";
	}

	@Override
	public String getSpecialName() {
		return "Mammoth feast";
	}

	@Override
	public boolean submitSpecial(Object object) {
		return false;
	}

}
