package com.rs.game.player.actions.magic.lunar;

import com.rs.game.item.Item;
import com.rs.game.player.Player;

public interface ItemSpell extends Spell {

	boolean spellEffect(final Player player, final Item item);
	
}
