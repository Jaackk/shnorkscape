package com.rs.game.npc.combat;

public final class NPCWeaknessValidator {

	public static boolean isStabWeapon(String name) {
		return name.contains("dagger") || name.contains(" sword") || name.contains("spear") || name.contains("hasta")
				|| name.contains("rapier") || name.contains("blade");
	}
	
	public static boolean isCrushWeapon(String name) {
		return name.contains("mace") || name.contains("warhammer") || name.contains("maul") || name.contains("pickaxe")
				|| name.contains("staff") || name.contains("flail") || name.equals("Saradomin sword")
				|| name.equals("Torag's hammer");
	}
	
	public static boolean isSlashWeapon(String name) {
		return name.contains("hatchet") || name.contains("claw") || name.contains("longsword") || name.contains("light")
				|| name.equals("Excalibur") || name.equals("Blurite sword") || name.contains("scimitar")
				|| name.contains("battleaxe") || name.contains("greataxe") || name.contains("2h sword")
				|| name.contains("godsword") || name.contains("katana") || name.contains("halberd")
				|| name.contains("whip");
	}
}
