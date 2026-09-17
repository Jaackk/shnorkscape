package com.rs.game.player.bots;

import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.soulwars.Avatar;
import com.rs.game.activites.soulwars.GameController;
import com.rs.game.activites.soulwars.GameTask;
import com.rs.game.activites.soulwars.LobbyController;
import com.rs.game.activites.soulwars.SoulWarsManager;
import com.rs.game.activites.soulwars.SoulWarsManager.PlayerType;
import com.rs.game.activites.soulwars.SoulWarsManager.Teams;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.combat.PlayerCombat;
import com.rs.utils.Utils;

import java.util.concurrent.ThreadLocalRandom;

public final class SoulWarsBotScript extends BotScript {

    public enum Role {
        ATTACKER,
        DEFENDER,
        FRAGMENTER,
        HYBRID
    }

    private enum State {
        LOBBY,
        SUPPLY,
        CAPTURE,
        FRAGMENT,
        DEPOSIT,
        ATTACK_AVATAR,
        FIGHT,
        DEFEND,
        RETREAT,
        RECOVER,
        IDLE
    }

    private static final int[] COMBAT_SKILLS = {
            Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE, Skills.HITPOINTS,
            Skills.RANGE, Skills.MAGIC, Skills.PRAYER, Skills.SUMMONING, Skills.SLAYER
    };
    private static final int[] SUPPORT_SKILLS = {
            Skills.WOODCUTTING, Skills.MINING, Skills.FISHING, Skills.HUNTER, Skills.COOKING,
            Skills.HERBLORE, Skills.CRAFTING, Skills.SMITHING, Skills.FLETCHING, Skills.RUNECRAFTING
    };
    private static final int[] SAFE_BARRIERS = { 42015, 42018, 42019, 42020 };
    private static final int[] BANDAGE_TABLES = { 42023, 42024 };
    private static final int[] BARRICADE_TABLES = { 42025, 42026 };
    private static final int[] FRAGMENT_NPCS = { SoulWarsManager.JELLY, SoulWarsManager.PYREFRIEND };

    private final Teams team;
    private final Role role;
    private final BotPersonality personality;

    private State state = State.IDLE;
    private String lastAction = "spawned";
    private int ticks;
    private int actionTicks;
    private int supplyCooldown;
    private int retargetCooldown;
    private int chatterCooldown;

    public SoulWarsBotScript(Teams team, Role role, BotPersonality personality) {
        this.team = team == null ? Teams.RED : team;
        this.role = role == null ? Role.HYBRID : role;
        this.personality = personality == null ? BotPersonality.random() : personality;
    }

    @Override
    protected void onStart() {
        prepareProfile();
        World.soulWars.forceEnterTeamLobby(bot(), team);
        delay(personality.reactionDelay(1, 3));
    }

    @Override
    protected boolean canActWhileBusy() {
        return true;
    }

    @Override
    protected void onTick() {
        tickCounters();
        if (bot().getControlerManager().getControler() instanceof LobbyController) {
            waitInLobby();
            return;
        }
        if (!(bot().getControlerManager().getControler() instanceof GameController)
                || !World.soulWars.isInGame(bot())) {
            state = State.LOBBY;
            lastAction = "joining lobby";
            World.soulWars.forceEnterTeamLobby(bot(), team);
            delay(personality.reactionDelay(2, 4));
            return;
        }
        if (bot().getAppearence().getTransformedNpcId() == SoulWarsManager.GHOST) {
            state = State.RECOVER;
            lastAction = "waiting to respawn";
            delay(2);
            return;
        }
        if (tryUseBandage()) {
            return;
        }
        if (waitForCurrentWork()) {
            return;
        }
        if (World.soulWars.isSafeZone(bot())) {
            leaveSafeZone();
            return;
        }
        if (shouldRetreat()) {
            retreatToBase();
            return;
        }
        if (tryPickupGroundLoot()) {
            return;
        }
        if (tryDepositFragments()) {
            return;
        }
        if (tryTakeSupplies()) {
            return;
        }
        if (tryRoleAction()) {
            return;
        }
        wanderTowardUsefulArea();
    }

    @Override
    public String getDebugInfo() {
        return "sw role=" + role + " arch=" + personality.getArchetype() + " team=" + team
                + " state=" + state + " hp=" + bot().getHitpoints() + "/" + bot().getMaxHitpoints()
                + " frags=" + bot().getInventory().getAmountOf(SoulWarsManager.SOUL_FRAGMENT)
                + " safe=" + World.soulWars.isSafeZone(bot()) + " last=" + lastAction;
    }

    private void waitInLobby() {
        state = State.LOBBY;
        lastAction = "waiting in lobby";
        WorldTile tile = World.soulWars.getLobbyTile(team);
        if (tile != null && !bot().withinDistance(tile, 5)) {
            api().routeNear(tile, 4);
        }
        if (personality.rollChat() && chatterCooldown <= 0) {
            api().forceTalk(ThreadLocalRandom.current().nextBoolean() ? "sw?" : "gl team");
            chatterCooldown = 40 + ThreadLocalRandom.current().nextInt(40);
        }
        delay(personality.reactionDelay(2, 5));
    }

    private boolean tryRoleAction() {
        if (role == Role.DEFENDER) {
            return tryDefenderAction();
        }
        if (role == Role.FRAGMENTER) {
            return tryFragmenterAction();
        }
        if (role == Role.ATTACKER) {
            return tryAttackerAction();
        }
        return tryHybridAction();
    }

    private boolean tryAttackerAction() {
        if (tryAttackEnemyPlayer(8, 0.45)) {
            return true;
        }
        if (tryAttackEnemyAvatar()) {
            return true;
        }
        if (shouldCaptureObelisk()) {
            return captureArea(1);
        }
        return tryFightFragmentNpc();
    }

    private boolean tryDefenderAction() {
        if (tryAttackEnemyPlayer(12, 0.20)) {
            return true;
        }
        if (bot().getInventory().getAmountOf(SoulWarsManager.BONES) > 0 && ThreadLocalRandom.current().nextInt(5) == 0) {
            state = State.DEFEND;
            lastAction = "burying bones";
            World.soulWars.buryBones(bot());
            delay(personality.reactionDelay(2, 4));
            return true;
        }
        if (bot().getInventory().getAmountOf(4053) > 0 && ThreadLocalRandom.current().nextInt(8) == 0
                && World.soulWars.placeHeldBarricade(bot())) {
            state = State.DEFEND;
            lastAction = "placing barricade";
            delay(personality.reactionDelay(2, 4));
            return true;
        }
        int graveyard = team.equals(Teams.BLUE) ? 0 : 2;
        GameTask task = World.soulWars.getGameTask();
        if (!team.equals(task.getTeamAreas()[graveyard]) || task.getTeamAreaValues()[graveyard] < 10) {
            return captureArea(graveyard);
        }
        Avatar avatar = World.soulWars.getAvatar(team);
        if (avatar != null && !avatar.hasFinished() && !avatar.isDead()) {
            state = State.DEFEND;
            lastAction = "defending avatar";
            return moveNear(avatar, 8);
        }
        return captureArea(graveyard);
    }

    private boolean tryFragmenterAction() {
        if (tryPickupGroundLoot()) {
            return true;
        }
        if (tryDepositFragments()) {
            return true;
        }
        if (shouldCaptureObelisk()) {
            return captureArea(1);
        }
        return tryFightFragmentNpc();
    }

    private boolean tryHybridAction() {
        if (tryAttackEnemyPlayer(10, 0.35)) {
            return true;
        }
        if (tryDepositFragments()) {
            return true;
        }
        if (shouldCaptureObelisk()) {
            return captureArea(1);
        }
        if (tryAttackEnemyAvatar()) {
            return true;
        }
        return tryFightFragmentNpc();
    }

    private boolean tryUseBandage() {
        if (bot().getInventory().getAmountOf(SoulWarsManager.BANDAGE_ID) <= 0) {
            return false;
        }
        if (!personality.shouldEat(bot().getHitpoints(), bot().getMaxHitpoints())) {
            return false;
        }
        state = State.RECOVER;
        lastAction = "using bandage";
        World.soulWars.useBandage(bot());
        delay(personality.reactionDelay(2, 4));
        return true;
    }

    private boolean shouldRetreat() {
        return bot().getInventory().getAmountOf(SoulWarsManager.BANDAGE_ID) <= 0
                && personality.shouldRetreat(bot().getHitpoints(), bot().getMaxHitpoints());
    }

    private void retreatToBase() {
        state = State.RETREAT;
        lastAction = "retreating";
        WorldTile base = World.soulWars.getGameTile(team);
        if (base != null) {
            api().routeNear(base, 4);
        }
        delay(personality.reactionDelay(2, 5));
    }

    private void leaveSafeZone() {
        state = State.RECOVER;
        WorldObject barrier = api().findNearestObject(14, object -> {
            if (!contains(SAFE_BARRIERS, object.getId())) {
                return false;
            }
            return !((team.equals(Teams.RED) && object.getId() == 42015)
                    || (team.equals(Teams.BLUE) && object.getId() == 42018));
        });
        if (barrier != null) {
            lastAction = "leaving safe zone";
            api().interactObject(barrier, 1);
        } else {
            lastAction = "walking from safe zone";
            api().routeNear(World.soulWars.getAreaCenter(1), 8);
        }
        delay(personality.reactionDelay(2, 5));
    }

    private boolean tryPickupGroundLoot() {
        if (!bot().getInventory().hasFreeSlots()) {
            return false;
        }
        FloorItem item = api().findNearestGroundItem(9, SoulWarsManager.SOUL_FRAGMENT, SoulWarsManager.BONES);
        if (item == null) {
            return false;
        }
        state = State.FRAGMENT;
        lastAction = "looting " + item.getId();
        boolean started = api().pickupGroundItem(item);
        delay(personality.reactionDelay(1, 3));
        return started;
    }

    private boolean tryDepositFragments() {
        int fragments = bot().getInventory().getAmountOf(SoulWarsManager.SOUL_FRAGMENT);
        if (fragments <= 0 || !World.soulWars.canDepositFragments(bot())) {
            return false;
        }
        int targetAmount = role == Role.FRAGMENTER ? 3 : Math.max(1, (int) Math.round(4.0 + personality.getGreed() * 6.0));
        if (fragments < targetAmount && role != Role.ATTACKER && role != Role.HYBRID) {
            return false;
        }
        WorldTile obelisk = World.soulWars.getAreaCenter(1);
        if (obelisk == null) {
            return false;
        }
        state = State.DEPOSIT;
        if (!bot().withinDistance(obelisk, 9)) {
            lastAction = "moving to obelisk";
            api().routeNear(obelisk, 4);
            delay(personality.reactionDelay(2, 5));
            return true;
        }
        WorldObject object = api().findNearestObject(10, 42010);
        lastAction = "depositing fragments";
        if (object != null) {
            api().useItemOnObject(SoulWarsManager.SOUL_FRAGMENT, object);
        } else {
            World.soulWars.depositSoulFragments(bot());
        }
        delay(personality.reactionDelay(2, 5));
        return true;
    }

    private boolean tryTakeSupplies() {
        if (supplyCooldown > 0 || bot().getAttackedByDelay() + 10000 > Utils.currentTimeMillis()) {
            return false;
        }
        if (bot().getInventory().getAmountOf(SoulWarsManager.BANDAGE_ID) < 3 && bot().getInventory().hasFreeSlots()) {
            return takeFromNearestTable(BANDAGE_TABLES, "taking bandage");
        }
        if (role == Role.DEFENDER && bot().getInventory().getAmountOf(4053) <= 0 && bot().getInventory().hasFreeSlots()) {
            return takeFromNearestTable(BARRICADE_TABLES, "taking barricade");
        }
        return false;
    }

    private boolean takeFromNearestTable(int[] tableIds, String action) {
        WorldObject table = api().findNearestObject(18, tableIds);
        if (table == null) {
            return false;
        }
        state = State.SUPPLY;
        lastAction = action;
        supplyCooldown = 8;
        boolean started = api().interactObject(table, 1);
        delay(personality.reactionDelay(2, 4));
        return started;
    }

    private boolean tryAttackEnemyPlayer(int radius, double minimumAggression) {
        if (retargetCooldown > 0 || personality.getAggression() < minimumAggression) {
            return false;
        }
        Player target = api().findNearestPlayer(radius, player -> player.getControlerManager().getControler() instanceof GameController
                && !World.soulWars.isSameTeam(bot(), player)
                && !World.soulWars.isSafeZone(player)
                && player.getAppearence().getTransformedNpcId() == -1);
        if (target == null) {
            return false;
        }
        state = State.FIGHT;
        lastAction = "attacking " + target.getDisplayName();
        if (personality.usesSpec()) {
            api().tryUseSpecial(50);
        }
        retargetCooldown = 10;
        boolean started = api().attack(target);
        delay(personality.reactionDelay(1, 3));
        return started;
    }

    private boolean tryAttackEnemyAvatar() {
        Teams enemyTeam = team.equals(Teams.RED) ? Teams.BLUE : Teams.RED;
        Avatar avatar = World.soulWars.getAvatar(enemyTeam);
        if (avatar == null || avatar.hasFinished() || avatar.isDead()) {
            return false;
        }
        if (World.soulWars.getGameTask().getAvatarSlayerLevel(enemyTeam) > bot().getSkills().getLevel(Skills.SLAYER)) {
            return false;
        }
        if (!bot().withinDistance(avatar, 12)) {
            state = State.ATTACK_AVATAR;
            lastAction = "rushing avatar";
            api().routeNear(avatar, 3);
            delay(personality.reactionDelay(2, 5));
            return true;
        }
        state = State.ATTACK_AVATAR;
        lastAction = "attacking avatar";
        if (personality.usesSpec()) {
            api().tryUseSpecial(50);
        }
        boolean started = api().attack(avatar);
        delay(personality.reactionDelay(1, 3));
        return started;
    }

    private boolean tryFightFragmentNpc() {
        NPC npc = api().findNearestNpc(18, npc1 -> contains(FRAGMENT_NPCS, npc1.getId()));
        if (npc == null) {
            WorldTile obelisk = World.soulWars.getAreaCenter(1);
            if (obelisk != null) {
                state = State.FRAGMENT;
                lastAction = "finding fragments";
                api().routeNear(obelisk, 12);
                delay(personality.reactionDelay(2, 5));
                return true;
            }
            return false;
        }
        if (!bot().withinDistance(npc, 10)) {
            state = State.FRAGMENT;
            lastAction = "chasing fragment npc";
            api().routeNear(npc, 2);
            delay(personality.reactionDelay(2, 4));
            return true;
        }
        state = State.FRAGMENT;
        lastAction = "killing fragment npc";
        boolean started = api().attack(npc);
        delay(personality.reactionDelay(1, 3));
        return started;
    }

    private boolean shouldCaptureObelisk() {
        GameTask task = World.soulWars.getGameTask();
        return !team.equals(task.getTeamAreas()[1]) || task.getTeamAreaValues()[1] < 10;
    }

    private boolean captureArea(int index) {
        WorldTile tile = World.soulWars.getAreaCenter(index);
        if (tile == null) {
            return false;
        }
        state = State.CAPTURE;
        lastAction = "capturing area " + index;
        if (bot().withinDistance(tile, 2)) {
            api().walkNear(tile, 1, 4);
        } else {
            api().routeNear(tile, 3);
        }
        delay(personality.reactionDelay(2, 5));
        return true;
    }

    private void wanderTowardUsefulArea() {
        state = State.IDLE;
        WorldTile target = role == Role.DEFENDER
                ? (World.soulWars.getAvatar(team) == null ? World.soulWars.getGameTile(team) : World.soulWars.getAvatar(team))
                : World.soulWars.getAreaCenter(1);
        lastAction = "wandering";
        if (target != null) {
            api().routeNear(target, role == Role.DEFENDER ? 8 : 5);
        }
        delay(personality.reactionDelay(3, 7));
    }

    private boolean waitForCurrentWork() {
        if (bot().getActionManager().getAction() instanceof PlayerCombat) {
            actionTicks++;
            Entity target = ((PlayerCombat) bot().getActionManager().getAction()).getTarget();
            if (target == null || target.hasFinished() || target.isDead() || actionTicks > 80) {
                bot().getActionManager().forceStop();
                actionTicks = 0;
                return false;
            }
            lastAction = "fighting";
            return true;
        }
        if (bot().getActionManager().getAction() != null) {
            actionTicks++;
            if (actionTicks > 30) {
                bot().getActionManager().forceStop();
                actionTicks = 0;
                return false;
            }
            lastAction = "working";
            return true;
        }
        actionTicks = 0;
        if (bot().getRouteEvent() != null || bot().hasWalkSteps()) {
            lastAction = "moving";
            return true;
        }
        return false;
    }

    private boolean moveNear(WorldTile tile, int radius) {
        if (tile == null) {
            return false;
        }
        if (bot().withinDistance(tile, radius)) {
            api().walkNear(tile, Math.min(2, radius), 4);
        } else {
            api().routeNear(tile, radius);
        }
        delay(personality.reactionDelay(2, 5));
        return true;
    }

    private void tickCounters() {
        ticks++;
        if (supplyCooldown > 0) {
            supplyCooldown--;
        }
        if (retargetCooldown > 0) {
            retargetCooldown--;
        }
        if (chatterCooldown > 0) {
            chatterCooldown--;
        }
    }

    private void prepareProfile() {
        BotProfileGenerator.forTier(BotProfileGenerator.Tier.MAXED).apply(bot());
        for (int skill : COMBAT_SKILLS) {
            setLevel(skill, 99);
        }
        for (int skill : SUPPORT_SKILLS) {
            setLevel(skill, 70 + ThreadLocalRandom.current().nextInt(30));
        }
        bot().getCombatDefinitions().resetSpecialAttack();
        bot().getPrayer().reset();
        bot().setHitpoints(bot().getMaxHitpoints());
        bot().refreshHitPoints();
        bot().setRunEnergy(100);
    }

    private void setLevel(int skill, int level) {
        bot().getSkills().set(skill, level);
        bot().getSkills().setXp(skill, Skills.getXPForLevel(skill, level));
    }

    private boolean contains(int[] values, int value) {
        for (int next : values) {
            if (next == value) {
                return true;
            }
        }
        return false;
    }
}
