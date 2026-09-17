package com.rs.game.player.bots;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Per-bot trait bundle. Generated at spawn time and consulted by scripts to add
 * variation to timing, decision making, combat reactions and chat. Two bots with
 * the same Role can still play very differently because they roll different traits.
 */
public final class BotPersonality {

    public enum Archetype {
        ROOKIE,    // sloppy, slow, makes mistakes, talks little
        GRINDER,   // efficient gatherer, avoids fights, deposits frequently
        RUSHER,    // very aggressive, low caution, dives into PvP
        VETERAN,   // efficient, uses spec/prayer, retreats smartly
        SOCIAL     // chatty, emotes, decent fighter but distractable
    }

    private final Archetype archetype;
    private final double aggression;        // 0..1 willingness to engage
    private final double caution;           // 0..1 retreat & food eagerness
    private final double efficiency;        // 0..1 picks optimal target/route
    private final double chattiness;        // 0..1 force-talk frequency per opportunity
    private final double greed;             // 0..1 hoards before depositing
    private final double pileOnTendency;    // 0..1 likelihood to join teammate fights
    private final double idleTendency;      // 0..1 chance to insert idle pauses
    private final int reactionTicks;        // base reaction lag in pulses
    private final int eatHpPercent;         // % HP at which to eat
    private final int retreatHpPercent;     // % HP at which to disengage
    private final boolean prayerFlick;      // attempts protection prayers in combat
    private final boolean usesSpec;         // triggers special attack on wounded targets
    private final boolean swapsWeapon;      // equips upgraded tool when found
    private final boolean callsTargets;     // shouts target callouts in chat
    private final boolean misclicks;        // occasionally takes wrong action

    private BotPersonality(Archetype archetype, double aggression, double caution, double efficiency,
                           double chattiness, double greed, double pileOnTendency, double idleTendency,
                           int reactionTicks, int eatHpPercent, int retreatHpPercent, boolean prayerFlick,
                           boolean usesSpec, boolean swapsWeapon, boolean callsTargets, boolean misclicks) {
        this.archetype = archetype;
        this.aggression = aggression;
        this.caution = caution;
        this.efficiency = efficiency;
        this.chattiness = chattiness;
        this.greed = greed;
        this.pileOnTendency = pileOnTendency;
        this.idleTendency = idleTendency;
        this.reactionTicks = reactionTicks;
        this.eatHpPercent = eatHpPercent;
        this.retreatHpPercent = retreatHpPercent;
        this.prayerFlick = prayerFlick;
        this.usesSpec = usesSpec;
        this.swapsWeapon = swapsWeapon;
        this.callsTargets = callsTargets;
        this.misclicks = misclicks;
    }

    public Archetype getArchetype() {
        return archetype;
    }

    public double getAggression() {
        return aggression;
    }

    public double getCaution() {
        return caution;
    }

    public double getEfficiency() {
        return efficiency;
    }

    public double getChattiness() {
        return chattiness;
    }

    public double getGreed() {
        return greed;
    }

    public double getPileOnTendency() {
        return pileOnTendency;
    }

    public double getIdleTendency() {
        return idleTendency;
    }

    public int getReactionTicks() {
        return reactionTicks;
    }

    public int getEatHpPercent() {
        return eatHpPercent;
    }

    public int getRetreatHpPercent() {
        return retreatHpPercent;
    }

    public boolean usesPrayerFlick() {
        return prayerFlick;
    }

    public boolean usesSpec() {
        return usesSpec;
    }

    public boolean swapsWeapon() {
        return swapsWeapon;
    }

    public boolean callsTargets() {
        return callsTargets;
    }

    public boolean misclicks() {
        return misclicks;
    }

    public boolean rollChat() {
        return ThreadLocalRandom.current().nextDouble() < chattiness;
    }

    public boolean rollPileOn() {
        return ThreadLocalRandom.current().nextDouble() < pileOnTendency;
    }

    public boolean rollIdle() {
        return ThreadLocalRandom.current().nextDouble() < idleTendency;
    }

    public boolean rollMisclick() {
        return misclicks && ThreadLocalRandom.current().nextDouble() < 0.05;
    }

    public boolean rollSubOptimal() {
        return ThreadLocalRandom.current().nextDouble() > efficiency;
    }

    public boolean shouldEat(int hp, int maxHp) {
        if (maxHp <= 0) {
            return false;
        }
        int percent = (hp * 100) / maxHp;
        return percent <= eatHpPercent;
    }

    public boolean shouldRetreat(int hp, int maxHp) {
        if (maxHp <= 0) {
            return false;
        }
        int percent = (hp * 100) / maxHp;
        return percent <= retreatHpPercent;
    }

    /**
     * Produces a delay (in pulses) for a routine action. Adds a small jitter around
     * the personality's reaction baseline plus an occasional longer "ponder" pause
     * when idleTendency rolls. The result is bounded so callers cannot accidentally
     * stall a bot for many seconds.
     */
    public int reactionDelay(int min, int max) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int base = Math.max(min, Math.min(max, reactionTicks + random.nextInt(min, max + 1)));
        if (rollIdle()) {
            base += random.nextInt(2, 6);
        }
        return Math.max(1, base);
    }

    public int reactionDelay() {
        return reactionDelay(1, 3);
    }

    public static BotPersonality random() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Archetype[] pool = Archetype.values();
        int roll = random.nextInt(100);
        Archetype archetype;
        if (roll < 20) {
            archetype = Archetype.ROOKIE;
        } else if (roll < 50) {
            archetype = Archetype.GRINDER;
        } else if (roll < 70) {
            archetype = Archetype.RUSHER;
        } else if (roll < 85) {
            archetype = Archetype.VETERAN;
        } else {
            archetype = Archetype.SOCIAL;
        }
        return forArchetype(archetype);
    }

    public static BotPersonality forArchetype(Archetype archetype) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double aggression;
        double caution;
        double efficiency;
        double chattiness;
        double greed;
        double pileOnTendency;
        double idleTendency;
        int reactionTicks;
        int eatHp;
        int retreatHp;
        boolean prayerFlick;
        boolean usesSpec;
        boolean swapsWeapon;
        boolean callsTargets;
        boolean misclicks;
        switch (archetype) {
            case ROOKIE:
                aggression = jitter(random, 0.30, 0.20);
                caution = jitter(random, 0.50, 0.20);
                efficiency = jitter(random, 0.35, 0.15);
                chattiness = jitter(random, 0.20, 0.15);
                greed = jitter(random, 0.50, 0.20);
                pileOnTendency = jitter(random, 0.45, 0.20);
                idleTendency = jitter(random, 0.20, 0.10);
                reactionTicks = 3;
                eatHp = 70;
                retreatHp = 30;
                prayerFlick = false;
                usesSpec = random.nextBoolean();
                swapsWeapon = false;
                callsTargets = false;
                misclicks = true;
                break;
            case GRINDER:
                aggression = jitter(random, 0.25, 0.10);
                caution = jitter(random, 0.70, 0.15);
                efficiency = jitter(random, 0.80, 0.10);
                chattiness = jitter(random, 0.10, 0.10);
                greed = jitter(random, 0.75, 0.15);
                pileOnTendency = jitter(random, 0.20, 0.10);
                idleTendency = jitter(random, 0.10, 0.05);
                reactionTicks = 2;
                eatHp = 60;
                retreatHp = 45;
                prayerFlick = false;
                usesSpec = false;
                swapsWeapon = true;
                callsTargets = false;
                misclicks = false;
                break;
            case RUSHER:
                aggression = jitter(random, 0.90, 0.10);
                caution = jitter(random, 0.20, 0.15);
                efficiency = jitter(random, 0.55, 0.15);
                chattiness = jitter(random, 0.45, 0.20);
                greed = jitter(random, 0.20, 0.15);
                pileOnTendency = jitter(random, 0.85, 0.10);
                idleTendency = jitter(random, 0.05, 0.05);
                reactionTicks = 1;
                eatHp = 45;
                retreatHp = 18;
                prayerFlick = false;
                usesSpec = true;
                swapsWeapon = true;
                callsTargets = true;
                misclicks = false;
                break;
            case VETERAN:
                aggression = jitter(random, 0.65, 0.10);
                caution = jitter(random, 0.55, 0.10);
                efficiency = jitter(random, 0.92, 0.05);
                chattiness = jitter(random, 0.25, 0.15);
                greed = jitter(random, 0.35, 0.15);
                pileOnTendency = jitter(random, 0.70, 0.15);
                idleTendency = jitter(random, 0.05, 0.05);
                reactionTicks = 1;
                eatHp = 55;
                retreatHp = 25;
                prayerFlick = true;
                usesSpec = true;
                swapsWeapon = true;
                callsTargets = true;
                misclicks = false;
                break;
            case SOCIAL:
            default:
                aggression = jitter(random, 0.45, 0.15);
                caution = jitter(random, 0.50, 0.15);
                efficiency = jitter(random, 0.50, 0.20);
                chattiness = jitter(random, 0.85, 0.10);
                greed = jitter(random, 0.40, 0.20);
                pileOnTendency = jitter(random, 0.60, 0.15);
                idleTendency = jitter(random, 0.30, 0.15);
                reactionTicks = 2;
                eatHp = 60;
                retreatHp = 30;
                prayerFlick = false;
                usesSpec = random.nextBoolean();
                swapsWeapon = true;
                callsTargets = true;
                misclicks = false;
                break;
        }
        return new BotPersonality(archetype, aggression, caution, efficiency, chattiness, greed,
                pileOnTendency, idleTendency, reactionTicks, eatHp, retreatHp, prayerFlick, usesSpec,
                swapsWeapon, callsTargets, misclicks);
    }

    private static double jitter(ThreadLocalRandom random, double mid, double radius) {
        double value = mid + (random.nextDouble() * 2.0 - 1.0) * radius;
        if (value < 0.0) {
            return 0.0;
        }
        if (value > 1.0) {
            return 1.0;
        }
        return value;
    }
}
