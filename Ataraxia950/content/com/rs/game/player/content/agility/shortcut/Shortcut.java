package com.rs.game.player.content.agility.shortcut;

import com.rs.game.Animation;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.RouteEvent;
import com.rs.game.player.content.agility.AgilityManager;
import com.rs.game.player.content.agility.Failable;
import com.rs.game.player.content.agility.Succeedable;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/**
 * The <tt>Shortcut</tt> interface defines
 * the contract for which all <tt>Shortcut</tt> subtypes
 * must supply. All <tt>Shortcut</tt> objects are {@link Succeedable}
 * by default.
 */
public interface Shortcut extends Succeedable {

	/** @return the level requried to traverse this shortcut  */
	int getLevel();

	/** @return the object IDs associated with this shortcut */
	int[] getObjectIds();

	/** @return the start <tt>WorldTile</tt> for this shortcut */
	WorldTile getStart();

	/** @return the end <tt>WorldTile</tt> for this shortcut */
	WorldTile getEnd();

	/**
	 * Performs the shortcut interaction on the supplied <tt>Player</tt>.
	 * @param player the player to process the shortcut for
     */
	default void process(final Player player) {
		final WorldTile start = getStart();
		final WorldTile end = getEnd();
		final boolean forward = AgilityManager.getClosestTile(player, start, end).matches(start);
		
		final boolean success = AgilityManager.calculateSuccess(player, this);
		player.setRouteEvent(new RouteEvent(forward ? start : end, () -> {
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					if (player.getSkills().getLevel(Skills.AGILITY) < getLevel()) {
						player.sendMessage("You need an Agility level of at least " + getLevel() + " to use this Agility shortcut.");
						return;
					}
					if(success)
						succeed(player, forward);
					else
						((Failable) Shortcut.this).fail(player, forward);
				}
			});
		}));
	}
	
	/**
	 * Default reset animation
	 */
	Animation RESET = new Animation(-1);

}
