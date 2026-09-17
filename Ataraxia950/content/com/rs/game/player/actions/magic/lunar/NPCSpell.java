package com.rs.game.player.actions.magic.lunar;

import com.rs.game.npc.NPC;
import com.rs.game.player.Player;

public interface NPCSpell extends Spell {

	boolean spellEffect(final Player player, final NPC npc);
	
}
