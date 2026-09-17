package com.rs.game.player.bots;

import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Drives bot-to-bot conversational replies near GE/lobby anchors. When a bot
 * speaks (via {@link IdleCrowdBotScript#speakReply} or the normal speak path),
 * this engine classifies the utterance by topic, picks a nearby peer with a
 * roulette-wheel weighted by the peer's role suitability and proximity, and
 * schedules a contextual reply that references the speaker's item when one
 * was advertised.
 *
 * Reply chains are capped at depth 2 (initial speaker + one reply + one
 * follow-up) so the GE doesn't slip into endless bot debate. A recently-replied
 * bot is locked out for 12 seconds so the same voice doesn't dominate the
 * crowd. AFK/QUIET/PASSERBY bots almost never reply — the silent majority
 * stays silent.
 */
public final class BotConversation {

    public enum Topic {
        TRADE_SELL,
        TRADE_BUY,
        PRICE_CHECK,
        SWAP,
        GREETING,
        EMOTE_REACT,
        NEWBIE_QUESTION,
        SKILLING,
        FASHION,
        AFK_STATUS,
        BANTER,
        UNREPLYABLE
    }

    private static final int MAX_DEPTH = 2;
    private static final int REPLY_CHANCE_PERCENT_DEPTH_0 = 32;
    private static final int REPLY_CHANCE_PERCENT_DEPTH_1 = 18;
    private static final int REPLY_DELAY_MIN_TICKS = 2;
    private static final int REPLY_DELAY_MAX_TICKS = 7;
    private static final int PEER_SEARCH_RADIUS = 9;
    private static final int REPLY_CHAT_COOLDOWN_MIN = 80;
    private static final int REPLY_CHAT_COOLDOWN_MAX = 200;
    private static final int EMOTE_WITH_REPLY_PERCENT = 8;
    private static final int MAX_ITEM_LENGTH = 24;
    private static final long RECENT_RESPONDER_TTL_MS = 12_000L;
    private static final long RECENT_SWEEP_INTERVAL_MS = 30_000L;

    private static final Map<String, Long> RECENT_RESPONDERS = new ConcurrentHashMap<>();
    private static volatile long nextSweepAt;

    // Reply pools split into ITEM (templated, used when the speaker's
    // utterance contained a parseable item) and GENERIC (plain). The engine
    // picks the templated pool when an item was extracted, otherwise the
    // generic pool. Item placeholders use {item}; if extraction failed the
    // template falls back to "that".
    private static final String[] TRADE_SELL_NEUTRAL_ITEM = {
            "how much for {item}", "{item} how much", "decent {item}",
            "bit high for {item}", "tempting", "got any more {item}",
            "how much per {item}", "is {item} noted", "what qty {item}",
            "seen cheaper {item}", "fair price on {item}?"
    };
    private static final String[] TRADE_SELL_NEUTRAL_GENERIC = {
            "how much", "price?", "decent", "bit high", "bit low",
            "tempting", "how much per"
    };
    private static final String[] TRADE_SELL_BUYER_ITEM = {
            "i'll take some {item}", "wtb {item}", "i'll buy {item}",
            "buying {item} too", "got any noted {item}", "send trade i want {item}",
            "need {item} for a trip", "i can take the {item}", "how many {item}"
    };
    private static final String[] TRADE_SELL_BUYER_GENERIC = {
            "i'll take some", "send trade", "i'll buy", "pm me",
            "got any noted", "wtb"
    };
    private static final String[] TRADE_SELL_MERCHER_ITEM = {
            "i'll buy {item} higher", "i pay more for {item}",
            "splitting {item}?", "i'll outbid on {item}", "i'll flip {item}",
            "bulk {item} i'll price it"
    };
    private static final String[] TRADE_SELL_MERCHER_GENERIC = {
            "i'll buy higher", "i pay more", "merch life",
            "i'll outbid", "split it"
    };
    private static final String[] TRADE_BUY_NEUTRAL_ITEM = {
            "got some {item}", "for what price on {item}", "how many {item}",
            "everyone needs {item}", "{item} prices crazy", "i might have {item}",
            "not sure i want to sell {item}", "{item} moving today"
    };
    private static final String[] TRADE_BUY_NEUTRAL_GENERIC = {
            "got some", "for what price", "how many", "no thanks",
            "what u offering", "ge prices crazy"
    };
    private static final String[] TRADE_BUY_SELLER_ITEM = {
            "got {item}", "wts {item}", "got plenty of {item}",
            "i can do {item}", "sending {item} now", "i've got noted {item}",
            "trade me for {item}", "{item} in bank"
    };
    private static final String[] TRADE_BUY_SELLER_GENERIC = {
            "got it", "wts", "send trade", "got plenty", "i'll do it",
            "stocked up"
    };
    private static final String[] PRICE_CHECK_ITEM = {
            "{item}? few hundred k", "{item} like 1m maybe",
            "no clue on {item} tbh", "{item} prices crazy",
            "more than that for {item}", "{item} less than you'd think",
            "depends on the day for {item}", "check recent trades on {item}",
            "{item} moves a lot", "i sold {item} earlier"
    };
    private static final String[] PRICE_CHECK_GENERIC = {
            "few hundred k", "1m maybe", "no clue tbh", "ge prices crazy",
            "more than that for sure", "less than you'd think",
            "depends on the day", "check ge"
    };
    private static final String[] SWAP_REPLIES = {
            "i'd swap", "no thanks", "what for", "maybe later",
            "not interested", "interested", "depends on the spread"
    };
    private static final String[] GREETING_REPLIES = {
            "hi", "hey", "wb", "o/", "yo", "sup", "hello", "ello",
            "hey hey", "yo o/"
    };
    private static final String[] EMOTE_REACT_REPLIES = {
            "lol", "ty", "np", "ikr", "yea", "gl", "hf", "true",
            "lol same", "fr"
    };
    private static final String[] NEWBIE_REPLIES = {
            "look around the ge", "bank is right there", "ask in help cc",
            "good luck", "type ::commands", "everyone starts somewhere",
            "google it", "wiki helps"
    };
    private static final String[] SKILLING_REPLIES = {
            "almost 99", "xp is xp", "long way to go", "feels endless",
            "bonus xp helps", "training is brutal", "halfway done",
            "maxed soon", "need more supplies", "bank skills never end",
            "buy mats first"
    };
    private static final String[] FASHION_REPLIES = {
            "drip", "love the colors", "looks good", "10/10",
            "needs work", "matching is hard", "fashion scape best scape",
            "rate mine", "outfit goals"
    };
    private static final String[] AFK_REPLIES = {
            "wb", "k", "have fun", "later", "cya", "afk too", "rip"
    };
    private static final String[] BANTER_REPLIES = {
            "true", "yea", "lol", "for real", "fr", "agreed", "facts",
            "this", "spot on", "preach"
    };

    // Subset of EMOTE_ANIMS suited for conversational reactions — yes, no,
    // think, wave, shrug, laugh. Excludes large emotes (jump, dance) that
    // would look out of place mid-banter.
    private static final int[] REPLY_EMOTE_ANIMS = { 855, 856, 857, 860, 861, 864 };

    private BotConversation() {
    }

    /**
     * Hook called whenever a crowd bot speaks. Rolls a chance, classifies the
     * utterance, picks a nearby peer with a plausible role, and schedules a
     * contextual reply. {@code depth} is the speaker's position in the reply
     * chain (0 = original utterance, 1 = first reply, etc.). Replies stop
     * being scheduled once depth hits {@link #MAX_DEPTH}.
     */
    public static void onUtterance(BotPlayer speaker, IdleCrowdBotScript script,
            String message, int depth) {
        if (speaker == null || script == null || message == null || message.isEmpty()
                || speaker.hasFinished()) {
            return;
        }
        if (depth >= MAX_DEPTH) {
            return;
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int chance = depth == 0 ? REPLY_CHANCE_PERCENT_DEPTH_0 : REPLY_CHANCE_PERCENT_DEPTH_1;
        if (random.nextInt(100) >= chance) {
            return;
        }
        Topic topic = classify(message);
        if (topic == Topic.UNREPLYABLE) {
            return;
        }
        long now = System.currentTimeMillis();
        maybeSweep(now);
        final BotPlayer originalSpeaker = speaker;
        final Topic finalTopic = topic;
        final String item = extractItem(message, topic);
        final int nextDepth = depth + 1;
        final int delay = random.nextInt(REPLY_DELAY_MIN_TICKS, REPLY_DELAY_MAX_TICKS + 1);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    if (originalSpeaker.hasFinished()) {
                        return;
                    }
                    BotPlayer responder = pickResponder(originalSpeaker, finalTopic);
                    if (responder == null) {
                        return;
                    }
                    IdleCrowdBotScript responderScript = BotManager.getCrowdScript(responder);
                    if (responderScript == null) {
                        return;
                    }
                    String reply = pickReply(responderScript.getRole(), finalTopic, item);
                    if (reply == null || reply.isEmpty()) {
                        return;
                    }
                    RECENT_RESPONDERS.put(responder.getUsername(), System.currentTimeMillis());
                    responderScript.speakReply(reply, nextDepth, originalSpeaker,
                            REPLY_CHAT_COOLDOWN_MIN, REPLY_CHAT_COOLDOWN_MAX);
                } finally {
                    stop();
                }
            }
        }, delay);
    }

    public static Topic classify(String message) {
        if (message == null || message.isEmpty()) {
            return Topic.UNREPLYABLE;
        }
        String m = message.toLowerCase().trim();
        if (m.length() <= 1) {
            return Topic.UNREPLYABLE;
        }
        if (m.equals("afk") || m.equals("brb") || m.equals("1 sec") || m.equals("phone")) {
            return Topic.AFK_STATUS;
        }
        if (startsWithWord(m, "hi") || startsWithWord(m, "hey") || startsWithWord(m, "hello")
                || m.equals("yo") || m.equals("wb") || m.equals("o/") || m.equals("sup")
                || m.equals("ello") || m.equals("yo o/")) {
            return Topic.GREETING;
        }
        if (startsWithWord(m, "lol") || startsWithWord(m, "lmao") || startsWithWord(m, "ty")
                || startsWithWord(m, "thx") || startsWithWord(m, "gz") || startsWithWord(m, "gratz")
                || m.equals("nice") || m.equals("nice cape") || m.equals("np")
                || m.equals("ikr") || m.equals("hf") || m.equals("gl") || m.equals("true")
                || m.equals("fr") || m.equals("facts")) {
            return Topic.EMOTE_REACT;
        }
        if (m.startsWith("pc on ") || m.startsWith("pc ") || m.startsWith("price check")
                || m.startsWith("pricecheck") || m.startsWith("how much is")
                || m.startsWith("what is ") && m.contains("worth")) {
            return Topic.PRICE_CHECK;
        }
        if (m.startsWith("swap")) {
            return Topic.SWAP;
        }
        if (m.startsWith("selling") || m.startsWith("sellling") || m.startsWith("sellign")
                || m.startsWith("wts") || m.startsWith("dumping")
                || m.startsWith("unloading") || m.startsWith("offloading")
                || m.startsWith("got ") && !m.startsWith("got any ")
                || m.startsWith("i got ") || m.startsWith("i have ")
                || m.contains("for sale") || m.contains("bank sale")
                || m.contains("taking offers") || m.contains("need cash")) {
            return Topic.TRADE_SELL;
        }
        if (m.startsWith("buying") || m.startsWith("buyign") || m.startsWith("wtb")
                || m.startsWith("lf ") || m.startsWith("lfb ") || m.startsWith("need ")
                || m.contains("anyone selling") || m.contains("anyone got")
                || m.contains("who has") || m.contains("looking for")
                || m.startsWith("i'll buy") || m.startsWith("ill buy")
                || m.startsWith("i'll take") || m.startsWith("ill take")
                || m.startsWith("i can do") || m.startsWith("got plenty")
                || m.startsWith("got any") || m.startsWith("got some")
                || m.equals("got it") || m.equals("send trade")) {
            return Topic.TRADE_BUY;
        }
        if (m.startsWith("how do i") || m.startsWith("where is")
                || m.startsWith("what world") || m.startsWith("first time")
                || m.startsWith("any tips") || m.startsWith("how much is")) {
            return Topic.NEWBIE_QUESTION;
        }
        if (m.contains(" xp") || m.startsWith("xp ") || m.contains("training")
                || m.contains("alching") || m.contains("99 ") || m.contains(" 99")
                || m.startsWith("almost") || m.contains("bank skills")
                || m.contains("bank is full") || m.contains("checking offers")) {
            return Topic.SKILLING;
        }
        if (m.contains(" fit") || m.contains("outfit") || m.contains("matching")
                || m.contains("fashion") || m.contains("hat stays")
                || m.contains("rate ") || m.contains("drip")) {
            return Topic.FASHION;
        }
        return Topic.BANTER;
    }

    private static boolean startsWithWord(String message, String word) {
        if (!message.startsWith(word)) {
            return false;
        }
        if (message.length() == word.length()) {
            return true;
        }
        char next = message.charAt(word.length());
        return next == ' ' || next == ',' || next == '!' || next == '.' || next == '?';
    }

    /**
     * Tries to pull the item name out of a trade/price-check utterance so
     * replies can reference it ("i'll take some sharks" instead of generic
     * "i'll take some"). Returns null if extraction fails — callers fall back
     * to the generic reply pool.
     */
    private static String extractItem(String message, Topic topic) {
        if (topic != Topic.TRADE_SELL && topic != Topic.TRADE_BUY
                && topic != Topic.PRICE_CHECK) {
            return null;
        }
        // Order matters — longer prefixes must come first so "pc on" wins
        // over "pc " on a "pc on torva" message.
        String[] prefixes = {
                "price check ", "pc on ", "pc ",
                "selling ", "sellling ", "sellign ", "wts ", "dumping ",
                "unloading ", "offloading ", "taking offers on ", "got plenty of ",
                "got some ", "got ", "i got ", "i have ",
                "buying ", "buyign ", "wtb ", "lfb ", "lf ", "need ",
                "looking for ", "anyone selling ", "anyone got ", "who has ",
                "i'll buy ", "ill buy ", "i'll take ", "ill take ",
                "i can do ", "got plenty of ", "got any noted ",
                "got any ", "got some ", "got "
        };
        String stripped = message.toLowerCase().trim();
        for (String prefix : prefixes) {
            if (stripped.startsWith(prefix)) {
                stripped = stripped.substring(prefix.length()).trim();
                break;
            }
        }
        if (stripped.isEmpty()) {
            return null;
        }
        String[] words = stripped.split("\\s+");
        // Skip leading numeric tokens — those are quantities, not the item.
        int start = 0;
        while (start < words.length
                && (words[start].isEmpty() || containsDigit(words[start])
                || isLeadingItemModifier(cleanItemWord(words[start])))) {
            start++;
        }
        StringBuilder item = new StringBuilder();
        for (int i = start; i < words.length; i++) {
            String w = cleanItemWord(words[i]);
            if (w.isEmpty()) {
                continue;
            }
            if (containsDigit(w) || isItemStopWord(w)) {
                break;
            }
            if (item.length() > 0) {
                item.append(' ');
            }
            item.append(w);
            if (item.length() > MAX_ITEM_LENGTH) {
                break;
            }
        }
        String result = item.toString().trim();
        return result.length() < 2 ? null : result;
    }

    private static String cleanItemWord(String word) {
        if (word == null || word.isEmpty()) {
            return "";
        }
        int start = 0;
        int end = word.length();
        while (start < end && !isItemWordChar(word.charAt(start))) {
            start++;
        }
        while (end > start && !isItemWordChar(word.charAt(end - 1))) {
            end--;
        }
        return start >= end ? "" : word.substring(start, end);
    }

    private static boolean isItemWordChar(char c) {
        return Character.isLetter(c) || c == '\'';
    }

    private static boolean containsDigit(String word) {
        for (int i = 0; i < word.length(); i++) {
            if (Character.isDigit(word.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isItemStopWord(String word) {
        switch (word) {
            case "for":
            case "at":
            case "@":
            case "ea":
            case "each":
            case "any":
            case "price":
            case "pls":
            case "please":
            case "noted":
            case "bulk":
            case "split":
            case "now":
            case "quick":
            case "instant":
            case "cheap":
            case "fast":
            case "asap":
            case "today":
            case "soon":
                return true;
            default:
                return false;
        }
    }

    private static boolean isLeadingItemModifier(String word) {
        switch (word) {
            case "a":
            case "an":
            case "the":
            case "some":
            case "any":
            case "my":
            case "your":
            case "that":
            case "these":
            case "those":
            case "noted":
            case "bulk":
            case "spare":
            case "extra":
            case "cheap":
            case "quick":
                return true;
            default:
                return false;
        }
    }

    private static BotPlayer pickResponder(BotPlayer speaker, Topic topic) {
        Collection<BotPlayer> bots = BotManager.getBots();
        long now = System.currentTimeMillis();
        List<BotPlayer> candidates = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();
        int total = 0;
        for (BotPlayer candidate : bots) {
            if (candidate == null || candidate == speaker || candidate.hasFinished()
                    || candidate.isDead()) {
                continue;
            }
            if (candidate.getPlane() != speaker.getPlane()) {
                continue;
            }
            int distance = Math.max(Math.abs(candidate.getX() - speaker.getX()),
                    Math.abs(candidate.getY() - speaker.getY()));
            if (distance > PEER_SEARCH_RADIUS) {
                continue;
            }
            // Skip bots that just replied — same voice shouldn't dominate.
            Long lastReply = RECENT_RESPONDERS.get(candidate.getUsername());
            if (lastReply != null && lastReply > now - RECENT_RESPONDER_TTL_MS) {
                continue;
            }
            IdleCrowdBotScript script = BotManager.getCrowdScript(candidate);
            if (script == null) {
                continue;
            }
            int weight = roleWeight(script.getRole(), topic);
            if (weight <= 0) {
                continue;
            }
            // Distance bonus: distance 0 multiplies weight by 10, distance 9
            // by 1. Keeps closer bots favored without making far bots
            // impossible.
            int weighted = weight * Math.max(1, PEER_SEARCH_RADIUS + 1 - distance);
            candidates.add(candidate);
            weights.add(weighted);
            total += weighted;
        }
        if (candidates.isEmpty() || total <= 0) {
            return null;
        }
        // Roulette-wheel pick — gives less-likely roles a real (small)
        // chance to chime in instead of always defaulting to the highest
        // scoring bot.
        int roll = ThreadLocalRandom.current().nextInt(total);
        int acc = 0;
        for (int i = 0; i < candidates.size(); i++) {
            acc += weights.get(i);
            if (roll < acc) {
                return candidates.get(i);
            }
        }
        return candidates.get(candidates.size() - 1);
    }

    private static int roleWeight(IdleCrowdBotScript.Role role, Topic topic) {
        if (role == null || role == IdleCrowdBotScript.Role.AFK) {
            return 0;
        }
        if (role == IdleCrowdBotScript.Role.PASSERBY) {
            // Passersby are mid-walk — only nod at greetings as they pass.
            return topic == Topic.GREETING ? 1 : 0;
        }
        if (role == IdleCrowdBotScript.Role.QUIET) {
            return (topic == Topic.GREETING || topic == Topic.BANTER
                    || topic == Topic.EMOTE_REACT) ? 1 : 0;
        }
        switch (topic) {
            case TRADE_SELL:
                if (role == IdleCrowdBotScript.Role.BUYER) return 9;
                if (role == IdleCrowdBotScript.Role.MERCHER) return 8;
                if (role == IdleCrowdBotScript.Role.CHATTY) return 4;
                if (role == IdleCrowdBotScript.Role.NEWCOMER) return 2;
                return 2;
            case TRADE_BUY:
                if (role == IdleCrowdBotScript.Role.SELLER) return 9;
                if (role == IdleCrowdBotScript.Role.MERCHER) return 8;
                if (role == IdleCrowdBotScript.Role.CHATTY) return 4;
                return 2;
            case PRICE_CHECK:
                if (role == IdleCrowdBotScript.Role.MAXED_SHOWOFF) return 8;
                if (role == IdleCrowdBotScript.Role.MERCHER) return 8;
                if (role == IdleCrowdBotScript.Role.CHATTY) return 5;
                return 2;
            case SWAP:
                if (role == IdleCrowdBotScript.Role.MERCHER) return 7;
                if (role == IdleCrowdBotScript.Role.CHATTY) return 4;
                return 2;
            case GREETING:
                if (role == IdleCrowdBotScript.Role.CHATTY) return 8;
                if (role == IdleCrowdBotScript.Role.FASHIONSCAPER) return 5;
                if (role == IdleCrowdBotScript.Role.NEWCOMER) return 5;
                return 4;
            case EMOTE_REACT:
                if (role == IdleCrowdBotScript.Role.CHATTY) return 6;
                if (role == IdleCrowdBotScript.Role.FASHIONSCAPER) return 4;
                return 3;
            case NEWBIE_QUESTION:
                if (role == IdleCrowdBotScript.Role.MAXED_SHOWOFF) return 9;
                if (role == IdleCrowdBotScript.Role.CHATTY) return 7;
                if (role == IdleCrowdBotScript.Role.BANKSTANDER) return 4;
                if (role == IdleCrowdBotScript.Role.NEWCOMER) return 1;
                return 3;
            case SKILLING:
                if (role == IdleCrowdBotScript.Role.FLETCHER
                        || role == IdleCrowdBotScript.Role.CRAFTER
                        || role == IdleCrowdBotScript.Role.HERBLORE
                        || role == IdleCrowdBotScript.Role.ALCHER) return 8;
                if (role == IdleCrowdBotScript.Role.MAXED_SHOWOFF) return 5;
                if (role == IdleCrowdBotScript.Role.BANKSTANDER) return 5;
                if (role == IdleCrowdBotScript.Role.CHATTY) return 4;
                return 2;
            case FASHION:
                if (role == IdleCrowdBotScript.Role.FASHIONSCAPER) return 9;
                if (role == IdleCrowdBotScript.Role.MAXED_SHOWOFF) return 7;
                if (role == IdleCrowdBotScript.Role.CHATTY) return 4;
                return 2;
            case AFK_STATUS:
                if (role == IdleCrowdBotScript.Role.CHATTY) return 5;
                return 3;
            case BANTER:
                if (role == IdleCrowdBotScript.Role.CHATTY) return 7;
                if (role == IdleCrowdBotScript.Role.FASHIONSCAPER) return 4;
                return 3;
            default:
                return 0;
        }
    }

    private static String pickReply(IdleCrowdBotScript.Role role, Topic topic, String item) {
        boolean hasItem = item != null && !item.isEmpty();
        switch (topic) {
            case TRADE_SELL:
                if (role == IdleCrowdBotScript.Role.MERCHER) {
                    return resolveItem(pick(hasItem ? TRADE_SELL_MERCHER_ITEM
                            : TRADE_SELL_MERCHER_GENERIC), item);
                }
                if (role == IdleCrowdBotScript.Role.BUYER) {
                    return resolveItem(pick(hasItem ? TRADE_SELL_BUYER_ITEM
                            : TRADE_SELL_BUYER_GENERIC), item);
                }
                return resolveItem(pick(hasItem ? TRADE_SELL_NEUTRAL_ITEM
                        : TRADE_SELL_NEUTRAL_GENERIC), item);
            case TRADE_BUY:
                if (role == IdleCrowdBotScript.Role.SELLER
                        || role == IdleCrowdBotScript.Role.MERCHER) {
                    return resolveItem(pick(hasItem ? TRADE_BUY_SELLER_ITEM
                            : TRADE_BUY_SELLER_GENERIC), item);
                }
                return resolveItem(pick(hasItem ? TRADE_BUY_NEUTRAL_ITEM
                        : TRADE_BUY_NEUTRAL_GENERIC), item);
            case PRICE_CHECK:
                return resolveItem(pick(hasItem ? PRICE_CHECK_ITEM : PRICE_CHECK_GENERIC), item);
            case SWAP:           return pick(SWAP_REPLIES);
            case GREETING:       return pick(GREETING_REPLIES);
            case EMOTE_REACT:    return pick(EMOTE_REACT_REPLIES);
            case NEWBIE_QUESTION:return pick(NEWBIE_REPLIES);
            case SKILLING:       return pick(SKILLING_REPLIES);
            case FASHION:        return pick(FASHION_REPLIES);
            case AFK_STATUS:     return pick(AFK_REPLIES);
            case BANTER:         return pick(BANTER_REPLIES);
            default:             return null;
        }
    }

    private static String resolveItem(String template, String item) {
        if (template == null) {
            return null;
        }
        if (!template.contains("{item}")) {
            return template;
        }
        return template.replace("{item}", item == null || item.isEmpty() ? "that" : item);
    }

    /** Chance (per 100) that a reply is accompanied by a small emote. */
    static int emoteWithReplyPercent() {
        return EMOTE_WITH_REPLY_PERCENT;
    }

    /** Animation IDs picked from when a reply triggers an accompanying emote. */
    static int[] replyEmoteAnims() {
        return REPLY_EMOTE_ANIMS;
    }

    private static void maybeSweep(long now) {
        if (now < nextSweepAt) {
            return;
        }
        nextSweepAt = now + RECENT_SWEEP_INTERVAL_MS;
        long cutoff = now - RECENT_RESPONDER_TTL_MS;
        RECENT_RESPONDERS.entrySet().removeIf(
                e -> e.getValue() == null || e.getValue() < cutoff);
    }

    private static String pick(String[] pool) {
        if (pool == null || pool.length == 0) {
            return null;
        }
        return pool[ThreadLocalRandom.current().nextInt(pool.length)];
    }
}
