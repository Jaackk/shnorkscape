package com.rs.game.player.actions.invention;

import java.io.Serializable;

import com.rs.game.item.Item;
import com.rs.game.player.Player;

import lombok.Getter;
import lombok.Setter;

public class InventionData implements Serializable {

    private static final long serialVersionUID = 8540705523019405600L;
    public static final int MAXIMUM_XP = 1000000;

    private double xp;
    private Gizmo[] gizmos;
    @Getter
    @Setter
    private int originalItemId;

    @Override
    public String toString() {
        return "InventionData [xp=" + xp + ", originalItemId=" + originalItemId + "]";
    }

    public InventionData(double xp) {
        this.xp = xp;
        this.gizmos = new Gizmo[2];
    }

    public InventionData(Gizmo gizmo) {
        this(0);
        this.gizmos[0] = gizmo;
    }

    public double getXp() {
        return xp >= MAXIMUM_XP ? MAXIMUM_XP : xp;
    }

    public void setXp(double xp) {
        this.xp = xp;
        if (this.xp >= MAXIMUM_XP)
            this.xp = MAXIMUM_XP;
    }

    public void setGizmos(Gizmo[] gizmos) {
        this.gizmos = gizmos;
    }

    public Gizmo[] getGizmos() {
        return gizmos;
    }

    public int getGizmosCount() {
        int count = 0;
        for (Gizmo gizmo : gizmos) {
            if (gizmo != null)
                count++;
        }
        return count;
    }

    public static int getItemLevel(Player player, Item item) {
        if (item.getInventionData() == null)
            return 0;
        int maxLevel = !player.getInventionManager().hasDiscoveredBluePrint(68) ? 1 : !player.getInventionManager().hasDiscoveredBluePrint(69) ? 5 : !player.getInventionManager().hasDiscoveredBluePrint(70) ? 10 : !player.getInventionManager().hasDiscoveredBluePrint(71) ? 15 : 20;
        return Math.min(maxLevel, getLevelForXp((int) item.getInventionData().getXp()));
    }

    public static int getLevelForXp(int xp) {
        if (xp < 1160)
            return 1;
        if (xp < 2607)
            return 2;
        if (xp < 5176)
            return 3;
        if (xp < 8285)
            return 4;
        if (xp < 11760)
            return 5;
        if (xp < 15835)
            return 6;
        if (xp < 21152)
            return 7;
        if (xp < 28761)
            return 8;
        if (xp < 40120)
            return 9;
        if (xp < 57095)
            return 10;
        if (xp < 81960)
            return 11;
        if (xp < 117397)
            return 12;
        if (xp < 166496)
            return 13;
        if (xp < 232886)
            return 14;
        if (xp < 320080)
            return 15;
        if (xp < 432785)
            return 16;
        if (xp < 575592)
            return 17;
        if (xp < 753631)
            return 18;
        if (xp < 972440)
            return 19;
        return 20;
    }

    public static class Gizmo implements Serializable {

        private static final long serialVersionUID = 5353051107616186154L;

        private final Perk[] perks;
        private final int[][] materialsUsed;

        public Gizmo(Perk[] perks, int[][] materialsUsed) {
            if (perks.length == 1)
                perks = new Perk[] { perks[0], null };
            else if (perks.length == 2 && perks[0] == null) {
                perks = new Perk[] { perks[1], null };
            }
            this.perks = perks;
            this.materialsUsed = materialsUsed;
        }

        public Perk[] getPerks() {
            return perks;
        }

        public int[][] getMaterialsUsed() {
            return materialsUsed;
        }
    }

    public static class Perk implements Serializable {
        private static final long serialVersionUID = 3407389596342206338L;

        private final int id;
        private final int rank;
        private boolean increasedChance;

        public Perk(int id, int rank) {
            this.id = id;
            this.rank = rank;
        }

        public int getId() {
            return id;
        }

        public int getRank() {
            return rank;
        }

        public boolean hasIncreasedChance() {
            return increasedChance;
        }

        public void setIncreasedChance(boolean increasedChance) {
            this.increasedChance = increasedChance;
        }

        @Override
        public String toString() {
            return "Perk [id=" + id + ", rank=" + rank + ", increasedChance=" + increasedChance + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + id;
            result = prime * result + (increasedChance ? 1231 : 1237);
            result = prime * result + rank;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Perk other = (Perk) obj;
            if (id != other.id)
                return false;
            if (increasedChance != other.increasedChance)
                return false;
            return rank == other.rank;
        }

    }

}
