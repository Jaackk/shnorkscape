package com.rs.game.player.actions.slayer;

import com.rs.cache.loaders.NPCDefinitions;
import com.rs.game.player.actions.slayer.elite.BasicEliteNPC;
import com.rs.game.player.actions.slayer.elite.EliteAnkou;
import com.rs.game.player.actions.slayer.elite.EliteNPC;
import com.rs.game.player.actions.slayer.elite.HarpieBugSwarmNPC;
import com.rs.game.player.actions.slayer.elite.SlasherDemon;
import com.rs.game.player.actions.slayer.elite.Supercow;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Kris | 3. okt 2018 : 14:34:40
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>
 */
@AllArgsConstructor
public enum EliteMonster {
	
	ABYSSAL_DEMON(21502, BasicEliteNPC.class),
	ANKOU(21433, EliteAnkou.class),
	AQUANITE(21499, BasicEliteNPC.class),
	CAVE_HORROR(21432, BasicEliteNPC.class),//no effect because the base npc doesnt exist.
	COW(21497, Supercow.class) { @Override public String toString() { return "Cow"; } },
	DARK_BEAST(21498, BasicEliteNPC.class),//TODO: Do not know how the screech attack looks.
	DUST_DEVIL(21503, BasicEliteNPC.class),
	EARTH_WARRIOR(21495, BasicEliteNPC.class),
	GREATER_DEMON(21496, BasicEliteNPC.class),
	HARPIE_BUG_SWARM(21505, HarpieBugSwarmNPC.class),
	HOBGOBLIN(21494, BasicEliteNPC.class),
	INFERNAL_MAGE(21504, BasicEliteNPC.class),
	NECHRYAEL(21501, BasicEliteNPC.class),
	SLASHER_DEMON(21995, SlasherDemon.class) { @Override public String toString() { return "Ripper demon"; } },
	WATERFIEND(21500, BasicEliteNPC.class),
	WYVERN(21992, BasicEliteNPC.class);
	
	/**
	 * TODO: Edimmu, soulgazer, rune dragon, cyclops, lava strykewyrm:
	 * These work completely differently from actual elite monsters.
	 */
	
	private static final EliteMonster[] VALUES = values();
	public static final Map<String, EliteMonster> MAP = new HashMap<String, EliteMonster>(VALUES.length);
	
	static {
		for (int i = VALUES.length - 1; i >= 0; i--) {
			val value = VALUES[i];
			MAP.put(value.toString(), value);
		}
	}
	
	@Getter private final int id;
	@Getter private final Class<? extends EliteNPC> clazz;
	
	@Override
	public String toString() {
		return NPCDefinitions.getNPCDefinitions(id).getName();
	}
}
