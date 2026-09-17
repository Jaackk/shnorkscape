package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

public class DismissD extends Dialogue {

	@Override
	public void finish() {
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1 && componentId == OPTION_1) {
			if (player.getFamiliar() != null) {
				player.getFamiliar().sendDeath(player);
			} else if (player.getPet() != null) {
				/*player.getPetManager().setNpcId(-1);
				player.getPetManager().setItemId(-1);
				player.getPetManager().removeDetails(player.getPet().getItemId());
				player.getPet().switchOrb(false);
				player.getPackets().closeInterface(player.getInterfaceManager().hasRezizableScreen() ? 98 : 212);
				player.getPackets().sendIComponentSettings(747, 17, 0, 0, 0);
				player.getPet().finish();
				player.getPackets().sendGlobalConfig(168, 4); // resets to
				// inventory tab,
				// cbf being in a
				// glitched
				// summoning tab
				if(!player.getObtainedPets().contains(player.getPet().getb))
					PetPerkUtils.redeemPet(player, player.getPet());
				player.setCurrentPet(null);
				player.setPet(null);
				player.getPackets().sendGameMessage("Your pet runs off until it's out of sight.");*/
				player.getPet().pickup();
			}
		}
		end();
	}

	@Override
	public void start() {
		sendOptionsDialogue(player.getPet() != null ? "Dissmiss pet?" : "Dismiss Familiar?", "Yes.", "No.");
	}
}