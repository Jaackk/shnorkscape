package com.rs.game.player.actions.magic.lunar;

import com.rs.game.WorldObject;
import com.rs.game.player.Player;

public interface ObjectSpell extends Spell {

	boolean spellEffect(final Player player, final WorldObject object);
	
}
