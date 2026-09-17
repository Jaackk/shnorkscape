package com.rs.game.player.content;

/*
 * @author Movee
 */

public class GoldenDying {

	public enum WARPRIEST {
		
		SARADOMIN(new int[] {28755, 28758, 28761, 28764, 28767, 28770}, "Saradomin"),
		
		ZAMORAK(new int[] {28773, 28776, 28779, 28782, 28785, 28788}, "Zamorak"),
		
		ARMADYL(new int[] {30288, 30291, 30294, 30297, 30300, 30303}, "Armadyl"),
		
		BANDOS(new int[] {30306, 30309, 30312, 30315, 30318, 30321}, "Bandos");
		
		private final int[] itemIds;
		private final String convertString;
		
		WARPRIEST(int[] itemIds, String convertString) {
			this.itemIds = itemIds;
			this.convertString = convertString;
		}
		
		public int[] getId() {
			return itemIds;
		}
		
		public String getConvertString() {
			return convertString;
		}
	}
	
	public enum NORMALITEMS {
		
		DHAROK(new int[] {4716, 4718, 4720, 4722}, "Dharok"),
		
		VERAC(new int[] {4753, 4755, 4757, 4759}, "Verac"),
		
		TORAG(new int[] {4745, 4747, 4749, 4751}, "Torag"),
		
		AHRIM(new int[] {4708, 4710, 4712, 4714}, "Ahrim"),
		
		GUTHAN(new int[] {4724, 4726, 4728, 4730}, "Guthan"),
		
		KARIL(new int[] {4732, 4734, 4736, 4738}, "Karil"),
		
		GODSWORD(new int[] {11694, 11696, 11698, 11700}, "Godsword");
		
		private final int[] itemIds;
		private final String convertString;
		
		NORMALITEMS(int[] itemIds, String convertString) {
			this.itemIds = itemIds;
			this.convertString = convertString;
		}
		
		public int[] getId() {
			return itemIds;
		}
		
		public String getConvertString() {
			return convertString;
		}
		
	}
	
	public enum GOLDENITEMS {
		
		DHAROK(new int[] {35473, 35476, 35479, 35482}, "Dharok"),
		
		VERAC(new int[] {35521, 35524, 35527, 35530}, "Verac"),
		
		TORAG(new int[] {35509, 35512, 35515, 35518}, "Torag"),
		
		AHRIM(new int[] {35461, 35464, 35467, 35470}, "Ahrim"),
		
		GUTHAN(new int[] {35485, 35488, 35491, 35494}, "Guthan"),
		
		KARIL(new int[] {35497, 35500, 35503, 35506}, "Karil"),
		
		GODSWORD(new int[] {32014, 32016, 32018, 32020}, "Godsword"),
		
		SARADOMIN(new int[] {32022, 32023, 32024, 32025, 32026, 32027}, "Saradomin"),
		
		ZAMORAK(new int[] {32028, 32029, 32030, 32031, 32032, 32033}, "Zamorak"),
		
		ARMADYL(new int[] {32034, 32035, 32036, 32037, 32038, 32039}, "Armadyl"),
		
		BANDOS(new int[] {32040, 32041, 32042, 32043, 32044, 32045}, "Bandos");
		
		private final int[] itemIds;
		private final String convertString;
		
		GOLDENITEMS(int[] itemIds, String convertString) {
			this.itemIds = itemIds;
			this.convertString = convertString;
		}
		
		public int[] getId() {
			return itemIds;	
		}
		
		public String getConvertString() {
			return convertString;
		}
	}
}
