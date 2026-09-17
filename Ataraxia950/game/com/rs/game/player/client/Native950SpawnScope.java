package com.rs.game.player.client;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/** Explicit region boundary for staged 950 data-spawn migration. Identity checks still apply. */
public final class Native950SpawnScope {
    public static final String PROPERTY = "ataraxia950.npcRegions";
    private final Set<Integer> regions;
    private Native950SpawnScope(Set<Integer> regions) { this.regions = regions; }
    public static Native950SpawnScope fromProperty() { return parse(System.getProperty(PROPERTY, "")); }
    public static Native950SpawnScope parse(String text) {
        if (text == null || text.trim().isEmpty()) return new Native950SpawnScope(null);
        Set<Integer> values = new TreeSet<Integer>();
        for (String value : text.split(",", -1)) {
            String trimmed = value.trim();
            if (!trimmed.matches("[0-9]{1,5}")) throw new IllegalArgumentException("Invalid 950 NPC region: " + value);
            int region = Integer.parseInt(trimmed);
            if (region > 65535) throw new IllegalArgumentException("950 NPC region exceeds 65535: " + region);
            values.add(region);
        }
        return new Native950SpawnScope(Collections.unmodifiableSet(values));
    }
    public boolean allows(int region) {
        return region >= 0 && region <= 65535 && (regions == null || regions.contains(region));
    }
    @Override public String toString() { return regions == null ? "all regions" : regions.toString(); }
}
