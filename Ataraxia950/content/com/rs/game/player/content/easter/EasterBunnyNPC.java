package com.rs.game.player.content.easter;

import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activites.EasterEvent;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

/*
 * Easter Bunny for Ataraxia Easter Event 2017
 * @author Movee
 * @date 13 04 2017
 */

@SuppressWarnings("serial")
public class EasterBunnyNPC extends NPC {

	private static final int[] EGG = { 7928, 7929, 7930, 7931, 7932, 7933 };
	
	private static int spawnEgg;
	
	public EasterBunnyNPC(int id, WorldTile tile) {
		super(id, tile, -1, false, false);
		setNoDistanceCheck(true);
		setName("Easter Bunny");
		setRun(true);
		setRandomWalk(1);
	}
	
	@Override
	public void processNPC() {
		
		if(!hasWalkSteps())
			sendNewRoutePath();
		
		if(Utils.random(85) == 0)
			dropEgg();
	}
	
	@Override
	public void sendDeath(Entity bunny) {
		super.sendDeath(bunny);
	}
	
	public void sendNewRoutePath() {
		WorldTile tile = findCoordinates();
		
		if(tile != null) {
			addWalkSteps(tile.getX(), tile.getY());
		} 
	}
	
	public WorldTile findCoordinates() {
		WorldTile tile = new WorldTile(getX(), getY(), getPlane());
		int X = Utils.random(20);
		int Y = Utils.random(20);
		
		int random = Utils.random(8);
		
		switch(random) {
			case 0:
				tile = new WorldTile(getX() - X, getY() + Y, getPlane());
				//Logger.getGlobal().info("-X+Y");
				break;
			case 1:
				tile = new WorldTile(getX() + X, getY() - Y, getPlane());
				//Logger.getGlobal().info("+X-Y");
				break;
			case 2:
				tile = new WorldTile(getX() + X, getY() + Y, getPlane());
				//Logger.getGlobal().info("+X+Y");
				break;
			case 3:
				tile = new WorldTile(getX() + X, getY() + Y, getPlane());
				//Logger.getGlobal().info("+X+Y");
				break;
			case 4:
				tile = new WorldTile(getX() - X, getY() - Y, getPlane());
				//Logger.getGlobal().info("-X-Y");
				break;
			case 5:
				tile = new WorldTile(getX() + X, getY(), getPlane());
				//Logger.getGlobal().info("+X");
				break;
			case 6:
				tile = new WorldTile(getX() - X, getY(), getPlane());
				//Logger.getGlobal().info("-X");
				break;
			case 7:
				tile = new WorldTile(getX(), getY() - Y, getPlane());
				//Logger.getGlobal().info("-Y");
				break;
			case 8:
				tile = new WorldTile(getX(), getY() + Y, getPlane());
				//Logger.getGlobal().info("+Y");
				break;
		}
		

		return tile;
	}
	
	public void dropEgg() {
		spawnEgg = EGG[Utils.random(EGG.length)];
		World.addGroundItem(new Item(spawnEgg, 1), new WorldTile(this.getX(), this.getY(), this.getPlane()), 1500);
		for(Player player : World.getPlayers()) {
			if(player.startedEasterEvent && !player.easterToggle)
				player.sendMessage(Colors.GOLD + "<shad=000000><img=6>News: The easter bunny has dropped an easter egg somewhere in " + EasterEvent.getDroppedArea() + ".");
		}
		//World.sendWorldMessage(Colors.gold + "<shad=000000><img=6>News: The easter bunny has dropped an easter egg somewhere in " + EasterEvent.getDroppedArea() + ".", false);
		
	}
	
	public void exchangeReward() {
		
	}

}
