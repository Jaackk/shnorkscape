package com.rs.game.npc;

import com.rs.game.item.Item;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;

public enum TertiaryLoot {

	ADAMANT_DRAGON("Adamant dragon", new NPCDrop(11286, 0.5, 1)),
	ANAGAMI("Anagami", new NPCDrop(11286, 1.5, 1)),
	ARHAT("Arhat", new NPCDrop(11286, 1.5, 1)),
	CELESTIAL_DRAGON("Celestial dragon", new NPCDrop(11286, 0.5, 1)),
	ELITE_RUNE_DRAGON("Elite rune dragon", new NPCDrop(11286, 1, 1)),
	BLACK_DRAGON("Black dragon", new NPCDrop(11286, 0.2, 1)),
	FROST_DRAGON("Frost dragon", new NPCDrop(11286, 0.3, 1)),
	KING_BLACK_DRAGON("King black dragon", new NPCDrop(11286, 2, 1)),
	IRON_DRAGON("Iron dragon", new NPCDrop(11286, 0.6, 1)),
	MITHRIL_DRAGON("Mithril dragon", new NPCDrop(11286, 0.7, 1)),
	SKELETAL_WYVERN("Skeletal wvern", new NPCDrop(11286, 0.6, 1)),
	STEEL_DRAGON("Steel dragon", new NPCDrop(11286, 0.8, 1)),
	WYVERN("Wyvern", new NPCDrop(11286, 1, 1)),
	ELITE_WYVERN("Wyvern (elite)", new NPCDrop(11286, 1, 1)),
	CAPSARIUS("Capsarius", new NPCDrop(31203, 0.2, 1)),
	GLADIUS("Gladius", new NPCDrop(31203, 0.2, 1)),
	RORARIUS("Rorarius", new NPCDrop(31203, 0.2, 1)),
	SCUTARIUS("Scutarius", new NPCDrop(31203, 0.2, 1)),
	LEGIO_PRIMUS("Legio Primus", new NPCDrop(31203, 0.2, 1)),
	LEGIO_QUARTUS("Legio Quartus", new NPCDrop(31203, 0.2, 1)),
	LEGIO_QUINTUS("Legio Quintus", new NPCDrop(31203, 0.2, 1)),
	LEGIO_SECUNDUS("Legio Secundus", new NPCDrop(31203, 0.2, 1)),
	LEGIO_SEXTUS("Legio Sextus", new NPCDrop(31203, 0.2, 1)),
	LEGIO_TERTIUS("Legio Tertius", new NPCDrop(31203, 0.2, 1)),
	BLOOD_NIHIL("Blood nihil", new NPCDrop(31334, 30, 1), new NPCDrop(31418, 30, 1)),
	ICE_NIHIL("Ice nihil", new NPCDrop(31334, 30, 1), new NPCDrop(31419, 30, 1)),
	SMOKE_NIHIL("Smoke nihil", new NPCDrop(31334, 30, 1), new NPCDrop(31421, 30, 1)),
	SHADOW_NIHIL("Shadow nihil", new NPCDrop(31334, 30, 1), new NPCDrop(31420, 30, 1)),
	BLADED_MUSPAH("Bladed muspah", new NPCDrop(31334, 30, 1), new NPCDrop(31330, 30, 1)),
	FORCE_MUSPAH("Force muspah", new NPCDrop(31334, 30, 1), new NPCDrop(31330, 30, 1)),
	THROWING_MUSPAH("Throwing muspah", new NPCDrop(31334, 30, 1), new NPCDrop(31330, 30, 1));
	
	TertiaryLoot(String npcName, NPCDrop... drop) {
		this.npcName = npcName;
		this.drop = drop;
	}
	
	private final String npcName;
	private final NPCDrop[] drop;
	
	public String getNPCName() {
		return npcName;
	}
	
	public NPCDrop[] getDrops() {
		return drop;
	}
	
	public Item getDrop(int length) {
		return new Item(drop[length].getItemId(), drop[length].getMinAmount());
	}
	
	public double getRate(int length) {
		return drop[length].getRate();
	}
}
