package com.rs.game.player.dialogue.impl.godwars2;

import com.rs.game.WorldTile;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.player.PerkManager;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

import java.util.List;

public class jmodteleports extends Dialogue {
		@Override
		public void start() {
			sendOptionsDialogue("<col=f2490c><img=6>      Mod Teleports", "Main Teleports", "Skilling Teleports", "Exit");
			stage = 0;
		}

		@Override
		public void run(int interfaceId, int componentId) {
				if (componentId == OPTION_1) {
					player.openTeleportInterface();
					}
			if (componentId == OPTION_2) {
				player.getInterfaceManager().sendInterface(1937);

			}
			if (componentId == OPTION_3) {
			end();
			}
			return;
		}





	@Override
		public void finish() {

		}
	}