package com.rs.game.player.content.interfaces.clientsettings;

import lombok.Getter;
import lombok.Setter;

/**
 * ataraxia-server
 * paolo 05/09/2019
 * #Shnek6969
 */
public enum PlayerGameSettings {

    NPC_NAMES(true, "When enabled players will have their name above them."),
    TITLES(true, "When player names is enabled also shows the player his title above his/her head."),
    PLAYER_NAMES(true, "When enabled players will have their name above them."),
    SPRITE_HOVERS(true, "When enabled players will have their name above them."),
    REMOVE_ATTACK_OPTION(true, "When enabled players will have their name above them."),
    // Add new  non clientsittings under here, client works with ordinal
    SLAYER_OVERLAY(false, "When enabled you'll see an interface on the left side displaying your slayer task and amount left."), // TODO
    HIDE_FAMILIARS(false, "When enabled you won't see familiars at home."),
    HEALTH_OVERLAY(false,"When enabled you'll see an interface on the left side displaying the health of your opponent."),// TODO
    POTION_OVERLAY(false,"When enabled you'll see an interface on the left side displaying the time left of your potions.");

    @Getter
    @Setter
    private boolean requiresPacket;

    @Getter @Setter
    private String description;

    PlayerGameSettings (boolean isClientSetting, String description){
        this.requiresPacket = isClientSetting;
        this.description = description;
    }

    public static PlayerGameSettings[] VALUES = PlayerGameSettings.values();
}
