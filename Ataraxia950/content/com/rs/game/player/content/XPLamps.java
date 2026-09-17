package com.rs.game.player.content;

import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.World;
import com.rs.game.activites.gim.GIM;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.hcim_news.HcimNewsManager;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.util.concurrent.ThreadLocalRandom;

public class XPLamps {

	public static final int LAMP_SMALL = 0;
	public static final int LAMP_MEDIUM = 1;
	public static final int LAMP_BIG = 2;
	public static final int LAMP_HUGE = 3;

	public static final int[] SELECTABLE_XP_LAMPS = new int[] { 23713, 23714, 23715, 23716 };
	public static final int[] SELECTABLE_XP_LAMPS_TYPES = new int[] { LAMP_SMALL, LAMP_MEDIUM, LAMP_BIG, LAMP_HUGE };
	public static final int[][] SKILL_LAMPS = new int[][] { { 23717, 23718, 23719, 23720 },
        { 23725, 23726, 23727, 23728 }, { 23721, 23722, 23723, 23724 }, { 23753, 23754, 23755, 23756 },
        { 23729, 23730, 23731, 23732 }, { 23737, 23738, 23739, 23740 }, { 23733, 23734, 23735, 23736 },
        { 23798, 23799, 23800, 23801 }, { 23806, 23807, 23808, 23809 }, { 23774, 23775, 23776, 23777 },
        { 23794, 23795, 23796, 23797 }, { 23802, 23803, 23804, 23805 }, { 23769, 23770, 23771, 23773 },
        { 23790, 23791, 23792, 23793 }, { 23786, 23787, 23788, 23789 }, { 23761, 23762, 23763, 23764 },
        { 23757, 23758, 23759, 23760 }, { 23765, 23766, 23767, 23768 }, { 23778, 23779, 23780, 23781 },
        { 23810, 23811, 23812, 23813 }, { 23741, 23742, 23743, 23744 }, { 23782, 23783, 23784, 23785 },
        { 23745, 23746, 23747, 23748 }, { 23814, 23815, 23816, 23817 }, { 23749, 23750, 23751, 23752 },
        { 29545, 29546, 29547, 29548 } , { 39424, 39425, 39426, 39427 }};
	public static final int[][] SKILL_LAMPS_TYPES = new int[][] { { LAMP_SMALL, LAMP_MEDIUM, LAMP_BIG, LAMP_HUGE },
			{ LAMP_SMALL, LAMP_MEDIUM, LAMP_BIG, LAMP_HUGE }, { LAMP_SMALL, LAMP_MEDIUM, LAMP_BIG, LAMP_HUGE },
			{ LAMP_SMALL, LAMP_MEDIUM, LAMP_BIG, LAMP_HUGE }, { LAMP_SMALL, LAMP_MEDIUM, LAMP_BIG, LAMP_HUGE },
			{ LAMP_SMALL, LAMP_MEDIUM, LAMP_BIG, LAMP_HUGE }, { LAMP_SMALL, LAMP_MEDIUM, LAMP_BIG, LAMP_HUGE },
			{ LAMP_SMALL, LAMP_MEDIUM, LAMP_BIG, LAMP_HUGE }, { LAMP_SMALL, LAMP_MEDIUM, LAMP_BIG, LAMP_HUGE },
			{ LAMP_SMALL, LAMP_MEDIUM, LAMP_BIG, LAMP_HUGE }, { LAMP_SMALL, LAMP_MEDIUM, LAMP_BIG, LAMP_HUGE },
			{ LAMP_SMALL, LAMP_MEDIUM, LAMP_BIG, LAMP_HUGE }, { LAMP_SMALL, LAMP_MEDIUM, LAMP_BIG, LAMP_HUGE },
			{ LAMP_SMALL, LAMP_MEDIUM, LAMP_BIG, LAMP_HUGE }, { LAMP_SMALL, LAMP_MEDIUM, LAMP_BIG, LAMP_HUGE },
			{ LAMP_SMALL, LAMP_MEDIUM, LAMP_BIG, LAMP_HUGE }, { LAMP_SMALL, LAMP_MEDIUM, LAMP_BIG, LAMP_HUGE },
			{ LAMP_SMALL, LAMP_MEDIUM, LAMP_BIG, LAMP_HUGE }, { LAMP_SMALL, LAMP_MEDIUM, LAMP_BIG, LAMP_HUGE },
			{ LAMP_SMALL, LAMP_MEDIUM, LAMP_BIG, LAMP_HUGE }, { LAMP_SMALL, LAMP_MEDIUM, LAMP_BIG, LAMP_HUGE },
			{ LAMP_SMALL, LAMP_MEDIUM, LAMP_BIG, LAMP_HUGE }, { LAMP_SMALL, LAMP_MEDIUM, LAMP_BIG, LAMP_HUGE },
			{ LAMP_SMALL, LAMP_MEDIUM, LAMP_BIG, LAMP_HUGE }, { LAMP_SMALL, LAMP_MEDIUM, LAMP_BIG, LAMP_HUGE },
			{ LAMP_SMALL, LAMP_MEDIUM, LAMP_BIG, LAMP_HUGE }, { LAMP_SMALL, LAMP_MEDIUM, LAMP_BIG, LAMP_HUGE }};

	public static final int[] OTHER_LAMPS = new int[] {}; // for future

	// do not look at wiki, wiki contains alot of incorrect xp data (I checked
	// myself)
	private static final double[] BASE_LAMPS_XP = new double[] { 62.5, 69, 77, 85, 94, 104, 115, 127, 139, 154, 170,
            188, 206, 229, 252, 262, 274, 285, 298, 310, 325, 337, 352, 367.5, 384, 399, 405, 414, 453, 473, 514, 528,
            536, 551, 583, 609, 635, 662, 692, 721, 752, 785, 818, 854, 890, 929, 971, 1013, 1055, 1101, 1149, 1200,
            1250, 1305, 1362, 1422, 1485, 1542, 1617, 1685, 1758, 1836, 1912, 2004.5, 2085, 2172, 2269, 2379, 2471,
            2593, 2693, 2810, 2947, 3082, 3214, 3339, 3496, 3648, 3793, 3980, 4166, 4348, 4522, 4762, 4919, 5150, 5376,
            5593, 5923, 6122, 6452, 6615, 6929, 7236, 7533, 8065, 8348, 8602 };

	public static void processLampClick(Player player, int slot, int id) {
		if (isSelectable(id)) {
			openSelectableDialog(player, slot, id);
		} else if (isSkillLamp(id)) {
			openSkillDialog(player, slot, id);
		} else if (isOtherLamp(id)) {
			openSelectableDialog(player, slot, id);
		}
	}

	private static void openSelectableDialog(Player player, final int slot, final int id) {
		player.getDialogueManager().startDialogue(new Dialogue() {
            private int skillChosen = -1;

			@Override
			public void start() {
				player.getInterfaceManager().sendInterface(1263);
				player.getPackets().sendGlobalString(2389, "What sort of XP would you like?");
				player.getPackets().sendGlobalConfig(1797, 0); // selectable
																// lamps don't
																// show xp
				player.getPackets().sendGlobalConfig(1798, 1); // minimum level
																// of 1 to show
				player.getPackets().sendGlobalConfig(1799, id);
				for(int i=22;i<=72;i++)
                player.getPackets().sendIComponentSettings(1263, i, -1, 0, 2);
                player.getPackets().sendIComponentSettings(1263, 13, -1, 0, 2);
                player.getPackets().sendIComponentSettings(1263, 19, -1, 0, 2);
                player.getPackets().sendIComponentSettings(1263, 79, -1, 0, 2);
                player.getPackets().sendExecuteScript(10370, 1, InterfaceManager.getComponentUId(1263, 13), "");
                player.getPackets().sendExecuteScript(785, 1, 1, id, 0, "What sort of XP would you like?");
			}


			public void run(int interfaceId, int componentId, int slotId) {
				if (componentId >= 22 && componentId <= 72) {
				    skillChosen = selectSkill(componentId);
                    player.getPackets().sendExecuteScript(785, 1, 1, id, ClientScriptMap.getMap(1482).getIntValue(skillChosen), "What sort of XP would you like?");
                    player.getPackets().sendExecuteScript(10370, 1, InterfaceManager.getComponentUId(1263, 13), "Confirm "+Skills.SKILL_NAME[skillChosen]+" XP");
				} else if (componentId == 13) {
                    if (skillChosen == -1) {
                        openSelectableDialog(player, slot, id);
                        return;
                    }
                    end();
                    if (!player.getInventory().containsItem(id, 1)) 
                        return;
                    int level = player.getSkills().getLevelForXp(skillChosen);
                    player.getInventory().deleteItem(slot, new Item(id, 1));
                    if (ItemDefinitions.getItemDefinitions(id).getName().equalsIgnoreCase("Dragonkin lamp"))
                        rollForPet(player);
                    double exp = ItemDefinitions.getItemDefinitions(id).getName().equalsIgnoreCase("Dragonkin lamp") ? XPLamps.getDragonKinLampXPForLevel(level) : getExp(level, selectableLampType(id));
                    player.getSkills().addXp(skillChosen, exp);
                    player.closeInterfaces();
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "<col=0000ff>Your wish has been granted!</col>",
                            "You have been awarded " + Utils.getFormattedNumber(exp, ',') + " XP in "
                                    + Skills.SKILL_NAME[skillChosen] + "!");
				} else if(componentId == 14 || componentId == 19) 
				    end();
			}

			@Override
			public void finish() {
				if (player.getInterfaceManager().containsScreenInter())
					player.getInterfaceManager().closeScreenInterface();
			}
			private int selectSkill(int componentId) {
			switch(componentId) {    
			case 22:
			    return 0;
			case 23:
			return 3;
			case 24:
			return 14;
			case 25:
			return 2;
			case 26:
			return 16;
			case 27:
			return 13;
			case 28:
			return 1;
			case 29:
			return 15;
			case 30:
			return 10;
			case 31:
			return 4;
			case 32:
			return 17;
			case 33:
			return 7;
			case 34:
			return 5;
			case 35:
			return 12;
			case 36:
			return 11;
			case 37:
			return 6;
			case 38:
			return 9;
			case 39:
			return 8;
			case 40:
			return 20;
			case 52:
			return 18;
			case 53:
			return 19;
			case 55:
			return 22;
			case 62:
			return 21;
			case 64:
			return 23;
			case 67:
			return 24;
			case 71:
			return 25;
			case 72:
			return 26;
			}
			return -1;
			}


            @Override
            public void run(int interfaceId, int componentId) {
            }




		});

	}

	public static double getDragonKinLampXPForLevel(int level) {
	    return (Math.pow(level, 3) - (2 * Math.pow(level, 2)) + (100.00 * (double)level)) / 20.00;
	}

    private static void rollForPet(Player player) {
        if (ThreadLocalRandom.current().nextInt(100) == 0) {
            Item petItem = new Item(42459);
            if (!player.hasItem(petItem)) {
                player.addItem(petItem);
                World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " has found Effy the effigy!", false);
                player.sendMessage("Congratulations, you have unlocked Effy the effigy!");
				HcimNewsManager.getInstance().addNews(player, "<#player> has unlocked Effy the effigy!");
            }
        }
    }

	private static void openSkillDialog(Player player, final int slot, final int id) {
		final int type = skillLampType(id);
		final int skillId = skillLampSkillId(id);

		player.getDialogueManager().startDialogue(new Dialogue() {
			@Override
			public void start() {
				sendOptionsDialogue("Gain <col=ff0000>" + Skills.SKILL_NAME[skillId] + "</col> experience?", "Yes",
						"No");
			}

			@Override
			public void run(int interfaceId, int componentId) {
				if (componentId != Dialogue.OPTION_1 || !player.getInventory().containsItem(id, 1)) {
					end();
					return;
				}

				player.getInventory().deleteItem(slot, new Item(id, 1));
				double exp = getExp(player.getSkills().getLevelForXp(skillId), type);
				player.getSkills().addXp(skillId, exp / 10);

				player.getDialogueManager().startDialogue("SimpleMessage",
						"<col=0000ff>Your wish has been granted!</col>",
						"You have been awarded " + Utils.getFormattedNumber(exp, ',') + " EXP in " + Skills.SKILL_NAME[skillId] + "!");
			}

			@Override
			public void finish() {
			}

		});
	}

	public static double getExp(int skillLevel, int lampType) {
		double xp;
		if (skillLevel <= BASE_LAMPS_XP.length)
			xp = BASE_LAMPS_XP[skillLevel - 1];
		else
			xp = BASE_LAMPS_XP[BASE_LAMPS_XP.length - 1];

		for (int i = 0; i < lampType; i++)
			xp *= 2D;
		return xp;
	}

	public static int selectableLampType(int id) {
		for (int i = 0; i < SELECTABLE_XP_LAMPS.length; i++) {
			if (SELECTABLE_XP_LAMPS[i] == id || ItemDefinitions.getItemDefinitions(SELECTABLE_XP_LAMPS[i]).getName().equalsIgnoreCase(ItemDefinitions.getItemDefinitions(id).getName()))
				return SELECTABLE_XP_LAMPS_TYPES[i];
		}
		return -1;
	}

	public static boolean isSelectable(int id) {
	    if (ItemDefinitions.getItemDefinitions(id).getName().equalsIgnoreCase("Dragonkin lamp"))
	        return true;
		for (int i = 0; i < SELECTABLE_XP_LAMPS.length; i++) {
			if (SELECTABLE_XP_LAMPS[i] == id || ItemDefinitions.getItemDefinitions(SELECTABLE_XP_LAMPS[i]).getName().equalsIgnoreCase(ItemDefinitions.getItemDefinitions(id).getName()))
				return true;
		}
		return false;
	}

	private static int skillLampType(int id) {
		for (int skillId = 0; skillId < SKILL_LAMPS.length; skillId++) {
			for (int i = 0; i < SKILL_LAMPS[skillId].length; i++) {
				if (SKILL_LAMPS[skillId][i] == id)
					return SKILL_LAMPS_TYPES[skillId][i];
			}
		}
		return -1;
	}

	private static int skillLampSkillId(int id) {
		for (int skillId = 0; skillId < SKILL_LAMPS.length; skillId++) {
			for (int i = 0; i < SKILL_LAMPS[skillId].length; i++) {
				if (SKILL_LAMPS[skillId][i] == id)
					return skillId;
			}
		}
		return -1;
	}

	public static boolean isSkillLamp(int id) {
		for (int skillId = 0; skillId < SKILL_LAMPS.length; skillId++) {
			for (int i = 0; i < SKILL_LAMPS[skillId].length; i++) {
				if (SKILL_LAMPS[skillId][i] == id)
					return true;
			}
		}
		return false;
	}

	public static boolean isOtherLamp(int id) {
		for (int i = 0; i < OTHER_LAMPS.length; i++) {
			if (OTHER_LAMPS[i] == id)
				return true;
		}
		return false;
	}

}
