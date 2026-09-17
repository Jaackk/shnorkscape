package com.rs.game.player.actions.magic;

import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.player.Player;

public interface SpellEffect {

	void spellEffect(final Player player, final Entity target, final int damage, final Graphics mage_hit_gfx);
	
}
