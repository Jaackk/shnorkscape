package com.rs.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.rs.game.npc.NPC;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.npc.pet.Pet;

import lombok.Getter;

public class DynamicArea {
    public static final List<DynamicArea> EXISTING_MAPS = new ArrayList<DynamicArea>(Short.MAX_VALUE);
    @Getter
    private final int fromRegionX;
    @Getter
    private final int fromRegionY;
    @Getter
    private final int regionsDistanceX;
    @Getter
    private final int regionsDistanceY;

    public DynamicArea(int fromRegionX, int fromRegionY, int regionsDistanceX, int regionsDistanceY) {
        this.fromRegionX = fromRegionX;
        this.fromRegionY = fromRegionY;
        this.regionsDistanceX = regionsDistanceX;
        this.regionsDistanceY = regionsDistanceY;
        npcs = new HashMap<Integer, NPC>();
    }

    public boolean containsRegionHash(int mapHash) {
        for (int regionX = fromRegionX; regionX < fromRegionX + regionsDistanceX; regionX++) {
            for (int regionY = fromRegionY; regionY < fromRegionY + regionsDistanceY; regionY++) {
                int regionId = MapBuilder.getRegionId(regionX, regionY);
                if (regionId == mapHash)
                    return true;
            }
        }
        return false;
    }

    public static DynamicArea getDynamicArea(int mapHash) {
        Iterator<DynamicArea> itr = EXISTING_MAPS.iterator();
        while (itr.hasNext()) {
            DynamicArea area = itr.next();
            if (area.containsRegionHash(mapHash))
                return area;
        }
        return null;
    }

    public static boolean regionExists(final int mapHash) {
        return getDynamicArea(mapHash) != null;
    }

    public static boolean removeDynamicArea(final int mapHash) {
        boolean removed = false;
        Iterator<DynamicArea> itr = EXISTING_MAPS.iterator();
        while (itr.hasNext()) {
            DynamicArea area = itr.next();
            if (area.containsRegionHash(mapHash)) {
                area.removeLinkedNPCs();
                itr.remove();
                removed = true;
            }
        }
        return removed;
    }

    public int[] getWidthHeight() {
        return new int[] { regionsDistanceX * 8, regionsDistanceY * 8 };
    }

    public static void addDynamicArea(int fromRegionX, int fromRegionY, int regionsDistanceX, int regionsDistanceY) {
        EXISTING_MAPS.add(new DynamicArea(fromRegionX, fromRegionY, regionsDistanceX, regionsDistanceY));
    }

    public int[] getBaseChunks() {
        return new int[] { fromRegionX * 8, fromRegionY * 8 };
    }

    public int[] getMinRealChunks() {
        int chunkX = Integer.MAX_VALUE;
        int chunkY = Integer.MAX_VALUE;
        for (int regionX = fromRegionX; regionX < fromRegionX + regionsDistanceX; regionX++) {
            for (int regionY = fromRegionY; regionY < fromRegionY + regionsDistanceY; regionY++) {
                int regionId = MapBuilder.getRegionId(regionX, regionY);
                Region region = World.getRegions().get(regionId);
                if (region instanceof DynamicRegion) {
                    DynamicRegion dynamicRegion = (DynamicRegion) region;
                    for (int i = 0; i < 8; i++) {
                        for (int j = 0; j < 8; j++) {
                            int[] regionCoords = dynamicRegion.getRegionCoords()[0][i][j];
                            if (regionCoords[0] != 0 && regionCoords[0] < chunkX)
                                chunkX = regionCoords[0];
                            if (regionCoords[1] != 0 && regionCoords[1] < chunkY)
                                chunkY = regionCoords[1];
                        }
                    }
                }
            }
        }
        return new int[] { chunkX, chunkY };
    }

    public int[] getMaxRealChunks() {
        int chunkX = 0;
        int chunkY = 0;
        for (int regionX = fromRegionX; regionX < fromRegionX + regionsDistanceX; regionX++) {
            for (int regionY = fromRegionY; regionY < fromRegionY + regionsDistanceY; regionY++) {
                int regionId = MapBuilder.getRegionId(regionX, regionY);
                Region region = World.getRegions().get(regionId);
                if (region instanceof DynamicRegion) {
                    DynamicRegion dynamicRegion = (DynamicRegion) region;
                    for (int i = 0; i < 8; i++) {
                        for (int j = 0; j < 8; j++) {
                            int[] regionCoords = dynamicRegion.getRegionCoords()[0][i][j];
                            if (regionCoords[0] != 0 && regionCoords[0] > chunkX)
                                chunkX = regionCoords[0];
                            if (regionCoords[1] != 0 && regionCoords[1] > chunkY)
                                chunkY = regionCoords[1];
                        }
                    }
                }
            }
        }
        return new int[] { chunkX, chunkY };
    }

    private final Map<Integer, NPC> npcs;

    public void removeLinkedNPCs() {
        Iterator<NPC> itr = npcs.values().iterator();
        while (itr.hasNext()) {
            NPC npc = itr.next();
            if (npc == null)
                continue;
            if (npc instanceof Familiar || npc instanceof Pet) {
                npc.setLocation(0, 0, 0); // sets 0,0,0 and forces to
                // teleto player and not force
                // load region and loss space
                continue;
            }
            npc.finish();
        }
    }

    public void addNPC(NPC npc) {
        npcs.put(npc.getIndex(), npc);
    }

}
