package com.rs.game.activites.gim.highscores;

import com.rs.cores.CoresManager;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activites.gim.event.GIMEventType;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.DTController;
import com.rs.utils.Colors;

import java.util.concurrent.ThreadLocalRandom;

/**
 * A class that keeps a record of GIM score factor gained while a player is online.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMScoreTracker {

    /**
     * The player instance.
     */
    private final Player player;

    /**
     * The levels gained.
     */
    private int levelsGained;

    /**
     * The XP gained.
     */
    private long xpGained;

    /**
     * The deaths gained.
     */
    private int deaths;

    /**
     * The boss points gained.
     */
    private int bpGained;

    /**
     * The prestiges gained.
     */
    private int prestiges;

    /**
     * Creates a new {@link GIMScoreTracker}.
     */
    public GIMScoreTracker(Player player) {
        this.player = player;
    }

    /**
     * Increments the levels gained.
     */
    public void incrementLevelsGained(int value) {
        if (player.isUnregisteredGIM())
            return;
        levelsGained += value;
    }

    /**
     * Increments the XP gained.
     */
    public void incrementXpGained(long value) {
        if (player.isUnregisteredGIM())
            return;
        xpGained += value;
    }

    /**
     * Increments the deaths gained.
     */
    public void incrementDeaths(String msg) {
        if (player.isUnregisteredGIM())
            return;
        deaths++;
        if (msg != null) {
        }
    }

    /**
     * Increments the boss points gained.
     */
    public void incrementBpGained(int value) {
        if (player.isUnregisteredGIM() || player.getControlerManager().getControler() instanceof DTController)
            return;
        if (GIM.getEventManager().isRunning(GIMEventType.BOSS_POINTS)) {
            value *= 2;
        }
        if (value > 3 && ThreadLocalRandom.current().nextInt(75) == 0 && player.getInventory().hasFreeSlots()) {
            GIM.sendPlayerMsg(player, "A sparkling glow emits from your inventory. I wonder what you've received?");
            player.getInventory().addItem(new Item(773, 1));
        }
        bpGained += value;
        GIM.sendPlayerMsg(player, "You have gained " + Colors.RED + value + "</col> " + Colors.DEF_SEARCH_CYAN + "boss points!</col>");
    }

    /**
     * Increments the prestiges gained.
     */
    public void incrementPrestiges() {
        if (player.isUnregisteredGIM())
            return;
       prestiges++;
    }

    /**
     * Saves these pending score factors back to the database.
     */
    public void save() {
        if (player.isUnregisteredGIM())
            return;
        if (canUpdate()) {
            CoresManager.getServiceProvider().executeNow(new GIMUpdateScoreSql(player.getUsername(), xpGained, deaths, bpGained, levelsGained, prestiges));
        }
    }

    /**
     * Resets this tracker back to its original state.
     */
    public void reset() {
        if (player.isUnregisteredGIM())
            return;
        levelsGained = 0;
        xpGained = 0;
        deaths = 0;
        bpGained = 0;
        prestiges = 0;
    }

    /**
     * If this tracker has tracked anything so far.
     */
    public boolean canUpdate() {
        if (player.isUnregisteredGIM())
            return false;
        return xpGained > 0 || deaths > 0 || bpGained > 0 || levelsGained > 0 || prestiges > 0;
    }

    public int getLevelsGained() {
        return levelsGained;
    }

    public long getXpGained() {
        return xpGained;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getBpGained() {
        return bpGained;
    }

    public int getPrestiges() {
        return prestiges;
    }
}
