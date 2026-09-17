package com.rs.game.player.dialogue.impl.godwars2;

import com.rs.Settings;
import com.rs.game.ForceMovement;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.npc.Transformation;
import com.rs.game.player.PerkManager;
import com.rs.game.player.Player;
import com.rs.game.player.Prayer;
import com.rs.game.player.Skills;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.player.dialogue.impl.OptionSelectionD;
import com.rs.utils.Colors;

import java.util.ArrayList;
import java.util.List;

public class jmodcommands extends Dialogue {
		@Override
		public void start() {
			sendOptionsDialogue("<col=f2490c><img=6>      Pick A Command", "GameCommands", "Exit");
			stage = 0;
		}
		@Override
		public void run(int interfaceId, int componentId ) {
				if (componentId == OPTION_1) {
				  List<String> options = new ArrayList<String>();
                options.add("Set Damage Buff");
                    options.add("Open An Clue Scroll");
                    options.add("Set Prayer Mod");
                    options.add("Set LoyalPoints Mod");
                    options.add("Set AtaraxiaCoins Mod");
                    options.add("Set Invention Materials Mod");
                    options.add("Set Master Rank Mod");
                    options.add("Set Forced Movement Y NORTH ");
                    options.add("Set Forced Movement Y EAST ");
                    options.add("Set Forced Movement Y SOUTH ");

                    player.getDialogueManager().startDialogue("OptionSelectionD", "what Command would u like?", options.toArray(new String[options.size()]), options.toArray(new String[options.size()]), (OptionSelectionD.SelectionEvent) (selector, option) -> {
                    int page = selector.getPage();
                    switch (page) {
                        case 0:
                            selector.close();
                            switch (option) {
                                case OptionSelectionD.OptionSelector.OPTION_1:
                                    player.sendMessage(Colors.RED + "You receive max hit buff your damage will be op ;)" +
                                            "!");
                                    Settings.static_damage_buff = 55;// static damage modifier to all combat stats
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_2:
                                    player.sendMessage(Colors.RED + "You open clue scroll Using Command Mod ;)" +
                                            "!");
                                    player.getTreasureTrails().giveRewards(2);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_3:
                                    player.sendMessage(Colors.RED + "You Active Your Prayer Mod;)" +
                                            "!");
                                    Prayer.prayerDrainRateMod = Double.parseDouble(String.valueOf(1000));
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_4:
                                    player.sendMessage(Colors.RED + "you set your loyal points to 100k;)" +
                                            "!");
                                    player.setLoyaltyPoints(Integer.valueOf(String.valueOf(100000)));
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_5:
                                    player.sendMessage(Colors.RED + "you set AtaraxiaCoins to 100k;)" +
                                            "!");
                                    player.setAtaraxiaCoins(Integer.valueOf(String.valueOf(100000)));
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_6:
                                    player.sendMessage(Colors.RED + "you set all your invention materials to 100k;)" +

                                    "!");
                                    for (int i = 0; i < player.getInventionManager().getMaterials().length; i++)
                                        player.getInventionManager().getMaterials()[i] += 100000;
                                    for (int i = 0; i < player.getInventionManager().discoveredBluePrints.length; i++)
                                        player.getInventionManager().discoveredBluePrints[i] = true;
                                    player.getInventionManager().refreshMaterials();
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_7:
                                    player.sendMessage(Colors.RED + "you set all your level to max;)" +
                                            "!");
                                    for (int i = 0; i < Skills.SKILL_COUNT; i++) {
                                        player.getSkills().addSkillXpRefresh(i, 50000000);
                                        player.getSkills().refresh(i);
                                    }
                                    player.setMax(true);
                                    player.getAchievements().quickFinish();
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_8:
                                    player.sendMessage(Colors.RED + "you moved;)" +
                                            "!");
                                    WorldTile toTile = player.transform(0, 30, 0);
                                    player.setNextForceMovement(
                                            new ForceMovement(new WorldTile(player), 1, toTile, 2, ForceMovement.NORTH));

                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_9:
                                    player.sendMessage(Colors.RED + "you moved;)" +
                                            "!");
                                    WorldTile toTile1 = player.transform(30, 0, 0);
                                    player.setNextForceMovement(
                                            new ForceMovement(new WorldTile(player), 1, toTile1, 2, ForceMovement.EAST));

                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_10:
                                    player.sendMessage(Colors.RED + "you moved;)" +
                                            "!");
                                    WorldTile toTile2 = player.transform(0, -30, 0);
                                    player.setNextForceMovement(
                                            new ForceMovement(new WorldTile(player), 1, toTile2, 2, ForceMovement.SOUTH));

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