package com.rs.game.npc;

import com.rs.game.player.Player;
import com.rs.utils.Utils;

public class BossPet {

	public BossPet(final int index, final int baseChance) {
		this.index = index;
		this.baseChance = baseChance;
	}
	
	private final int index, baseChance;
	
	public final int getIndex() {
		return index;
	}
	
	public final int getBaseChance() {
		return baseChance;
	}
	
	public static final int generatePetDrop(final String name, boolean hardMode, final Player player) {
		if (name == null || player == null)
			return -1;
		for (Treshold pets : Treshold.values()) {
			if (pets.toString().equalsIgnoreCase(name)) {
				int kills = player.getKillStatistics(pets.getPets()[0].getIndex());
				if (pets.getPets().length > 1 && pets.getPets()[1].getIndex() != pets.getPets()[0].getIndex())
					kills += player.getKillStatistics(pets.getPets()[1].getIndex());
				kills = 3067;
				final BossPet p = pets.getPets().length > 1 && hardMode ? pets.getPets()[1] : pets.getPets()[0];
				final int chance = (int) (1 + (Math.min((kills / (pets.getPets()[0].getBaseChance() * 0.2)), 9)));
				if (Utils.random(p.getBaseChance() / chance) == 0) 
					return pets.getItemId();
				break;
			}
		}
		return -1;
	}
	
	private enum Treshold {
		
		KREE_ARRA(33804, new BossPet(3, 5000), new BossPet(3, 1000)),
		COMMANDER_ZILYANA(33807, new BossPet(4, 5000), new BossPet(4, 1000)),
		GENERAL_GRAARDOR(33806, new BossPet(1, 5000), new BossPet(1, 1000)),
		KRIL_TSUTSAROTH(33805, new BossPet(2, 5000), new BossPet(2, 1000)),
		NEX(33808, new BossPet(5, 2000)),
		//GIANT_MOLE(33813, new BossPet(89, 2500), new BossPet(500, 89)),
		HAR_AKEN(33814, new BossPet(125, 50)),
		TZTOK_JAD(21512, new BossPet(95, 1)),
		KING_BLACK_DRAGON(33818, new BossPet(8, 2500)),
		QUEEN_BLACK_DRAGON(33825, new BossPet(7, 2500)),
		KALPHITE_QUEEN(33816, new BossPet(54, 2500)),
		KALPHITE_KING(33815, new BossPet(90, 2000)),
		CORPOREAL_BEAST(33812, new BossPet(6, 2500)),
		CHAOS_ELEMENTAL(33811, new BossPet(10, 2500)),
		LEGIO_PRIMUS(33819, new BossPet(83, 1000)),
		LEGIO_SECUNDUS(33820, new BossPet(84, 1000)),
		LEGIO_TERTIUS(33821, new BossPet(85, 1000)),
		LEGIO_QUARTUS(33822, new BossPet(86, 1000)),
		LEGIO_QUINTUS(33823, new BossPet(87, 1000)),
		LEGIO_SEXTUS(33824, new BossPet(88, 1000)),
		DAGANNOTH_REX(33827, new BossPet(70, 2500)),
		DAGANNOTH_PRIME(33826, new BossPet(69, 2500)),
		DAGANNOTH_SUPREME(33828, new BossPet(68, 2500)),
		VORAGO(28630, new BossPet(93, 5000), new BossPet(93, 2500)),
		GREGOROVIC(37183, new BossPet(123, 2000), new BossPet(124, 1000)),
		HELWYR(37182, new BossPet(113, 2000), new BossPet(114, 1000)),
		AVARYSS(37185, new BossPet(121, 2000), new BossPet(122, 1000)),
		NYMORA(37184, new BossPet(121, 2000), new BossPet(122, 1000)),
		VINDICTA(37180, new BossPet(115, 2000), new BossPet(120, 1000)),
		GORVEK(37181, new BossPet(115, 2000), new BossPet(120, 1000)),
		EDDY(32730, new BossPet(9, 2500)),
		ENDURING_GLACYTE(41380, new BossPet(11, 1)),
		UNSTABLE_GLACYTE(41381, new BossPet(12, 1)),
		SAPPING_GLACYTE(41379, new BossPet(13, 1));
		
		private final int itemId;
		private final BossPet[] pet;
		
		Treshold(final int itemId, final BossPet... pet) {
			this.itemId = itemId;
			this.pet = pet;
		}
		
		public final int getItemId() {
			return itemId;
		}
		
		public final BossPet[] getPets() {
			return pet;
		}
		
		@Override
		public String toString() {
			if (equals(KREE_ARRA))
				return "Kree'Arra";
			if (equals(KRIL_TSUTSAROTH))
				return "K'ril Tsutsaroth";
			String name = name().toLowerCase().replace("_", " ");
			return name.substring(0, 1).toUpperCase() + name.substring(1);
		}
		
	}
	
}
