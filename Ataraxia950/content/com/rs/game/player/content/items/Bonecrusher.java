package com.rs.game.player.content.items;

import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

/**
 * Handles the Bonecrusher item.
 * 
 * @author Kris
 */
public class Bonecrusher {

	private static final String[] BONE_NAMES = new String[] { "Bones", "Big bones", "Jogre bones", "Zogre bones", "Ourg bones", "Airut bones", "Bat bones", "Wolf bones", "Monkey bones", "Dagannoth bones", "Baby dragon bones", "Dragon bones", "Frost dragon bones", "Wyvern bones", "Adamant drag. bones", "Rune dragon bones", "Dinosaur bones" };

	private static final int[] BONES = new int[] { 526, 532, 3125, 4812, 4834, 30209, 530, 2859, 3179, 6729, 534, 536, 18830, 6812, 35008, 35010, 48075 };
	private static final double[] EXPERIENCE = new double[] { 9, 30, 30, 45, 280, 265, 10.6, 9, 10, 250, 60, 144, 360, 100, 288, 380, 170 };

	public static void openBonecrusher(Player player) {
		player.getInterfaceManager().sendCentralOverlayInterface(1641);
		refreshSettings(player);
	}

	public static void handleBonecrusher(Player player, int componentId) {
		if (componentId >= 33 && componentId <= 45 || componentId == 155 || componentId == 48) {
		    if (componentId == 48) {
		         boolean disableAll = true;
		         for(int i=0;i<=5;i++)
		             if(!player.bonecrusherSettings[i]) {
		                 disableAll = false;
		                 break;
		             }
                 for(int i=0;i<=5;i++)
                     player.bonecrusherSettings[i] = !disableAll;
		         refreshSettings(player);
		         return;
		    }
			int id = componentId == 155 ? 0 : ((componentId - 33) / 3) + 1;
			player.bonecrusherSettings[id] = !player.bonecrusherSettings[id];
			player.sendMessage(BONE_NAMES[id] + (player.bonecrusherSettings[id] ? " are now being crushed for 2x burying experience." : "s are no longer being crushed for 2x burying experience."));
		    refreshSettings(player);
		} else if (componentId >= 22 && componentId <= 28 || componentId == 18 || componentId == 179 || componentId == 55) {
            if (componentId == 55) {
                 boolean disableAll = true;
                 for(int i=6;i<=9;i++)
                     if(!player.bonecrusherSettings[i]) {
                         disableAll = false;
                         break;
                     }
                 if (!player.bonecrusherSettings[16])
                     disableAll = false;
                 
                 for(int i=6;i<=9;i++)
                     player.bonecrusherSettings[i] = !disableAll;
                 player.bonecrusherSettings[16] = !disableAll;
                 refreshSettings(player);
                 return;
            }
            int id = componentId == 18 ? 6 : componentId == 179 ? 16 : ((componentId - 22) / 3) + 7;
            player.bonecrusherSettings[id] = !player.bonecrusherSettings[id];
            player.sendMessage(BONE_NAMES[id] + (player.bonecrusherSettings[id] ? " are now being crushed for 2x burying experience." : "s are no longer being crushed for 2x burying experience."));
            refreshSettings(player);
        } else if (componentId >= 118 && componentId <= 148 || componentId == 162) {
            if (componentId == 162) {
                boolean disableAll = true;
                for(int i=10;i<=15;i++)
                    if(!player.bonecrusherSettings[i]) {
                        disableAll = false;
                        break;
                    }
                for(int i=10;i<=15;i++)
                    player.bonecrusherSettings[i] = !disableAll;
                refreshSettings(player);
                return;
           }
           int id = componentId == 148 ? 14 : componentId == 142 ? 15 : ((componentId - 118) / 6) + 10;
           player.bonecrusherSettings[id] = !player.bonecrusherSettings[id];
           player.sendMessage(BONE_NAMES[id] + (player.bonecrusherSettings[id] ? " are now being crushed for 2x burying experience." : "s are no longer being crushed for 2x burying experience."));
           refreshSettings(player);
       }
	}

	public static boolean handleDrop(Player player, Item item) {
		int i = 0;
		for (int settings : BONES) {
			if (settings == item.getId() && player.bonecrusherSettings[i]) {
				player.getSkills().addXp(Skills.PRAYER, EXPERIENCE[i] * item.getAmount());
				player.addBonesOffered();
				player.getPackets().sendGameMessage("The bonecrusher instantly crushes the " + item.getName().toLowerCase() + ". Bones offered: " + Colors.RED + Utils.getFormattedNumber(player.getBonesOffered()) + ".", true);
				int restoration = 0;
				boolean aura = false;
				boolean amulet = false;
				if (player.getAuraManager().getPrayerRestoration() != 0) {
					aura = true;
					restoration = ((int) ((int) (Math.floor(player.getSkills().getLevelForXp(Skills.PRAYER))) * player.getAuraManager().getPrayerRestoration()));
				}
				if (player.getEquipment().getAmuletId() == 19886) {
					restoration = restoration + ((int) ((int) EXPERIENCE[i] * item.getAmount() * 0.5));
					amulet = true;
				} else if (player.getEquipment().getAmuletId() == 19887) {
					restoration = restoration + ((int) ((int) EXPERIENCE[i] * item.getAmount() * 0.75));
					amulet = true;
				} else if (player.getEquipment().getAmuletId() == 19888) {
					restoration = restoration + ((int) EXPERIENCE[i] * item.getAmount());
					amulet = true;
				}
				if (restoration + player.getPrayer().getPrayerpoints() > player.getSkills().getLevelForXp(Skills.PRAYER) * 10)
					restoration = (player.getSkills().getLevelForXp(Skills.PRAYER) * 10) - player.getPrayer().getPrayerpoints();
				if (restoration > 0) {
					player.getPrayer().restorePrayer(restoration);
					if (aura && amulet)
						player.sendMessage("Your aura along with the amulet restore " + restoration + " prayer points.", true);
					else if (aura)
						player.sendMessage("Your aura restores " + restoration + " prayer points.", true);
					else if (amulet)
						player.sendMessage("Your amulet restores " + restoration + " prayer points.", true);
				}
				return true;
			}
			i++;
		}
		return false;
	}
	
	public static void refreshSettings(Player player) {
	    if (player.bonecrusherSettings.length != 17) {
	        boolean[] temp = new boolean[17];
	        for(int i=0;i<player.bonecrusherSettings.length;i++)
	            temp[i] = player.bonecrusherSettings[i];
	        player.bonecrusherSettings = temp;
	    }
	    int v1 = 0;
	    int v2 = 0;
	    int v3 = 0;
	    for (int i=0;i < player.bonecrusherSettings.length;i++) {
	        if (i <= 5)
	            v1 |= (player.bonecrusherSettings[i] ? 1 : 0) << i;
	        else if (i <= 9)
	            v2 |= (player.bonecrusherSettings[i] ? 1 : 0) << (i - 6);
	        else if (i <= 15)
	            v3 |= (player.bonecrusherSettings[i] ? 1 : 0) << ((i == 14 ? 15 : i == 15 ? 14 : i) - 10);
	        int varbitId = getVarbitId(i);
	        if (varbitId != -1)
	            player.getPackets().sendConfigByFile(varbitId, player.bonecrusherSettings[i] ? 1 : 0);
	    }
	    player.getPackets().sendConfigByFile(28425, v1);
	    player.getPackets().sendConfigByFile(28430, v2);
	    player.getPackets().sendConfigByFile(28437, v3);
	}
	
	public static int getVarbitId(int index) {
	    switch(index) {
        case 0:
            return 28419;
        case 1:
            return 28420;
        case 2:
            return 28421;
        case 3:
            return 28422;
        case 4:
            return 28423;
        case 5:
            return 28424;
        case 6:
            return 28426;
        case 7:
            return 28427;
        case 8:
            return 28428;
        case 9:
            return 28429;
        case 10:
            return 28431;
        case 11:
            return 28432;
        case 12:
            return 28433;
        case 13:
            return 28434;
        case 15:
            return 28435;
        case 14:
            return 28436;
        case 16:
            return 44564;
	    }
	    return -1;
	} 
}