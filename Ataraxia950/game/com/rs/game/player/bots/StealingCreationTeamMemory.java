package com.rs.game.player.bots;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activites.creations.StealingCreation;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Shared short-term calls for one Stealing Creation team.
 */
final class StealingCreationTeamMemory {

    private static final StealingCreationTeamMemory RED = new StealingCreationTeamMemory(true);
    private static final StealingCreationTeamMemory BLUE = new StealingCreationTeamMemory(false);

    private static final int RESOURCE_FORGET_TICKS = 300;
    private static final int CLAIM_DEFAULT_TICKS = 75;
    private static final int KILN_FORGET_TICKS = 240;

    private final boolean redTeam;
    private final Map<Integer, ResourceSighting> resources = new HashMap<Integer, ResourceSighting>();
    private final Map<Integer, ResourceClaim> claims = new HashMap<Integer, ResourceClaim>();
    private final Map<String, TargetCall> targetCalls = new HashMap<String, TargetCall>();
    private final Map<Integer, DangerCall> dangerCalls = new HashMap<Integer, DangerCall>();

    private WorldTile knownKiln;
    private int knownKilnTicks;
    private long lastPulseMillis;

    private StealingCreationTeamMemory(boolean redTeam) {
        this.redTeam = redTeam;
    }

    static StealingCreationTeamMemory forTeam(boolean redTeam) {
        return redTeam ? RED : BLUE;
    }

    synchronized void pulse() {
        long now = Utils.currentTimeMillis();
        if (now - lastPulseMillis < 500) {
            return;
        }
        lastPulseMillis = now;
        decrementResources();
        decrementClaims();
        decrementTargetCalls();
        decrementDangerCalls();
        if (knownKilnTicks > 0 && --knownKilnTicks == 0) {
            knownKiln = null;
        }
    }

    synchronized void rememberKiln(WorldTile tile) {
        if (tile != null) {
            knownKiln = new WorldTile(tile);
            knownKilnTicks = KILN_FORGET_TICKS;
        }
    }

    synchronized WorldTile getKnownKiln() {
        return knownKiln == null ? null : new WorldTile(knownKiln);
    }

    synchronized void rememberResource(BotPlayer bot, WorldTile tile, int tier, int style) {
        if (bot == null || tile == null || tier < 0) {
            return;
        }
        ResourceSighting sighting = new ResourceSighting();
        sighting.tile = new WorldTile(tile);
        sighting.tier = tier;
        sighting.style = style;
        sighting.reporterUsername = bot.getUsername();
        sighting.ticksLeft = RESOURCE_FORGET_TICKS;
        resources.put(tile.getTileHash(), sighting);
    }

    synchronized ResourceHint getBestKnownResource(BotPlayer bot, int style, int maxTier, int maxDistance) {
        ResourceHint best = null;
        int bestScore = Integer.MIN_VALUE;
        for (ResourceSighting sighting : resources.values()) {
            if (sighting.style != style || sighting.tier > maxTier || isClaimedByOther(bot, sighting.tile)) {
                continue;
            }
            int distance = Utils.getDistance(bot.getX(), bot.getY(), sighting.tile.getX(), sighting.tile.getY());
            if (distance > maxDistance) {
                continue;
            }
            int score = (sighting.tier + 1) * 100 - distance;
            if (score > bestScore) {
                bestScore = score;
                best = new ResourceHint(sighting.tile, sighting.tier, sighting.style);
            }
        }
        return best;
    }

    synchronized boolean claimResource(BotPlayer bot, WorldTile tile, int tier, int style) {
        return claimResource(bot, tile, tier, style, CLAIM_DEFAULT_TICKS);
    }

    synchronized boolean claimResource(BotPlayer bot, WorldTile tile, int tier, int style, int ticks) {
        if (bot == null || tile == null) {
            return false;
        }
        ResourceClaim existing = claims.get(tile.getTileHash());
        if (existing != null && !existing.ownerUsername.equals(bot.getUsername())) {
            return false;
        }
        ResourceClaim claim = new ResourceClaim();
        claim.tile = new WorldTile(tile);
        claim.ownerUsername = bot.getUsername();
        claim.tier = tier;
        claim.style = style;
        claim.ticksLeft = Math.max(10, ticks);
        claims.put(tile.getTileHash(), claim);
        return true;
    }

    synchronized boolean isClaimedByOther(BotPlayer bot, WorldTile tile) {
        if (bot == null || tile == null) {
            return false;
        }
        ResourceClaim claim = claims.get(tile.getTileHash());
        return claim != null && !claim.ownerUsername.equals(bot.getUsername());
    }

    synchronized void reportTarget(BotPlayer caller, Player target, int priority, int ticks) {
        if (caller == null || target == null || !StealingCreation.isInGame(target)
                || StealingCreation.isSameTeam(caller, target)) {
            return;
        }
        TargetCall call = new TargetCall();
        call.targetUsername = target.getUsername();
        call.callerUsername = caller.getUsername();
        call.priority = priority;
        call.ticksLeft = Math.max(8, ticks);
        targetCalls.put(target.getUsername(), call);
    }

    synchronized Player getCalledTarget(BotPlayer bot, int radius) {
        Player best = null;
        int bestScore = Integer.MIN_VALUE;
        Iterator<Map.Entry<String, TargetCall>> iterator = targetCalls.entrySet().iterator();
        while (iterator.hasNext()) {
            TargetCall call = iterator.next().getValue();
            Player target = World.getPlayer(call.targetUsername);
            if (target == null || target.hasFinished() || !StealingCreation.isInGame(target)
                    || StealingCreation.isSameTeam(bot, target)) {
                iterator.remove();
                continue;
            }
            int distance = Utils.getDistance(bot.getX(), bot.getY(), target.getX(), target.getY());
            if (distance > radius) {
                continue;
            }
            int score = call.priority - distance;
            if (score > bestScore) {
                bestScore = score;
                best = target;
            }
        }
        return best;
    }

    synchronized void reportDanger(BotPlayer caller, Player enemy, int ticks) {
        if (caller == null || enemy == null || StealingCreation.isSameTeam(caller, enemy)) {
            return;
        }
        DangerCall call = new DangerCall();
        call.tile = new WorldTile(enemy);
        call.enemyUsername = enemy.getUsername();
        call.ticksLeft = Math.max(8, ticks);
        dangerCalls.put(call.tile.getTileHash(), call);
    }

    synchronized int getDangerNear(WorldTile tile, int radius) {
        if (tile == null) {
            return 0;
        }
        int danger = 0;
        for (DangerCall call : dangerCalls.values()) {
            if (Utils.getDistance(tile.getX(), tile.getY(), call.tile.getX(), call.tile.getY()) <= radius) {
                danger += 20;
            }
        }
        return danger;
    }

    boolean isRedTeam() {
        return redTeam;
    }

    private void decrementResources() {
        Iterator<Map.Entry<Integer, ResourceSighting>> iterator = resources.entrySet().iterator();
        while (iterator.hasNext()) {
            ResourceSighting sighting = iterator.next().getValue();
            if (--sighting.ticksLeft <= 0) {
                iterator.remove();
            }
        }
    }

    private void decrementClaims() {
        Iterator<Map.Entry<Integer, ResourceClaim>> iterator = claims.entrySet().iterator();
        while (iterator.hasNext()) {
            ResourceClaim claim = iterator.next().getValue();
            Player owner = World.getPlayer(claim.ownerUsername);
            if (owner == null || owner.hasFinished() || --claim.ticksLeft <= 0) {
                iterator.remove();
            }
        }
    }

    private void decrementTargetCalls() {
        Iterator<Map.Entry<String, TargetCall>> iterator = targetCalls.entrySet().iterator();
        while (iterator.hasNext()) {
            TargetCall call = iterator.next().getValue();
            if (--call.ticksLeft <= 0) {
                iterator.remove();
            }
        }
    }

    private void decrementDangerCalls() {
        Iterator<Map.Entry<Integer, DangerCall>> iterator = dangerCalls.entrySet().iterator();
        while (iterator.hasNext()) {
            DangerCall call = iterator.next().getValue();
            if (--call.ticksLeft <= 0) {
                iterator.remove();
            }
        }
    }

    static final class ResourceHint {
        private final WorldTile tile;
        private final int tier;
        private final int style;

        private ResourceHint(WorldTile tile, int tier, int style) {
            this.tile = new WorldTile(tile);
            this.tier = tier;
            this.style = style;
        }

        WorldTile getTile() {
            return new WorldTile(tile);
        }

        int getTier() {
            return tier;
        }

        int getStyle() {
            return style;
        }
    }

    private static final class ResourceSighting {
        private WorldTile tile;
        private int tier;
        private int style;
        private String reporterUsername;
        private int ticksLeft;
    }

    private static final class ResourceClaim {
        private WorldTile tile;
        private String ownerUsername;
        private int tier;
        private int style;
        private int ticksLeft;
    }

    private static final class TargetCall {
        private String targetUsername;
        private String callerUsername;
        private int priority;
        private int ticksLeft;
    }

    private static final class DangerCall {
        private WorldTile tile;
        private String enemyUsername;
        private int ticksLeft;
    }
}
