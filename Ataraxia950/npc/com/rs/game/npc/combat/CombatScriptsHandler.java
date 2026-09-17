package com.rs.game.npc.combat;

import com.rs.Settings;
import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.player.content.araxxor.npcs.AraxxiCombat;
import com.rs.game.player.content.araxxor.npcs.AraxxorCombat;
import com.rs.game.player.content.araxxor.npcs.BladedMinionCombat;
import com.rs.game.player.content.araxxor.npcs.ImbuedMinionCombat;
import com.rs.game.player.content.araxxor.npcs.SpittingMinionCombat;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.util.HashMap;

public class CombatScriptsHandler {

	private static final HashMap<Object, CombatScript> cachedCombatScripts = new HashMap<Object, CombatScript>();
	public static final CombatScript DEFAULT_SCRIPT = new Default();

	@SuppressWarnings("rawtypes")
	public static final void init() {
		try {
			final Class[] classes = Utils.getClasses("com.rs.game.npc.combat.impl");
			for (final Class c : classes) {
				if (c.isAnonymousClass()) { // next
					continue;
				}
				final Object o = c.newInstance();
				if (!(o instanceof CombatScript)) {
					continue;
				}
				final CombatScript script = (CombatScript) o;
				for (final Object key : script.getKeys()) {
					cachedCombatScripts.put(key, script);
				}
			}
			final Class[] RoTSclasses = Utils.getClasses("com.rs.game.activities.rots.combat");
			for (final Class c : RoTSclasses) {
				if (c.isAnonymousClass()) { // next
					continue;
				}
				final Object o = c.newInstance();
				if (!(o instanceof CombatScript)) {
					continue;
				}
				final CombatScript script = (CombatScript) o;
				for (final Object key : script.getKeys()) {
					cachedCombatScripts.put(key, script);
				}
			}
			final Class[] AoDclasses = Utils.getClasses("com.rs.game.activities.aod.npc.combat");
			for (final Class c : AoDclasses) {
				if (c.isAnonymousClass()) { // next
					continue;
				}
				final Object o = c.newInstance();
				if (!(o instanceof CombatScript)) {
					continue;
				}
				final CombatScript script = (CombatScript) o;
				for (final Object key : script.getKeys()) {
					cachedCombatScripts.put(key, script);
				}
			}
			if(Settings.ARAXXOR_DEBUG) {
				final Class araxxorCBSclass = AraxxorCombat.class;
				final Class araxxiCBSclass = AraxxiCombat.class;
				final Class bladedMinionCBSclass = BladedMinionCombat.class;
				final Class imbuedMinionCBSclass = ImbuedMinionCombat.class;
				final Class spittingMinionCBSclass = SpittingMinionCombat.class;
				final CombatScript raxScript = (CombatScript) araxxorCBSclass.newInstance();
				final CombatScript raxiScript = (CombatScript) araxxiCBSclass.newInstance();
				final CombatScript bmScript = (CombatScript) bladedMinionCBSclass.newInstance();
				final CombatScript imbuedScript = (CombatScript) imbuedMinionCBSclass.newInstance();
				final CombatScript spittingScript = (CombatScript) spittingMinionCBSclass.newInstance();
				for(final Object key : raxiScript.getKeys()) {
					cachedCombatScripts.put(key, raxiScript);
				}
				for(final Object key : raxScript.getKeys()) {
					cachedCombatScripts.put(key, raxScript);
				}
				for(final Object key : bmScript.getKeys()) {
					cachedCombatScripts.put(key, bmScript);
				}
				for(final Object key : imbuedScript.getKeys()) {
					cachedCombatScripts.put(key, imbuedScript);
				}
				for(final Object key : spittingScript.getKeys()) {
					cachedCombatScripts.put(key, spittingScript);
				}
				Logger.getGlobal().info(cachedCombatScripts.size() + " combat scripts initiated..");
			}
		} catch (final Throwable e) {
			Logger.getGlobal().catching(e);
		}
		final int registered = cachedCombatScripts.size();
		Logger.getGlobal().info("Loaded " + registered + " NPC combat script keys from the class path.");
		if (registered == 0) {
			// Fail closed: a packaged (jar) runtime that resolves no scripts would otherwise fall back to
			// Default for every NPC without a single log line. Utils.getClasses now scans jars, so zero
			// means the scan itself is broken, not that the packages are absent.
			throw new IllegalStateException("Loaded 0 NPC combat scripts: Utils.getClasses found nothing under "
					+ "com.rs.game.npc.combat.impl / com.rs.game.activities.rots.combat / com.rs.game.activities.aod.npc.combat");
		}
	}

	/** Number of registered script keys (NPC ids and names); 0 only before {@link #init()} ran. */
	public static int getScriptKeyCount() {
		return cachedCombatScripts.size();
	}

	public static int specialAttack(final NPC npc, final Entity target) {
		CombatScript script = cachedCombatScripts.get(npc.getId());
		if (script == null) {
			script = cachedCombatScripts.get(npc.getDefinitions().name);
			if (script == null) {
				script = DEFAULT_SCRIPT;
			}
		}
		return script.attack(npc, target);
	}
}