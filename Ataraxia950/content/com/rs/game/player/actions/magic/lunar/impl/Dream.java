package com.rs.game.player.actions.magic.lunar.impl;

import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.actions.DreamSpellAction;
import com.rs.game.player.actions.magic.lunar.DefaultSpell;

public class Dream implements DefaultSpell {

	@Override
	public int getId() {
		return 14849;
	}

	@Override
	public int getLevel() {
		return 79;
	}

	@Override
	public Item[] getRunes() {
		return new Item[] { new Item(ASTRAL_RUNE, 2), new Item(COSMIC_RUNE, 1), new Item(BODY_RUNE, 5) };
	}

	@Override
	public int getDelay() {
		return 3000;
	}

	@Override
	public boolean spellEffect(Player player) {
		if (player.isUnderCombat(6)) {
			player.getPackets().sendGameMessage("You can't cast dream until 10 seconds after the end of combat.");
			return false;
		}
		player.getActionManager().setAction(new DreamSpellAction());
		return true;
	}

}
