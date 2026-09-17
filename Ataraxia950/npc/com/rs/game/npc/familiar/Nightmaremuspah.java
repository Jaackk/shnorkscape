package com.rs.game.npc.familiar;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.summoning.Summoning.Pouches;

public class Nightmaremuspah extends Familiar {

	private static final long serialVersionUID = 6432162246942825101L;

	public Nightmaremuspah(Player owner, Pouches pouch, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		super(owner, pouch, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
	}

	@Override
	public int getBOBSize() {
		return 30;
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
		return "Grants a 3% boost to finding enriched memories & 5% chance to harvest 4 times the energy while harvesting wisps.";
	}

	@Override
	public String getSpecialName() {
		return "Siphon self";
	}
	
	private static final int[] ENERGY = new int[] {
			29189, 29190, 29191, 29192, 29193, 29195, 29196, 31312, 29197, 29198, 29323, 29324
	};

	@Override
	public boolean submitSpecial(Object object) {
		Player owner = this.getOwner();
		for (int i = ENERGY.length; i >= 0; i--) {
			if (owner.getSkills().getLevel(Skills.DIVINATION) >= (i == 0 ? 1 : i > 6 ? 70 + ((i - 7) * 5) : i * 10)) {
				owner.getFamiliar().getBob().addItem(new Item(ENERGY[i - 1], 1));
				break;
			}
		}
		this.setNextGraphics(new Graphics(4833));
		this.setNextAnimation(new Animation(22980));
		return false;
	}

}
