package com.rs.game.player.content.easter;

import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.utils.Colors;

@SuppressWarnings("serial")
public class SickEasterBunnyNPC extends NPC {

	public boolean T1, T2, T3, T4, T5, T6;
	
	public SickEasterBunnyNPC(int id, WorldTile tile) {
		super(id, tile, -1, false, false);
		setHitpoints(1);
		setName("Sick Easter Bunny");
		setRandomWalk(0);
		setCantInteract(true);
	}
	
	@Override
	public void processNPC() {
		if(!getName().equals("Sick Easter Bunny"))
			setName("Sick Easter Bunny");
		
		if(getHitpoints() >= 1000 && !T1) { 
			World.sendWorldMessage("<img=7><col=F5AB00> Easter Event: The easter bunny has surpassed 1,000 hitpoints. Keep it going!", false);
			T1 = true;
		}
		
		if(getHitpoints() >= 2500 && !T2) { 
			World.sendWorldMessage("<img=7><col=F5AB00> Easter Event: The easter bunny has surpassed 2,500 hitpoints. Keep it going!", false);
			T2 = true;
		}
		
		if(getHitpoints() >= 4000 && !T3) { 
			World.sendWorldMessage("<img=7><col=F5AB00> Easter Event: The easter bunny has surpassed 4,000 hitpoints. Keep it going!", false);
			T3 = true;
		}
		
		if(getHitpoints() >= 9000 && !T4) { 
			World.sendWorldMessage("<img=7><col=F5AB00> Easter Event: The easter bunny has surpassed 9,000 hitpoints. Keep it going!", false);
			T4 = true;
		}
		
		if(getHitpoints() >= 12000 && !T5) { 
			World.sendWorldMessage("<img=7><col=F5AB00> Easter Event: The easter bunny has surpassed 12,000 hitpoints. Keep it going!", false);
			T5 = true;
		}
		
		if(getHitpoints() >= 15000 && !T6) { 
			World.sendWorldMessage("<img=7><col=F5AB00> Easter Event: The easter bunny has fully recovered thanks to the hardwork and teamwork of every player! Happy Easter.", false);
			T6 = true;
			
			for(Player player : World.getPlayers()) {
				player.sendMessage(Colors.GOLD + "You have received 5 treasure hunter keys for your effort to save the easter bunny! Happy Easter");
				player.setNextGraphics(new Graphics(4459));
				player.getTreasureHunter().setEarnedKeys(player.getTreasureHunter().getEarnedKeys() + 5);
			}
		}

	}

}
