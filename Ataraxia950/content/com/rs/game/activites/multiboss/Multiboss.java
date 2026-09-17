package com.rs.game.activites.multiboss;

import com.rs.game.Animation;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.content.FadingScreen;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

import java.io.Serializable;

/**
 * @author Noel Usage: Superclass for creating multi-boss events For Ataraxian
 *         use only! <3
 */

public class Multiboss implements Serializable {

	/**
	 * Serialized ID for saving data
	 */
	private static final long serialVersionUID = 5963962919718397152L;
	/**
	 * All player related data and shizz here
	 */
	public boolean inRaid;
	/**
	 * The player instance
	 */
	private transient Player player;

	/**
	 * The player instance saving to.
	 *
	 * @param player
	 *            The player.
	 */
	public void setPlayer(Player player) {
		this.player = player;
	}

	public void starte(boolean leave) {
		if (inRaid) {

		}
		player.setNextAnimation(new Animation(7376));
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				FadingScreen.fade(player, 0, new Runnable() {

					@Override
					public void run() {
						player.unlock();
						player.setNextAnimation(new Animation(-1));
						if (leave) {
							Dialogue.closeNoContinueDialogue(player);
							player.getHintIconsManager().removeUnsavedHintIcon();
							player.getControlerManager().forceStop();
							player.setNextWorldTile(new WorldTile(2333, 3171, 0));
							inRaid = false;
						} else {
							player.setNextWorldTile(new WorldTile(2002, 6007, 1));
							inRaid = true;
						}
					}
				});
			}
		}, 0);
	}

}