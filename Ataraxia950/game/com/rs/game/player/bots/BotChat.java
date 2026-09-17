package com.rs.game.player.bots;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Phrase pools used by Stealing Creation bots to spice up matches with chat
 * lines. Lines are short, lower-case, and feel like quick keyboard mashing
 * rather than polished prose. The script chooses when to talk; this class
 * only owns the strings.
 */
public final class BotChat {

    public static final String[] LOBBY_GREET = {
            "yo", "hi", "sup", "gl team", "let's go", "ready", "easy w incoming",
            "rdy", "hf", "lets win this one", "noobs gonna lose", "wat tier 5 ppl"
    };

    public static final String[] GAME_START = {
            "lets go", "go go go", "rush their base", "let's gather first",
            "stay together", "team up", "spread out", "ill mine", "ill chop",
            "ill fish", "ill hunt", "kiln up first"
    };

    public static final String[] FIGHT_OPEN = {
            "got him", "tagged", "1v1", "come here", "u gonna die", "come here noob",
            "incoming", "heads up", "engaging", "on me", "lol", "ez"
    };

    public static final String[] FIGHT_CALL = {
            "help me", "+1 plz", "need backup", "they pushing", "double on him",
            "focus this guy", "kill this one", "low hp here", "finish him"
    };

    public static final String[] KILL_TAUNT = {
            "ez", "owned", "git gud", "+1", "lmao", "sit down", "easy", "next",
            "haha", "rip", "noob", "too slow"
    };

    public static final String[] DEATH_REACTION = {
            "rip", "fml", "wtf", "lag", "bs", "no way", "lucky", "cmon",
            "i was 1hp lol", "ill be back", "brb"
    };

    public static final String[] FOUND_T5 = {
            "rune!", "tier 5!", "lemme get this", "found rune", "rune over here",
            "5 right here", "score"
    };

    public static final String[] DEPOSITING = {
            "depositing", "back to base", "brb base", "deposit run", "dropping off",
            "scoring up"
    };

    public static final String[] LOW_HP = {
            "low", "low hp", "need food", "running", "one more hit", "outta here",
            "back off", "almost dead"
    };

    public static final String[] RECOVERED = {
            "back", "ok im up", "good now", "ready again", "lets go again"
    };

    public static final String[] IDLE_BANTER = {
            "anyone seen the t5 spot?", "good map", "kiln on west", "watch the flank",
            "ill scout", "this is fun", "another match?", "afk a sec"
    };

    public static final String[] SPECIAL_CALLOUT = {
            "spec", "speccing", "spec on him", "watch out spec", "spec time"
    };

    public static final String[] PILED_ON = {
            "+1", "got u covered", "on it", "joining", "ill take this one", "gank"
    };

    public static final String[] SPOTTED_CARRIER = {
            "carrier!", "rich one", "drop em", "loot pinata", "look at his inv lol",
            "stop him", "hes loaded"
    };

    public static final String[] HELP_RETREAT = {
            "help me!", "they got me", "save me", "low low", "too many"
    };

    public static final String[] DEPOSIT_BIG = {
            "big cash", "huge deposit", "+50", "free score", "gg ez score"
    };

    public static final String[] AFK_QUICK = {
            "brb", "phone", "1 sec", "afk", "dog", "knock at door"
    };

    public static final String[] FIRST_BLOOD = {
            "first!", "got him first", "opening kill", "1-0 us", "+1 already"
    };

    public static String pick(String[] pool) {
        if (pool == null || pool.length == 0) {
            return "";
        }
        return pool[ThreadLocalRandom.current().nextInt(pool.length)];
    }

    private BotChat() {
    }
}
