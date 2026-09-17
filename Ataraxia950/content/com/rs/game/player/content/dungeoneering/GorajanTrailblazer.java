package com.rs.game.player.content.dungeoneering;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.player.Player;

import java.io.Serializable;

public final class GorajanTrailblazer implements Serializable {

	private static final long serialVersionUID = -6840673395817120309L;

	private final boolean[][] outfits = new boolean[5][5];
	private final int[] activeOutfits = new int[5];
	private transient Player player;
	public static final int HEAD = 0, BODY = 1, LEGS = 2, GLOVES = 3, BOOTS = 4;
	public static final int FROZEN = 0, FURNISHED = 1, ABANDONED = 2, OCCULT = 3, WARPED = 4;
	
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public int getPiece(int type, int slot) {
		if (!outfits[type][slot])
			return 0;
		return activeOutfits[slot] == (38521 + (5 * type)) + slot ? 2 : 1;
	}
	
	public void setPieceActive(int type, int slot, boolean active) {
		activeOutfits[slot] = active ? (38521 + (5 * type)) + slot : 0;
	}
	
	public void setSlots() {
		for (int i = 0; i < 5; i++)
		activeOutfits[i] = 38541 + i;
	}
	
	public int getActivePiece(int slot) {
		return activeOutfits[slot];
	}
	
	public double getExperienceBoost() {
		for (int sets = 4; sets >= 0; sets--) 
			if (hasOutfit(sets))
				return sets == 4 ? 1.07 : 1.05;
		return 1;
	}
	
	public int getLevelBoosts() {
		return hasOutfit(WARPED) ? 3 : 0;
	}
	
	public boolean hasOutfit(int type) {
		boolean hasOutfit = true;
		for (int i = 0; i < 5; i++)
			if (!outfits[type][i])
				hasOutfit = false;
		return hasOutfit;
	}
	
	public void unlockOutfit(final int id) {
		if (id < 38521 || id > 38545)
			return;
		int tier = (id - 38521) / 5;
		int piece = id - (38521 + (5 * tier));
		if (!outfits[tier][piece]) {
			outfits[tier][piece] = true;
			player.sendMessage("You've unlocked " + ItemDefinitions.getItemDefinitions(id).getName() + ".");
		}
	}
	
}
