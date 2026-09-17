package com.rs.game.player.dialogue.impl.godwars2;

import com.rs.game.WorldTile;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.npc.Transformation;
import com.rs.game.player.PerkManager;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.player.dialogue.impl.OptionSelectionD;
import com.rs.utils.Colors;

import java.util.ArrayList;
import java.util.List;

public class jmodoutfit extends Dialogue {
		@Override
		public void start() {
			sendOptionsDialogue("<col=f2490c><img=6>      ALL ARMOURS AND WEAPONS FOR PLAYER", "Melee Outfits", "Exit");
			stage = 0;
		}
		@Override
		public void run(int interfaceId, int componentId) {
				if (componentId == OPTION_1) {
				  List<String> options = new ArrayList<String>();
                options.add("Rune Armour & Weapons Set");
                options.add("Dragon Armour & Weapons Set");
                options.add("Master Armour & Weapons Set");
                player.getDialogueManager().startDialogue("OptionSelectionD", "what outfit would u like transfrom to?", options.toArray(new String[options.size()]), options.toArray(new String[options.size()]), (OptionSelectionD.SelectionEvent) (selector, option) -> {
                    int page = selector.getPage();
                    switch (page) {
                        case 0:
                            selector.close();
                            switch (option) {
                                case OptionSelectionD.OptionSelector.OPTION_1:
                                    player.getInventory().addItem(1079, 1);
                                    player.getInventory().addItem(1127, 1);
                                    player.getInventory().addItem(1163, 1);
                                    player.getInventory().addItem(1185, 1);
                                    player.getInventory().addItem(1201, 1);
                                    player.getInventory().addItem(1213, 1);
                                    player.getInventory().addItem(1303, 1);
                                    player.getInventory().addItem(1319, 1);
                                    player.getInventory().addItem(1333, 1);
                                    player.getInventory().addItem(1373, 1);
                                    player.getInventory().addItem(13003, 1);
                                    player.getInventory().addItem(11838, 1);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_2:
                                    player.getInventory().addItem(1149, 1);
                                    player.getInventory().addItem(1187, 1);
                                    player.getInventory().addItem(1215, 1);
                                    player.getInventory().addItem(1305, 1);
                                    player.getInventory().addItem(1377, 1);
                                    player.getInventory().addItem(1434, 1);
                                    player.getInventory().addItem(1672, 1);
                                    player.getInventory().addItem(3140, 1);
                                    player.getInventory().addItem(3204, 1);
                                    player.getInventory().addItem(4087, 1);
                                    player.getInventory().addItem(4587, 1);
                                    player.getInventory().addItem(11842, 1);
                                    player.getInventory().addItem(13006, 1);
                                    player.getInventory().addItem(13449, 1);
                                    player.getInventory().addItem(13462, 1);
                                    player.getInventory().addItem(14479, 1);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_3:
                                    player.getAppearence().transformIntoNPC(24391);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_4:
                                    player.getAppearence().transformIntoNPC(20578);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_5:
                                    player.getAppearence().transformIntoNPC(26559);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_6:
                                    player.getAppearence().transformIntoNPC(26560);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_7:
                                    player.getAppearence().transformIntoNPC(26561);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_8:
                                    player.getAppearence().transformIntoNPC(26562);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_9:
                                    player.getAppearence().transformIntoNPC(22159);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_10:
                                    player.getAppearence().transformIntoNPC(25523);
                                    break;
                            }
                            return;
                    }
                });
            }
			return;
		}
    @Override
		public void finish() {
		}
	}