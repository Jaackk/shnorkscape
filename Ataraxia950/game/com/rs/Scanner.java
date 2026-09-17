package com.rs;

import java.util.Map;

import com.rs.game.player.actions.magic.lunar.DefaultSpell;
import com.rs.game.player.actions.magic.lunar.ItemSpell;
import com.rs.game.player.actions.magic.lunar.NPCSpell;
import com.rs.game.player.actions.magic.lunar.ObjectSpell;
import com.rs.game.player.actions.magic.lunar.PlayerSpell;
import com.rs.game.player.actions.magic.lunar.Spell;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandHandler;
import com.rs.game.player.content.Magic;
import com.rs.game.player.content.eds.rooms.EliteDungeonHandledRoom;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.player.dialogue.DialogueHandler;
import com.rs.utils.Logger;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.SubclassMatchProcessor;
import lombok.val;

/**
 * @author Kris | 8. sept 2018 : 22:47:25
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>
 */
public class Scanner {

	public static final void scan() {
		val ms = System.currentTimeMillis();
		val scanner = new FastClasspathScanner(Scanner.class.getPackage().getName());
		scanner.matchSubclassesOf(Dialogue.class, (SubclassMatchProcessor<Dialogue>) DialogueHandler::add);
		scanner.matchSubclassesOf(Command.class, (SubclassMatchProcessor<Command>) CommandHandler::add);
		scanner.matchClassesImplementing(DefaultSpell.class, c -> addSpell(Magic.DEFAULT_LUNAR_SPELLS, c));
		scanner.matchClassesImplementing(PlayerSpell.class, c -> addSpell(Magic.PLAYER_LUNAR_SPELLS, c));
		scanner.matchClassesImplementing(ObjectSpell.class, c -> addSpell(Magic.OBJECT_LUNAR_SPELLS, c));
		scanner.matchClassesImplementing(NPCSpell.class, c -> addSpell(Magic.NPC_LUNAR_SPELLS, c));
		scanner.matchClassesImplementing(ItemSpell.class, c -> addSpell(Magic.ITEM_LUNAR_SPELLS, c));
	    scanner.matchSubclassesOf(EliteDungeonHandledRoom.class, (SubclassMatchProcessor<EliteDungeonHandledRoom>) EliteDungeonHandledRoom::add);
		scanner.scan();
		Logger.getGlobal().info("Scanning classes took " + (System.currentTimeMillis() - ms) + " milliseconds.");
	}

	private static <T> void addSpell(Map<Integer, T> map, Class<? extends T> clazz) {
		try {
			final Spell spell = (Spell) Class.forName(clazz.getCanonicalName()).newInstance();
			addSpell(map, spell.getId(), clazz);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			Logger.getGlobal().catching(e);
		}
	}

	private static <T> void addSpell(Map<Integer, T> map, int id, Class<? extends T> clazz) {
		try {
			map.put(id, (T) Class.forName(clazz.getCanonicalName()).newInstance());
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			Logger.getGlobal().catching(e);
		}
	}
	
}
