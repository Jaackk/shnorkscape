package com.rs.game.npc.familiar;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.actions.summoning.Summoning.Pouches;

public class Lesserdemonbrawler extends Familiar {

	private static final long serialVersionUID = 8260110864933220396L;

	public Lesserdemonbrawler(Player owner, Pouches pouch, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		super(owner, pouch, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
	}

	@Override
	public int getBOBSize() {
		return 0;
	}

	@Override
	public int getSpecialAmount() {
		return 9;
	}

	@Override
	public SpecialAttack getSpecialAttack() {
		return SpecialAttack.ENTITY;
	}

	@Override
	public String getSpecialDescription() {
		return "Hits all surrounding targets for up to 107 magic damage.";
	}

	@Override
	public String getSpecialName() {
		return "Ring of Fire";
	}

	@Override
	public boolean submitSpecial(Object object) {
		return true;
	}

}
