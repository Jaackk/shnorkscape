package com.rs.game.player.dialogue.impl.godwars2;

import com.rs.game.player.content.eds.EliteDungeonPartyManager;
import com.rs.game.player.dialogue.Dialogue;
public class ed3gate extends Dialogue {
	//Elite Dungeon Bosses
		@Override
		public void start() {
			sendOptionsDialogue( "<col=f2490c>Elite Dungeon 3 Shadow Reef", "Shadow Reef Start", "Exit");
			stage = 0;
		}
		@Override
		public void run(int interfaceId, int componentId) {
				if (componentId == OPTION_1) {
					end();
					EliteDungeonPartyManager.enterDungeon(player, 3);
			    	
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
