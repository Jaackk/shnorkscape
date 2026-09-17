package com.rs.game.player.content.items;

import com.rs.game.player.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EliteOutfits {
	
	public static boolean isComponent(int id) {
		return Sentinel.allComponents.contains(id);
	}
	
	public static String getPrefix(String input) {
		return input.substring(0, input.indexOf(" ")).toLowerCase();
	}
	
	public static boolean wearingSentinel(Player player) {
		return (Sentinel.isWearing("oaken", player) || Sentinel.isWearing("maple", player) ||
				Sentinel.isWearing("willow", player) || Sentinel.isWearing("nature's", player));
	}

	private static final List<String> equips = Arrays.asList("helmet", "body", "legs", "gloves", "boots");
	
	public enum Sentinel {
		OAKEN_SENTINEL("oaken", new int[] {39730, 39731, 39732, 39733, 39734}),
		WILLOW_SENTINEL("willow", new int[] {39735, 39736, 39737, 39738, 39739}),
		MAPLE_SENTINEL("maple", new int[] {39740, 39741, 39742, 39743, 39744}),
		NATURE_SENTINEL("nature's", new int[] {39745, 39746, 39747, 39748, 39749});
		
		private static final ArrayList<Integer> allComponents = new ArrayList<Integer>();
		private static final Map<String, Sentinel> sentinels = new HashMap<String, Sentinel>();
			
		static {
			for (Sentinel sentinel : Sentinel.values()) {
				for(int component : sentinel.components)
					allComponents.add(component);
				sentinels.put(sentinel.getName(), sentinel);
			}
		}
		
		private final String name;
		private final int[] components;
		
		Sentinel(String name, int[] components) {
			this.name = name;
			this.components = components;
		}
		
		public static Sentinel getOutfit(String name) {
			return sentinels.get(name.toLowerCase());
		}
		
		public String getName() {
			return this.name;
		}
		
		public int[] getComponents() {
			return this.components;
		}
		
		public int getPiece(String name) {
			if(equips.contains(name))
				return this.components[equips.indexOf(name)];
			return this.components[0];
		}
		
		public String getPiece(int id) {
			for(int index = 0; index <= components.length; index++) {
				if(id == components[index])
					return equips.get(index);
			}
			return "helmet";
		}
		
		public static boolean isWearing(String name, Player player) {
			return (player.getEquipment().getHatId() == getOutfit(name).getPiece("helmet") &&
				player.getEquipment().getChestId() == getOutfit(name).getPiece("body") &&
				player.getEquipment().getLegsId() == getOutfit(name).getPiece("legs") &&
				player.getEquipment().getGlovesId() == getOutfit(name).getPiece("gloves") &&
				player.getEquipment().getBootsId() == getOutfit(name).getPiece("boots"));
		}
	}
}
