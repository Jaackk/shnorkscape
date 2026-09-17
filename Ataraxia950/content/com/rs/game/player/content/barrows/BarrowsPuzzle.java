package com.rs.game.player.content.barrows;

import com.rs.game.WorldObject;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

public final class BarrowsPuzzle {

	private int ordinal, correctSlot;
	private int[] options;
	private final Player player;
	private final Barrows barrows;
	
	public BarrowsPuzzle(final Player player, final Barrows barrows) {
		this.player = player;
		this.barrows = barrows;
		shufflePuzzle();
	}
	
	public final void shufflePuzzle() {
		ordinal = Utils.random(4);
		options = BarrowsPuzzleData.values()[ordinal].getOptions();
		Utils.shuffleIntegerArray(options);
		for (int i = 0; i < 3; i++)
			if (options[i] == BarrowsPuzzleData.values()[ordinal].getAnswer())
				correctSlot = i;
			
	}

	public final void sendInterface() {
		player.getInterfaceManager().sendInterface(25);
		for (int i = 0; i < 3; i++) {
			player.getPackets().sendIComponentModel(25, i < 2 ? 3 + i : 6, options[i]);
			player.getPackets().sendIComponentModel(25, 7 + i, BarrowsPuzzleData.values()[ordinal].getSequenceModel(i));
		}
	}
	
	public final boolean checkPuzzle(int interfaceId, int componentId) {
		if (interfaceId != 25)
			return false;
		int selectedSlot = componentId < 5 ? componentId - 3 : 2;
		if (correctSlot == selectedSlot) {
			player.sendMessage("You hear the doors' locking mechanism grind open.");
			barrows.switchLock();
			barrows.openDoor((WorldObject) player.getTemporaryAttributtes().remove("barrowsDoor"));
		} else {
			player.sendMessage("You hear the doors' locking mechanism reset.");
			barrows.shiftDoors();
			shufflePuzzle();
		}
		player.getInterfaceManager().closeScreenInterface();
		return true;
	}
}
