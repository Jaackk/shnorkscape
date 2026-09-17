package com.rs.game.player;

import com.rs.game.activities.aod.AoDController;
import com.rs.game.activities.aod.npc.AoDNex;
import com.rs.game.npc.NPC;
import com.rs.game.npc.corp.CorporealBeast;
import com.rs.game.npc.dragons.KingBlackDragon;
import com.rs.game.npc.fightcaves.TzTok_Jad;
import com.rs.game.npc.fightkiln.HarAken;
import com.rs.game.npc.giantmole.GiantMole;
import com.rs.game.npc.godwars.armadyl.KreeArra;
import com.rs.game.npc.godwars.bandos.GeneralGraardor;
import com.rs.game.npc.godwars.saradomin.CommanderZilyana;
import com.rs.game.npc.godwars.zammorak.KrilTsutsaroth;
import com.rs.game.npc.godwars.zaros.Nex;
import com.rs.game.npc.gwd2.gregorovic.Gregorovic;
import com.rs.game.npc.gwd2.helwyr.Helwyr;
import com.rs.game.npc.gwd2.twinfuries.Avaryss;
import com.rs.game.npc.gwd2.twinfuries.Nymora;
import com.rs.game.npc.gwd2.vindicta.Vindicta;
import com.rs.game.npc.kalphite.KalphiteQueen;
import com.rs.game.npc.kalphiteking.KalphiteKing;
import com.rs.game.npc.others.Legios;
import com.rs.game.npc.qbd.QueenBlackDragon;
import com.rs.game.npc.spiderboss.Araxxor;
import com.rs.game.npc.themagister.TheMagister;
import com.rs.game.npc.vorago.Vorago;
import com.rs.game.player.content.araxxor.AraxxorController;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.controllers.QueenBlackDragonController;
import com.rs.game.player.controllers.ZGDController;
import com.rs.game.player.controllers.bossInstance.KalphiteKingInstanceController;
import com.rs.game.player.controllers.bossInstance.VoragoInstanceController;
import com.rs.game.player.controllers.bossInstance.gwd2.HeartOfGielinorController;
import com.rs.network.packet.PacketDispatcher;
import com.rs.utils.Utils;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Jan 8, 2019.
 */
public class ActivityTimersManager implements Serializable {

	private static final long serialVersionUID = -8632497327836609421L;

	private transient Player player;

	private final LinkedHashMap<ActivityTimers, Long> currentTimers;
	private final LinkedHashMap<ActivityTimers, Long> bestTimers;

	/**
	 * Constructs a new class.
	 */
	public ActivityTimersManager() {
		this.currentTimers = new LinkedHashMap<ActivityTimers, Long>();
		this.bestTimers = new LinkedHashMap<ActivityTimers, Long>();
	}

	/**
	 * Use this function for minigame type shit.
	 * @param timer
	 */
	public void startTimer(ActivityTimers timer) {
//		if (currentTimers == null)
//			return;
//
//		currentTimers.put(timer, Utils.currentTimeMillis() - player.getTimePlayed());
		startTimer(timer, false);
	}
	
	/**
	 * Use this function for bosses and other applicable npcs.
	 * @param timer
	 * @param noReset
	 */
	public void startTimer(ActivityTimers timer, boolean noReset) {
		if (currentTimers == null)
			return;
		
		if (noReset) {
			if (currentTimers.containsKey(timer)) {
				return;
			}
		}

		currentTimers.put(timer, Utils.currentTimeMillis() - player.getTimePlayed());
	}

	public void finishTimer(ActivityTimers timer, boolean reset, boolean temporary) {
		if (currentTimers == null)
			return;

		// if the player does logout/dc we don't want anything here to happen. this excludes bosses.
		if (temporary) {
			return;
		}
		
		if (!currentTimers.containsKey(timer))
			return;

		// this exists cos the player can start timer but if they leave the activity without actually completing the task, then we just reset the start timer
		// but not actually give them a best time.
		if (reset) {
			// this should always come back true.
			currentTimers.remove(timer);

			// return here cos we don't award the player anything etc for not completing the main objective.
			return;
		}

		// this is the time it took for player to complete activity.
		long time = ((Utils.currentTimeMillis() - player.getTimePlayed()) - currentTimers.remove(timer));
		player.sendMessage("Time took to finish: " + Utils.formatTime(time) + ".", false);

		if (bestTimers == null)
			return;

		// if this is the first time player does activity, just add it to best as default.
		if (!bestTimers.containsKey(timer))
			bestTimers.put(timer, time);
		else {
			// this only happens if the player already has a best time for this activity. so now we check if its better time.
			long bestTime = bestTimers.get(timer);
			if (time < bestTime) {
				// if the new time beats the saved best time.
				long previousTime = bestTimers.put(timer, time);// or use bestTime but either way.
				player.sendMessage("Congratulations! You beat your personal best time of " + Utils.formatTime(previousTime) + ".", false);
				// TODO some sort of reward here for beating your personal best time.
			}
		}
	}

	public void showBestTimes() {
		PacketDispatcher packets = player.getPackets();
		if (packets == null)
			return;

		for (int i = 0; i < 309; i++)
			packets.sendIComponentText(275, i, "");

		packets.sendIComponentText(275, 1, Utils.formatPlayerNameForDisplay(player.getDisplayName()) + "'s Best Times Log.");

		if (bestTimers == null) {
			// just show nothing if this is somehow null.
			player.getInterfaceManager().sendInterface(275);
			return;
		}

		if (bestTimers.isEmpty())
			packets.sendIComponentText(275, 10, "None!");
		else {
			int index = 10;
			for (Entry<ActivityTimers, Long> timer : bestTimers.entrySet()) {
				if (timer == null)// should never happen.
					continue;

				if (timer.getValue() < 0)// this also should never happen but something obv went wrong if this is true.
					continue;

				packets.sendIComponentText(275, index++, Utils.formatString(timer.getKey().name().replaceAll("_", " ").toLowerCase()) + ": " + Utils.formatTime(timer.getValue()) + ".");
			}
		}

		player.getInterfaceManager().sendInterface(275);
	}

	public Long getTimerByName(String npc){
		AtomicReference<Long> time = new AtomicReference<>(Long.valueOf(-1));
		bestTimers.entrySet().forEach(x -> {
			if(Utils.formatString(x.getKey().name().replaceAll("_", " ")).equals(npc)){
				time.set(x.getValue());
			}
		});
		return time.get();
	}
	
	public void startBossTimer(NPC target) {
		if (target instanceof GeneralGraardor) {
			startTimer(ActivityTimers.GENERAL_GRAARDOR, true);
		} else if (target instanceof KreeArra) {
			startTimer(ActivityTimers.KREEARRA, true);
		} else if (target instanceof CommanderZilyana) {
			startTimer(ActivityTimers.COMMANDER_ZILYANA, true);
		} else if (target instanceof KrilTsutsaroth) {
			startTimer(ActivityTimers.KRIL_TSUTSAROTH, true);
		} else if (target instanceof Nex) {
			startTimer(ActivityTimers.NEX, true);
		} else if (target instanceof CorporealBeast) {
			startTimer(ActivityTimers.CORPOREAL_BEAST, true);
		} else if (target instanceof KingBlackDragon) {
			startTimer(ActivityTimers.KING_BLACK_DRAGON, true);
		} else if (target instanceof TzTok_Jad) {
			startTimer(ActivityTimers.TZTOK_JAD, true);
		} else if (target instanceof HarAken) {
			startTimer(ActivityTimers.HAR_AKEN, true);
		} else if (target instanceof GiantMole) {
			startTimer(ActivityTimers.GIANT_MOLE, true);
		} else if (target instanceof Gregorovic) {
			startTimer(ActivityTimers.GREGOROVIC, true);
		} else if (target instanceof Helwyr) {
			startTimer(ActivityTimers.HELWYR, true);
		} else if (target instanceof Avaryss) {
			startTimer(ActivityTimers.AVARYSS, true);
		} else if (target instanceof Nymora) {
			startTimer(ActivityTimers.NYMORA, true);
		} else if (target instanceof Vindicta) {
			startTimer(ActivityTimers.VINDICTA, true);
		} else if (target instanceof KalphiteQueen) {
			startTimer(ActivityTimers.KALPHITE_QUEEN, true);
		} else if (target instanceof KalphiteKing) {
			startTimer(ActivityTimers.KALPHITE_KING, true);
		} else if (target instanceof Legios) {
			startTimer(ActivityTimers.LEGIOS, true);
		} else if (target instanceof QueenBlackDragon) {
			startTimer(ActivityTimers.QUEEN_BLACK_DRAGON, true);
		} else if (target instanceof Vorago) {
			startTimer(ActivityTimers.VORAGO, true);
		} else if (target instanceof Araxxor) {
			startTimer(ActivityTimers.ARAXXOR, true);
		} else if (target instanceof AoDNex) {
			startTimer(ActivityTimers.ANGEL_OF_DEATH, true);
		} else if (target instanceof TheMagister) {
            startTimer(ActivityTimers.THE_MAGISTER, true);
        }
	}
	
	public void finishBossTimer(NPC target) {
		if (target instanceof GeneralGraardor) {
			finishTimer(ActivityTimers.GENERAL_GRAARDOR, false, false);
		} else if (target instanceof KreeArra) {
			finishTimer(ActivityTimers.KREEARRA, false, false);
		} else if (target instanceof CommanderZilyana) {
			finishTimer(ActivityTimers.COMMANDER_ZILYANA, false, false);
		} else if (target instanceof KrilTsutsaroth) {
			finishTimer(ActivityTimers.KRIL_TSUTSAROTH, false, false);
		} else if (target instanceof Nex) {
			finishTimer(ActivityTimers.NEX, false, false);
		} else if (target instanceof CorporealBeast) {
			finishTimer(ActivityTimers.CORPOREAL_BEAST, false, false);
		} else if (target instanceof KingBlackDragon) {
			finishTimer(ActivityTimers.KING_BLACK_DRAGON, false, false);
		} else if (target instanceof TzTok_Jad) {
			finishTimer(ActivityTimers.TZTOK_JAD, false, false);
		} else if (target instanceof HarAken) {
			finishTimer(ActivityTimers.HAR_AKEN, false, false);
		} else if (target instanceof GiantMole) {
			finishTimer(ActivityTimers.GIANT_MOLE, false, false);
		} else if (target instanceof Gregorovic) {
			finishTimer(ActivityTimers.GREGOROVIC, false, false);
		} else if (target instanceof Helwyr) {
			finishTimer(ActivityTimers.HELWYR, false, false);
		} else if (target instanceof Avaryss) {
			finishTimer(ActivityTimers.AVARYSS, false, false);
		} else if (target instanceof Nymora) {
			finishTimer(ActivityTimers.NYMORA, false, false);
		} else if (target instanceof Vindicta) {
			finishTimer(ActivityTimers.VINDICTA, false, false);
		} else if (target instanceof KalphiteQueen) {
			finishTimer(ActivityTimers.KALPHITE_QUEEN, false, false);
		} else if (target instanceof KalphiteKing) {
			finishTimer(ActivityTimers.KALPHITE_KING, false, false);
		} else if (target instanceof Legios) {
			finishTimer(ActivityTimers.LEGIOS, false, false);
		} else if (target instanceof QueenBlackDragon) {
			finishTimer(ActivityTimers.QUEEN_BLACK_DRAGON, false, false);
		} else if (target instanceof Vorago) {
			finishTimer(ActivityTimers.VORAGO, false, false);
		} else if (target instanceof Araxxor) {
			finishTimer(ActivityTimers.ARAXXOR, false, false);
		} else if (target instanceof AoDNex) {
			finishTimer(ActivityTimers.ANGEL_OF_DEATH, false, false);
		} else if (target instanceof TheMagister) {
		    finishTimer(ActivityTimers.THE_MAGISTER, false, false);
        }
	}
	
	public void resetTimers() {
		finishTimer(ActivityTimers.GENERAL_GRAARDOR, true, false);
		finishTimer(ActivityTimers.KREEARRA, true, false);
		finishTimer(ActivityTimers.COMMANDER_ZILYANA, true, false);
		finishTimer(ActivityTimers.KRIL_TSUTSAROTH, true, false);
		finishTimer(ActivityTimers.NEX, true, false);
		finishTimer(ActivityTimers.CORPOREAL_BEAST, true, false);
		finishTimer(ActivityTimers.KING_BLACK_DRAGON, true, false);
		finishTimer(ActivityTimers.TZTOK_JAD, true, false);
		finishTimer(ActivityTimers.HAR_AKEN, true, false);
		finishTimer(ActivityTimers.GIANT_MOLE, true, false);
		finishTimer(ActivityTimers.GREGOROVIC, true, false);
		finishTimer(ActivityTimers.HELWYR, true, false);
		finishTimer(ActivityTimers.AVARYSS, true, false);
		finishTimer(ActivityTimers.NYMORA, true, false);
		finishTimer(ActivityTimers.VINDICTA, true, false);
		finishTimer(ActivityTimers.KALPHITE_QUEEN, true, false);
		finishTimer(ActivityTimers.KALPHITE_KING, true, false);
		finishTimer(ActivityTimers.LEGIOS, true, false);
		finishTimer(ActivityTimers.QUEEN_BLACK_DRAGON, true, false);
		finishTimer(ActivityTimers.VORAGO, true, false);
		finishTimer(ActivityTimers.ARAXXOR, true, false);
		finishTimer(ActivityTimers.ANGEL_OF_DEATH, true, false);
	    finishTimer(ActivityTimers.THE_MAGISTER, true, false);
	}
	
	public boolean isInsideSpecialBoss() {
		Controller c = player.getControlerManager().getControler();
		return c != null && (c instanceof ZGDController || c instanceof QueenBlackDragonController || c instanceof VoragoInstanceController || c instanceof KalphiteKingInstanceController || c instanceof AoDController || c instanceof AraxxorController || c instanceof HeartOfGielinorController);
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public Map<ActivityTimers, Long> getCurrentTimers() {
		return currentTimers;
	}

	public Map<ActivityTimers, Long> getBestTimers() {
		return bestTimers;
	}

	public enum ActivityTimers {
		FIGHT_CAVES, FIGHT_KILN, GENERAL_GRAARDOR, KREEARRA, COMMANDER_ZILYANA, KRIL_TSUTSAROTH, NEX, CORPOREAL_BEAST, KING_BLACK_DRAGON, TZTOK_JAD, HAR_AKEN, GIANT_MOLE, GREGOROVIC, HELWYR, AVARYSS, NYMORA, VINDICTA, KALPHITE_QUEEN, KALPHITE_KING, LEGIOS, QUEEN_BLACK_DRAGON, VORAGO, ARAXXOR, ANGEL_OF_DEATH, THE_MAGISTER
	}

}
