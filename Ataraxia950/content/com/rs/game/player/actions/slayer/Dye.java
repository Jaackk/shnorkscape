package com.rs.game.player.actions.slayer;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Kris | 2. okt 2018 : 19:52:05
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>
 */
@AllArgsConstructor
public enum Dye {

	RED(1763),
	YELLOW(1765),
	BLUE(1767),
	GREEN(1771);
	
	public static final Dye[] VALUES = values();
	@Getter private final int id;
}
