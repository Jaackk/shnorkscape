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

public class jmodnpc extends Dialogue {
		@Override
		public void start() {
			sendOptionsDialogue("<col=f2490c><img=6>      What Npc Page Would You Like", "Page 1", "Page 2", "Page 3", "Page 4", "Page 5");
			stage = 0;
		}
		@Override
		public void run(int interfaceId, int componentId) {
				if (componentId == OPTION_1) {
				  List<String> options = new ArrayList<String>();
                options.add("Switch Back To Player");
                options.add("Telos, the Warden");
                options.add("Animasaurus Rex");
                options.add("Prime Hatchling");
                options.add("Feral Dinosaur");
                options.add("Brutish dinosaur");
                options.add("Venomous dinosaur");
                options.add("Ripper dinosaur");
                options.add("250mill coins");
                options.add("Solak (Withering)");
                player.getDialogueManager().startDialogue("OptionSelectionD", "what npc would u like transfrom to?", options.toArray(new String[options.size()]), options.toArray(new String[options.size()]), (OptionSelectionD.SelectionEvent) (selector, option) -> {
                    int page = selector.getPage();
                    switch (page) {
                        case 0:
                            selector.close();
                            switch (option) {
                                case OptionSelectionD.OptionSelector.OPTION_1:
                                    player.getAppearence().transformIntoNPC(-1);
                                break;
                                case OptionSelectionD.OptionSelector.OPTION_2:
                                    player.getAppearence().transformIntoNPC(22925);
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
			if (componentId == OPTION_2) {
                List<String> options = new ArrayList<String>();
                options.add("Switch Back To Player");
                options.add("Bill");
                options.add("Dave");
                options.add("Steve");
                options.add("Bladed spider");
                options.add("Imbued spider");
                options.add("Spitting spider");
                options.add("Mallory");
                options.add("Ellie");
                options.add("Barry");
                player.getDialogueManager().startDialogue("OptionSelectionD", "what npc would u like transfrom to?", options.toArray(new String[options.size()]), options.toArray(new String[options.size()]), (OptionSelectionD.SelectionEvent) (selector, option) -> {
                    int page = selector.getPage();
                    switch (page) {
                        case 0:
                            selector.close();
                            switch (option) {
                                case OptionSelectionD.OptionSelector.OPTION_1:
                                    player.getAppearence().transformIntoNPC(-1);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_2:
                                    player.getAppearence().transformIntoNPC(19480);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_3:
                                    player.getAppearence().transformIntoNPC(19481);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_4:
                                    player.getAppearence().transformIntoNPC(19482);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_5:
                                    player.getAppearence().transformIntoNPC(19458);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_6:
                                    player.getAppearence().transformIntoNPC(19459);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_7:
                                    player.getAppearence().transformIntoNPC(19460);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_8:
                                    player.getAppearence().transformIntoNPC(20536);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_9:
                                    player.getAppearence().transformIntoNPC(20537);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_10:
                                    player.getAppearence().transformIntoNPC(20535);
                                    break;
                            }
                            return;
                    }
                });
			}
			if (componentId == OPTION_3) {
                List<String> options = new ArrayList<String>();
                options.add("Switch Back To Player");
                options.add("General Awwdor");
                options.add("Commander Miniana");
                options.add("Nexterminator");
                options.add("Corporeal Puppy");
                options.add("King Black Dragonling");
                options.add("Queen Black Dragonling");
                options.add("Ghost");
                options.add("Vorago");
                options.add("Vorago Bombi");
                player.getDialogueManager().startDialogue("OptionSelectionD", "what npc would u like transfrom to?", options.toArray(new String[options.size()]), options.toArray(new String[options.size()]), (OptionSelectionD.SelectionEvent) (selector, option) -> {
                    int page = selector.getPage();
                    switch (page) {
                        case 0:
                            selector.close();
                            switch (option) {
                                case OptionSelectionD.OptionSelector.OPTION_1:
                                    player.getAppearence().transformIntoNPC(-1);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_2:
                                    player.getAppearence().transformIntoNPC(20532);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_3:
                                    player.getAppearence().transformIntoNPC(20533);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_4:
                                    player.getAppearence().transformIntoNPC(20534);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_5:
                                    player.getAppearence().transformIntoNPC(20538);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_6:
                                    player.getAppearence().transformIntoNPC(20544);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_7:
                                    player.getAppearence().transformIntoNPC(20551);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_8:
                                    player.getAppearence().transformIntoNPC(1549);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_9:
                                    player.getAppearence().transformIntoNPC(17181);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_10:
                                    player.getAppearence().transformIntoNPC(20461);
                                    break;
                            }
                            return;
                    }
                });
			}
            if (componentId == OPTION_4) {
                List<String> options = new ArrayList<String>();
                options.add("J Mod");
                options.add("Flying Shadow Drake");
                options.add("Baby Shadow Drake");
                options.add("Flying Fire drake");
                options.add("Shadow gorilla");
                options.add("Small Shadow gorilla");
                options.add("Rawrvek");
                options.add("Nylessa");
                options.add("Greg");
                options.add("Vindiddy");
                player.getDialogueManager().startDialogue("OptionSelectionD", "what npc would u like transfrom to?", options.toArray(new String[options.size()]), options.toArray(new String[options.size()]), (OptionSelectionD.SelectionEvent) (selector, option) -> {
                    int page = selector.getPage();
                    switch (page) {
                        case 0:
                            selector.close();
                            switch (option) {
                                case OptionSelectionD.OptionSelector.OPTION_1:
                                    player.getAppearence().transformIntoNPC(20587);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_2:
                                    player.getAppearence().transformIntoNPC(21042);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_3:
                                    player.getAppearence().transformIntoNPC(21034);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_4:
                                    player.getAppearence().transformIntoNPC(21041);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_5:
                                    player.getAppearence().transformIntoNPC(24000);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_6:
                                    player.getAppearence().transformIntoNPC(23999);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_7:
                                    player.getAppearence().transformIntoNPC(22514);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_8:
                                    player.getAppearence().transformIntoNPC(22515);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_9:
                                    player.getAppearence().transformIntoNPC(22517);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_10:
                                    player.getAppearence().transformIntoNPC(22513);
                                    break;
                            }
                            return;
                    }
                });
            }
            if (componentId == OPTION_5) {
                List<String> options = new ArrayList<String>();
                options.add("Lil' Tuzzy");
                options.add("Krar Jnr");
                options.add("Yakaminu");
                options.add("Reeves");
                options.add("Ahrim the Bobbled");
                options.add("Dharok the Bobbled");
                options.add("Guthan the Bobbled");
                options.add("Karil the Bobbled");
                player.getDialogueManager().startDialogue("OptionSelectionD", "what npc would u like transfrom to?", options.toArray(new String[options.size()]), options.toArray(new String[options.size()]), (OptionSelectionD.SelectionEvent) (selector, option) -> {
                    int page = selector.getPage();
                    switch (page) {
                        case 0:
                            selector.close();
                            switch (option) {
                                case OptionSelectionD.OptionSelector.OPTION_1:
                                    player.getAppearence().transformIntoNPC(21402);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_2:
                                    player.getAppearence().transformIntoNPC(21404);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_3:
                                    player.getAppearence().transformIntoNPC(18033);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_4:
                                    player.getAppearence().transformIntoNPC(24021);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_5:
                                    player.getAppearence().transformIntoNPC(23501);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_6:
                                    player.getAppearence().transformIntoNPC(23502);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_7:
                                    player.getAppearence().transformIntoNPC(23503);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_8:
                                    player.getAppearence().transformIntoNPC(23504);
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