package com.rs.game.player.dialogue.impl.slayer;


import java.util.ArrayList;
import java.util.List;

import com.rs.game.npc.NPC;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.slayer.SlayerMasterData;
import com.rs.game.player.actions.slayer.SlayerTaskData;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

public class SlayerMasterD extends Dialogue {

	private NPC m;
	private int masterId;
	private boolean hasTask, quickTask;
	private List<SlayerMasterData> masters;
	private String[] firstPage, secondPage;
	
	@Override
	public void start() {
		m = (NPC) parameters[0];
		quickTask = (boolean) parameters[1];
		masterId = player.getSlayer().getSpawnedMasterId();
		if (quickTask) {
			sendPlayerDialogue(NORMAL, "I need another assignment.");
			stage = 1;
			return;
		}
		masters = new ArrayList<SlayerMasterData>();
		for (SlayerMasterData data : SlayerMasterData.values()) {
			if (data.getNpcId() != masterId) {
				if (player.getSkills().getCombatLevelWithSummoning() >= data.getCombatRequirement() && player.getSkills().getLevel(Skills.SLAYER) >= data.getSlayerRequirement()) {
					if (data.equals(SlayerMasterData.MORVRAN)) {
						if (player.getPerkManager().hasPerkActive(DonationPerk.ELF__S_FRIEND) || player.getSkills().getTotalLevel() >= 2250) {
							masters.add(data);
						}
					} else
						masters.add(data);
				}
			}
		}
		firstPage = new String[masters.size() > 5 ? 5 : masters.size() > 4 ? masters.size() : masters.size()];
		if (masters.size() > 4)
			secondPage = new String[masters.size() - 3];
		for (int i = 0; i < masters.size(); i++) {
			if (i < 4)
				firstPage[i] = getMasterName(masters.get(i));
			else
				secondPage[i - 4] = getMasterName(masters.get(i));
		}
		if (secondPage != null) {
			firstPage[masters.size() < 5 ? masters.size() : 4] = "Next page";
			secondPage[masters.size() - 4] = "Previous page";
		}
		sendNPCDialogue(masterId, NORMAL, "'Ello, and what are you after then?");
	}
	
	private String getMasterName(SlayerMasterData data) {
		return Utils.formatPlayerNameForDisplay(data.toString());
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch(stage) {
		case -1:
			if (masters.size() > 0)
				sendOptionsDialogue("Select an Option", "I need another assignment.", "Do you have anything for trade?", "I'm here to discuss any rewards I might be eligible for.", "I'd like to summon a new Slayer master.", "Er...nothing...");
			else
				sendOptionsDialogue("Select an Option", "I need another assignment.", "Do you have anything for trade?", "I'm here to discuss any rewards I might be eligible for.", "Er...nothing...");
			break;
		case 0:
			switch(componentId) {
			case OPTION_1:
				sendPlayerDialogue(NORMAL, "I need another assignment.");
				break;
			case OPTION_2:
				stage = 20;
				sendPlayerDialogue(NORMAL, "Do you have anything for trade?");
				return;
			case OPTION_3:
				stage = 40;
				sendPlayerDialogue(NORMAL, "I'm here to discuss any rewards I might be eligible for.");
				return;
			case OPTION_4:
				if (masters.size() > 0) {
					sendPlayerDialogue(NORMAL, "I'd like to summon a new Slayer master.");
					stage = 60;
				} else {
					stage = 100;
					sendPlayerDialogue(NORMAL, "Er...nothing...");
				}
				return;
			case OPTION_5:
				stage = 100;
				sendPlayerDialogue(NORMAL, "Er...nothing...");
				return;
			}
			break;
		case 1:
			if (player.getSlayer().getSlayerTaskData() != null) {
				/**
				 * Check for Turael and all replacement task here.
				 */
				hasTask = true;
				sendNPCDialogue(masterId, NORMAL, "You're still hunting " + player.getSlayer().getSlayerTaskData().toString() + "; come back when you've finished your task.");
			} else {
				player.getSlayer().requestTask(false, false);
				sendNPCDialogue(masterId, NORMAL, "Excellent, you're doing great. Your new task is to kill " + player.getSlayer().getCurrentAmount() + " " + (SlayerTaskData.values()[player.getSlayer().getCurrentTask()].toString() + "."));
			}
			break;
		case 2:
			if (hasTask) {
				end();
				return;
			}
			sendOptionsDialogue("Select an Option", "Got any tips for me?", "Okay, great!", "I'd like a new assignment please.");
			break;
		case 3:
			switch(componentId) {
			case OPTION_1:
				sendPlayerDialogue(NORMAL, "Got any tips for me?");
				break;
			case OPTION_2:
				stage = 100;
				sendPlayerDialogue(NORMAL, "Okay, great!");
				return;
			case OPTION_3:
				stage = 5;
				sendPlayerDialogue(NORMAL, "I'd like a new assignment please.");
				return;
			}
			break;
		case 4:
			sendNPCDialogue(masterId, NORMAL, player.getSlayer().getSlayerTaskData().getTip());
			stage = 100;
			return;
		case 5:
			sendNPCDialogue(masterId, NORMAL, "A new assignment will cost you 30 Slayer points. Are you sure you wish to reset your task?");
			break;
		case 6:
			if (player.getSlayer().removeSlayerPoints(30)) {
				player.getSlayer().requestTask(false, true);
				sendNPCDialogue(masterId, NORMAL, "Excellent, you're doing great. Your new task is to kill " + player.getSlayer().getCurrentAmount() + " " + (SlayerTaskData.values()[player.getSlayer().getCurrentTask()].toString() + "."));
				stage = 3;
				return;
			}
			stage = 100;
			sendNPCDialogue(masterId, ANGRY, "You don't have enough Slayer points to pay for this service!");
			return;
		case 20:
			sendNPCDialogue(masterId, NORMAL, "I have a wide selection of Slayer equipment - take a look!");
			break;
		case 21:
			end();
			ShopsDataParser.openShop(player, 1);//TODO: Change shop ID.
			break;
		case 40:
			sendOptionsDialogue("Select an Option", "View rewards.", "How do I earn co-op points to spend on rewards?", "Where can I use the co-op food and potion rewards?");
			break;
		case 41:
			switch(componentId) {
			case OPTION_1:
				end();
				//TODO: Open slayer shop (interface 1308)
				break;
			case OPTION_2:
				sendPlayerDialogue(NORMAL, "How do I earn co-op points to spend on rewards?");
				break;
			case OPTION_3:
				sendPlayerDialogue(NORMAL, "Where can I use the co-op food and potion rewards?");
				stage = 46;
				return;
			}
			break;
		case 42:
			sendNPCDialogue(masterId, NORMAL, "You'll gain co-op reward points if you complete at least half of your contribution to a task with a friend. That means you need to be in a group for most of the time you're working on the task, but it doesn't matter if your friend beats you to the majority of kills!");
			break;
		case 43:
			sendNPCDialogue(masterId, NORMAL, "You'll still have been in a group for most of your contribution, so you're still entitled to a co-op reward point.");
			break;
		case 44:
			sendOptionsDialogue("Select an Option", "View rewards.", "Where can I use the co-op food and potion rewards?", "That's all, thanks.");
			break;
		case 45:
			switch(componentId) {
			case OPTION_1:
				end();
				//TODO: Open slayer shop (interface 1308)
				break;
			case OPTION_2:
				sendPlayerDialogue(NORMAL, "Where can I use the co-op food and potion rewards?");
				stage = 46;
				return;
			case OPTION_3:
				sendPlayerDialogue(NORMAL, "That's all, thanks.");
				stage = 100;
				return;
			}
			break;
		case 46:
			sendNPCDialogue(masterId, NORMAL, "The rewards can be used in most of the areas we commonly send you to complete your tasks.");
			break;
		case 47:
			sendOptionsDialogue("Select an Option", "View rewards", "How do I earn co-op points to spend on rewards?", "That's all, thanks.");
			break;
		case 48:
			switch(componentId) {
			case OPTION_1:
				end();
				//TODO: Open slayer shop (interface 1308)
				break;
			case OPTION_2:
				sendPlayerDialogue(NORMAL, "How do I earn co-op points to spend on rewards?");
				stage = 42;
				return;
			}
			break;
		case 60:
			if (masters.size() == 1) {
				player.getPackets().sendSlayerMasterUpdate(m, masters.get(0).getNpcId());
				end();
			} else
				sendOptionsDialogue("Select a Master", firstPage.clone());
			break;
		case 61:
			switch(componentId) {
			case OPTION_1:
				player.getPackets().sendSlayerMasterUpdate(m, masters.get(0).getNpcId());
				break;
			case OPTION_2:
				player.getPackets().sendSlayerMasterUpdate(m, masters.get(1).getNpcId());
				break;
			case OPTION_3:
				player.getPackets().sendSlayerMasterUpdate(m, masters.get(2).getNpcId());
				break;
			case OPTION_4:
				player.getPackets().sendSlayerMasterUpdate(m, masters.get(3).getNpcId());
				break;
			case OPTION_5:
				sendOptionsDialogue("Select a Master", secondPage.clone());
				break;
			}
			if (componentId != OPTION_5)
				end();
			break;
		case 62:
			int size = masters.size();
			int c = size == 8 ? OPTION_5 : size == 7 ? OPTION_4: size == 6 ? OPTION_3 : size == 5 ? OPTION_2 : OPTION_1;
			if (c == componentId) {
				sendOptionsDialogue("Select a Master", firstPage.clone());
				stage = 61;
				return;
			}
			switch(componentId) {
			case OPTION_1:
				player.getPackets().sendSlayerMasterUpdate(m, masters.get(4).getNpcId());
				break;
			case OPTION_2:
				player.getPackets().sendSlayerMasterUpdate(m, masters.get(5).getNpcId());
				break;
			case OPTION_3:
				player.getPackets().sendSlayerMasterUpdate(m, masters.get(6).getNpcId());
				break;
			case OPTION_4:
				player.getPackets().sendSlayerMasterUpdate(m, masters.get(7).getNpcId());
				break;
			}
			if (componentId != c)
				end();
			break;
		case 100:
			end();
			return;
		}
		stage++;
	}

	@Override
	public void finish() {}

}
