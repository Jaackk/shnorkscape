package com.rs.game.player.content.achievements;

import com.rs.game.player.Player;
import com.rs.game.player.content.achievements.ctx.AchievementDifficulty;
import com.rs.game.player.content.achievements.ctx.AchievementList;
import com.rs.game.player.content.achievements.ctx.AchievementState;
import com.rs.game.player.content.achievements.ctx.Rewards;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * @author David (Chryonic)
 */
public class Achievement implements Serializable {

    private static final long serialVersionUID = -3783128889698721831L;
    private String name;
    private Rewards rewards;
    private AchievementDifficulty difficulty;
    private AchievementState state;
    @Setter
    transient Player player;
    @Getter
    private LinkedHashMap<AchievementList, AchievementState> achievements;
    int AMOUNT_COMPLETED = 2, AMOUNT_TO_COMPLETE = 1;

    @Getter
    @Setter
    private Object[][] achievementData;

    public Achievement(Player player) {
        this.player = player;
    }
}
