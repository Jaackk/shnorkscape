package com.rs.game.player.content;

import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.GeneralRequirementMap;
import com.rs.game.Animation;
import com.rs.game.player.GlobalPlayerUpdater;
import com.rs.game.player.Player;
import com.rs.game.player.TemporaryAttributes.Key;
import com.rs.utils.Utils;

public final class PlayerLook {

	private PlayerLook() {

	}


	public static void handleMageMakeOverButtons(Player player, int buttonId) {
		if (buttonId == 26 || buttonId == 28 || buttonId == 23 || buttonId == 29)
			player.getTemporaryAttributtes().put("MageMakeOverGender", buttonId == 26 || buttonId == 28);
		else if (buttonId >= 5 && buttonId <= 16) {

			int skin;
			if (buttonId == 16)
				skin = 11;
			else if (buttonId == 15)
				skin = 10;
			else if (buttonId == 14)
				skin = 6;
			else if (buttonId == 13)
				skin = 5;
			else if (buttonId == 12)
				skin = 4;
			else if (buttonId == 11)
				skin = 3;
			else if (buttonId == 10)
				skin = 2;
			else if (buttonId == 9)
				skin = 1;
			else if (buttonId == 8)
				skin = 0;
			else if (buttonId == 7)
				skin = 7;
			else if (buttonId == 6)
				skin = 8;
			else
				skin = 9;
			player.getTemporaryAttributtes().put("MageMakeOverSkin", skin);
		} else if (buttonId == 62) {
			Boolean male = (Boolean) player.getTemporaryAttributtes().remove("MageMakeOverGender");
			Integer skin = (Integer) player.getTemporaryAttributtes().remove("MageMakeOverSkin");
			player.closeInterfaces();
			if (male == null || skin == null)
				return;
	
			if (male == player.getAppearence().isMale() && skin == player.getAppearence().getSkinColor())
				player.getDialogueManager().startDialogue("MakeOverMage", 2676, 1);
			else {
				player.getDialogueManager().startDialogue("MakeOverMage", 2676, 2);
				if (player.getAppearence().isMale() != male) {
					if (player.getEquipment().wearingArmour()) {
						player.getDialogueManager().startDialogue("SimpleMessage",
								"You cannot have armor on while changing your gender.");
						return;
					}
					if (male)
					    player.getAppearence().resetAppearence();
					else
					    player.getAppearence().female();
				}
				player.getAppearence().setSkinColor(skin);
				player.getAppearence().generateAppearenceData();
			}
		}
	}

	public static void handleThessaliasMakeOverButtons(Player player, int buttonId, int slotId) {
		if (buttonId == 12)
			player.getTemporaryAttributtes().put("ThessaliasMakeOver", 0);
		else if (buttonId == 13) {
			if (ClientScriptMap.getMap(player.getAppearence().isMale() ? 690 : 1591)
					.getKeyForValue(player.getAppearence().getTopStyle()) >= 32) {
				player.getTemporaryAttributtes().put("ThessaliasMakeOver", 1);
			} else
				player.getPackets().sendGameMessage("You can't select different arms to go with that top.");
		} else if (buttonId == 14) {
			if (ClientScriptMap.getMap(player.getAppearence().isMale() ? 690 : 1591)
					.getKeyForValue(player.getAppearence().getTopStyle()) >= 32) {
				player.getTemporaryAttributtes().put("ThessaliasMakeOver", 2);
			} else
				player.getPackets().sendGameMessage("You can't select different wrists to go with that top.");
		} else if (buttonId == 15)
			player.getTemporaryAttributtes().put("ThessaliasMakeOver", 3);
		else if (buttonId == 26) { // confirm
			player.closeInterfaces();
		} else if (buttonId == 17) { // set part
			Integer stage = (Integer) player.getTemporaryAttributtes().get("ThessaliasMakeOver");
			if (stage == null || stage == 0) {
				player.getAppearence().setTopStyle(
						ClientScriptMap.getMap(player.getAppearence().isMale() ? 690 : 1591).getIntValue(slotId / 2));
				if (!player.getAppearence().isMale())
					player.getAppearence().setBeardStyle(player.getAppearence().getTopStyle());
				player.getAppearence().setArmsStyle(player.getAppearence().isMale() ? 26 : 65); // default
				player.getAppearence().setWristsStyle(player.getAppearence().isMale() ? 34 : 68); // default
			} else if (stage == 1) // arms
				player.getAppearence().setArmsStyle(
						ClientScriptMap.getMap(player.getAppearence().isMale() ? 711 : 693).getIntValue(slotId / 2));
			else if (stage == 2) // wrists
				player.getAppearence().setWristsStyle(ClientScriptMap.getMap(player.getAppearence().isMale() ? 749 : 751).getIntValue(slotId / 2));
			else
				player.getAppearence().setLegsStyle(
						ClientScriptMap.getMap(player.getAppearence().isMale() ? 1586 : 1607).getIntValue(slotId / 2));

		} else if (buttonId == 20) {// color
			Integer stage = (Integer) player.getTemporaryAttributtes().get("ThessaliasMakeOver");
			if (stage == null || stage == 0 || stage == 1)
				player.getAppearence().setTopColor(ClientScriptMap.getMap(3282).getIntValue(slotId / 2));
			else if (stage == 3)
				player.getAppearence().setLegsColor(ClientScriptMap.getMap(3284).getIntValue(slotId / 2));
		}
	}


	public static void openMageMakeOver(Player player) {
		player.stopAll(true, true);
		if (player.getEquipment().wearingArmour()) {
			player.getDialogueManager().startDialogue("SimpleNPCMessage", 2676,
					"You can't do this with all that armour. Take it off and then speak to me again.");
			return;
		}
		player.getInterfaceManager().sendInterface(900);
		player.getPackets().sendIComponentText(900, 33, "Confirm");
		player.getPackets().sendConfigByFile(485, player.getAppearence().isMale() ? 0 : 1);
		player.getPackets().sendConfigByFile(486, player.getAppearence().getSkinColor());
		player.getTemporaryAttributtes().put("MageMakeOverGender", player.getAppearence().isMale());
		player.getTemporaryAttributtes().put("MageMakeOverSkin", player.getAppearence().getSkinColor());
	}

	public static void openThessaliasMakeOver(final Player player) {
		if (player.getEquipment().wearingArmour()) {
			player.getDialogueManager().startDialogue("SimpleNPCMessage", 548,
					"You're not able to try on my clothes with all that armour. Take it off and then speak to me again.");
			return;
		}
		player.setNextAnimation(new Animation(11623));
		player.getInterfaceManager().sendInterface(729);
		player.getPackets().sendIComponentText(729, 30, "Free!");
		player.getTemporaryAttributtes().put("ThessaliasMakeOver", 0);
		player.getPackets().sendUnlockIComponentOptionSlots(729, 17, 0, 500, 0);
		player.getPackets().sendUnlockIComponentOptionSlots(729, 20, 0, 500, 0);
		player.setCloseInterfacesEvent(new Runnable() {

			@Override
			public void run() {
				player.getDialogueManager().startDialogue("SimpleNPCMessage", 548,
						"A marvellous choise. You look splendid!");
				player.setNextAnimation(new Animation(-1));
				player.getAppearence().getAppeareanceData();
				player.getTemporaryAttributtes().remove("ThessaliasMakeOver");
			}

		});
	}
        public static int SET_COUNT = 96;
        
        public static int[] getSet(int index) {
            GeneralRequirementMap map = GeneralRequirementMap.getMap((index >= 64 ? (28114 - 64) : 1048) + index);
            int[] parts = new int[5];
            for (int i = 1182; i <= 1186; i++)
                parts[i - 1182] = map.getIntValue(i);
            return parts;
        }
        
	   public static void randomizeLook(GlobalPlayerUpdater appearence) {

	        ClientScriptMap skinMap = ClientScriptMap.getMap(7724);

	        // dont allow skins out of default skins
	        appearence.setSkinColor(
	                ClientScriptMap.getMap(748).getIntValue(skinMap.getIntValueAtIndex(Utils.random(skinMap.getSize()))));

	        ClientScriptMap hairColor = ClientScriptMap.getMap(2345);
	        appearence.setHairColor(hairColor.getIntValueAtIndex(Utils.random(hairColor.getSize())));

	        ClientScriptMap topColor = ClientScriptMap.getMap(3282);
	        appearence.setTopColor(topColor.getIntValueAtIndex(Utils.random(topColor.getSize())));
	        appearence.setLegsColor(topColor.getIntValueAtIndex(Utils.random(topColor.getSize())));

	        ClientScriptMap bootsColor = ClientScriptMap.getMap(3297);
	        appearence.setBootsColor(bootsColor.getIntValueAtIndex(Utils.random(bootsColor.getSize())));

	        boolean male = appearence.isMale();

	        ClientScriptMap hairStyle = ClientScriptMap.getMap(male ? 3304 : 3302);
	        GeneralRequirementMap map = GeneralRequirementMap
	                .getMap(hairStyle.getIntValueAtIndex(Utils.random(hairStyle.getSize())));
	        appearence.setHairStyle(map.getIntValue(788));

	        ClientScriptMap topStyle = ClientScriptMap.getMap(male ? 3287 : 3299);
	        appearence.setTopStyle(topStyle.getIntValueAtIndex(Utils.random(topStyle.getSize())));
	        for (int i = 0; i < SET_COUNT; i++) {
	            int[] set = getSet(i);
	            if (set[0] == appearence.getTopStyle()) {
	                appearence.setArmsStyle(set[1]);
	                appearence.setHandsStyle(set[2]);
	                break;
	            }
	        }

	        ClientScriptMap legsStyle = ClientScriptMap.getMap(male ? 3289 : 3301);
	        appearence.setLegsStyle(legsStyle.getIntValueAtIndex(Utils.random(legsStyle.getSize())));

	        ClientScriptMap bootsStyle = ClientScriptMap.getMap(male ? 3290 : 3293);
	        appearence.setBootsStyle(bootsStyle.getIntValueAtIndex(Utils.random(bootsStyle.getSize())));

	        if (male) {
	            ClientScriptMap beardStyle = ClientScriptMap.getMap(3307);
	            appearence.setBeardStyle(beardStyle.getIntValueAtIndex(Utils.random(beardStyle.getSize())));
	        }
	    }
	   
	   
	    public static void openCharacterCustomizing(Player player) {
	        if (player.getEquipment().wearingArmour()) {
	               player.getDialogueManager().startDialogue("SimpleMessage", "You can't do this with all that armour on. Remove all of it and then try again.");
	               return;
	        }
	        player.getInterfaceManager().sendLoadingScreen(1420);
	        player.getPackets().sendHideIComponent(1420, 168, false);
	        player.getPackets().sendHideIComponent(1420, 169, true);
	        player.getPackets().sendGlobalConfig(1008, 312);
	        player.getPackets().sendGlobalConfig(1009, 16);
	        player.getPackets().sendGlobalConfig(1010, 869);
	        player.getPackets().sendGlobalConfig(1011, -1);
	        player.getPackets().sendGlobalConfig(1012, 805);
	        player.getPackets().sendGlobalConfig(1013, 821);
	        player.getPackets().sendGlobalConfig(1014, 837);
	        player.getPackets().sendGlobalConfig(1015, 2);
	        player.getPackets().sendGlobalConfig(1016, 68);
	        player.getPackets().sendGlobalConfig(1017, 69);
	        player.getPackets().sendGlobalConfig(1018, 197);
	        player.getPackets().sendGlobalConfig(1019, 6);
	        player.getPackets().sendGlobalConfig(1015, 2);
	        player.getPackets().sendGlobalConfig(1016, 68);
	        player.getPackets().sendGlobalConfig(1017, 69);
	        player.getPackets().sendGlobalConfig(1018, 197);
	        player.getPackets().sendGlobalConfig(1019, 6);
	        setCurrentCustomizationTab(player, 0);
	        player.getPackets().sendIComponentSettings(1420, 44, -1, -1, 2);
	        player.getPackets().sendIComponentSettings(1420, 43, -1, -1, 2);
	        player.getPackets().sendIComponentSettings(1420, 120, -1, -1, 1026);
	        player.getPackets().sendIComponentSettings(1420, 195, 0, 200, 2);
	        player.getPackets().sendIComponentSettings(1420, 173, 0, 200, 2);
//	        player.getPackets().sendIComponentSettings(1420, 441, -1, -1, 2);
//	        player.getPackets().sendIComponentSettings(1420, 222, -1, -1, 2);
//	        player.getPackets().sendIComponentSettings(1420, 620, 0, 2, 2);
            player.getPackets().sendExecuteScript(10370, 1, InterfaceManager.getComponentUId(1420, 168), "Done");
            player.getPackets().sendExecuteScript(10370, 1, InterfaceManager.getComponentUId(1420, 449), "Yes");
            player.getPackets().sendIComponentSettings(1420, 168, -1, 0, 2);
	        player.getPackets().sendExecuteScript(7936, 1);
	        player.getPackets().sendAppearenceLook();
	    }

	    public static void handleCharacterCustomizingButtons(Player player, int buttonId, int slotId) {
	        if (buttonId == 43 || buttonId == 44) {
	            boolean male = buttonId == 44;
	            if (male != player.getAppearence().isMale()) {
	                if (!male)
	                    player.getAppearence().female();
	                else
	                    player.getAppearence().male();
	                randomizeLook(player.getAppearence());
	                player.getPackets().sendAppearenceLook();
	            }
	        } else if (buttonId == 120) {
	            randomizeLook(player.getAppearence());
	            player.getPackets().sendAppearenceLook();
	        } else if (buttonId == 202 || buttonId >= 229 && buttonId <= 233) {
	            setCurrentCustomizationTab(player, buttonId == 202 ? 0 : buttonId - 228);
	        } else if (buttonId == 173) {
	            int currentTab = getCurrentCustomizationTab(player);
	            if (currentTab == 0) {// skin colour
	                player.getAppearence().setSkinColor(
	                        ClientScriptMap.getMap(748).getIntValue(ClientScriptMap.getMap(7724).getIntValue(slotId / 2)));
	                player.getPackets().sendAppearenceLook();
	            } else if (currentTab == 1 || (currentTab == 5 && player.getAppearence().isMale())) {
	                player.getAppearence().setHairColor(
	                        ClientScriptMap.getMap(2345).getIntValue(ClientScriptMap.getMap(7723).getIntValue(slotId / 2)));
	                player.getPackets().sendAppearenceLook();
	            } else if (currentTab == 2) { // top
	                player.getAppearence().setTopColor(
	                        ClientScriptMap.getMap(3282).getIntValue(ClientScriptMap.getMap(7721).getIntValue(slotId / 2)));

	                player.getPackets().sendAppearenceLook();
	            } else if (currentTab == 3) { // legs
	                player.getAppearence().setLegsColor(
	                        ClientScriptMap.getMap(3282).getIntValue(ClientScriptMap.getMap(7721).getIntValue(slotId / 2)));

	                player.getPackets().sendAppearenceLook();
	            } else if (currentTab == 4) { // boot
	                player.getAppearence().setBootsColor(
	                        ClientScriptMap.getMap(3297).getIntValue(ClientScriptMap.getMap(7722).getIntValue(slotId / 2)));

	                player.getPackets().sendAppearenceLook();
	            }
	        } else if (buttonId == 195) {
                int currentTab = getCurrentCustomizationTab(player);
                boolean male = player.getAppearence().isMale();
                if (currentTab == 1) { // hair style
                    int map1 = ClientScriptMap.getMap(male ? 3304 : 3302).getIntValue(slotId / 2);
                    if (map1 == 0)
                        return;
                    GeneralRequirementMap map = GeneralRequirementMap.getMap(map1);
                    player.getAppearence().setHairStyle(map.getIntValue(788));
                    player.getPackets().sendAppearenceLook();
                } else if (currentTab == 2) { // top
                    player.getAppearence().setTopStyle(ClientScriptMap.getMap(male ? 3287 : 3299).getIntValue(slotId / 2));
                    for (int i = 0; i < SET_COUNT; i++) {
                        int[] set = getSet(i);
                        if (set[0] == player.getAppearence().getTopStyle()) {
                            player.getAppearence().setArmsStyle(set[1]);
                            player.getAppearence().setHandsStyle(set[2]);
                            break;
                        }
                    }
                    player.getPackets().sendAppearenceLook();
                } else if (currentTab == 3) { // legs
                    player.getAppearence().setLegsStyle(ClientScriptMap.getMap(male ? 3289 : 3301).getIntValue(slotId / 2));
                    player.getPackets().sendAppearenceLook();
                } else if (currentTab == 4) { // boot
                    player.getAppearence()
                            .setBootsStyle(ClientScriptMap.getMap(male ? 3290 : 3293).getIntValue(slotId / 2));
                    player.getPackets().sendAppearenceLook();
                } else if (currentTab == 5 && male) {
                    player.getAppearence().setBeardStyle(ClientScriptMap.getMap(3307).getIntValue(slotId / 2));
                    player.getPackets().sendAppearenceLook();
                }
            } else if (buttonId == 168) {
                player.getAppearence().generateAppearenceData();
                player.getPackets().sendAppearenceLook();
                player.getInterfaceManager().closeLoadingScreen();
            }
	    }
	    
	    public static void setCurrentCustomizationTab(Player player, int tabId) {
	        player.getTemporaryAttributtes().put(Key.PLAYER_CUSTOMIZATION_TAB, tabId);
	        refreshCurrentCustomizationTab(player);
	    }
	    
	    public static int getCurrentCustomizationTab(Player player) {
	           Integer currentTab = (Integer) player.getTemporaryAttributtes().get(Key.PLAYER_CUSTOMIZATION_TAB);
	            if (currentTab == null)
	                currentTab =  (Integer) player.getTemporaryAttributtes().put(Key.PLAYER_CUSTOMIZATION_TAB, 0);
	        return currentTab;
	    }
	    
	    public static void refreshCurrentCustomizationTab(Player player) {
	        player.getPackets().sendGlobalConfig(3482, getCurrentCustomizationTab(player));// current tab
	    }
}
