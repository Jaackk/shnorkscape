package com.rs.game.player.dialogue.impl;

import com.rs.game.WorldTile;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;

import java.util.ArrayList;
import java.util.List;

public class HomeTeleportD extends Dialogue {

	@Override
	public void start() {
		//sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Lodestone Network", "Homes & GE");
        Magic.openLodestoneNetwork(player);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (componentId == OPTION_1) {
			Magic.openLodestoneNetwork(player);
		} else if (componentId == OPTION_2) {
			 List<String> options = new ArrayList<String>();
                    options.add("Home");
                    options.add("Market-Ge");
                    options.add("Home-New");
                     options.add("Home-In-The-Making");

                    player.getDialogueManager().startDialogue("OptionSelectionD", "Where do you want to go?", options.toArray(new String[options.size()]), options.toArray(new String[options.size()]), (OptionSelectionD.SelectionEvent) (selector, option) -> {
                        int page = selector.getPage();
                        switch (page) {
                            case 0:
                                selector.close();
                                switch (option) {
                                    case OptionSelectionD.OptionSelector.OPTION_1:
                                       
                                       
							Magic.vineTeleport(player, new WorldTile(5414, 2339, 0));

                                      
                                        break;
                                         case OptionSelectionD.OptionSelector.OPTION_2:
                                       
                                       
								Magic.vineTeleport(player, new WorldTile(3162, 3465, 0));

                                      
                                        break;
                                         case OptionSelectionD.OptionSelector.OPTION_3:
                                       
                                       
								Magic.vineTeleport(player, new WorldTile(3554, 6056, 1));

                                      
                                        break;
                                         case OptionSelectionD.OptionSelector.OPTION_4:
                                       
                                       
								Magic.vineTeleport(player, new WorldTile(3169, 2729, 0));

                                      
                                        break;



                                }
                                return;
                        }

                    });
                }
			
	

	}

	@Override
	public void finish() {
		
	}
}
