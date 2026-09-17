package com.rs.game.player.actions.herblore;

import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

import java.util.LinkedList;
import java.util.List;

public class HerbCleaning {

    public static boolean clean(final Player player, Item item, final int slotId) {
        final Herbs herb = getHerb(item.getId());
        if (herb == null)
            return false;
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                if (player.getSkills().getLevel(Skills.HERBLORE) < herb.getLevel()) {
                    player.sendMessage("You do not have the required level to clean this.", true);
                    return;
                }
                if (!player.getInventory().containsOneItem(herb.herbId)) {
                    return;
                }
                player.getInventory().deleteItem(new Item(herb.herbId));
                player.getInventory().addItem(new Item(herb.cleanId));
                player.getInventory().refresh();
                player.getSkills().addXp(Skills.HERBLORE, herb.getExperience());
                player.sendMessage("You clean the herb.", true);
                ClueScrollDistributor.givePlayerClueScrollIfProbable(player, ClueScrollDistributor.SKILLING_PERCENT);
                this.stop();
            }
        });
        return true;
    }

    public static Herbs getHerb(int id) {
        for (final Herbs herb : Herbs.values())
            if (herb.getHerbId() == id)
                return herb;
        return null;
    }

    public static List<Herbs> getHerbs() {
        List<Herbs> herbs = new LinkedList<Herbs>();
        for (Herbs herb : Herbs.values()) {
            if (herb.ordinal() < 17) // Felstalks
                herbs.add(herb);
        }
        return herbs;
    }

    public enum Herbs {

        GUAM(199, 2.5, 1, 249, 91),

        MARRENTILL(201, 3.8, 5, 251, 93),

        TARROMIN(203, 5, 11, 253, 95),

        HARRALANDER(205, 6.3, 20, 255, 97),

        RANARR(207, 7.5, 25, 257, 99),

        TOADFLAX(3049, 8, 30, 2998, 3002),

        SPIRIT_WEED(12174, 7.8, 35, 12172, 12181),

        IRIT(209, 8.8, 40, 259, 101),

        WERGALI(14836, 9.5, 41, 14854, 14856),

        AVANTOE(211, 10, 48, 261, 103),

        KWUARM(213, 11.3, 54, 263, 105),

        SNAPDRAGON(3051, 11.8, 59, 3000, 3004),

        CADANTINE(215, 12.5, 65, 265, 107),

        LANTADYME(2485, 13.1, 67, 2481, 2483),

        DWARF_WEED(217, 13.8, 70, 267, 109),

        TORSTOL(219, 15, 75, 269, 111),

        FELLSTALK(21626, 16.8, 91, 21624, 21628),

        ERZILLE(19984, 10.0, 54, 19989, 19998),

        ARGWAY(19985, 11.6, 56, 19990, 20000),

        UGUNE(19986, 11.5, 55, 19991, 19999),

        SHENGO(19987, 11.7, 57, 19992, 20001),

        SAMADEN(19988, 11.7, 58, 19993, 20002),

        BLOODWEED(37975, 11.5, 57, 37953, 37973);

        private final int herbId;
        private final int level;
        private final int cleanId;
        private final double xp;
        private final int unf;

        Herbs(int herbId, double xp, int level, int cleanId, int unf) {
            this.herbId = herbId;
            this.xp = xp;
            this.level = level;
            this.cleanId = cleanId;
            this.unf = unf;
        }

        public int getCleanId() {
            return cleanId;
        }

        public double getExperience() {
            return xp;
        }

        public int getHerbId() {
            return herbId;
        }

        public int getLevel() {
            return level;
        }

        public int getUnf() {
            return unf;
        }
    }
}