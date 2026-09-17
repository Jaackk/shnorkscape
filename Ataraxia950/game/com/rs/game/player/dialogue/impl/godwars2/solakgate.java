package com.rs.game.player.dialogue.impl.godwars2;

import com.rs.game.WorldTile;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.player.PerkManager;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

import java.util.List;

public class solakgate extends Dialogue {
		@Override
		public void start() {
			sendOptionsDialogue("<col=f2490c><img=6>      Solak World Boss", "Start The Solak Fight", "Exit");
			stage = 0;
		}
	private int getKcRequired() {
		if (player.getPerkManager().hasPerkActive(PerkManager.DonationPerk.DUNGEONS_MASTER)) {
			return 5;
		}

		return 15;
	}
		@Override
		public void run(int interfaceId, int componentId) {
				if (componentId == OPTION_1) {
					if (player.getSkills().getLevel(Skills.SLAYER) < 110) {
						player.getPackets().sendGameMessage("You need a Slayer level of 110 to Enter this Room.");
						return;
					}
						if (player.dungKills < getKcRequired()) {
							player.getPackets().sendPlayerMessage(1, 15263739, "<col=0867af>" + "You need " + Colors.RED + getKcRequired() + "</col> kills to enter Solak Room; " + "you only have " + Colors.RED + player.dungKills + "</col>.", true);
							end();
							return;
					}
					player.dungKills = 0;
					player.inDungeoneering = true;
					player.setNextWorldTile(new WorldTile(1376, 5648, 0));

					end();
					}
			if (componentId == OPTION_2) {
				end();
			}
			return;
		}





	@Override
		public void finish() {
		player.dungKills = 0;
		}
	}