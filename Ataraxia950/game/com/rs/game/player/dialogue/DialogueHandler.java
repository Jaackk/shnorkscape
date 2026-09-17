package com.rs.game.player.dialogue;

import com.rs.utils.Logger;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class DialogueHandler {

	private static final HashMap<Object, Class<? extends Dialogue>> handledDialogues = new HashMap<Object, Class<? extends Dialogue>>();

	private DialogueHandler() {

	}

	@SuppressWarnings("rawtypes")
	private static List<Class> findClasses(final File directory, final String packageName) throws ClassNotFoundException {
		final List<Class> classes = new ArrayList<Class>();
		if (!directory.exists()) {
			return classes;
		}
		final File[] files = directory.listFiles();
		for (final File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file, packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
			}
		}
		return classes;
	}

	@Deprecated
	public static final void init() {
		throw new RuntimeException("No longer using reflection to find dialogues.");
		/**final String fileLoc = "build/classes/java/main/com/rs/game/player/dialogue/impl";
		final String packageDir = "com.rs.game.player.dialogue.impl";
		try {
			final List<Class> files = findClasses(new File(fileLoc), packageDir);
			for (final Class<Dialogue> c : files) {
				if (Dialogue.class.isAssignableFrom(c)) {
					handledDialogues.put("" + c.getSimpleName() + "", (Class<Dialogue>) Class.forName(c.getCanonicalName()));
				}
			}
			//handledDialogues.put("RiseOfTheSixEnterD", (Class<Dialogue>) Class.forName(RiseOfTheSixEnterD.class.getCanonicalName()));

			//handledDialogues.put("RiseOfTheSixDisruptionD",
			//		(Class<Dialogue>) Class.forName(RiseOfTheSixDisruptionD.class.getCanonicalName()));
			//handledDialogues.put("RiseOfTheSixLootingD", (Class<Dialogue>) Class.forName(RiseOfTheSixLootingD.class.getCanonicalName()));
			//handledDialogues.put("AoDInstanceD", (Class<Dialogue>) Class.forName(AoDInstanceD.class.getCanonicalName()));
			//handledDialogues.put("AoDInstanceCreationD", (Class<Dialogue>) Class.forName(AoDInstanceCreationD.class.getCanonicalName()));
			//handledDialogues.put("AoDInstanceJoiningD", (Class<Dialogue>) Class.forName(AoDInstanceJoiningD.class.getCanonicalName()));
			//handledDialogues.put("AoDFightEnterD", (Class<Dialogue>) Class.forName(AoDFightEnterD.class.getCanonicalName()));

			//handledDialogues.put("EasterDialogue", (Class<Dialogue>) Class.forName(EasterDialogue.class.getCanonicalName()));
			//handledDialogues.put("XmasDialogue", (Class<Dialogue>) Class.forName(XmasDialogue.class.getCanonicalName()));
			//handledDialogues.put("GroupD", (Class<Dialogue>) Class.forName(GroupD.class.getCanonicalName()));
			handledDialogues.put("AraxxorStartD", (Class<Dialogue>) Class.forName(AraxxorStartDialogue.class.getCanonicalName()));
			handledDialogues.put("AcceptAraxxorD", (Class<Dialogue>) Class.forName(AcceptAraxInviteD.class.getCanonicalName()));
			Logger.getGlobal().info(handledDialogues.size() + " dialogues initiated..");
		} catch (final ClassNotFoundException e) {
			Logger.getGlobal().catching(e);
		}*/
	}

	public static final void add(final Class<? extends Dialogue> c) {
		try {
			if (c.isAnonymousClass() || !Modifier.isPublic(c.getModifiers())) {
				return;
			}
			handledDialogues.put(c.getSimpleName(), c);
		} catch (final Exception e) {
			Logger.getGlobal().catching(e);
		}
	}

	public static final Dialogue getDialogue(final Object key) {
		if (key instanceof Dialogue) {
			return (Dialogue) key;
		}
		final Class<? extends Dialogue> classD = handledDialogues.get(key);
		if (classD == null) {
			return null;
		}
		try {
			return classD.newInstance();
		} catch (final Throwable e) {
			Logger.getGlobal().catching(e);
		}
		return null;
	}
}