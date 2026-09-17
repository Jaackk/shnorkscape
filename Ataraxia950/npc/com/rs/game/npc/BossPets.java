package com.rs.game.npc;

import com.google.common.collect.ImmutableMap;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

/**
 * NOTE TO DEVELOPERS= this class is not used for bosspets, idk why not.
 * Only being used for the ;;addbosspets command for the dropcollection. Values may be inaccurate, so check before using this code.
 * #Shnek
 */
public enum BossPets {

	KREE_ARRA(new int[] { 33804, 5000, 1000, 3 }),
	COMMANDER_ZILYANA(new int[] { 33807, 5000, 1000, 4 }),
	GENERAL_GRAARDOR(new int[] { 33806, 5000, 1000, 1 }),
	KRIL_TSUTSAROTH(new int[] { 33805, 5000, 1000, 2 }),
	NEX(new int[] { 33808, 2000, 400, 5 }),
	GIANT_MOLE(new int[] { 33813, 2500, 500, 89 }),
	JAD_PET(new int[] { 21512, 50, 100, 95 }),
	HAR_AKEN(new int[] { 33814, 50, 100, 125 }),
	KING_BLACK_DRAGON(new int[] { 33818, 2500, 500, 8 }),
	QUEEN_BLACK_DRAGON(new int[] { 33825, 2500, 500, 7 }),
	KALPHITE_QUEEN(new int[] { 33816, 2500, 500, 54, 33817, 2500, 500, 54 }),
	KALPHITE_KING(new int[] { 33815, 2000, 400, 90 }),
	CORPOREAL_BEAST(new int[] { 33812, 2500, 500, 6 }),
	CHAOS_ELEMENTAL(new int[] { 33811, 2500, 500, 10 }),
	LEGIO_PRIMUS(new int[] { 33819, 1000, 1200, 83 }),
	LEGIO_SECUNDUS(new int[] { 33820, 1000, 1200, 84 }),
	LEGIO_TERTIUS(new int[] { 33821, 1000, 1200, 85 }),
	LEGIO_QUARTUS(new int[] { 33822, 1000, 1200, 86 }),
	LEGIO_QUINTUS(new int[] { 33823, 1000, 1200, 87 }),
	LEGIO_SEXTUS(new int[] { 33824, 1000, 1200, 88 }),
	DAGANNOTH_REX(new int[] { 33827, 2500, 1500, 70 }),
	DAGANNOTH_PRIME(new int[] { 33826, 2500, 1500, 69 }),
	DAGANNOTH_SUPREME(new int[] { 33828, 2500, 1500, 68 }),
	VORAGO(new int[] { 28630, 5000, -1, 93, 33717, 100, -1, 93 }),
	GREGOROVIC(new int[] { 37183, 2000, 400, -1 }),
	HELWYR(new int[] { 37182, 2000, 400, -1 }),
	TWIN_FURIES(new int[] { 37185, 2000, 400, -1, 37184, 2000, 400, -1 }),
	NYLESSA(new int[] { 37184, 2000,0 }),
	VINDICTA(new int[] { 37180, 2000, 400, -1, 37181, 2000, 400, -1 }),
	RAWRVEK(new int[] { 37181, 2000,0	}),
	TELOS_THE_WARDEN(new int[] { -1, 1400, 300, -1 }),
	NEX_ANGEL_OF_DEATH(new int[] { -1, 3000, 1000, -1 }),
	BEASTMASTER_DURZAG(new int[] { -1, 300, 60, -1 }),
	YAKAMARU(new int[] { -1, 300, 60, -1 }),
	EDDY(new int[] { 32730, 2500, 500, -1}),
	TESS(new int[]{37679,300, 60, -1 }),
	ENDURING_GLACYTE(new int[] { 41380, 1500, 500, -1}),
	UNSTABLE_GLACYTE(new int[] { 41381, 1500, 500, -1}),
	SAPPING_GLACYTE(new int[] { 41379, 1500, 500, -1});

	public static final ImmutableMap<Integer, BossPets> PETS;
	static {
		Map<Integer, BossPets> pets = new HashMap<>();
		for(val next : values()) {
			pets.put(next.getPetId(), next);
		}
		PETS = ImmutableMap.copyOf(pets);
	}
	private final int[] set;
	
	BossPets(int[] set) {
		this.set = set;
	}
	
	public final int getAmount() {
		return set.length / 4;
	}
	
	public final int getPetId() {
		return set[0];
	}
	
	public final int getBaseChance(int index) {
		return set[1 + (index * 4)];
	}
	
	public final int getTreshold(int index) {
		return set[2 + (index * 4)];
	}
	
	public final int getArrayIndex(int index) {
		return set[3 + (index * 4)];
	}
}