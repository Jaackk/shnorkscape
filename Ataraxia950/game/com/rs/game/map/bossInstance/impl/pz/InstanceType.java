package com.rs.game.map.bossInstance.impl.pz;

import static com.rs.game.map.bossInstance.impl.pz.InstanceConstants.DI_SPAWN;
import static com.rs.game.map.bossInstance.impl.pz.InstanceConstants.HEXA_SPAWN;
import static com.rs.game.map.bossInstance.impl.pz.InstanceConstants.MONO_SPAWN;
import static com.rs.game.map.bossInstance.impl.pz.InstanceConstants.TETRA_SPAWN;
import static com.rs.game.map.bossInstance.impl.pz.InstanceConstants.TRI_SPAWN;
import static com.rs.game.map.bossInstance.impl.pz.InstanceConstants.DECCA_SPAWN;

import com.rs.game.WorldTile;
import com.rs.game.map.bossInstance.impl.pz.npcs.InstancedAirut;
import com.rs.game.map.bossInstance.impl.pz.npcs.InstancedAutomaton;
import com.rs.game.map.bossInstance.impl.pz.npcs.InstancedCelestialDragon;
import com.rs.game.map.bossInstance.impl.pz.npcs.InstancedDagannothKing;
import com.rs.game.map.bossInstance.impl.pz.npcs.InstancedGemstoneDragon;
import com.rs.game.map.bossInstance.impl.pz.npcs.InstancedKalgerionDemon;
import com.rs.game.map.bossInstance.impl.pz.npcs.InstancedKalphiteQueen;
import com.rs.game.map.bossInstance.impl.pz.npcs.InstancedKingBlackDragon;
import com.rs.game.map.bossInstance.impl.pz.npcs.InstancedMonster;
import com.rs.game.map.bossInstance.impl.pz.npcs.InstancedRuneDragon;
import com.rs.game.map.bossInstance.impl.pz.npcs.InstancedTormentedDemon;
import com.rs.game.npc.NPC;
import com.rs.utils.Utils;

import lombok.Getter;


/**
 * @author Kris | 9. sept 2018 : 21:30:09
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>
 */
public enum InstanceType {

//	GENERAL_GRAARDOR(6260, InstancedGeneralGraardor.class, MONO_SPAWN),
//	KREE_ARRA(6222, InstancedKreeArra.class, MONO_SPAWN) {
//		@Override
//		public String toString() {
//			return "Kree'Arra";
//		}
//	},
//	KRIL_TSUTSAROTH(6203, InstancedKrilTsutsaroth.class, MONO_SPAWN) {
//		@Override
//		public String toString() {
//			return "K'ril Tsutsaroth";
//		}
//	},
//	COMMANDER_ZILYANA(6247, InstancedCommanderZilyana.class, MONO_SPAWN),
	KALPHITE_QUEEN(1158, InstancedKalphiteQueen.class, MONO_SPAWN),
	KING_BLACK_DRAGON(50, InstancedKingBlackDragon.class, MONO_SPAWN),
	CHAOS_ELEMENTAL(3200, InstancedMonster.class, MONO_SPAWN),
	DAGANNOTH_REX(2883, InstancedDagannothKing.class, MONO_SPAWN),
	DAGANNOTH_PRIME(2882, InstancedDagannothKing.class, MONO_SPAWN),
	DAGANNOTH_SUPREME(2881, InstancedDagannothKing.class, MONO_SPAWN),
	TORMENTED_DEMON(8349, InstancedTormentedDemon.class, MONO_SPAWN),
	GANODERMIC_BEASTS(14696, InstancedMonster.class, HEXA_SPAWN),
	GANODERMIC_RUNTS(14698, InstancedMonster.class, HEXA_SPAWN),
	DARK_BEASTS(2783, InstancedMonster.class, HEXA_SPAWN),
	ABYSSAL_DEMONS(1615, InstancedMonster.class, HEXA_SPAWN),
	GREATER_DEMONS(83, InstancedMonster.class, HEXA_SPAWN),
	GARGOYLE(1610, InstancedMonster.class, HEXA_SPAWN),
	RORARIUS(17144, InstancedMonster.class, HEXA_SPAWN),
    HELLHOUNDS(49, InstancedMonster.class, HEXA_SPAWN),
    EDIMMU(20290, InstancedMonster.class, HEXA_SPAWN),
	FROST_DRAGONS(51, InstancedMonster.class, TRI_SPAWN),
	RUNE_DRAGONS(21136, InstancedRuneDragon.class, TRI_SPAWN),
	ADAMANT_DRAGONS(21135, InstancedMonster.class, TRI_SPAWN),
	MITHRIL_DRAGONS(5363, InstancedMonster.class, TRI_SPAWN),
	STEEL_DRAGONS(1592, InstancedMonster.class, TETRA_SPAWN),
	IRON_DRAGONS(1591, InstancedMonster.class, TETRA_SPAWN),
	BLUE_DRAGONS(4681, InstancedMonster.class, TETRA_SPAWN),
	GREEN_DRAGONS(4677, InstancedMonster.class, TETRA_SPAWN),
	CELESTIAL_DRAGONS(19109, InstancedCelestialDragon.class, DI_SPAWN),
	AUTOMATON_TRACER(16906, InstancedAutomaton.class, DI_SPAWN),
	AUTOMATON_GUARDIAN(16907, InstancedAutomaton.class, DI_SPAWN),
	AUTOMATON_GENERATOR(16905, InstancedAutomaton.class, DI_SPAWN),
	HYDRIX_DRAGON(24172, InstancedGemstoneDragon.class, DI_SPAWN),
	ONYX_DRAGON(24171, InstancedGemstoneDragon.class, DI_SPAWN),
	DRAGONSTONE_DRAGON(24170, InstancedGemstoneDragon.class, DI_SPAWN),
	AIRUTS(18621, InstancedAirut.class, TETRA_SPAWN),
	NECHRYAEL(1613, InstancedMonster.class, TETRA_SPAWN),
	KALGERION_DEMONS(14973, InstancedKalgerionDemon.class, DI_SPAWN) {
		private final int[] ids = new int[] { 14973, 14974, 14975, 14976, 14977 };

		@Override
		public int getId() {
			return ids[Utils.random(ids.length)];
		}
	},
	WATERFIENDS(5361, InstancedMonster.class, DECCA_SPAWN);
/*
	LEGIOS(17149, InstancedLegios.class, MONO_SPAWN) {
		private final int[] ids = new int[] { 17149, 17150, 17151, 17152, 17153, 17154 };

		@Override
		public int getId() {
			return ids[Utils.random(ids.length)];
		}
	};
*/
	@Getter private final int id;
	@Getter private final Class<? extends NPC> npc;
	@Getter private final WorldTile[] spawnLocations;
	
	public static final InstanceType[] VALUES = values();
	
	InstanceType(final int id, final Class<? extends NPC> npc, final WorldTile... spawnLocations) {
		this.id = id;
		this.npc = npc;
		this.spawnLocations = spawnLocations;
	}
	
	@Override
	public String toString() {
		return Utils.formatPlayerNameForDisplay(name());
	}
	
}
