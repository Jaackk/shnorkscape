package com.rs.game.player.content.achievements.ctx;

import lombok.Getter;

/**
 * @author David (Chryonic)
 */
public enum AchievementDifficulty {
	EASY("<col=019e16>EASY</col>"),
	MEDIUM("<col=e2df16>MEDIUM</col>"),
	HARD("<col=e27516>HARD</col>"),
	ELITE("<col=e21616>ELITE</col>");

	@Getter
	private final String name;

	AchievementDifficulty(String name) {
		this.name = name;
	}
}
