package com.rs.game.player.content;

import com.rs.Settings;
import com.rs.game.player.Player;
import com.rs.utils.Logger;
import com.rs.utils.SerializableFilesManager;
import com.rs.utils.Utils;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

public final class TelosEnrageRanks implements Serializable {

    private static final long serialVersionUID = 5403480618483552509L;

    private final String username;
    private final int killCount;
    private final int maxEnrage;

    private static TelosEnrageRanks[] ranks;

    private static final String PATH = "data/enrageRanks.ser";

    public TelosEnrageRanks(Player player) {
        this.username = player.getUsername();
        this.maxEnrage = player.getMaxEnrage();
        this.killCount = player.getKillStatistics(133);
    }

    public static void init() {
        File file = new File(PATH);
        if (file.exists())
            try {
                ranks = (TelosEnrageRanks[]) SerializableFilesManager.loadSerializedFile(file);
                return;
            } catch (Throwable e) {
                Logger.getGlobal().catching(e);
            }
        ranks = new TelosEnrageRanks[30];
    }

    public static final void save() {
        try {
            SerializableFilesManager.storeSerializableClass(ranks, new File(PATH));
        } catch (Throwable e) {
            Logger.getGlobal().catching(e);
        }
    }

    public static void showRanks(Player player) {
        StringBuilder list = new StringBuilder();
        for (int i = 0; i < 30; i++) {
            if (ranks[i] == null)
                break;
            list.append(i + 1).
                    append(". ").
                    append(Utils.formatPlayerNameForDisplay(ranks[i].username)).
                    append(" ~ Max Enrage: ").
                    append(ranks[i].maxEnrage).
                    append(" ~ KC: ").
                    append(ranks[i].killCount).
                    append("<br>");
        }
        player.getPackets().sendIComponentText(1166, 23, "Top 30 Enrage");
        player.getPackets().sendIComponentText(1166, 1, list.toString());
        player.getPackets().sendHideIComponent(1166, 2, true);
        player.getInterfaceManager().sendInterface(1166);
    }

    public static void sort() {
        Arrays.sort(ranks, new Comparator<TelosEnrageRanks>() {
            @Override
            public int compare(TelosEnrageRanks arg0, TelosEnrageRanks arg1) {
                if (arg0 == null)
                    return 1;
                if (arg1 == null)
                    return -1;
                if (arg0.maxEnrage < arg1.maxEnrage)
                    return 1;
                else if (arg0.maxEnrage > arg1.maxEnrage)
                    return -1;
                else
                    return 0;
            }

        });
    }

    public static void checkRank(Player player) {
        if (player.getRights() == 2 && !Settings.TEST_SERVER_MODE)
            return;
        int maxEnrage = player.getMaxEnrage();
        if (maxEnrage == 0)
            return;
        for (int i = 0; i < ranks.length; i++) {
            TelosEnrageRanks rank = ranks[i];
            if (rank == null)
                break;
            if (rank.username.equalsIgnoreCase(player.getUsername())) {
                ranks[i] = new TelosEnrageRanks(player);
                sort();
                return;
            }
        }
        for (int i = 0; i < ranks.length; i++) {
            TelosEnrageRanks rank = ranks[i];
            if (rank == null) {
                ranks[i] = new TelosEnrageRanks(player);
                sort();
                return;
            }
        }
        for (int i = 0; i < ranks.length; i++) {
            if (ranks[i].maxEnrage < maxEnrage) {
                ranks[i] = new TelosEnrageRanks(player);
                sort();
                return;
            }
        }
    }

}
