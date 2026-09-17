package com.rs.utils;

import com.rs.game.player.Player;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;

public final class LmsVictoriesHiscores implements Serializable {

    private static final long serialVersionUID = 5403480618483552509L;
    private static final String PATH = "data/hiscores/lmsvictories.vichy";
    private static LmsVictoriesHiscores[] ranks;
    private final String username;
    private final int victories;

    public LmsVictoriesHiscores(Player player) {
        this.username = player.getUsername();
        this.victories = player.getLmsVictories();
    }

    public static void init() {
        File file = new File(PATH);
        if (file.exists())
            try {
                ranks = (LmsVictoriesHiscores[]) SerializableFilesManager.loadSerializedFile(file);
                return;
            } catch (Throwable e) {
                Logger.getGlobal().catching(e);
            }
        ranks = new LmsVictoriesHiscores[300];
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
        player.getPackets().sendIComponentText(275, 1, "<shad=000000>LMS Victories (since patch #25)");
        int count = 0;
        for (LmsVictoriesHiscores rank : ranks) {
            if (rank == null)
                return;
            player.getPackets().sendIComponentText(275, count + 10, "#" + (count + 1) + " - "
                    + Utils.formatPlayerNameForDisplay(rank.username) + " - times won: " + rank.victories);
            count++;
        }
    }

    public static void sort() {
        Arrays.sort(ranks, (arg0, arg1) -> {
            if (arg0 == null)
                return 1;
            if (arg1 == null)
                return -1;
            if (arg0.victories < arg1.victories)
                return 1;
            else if (arg0.victories > arg1.victories)
                return -1;
            else
                return 0;
        });
    }

    public static void checkRank(Player player) {
        int victories = player.getLmsVictories();
        for (int i = 0; i < ranks.length; i++) {
            LmsVictoriesHiscores rank = ranks[i];
            if (rank == null)
                break;
            if (rank.username.equalsIgnoreCase(player.getUsername())) {
                ranks[i] = new LmsVictoriesHiscores(player);
                sort();
                return;
            }
        }
        for (int i = 0; i < ranks.length; i++) {
            LmsVictoriesHiscores rank = ranks[i];
            if (rank == null) {
                ranks[i] = new LmsVictoriesHiscores(player);
                sort();
                return;
            }
        }
        for (int i = 0; i < ranks.length; i++) {
            if (ranks[i].victories < victories) {
                ranks[i] = new LmsVictoriesHiscores(player);
                sort();
                return;
            }
        }
    }
}