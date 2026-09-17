package com.rs.game.player.content.ectofuntus;

import com.rs.game.Animation;
import com.rs.game.player.Player;

/**
 * A class handling the {@link Ectophial} item to teleport.
 * 
 * @author _Waterfiend <skype:alco-wahidi>
 *
 * Created in Apr 29, 2017 at 12:02:03 PM.
 */
public class Ectophial {
	
	/**
	 * The player that uses the {@link Ectophial}.
	 */
	protected Player player;

	/**
	 * A constructor of the {@link Ectophial}.
	 * @param player The player to use the {@link Ectophial}.
	 */
	public Ectophial(Player player) {
		this.player = player;
	}
	/**
	 * Manages the ectophial teleport.
	 * @param player The player that uses the ectophial.
	 */
/*	public void handleEctoTeleport(final Player player) {
		WorldTasksManager.schedule(new WorldTask() {
			int loop;
			@Override
			public void run() {
				player.getInterfaceManager().closeChatBoxInterface();
				if (loop == 0) {
					player.setNextAnimation(new Animation(9609));
					player.setNextGraphics(new Graphics(1688));
				} else if (loop == 2) {
					player.getInventory().deleteItem(4251, 1);
					player.setNextAnimation(new Animation(8939));
					player.setNextGraphics(new Graphics(1678));
				} else if (loop == 4) {
					player.getInventory().addItem(4252, 1);
					player.setNextWorldTile(new WorldTile(3659, 3517, 0));
					player.setNextAnimation(new Animation(8941));
					player.setNextGraphics(new Graphics(1679));
					player.closeInterfaces();
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}*/

	/**
	 * Refills the ectophial after being used.
	 * @param player The player.
	 */
	public void refillEctophial(Player player) {
		if (player.getInventory().containsItem(4252, 1)) {
			player.lock(2);
			player.getInventory().deleteItem(4252, 1);
			player.sendMessage("You refill the ectophial.");
			player.setNextAnimation(new Animation(1649));
			player.getInventory().addItem(4251, 1);
		} else {
			player.sendMessage("You are unable to workship the Ectofuntus at this time.");
		}
	}

}
