package com.rs.game.player.dialogue.impl;

import com.rs.game.player.Skills;
import com.rs.game.player.content.items.AncientEffigy;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Utils;

/**
 * Ancient effifies dialogue handling.
 *
 * @author Raghav/Own4g3 <Raghav_ftw@hotmail.com> Improved alot by Dragonkk as
 *         always...
 */
public class AncientEffigyD extends Dialogue {

	int itemId;
	int skill1;
	int skill2;
	int selected;

	@Override
	public void start() {
		itemId = (Integer) parameters[0];
		sendDialogue("As you inspect the ancient effigy, you begin to feel a",
				"strange sensation of the relic searching your mind,", "drawing on your knowledge.");
		int random = Utils.random(8);
		skill1 = AncientEffigy.SKILL_1[random];
		skill2 = AncientEffigy.SKILL_2[random];
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			sendDialogue("Images from your experiences of " + AncientEffigy.getMessage(skill1),
					"fill your mind.");
			stage = 0;
		} else if (stage == 0) {
			sendOptionsDialogue("Which images do you wish to focus on?", Skills.SKILL_NAME[skill1],
					Skills.SKILL_NAME[skill2]);
			stage = 1;
		} else if (stage == 1) {
			int skill = componentId == OPTION_1 ? skill1 : skill2;
			selected = componentId == OPTION_1 ? OPTION_1 : OPTION_2;
			if (player.getSkills().getLevelForXp(skill) < AncientEffigy.getRequiredLevel(itemId)) {
				sendDialogue(

						"The images in your mind fade; the ancient effigy seems",
						"to desire knowledge of experiences you have not yet", "had.");
				player.getPackets()
						.sendGameMessage("You require at least level " + AncientEffigy.getRequiredLevel(itemId) + " "
								+ Skills.SKILL_NAME[skill] + " to investigate the ancient effigy further.");
				stage = -2;
			} else {
				sendDialogue("As you focus on your memories, you can almost hear a",
						"voice in the back of your mind whispering to you...");
				stage = 2;
			}
		} else if (stage == 2) {
			int skill = selected == OPTION_1 ? skill1 : skill2;
			int xp = AncientEffigy.getExp(itemId);
			player.getSkills().addXp(skill, xp);
			AncientEffigy.effigyInvestigation(player, itemId);
			sendDialogue("The ancient effigy glows briefly; it seems changed",
					"somehow and no longer responds to the same memories", "as before.");
			stage = 3;
		} else if (stage == 3) {
			sendDialogue("A sudden bolt of inspiration flashes through your mind,",
					"revealing new insight into your experiences!");
			stage = -2;
		} else {
			end();
		}
	}

	@Override
	public void finish() {
	}
}