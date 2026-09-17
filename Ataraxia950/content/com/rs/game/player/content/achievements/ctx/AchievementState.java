package com.rs.game.player.content.achievements.ctx;

import lombok.Getter;

/**
 * @author David (Chryonic)
 */
public enum AchievementState {
	NOT_STARTED("<col=e21616>Not Started</col>"),
	STARTED("<col=e2d716>Started</col>"),
	COMPLETED("<col=2ecc00>Completed</col>");

	@Getter
	private final String name;

	AchievementState(String name) {
		this.name = name;
	}
}
