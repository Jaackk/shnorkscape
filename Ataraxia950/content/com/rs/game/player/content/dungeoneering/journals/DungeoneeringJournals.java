package com.rs.game.player.content.dungeoneering.journals;

import com.rs.game.player.Player;

import java.io.Serializable;

public class DungeoneeringJournals implements Serializable {

	private static final long serialVersionUID = 6970803563408172065L;

	private transient Player player;
	private final boolean[] chronicles = new boolean[75];
	
	public DungeoneeringJournals(Player player) {
		this.player = player;
	}
	
	public void setPlayer(final Player player) {
		this.player = player;
	}
	
	public void unlockJournal(int itemId) {
		Chronicles chronicle = null;
		for (Chronicles c : Chronicles.values())
			if (c.getItemId() == itemId) {
				chronicle = c;
				break;
			}
		if (chronicle == null)
			return;
		chronicles[chronicle.ordinal()] = true;
		player.sendMessage("Congratulations! You've unlocked a chronicle: " + chronicle.getTitle() + ".");
	}
	
	public boolean openJournal(int componentId) {
		Chronicles chronicle = null;
		for (Chronicles c : Chronicles.values())
			if (c.getClickableComponentId() == componentId) {
				chronicle = c;
				break;
			}
		if (chronicle == null)
			return false;
		player.getDialogueManager().startDialogue("DungeoneeringChroniclesD", chronicle);
		return true;
	}
	
	public boolean hasUnlockedJournal(Chronicles chronicle) {
		if (chronicle == null)
			return true;
        return chronicles[chronicle.ordinal()];
    }
	
	public void sendInterface() {
		for (int i = 0; i < 47; i++) {
			if (i < 22) {
				if (chronicles[i]) {
					i = 21;
					player.getPackets().sendIComponentText(948, 232, "Chronicles of Bilrach");
					continue;
				}
			} else if (i < 27) {
				if (chronicles[i]) {
					i = 26;
					player.getPackets().sendIComponentText(948, 155, "Kal'Gerion Notes");
					continue;
				}
			} else if (i < 32) {
				if (chronicles[i]) {
					i = 31;
					player.getPackets().sendIComponentText(948, 189, "Stalker Notes");
					continue;
				}
			} else if (i < 37) {
				if (chronicles[i]) {
					i = 36;
					player.getPackets().sendIComponentText(948, 172, "Behemoth Notes");
					continue;
				}
			} else if (i < 47) {
				if (chronicles[i]) {
					player.getPackets().sendIComponentText(948, 206, "Marmaros and Thok");
					 break;
				}
			}
		}
		for (int i = 0; i < 75; i++) {
			if (chronicles[i]) 
				player.getPackets().sendHideIComponent(948, Chronicles.values()[i].getComponentId(), false);
		}
		player.getInterfaceManager().sendInterface(948);
	}
	
}
