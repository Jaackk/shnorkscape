package com.rs.game.npc.familiar;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.actions.summoning.Summoning.Pouches;

public class Shadownihil extends Familiar {

	private static final long serialVersionUID = -2774988688042119538L;

	public Shadownihil(Player owner, Pouches pouch, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
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
		return "An attack which deals up to 77 magic damage & stuns the opponent.";
	}

	@Override
	public String getSpecialName() {
		return "Annihilate";
	}

	@Override
	public boolean submitSpecial(Object object) {
		return false;
	}

}
