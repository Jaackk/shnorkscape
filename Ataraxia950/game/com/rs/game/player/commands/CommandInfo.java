package com.rs.game.player.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ataraxia-server
 * paolo 08/09/2019
 * #Shnek6969
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandInfo {

    CommandRights rank();
    String[] possibleCommands();
    String description();
}
