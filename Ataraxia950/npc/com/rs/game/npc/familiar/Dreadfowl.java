package com.rs.game.npc.familiar;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.actions.summoning.Summoning.Pouches;

public class Dreadfowl extends Familiar {

	private static final long serialVersionUID = 5094107896935668511L;

		public Dreadfowl(Player owner, Pouches pouch, WorldTile tile,
	                      int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
	        super(owner, pouch, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
	    }

	    @Override
	    public String getSpecialName() {
	        return "Dreadfowl strike";
	    }

	    @Override
	    public String getSpecialDescription() {
	        return "A long ranged magic attack, dealing a maximum of 51 damage.";
	    }

	    @Override
	    public int getBOBSize() {
	        return 0;
	    }

	    @Override
	    public int getSpecialAmount() {
	        return 3;
	    }

	    @Override
	    public SpecialAttack getSpecialAttack() {
	        return SpecialAttack.ENTITY;
	    }

	    @Override
	    public boolean submitSpecial(Object object) {
	        return true;
	    }
	}

