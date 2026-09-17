package com.rs.game.player.actions.magic.lunar;

import com.rs.game.player.Player;

public interface PlayerSpell extends Spell {

	boolean spellEffect(final Player player, final Player target);
	
}
