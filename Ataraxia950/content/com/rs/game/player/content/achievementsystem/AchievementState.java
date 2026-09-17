package com.rs.game.player.content.achievementsystem;

import com.rs.utils.Colors;
import com.rs.utils.Utils;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Oct 25, 2018.
 */
public enum AchievementState {

	NOT_STARTED(Colors.RED), STARTED(Colors.YELLOW), COMPLETED(Colors.GREEN);

	private final String stateColor;

	/**
	 * Constructs a new class.
	 */
    AchievementState(String stateColor) {
		this.stateColor = stateColor;
	}

	public static String toString(AchievementState state) {
		return (state.stateColor + Utils.formatString(state.name()) + "</col>");
	}

	public String getStateColor() {
		return stateColor;
	}

}
