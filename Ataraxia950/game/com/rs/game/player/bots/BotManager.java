package com.rs.game.player.bots;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activites.creations.StealingCreation;
import com.rs.game.activites.soulwars.SoulWarsManager.Teams;
import com.rs.game.player.Player;
import com.rs.game.player.bots.trading.BotTrading;
import com.rs.game.player.bots.lumbridge.LumbridgeIronmanBotScript;
import com.rs.game.player.bots.lumbridge.LumbridgeIronmanData;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public final class BotManager {

    private static final Map<String, BotContext> BOTS = new ConcurrentHashMap<>();
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);
    private static final int GE_WANDER_RADIUS_BONUS = 5;
    private static final int GE_CLOSE_CROWD_PERCENT = 50;
    private static final int GE_HEATMAP_CANDIDATES = 48;
    private static final int RANDOM_NAME_ATTEMPTS = 128;
    private static final int MAX_USERNAME_LENGTH = BotName.MAX_NAME_LENGTH;
    private static WorldTask pulseTask;

    private BotManager() {
    }

    public static BotPlayer spawnIdleBot(String requestedName, WorldTile tile) {
        return spawnBot(requestedName, tile, new IdleBotScript());
    }

    public static BotPlayer spawnStealingCreationBot(String requestedName, boolean inRedTeam) {
        return spawnStealingCreationBot(requestedName, inRedTeam, StealingCreationBotScript.Role.HYBRID);
    }

    public static BotPlayer spawnStealingCreationBot(String requestedName, boolean inRedTeam,
            StealingCreationBotScript.Role role) {
        return spawnStealingCreationBot(requestedName, inRedTeam, role, BotPersonality.random());
    }

    public static BotPlayer spawnStealingCreationBot(String requestedName, boolean inRedTeam,
            StealingCreationBotScript.Role role, BotPersonality personality) {
        return spawnBot(requestedName, StealingCreation.getLobbyTile(inRedTeam),
                new StealingCreationBotScript(inRedTeam, role, personality));
    }

    public static BotPlayer spawnStealingCreationBot(String requestedName, boolean inRedTeam,
            StealingCreationBotScript.Role role, BotPersonality.Archetype archetype) {
        return spawnStealingCreationBot(requestedName, inRedTeam, role,
                archetype == null ? BotPersonality.random() : BotPersonality.forArchetype(archetype));
    }

    public static BotPlayer spawnSoulWarsBot(String requestedName, Teams team) {
        return spawnSoulWarsBot(requestedName, team, SoulWarsBotScript.Role.HYBRID);
    }

    public static BotPlayer spawnSoulWarsBot(String requestedName, Teams team, SoulWarsBotScript.Role role) {
        return spawnSoulWarsBot(requestedName, team, role, BotPersonality.random());
    }

    public static BotPlayer spawnSoulWarsBot(String requestedName, Teams team, SoulWarsBotScript.Role role,
            BotPersonality personality) {
        Teams safeTeam = team == null ? Teams.RED : team;
        return spawnBot(requestedName, World.soulWars.getLobbyTile(safeTeam),
                new SoulWarsBotScript(safeTeam, role, personality));
    }

    public static BotPlayer spawnSoulWarsBot(String requestedName, Teams team, SoulWarsBotScript.Role role,
            BotPersonality.Archetype archetype) {
        return spawnSoulWarsBot(requestedName, team, role,
                archetype == null ? BotPersonality.random() : BotPersonality.forArchetype(archetype));
    }

    public static BotPlayer spawnLumbridgeIronmanBot(String requestedName, WorldTile tile) {
        return spawnLumbridgeIronmanBot(requestedName, tile, null);
    }

    public static BotPlayer spawnLumbridgeIronmanBot(String requestedName, WorldTile tile,
            BotPersonality.Archetype archetype) {
        WorldTile spawnTile = tile == null ? LumbridgeIronmanData.LUMBRIDGE_SPAWN : tile;
        BotPersonality personality = archetype == null
                ? BotPersonality.random()
                : BotPersonality.forArchetype(archetype);
        return spawnBot(requestedName, spawnTile, new LumbridgeIronmanBotScript(personality));
    }

    public static int spawnLumbridgeIronmanBots(int amount, WorldTile center, int radius) {
        int spawned = 0;
        WorldTile spawnCenter = center == null ? LumbridgeIronmanData.LUMBRIDGE_SPAWN : center;
        int spread = Math.max(0, radius);
        for (int index = 0; index < amount; index++) {
            int dx = spread == 0 ? 0 : ThreadLocalRandom.current().nextInt(-spread, spread + 1);
            int dy = spread == 0 ? 0 : ThreadLocalRandom.current().nextInt(-spread, spread + 1);
            spawnLumbridgeIronmanBot(null, spawnCenter.transform(dx, dy, 0));
            spawned++;
        }
        return spawned;
    }

    public static int despawnLumbridgeIronmanBots() {
        int count = 0;
        for (BotContext context : BOTS.values()) {
            if (!(context.script instanceof LumbridgeIronmanBotScript)) {
                continue;
            }
            destroyContext(context);
            count++;
        }
        return count;
    }

    public static BotPlayer spawnWandererBot(String requestedName, WorldTile tile) {
        return spawnWandererBot(requestedName, tile, null);
    }

    public static BotPlayer spawnWandererBot(String requestedName, WorldTile tile,
            BotPersonality.Archetype archetype) {
        WorldTile spawnTile = tile == null ? BotLocations.randomCity() : tile;
        BotPersonality personality = archetype == null
                ? BotPersonality.random()
                : BotPersonality.forArchetype(archetype);
        BotProfileGenerator.BotProfile profile = BotProfileGenerator.random();
        // Use the generated name if the caller didn't specify one - gives wanderers
        // varied real-looking handles instead of the canned RANDOM_PLAYER_NAMES list.
        String spawnName = requestedName != null && !requestedName.trim().isEmpty()
                ? requestedName
                : profile.getDisplayName();
        BotPlayer bot = spawnBot(spawnName, spawnTile, new WandererBotScript(personality));
        profile.apply(bot);
        return bot;
    }

    public static int spawnWandererBots(int amount) {
        int spawned = 0;
        for (int index = 0; index < amount; index++) {
            spawnWandererBot(null, BotLocations.randomCity());
            spawned++;
        }
        return spawned;
    }

    public static int despawnWandererBots() {
        int count = 0;
        for (BotContext context : BOTS.values()) {
            if (!(context.script instanceof WandererBotScript)) {
                continue;
            }
            destroyContext(context);
            count++;
        }
        return count;
    }

    /**
     * Populates the Grand Exchange with bots distributed in a way that
     * approximates a real GE crowd:
     *
     *  - 50% stand very close to the full inner booth ring.
     *  - 50% thin outward from that ring over the next ten tiles.
     *  - Bankstanding roles fletch, craft, mix potions, and alch near tellers.
     *
     * Each bot is given a randomised profile (name, stats, gear) so the crowd
     * looks varied rather than all the same combat level.
     */
    public static int spawnGECrowd(int amount) {
        int spawned = 0;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<WorldTile> reservedTiles = new ArrayList<>();
        int closeRemaining = Math.max(0, (amount * GE_CLOSE_CROWD_PERCENT + 99) / 100);
        int outerRemaining = Math.max(0, amount - closeRemaining);
        for (int i = 0; i < amount; i++) {
            boolean closeToRing = shouldUseCloseGeSlot(closeRemaining, outerRemaining, random);
            if (closeToRing) {
                closeRemaining--;
            } else {
                outerRemaining--;
            }
            GeCrowdZone zone = pickGeZone(closeToRing, random);
            IdleCrowdBotScript.Role role = pickGeRole(zone, random);
            GeCrowdSpot spot = sampleGeCrowdSpot(zone, role, random, reservedTiles);
            reservedTiles.add(spot.tile);
            int wanderRadius = pickGeWanderRadius(zone, role, random);
            spawnIdleCrowdBot(spot.anchor, spot.tile, wanderRadius, IdleCrowdBotScript.GE_CHAT,
                    role, spot.focusTile);
            spawned++;
        }
        return spawned;
    }

    /**
     * Spawns one idle-crowd bot at {@code spawnTile}, anchored to {@code anchor}
     * (the centre of the crowd it shuffles around). Applies a randomised profile.
     * Uses a randomly rolled role.
     */
    public static BotPlayer spawnIdleCrowdBot(WorldTile anchor, WorldTile spawnTile,
            int wanderRadius, String[] chatPool) {
        return spawnIdleCrowdBot(anchor, spawnTile, wanderRadius, chatPool,
                IdleCrowdBotScript.randomRole());
    }

    /** Same as above but lets the caller pin the bot's role explicitly. */
    public static BotPlayer spawnIdleCrowdBot(WorldTile anchor, WorldTile spawnTile,
            int wanderRadius, String[] chatPool, IdleCrowdBotScript.Role role) {
        return spawnIdleCrowdBot(anchor, spawnTile, wanderRadius, chatPool, role, null);
    }

    public static BotPlayer spawnIdleCrowdBot(WorldTile anchor, WorldTile spawnTile,
            int wanderRadius, String[] chatPool, IdleCrowdBotScript.Role role, WorldTile focusTile) {
        BotPersonality personality = BotPersonality.random();
        BotProfileGenerator.BotProfile profile = rollCrowdProfile(role);
        IdleCrowdBotScript script = new IdleCrowdBotScript(
                anchor, wanderRadius, chatPool, role, personality, focusTile);
        BotPlayer bot = spawnBot(profile.getDisplayName(), spawnTile, script);
        profile.apply(bot);
        BotTrading.register(bot, role);
        return bot;
    }

    public static int despawnIdleCrowdBots() {
        int count = 0;
        for (BotContext context : BOTS.values()) {
            if (!(context.script instanceof IdleCrowdBotScript)) {
                continue;
            }
            destroyContext(context);
            count++;
        }
        return count;
    }

    private static boolean shouldUseCloseGeSlot(int closeRemaining, int outerRemaining,
            ThreadLocalRandom random) {
        if (closeRemaining <= 0) {
            return false;
        }
        if (outerRemaining <= 0) {
            return true;
        }
        return random.nextInt(closeRemaining + outerRemaining) < closeRemaining;
    }

    private static BotProfileGenerator.BotProfile rollCrowdProfile(IdleCrowdBotScript.Role role) {
        if (role == IdleCrowdBotScript.Role.MAXED_SHOWOFF) {
            return BotProfileGenerator.forTier(BotProfileGenerator.Tier.MAXED,
                    BotProfileGenerator.OutfitPreference.MAXED_SHOWOFF);
        }
        if (role == IdleCrowdBotScript.Role.FASHIONSCAPER) {
            return BotProfileGenerator.random(BotProfileGenerator.OutfitPreference.FASHIONSCAPE);
        }
        if (role == IdleCrowdBotScript.Role.NEWCOMER) {
            return BotProfileGenerator.forTier(BotProfileGenerator.Tier.NEWBIE,
                    BotProfileGenerator.OutfitPreference.NEWBIE);
        }
        if (role == IdleCrowdBotScript.Role.BANKSTANDER
                || role == IdleCrowdBotScript.Role.FLETCHER
                || role == IdleCrowdBotScript.Role.CRAFTER
                || role == IdleCrowdBotScript.Role.HERBLORE
                || role == IdleCrowdBotScript.Role.ALCHER) {
            return BotProfileGenerator.random(BotProfileGenerator.OutfitPreference.BANKSTANDER);
        }
        if (role == IdleCrowdBotScript.Role.BUYER
                || role == IdleCrowdBotScript.Role.SELLER
                || role == IdleCrowdBotScript.Role.MERCHER) {
            return BotProfileGenerator.random(BotProfileGenerator.OutfitPreference.TRADER);
        }
        return BotProfileGenerator.random();
    }

    private static GeCrowdZone pickGeZone(boolean closeToRing, ThreadLocalRandom random) {
        int totalWeight = 0;
        for (GeCrowdZone zone : GeCrowdZone.values()) {
            if (zone.closeToRing == closeToRing) {
                totalWeight += zone.weight;
            }
        }
        int roll = random.nextInt(Math.max(1, totalWeight));
        for (GeCrowdZone zone : GeCrowdZone.values()) {
            if (zone.closeToRing != closeToRing) {
                continue;
            }
            roll -= zone.weight;
            if (roll < 0) {
                return zone;
            }
        }
        return closeToRing ? GeCrowdZone.BOOTH_RING : GeCrowdZone.OUTER_SPILLOVER;
    }

    private static IdleCrowdBotScript.Role pickGeRole(GeCrowdZone zone, ThreadLocalRandom random) {
        int roll = random.nextInt(100);
        switch (zone) {
            case BOOTH_RING:
                if (roll < 24) return IdleCrowdBotScript.Role.BANKSTANDER;
                if (roll < 40) return IdleCrowdBotScript.Role.ACTIVE;
                if (roll < 56) return IdleCrowdBotScript.Role.QUIET;
                if (roll < 70) return IdleCrowdBotScript.Role.CHATTY;
                if (roll < 86) return IdleCrowdBotScript.Role.FASHIONSCAPER;
                return IdleCrowdBotScript.Role.MAXED_SHOWOFF;
            case BANK_SKILLER:
                if (roll < 18) return IdleCrowdBotScript.Role.FLETCHER;
                if (roll < 36) return IdleCrowdBotScript.Role.CRAFTER;
                if (roll < 54) return IdleCrowdBotScript.Role.HERBLORE;
                if (roll < 72) return IdleCrowdBotScript.Role.ALCHER;
                if (roll < 90) return IdleCrowdBotScript.Role.BANKSTANDER;
                if (roll < 96) return IdleCrowdBotScript.Role.ACTIVE;
                return IdleCrowdBotScript.Role.QUIET;
            case SOCIAL_INNER:
                if (roll < 25) return IdleCrowdBotScript.Role.CHATTY;
                if (roll < 48) return IdleCrowdBotScript.Role.FASHIONSCAPER;
                if (roll < 63) return IdleCrowdBotScript.Role.MAXED_SHOWOFF;
                if (roll < 78) return IdleCrowdBotScript.Role.ACTIVE;
                if (roll < 88) return IdleCrowdBotScript.Role.BANKSTANDER;
                return IdleCrowdBotScript.Role.QUIET;
            case SOCIAL_OUTER:
                if (roll < 12) return IdleCrowdBotScript.Role.CHATTY;
                if (roll < 34) return IdleCrowdBotScript.Role.BUYER;
                if (roll < 56) return IdleCrowdBotScript.Role.SELLER;
                if (roll < 72) return IdleCrowdBotScript.Role.MERCHER;
                if (roll < 80) return IdleCrowdBotScript.Role.NEWCOMER;
                if (roll < 88) return IdleCrowdBotScript.Role.FASHIONSCAPER;
                if (roll < 97) return IdleCrowdBotScript.Role.ACTIVE;
                return IdleCrowdBotScript.Role.MAXED_SHOWOFF;
            case WALK_LANE:
                if (roll < 42) return IdleCrowdBotScript.Role.PASSERBY;
                if (roll < 66) return IdleCrowdBotScript.Role.ACTIVE;
                if (roll < 84) return IdleCrowdBotScript.Role.NEWCOMER;
                if (roll < 92) return IdleCrowdBotScript.Role.CHATTY;
                return IdleCrowdBotScript.Role.QUIET;
            case EDGE_IDLE:
                if (roll < 14) return IdleCrowdBotScript.Role.NEWCOMER;
                if (roll < 57) return IdleCrowdBotScript.Role.QUIET;
                if (roll < 73) return IdleCrowdBotScript.Role.AFK;
                if (roll < 87) return IdleCrowdBotScript.Role.ACTIVE;
                if (roll < 94) return IdleCrowdBotScript.Role.PASSERBY;
                return IdleCrowdBotScript.Role.CHATTY;
            case OUTER_SPILLOVER:
            default:
                if (roll < 26) return IdleCrowdBotScript.Role.BUYER;
                if (roll < 52) return IdleCrowdBotScript.Role.SELLER;
                if (roll < 70) return IdleCrowdBotScript.Role.MERCHER;
                if (roll < 75) return IdleCrowdBotScript.Role.NEWCOMER;
                if (roll < 84) return IdleCrowdBotScript.Role.ACTIVE;
                if (roll < 91) return IdleCrowdBotScript.Role.QUIET;
                if (roll < 96) return IdleCrowdBotScript.Role.CHATTY;
                return IdleCrowdBotScript.Role.PASSERBY;
        }
    }

    private static int pickGeWanderRadius(GeCrowdZone zone, IdleCrowdBotScript.Role role,
            ThreadLocalRandom random) {
        switch (role) {
            case FLETCHER:
            case CRAFTER:
            case HERBLORE:
            case ALCHER:
            case BANKSTANDER:
            case BUYER:
            case SELLER:
            case MERCHER:
                return Math.max(isMarketTradingRole(role) ? 2 : 1, zone.roamRadius);
            case PASSERBY:
                return zone.roamRadius + GE_WANDER_RADIUS_BONUS + random.nextInt(1, 4);
            case NEWCOMER:
                return zone.roamRadius + 2;
            case FASHIONSCAPER:
            case MAXED_SHOWOFF:
                return Math.max(2, zone.roamRadius + random.nextInt(0, 3));
            default:
                return zone.roamRadius + (zone.closeToRing ? 0 : GE_WANDER_RADIUS_BONUS);
        }
    }

    private static GeCrowdSpot sampleGeCrowdSpot(GeCrowdZone zone,
            IdleCrowdBotScript.Role role, ThreadLocalRandom random, List<WorldTile> reservedTiles) {
        GeCrowdCandidate best = null;
        for (int attempt = 0; attempt < GE_HEATMAP_CANDIDATES; attempt++) {
            GeRingPoint ringPoint = sampleGeRingPoint(random);
            int distance = sampleGeZoneDistance(zone, random);
            if (isMarketTradingRole(role)) {
                distance = Math.max(distance, BotLocations.GE_RING_CLOSE_DISTANCE + 3);
            }
            int tangentJitter = sampleGeTangentJitter(zone, distance, random);
            WorldTile tile = ringPoint.project(distance, tangentJitter);
            if (!isGeSpawnTileUsable(tile, reservedTiles)) {
                continue;
            }
            double score = scoreGeTile(tile, zone, role, distance, reservedTiles, random);
            if (best == null || score > best.score) {
                WorldTile focusTile = focusFor(zone, ringPoint, tile);
                best = new GeCrowdCandidate(tile, anchorFor(zone, ringPoint, tile, focusTile),
                        focusTile, score);
            }
        }
        if (best != null) {
            return new GeCrowdSpot(best.tile, best.anchor, best.focusTile);
        }
        WorldTile fallback = pickGeFallbackTile(reservedTiles);
        return new GeCrowdSpot(fallback, fallback, BotLocations.GE_CENTER);
    }

    private static WorldTile pickGeFallbackTile(List<WorldTile> reservedTiles) {
        int distance = BotLocations.GE_RING_OUTER_DISTANCE + 3;
        WorldTile[] fallbacks = {
                BotLocations.GE_CENTER.transform(0, distance, 0),
                BotLocations.GE_CENTER.transform(distance, 0, 0),
                BotLocations.GE_CENTER.transform(0, -distance, 0),
                BotLocations.GE_CENTER.transform(-distance, 0, 0)
        };
        for (WorldTile tile : fallbacks) {
            if (isGeSpawnTileUsable(tile, reservedTiles)) {
                return tile;
            }
        }
        return fallbacks[0];
    }

    private static boolean isMarketTradingRole(IdleCrowdBotScript.Role role) {
        return role == IdleCrowdBotScript.Role.BUYER
                || role == IdleCrowdBotScript.Role.SELLER
                || role == IdleCrowdBotScript.Role.MERCHER
                || role == IdleCrowdBotScript.Role.NEWCOMER;
    }

    private static GeRingPoint sampleGeRingPoint(ThreadLocalRandom random) {
        WorldTile[] ring = BotLocations.GE_INNER_RING;
        double totalLength = 0;
        for (int i = 0; i < ring.length; i++) {
            WorldTile start = ring[i];
            WorldTile end = ring[(i + 1) % ring.length];
            double edgeX = end.getX() - start.getX();
            double edgeY = end.getY() - start.getY();
            totalLength += Math.sqrt(edgeX * edgeX + edgeY * edgeY);
        }
        double roll = totalLength <= 0 ? 0 : random.nextDouble(totalLength);
        int index = ring.length - 1;
        double edgeLength = 0;
        for (int i = 0; i < ring.length; i++) {
            WorldTile start = ring[i];
            WorldTile end = ring[(i + 1) % ring.length];
            double edgeX = end.getX() - start.getX();
            double edgeY = end.getY() - start.getY();
            edgeLength = Math.sqrt(edgeX * edgeX + edgeY * edgeY);
            if (roll <= edgeLength) {
                index = i;
                break;
            }
            roll -= edgeLength;
        }
        WorldTile start = ring[index];
        WorldTile end = ring[(index + 1) % ring.length];
        double edgeX = end.getX() - start.getX();
        double edgeY = end.getY() - start.getY();
        edgeLength = Math.sqrt(edgeX * edgeX + edgeY * edgeY);
        double t = edgeLength <= 0 ? 0 : random.nextDouble();
        double x = start.getX() + edgeX * t;
        double y = start.getY() + edgeY * t;
        double tangentX = edgeLength == 0 ? 1.0 : edgeX / edgeLength;
        double tangentY = edgeLength == 0 ? 0.0 : edgeY / edgeLength;
        double outwardX = x - BotLocations.GE_CENTER.getX();
        double outwardY = y - BotLocations.GE_CENTER.getY();
        double outwardLength = Math.sqrt(outwardX * outwardX + outwardY * outwardY);
        if (outwardLength == 0) {
            outwardX = -tangentY;
            outwardY = tangentX;
            outwardLength = 1;
        }
        return new GeRingPoint(x, y, outwardX / outwardLength, outwardY / outwardLength,
                tangentX, tangentY, BotLocations.GE_CENTER.getPlane());
    }

    private static int sampleGeZoneDistance(GeCrowdZone zone, ThreadLocalRandom random) {
        int range = Math.max(0, zone.maxDistance - zone.minDistance);
        if (range == 0) {
            return zone.minDistance;
        }
        if (zone == GeCrowdZone.EDGE_IDLE) {
            return zone.minDistance + (int) Math.floor(Math.sqrt(random.nextDouble()) * range);
        }
        if (zone == GeCrowdZone.WALK_LANE || zone.closeToRing) {
            return zone.minDistance + random.nextInt(range + 1);
        }
        double falloff = 1.0 - Math.sqrt(random.nextDouble());
        return Math.min(zone.maxDistance, zone.minDistance + (int) Math.floor(falloff * (range + 1)));
    }

    private static int sampleGeTangentJitter(GeCrowdZone zone, int distance, ThreadLocalRandom random) {
        int maxJitter = Math.max(zone.minTangentJitter,
                Math.min(zone.maxTangentJitter, Math.max(1, distance / 2)));
        return random.nextInt(-maxJitter, maxJitter + 1);
    }

    private static WorldTile focusFor(GeCrowdZone zone, GeRingPoint ringPoint, WorldTile tile) {
        if (zone == GeCrowdZone.SOCIAL_INNER || zone == GeCrowdZone.SOCIAL_OUTER) {
            return tile.transform((BotLocations.GE_CENTER.getX() - tile.getX()) / 2,
                    (BotLocations.GE_CENTER.getY() - tile.getY()) / 2, 0);
        }
        if (zone == GeCrowdZone.WALK_LANE || zone == GeCrowdZone.EDGE_IDLE) {
            return BotLocations.GE_CENTER;
        }
        return ringPoint.anchor;
    }

    private static WorldTile anchorFor(GeCrowdZone zone, GeRingPoint ringPoint, WorldTile tile,
            WorldTile focusTile) {
        if (zone == GeCrowdZone.SOCIAL_INNER || zone == GeCrowdZone.SOCIAL_OUTER) {
            return focusTile;
        }
        if (zone == GeCrowdZone.WALK_LANE || zone == GeCrowdZone.EDGE_IDLE) {
            return tile;
        }
        return ringPoint.anchor;
    }

    private static double scoreGeTile(WorldTile tile, GeCrowdZone zone,
            IdleCrowdBotScript.Role role, int distance, List<WorldTile> reservedTiles,
            ThreadLocalRandom random) {
        int near1 = countGeOccupantsNear(tile, 1, reservedTiles);
        int near2 = countGeOccupantsNear(tile, 2, reservedTiles);
        double score = zone.baseScore - (distance - zone.minDistance) * zone.distancePenalty();
        score -= near1 * zone.nearPenalty;
        score -= Math.max(0, near2 - near1) * (zone.nearPenalty / 3.0);
        if (role == IdleCrowdBotScript.Role.PASSERBY) {
            score -= near1 * 8.0;
        } else if (role == IdleCrowdBotScript.Role.CHATTY
                || role == IdleCrowdBotScript.Role.FASHIONSCAPER
                || role == IdleCrowdBotScript.Role.MAXED_SHOWOFF) {
            score += Math.min(near2, 3) * 5.0;
        }
        return score + random.nextDouble(8.0);
    }

    private static int countGeOccupantsNear(WorldTile tile, int radius, List<WorldTile> reservedTiles) {
        int count = 0;
        for (WorldTile reserved : reservedTiles) {
            if (samePlaneWithin(tile, reserved, radius)) {
                count++;
            }
        }
        for (BotContext context : BOTS.values()) {
            BotPlayer bot = context.bot;
            if (bot != null && !bot.hasFinished() && samePlaneWithin(tile, bot, radius)) {
                count++;
            }
        }
        return count;
    }

    private static boolean isGeSpawnTileUsable(WorldTile tile, List<WorldTile> reservedTiles) {
        if (tile == null || tile.getPlane() != BotLocations.GE_CENTER.getPlane()
                || !World.isNotCliped(tile)) {
            return false;
        }
        for (WorldTile reserved : reservedTiles) {
            if (sameTile(tile, reserved)) {
                return false;
            }
        }
        for (BotContext context : BOTS.values()) {
            BotPlayer bot = context.bot;
            if (bot != null && !bot.hasFinished() && sameTile(tile, bot)) {
                return false;
            }
        }
        return true;
    }

    private static boolean sameTile(WorldTile a, WorldTile b) {
        return a != null && b != null && a.getX() == b.getX() && a.getY() == b.getY()
                && a.getPlane() == b.getPlane();
    }

    private static boolean samePlaneWithin(WorldTile a, WorldTile b, int radius) {
        return a != null && b != null && a.getPlane() == b.getPlane()
                && Math.max(Math.abs(a.getX() - b.getX()), Math.abs(a.getY() - b.getY())) <= radius;
    }

    public static BotPlayer spawnBot(String requestedName, WorldTile tile, BotScript script) {
        if (tile == null) {
            throw new IllegalArgumentException("tile");
        }
        if (script == null) {
            throw new IllegalArgumentException("script");
        }
        String displayName = getSpawnDisplayName(requestedName);
        String username = nextAvailableUsername(displayName);
        displayName = Utils.formatPlayerNameForDisplay(username);
        BotPlayer bot = BotPlayer.create(username, displayName, tile);
        script.bind(bot);
        BOTS.put(username, new BotContext(bot, script));
        ensurePulseTask();
        return bot;
    }

    public static boolean despawn(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        String username = Utils.formatPlayerNameForProtocol(name.trim());
        BotContext context = BOTS.remove(username);
        if (context == null) {
            Player player = World.getPlayer(username);
            if (!(player instanceof BotPlayer)) {
                return false;
            }
            BotTrading.unregister((BotPlayer) player);
            ((BotPlayer) player).destroy();
            return true;
        }
        destroyContext(context);
        return true;
    }

    public static int despawnAll() {
        int count = 0;
        for (BotContext context : BOTS.values()) {
            destroyContext(context);
            count++;
        }
        return count;
    }

    public static int despawnStealingCreationBots() {
        int count = 0;
        for (BotContext context : BOTS.values()) {
            if (!(context.script instanceof StealingCreationBotScript)) {
                continue;
            }
            destroyContext(context);
            count++;
        }
        return count;
    }

    public static int despawnSoulWarsBots() {
        int count = 0;
        for (BotContext context : BOTS.values()) {
            if (!(context.script instanceof SoulWarsBotScript)) {
                continue;
            }
            destroyContext(context);
            count++;
        }
        return count;
    }

    static void unregister(BotPlayer bot) {
        if (bot != null && bot.getUsername() != null) {
            BotTrading.unregister(bot);
            BOTS.remove(bot.getUsername());
        }
    }

    private static void destroyContext(BotContext context) {
        if (context == null || context.bot == null) {
            return;
        }
        BotTrading.unregister(context.bot);
        BOTS.remove(context.bot.getUsername());
        if (context.script != null) {
            context.script.stop();
        }
        context.bot.destroy();
    }

    public static int getBotCount() {
        return BOTS.size();
    }

    public static Collection<BotPlayer> getBots() {
        java.util.List<BotPlayer> bots = new java.util.ArrayList<>();
        for (BotContext context : BOTS.values()) {
            bots.add(context.bot);
        }
        return Collections.unmodifiableList(bots);
    }

    public static String getScriptDebug(BotPlayer bot) {
        BotContext context = bot == null ? null : BOTS.get(bot.getUsername());
        return context == null ? "script=unknown" : context.script.getDebugInfo();
    }

    public static IdleCrowdBotScript getCrowdScript(BotPlayer bot) {
        if (bot == null || bot.getUsername() == null) {
            return null;
        }
        BotContext context = BOTS.get(bot.getUsername());
        if (context == null || !(context.script instanceof IdleCrowdBotScript)) {
            return null;
        }
        return (IdleCrowdBotScript) context.script;
    }

    public static boolean isStealingCreationBot(BotPlayer bot) {
        BotContext context = bot == null ? null : BOTS.get(bot.getUsername());
        return context != null && context.script instanceof StealingCreationBotScript;
    }

    public static boolean isSoulWarsBot(BotPlayer bot) {
        BotContext context = bot == null ? null : BOTS.get(bot.getUsername());
        return context != null && context.script instanceof SoulWarsBotScript;
    }

    /**
     * Lets external systems (e.g. SC death handler) inform a bot that it was
     * just killed by a specific player. The script uses this to remember and
     * temporarily avoid that opponent on respawn.
     */
    public static void notifyDeath(Player victim, Player killer) {
        if (!(victim instanceof BotPlayer)) {
            return;
        }
        BotContext context = BOTS.get(victim.getUsername());
        if (context == null || !(context.script instanceof StealingCreationBotScript)) {
            return;
        }
        ((StealingCreationBotScript) context.script).observeDeathBy(killer);
    }

    private static synchronized void ensurePulseTask() {
        if (pulseTask != null && !pulseTask.isCancelled()) {
            return;
        }
        pulseTask = new WorldTask() {
            @Override
            public void run() {
                if (BOTS.isEmpty()) {
                    pulseTask = null;
                    stop();
                    return;
                }
                for (BotContext context : BOTS.values()) {
                    if (context.bot.hasFinished() || !context.script.isRunning()) {
                        unregister(context.bot);
                        continue;
                    }
                    context.bot.enableRunMode();
                    context.script.pulse();
                }
            }
        };
        WorldTasksManager.schedule(pulseTask, 0, 0);
    }

    private static String getSpawnDisplayName(String requestedName) {
        if (!shouldUseRandomName(requestedName)) {
            return requestedName.trim().replace('_', ' ');
        }
        return nextRandomDisplayName();
    }

    private static boolean shouldUseRandomName(String requestedName) {
        if (requestedName == null || requestedName.trim().isEmpty()) {
            return true;
        }
        String protocolName = Utils.formatPlayerNameForProtocol(requestedName.trim());
        return isNumberedName(protocolName, "sc_red_bot_")
                || isNumberedName(protocolName, "sc_blue_bot_")
                || isNumberedName(protocolName, "sw_red_bot_")
                || isNumberedName(protocolName, "sw_blue_bot_")
                || isNumberedName(protocolName, "bot_");
    }

    private static boolean isNumberedName(String name, String prefix) {
        if (name == null || !name.startsWith(prefix) || name.length() <= prefix.length()) {
            return false;
        }
        for (int index = prefix.length(); index < name.length(); index++) {
            if (!Character.isDigit(name.charAt(index))) {
                return false;
            }
        }
        return true;
    }

    private static String nextRandomDisplayName() {
        for (int attempt = 0; attempt < RANDOM_NAME_ATTEMPTS; attempt++) {
            String displayName = BotName.generate();
            if (isDisplayNameAvailable(displayName)) {
                return displayName;
            }
        }
        return BotName.generateFallback(NEXT_ID.getAndIncrement());
    }

    private static boolean isDisplayNameAvailable(String displayName) {
        String username = safeUsernameBase(displayName);
        return !username.isEmpty() && !BOTS.containsKey(username) && !World.containsPlayer(username);
    }

    private static String nextAvailableUsername(String displayName) {
        String base = safeUsernameBase(displayName);
        if (base.isEmpty()) {
            base = "bot";
        }
        String candidate = base;
        int suffix = 1;
        while (BOTS.containsKey(candidate) || World.containsPlayer(candidate)) {
            candidate = appendUsernameSuffix(base, suffix++);
        }
        return candidate;
    }

    private static String safeUsernameBase(String displayName) {
        String protocolName = Utils.formatPlayerNameForProtocol(displayName == null ? "" : displayName);
        StringBuilder builder = new StringBuilder(protocolName.length());
        boolean previousUnderscore = false;
        for (int index = 0; index < protocolName.length(); index++) {
            char c = protocolName.charAt(index);
            if (isUsernameLetterOrDigit(c)) {
                builder.append(c);
                previousUnderscore = false;
            } else if (c == '_' && !previousUnderscore && builder.length() > 0) {
                builder.append(c);
                previousUnderscore = true;
            }
        }
        String username = trimUsernameEdges(builder.toString());
        if (username.length() > MAX_USERNAME_LENGTH) {
            username = trimUsernameEdges(username.substring(0, MAX_USERNAME_LENGTH));
        }
        return username;
    }

    private static String appendUsernameSuffix(String base, int suffix) {
        String suffixText = "_" + Math.max(1, suffix);
        if (suffixText.length() >= MAX_USERNAME_LENGTH) {
            String compact = "bot" + Math.max(1, suffix);
            return compact.length() > MAX_USERNAME_LENGTH
                    ? compact.substring(0, MAX_USERNAME_LENGTH)
                    : compact;
        }
        int maxBaseLength = MAX_USERNAME_LENGTH - suffixText.length();
        String root = base == null ? "" : base;
        if (root.length() > maxBaseLength) {
            root = trimUsernameEdges(root.substring(0, maxBaseLength));
        }
        if (root.length() < 2) {
            root = "bot";
            if (root.length() > maxBaseLength) {
                root = root.substring(0, maxBaseLength);
            }
        }
        return trimUsernameEdges(root) + suffixText;
    }

    private static String trimUsernameEdges(String username) {
        if (username == null || username.isEmpty()) {
            return "";
        }
        int start = 0;
        int end = username.length();
        while (start < end && username.charAt(start) == '_') {
            start++;
        }
        while (end > start && username.charAt(end - 1) == '_') {
            end--;
        }
        return username.substring(start, end);
    }

    private static boolean isUsernameLetterOrDigit(char c) {
        return c >= 'a' && c <= 'z' || c >= '0' && c <= '9';
    }

    private enum GeCrowdZone {
        BOOTH_RING(true, 44, 1, 2, 1, 2, 120.0, 22.0, 1),
        BANK_SKILLER(true, 34, 1, 3, 1, 2, 112.0, 18.0, 1),
        SOCIAL_INNER(true, 22, 2, 4, 1, 3, 108.0, 11.0, 3),
        OUTER_SPILLOVER(false, 42, 3, 10, 1, 4, 95.0, 12.0, 5),
        SOCIAL_OUTER(false, 22, 4, 9, 2, 5, 92.0, 8.0, 5),
        WALK_LANE(false, 20, 5, 11, 2, 6, 88.0, 20.0, 7),
        EDGE_IDLE(false, 16, 8, 12, 2, 5, 78.0, 10.0, 6);

        private final boolean closeToRing;
        private final int weight;
        private final int minDistance;
        private final int maxDistance;
        private final int minTangentJitter;
        private final int maxTangentJitter;
        private final double baseScore;
        private final double nearPenalty;
        private final int roamRadius;

        private GeCrowdZone(boolean closeToRing, int weight, int minDistance, int maxDistance,
                int minTangentJitter, int maxTangentJitter, double baseScore,
                double nearPenalty, int roamRadius) {
            this.closeToRing = closeToRing;
            this.weight = weight;
            this.minDistance = minDistance;
            this.maxDistance = maxDistance;
            this.minTangentJitter = minTangentJitter;
            this.maxTangentJitter = maxTangentJitter;
            this.baseScore = baseScore;
            this.nearPenalty = nearPenalty;
            this.roamRadius = roamRadius;
        }

        private double distancePenalty() {
            return closeToRing ? 7.0 : 5.0;
        }
    }

    private static final class GeRingPoint {
        private final double x;
        private final double y;
        private final double outwardX;
        private final double outwardY;
        private final double tangentX;
        private final double tangentY;
        private final int plane;
        private final WorldTile anchor;

        private GeRingPoint(double x, double y, double outwardX, double outwardY,
                double tangentX, double tangentY, int plane) {
            this.x = x;
            this.y = y;
            this.outwardX = outwardX;
            this.outwardY = outwardY;
            this.tangentX = tangentX;
            this.tangentY = tangentY;
            this.plane = plane;
            this.anchor = new WorldTile((int) Math.round(x), (int) Math.round(y), plane);
        }

        private WorldTile project(int outwardDistance, int tangentJitter) {
            int tileX = (int) Math.round(x + outwardX * outwardDistance + tangentX * tangentJitter);
            int tileY = (int) Math.round(y + outwardY * outwardDistance + tangentY * tangentJitter);
            return new WorldTile(tileX, tileY, plane);
        }
    }

    private static final class GeCrowdSpot {
        private final WorldTile tile;
        private final WorldTile anchor;
        private final WorldTile focusTile;

        private GeCrowdSpot(WorldTile tile, WorldTile anchor, WorldTile focusTile) {
            this.tile = tile;
            this.anchor = anchor;
            this.focusTile = focusTile;
        }
    }

    private static final class GeCrowdCandidate {
        private final WorldTile tile;
        private final WorldTile anchor;
        private final WorldTile focusTile;
        private final double score;

        private GeCrowdCandidate(WorldTile tile, WorldTile anchor, WorldTile focusTile,
                double score) {
            this.tile = tile;
            this.anchor = anchor;
            this.focusTile = focusTile;
            this.score = score;
        }
    }

    private static final class BotContext {
        private final BotPlayer bot;
        private final BotScript script;

        private BotContext(BotPlayer bot, BotScript script) {
            this.bot = bot;
            this.script = script;
        }
    }
}
