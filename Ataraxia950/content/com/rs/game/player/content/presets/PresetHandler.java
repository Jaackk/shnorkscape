package com.rs.game.player.content.presets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.rs.game.player.Player;

import lombok.Getter;
import lombok.Setter;

/**
 * ataraxia-server
 * paolo 15/06/2019
 * #Shnek6969
 */
public class PresetHandler implements Serializable {

    private static final long serialVersionUID = 15112515;

    private final Player player;
    @Getter
    @Setter
    private List<Preset> presets;

    public PresetHandler(Player player) {
        this.player = player;
        presets = new ArrayList<>();
    }

}
