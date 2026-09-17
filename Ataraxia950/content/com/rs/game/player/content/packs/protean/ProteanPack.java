package com.rs.game.player.content.packs.protean;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Kris | 2. okt 2018 : 06:37:53
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>
 */
@AllArgsConstructor
public enum ProteanPack {

	MINI_PROTEAN_PACK(36813, 10),
	SMALL_PROTEAN_PACK(34023, 75),
	MEDIUM_PROTEAN_PACK(34024, 150),
	LARGE_PROTEAN_PACK(34025, 300);
	
	public static final ProteanPack[] VALUES = values();
	
	@Getter private final int id, amount;
	
}
