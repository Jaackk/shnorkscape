package com.rs.game.player.dialogue.impl.godwars2;

import com.rs.game.WorldTile;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.player.PerkManager;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.RouteEvent;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.util.List;

public class blackcoins extends Dialogue {

		@Override
		public void start() {
			sendOptionsDialogue("<col=f2490c><img=6>      blackcoin master", "transfer blackcoins", "trasnfer gp into blackcoins");
			stage = 0;
		}
		@Override
		public void run(int interfaceId, int componentId) {
				if (componentId == OPTION_1) {

						if (!player.getInventory().containsItem(42311, 10)) {
							player.getPackets().sendGameMessage(
									"you need 10 blackcoins in your backpack");
							player.getPackets().sendPlayerMessage(1, 15263739, "<col=ffffff>" + "you need 10 blackcoins in your backpack", true);


							return;
						}else
							player.getPackets().sendPlayerMessage(1, 15263739, "<col=ffffff>" + "250k has been added to your backpack", true);

							player.getInventory().addItem(995, 250000);
						player.getInventory().deleteItem(42311, 10);
						player.getPackets().sendGameMessage(
								"you have transfered 10 blackcoin for 250k");

					}
			if (componentId == OPTION_2) {
				if (!player.getInventory().containsItem(995, 250000)) {
					player.getPackets().sendGameMessage(
							"you need 10 blackcoins in your backpack");
					player.getPackets().sendPlayerMessage(1, 15263739, "<col=ffffff>" + "you need 250k gp in your backpack", true);


					return;
				}else
					player.getPackets().sendPlayerMessage(1, 15263739, "<col=ffffff>" + "10 blackcoins has been added to your backpack", true);

				player.getInventory().addItem(42311, 10);
				player.getInventory().deleteItem(995, 250000);
				player.getPackets().sendGameMessage(
						"you have transfered 250k gp for 10 blackcoins");

			}

			return;
		}





	@Override
		public void finish() {
		}
	}