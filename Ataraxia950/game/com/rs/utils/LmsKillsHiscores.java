package com.rs.utils;

import com.rs.game.player.Player;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;

public final class LmsKillsHiscores implements Serializable {

    private static final long serialVersionUID = 5403480618483552509L;
    private static final String PATH = "data/hiscores/lmskills.vichy";
    private static LmsKillsHiscores[] ranks;
    private final String username;
    private final int kills;

    public LmsKillsHiscores(Player player) {
        this.username = player.getUsername();
        this.kills = player.getLmsKills();
    }

    public static void init() {
        File file = new File(PATH);
        if (file.exists())
            try {
                ranks = (LmsKillsHiscores[]) SerializableFilesManager.loadSerializedFile(file);
                return;
            } catch (Throwable e) {
                Logger.getGlobal().catching(e);
            }
        ranks = new LmsKillsHiscores[300];
    }

    public static final void save() {
        try {
            SerializableFilesManager.storeSerializableClass(ranks, new File(PATH));
        } catch (Throwable e) {
            Logger.getGlobal().catching(e);
        }
    }

    public static void showRanks(Player player) {
        player.getInterfaceManager().sendInterface(275);
        for (int i = 0; i <= 309; i++)
            player.getPackets().sendIComponentText(275, i, "");
        player.getPackets().sendIComponentText(275, 1, "<shad=000000>LMS Kills (since patch #25)");
        int count = 0;
        for (LmsKillsHiscores rank : ranks) {
            if (rank == null)
                return;
            player.getPackets().sendIComponentText(275, count + 10, "#" + (count + 1) + " - "
                    + Utils.formatPlayerNameForDisplay(rank.username) + " - kills: " + rank.kills);
            count++;
        }
    }

    public static void sort() {
        Arrays.sort(ranks, (arg0, arg1) -> {
            if (arg0 == null)
                return 1;
            if (arg1 == null)
                return -1;
            if (arg0.kills < arg1.kills)
                return 1;
            else if (arg0.kills > arg1.kills)
                return -1;
            else
                return 0;
        });
    }

    public static void checkRank(Player player) {
        int victories = player.getLmsKills();
        for (int i = 0; i < ranks.length; i++) {
            LmsKillsHiscores rank = ranks[i];
            if (rank == null)
                break;
            if (rank.username.equalsIgnoreCase(player.getUsername())) {
                ranks[i] = new LmsKillsHiscores(player);
                sort();
                return;
            }
        }
        for (int i = 0; i < ranks.length; i++) {
            LmsKillsHiscores rank = ranks[i];
            if (rank == null) {
                ranks[i] = new LmsKillsHiscores(player);
                sort();
                return;
            }
        }
        for (int i = 0; i < ranks.length; i++) {
            if (ranks[i].kills < victories) {
                ranks[i] = new LmsKillsHiscores(player);
                sort();
                return;
            }
        }
    }
}