package com.rs.game.player.bots;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Bots that pick a city, wander around it for a while, occasionally chat with
 * nearby players, then teleport to a different destination. Distilled from
 * 2009scape's Adventurer script — minus the banking/GE/combat plumbing, which
 * would each need a separate porting pass against this server's APIs.
 *
 * The point of this bot is presence: it makes cities feel populated and gives
 * real players something to wave at.
 */
public class WandererBotScript extends BotScript {

    private static final String[] GREETINGS = {
            "hi", "hey", "yo", "sup", "hello", "morning", "what's up", "o/"
    };

    private static final String[] OBSERVATIONS = {
            "lovely day", "nice gear", "this place is busy", "anyone training here?",
            "love this city", "haven't been here in a while", "always good to be back",
            "anyone selling rune?", "world feels alive today", "great spot to chill"
    };

    private static final String[] TRAVEL_LINES = {
            "off to varrock", "heading to falador", "bank time", "off to the next adventure",
            "see ya", "later", "moving on", "time to explore", "brb"
    };

    private static final String[] SOLO_MUTTERS = {
            "hmm", "interesting", "where to next", "so much to do", "i should train",
            "another day, another adventure", "..."
    };

    private final BotPersonality personality;

    private State state = State.SPAWNING;
    private WorldTile currentDestination;
    private int ticksAtDestination;
    private int wanderRadius;
    private int travelDelay;
    private int chatCooldown;

    public WandererBotScript() {
        this(BotPersonality.random());
    }

    public WandererBotScript(BotPersonality personality) {
        this.personality = personality == null ? BotPersonality.random() : personality;
    }

    public WandererBotScript(BotPersonality.Archetype archetype) {
        this(archetype == null ? BotPersonality.random() : BotPersonality.forArchetype(archetype));
    }

    @Override
    protected void onStart() {
        currentDestination = BotLocations.randomDestination();
        wanderRadius = pickWanderRadius();
        chatCooldown = ThreadLocalRandom.current().nextInt(20, 80);
        if (personality.rollChat()) {
            api().forceTalk("hello world");
        }
        delay(personality.reactionDelay(2, 6));
    }

    @Override
    protected boolean canActWhileBusy() {
        return false;
    }

    @Override
    protected void onTick() {
        if (bot().hasFinished() || bot().isDead()) {
            delay(3);
            return;
        }
        if (chatCooldown > 0) {
            chatCooldown--;
        }

        switch (state) {
            case SPAWNING:
                teleportTo(currentDestination, "spawn");
                state = State.WANDERING;
                ticksAtDestination = 0;
                travelDelay = ThreadLocalRandom.current().nextInt(150, 400);
                delay(personality.reactionDelay(2, 5));
                return;

            case WANDERING:
                tickWander();
                return;

            case TRAVELING:
                WorldTile next = pickNewDestination();
                if (personality.rollChat() && chatCooldown <= 0) {
                    api().forceTalk(pick(TRAVEL_LINES));
                    chatCooldown = 30;
                }
                teleportTo(next, "travel");
                currentDestination = next;
                wanderRadius = pickWanderRadius();
                ticksAtDestination = 0;
                travelDelay = ThreadLocalRandom.current().nextInt(150, 400);
                state = State.WANDERING;
                delay(personality.reactionDelay(3, 8));
                return;
        }
    }

    private void tickWander() {
        ticksAtDestination++;
        if (ticksAtDestination >= travelDelay) {
            state = State.TRAVELING;
            return;
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int roll = random.nextInt(100);

        // Try to chat with a nearby real player ~5% of ticks if cooldown allows.
        if (roll < 5 && chatCooldown <= 0 && personality.rollChat()) {
            Player nearby = api().findNearestPlayer(8, p -> p != null && !p.isBot());
            if (nearby != null) {
                api().faceTile(nearby);
                api().forceTalk(pick(GREETINGS) + " " + nearby.getDisplayName().toLowerCase());
                chatCooldown = ThreadLocalRandom.current().nextInt(40, 120);
                return;
            }
        }

        // Occasional solo banter so empty cities don't feel dead either.
        if (roll < 8 && chatCooldown <= 0 && personality.rollChat()) {
            api().forceTalk(pick(random.nextBoolean() ? OBSERVATIONS : SOLO_MUTTERS));
            chatCooldown = ThreadLocalRandom.current().nextInt(50, 150);
            return;
        }

        // Otherwise drift around the current destination.
        if (!bot().hasWalkSteps() && roll < 35) {
            WorldTile target = BotLocations.randomNear(currentDestination, wanderRadius);
            if (!api().routeNear(target, 2)) {
                api().walkNear(target, 4);
            }
            delay(personality.reactionDelay(2, 6));
        } else {
            delay(personality.reactionDelay(1, 4));
        }
    }

    private WorldTile pickNewDestination() {
        // 80% chance to pick somewhere different from where we are now so the bot
        // visibly moves between spots over time.
        for (int attempt = 0; attempt < 5; attempt++) {
            WorldTile candidate = BotLocations.randomDestination();
            if (currentDestination == null || !sameTile(candidate, currentDestination)) {
                return candidate;
            }
        }
        return BotLocations.randomDestination();
    }

    private void teleportTo(WorldTile tile, String reason) {
        if (tile == null) {
            return;
        }
        api().cancelAll();
        bot().setNextWorldTile(tile);
    }

    private int pickWanderRadius() {
        return ThreadLocalRandom.current().nextInt(11, 23);
    }

    private static boolean sameTile(WorldTile a, WorldTile b) {
        return a != null && b != null && a.getX() == b.getX() && a.getY() == b.getY()
                && a.getPlane() == b.getPlane();
    }

    private static String pick(String[] pool) {
        return pool[ThreadLocalRandom.current().nextInt(pool.length)];
    }

    @Override
    public String getDebugInfo() {
        String dest = currentDestination == null
                ? "?"
                : currentDestination.getX() + "," + currentDestination.getY();
        return "wanderer arch=" + personality.getArchetype()
                + " state=" + state
                + " dest=" + dest
                + " radius=" + wanderRadius
                + " ticks=" + ticksAtDestination + "/" + travelDelay;
    }

    private enum State {
        SPAWNING,
        WANDERING,
        TRAVELING
    }
}
