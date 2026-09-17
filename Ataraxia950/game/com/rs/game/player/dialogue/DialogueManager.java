package com.rs.game.player.dialogue;

import com.rs.game.player.Player;
import com.rs.game.player.controllers.ArtisansWorkShopControler;
import com.rs.game.player.dialogue.impl.BookDialogue;
import com.rs.game.player.dialogue.impl.ClanInvite;
import com.rs.game.player.dialogue.impl.DonationPerksD;
import com.rs.game.player.dialogue.impl.DungeoneeringChroniclesD;
import com.rs.game.player.dialogue.impl.InventionManufactureD;

public class DialogueManager {

	private final Player player;
	private Dialogue lastDialogue;

	public DialogueManager(Player player) {
		this.player = player;
	}

	public void startDialogue(Object key, Object... parameters) {
		if (!player.getControlerManager().useDialogueScript(key))
			return;
		if (lastDialogue != null)
			lastDialogue.finish();
		lastDialogue = DialogueHandler.getDialogue(key);
		if (lastDialogue == null)
			return;
		lastDialogue.parameters = parameters;
		lastDialogue.setPlayer(player);
		lastDialogue.start();
	}
	
    public void continueDialogue(int interfaceId, int componentId, int slotId) throws ClassNotFoundException {
        if (lastDialogue == null)
            return;
        if (!player.getInterfaceManager().containsChatBoxInter() && !(lastDialogue instanceof DungeoneeringChroniclesD)
                && !(lastDialogue instanceof ClanInvite)
                && !(lastDialogue instanceof BookDialogue)
                && !(player.getControlerManager().getControler() instanceof ArtisansWorkShopControler)
                && !(lastDialogue instanceof DonationPerksD)
                && !(lastDialogue instanceof InventionManufactureD)
                && interfaceId != 1048
                && interfaceId != 1370
                && interfaceId != 1263
                && interfaceId != 1929
                && interfaceId != 1306
                && interfaceId != 1562
                && interfaceId != 1845
                && interfaceId != 20)
            return;
        lastDialogue.run(interfaceId, componentId, slotId);
    }
    
	public void continueDialogue(int interfaceId, int componentId) {
		if (lastDialogue == null)
			return;
		if (!player.getInterfaceManager().containsChatBoxInter() && !(lastDialogue instanceof DungeoneeringChroniclesD)
				&& !(lastDialogue instanceof ClanInvite)
				&& !(lastDialogue instanceof BookDialogue)
				&& !(player.getControlerManager().getControler() instanceof ArtisansWorkShopControler)
				&& !(lastDialogue instanceof DonationPerksD)
				&& !(lastDialogue instanceof InventionManufactureD)
				&& interfaceId != 1048
				&& interfaceId != 1370
				&& interfaceId != 1371
				&& interfaceId != 1578
				&& interfaceId != 517
				&& interfaceId != 1929
				&& interfaceId != 1306
				&& interfaceId != 1562
			    && interfaceId != 1845
				&& interfaceId != 20
				&& interfaceId != 1262 && interfaceId != 382 && interfaceId != 1292 && interfaceId != 793)
			return;
		lastDialogue.run(interfaceId, componentId);
	}

	public void finishDialogue() {
		if (lastDialogue == null)
			return;
		if (player.isNative950()) {
			// Retire ownership before content cleanup: a throwing or reentrant
			// finish must not leave the previous native dialogue accepting replies.
			Dialogue previous = lastDialogue;
			lastDialogue = null;
			try {
				previous.finish();
			} finally {
				player.getInterfaceManager().closeChatBoxInterface();
			}
			return;
		}
		lastDialogue.finish();
		lastDialogue = null;
		if (player.getInterfaceManager().containsChatBoxInter())
			player.getInterfaceManager().closeChatBoxInterface();
	}

	public void restartDialogue() {
		if (lastDialogue == null)
			return;
		startDialogue(lastDialogue, lastDialogue.parameters);
	}
	
	public boolean hasDialogue() {
		return (lastDialogue != null);
	}
	
	public Dialogue getDialogue() {
		return lastDialogue;
	}
}
