package com.rs.game.player.commands;

import com.rs.utils.Logger;

import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * ataraxia-server
 * paolo 08/09/2019
 * #Shnek6969
 */
public class CommandHandler {

	/**
	 * Java HashMap stores references to Objects. If you store same object with two different keys, the keys will point to the same value. ;)
	 */
	public static final HashMap<Object, Class<? extends Command>> possibleCommands = new HashMap<Object, Class<? extends Command>>();

	public static final void add(final Class<? extends Command> c) {
		try {
			if (c.isAnonymousClass() || !Modifier.isPublic(c.getModifiers())) {
				return;
			}

			CommandInfo metaData =  c.getAnnotation(CommandInfo.class);
			if(metaData == null){
				Logger.getGlobal().info("[Command error] command -> "+c.getSimpleName()+" seems to not have the right metadata.");
				return;
			}

			for(String s : metaData.possibleCommands()){
				possibleCommands.put(s.toLowerCase(), c);
			}
		} catch (final Exception e) {
			Logger.getGlobal().catching(e);
		}
	}

	public static Command forSyntax(String syntax) {
		final Class<? extends Command> command = possibleCommands.get(syntax);
		if (command == null) {
			return null;
		}
		try {
			return command.newInstance();
		} catch (final Throwable e) {
			Logger.getGlobal().catching(e);
		}
		return null;
	}

}