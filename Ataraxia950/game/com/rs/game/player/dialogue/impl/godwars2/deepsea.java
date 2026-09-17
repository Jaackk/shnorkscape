package com.rs.game.player.dialogue.impl.godwars2;

import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
public class deepsea extends Dialogue {
	//Elite Dungeon Bosses
		@Override
		public void start() {
			sendOptionsDialogue( "<col=f2490c>Deep Sea Fishing", "Teleport", "Exit");
			stage = 0;
		}
		@Override
		public void run(int interfaceId, int componentId) {
				if (componentId == OPTION_1) {

					
					player.setNextWorldTile(new WorldTile(2135, 7105, 0));{
					end();
					}
			    	
		    	}
			

			if (componentId == OPTION_2) {
				end();
			}
			
			return;
		}


		@Override
		public void finish() {
			
		}
	}