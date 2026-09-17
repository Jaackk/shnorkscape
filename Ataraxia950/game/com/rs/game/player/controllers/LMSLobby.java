package com.rs.game.player.controllers;

import com.rs.utils.Colors;

public class LMSLobby extends Controller {
	
	/*
	 * Deposit all items / BoB check
	 */
	public void check() {
		if(player.hasFamiliar())
			player.getFamiliar().dissmissFamiliar(false);
		player.getBank().depositAllEquipment(false);
		player.getBank().depositAllInventory(false);
	}
	
	@Override
	public void start() {
		
	}
	
	@Override
	public boolean processCommand(String s, boolean b, boolean c) {
		if (player.isOwner())
			return true;
		if(s.contains("home") || s.contains("respawn") || s.contains("llet") || s.contains("sang")) {
			player.sendMessage(Colors.RED+"There are other ways to leave this area!");
			return false;
		} else
			player.sendMessage(Colors.RED+"You cannot use commands in this area!");
		return false;
	}

	public void reset() {
		player.getAppearence().setRenderEmote(-1);
	}

	
	
}
