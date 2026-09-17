package com.rs.game.player.actions.magic.lunar;

import com.rs.game.item.Item;

public interface Spell {
	
	int EARTH_RUNE = 557;
	int ASTRAL_RUNE = 9075;
	int AIR_RUNE = 556;
	int WATER_RUNE = 555;
	int FIRE_RUNE = 554;
	int MIND_RUNE = 558;
	int NATURE_RUNE = 561;
	int CHAOS_RUNE = 562;
	int DEATH_RUNE = 560;
	int BLOOD_RUNE = 565;
	int SOUL_RUNE = 566;
	int COSMIC_RUNE = 564;
	int BODY_RUNE = 559;
	int LAW_RUNE = 563;
	
	int getId();
	int getLevel();
	Item[] getRunes();
	int getDelay();
	
}
