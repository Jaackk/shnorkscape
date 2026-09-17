package com.rs.game.player.dialogue.impl.godwars2;

import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.player.PerkManager;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.RouteEvent;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.util.List;

public class thievingoutfit extends Dialogue {

		@Override
		public void start() {
			sendOptionsDialogue("<col=f2490c><img=6>OBTAIN_SKILLING_OUTFITS", "Thieving Skilling Outfits", "Exit");
			stage = 0;
		}
		@Override
		public void run(int interfaceId, int componentId) {
				if (componentId == OPTION_1) {
					if (player.getTimesStolen() >= 1000) {
						if (!player.hasItem(new Item(21483))) {
							player.addItem(new Item(21483));
							player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
							player.getPackets().sendPlayerMessage(1, 15263739, "<col=ffffff>You have been given thieving outfit as you have stole 1000 in total", true);

						}
					}
					if (player.getTimesStolen() >= 2000) {
						if (!player.hasItem(new Item(21482))) {
							player.addItem(new Item(21482));
							player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
							player.getPackets().sendPlayerMessage(1, 15263739, "<col=ffffff>You have been given thieving outfit as you have stole 2000 in total", true);

						}
					}
					if (player.getTimesStolen() >= 3000) {
						if (!player.hasItem(new Item(21481))) {
							player.addItem(new Item(21481));
							player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
							player.getPackets().sendPlayerMessage(1, 15263739, "<col=ffffff>You have been given thieving outfit as you have stole 3000 in total", true);

						}
					}
					if (player.getTimesStolen() >= 4000) {
						if (!player.hasItem(new Item(21480))) {
							player.addItem(new Item(21480));
							player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
							player.getPackets().sendPlayerMessage(1, 15263739, "<col=ffffff>You have been given thieving outfit as you have stole 4000 in total", true);

						}
						if (player.hasItem(new Item(21480)) || player.hasItem(new Item(21481)) || player.hasItem(new Item(21482)) || player.hasItem(new Item(21483))) {

							player.getPackets().sendPlayerMessage(1, 15263739, "<col=ffffff>You have allready been given thieving outfit", true);

						}
					}

					}


			return;
		}





	@Override
		public void finish() {

		}
	}