package com.rs.game.player.content.packs.protean;

import com.rs.cache.loaders.ItemDefinitions;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Kris | 2. okt 2018 : 06:41:58
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>
 */
@AllArgsConstructor
public enum ProteanReward {

	PROTEAN_BAR(31350),
	//PROTEAN_COG(39401),
	PROTEAN_HIDE(33740),
	PROTEAN_LOGS(34528),
	//PROTEAN_MEMORY(37363),
	PROTEAN_PLANK(30037),
	PROTEAN_TRAP(32337);
	
	public static final ProteanReward[] VALUES = values();
	
	@Getter private final int id;
	
	@Override
	public String toString() {
		return ItemDefinitions.getItemDefinitions(id).getName();
	}
	
}
