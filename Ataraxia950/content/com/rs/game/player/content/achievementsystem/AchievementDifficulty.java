package com.rs.game.player.content.achievementsystem;

import com.rs.utils.Colors;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Oct 25, 2018.
 */
public enum AchievementDifficulty {

	EASY(Colors.GREEN), MEDIUM(Colors.YELLOW), HARD(Colors.ORANGE), ELITE(Colors.RED);

	private final String difficultyColor;

	AchievementDifficulty(String difficultyColor) {
		this.difficultyColor = difficultyColor;
	}

	public static String toString(AchievementDifficulty difficulty) {
		return (difficulty.difficultyColor + difficulty.name() + "</col>");
	}

	public String getDifficultyColor() {
		return difficultyColor;
	}

}
