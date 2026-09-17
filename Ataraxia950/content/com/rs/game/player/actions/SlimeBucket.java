package com.rs.game.player.actions;

import com.rs.game.Animation;
import com.rs.game.player.Player;

/**
 * A class handling the {@link Action} of adding slime to a bucket.
 * 
 * @author _Waterfiend <skype:alco-wahidi>
 *
 * Created in Apr 29, 2017 at 11:59:37 AM.
 */
public class SlimeBucket extends Action {

	/**
	 * Fills the bucket with slime.
	 * @param player The player that fills the bucket.
	 * @return the slime_bucket
	 */
	public boolean fillBucket(Player player) {
		if (player.getInventory().containsItem(1925, 1)) {
			player.setNextAnimation(new Animation(4471));
			player.getInventory().deleteItem(1925, 1);
			player.getInventory().addItem(4286, 1);
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rs.game.player.actions.Action#process(com.rs.game.player.Player)
	 */
	@Override
	public boolean process(Player player) {
        return player.getInventory().containsItem(1925, 1);
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rs.game.player.actions.Action#processWithDelay(com.rs.game.player.Player)
	 */
	@Override
	public int processWithDelay(Player player) {
		if (fillBucket(player)) {
			return 1;
		}
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rs.game.player.actions.Action#start(com.rs.game.player.Player)
	 */
	@Override
	public boolean start(Player player) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rs.game.player.actions.Action#stop(com.rs.game.player.Player)
	 */
	@Override
	public void stop(Player player) {
       /* empty */
	}
}

