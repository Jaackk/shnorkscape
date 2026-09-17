package com.rs.game.player.dialogue.impl.heartofgielinor;

import com.rs.game.player.dialogue.Dialogue;

import static com.rs.game.player.content.HeartOfGielinor.SEREN;
import static com.rs.game.player.content.HeartOfGielinor.SLISKE;
import static com.rs.game.player.content.HeartOfGielinor.ZAMORAK;
import static com.rs.game.player.content.HeartOfGielinor.ZAROS;
import static com.rs.utils.Colors.GREEN;
import static com.rs.utils.Colors.RED;

public class InsigniaSettingsD extends Dialogue {
	
	@Override
	public void start() {
		sendOptionsDialogue("Select an Option",
				"Insignia selection.",
				(player.getHeart().getInsigniaSettings()[0] ? GREEN : RED) + "Automatically convert seals.",
				(player.getHeart().getInsigniaSettings()[1] ? GREEN : RED) + "Automatically pick up ingression fragments.",
				(player.getHeart().getInsigniaSettings()[2] ? GREEN : RED) + "Never damage your faction.",
				"Close.");
	}
	
	private final void sendInsigniaSelection() {
		sendOptionsDialogue("Select an Insignia",
				(player.getHeart().getActiveInsignia() == SEREN ? GREEN : RED) + "Seren.",
				(player.getHeart().getActiveInsignia() == SLISKE ? GREEN : RED) + "Sliske.",
				(player.getHeart().getActiveInsignia() == ZAROS ? GREEN : RED) + "Zaros.",
				(player.getHeart().getActiveInsignia() == ZAMORAK ? GREEN : RED) + "Zamorak.",
				"Return to previous options.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch(stage) {
		case -1: 
			switch(componentId) {
			case OPTION_1:
				sendInsigniaSelection();
				stage++;
				break;
			case OPTION_2:
				player.getHeart().setInsigniaSettings(0, !player.getHeart().getInsigniaSettings()[0]);
				stage = -1;
				start();
				return;
			case OPTION_3:
				player.getHeart().setInsigniaSettings(1, !player.getHeart().getInsigniaSettings()[1]);
				stage = -1;
				start();
				return;
			case OPTION_4:
				player.getHeart().setInsigniaSettings(2, !player.getHeart().getInsigniaSettings()[2]);
				stage = -1;
				start();
				return;
			case OPTION_5:
				end();
				return;
			}
			break;
		case 0:
			switch(componentId) {
			case OPTION_1:
				if (player.getHeart().getReputation(SEREN) < 500) {
					player.getPackets().sendPlayerMessage(1, 15263739, "You need to obtain at least 500 Seren faction reputation to activate the Seren insignia.", true);
					sendInsigniaSelection();
					return;
				}
				player.getHeart().setActiveInsignia(player.getHeart().getActiveInsignia() == SEREN ? -1 : SEREN);
				break;
			case OPTION_2:
				if (player.getHeart().getReputation(SLISKE) < 500) {
					player.getPackets().sendPlayerMessage(1, 15263739, "You need to obtain at least 500 Sliske faction reputation to activate the Sliske insignia.", true);
					sendInsigniaSelection();
					return;
				}
				player.getHeart().setActiveInsignia(player.getHeart().getActiveInsignia() == SLISKE ? -1 : SLISKE);
				break;
			case OPTION_3:
				if (player.getHeart().getReputation(ZAROS) < 500) {
					player.getPackets().sendPlayerMessage(1, 15263739, "You need to obtain at least 500 Zaros faction reputation to activate the Zaros insignia.", true);
					sendInsigniaSelection();
					return;
				}
				player.getHeart().setActiveInsignia(player.getHeart().getActiveInsignia() == ZAROS ? -1 : ZAROS);
				break;
			case OPTION_4:
				if (player.getHeart().getReputation(ZAMORAK) < 500) {
					player.getPackets().sendPlayerMessage(1, 15263739, "You need to obtain at least 500 Zamorak faction reputation to activate the Zamorak insignia.", true);
					sendInsigniaSelection();
					return;
				}
				player.getHeart().setActiveInsignia(player.getHeart().getActiveInsignia() == ZAMORAK ? -1 : ZAMORAK);
				break;
			case OPTION_5:
				stage = -1;
				start();
				return;
			}
			sendInsigniaSelection();
			return;
		}
	}

	@Override
	public void finish() {
		
	}

}
