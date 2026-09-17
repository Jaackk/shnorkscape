package com.rs.game.player.bots;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.WorldTile;
import com.rs.game.player.bots.trading.BotTrading;
import com.rs.game.player.content.PublicChatMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Bot that stands around an anchor tile, occasionally fidgets, occasionally
 * chats, occasionally emotes. Designed to make crowd locations (Grand
 * Exchange, banks, lobbies) feel populated.
 *
 * Each bot is assigned a {@link Role} at construction that controls how it
 * behaves. Most real GE-style crowds are mostly silent with a few loud
 * outliers, so default role rolls bias heavily toward AFK/QUIET.
 *
 * Conversation bursts: when one bot speaks, nearby bots (same anchor) get a
 * short window of elevated chat probability. Creates the feel of a back and
 * forth instead of evenly-spaced chatter.
 */
public class IdleCrowdBotScript extends BotScript {

    public enum Role {
        /** Statue. Never moves, never chats. Occasional yawn. ~30% of crowd. */
        AFK,
        /** Rare fidget, no chat. The silent majority. ~35% of crowd. */
        QUIET,
        /** Regular fidget, very rare chat. ~15% of crowd. */
        ACTIVE,
        /** More fidget, chats sometimes, can trigger conversation bursts. ~12%. */
        CHATTY,
        /** Bank-focused idler who faces the booth and occasionally checks offers. */
        BANKSTANDER,
        /** Faces a teller and occasionally advertises buy offers. */
        BUYER,
        /** Faces a teller and occasionally advertises sell offers. */
        SELLER,
        /** Stands in place spamming a single fixed offer. ~8%. */
        MERCHER,
        /** Bankstanding: repeats fletching animations near the booth ring. */
        FLETCHER,
        /** Bankstanding: repeats crafting/gem-cutting animations near the booth ring. */
        CRAFTER,
        /** Bankstanding: repeats potion-mixing animations near the booth ring. */
        HERBLORE,
        /** Bankstanding: repeatedly casts high alchemy near the booth ring. */
        ALCHER,
        /** Fashionscape idler with higher emote/chat frequency. */
        FASHIONSCAPER,
        /** High-level flexer who tends to stand near the busy inner/social areas. */
        MAXED_SHOWOFF,
        /** Edge-of-crowd account that asks newbie-flavoured questions. */
        NEWCOMER,
        /** Passes through the GE lanes instead of standing at one booth tile. */
        PASSERBY
    }

    public static final String[] GE_CHAT = {
            // Trade offers (general)
            "buying rune", "buying whip", "buying dragon claws", "buying sara sword",
            "buying torva any price", "buying dharok set", "buying yew logs", "buying sharks",
            "selling rune armor", "selling magic logs", "selling sharks", "selling yew logs",
            "selling whip", "selling bandos", "selling armadyl", "selling chaotic",
            // Abbreviated trade offers
            "wts d boots", "wts d scim", "wts mage book", "wts torva pls",
            "wts whip 1m", "wts blue mask", "wts party hat",
            "wtb dharok set", "wtb prosely", "wtb 100 sharks", "wtb chaotic",
            "wtb bandos any price", "wtb full void", "wtb magic logs bulk",
            // Swaps
            "swap nox for dks", "swap rune for cash", "swap range for melee",
            "swap mains for ironman", "swap 07 for rs3",
            // Price checks
            "pc on torva pls", "pc 100 sharks", "pc whip vine", "pc full bandos",
            "pc on this", "pc dragon claws",
            // Social / replies / typed-out
            "lol", "ty", "thx", "deal", "no thanks", "bit high", "bit low",
            "ok", "sounds good", "let me think", "wb", "o/", "hi",
            "nice cape", "gz", "gratz", "merch life", "ge prices crazy",
            "rune dropping", "this place packed today", "anyone selling 99 prayer",
            "lf clan", "any clans recruiting", "anyone bossing", "splitting offers",
            // Mild typos / lowercase
            "buyign nats", "selling whpi", "wts bowfa", "any merchs here",
            // AFK / status
            "afk", "brb", "1 sec", "phone"
    };

    public static final String[] CROWD_CHAT = {
            "what a day", "anyone training?", "love this spot", "long line today",
            "bank standing", "afk", "brb", "wb", "o/", "hey", "hi",
            "anyone slaying?", "any bossers here?", "gz", "nice cape",
            "where's the action", "just chillin", "world is busy",
            "lol", "ty", "thx", "ok", "sup", "yo", "hello"
    };

    /** Items a mercher will pick from when generating their fixed phrase. */
    public static final String[] MERCHER_ITEMS = {
            "rune", "whip", "torva", "dragon claws", "dharok set",
            "yew logs", "magic logs", "sharks", "bandos", "armadyl",
            "saradomin sword", "abyssal whip", "barrows gloves", "fire cape",
            "santa hat", "blue partyhat", "phats", "spirit shield", "chaotic"
    };

    private static final String[] BUYER_LINES = {
            "buying herbs", "buying logs", "buying sharks", "buying rune sets",
            "buying broad bolts", "buying prayer pots", "wtb dragon boots",
            "wtb supplies", "instant buying pls", "anyone selling bulk?",
            "buying fish or food", "wtb runes", "buying ores bars",
            "buying crafting mats", "wtb seeds", "buying charms",
            "need boss supplies", "anyone got ammo?", "buying jewelry"
    };

    private static final String[] SELLER_LINES = {
            "selling loot tab", "selling cooked sharks", "selling yew logs",
            "selling herbs", "selling rune armour", "wts supplies",
            "wts clue items", "wts fashion pieces", "offer in ge",
            "selling potion tab", "wts runes", "selling smithing mats",
            "wts gems jewelry", "selling farm stock", "wts pvm supplies",
            "selling ammo", "bank sale mixed stuff"
    };

    private static final String[] BANK_LINES = {
            "checking offers", "one more slot", "bank is full", "need more nats",
            "where did my cash stack go", "offer updated", "waiting on ge",
            "bankstanding gains"
    };

    private static final String[] SKILLING_LINES = {
            "need more logs", "almost out of supplies", "xp is xp", "bank skills go brr",
            "making supplies", "slow but steady", "alching stack done soon"
    };

    private static final String[] FASHION_LINES = {
            "rate fit", "nice outfit", "fashionscape best cape", "matching is overrated",
            "this hat stays on", "bankstanding in style", "need better boots"
    };

    private static final String[] NEWCOMER_LINES = {
            "how do i sell this", "where is the banker", "what world is busy",
            "how much is this worth", "first time here", "why are prices changing",
            "any tips?"
    };

    private static final String[] PASSERBY_LINES = {
            "passing through", "bank run", "gotta restock", "quick ge trip",
            "off to boss", "sup ge", "later"
    };

    /** Verified emote animation IDs from EmotesManager.java. */
    private static final int[] EMOTE_ANIMS = {
            855,  // yes
            856,  // no
            857,  // think
            860,  // wave
            861,  // shrug
            862,  // cheer
            864,  // laugh
            865,  // jump for joy
            866,  // yawn
            7071  // dance
    };

    private static final int[] FLETCHING_ANIMS = {
            24938, 24939, 24943, 19516
    };

    private static final int[] CRAFTING_ANIMS = {
            22774, 22775, 22776, 22777
    };

    private static final int QUIET_WANDER_LIMIT = 7;
    private static final int ACTIVE_WANDER_LIMIT = 8;
    private static final long MARKET_CHAT_FIRST_MIN_MS = 2000L;
    private static final long MARKET_CHAT_FIRST_MAX_MS = 8000L;
    private static final long MARKET_CHAT_INTERVAL_MIN_MS = 15000L;
    private static final long MARKET_CHAT_INTERVAL_MAX_MS = 20000L;

    /** Per-anchor "conversation in progress" timestamps. */
    private static final Map<String, Long> CONVERSATION_WAVES = new ConcurrentHashMap<>();
    private static final long WAVE_DURATION_MS = 6000;

    private final BotPersonality personality;
    private final WorldTile anchor;
    private final WorldTile focusTile;
    private final int wanderRadius;
    private final String[] chatPool;
    private final Role role;
    private final String mercherPhrase;

    private int chatCooldown;
    private int emoteCooldown;
    private int activityCooldown;
    private int afkUntil;
    private long nextMarketChatAt;

    public IdleCrowdBotScript(WorldTile anchor, int wanderRadius, String[] chatPool,
            BotPersonality personality) {
        this(anchor, wanderRadius, chatPool, randomRole(), personality);
    }

    public IdleCrowdBotScript(WorldTile anchor, int wanderRadius, String[] chatPool,
            Role role, BotPersonality personality) {
        this(anchor, wanderRadius, chatPool, role, personality, null);
    }

    public IdleCrowdBotScript(WorldTile anchor, int wanderRadius, String[] chatPool,
            Role role, BotPersonality personality, WorldTile focusTile) {
        this.personality = personality == null ? BotPersonality.random() : personality;
        this.anchor = anchor == null ? null : new WorldTile(anchor);
        this.focusTile = focusTile == null
                ? (this.anchor == null ? null : new WorldTile(this.anchor))
                : new WorldTile(focusTile);
        this.wanderRadius = Math.max(1, wanderRadius);
        this.chatPool = chatPool == null || chatPool.length == 0 ? CROWD_CHAT : chatPool;
        this.role = role == null ? Role.QUIET : role;
        this.mercherPhrase = this.role == Role.MERCHER ? generateMercherPhrase() : null;
    }

    public static Role randomRole() {
        int roll = ThreadLocalRandom.current().nextInt(100);
        if (roll < 15) return Role.AFK;       // statues - was 30
        if (roll < 45) return Role.QUIET;     // silent fidgety - was 35
        if (roll < 70) return Role.ACTIVE;    // moving around - was 15
        if (roll < 90) return Role.CHATTY;    // chatting + emoting - was 12
        return Role.MERCHER;                  // booth spammers - was 8
    }

    public static Role randomGeRole(boolean closeToRing) {
        int roll = ThreadLocalRandom.current().nextInt(100);
        if (closeToRing) {
            if (roll < 7) return Role.AFK;
            if (roll < 17) return Role.QUIET;
            if (roll < 29) return Role.BANKSTANDER;
            if (roll < 41) return Role.BUYER;
            if (roll < 53) return Role.SELLER;
            if (roll < 66) return Role.MERCHER;
            if (roll < 74) return Role.FLETCHER;
            if (roll < 82) return Role.CRAFTER;
            if (roll < 90) return Role.HERBLORE;
            if (roll < 96) return Role.FASHIONSCAPER;
            return Role.ALCHER;
        }
        if (roll < 14) return Role.AFK;
        if (roll < 36) return Role.QUIET;
        if (roll < 58) return Role.ACTIVE;
        if (roll < 73) return Role.CHATTY;
        if (roll < 83) return Role.PASSERBY;
        if (roll < 91) return Role.NEWCOMER;
        if (roll < 97) return Role.FASHIONSCAPER;
        return Role.MAXED_SHOWOFF;
    }

    private static String generateMercherPhrase() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        String item = MERCHER_ITEMS[r.nextInt(MERCHER_ITEMS.length)];
        switch (r.nextInt(5)) {
            case 0: return "buying " + item;
            case 1: return "selling " + item;
            case 2: return "wts " + item;
            case 3: return "wtb " + item + " any price";
            default:
                String item2 = MERCHER_ITEMS[r.nextInt(MERCHER_ITEMS.length)];
                return item.equals(item2) ? "wts " + item : "swap " + item + " for " + item2;
        }
    }

    public static String marketOfferLine(boolean buying, String item, String priceEach) {
        if (item == null || item.isEmpty()) {
            return buying ? pick(BUYER_LINES) : pick(SELLER_LINES);
        }
        ThreadLocalRandom r = ThreadLocalRandom.current();
        String price = priceEach == null || priceEach.isEmpty() ? "" : " " + priceEach + " ea";
        if (buying) {
            switch (r.nextInt(11)) {
                case 0: return "buying " + item + price;
                case 1: return "wtb " + item + price;
                case 2: return "need " + item + price;
                case 3: return "buying bulk " + item;
                case 4: return "anyone selling " + item + "?";
                case 5: return "paying for " + item + price;
                case 6: return "lf " + item + price;
                case 7: return "need noted " + item;
                case 8: return "buying stacks of " + item;
                case 9: return "who has " + item + "?";
                default: return pick(BUYER_LINES).replace("bulk?", item + "?");
            }
        }
        switch (r.nextInt(11)) {
            case 0: return "selling " + item + price;
            case 1: return "wts " + item + price;
            case 2: return "selling bulk " + item;
            case 3: return item + " for sale";
            case 4: return "need cash, selling " + item;
            case 5: return "taking offers on " + item;
            case 6: return "got " + item + price;
            case 7: return "bank sale " + item;
            case 8: return "offloading " + item;
            case 9: return "wts noted " + item;
            default: return pick(SELLER_LINES).replace("loot tab", item);
        }
    }

    @Override
    protected void onStart() {
        // Spread out initial cooldowns so a freshly-spawned crowd doesn't all chat
        // on the same tick.
        ThreadLocalRandom random = ThreadLocalRandom.current();
        chatCooldown = isMarketTradingRole() ? random.nextInt(5, 45) : random.nextInt(20, 220);
        if (isMarketTradingRole()) {
            scheduleNextMarketChat(random, MARKET_CHAT_FIRST_MIN_MS, MARKET_CHAT_FIRST_MAX_MS);
        }
        emoteCooldown = random.nextInt(80, 600);
        activityCooldown = random.nextInt(1, 30);
        // Randomise facing so the whole crowd doesn't stare north on spawn.
        if (isStationaryBankstandingRole() && focusTile != null) {
            api().faceTile(focusTile);
        } else {
            randomizeFacing(random);
        }
        delay(personality.reactionDelay(2, 8));
    }

    private void randomizeFacing(ThreadLocalRandom random) {
        int dx = random.nextInt(-3, 4);
        int dy = random.nextInt(-3, 4);
        if (dx == 0 && dy == 0) {
            dx = 1;
        }
        api().faceTile(bot().transform(dx, dy, 0));
    }

    /**
     * Subtle scanning motion - the bot turns to face a random nearby tile.
     * Free of pathfinding cost. Real players do this constantly while idle.
     */
    private void maybeLookAround(ThreadLocalRandom random, int chancePer100) {
        if (random.nextInt(100) < chancePer100) {
            randomizeFacing(random);
        }
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
        if (BotTrading.tickMeeting(bot())) {
            delay(2);
            return;
        }
        if (chatCooldown > 0) chatCooldown--;
        if (emoteCooldown > 0) emoteCooldown--;
        if (activityCooldown > 0) activityCooldown--;
        if (afkUntil > 0) afkUntil--;

        ThreadLocalRandom random = ThreadLocalRandom.current();

        switch (role) {
            case AFK:
                tickAfk(random);
                return;
            case QUIET:
                tickQuiet(random);
                return;
            case ACTIVE:
                tickActive(random);
                return;
            case CHATTY:
                tickChatty(random);
                return;
            case BANKSTANDER:
                tickBankstander(random);
                return;
            case BUYER:
            case SELLER:
                tickTrader(random);
                return;
            case MERCHER:
                tickMercher(random);
                return;
            case FLETCHER:
            case CRAFTER:
            case HERBLORE:
            case ALCHER:
                tickBankstandingSkill(random);
                return;
            case FASHIONSCAPER:
            case MAXED_SHOWOFF:
                tickFashionscaper(random);
                return;
            case NEWCOMER:
                tickNewcomer(random);
                return;
            case PASSERBY:
                tickPasserby(random);
                return;
            default:
                delay(8);
        }
    }

    private void tickAfk(ThreadLocalRandom random) {
        // Statues, but they still glance around so they don't look frozen.
        maybeLookAround(random, 8);
        if (emoteCooldown <= 0 && random.nextInt(500) == 0) {
            doEmote(866, random); // yawn
            return;
        }
        delay(random.nextInt(8, 18));
    }

    private void tickQuiet(ThreadLocalRandom random) {
        maybeLookAround(random, 14);
        if (afkUntil > 0) {
            delay(random.nextInt(6, 12));
            return;
        }
        // Occasional brief AFK pause.
        if (random.nextInt(500) == 0) {
            afkUntil = random.nextInt(30, 120);
            delay(random.nextInt(4, 8));
            return;
        }
        // Light fidget, no chat. Bumped from 3% to 7%.
        if (!bot().hasWalkSteps() && random.nextInt(100) < 7) {
            api().walkNear(anchorOrSpawn(), Math.min(QUIET_WANDER_LIMIT, wanderRadius));
            delay(random.nextInt(5, 12));
            return;
        }
        delay(random.nextInt(6, 14));
    }

    private void tickActive(ThreadLocalRandom random) {
        maybeLookAround(random, 18);
        // ~1/100 × ~35% chattiness ≈ a chat every ~3 min per active bot.
        if (chatCooldown <= 0 && random.nextInt(100) == 0 && personality.rollChat()) {
            speak(pickChatLine(random), random);
            return;
        }
        if (emoteCooldown <= 0 && random.nextInt(400) == 0) {
            doEmote(EMOTE_ANIMS[random.nextInt(EMOTE_ANIMS.length)], random);
            return;
        }
        // Active bots move more visibly. Bumped from 7% to 14%.
        if (!bot().hasWalkSteps() && random.nextInt(100) < 14) {
            api().walkNear(anchorOrSpawn(), Math.min(ACTIVE_WANDER_LIMIT, wanderRadius));
            delay(random.nextInt(4, 9));
            return;
        }
        delay(random.nextInt(5, 11));
    }

    private void tickChatty(ThreadLocalRandom random) {
        maybeLookAround(random, 22);
        boolean wave = isConversationActive();
        int baseChatPer1000 = wave ? 60 : 15;
        if (chatCooldown <= 0 && random.nextInt(1000) < baseChatPer1000 && personality.rollChat()) {
            speak(pickChatLine(random), random);
            return;
        }
        if (emoteCooldown <= 0 && random.nextInt(250) == 0) {
            doEmote(EMOTE_ANIMS[random.nextInt(EMOTE_ANIMS.length)], random);
            return;
        }
        // Bumped from 10% to 18%.
        if (!bot().hasWalkSteps() && random.nextInt(100) < 18) {
            api().walkNear(anchorOrSpawn(), wanderRadius);
            delay(random.nextInt(3, 8));
            return;
        }
        delay(random.nextInt(4, 10));
    }

    private void tickBankstander(ThreadLocalRandom random) {
        faceFocus(random, 35);
        // ~1/100 × ~35% chattiness ≈ a chat every ~3 min per bankstander.
        if (chatCooldown <= 0 && random.nextInt(100) == 0 && personality.rollChat()) {
            speak(pick(BANK_LINES), random);
            return;
        }
        if (activityCooldown <= 0 && random.nextInt(100) < 35) {
            int anim = random.nextBoolean() ? 857 : 856; // think or no
            bot().setNextAnimation(new Animation(anim));
            activityCooldown = random.nextInt(18, 45);
            delay(random.nextInt(3, 7));
            return;
        }
        if (!bot().hasWalkSteps() && random.nextInt(100) < 4) {
            api().walkNear(anchorOrSpawn(), Math.min(2, wanderRadius));
            delay(random.nextInt(5, 10));
            return;
        }
        delay(random.nextInt(4, 9));
    }

    private void tickTrader(ThreadLocalRandom random) {
        faceFocus(random, 45);
        if (isMarketChatDue()) {
            speakMarketOffer(random);
            return;
        }
        if (emoteCooldown <= 0 && random.nextInt(260) == 0) {
            doEmote(random.nextBoolean() ? 857 : 860, random); // think or wave
            return;
        }
        if (!bot().hasWalkSteps() && random.nextInt(100) < 5) {
            api().walkNear(anchorOrSpawn(), Math.min(2, wanderRadius));
            delay(random.nextInt(5, 10));
            return;
        }
        delay(random.nextInt(4, 10));
    }

    private void tickMercher(ThreadLocalRandom random) {
        // Stationary but turns to scan the crowd, cheers when shouting offers.
        faceFocus(random, 30);
        maybeLookAround(random, 8);
        if (isMarketChatDue()) {
            speakMarketOffer(random);
            if (emoteCooldown <= 0 && random.nextInt(4) == 0) {
                int anim = random.nextBoolean() ? 862 : 860; // cheer or wave
                doEmote(anim, random);
                return;
            }
        }
        delay(random.nextInt(5, 10));
    }

    private void tickBankstandingSkill(ThreadLocalRandom random) {
        if (focusTile != null && random.nextInt(100) < 35) {
            api().faceTile(focusTile);
        } else {
            maybeLookAround(random, 8);
        }
        // ~1/100 × ~35% chattiness ≈ a chat every ~3 min per skilling bankstander.
        if (chatCooldown <= 0 && random.nextInt(100) == 0 && personality.rollChat()) {
            speak(pick(SKILLING_LINES), random);
            return;
        }
        if (activityCooldown <= 0) {
            doBankstandingAnimation(random);
            return;
        }
        delay(random.nextInt(2, 6));
    }

    private void doBankstandingAnimation(ThreadLocalRandom random) {
        switch (role) {
            case FLETCHER:
                bot().setNextAnimation(new Animation(FLETCHING_ANIMS[random.nextInt(FLETCHING_ANIMS.length)]));
                activityCooldown = random.nextInt(2, 5);
                delay(random.nextInt(2, 4));
                return;
            case CRAFTER:
                bot().setNextAnimation(new Animation(CRAFTING_ANIMS[random.nextInt(CRAFTING_ANIMS.length)]));
                activityCooldown = random.nextInt(3, 6);
                delay(random.nextInt(2, 5));
                return;
            case HERBLORE:
                bot().setNextAnimation(new Animation(random.nextBoolean() ? 363 : 364));
                activityCooldown = random.nextInt(2, 5);
                delay(random.nextInt(2, 4));
                return;
            case ALCHER:
                bot().setNextAnimation(new Animation(713));
                bot().setNextGraphics(new Graphics(113));
                activityCooldown = random.nextInt(5, 9);
                delay(random.nextInt(4, 7));
                return;
            default:
                activityCooldown = random.nextInt(4, 8);
                delay(random.nextInt(3, 6));
        }
    }

    private void tickFashionscaper(ThreadLocalRandom random) {
        if (focusTile != null && random.nextInt(100) < 22) {
            api().faceTile(focusTile);
        } else {
            maybeLookAround(random, 18);
        }
        boolean wave = isConversationActive();
        int chatChance = role == Role.MAXED_SHOWOFF ? 10 : 22;
        if (wave) {
            chatChance += 28;
        }
        if (chatCooldown <= 0 && random.nextInt(1000) < chatChance && personality.rollChat()) {
            speak(pick(FASHION_LINES), random);
            return;
        }
        if (emoteCooldown <= 0 && random.nextInt(role == Role.MAXED_SHOWOFF ? 140 : 170) == 0) {
            int[] flexEmotes = {862, 864, 865, 7071, 860};
            doEmote(flexEmotes[random.nextInt(flexEmotes.length)], random);
            return;
        }
        if (!bot().hasWalkSteps() && random.nextInt(100) < 8) {
            api().walkNear(anchorOrSpawn(), Math.min(4, wanderRadius));
            delay(random.nextInt(4, 9));
            return;
        }
        delay(random.nextInt(4, 10));
    }

    private void tickNewcomer(ThreadLocalRandom random) {
        maybeLookAround(random, 28);
        if (isMarketChatDue()) {
            speakMarketOffer(random);
            return;
        }
        if (chatCooldown <= 0 && random.nextInt(1000) < 16 && personality.rollChat()) {
            speak(pick(NEWCOMER_LINES), random);
            return;
        }
        if (!bot().hasWalkSteps() && random.nextInt(100) < 16) {
            api().walkNear(anchorOrSpawn(), Math.min(5, wanderRadius));
            delay(random.nextInt(4, 10));
            return;
        }
        delay(random.nextInt(4, 10));
    }

    private void tickPasserby(ThreadLocalRandom random) {
        if (chatCooldown <= 0 && random.nextInt(1000) < 10 && personality.rollChat()) {
            speak(pick(PASSERBY_LINES), random);
            return;
        }
        if (!bot().hasWalkSteps() && random.nextInt(100) < 34) {
            api().walkNear(anchorOrSpawn(), wanderRadius);
            delay(random.nextInt(3, 7));
            return;
        }
        maybeLookAround(random, 10);
        delay(random.nextInt(3, 7));
    }

    private String pickChatLine(ThreadLocalRandom random) {
        return chatPool[random.nextInt(chatPool.length)];
    }

    private static String pick(String[] pool) {
        if (pool == null || pool.length == 0) {
            return "";
        }
        return pool[ThreadLocalRandom.current().nextInt(pool.length)];
    }

    private void faceFocus(ThreadLocalRandom random, int chancePer100) {
        if (focusTile != null && random.nextInt(100) < chancePer100) {
            api().faceTile(focusTile);
        }
    }

    private void speak(String phrase, ThreadLocalRandom random) {
        bot().sendPublicChatMessage(new PublicChatMessage(phrase, 0));
        api().forceTalk(phrase);
        chatCooldown = random.nextInt(150, 350);
        startConversationWave();
        BotConversation.onUtterance(bot(), this, phrase, 0);
        delay(personality.reactionDelay(2, 5));
    }

    private void speakMarketOffer(ThreadLocalRandom random) {
        boolean preferBuying = random.nextBoolean();
        String marketLine = BotTrading.getTradeChatter(bot(), preferBuying);
        marketSpeak(marketLine == null ? pick(preferBuying ? BUYER_LINES : SELLER_LINES) : marketLine, random);
    }

    private void marketSpeak(String phrase, ThreadLocalRandom random) {
        if (phrase == null || phrase.isEmpty()) {
            return;
        }
        // sendPublicChatMessage only writes to nearby players' chat boxes — it never
        // sets a chat mask on the sender, so no overhead bubble appears. forceTalk
        // sets the entity update mask that produces the visible chat bubble above
        // the bot. We do both so the chat shows up in both places.
        bot().sendPublicChatMessage(new PublicChatMessage(phrase, BotTrading.randomMarketChatEffect()));
        api().forceTalk(phrase);
        chatCooldown = random.nextInt(2, 6);
        scheduleNextMarketChat(random, MARKET_CHAT_INTERVAL_MIN_MS, MARKET_CHAT_INTERVAL_MAX_MS);
        startConversationWave();
        BotConversation.onUtterance(bot(), this, phrase, 0);
        delay(personality.reactionDelay(2, 5));
    }

    /**
     * Speak as a reply scheduled by {@link BotConversation}. Runs outside the
     * bot's onTick so it must not call {@link #delay}; it just sets the chat
     * state and feeds the utterance back into the conversation engine so a
     * second-level reply can chain (subject to the engine's depth cap).
     */
    void speakReply(String phrase, int depth, int cooldownMin, int cooldownMax) {
        speakReply(phrase, depth, null, cooldownMin, cooldownMax);
    }

    void speakReply(String phrase, int depth, BotPlayer replyTarget, int cooldownMin, int cooldownMax) {
        if (phrase == null || phrase.isEmpty() || bot() == null
                || bot().hasFinished() || bot().isDead() || bot().isLocked()) {
            return;
        }
        faceConversationTarget(replyTarget);
        bot().sendPublicChatMessage(new PublicChatMessage(phrase, 0));
        api().forceTalk(phrase);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        maybeDoReplyEmote(random);
        int cooldown = cooldownMax > cooldownMin
                ? random.nextInt(cooldownMin, cooldownMax + 1)
                : Math.max(1, cooldownMin);
        if (cooldown > chatCooldown) {
            chatCooldown = cooldown;
        }
        startConversationWave();
        BotConversation.onUtterance(bot(), this, phrase, depth);
    }

    private void faceConversationTarget(BotPlayer target) {
        if (target == null || target.hasFinished() || target.isDead()
                || target.getPlane() != bot().getPlane()) {
            return;
        }
        bot().setNextFaceEntity(target);
        target.setNextFaceEntity(bot());
    }

    private void maybeDoReplyEmote(ThreadLocalRandom random) {
        if (emoteCooldown > 0
                || random.nextInt(100) >= BotConversation.emoteWithReplyPercent()) {
            return;
        }
        int[] anims = BotConversation.replyEmoteAnims();
        if (anims == null || anims.length == 0) {
            return;
        }
        bot().setNextAnimation(new Animation(anims[random.nextInt(anims.length)]));
        emoteCooldown = random.nextInt(120, 320);
    }

    public Role getRole() {
        return role;
    }

    private void doEmote(int animationId, ThreadLocalRandom random) {
        bot().setNextAnimation(new Animation(animationId));
        emoteCooldown = random.nextInt(300, 800);
        delay(personality.reactionDelay(2, 5));
    }

    private WorldTile anchorOrSpawn() {
        return anchor == null ? bot().getSpawnTile() : anchor;
    }

    private boolean isStationaryBankstandingRole() {
        return role == Role.BANKSTANDER || role == Role.BUYER || role == Role.SELLER
                || role == Role.MERCHER || role == Role.FLETCHER || role == Role.CRAFTER
                || role == Role.HERBLORE || role == Role.ALCHER;
    }

    private boolean isMarketTradingRole() {
        return role == Role.BUYER || role == Role.SELLER || role == Role.MERCHER
                || role == Role.NEWCOMER;
    }

    private boolean isMarketChatDue() {
        return isMarketTradingRole() && nextMarketChatAt > 0L
                && System.currentTimeMillis() >= nextMarketChatAt;
    }

    private void scheduleNextMarketChat(ThreadLocalRandom random, long minMillis, long maxMillis) {
        long min = Math.max(1L, minMillis);
        long max = Math.max(min, maxMillis);
        nextMarketChatAt = System.currentTimeMillis() + random.nextLong(min, max + 1L);
    }

    private void startConversationWave() {
        if (anchor != null) {
            CONVERSATION_WAVES.put(anchorKey(anchor), System.currentTimeMillis() + WAVE_DURATION_MS);
        }
    }

    private boolean isConversationActive() {
        if (anchor == null) return false;
        Long until = CONVERSATION_WAVES.get(anchorKey(anchor));
        return until != null && until > System.currentTimeMillis();
    }

    private static String anchorKey(WorldTile t) {
        return t.getX() + "," + t.getY() + ":" + t.getPlane();
    }

    @Override
    public String getDebugInfo() {
        String anchorStr = anchor == null
                ? "spawn"
                : anchor.getX() + "," + anchor.getY();
        return "idle-crowd role=" + role
                + " arch=" + personality.getArchetype()
                + " spec=" + BotTrading.getTraderSpecialty(bot())
                + " anchor=" + anchorStr
                + " radius=" + wanderRadius
                + " chatCd=" + chatCooldown
                + (mercherPhrase == null ? "" : " merch=\"" + mercherPhrase + "\"");
    }
}
