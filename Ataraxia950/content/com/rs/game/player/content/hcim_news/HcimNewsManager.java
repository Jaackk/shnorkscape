package com.rs.game.player.content.hcim_news;

import com.google.common.collect.ImmutableMap;
import com.rs.Settings;
import com.rs.game.player.Player;
import com.rs.utils.Logger;
import com.rs.utils.PersistentObject;
import com.rs.utils.DataPaths;
import com.rs.utils.PersistentStringList;
import com.rs.utils.Utils;
import lombok.val;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author lare96 <http://github.com/lare96>
 */
public class HcimNewsManager {

    private static final HcimNewsManager instance = new HcimNewsManager();

    public static HcimNewsManager getInstance() {
        return instance;
    }

    private final PersistentStringList deaths = new PersistentStringList(DataPaths.path("hcim/deaths.json"));
    private final PersistentStringList news = new PersistentStringList(DataPaths.path("hcim/news.json"));
    private final PersistentObject<LocalDate> sendDate = new PersistentObject<>(LocalDate.class, DataPaths.path("hcim/date.json"));
    private volatile ImmutableMap<String, TopHcim> topHcimList = ImmutableMap.of();

    private HcimNewsManager() {
    }

    public void start() {
        if (Settings.TEST_SERVER_MODE)
            return;
        deaths.load();
        news.load();
        sendDate.load();
        if (sendDate.getValue() == null) {
            sendDate.setValue(computeNextNewsDate());
            sendDate.save();
        }
    }

    public void addDeath(Player player) {
        if (Settings.TEST_SERVER_MODE || !player.isHCIronMan())
            return;
        val topHcim = topHcimList.get(player.getUsername());
        if (topHcim != null) {
            sendDeathInstant(player,topHcim);
        } else {
            deaths.add("**" + player.getDisplayName() + "** (Total level **" + Utils.formatNumber(player.getSkills().getTotalLevel()) + "**)");
        }
    }

    public void addNews(Player player, String msg, int chance) {
        if (Settings.TEST_SERVER_MODE || !player.isHCIronMan())
            return;
        if (chance < 1) {
            throw new IllegalStateException("Chance cannot be lower than 1.");
        }
        boolean add = chance == 1 || ThreadLocalRandom.current().nextInt(chance) == 0;
        if (player.isHCIronMan() && add) {
            news.add(msg.replaceAll("<#player>", "**" + player.getDisplayName() + "**"));
        }
    }

    public void addNews(Player player, String msg) {
        addNews(player, msg, 1);
    }

    /**
     * Formerly posted a JDA embed to the Discord HCIM news channel. The Discord
     * integration (the Discord API package and the JDA library) was removed in
     * P8; the death is logged instead so the file-backed deaths/news lists keep
     * working.
     */
    private void sendDeathInstant(Player player,  TopHcim hcim) {
        if (Settings.TEST_SERVER_MODE)
            return;
        Logger.getGlobal().info("HCIM death (Discord disabled): " + player.getDisplayName()
                + " rank " + Utils.formatNumber(hcim.getOverallRank())
                + ", combat " + Utils.formatNumber(player.getSkills().getCombatLevel())
                + ", total level " + Utils.formatNumber(player.getSkills().getTotalLevel())
                + ", total xp " + Utils.formatNumber(player.getSkills().getTotalXp())
                + ", playtime " + Utils.getTimePlayed(player.getTimePlayed()));
    }

    public LocalDate computeNextNewsDate() {
        return LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    public PersistentStringList getDeaths() {
        return deaths;
    }

    public PersistentStringList getNews() {
        return news;
    }

    public PersistentObject<LocalDate> getSendDate() {
        return sendDate;
    }

    void setTopHcimList(ImmutableMap<String, TopHcim> topHcimList) {
        Objects.requireNonNull(topHcimList);
        this.topHcimList = topHcimList;
    }

    public ImmutableMap<String, TopHcim> getTopHcimList() {
        return topHcimList;
    }
}
