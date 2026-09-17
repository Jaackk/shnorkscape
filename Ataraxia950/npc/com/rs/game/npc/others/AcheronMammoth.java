package com.rs.game.npc.others;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Hit;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class AcheronMammoth extends NPC {

	private static final long serialVersionUID = 6234969515542474409L;

	public boolean dialogueOccured;
	private int index;
	private String npcName;

	private static final String[] PLAYER_SENTENCES = { "Mammoth! I challenge you!", "En garde!", "Good sir, I challenge you to a duel.", "I challenge thee to a duel.", "I mock your ancestors!", "I'd throw down my gloves, but they're rather pricey.", "Let's do this!", "CHAAAAARGE!", "Manners maketh man", "Round one: FIGHT!" };

	private static final String[] NPC_SENTENCES = { "My name is", "Ahaa as you wish", "I accept your challenge!", "That's my job!", "I'm sorry you feel the need to throw your life away.", "Yours are not even worthy of that.", "You're going to need them to defeat me!", "I trust you have a good note about how your remains will be handled?", "Prepare to join your ancestors!", "Are we going to stand here talking, or are we going to fight?", "Erm... yes?" };

	private static final String[] MAMMOTH_NAMES = { "Anthony", "Ashley", "Daniel", "Henrik", "James", "Liam", "Luke", "Nils", "Thomas", "Wing" };

	public AcheronMammoth(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
	}

	@Override
	public double getMeleePrayerMultiplier() {
		return 0.7;
	}

	@Override
	public double getMagePrayerMultiplier() {
		return 0.7;
	}

	@Override
	public double getRangePrayerMultiplier() {
		return 0.7;
	}

	@Override
	public void handleIngoingHit(final Hit hit) {
		super.handleIngoingHit(hit);
		if (!dialogueOccured && hit.getSource() != null && hit.getSource() instanceof Player) {
			dialogueOccured = true;
			index = Utils.random(PLAYER_SENTENCES.length);
			hit.getSource().setNextForceTalk(new ForceTalk(PLAYER_SENTENCES[index]));
			startDialogue();
		}
	}

	public void startDialogue() {
		if (index == 0) {
			npcName = MAMMOTH_NAMES[Utils.random(MAMMOTH_NAMES.length)];
			setNextForceTalk(new ForceTalk(NPC_SENTENCES[0] + " " + npcName + "."));
			setName(npcName);
		} else
			setNextForceTalk(new ForceTalk(NPC_SENTENCES[index]));
	}

	@Override
	public void sendDeath(Entity source) {
		final NPCCombatDefinition defs = getCombatDefinitions();

		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0)
					setNextAnimation(new Animation(defs.getDeathEmote()));
				else if (loop == 3) {
					drop();
					reset();
					getCombat().removeTarget();
					setLocation(getRespawnTile());
					finish();
					setRespawnTask();
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}

}
