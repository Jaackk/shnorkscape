package com.rs.game.player.content.agility;

import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.agility.courses.Course;
import com.rs.game.player.content.agility.courses.Obstacle;
import com.rs.game.player.content.agility.courses.gnome_agility.GnomeAgilityCourse;
import com.rs.game.player.content.agility.shortcut.Shortcut;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AgilityManager {

	private static final Map<Integer, Shortcut> SHORTCUTS = new HashMap<>();

	public static final List<Course> HANDLED_COURSES = new ArrayList<>();
	private static final Map<Integer, Obstacle> COURSE_OBSTACLES = new HashMap<>();

	public static void initCourses() {
		mapCourse(new GnomeAgilityCourse());
	}

	private static void mapCourse(Course course) {
		HANDLED_COURSES.add(course);
		course.getObstacles().forEach(o -> COURSE_OBSTACLES.put(o.getObjectId(), o));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void init() {
		String fileLoc = "build/classes/java/main/com/rs/game/player/content/agility/shortcut/impl";
		String packageDir = "com.rs.game.player.content.agility.shortcut.impl";
		try {
			List<Class> files = Utils.findClasses(new File(fileLoc), packageDir);
			int classes = 0;
			for (Class<Shortcut> c : files)
				if (Shortcut.class.isAssignableFrom(c))
					try {
						final Shortcut shortcut = (Shortcut) Class.forName(c.getCanonicalName()).newInstance();
						classes++;
						for (int i : shortcut.getObjectIds())
							SHORTCUTS.put(i, shortcut);
					} catch (InstantiationException | IllegalAccessException e) {
						Logger.getGlobal().catching(e);
					}
			Logger.getGlobal().info(classes + "/73 agility shortcuts initiated..");
		} catch (ClassNotFoundException e) {
			Logger.getGlobal().catching(e);
		}
	}

	/**
	 * Calculates the probablility of success for a given {@link Shortcut}.
	 * @param player the player traversing the shortcut
	 * @param shortcut the <tt>Shortcut</tt> being traversed
     * @return <tt>true</tt> if the player will successfully traverse,
	 * 		   false otherwise
     */
	public static boolean calculateSuccess(final Player player, final Shortcut shortcut) {
		if(!(shortcut instanceof Failable))
			return true;
		final int pLevel = player.getSkills().getLevel(Skills.AGILITY);
		final int sLevel = shortcut.getLevel();
		double scale = 0.5 + ((pLevel - sLevel) / ((sLevel + 30) - sLevel)) / 2;
		return Math.random() < scale;
	}

	public static boolean calculateSuccess(final Player player, final Obstacle obstacle) {
		if(!(obstacle instanceof Failable))
			return true;
		final int pLevel = player.getSkills().getLevel(Skills.AGILITY);
		final int oLevel = obstacle.getLevelReq();
		double scale = 0.5 + ((pLevel - oLevel) / ((oLevel + 30) - oLevel)) / 2;
		return Math.random() < scale;
	}

	/**
	 * Checks if the given object ID corresponds to an agility shortcut,
	 * and processes the player through the shortcut if so.
	 * @param player the player to traverse the shortcut
	 * @param object the object corresponding to a potential shortcut
     * @return <tt>true</tt> if the object supplied corresponds to a shortcut,
	 * 		   false otherwise
     */
	public static boolean handleShortcut(final Player player, final WorldObject object) {
		final Shortcut shortcut = SHORTCUTS.get(object.getId());
		if (shortcut == null)
			return false;
		shortcut.process(player);
		return true;
	}

	public static boolean handleObstacle(final Player player, final WorldObject object) {
		final Obstacle obstacle = COURSE_OBSTACLES.get(object.getId());
		if (obstacle == null)
			return false;
		if (player.getSkills().getLevel(Skills.AGILITY) >= obstacle.getLevelReq())
			obstacle.process(player);
		else {
			player.getDialogueManager().startDialogue("SimpleMessage",
					"You need an Agility level of at least "
							+ obstacle.getLevelReq() + " to use this Agility shortcut.");
		}
		return true;
	}

	/**
	 * Compares the supplied start and end tiles to determine
	 * which tile the player is closest to. This is used for determining
	 * directionality in traversing the shortcut.
	 * @param player the player to compare the tiles to
	 * @param start the shortcut start tile
	 * @param end the shortcut end tile
     * @return the <tt>WorldTile</tt> the player is closest to, either
	 * 		   <tt>start</tt> or <tt>end</tt>
     */
	public static WorldTile getClosestTile(final Player player, final WorldTile start, final WorldTile end) {
		if (start.getFurthestDistance(player) < end.getFurthestDistance(player))
			return start;
		return end;
	}
	
}
