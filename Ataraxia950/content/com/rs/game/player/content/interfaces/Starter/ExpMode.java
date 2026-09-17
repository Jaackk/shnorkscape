package com.rs.game.player.content.interfaces.Starter;

import com.rs.Settings;
import lombok.Getter;

/**
 * ataraxia-server
 * paolo 13/06/2019
 * #Shnek6969
 */
public enum ExpMode {

    NOVICE("Novice", "<col=ff0000>"+Settings.INTERM_XP + "</col> x EXP & <col=ff0000>0%</col> drop boost"),
    INTERMEDIATE("Intermediate","<col=ff0000>"+Settings.INTERMEDIATE_XP + "</col> x EXP & <col=ff0000>5%</col> drop boost"),
    EXPERT("Expert","<col=ff0000>"+Settings.VET_XP + "</col> x EXP & <col=ff0000>10%</col> drop boost"),
    LEGENDARY("Legendary ","<col=ff0000>"+Settings.EXPERT_XP + "</col> x EXP & <col=ff0000>15%</col> drop boost");

    @Getter
    private final String fullname;
    @Getter
    private final String description;

    ExpMode(String fullname, String description){
        this.description = description;
        this.fullname = fullname;
    }

}


