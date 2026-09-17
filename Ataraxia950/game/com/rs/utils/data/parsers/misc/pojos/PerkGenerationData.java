package com.rs.utils.data.parsers.misc.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PerkGenerationData {

    private PerkComponent[] comps;
    private PerkData[] perks;

    @Data
    @AllArgsConstructor
    public static class PerkComponent {
        private int compId;
        private String name;
        private double xp;
        PerkComponentData[] weaponPerks;
        PerkComponentData[] armourPerks;
        PerkComponentData[] toolPerks;
        
        public PerkComponentData[] getPerks(int gizmoType) {
            return gizmoType == 0 ? weaponPerks : gizmoType == 1 ? armourPerks : toolPerks;
        }
    }

    @Data
    @AllArgsConstructor
    public static class PerkComponentData {
        private int perkId;
        private String name;
        private int base;
        private int roll;
    }

    @Data
    @AllArgsConstructor
    public static class PerkData {
        private int perkId;
        private String name;
        private boolean doubleslot;
        private PerkRank[] ranks;
    }

    @Data
    @AllArgsConstructor
    public static class PerkRank {
        private int threshold;
        private int cost;
    }
}
