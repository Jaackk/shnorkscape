package com.rs.utils.data.parsers.npcs;

import com.rs.utils.Utils;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** The ordinary NPC.drop table algorithm, shared by legacy content and native clients. */
public final class NPCDropTableRolls {
    private NPCDropTableRolls() { }
    public static final int MAX_RANDOM_DROPS = 3;
    public interface Rolls {
        double percentage(double maximum);
        void shuffle(List<NPCDrop> drops);
        int inclusive(int maximum);
    }
    public static final Rolls LEGACY_ROLLS = new Rolls() {
        public double percentage(double maximum) { return Utils.getRandomDouble(maximum); }
        public void shuffle(List<NPCDrop> drops) { Collections.shuffle(drops); }
        public int inclusive(int maximum) { return Utils.getRandom(maximum); }
    };
    public static void roll(NPCDrop[] table, double rareMultiplier, double maximum,
            Predicate<NPCDrop> eligible, Consumer<NPCDrop> emit) {
        roll(table, rareMultiplier, maximum, eligible, emit, LEGACY_ROLLS);
    }
    public static void roll(NPCDrop[] table, double rareMultiplier, double maximum,
            Predicate<NPCDrop> eligible, Consumer<NPCDrop> emit, Rolls random) {
        if (table == null) return;
        List<NPCDrop> possible = new ArrayList<NPCDrop>();
        for (NPCDrop drop : table) {
            if (drop == null || !eligible.test(drop)) continue;
            if (drop.getRate() == 100) {
                // Guaranteed rows never compete for the three random slots.
                emit.accept(drop);
            } else {
                double rate = drop.getRate();
                double sample = random.percentage(maximum);
                if (rate < 30) rate *= rareMultiplier;
                // Keep the legacy endpoint exclusions and Utils' [0, maximum+1) range.
                if (sample <= rate && sample != 100 && sample != 0) possible.add(drop);
            }
        }
        random.shuffle(possible);
        for (int i = 0; i < Math.min(MAX_RANDOM_DROPS, possible.size()); i++) emit.accept(possible.get(i));
    }
    /** NPC.sendDrop's inclusive min..max quantity roll. */
    public static int amount(NPCDrop drop) { return amount(drop, LEGACY_ROLLS); }
    public static int amount(NPCDrop drop, Rolls random) {
        return drop.getMinAmount() + random.inclusive(drop.getExtraAmount());
    }
}
