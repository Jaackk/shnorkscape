package com.rs.game.player.content.interfaces.Starter;

import com.rs.Settings;
import lombok.Getter;

/**
 * ataraxia-server
 * paolo 13/06/2019
 * #Shnek6969
 */
public enum GameMode {

    NORMALE("Regular", "As a regular player you'll experience the game as you are used to."),
    IRONMAN("<img=17>Ironman", "As an ironman player you won't be able to interact with other players."),
    HARDCODE_IRONMAN("<img=18>Hardcore Ironman", "As an Hardcore ironman you won't be able to interact with other players and you'll have an exp rate of "+ Settings.IRONMAN_XP+" and a bonus droprate of 20%."),
    GROUP_IRONMAN("<img=33>Group Ironman", "As a group ironman, you will only be able to interact with your group. You'll compete with other groups to gain as much points as possible within 3-month periods.");
    @Getter
    private final String fullName;
    @Getter
    private final String description;

    GameMode(String fullName, String description){
        this.fullName = fullName;
        this.description = description;
    }
}