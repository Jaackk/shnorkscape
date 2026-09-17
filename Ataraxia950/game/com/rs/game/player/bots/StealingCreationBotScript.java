package com.rs.game.player.bots;

import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.creations.StealingCreation;
import com.rs.game.activites.creations.StealingCreationGame;
import com.rs.game.activites.creations.StealingCreationLobby;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.combat.PlayerCombat;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Bot script for Stealing Creation. Each bot is given a {@link BotPersonality}
 * at spawn that drives delays, target selection, retreat thresholds, prayer
 * usage, chat frequency and other quirks. Two bots with the same Role can play
 * very differently because their personalities roll different traits.
 */
public class StealingCreationBotScript extends BotScript {

    public enum Role {
        GATHERER,
        FIGHTER,
        HYBRID
    }

    private enum State {
        LOBBY,
        GATHER,
        PROCESS,
        DEPOSIT,
        FIGHT,
        PICKPOCKET,
        DEFEND,
        ESCORT,
        BARRIER,
        SCOUT,
        RETREAT,
        IDLE,
        RECOVER
    }

    private enum Assignment {
        RUNNER,
        TOOL_UPGRADER,
        T5_SPECIALIST,
        RUSHER,
        DEFENDER,
        HARASSER,
        ANTI_CARRIER,
        ESCORT,
        SCOUT,
        BARRIER_BUILDER,
        SABOTEUR,
        OPPORTUNIST
    }

    private enum Goal {
        RETREAT,
        DEPOSIT,
        PROCESS_TOOL,
        PROCESS_SUPPLIES,
        BUILD_BARRIER,
        BREAK_BARRIER,
        DEFEND_BASE,
        ESCORT_CARRIER,
        PICKPOCKET,
        FIGHT,
        GATHER,
        SCOUT,
        IDLE,
        RECOVER
    }

    private static final int[] SC_BOT_SKILLS = { Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE, Skills.HITPOINTS,
            Skills.RANGE, Skills.MAGIC, Skills.PRAYER, Skills.SUMMONING, Skills.WOODCUTTING, Skills.MINING,
            Skills.FISHING, Skills.HUNTER, Skills.COOKING, Skills.HERBLORE, Skills.CRAFTING, Skills.SMITHING,
            Skills.FLETCHING, Skills.RUNECRAFTING, Skills.CONSTRUCTION, Skills.THIEVING };

    private static final int KILN_PROCESS_FALLBACK_RADIUS = 12;
    private static final int RESOURCE_SEARCH_RADIUS = 96;
    private static final int RESOURCE_ROUTE_RADIUS = 4;
    private static final int FIGHT_SEARCH_RADIUS = 35;
    private static final int PICKPOCKET_SEARCH_RADIUS = 24;
    private static final int BASE_GATE_PASS_RADIUS = 14;
    private static final int BASE_GATE_FALLBACK_RADIUS = 20;
    private static final int BASE_COMBAT_PADDING = 2;
    private static final int STALE_FIGHT_ACTION_TICKS = 10;
    private static final int LAST_KILLER_AVOID_RADIUS = 12;
    private static final int LAST_KILLER_FORGET_TICKS = 80;
    private static final int PILE_ON_RADIUS = 18;
    private static final int DEFEND_BASE_RADIUS = 22;
    private static final int ESCORT_RADIUS = 16;
    private static final int BARRIER_SEARCH_RADIUS = 64;
    private static final int PERCEPTION_RADIUS = 48;
    private static final int CARRIER_VALUE_SCORE = 80;
    private static final int TEAM_CALL_TICKS = 28;
    private static final int RESOURCE_CLAIM_TICKS = 85;
    private static final int ASSIGNMENT_RETHINK_TICKS = 180;
    // Normal-prayer-book slot ids (book 0). Slot 17 is Chivalry, slot 19 is Piety
    // - the previous PRAYER_PROTECT_FROM_MELEE = 17 was actually Chivalry, not a
    // damage-reducing protection prayer. Fixed to slot 13 (Protect from Melee).
    private static final int PRAYER_PROTECT_FROM_MAGIC = 11;
    private static final int PRAYER_PROTECT_FROM_RANGED = 12;
    private static final int PRAYER_PROTECT_FROM_MELEE = 13;
    private static final int PRAYER_PIETY = 19;
    private static final int PRAYER_RIGOUR = 20;
    private static final int PRAYER_AUGURY = 21;

    private final boolean inRedTeam;
    private final Role role;
    private final BotPersonality personality;
    private final StealingCreationBotMemory memory = new StealingCreationBotMemory();

    private State state = State.LOBBY;
    private Goal currentGoal = Goal.RECOVER;
    private Assignment assignment = Assignment.OPPORTUNIST;
    private StealingCreationTeamMemory teamMemory;
    private String lastAction = "starting";
    private int processedClay;
    private int gatherStyle;
    private int busyTicks;
    private int actionTicks;
    private int processRouteAttempts;
    private int lastX;
    private int lastY;
    private int idleTicksSinceCombat;
    private int sinceLastChatTicks;
    private int lastKillerAvoidTicks;
    private int specCooldownTicks;
    private int prayerCooldownTicks;
    private int assignmentTicks;
    private int supplyCraftCooldownTicks;
    private int styleSwitchCooldownTicks;
    private String lastKillerName;

    public StealingCreationBotScript(boolean inRedTeam) {
        this(inRedTeam, Role.HYBRID, BotPersonality.random());
    }

    public StealingCreationBotScript(boolean inRedTeam, Role role) {
        this(inRedTeam, role, BotPersonality.random());
    }

    public StealingCreationBotScript(boolean inRedTeam, Role role, BotPersonality personality) {
        this.inRedTeam = inRedTeam;
        this.role = role == null ? Role.HYBRID : role;
        this.personality = personality == null ? BotPersonality.random() : personality;
    }

    public Role getRole() {
        return role;
    }

    public BotPersonality getPersonality() {
        return personality;
    }

    @Override
    protected void onStart() {
        teamMemory = StealingCreationTeamMemory.forTeam(inRedTeam);
        assignment = pickAssignment();
        // Pick style FIRST, then prepareStats so the primary gather skill comes
        // out at the proper level. Switching style mid-match also re-grades.
        gatherStyle = pickInitialGatherStyle();
        prepareStats();
        ensurePrimaryGatherLevel();
        lastX = bot().getX();
        lastY = bot().getY();
        StealingCreation.forceEnterTeamLobby(bot(), inRedTeam);
        lastAction = "entered lobby as " + assignment;
        delay(personality.reactionDelay(2, 5));
        if (personality.rollChat()) {
            api().forceTalk(BotChat.pick(BotChat.LOBBY_GREET));
        }
    }

    @Override
    protected boolean canActWhileBusy() {
        return true;
    }

    @Override
    protected void onTick() {
        tickCounters();
        Object controller = bot().getControlerManager().getControler();
        if (controller == null) {
            state = State.LOBBY;
            lastAction = "re-entering lobby";
            StealingCreation.forceEnterTeamLobby(bot(), inRedTeam);
            delay(personality.reactionDelay(2, 5));
            return;
        }
        if (controller instanceof StealingCreationLobby) {
            handleLobby();
            return;
        }
        if (!(controller instanceof StealingCreationGame)) {
            state = State.RECOVER;
            lastAction = "outside SC";
            delay(4);
            return;
        }
        if (StealingCreation.recoverToGameArena(bot())) {
            state = State.RECOVER;
            lastAction = "returning to arena";
            delay(2);
            return;
        }
        if (combatReact()) {
            return;
        }
        if (leaveOwnBaseBeforeWork()) {
            return;
        }
        if (waitForCurrentWork()) {
            return;
        }
        runGameLoop();
    }

    @Override
    public String getDebugInfo() {
        return "sc role=" + role + " arch=" + personality.getArchetype() + " team=" + (inRedTeam ? "red" : "blue")
                + " assign=" + assignment + " goal=" + currentGoal + " state=" + state + " clay="
                + StealingCreation.getSacredClayAmount(bot()) + " processed="
                + processedClay + " value=" + StealingCreation.getInventoryScoreValue(bot()) + " hp="
                + bot().getHitpoints() + "/" + bot().getMaxHitpoints() + " tool="
                + getTierName(StealingCreation.getBestToolIndexForStyle(bot(), gatherStyle)) + " target="
                + getTierName(getTargetResourceIndex()) + " busy=" + getBusyState() + " last=" + lastAction;
    }

    private void tickCounters() {
        memory.pulse();
        if (teamMemory != null) {
            teamMemory.pulse();
        }
        if (++assignmentTicks > ASSIGNMENT_RETHINK_TICKS && role == Role.HYBRID && personality.rollSubOptimal()) {
            assignment = pickAssignment();
            assignmentTicks = 0;
        }
        if (sinceLastChatTicks < Integer.MAX_VALUE) {
            sinceLastChatTicks++;
        }
        if (lastKillerAvoidTicks > 0) {
            lastKillerAvoidTicks--;
            if (lastKillerAvoidTicks == 0) {
                lastKillerName = null;
            }
        }
        if (specCooldownTicks > 0) {
            specCooldownTicks--;
        }
        if (prayerCooldownTicks > 0) {
            prayerCooldownTicks--;
        }
        if (supplyCraftCooldownTicks > 0) {
            supplyCraftCooldownTicks--;
        }
        if (styleSwitchCooldownTicks > 0) {
            styleSwitchCooldownTicks--;
        }
    }

    private void handleLobby() {
        state = State.LOBBY;
        lastAction = "waiting";
        api().walkNear(StealingCreation.getLobbyTile(inRedTeam), 3);
        delay(personality.reactionDelay(5, 10));
        if (personality.rollChat() && sinceLastChatTicks > 30) {
            api().forceTalk(BotChat.pick(BotChat.GAME_START));
            sinceLastChatTicks = 0;
        }
    }

    /**
     * Reactive combat behavior runs before the regular state machine each tick.
     * Lets the bot heal, retreat or pray-flick mid-combat instead of mechanically
     * grinding through whatever it was doing.
     *
     * Triggers when EITHER:
     *  - the bot was recently attacked (incoming damage), OR
     *  - the bot is currently the attacker (so a kiting / spec-down bot still
     *    eats food and primes prayer between blows).
     */
    private boolean combatReact() {
        boolean beingAttacked = bot().getAttackedBy() != null
                && bot().getAttackedByDelay() > Utils.currentTimeMillis();
        boolean isAttacking = bot().getActionManager().getAction() instanceof PlayerCombat
                || (bot().getAttackingDelay() > Utils.currentTimeMillis());
        boolean inCombat = beingAttacked || isAttacking;
        if (inCombat) {
            idleTicksSinceCombat = 0;
        } else {
            idleTicksSinceCombat++;
            if (idleTicksSinceCombat == 8 && state == State.RETREAT && personality.rollChat()) {
                api().forceTalk(BotChat.pick(BotChat.RECOVERED));
                sinceLastChatTicks = 0;
            }
        }
        // Always allow eating below the eat threshold even outside combat - emulates
        // a player topping up between gathering hits.
        int hp = bot().getHitpoints();
        int max = bot().getMaxHitpoints();
        if (personality.shouldRetreat(hp, max)) {
            // Retreat eagerness: panic eat then disengage if we're being hit.
            if (api().hasFood() && api().tryEatFood(personality.getEatHpPercent())
                    && personality.rollChat()) {
                api().forceTalk(BotChat.pick(BotChat.LOW_HP));
                sinceLastChatTicks = 0;
            }
            if (beingAttacked || isAttacking) {
                return retreat();
            }
        }
        if (personality.shouldEat(hp, max) && api().hasFood()
                && api().tryEatFood(personality.getEatHpPercent())) {
            if (personality.rollChat() && sinceLastChatTicks > 8) {
                api().forceTalk(BotChat.pick(BotChat.LOW_HP));
                sinceLastChatTicks = 0;
            }
            // ate but keep current goal
        }
        if (inCombat && personality.usesPrayerFlick() && prayerCooldownTicks <= 0
                && !api().hasPrayersOn() && api().hasPrayerPoints(40)) {
            // Prefer the protection prayer that matches the attacker's style.
            // Veteran fighters may stack an offensive prayer too, but that's a
            // bonus tick not done here.
            Player attacker = bot().getAttackedBy() instanceof Player
                    ? (Player) bot().getAttackedBy() : null;
            int prayerSlot = pickProtectionSlotForAttacker(attacker);
            if (prayerSlot < 0 && role == Role.FIGHTER) {
                prayerSlot = PRAYER_PIETY;
            }
            if (prayerSlot >= 0 && api().togglePrayer(prayerSlot)) {
                prayerCooldownTicks = 25;
            }
        }
        return false;
    }

    /**
     * Reads the attacker's equipped weapon name to choose Protect-from-Melee /
     * Missiles / Magic. Falls back to Protect-from-Melee when the gear isn't
     * obvious (typical SC bot loadout) or to -1 when no protection prayer is
     * needed.
     */
    private int pickProtectionSlotForAttacker(Player attacker) {
        if (attacker == null) {
            return PRAYER_PROTECT_FROM_MELEE;
        }
        int weaponId = attacker.getEquipment() == null ? -1 : attacker.getEquipment().getWeaponId();
        if (weaponId <= 0) {
            return PRAYER_PROTECT_FROM_MELEE;
        }
        com.rs.cache.loaders.ItemDefinitions defs = com.rs.cache.loaders.ItemDefinitions.getItemDefinitions(weaponId);
        if (defs == null || defs.getName() == null) {
            return PRAYER_PROTECT_FROM_MELEE;
        }
        String name = defs.getName().toLowerCase();
        if (name.contains("bow") || name.contains("crossbow") || name.contains("dart")
                || name.contains("throwing") || name.contains("knife") || name.contains("javelin")) {
            return PRAYER_PROTECT_FROM_RANGED;
        }
        if (name.contains("staff") || name.contains("wand") || name.contains("battlestaff")
                || name.contains("of light") || name.contains("of darkness")) {
            return PRAYER_PROTECT_FROM_MAGIC;
        }
        return PRAYER_PROTECT_FROM_MELEE;
    }

    private boolean retreat() {
        state = State.RETREAT;
        lastAction = "retreating";
        api().cancelAll();
        // Wounded bots shout for help so teammates with pile-on tendency can come.
        if (personality.rollChat() && sinceLastChatTicks > 12) {
            api().forceTalk(BotChat.pick(BotChat.HELP_RETREAT));
            sinceLastChatTicks = 0;
        }
        // Aim for the team base but with a bit of jitter so multiple retreating
        // bots don't all funnel through the same single tile.
        WorldTile baseTile = StealingCreation.getGameTile(inRedTeam);
        WorldTile target = StealingCreation.getRandomGameMapTileNear(baseTile, 3);
        boolean walked = api().routeNear(target, 4) || api().walkNear(target, 5);
        if (!walked) {
            walked = api().routeNear(baseTile, 4) || api().walkNear(baseTile, 5);
        }
        delay(personality.reactionDelay(3, 6));
        return walked;
    }

    private void runGameLoop() {
        observeSurroundings();
        if (personality.swapsWeapon()) {
            tryEquipBestTool();
        }

        GoalChoice choice = chooseGoal();
        Goal previousGoal = currentGoal;
        currentGoal = choice.goal;

        // Brief "hesitation" when switching to a meaningfully different goal -
        // a real player takes a moment to read the screen before committing.
        if (previousGoal != currentGoal && isMeaningfulSwitch(previousGoal, currentGoal)
                && personality.rollIdle()) {
            lastAction = "rethinking " + previousGoal + "->" + currentGoal;
            delay(personality.reactionDelay(1, 3));
            return;
        }

        if (!executeGoal(choice)) {
            recover();
        }
    }

    private boolean isMeaningfulSwitch(Goal previous, Goal next) {
        if (previous == null || next == null || previous == next) {
            return false;
        }
        // Bouncing between gathering states is fine; switching to/from combat
        // or deposit is a real context shift worth pausing for.
        if (previous == Goal.GATHER && next == Goal.RECOVER) {
            return false;
        }
        return previous == Goal.FIGHT || next == Goal.FIGHT
                || previous == Goal.DEFEND_BASE || next == Goal.DEFEND_BASE
                || previous == Goal.RETREAT || next == Goal.RETREAT
                || previous == Goal.DEPOSIT || next == Goal.DEPOSIT
                || previous == Goal.ESCORT_CARRIER || next == Goal.ESCORT_CARRIER;
    }

    private void observeSurroundings() {
        WorldTile kiln = StealingCreation.getNearestProcessingKilnTile(bot());
        memory.rememberKiln(kiln);
        if (teamMemory != null) {
            teamMemory.rememberKiln(kiln);
        }
        for (WorldObject object : api().findObjects(PERCEPTION_RADIUS,
                obj -> StealingCreation.getResourceIndex(obj.getId()) >= 0)) {
            int tier = StealingCreation.getResourceIndex(object.getId());
            int style = StealingCreation.getResourceAnimationStyle(object);
            memory.rememberResource(object, tier, style);
            if (teamMemory != null) {
                teamMemory.rememberResource(bot(), object, tier, style);
            }
        }
        boolean shoutedCarrier = false;
        for (Player player : api().findPlayers(PERCEPTION_RADIUS, p -> StealingCreation.isInGame(p))) {
            if (StealingCreation.isSameTeam(bot(), player)) {
                continue;
            }
            memory.rememberSeenEnemy(player);
            int value = StealingCreation.getInventoryScoreValue(player);
            boolean isCarrier = value >= CARRIER_VALUE_SCORE;
            if (teamMemory != null && (isCarrier || StealingCreation.isNearAnyBase(player, DEFEND_BASE_RADIUS))) {
                teamMemory.reportDanger(bot(), player, TEAM_CALL_TICKS);
                teamMemory.reportTarget(bot(), player, getTargetValue(player, true), TEAM_CALL_TICKS);
            }
            if (isCarrier && !shoutedCarrier && personality.callsTargets()
                    && personality.rollChat() && sinceLastChatTicks > 18) {
                api().forceTalk(BotChat.pick(BotChat.SPOTTED_CARRIER));
                sinceLastChatTicks = 0;
                shoutedCarrier = true;
            }
        }
    }

    private GoalChoice chooseGoal() {
        int clayAmount = StealingCreation.getSacredClayAmount(bot());
        int inventoryValue = StealingCreation.getInventoryScoreValue(bot());
        int carriedDanger = inventoryValue / 4 + (clayAmount / 2);
        GoalChoice best = GoalChoice.of(Goal.RECOVER, 1, "fallback");

        if (personality.shouldRetreat(bot().getHitpoints(), bot().getMaxHitpoints())) {
            best = bestOf(best, GoalChoice.of(Goal.RETREAT, 950 + carriedDanger, "low hp"));
        }
        if (shouldDeposit(clayAmount)) {
            int score = 760 + inventoryValue + (bot().getInventory().hasFreeSlots() ? 0 : 220);
            if (teamMemory != null) {
                score += teamMemory.getDangerNear(bot(), 10);
            }
            best = bestOf(best, GoalChoice.of(Goal.DEPOSIT, score, "cash in"));
        }
        if (clayAmount > 0 && shouldProcess(clayAmount)) {
            best = bestOf(best, GoalChoice.of(Goal.PROCESS_TOOL, 700 + (5 - Math.max(0,
                    StealingCreation.getBestToolIndexForStyle(bot(), gatherStyle))) * 35, "tool upgrade"));
        }
        if (clayAmount > 0 && shouldCraftSupplies(clayAmount)) {
            best = bestOf(best, GoalChoice.of(Goal.PROCESS_SUPPLIES, getSupplyScore(clayAmount), "team supplies"));
        }

        WorldObject buildSpot = findBarrierBuildSpot();
        if (buildSpot != null && shouldBuildBarrier(buildSpot)) {
            int tier = findNearbyResourceTierForBuildSpot(buildSpot);
            best = bestOf(best, GoalChoice.forObject(Goal.BUILD_BARRIER, 610 + Math.max(0, tier) * 80,
                    buildSpot, "protect resource"));
        }

        WorldObject enemyBarrier = findEnemyBarrier();
        if (enemyBarrier != null && shouldBreakBarrier(enemyBarrier)) {
            int tier = StealingCreation.getResourceBarrierTier(enemyBarrier);
            best = bestOf(best, GoalChoice.forObject(Goal.BREAK_BARRIER,
                    assignment == Assignment.SABOTEUR ? 760 : 560 + Math.max(0, tier) * 35,
                    enemyBarrier, "break barrier"));
        }

        TargetScore defenderTarget = pickBestTarget(true);
        if (defenderTarget != null && isNearOwnBase(defenderTarget.target, DEFEND_BASE_RADIUS)) {
            best = bestOf(best, GoalChoice.forPlayer(Goal.DEFEND_BASE, 680 + defenderTarget.score,
                    defenderTarget.target, "base defense"));
        }

        Player escortTarget = findCarrierToEscort();
        if (escortTarget != null) {
            int score = assignment == Assignment.ESCORT ? 700 : 520;
            score += StealingCreation.getInventoryScoreValue(escortTarget) / 3;
            best = bestOf(best, GoalChoice.forPlayer(Goal.ESCORT_CARRIER, score, escortTarget, "escort carrier"));
        }

        TargetScore pickpocketTarget = pickBestPickpocketTarget();
        if (pickpocketTarget != null && shouldPickpocket(clayAmount, inventoryValue, pickpocketTarget)) {
            int score = getPickpocketGoalBaseScore() + pickpocketTarget.score;
            if (inventoryValue > 0) {
                score -= inventoryValue / 3;
            }
            best = bestOf(best, GoalChoice.forPlayer(Goal.PICKPOCKET, score,
                    pickpocketTarget.target, "pickpocket carrier"));
        }

        TargetScore fightTarget = pickBestTarget(false);
        if (fightTarget != null && shouldFight(clayAmount, inventoryValue)) {
            int score = getFightGoalBaseScore() + fightTarget.score;
            if (inventoryValue > 0) {
                score -= inventoryValue / 2;
            }
            best = bestOf(best, GoalChoice.forPlayer(Goal.FIGHT, score, fightTarget.target, "fight"));
        }

        if (role != Role.FIGHTER && bot().getInventory().hasFreeSlots()) {
            GoalChoice gather = getGatherGoalChoice();
            if (gather != null) {
                best = bestOf(best, gather);
            }
        }

        if (assignment == Assignment.SCOUT || role == Role.FIGHTER) {
            best = bestOf(best, GoalChoice.of(Goal.SCOUT, assignment == Assignment.SCOUT ? 420 : 220, "scout"));
        }
        if (personality.rollIdle()) {
            best = bestOf(best, GoalChoice.of(Goal.IDLE, 180 + (int) (personality.getIdleTendency() * 120), "idle"));
        }
        if (personality.rollMisclick()) {
            best = addHumanMistake(best);
        }
        return best;
    }

    private GoalChoice bestOf(GoalChoice current, GoalChoice candidate) {
        if (candidate == null) {
            return current;
        }
        // Stickiness: a goal we're already executing gets a small bonus so a
        // momentary score swing doesn't yank a half-finished deposit run into
        // a fight, then back, then forward again. Higher-efficiency bots hold
        // a goal more strongly than rookies.
        int candidateScore = candidate.score;
        int currentScore = current == null ? Integer.MIN_VALUE : current.score;
        if (current != null && current.goal == currentGoal) {
            currentScore += getGoalMomentumBonus();
        }
        if (candidate.goal == currentGoal) {
            candidateScore += getGoalMomentumBonus();
        }
        return candidateScore > currentScore ? candidate : current;
    }

    private int getGoalMomentumBonus() {
        // 25-65 score points depending on focus. A really sharp bot resists
        // distractions; a chaotic one flips on a dime.
        return 25 + (int) (personality.getEfficiency() * 40);
    }

    private boolean executeGoal(GoalChoice choice) {
        if (choice == null) {
            return false;
        }
        // Only pass the barrier when we're inside our own base AND the goal
        // requires being outside (combat, gather, build, scout...). The earlier
        // symmetric "passBaseBarrierIfNeeded" yanked the bot back inside whenever
        // an outside helper was within range.
        if (goalRequiresLeavingBase(choice.goal)
                && StealingCreation.isInsideOwnBase(bot(), inRedTeam)
                && tryLeaveOwnBase()) {
            return true;
        }
        // Slow-click mistake: applied as an extra delay before the goal action,
        // so the bot looks like a person hovering before clicking.
        if (choice.reason != null && choice.reason.startsWith("slow click ")) {
            delay(personality.reactionDelay(2, 4));
        }
        switch (choice.goal) {
            case RETREAT:
                return retreat();
            case DEPOSIT:
                return depositItems();
            case PROCESS_TOOL:
                return processClay();
            case PROCESS_SUPPLIES:
                return processSupplies();
            case BUILD_BARRIER:
                return buildBarrier(choice.object);
            case BREAK_BARRIER:
                return breakBarrier(choice.object);
            case DEFEND_BASE:
            case FIGHT:
                return attackTarget(choice.target);
            case PICKPOCKET:
                return pickpocketTarget(choice.target);
            case ESCORT_CARRIER:
                return escortCarrier(choice.target);
            case GATHER:
                if (choice.object != null) {
                    return gatherClay(choice.object);
                }
                if (choice.tile != null) {
                    return routeToResourceTile(choice.tile, choice.reason);
                }
                return gatherClay();
            case SCOUT:
                return scout();
            case IDLE:
                return maybeIdleBanter();
            case RECOVER:
            default:
                recover();
                return true;
        }
    }

    private boolean goalRequiresLeavingBase(Goal goal) {
        // DEPOSIT goes the other way (entering); RETREAT/RECOVER/IDLE are fine
        // wherever we are; PROCESS_TOOL/PROCESS_SUPPLIES happens at the kiln,
        // outside base so requires leaving when we're stuck inside.
        return goal != Goal.DEPOSIT && goal != Goal.RETREAT && goal != Goal.IDLE
                && goal != Goal.RECOVER;
    }

    private boolean leaveOwnBaseBeforeWork() {
        if (!StealingCreation.isInsideOwnBase(bot(), inRedTeam)
                || shouldDeposit(StealingCreation.getSacredClayAmount(bot()))) {
            return false;
        }
        if (personality.shouldRetreat(bot().getHitpoints(), bot().getMaxHitpoints())) {
            return false;
        }
        if (bot().isLocked() || bot().isFrozen()) {
            return false;
        }
        bot().setRouteEvent(null);
        bot().resetWalkSteps();
        if (bot().getActionManager().getAction() != null) {
            bot().getActionManager().forceStop();
        }
        currentGoal = Goal.RECOVER;
        return tryLeaveOwnBase();
    }

    /**
     * A concrete table of small human-like mistakes. Each mistake is a real
     * action (slow click, wrong target, wrong gather tile) instead of a vague
     * +5 score nudge. Returns the original goal if no mistake fits this state,
     * or a substitute that still ties into the goal pipeline.
     */
    private GoalChoice addHumanMistake(GoalChoice original) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        // 1) Slow-click: stretch the next reaction by an extra tick or two,
        //    leaving the same goal in place. Looks like a player squinting.
        if (random.nextInt(5) == 0) {
            // Marker reason - we don't change the goal but we extend the delay
            // when the goal actually executes. handleMistakeDelay() reads this.
            return new GoalChoice(original.goal, original.score - 2, original.target,
                    original.object, original.tile, "slow click " + original.reason);
        }

        // 2) Wrong-tier gather: pick a lower tier than optimal so the bot looks
        //    like it click-misclicked the wrong rock.
        if (original.goal == Goal.GATHER && bot().getInventory().hasFreeSlots()
                && random.nextInt(4) == 0) {
            int currentTarget = getTargetResourceIndex();
            int wrongTier = Math.max(0, currentTarget - 1 - random.nextInt(2));
            WorldObject lower = findNearestResourceAtOrBelow(
                    StealingCreation.getResourceObjectsForStyle(gatherStyle), wrongTier, RESOURCE_SEARCH_RADIUS);
            if (lower != null) {
                return GoalChoice.forObject(Goal.GATHER, original.score - 5, lower, "miscliked tier");
            }
        }

        // 3) Wandered off: small chance to insert a SCOUT detour mid-gather.
        if (original.goal == Goal.GATHER && random.nextInt(6) == 0) {
            return GoalChoice.of(Goal.SCOUT, original.score - 8, "wandered off");
        }

        // 4) Mid-fight inventory check: rusher with full inventory turns to deposit
        //    instead of pressing for a kill (greedy mode).
        if (original.goal == Goal.FIGHT && !bot().getInventory().hasFreeSlots()
                && random.nextInt(3) == 0) {
            return GoalChoice.of(Goal.DEPOSIT, original.score - 10, "second-guessed fight");
        }

        // 5) Hesitated to deposit: with empty hands they sometimes idle for a tick
        //    before choosing what to do next.
        if (original.goal == Goal.DEPOSIT && bot().getInventory().hasFreeSlots()
                && random.nextInt(3) == 0) {
            return GoalChoice.of(Goal.IDLE, original.score - 5, "hesitated");
        }

        // 6) Picked the wrong process target (clay vs supplies) - swap
        if (original.goal == Goal.PROCESS_TOOL && random.nextInt(8) == 0) {
            return GoalChoice.of(Goal.PROCESS_SUPPLIES, original.score - 5, "wrong kiln menu");
        }

        return original;
    }

    private boolean shouldCraftSupplies(int clayAmount) {
        if (supplyCraftCooldownTicks > 0 || clayAmount <= 0) {
            return false;
        }
        int barrierCount = StealingCreation.getBestBarrierItemCountForBot(bot());
        if (assignment == Assignment.BARRIER_BUILDER && StealingCreation.getBestBarrierItemIdForBot(bot()) < 0
                && barrierCount + clayAmount >= 4) {
            return true;
        }
        if ((role == Role.FIGHTER || role == Role.HYBRID || assignment == Assignment.ESCORT)
                && !api().hasFood() && clayAmount >= 1) {
            return true;
        }
        return !bot().getInventory().hasFreeSlots() && !api().hasFood() && clayAmount > 0;
    }

    private int getSupplyScore(int clayAmount) {
        int score = 430 + clayAmount * 8;
        if (assignment == Assignment.BARRIER_BUILDER) {
            score += 220;
        }
        if (!api().hasFood() && (role == Role.FIGHTER || assignment == Assignment.ESCORT)) {
            score += 180;
        }
        return score;
    }

    private boolean shouldBuildBarrier(WorldObject spot) {
        if (spot == null || StealingCreation.getBestBarrierItemIdForBot(bot()) < 0) {
            return false;
        }
        if (assignment == Assignment.BARRIER_BUILDER || assignment == Assignment.T5_SPECIALIST) {
            return true;
        }
        return personality.rollSubOptimal() || StealingCreation.getInventoryScoreValue(bot()) < 120;
    }

    private boolean shouldBreakBarrier(WorldObject barrier) {
        if (barrier == null) {
            return false;
        }
        return assignment == Assignment.SABOTEUR || assignment == Assignment.RUSHER
                || assignment == Assignment.ANTI_CARRIER || role == Role.FIGHTER
                || ThreadLocalRandom.current().nextDouble() < personality.getAggression();
    }

    private WorldObject findBarrierBuildSpot() {
        return api().findNearestObject(BARRIER_SEARCH_RADIUS, object -> StealingCreation.isResourceBarrierBuildSpot(object));
    }

    private int findNearbyResourceTierForBuildSpot(WorldObject spot) {
        if (spot == null) {
            return -1;
        }
        int bestTier = -1;
        for (WorldObject resource : api().findObjects(10, object -> StealingCreation.getResourceIndex(object.getId()) >= 0)) {
            if (resource.getChunkX() == spot.getChunkX() && resource.getChunkY() == spot.getChunkY()) {
                bestTier = Math.max(bestTier, StealingCreation.getResourceIndex(resource.getId()));
            }
        }
        return bestTier;
    }

    private WorldObject findEnemyBarrier() {
        return api().findNearestObject(BARRIER_SEARCH_RADIUS,
                object -> StealingCreation.isEnemyResourceBarrier(bot(), object));
    }

    private boolean isNearOwnBase(Player player, int radius) {
        return player != null && Utils.getDistance(player.getX(), player.getY(),
                StealingCreation.getGameTile(inRedTeam).getX(), StealingCreation.getGameTile(inRedTeam).getY()) <= radius;
    }

    private Player findCarrierToEscort() {
        Player best = null;
        int bestScore = 0;
        for (Player player : api().findPlayers(ESCORT_RADIUS, p -> StealingCreation.isInGame(p))) {
            if (player == bot() || !StealingCreation.isSameTeam(bot(), player)) {
                continue;
            }
            int value = StealingCreation.getInventoryScoreValue(player);
            if (value < CARRIER_VALUE_SCORE || StealingCreation.isInsideOwnBase(player, inRedTeam)) {
                continue;
            }
            int distance = Utils.getDistance(bot().getX(), bot().getY(), player.getX(), player.getY());
            int score = value - distance * 4;
            if (score > bestScore) {
                best = player;
                bestScore = score;
            }
        }
        return best;
    }

    private int getFightGoalBaseScore() {
        switch (assignment) {
            case RUSHER:
            case ANTI_CARRIER:
            case HARASSER:
                return 390;
            case DEFENDER:
            case ESCORT:
                return 270;
            case SCOUT:
            case SABOTEUR:
                return 310;
            default:
                return role == Role.FIGHTER ? 360 : 210;
        }
    }

    private int getPickpocketGoalBaseScore() {
        switch (assignment) {
            case ANTI_CARRIER:
                return 360;
            case SABOTEUR:
            case HARASSER:
                return 320;
            case OPPORTUNIST:
                return 260;
            case RUSHER:
                return 230;
            default:
                return role == Role.FIGHTER ? 240 : 120;
        }
    }

    private GoalChoice getGatherGoalChoice() {
        WorldObject resource = findResourceToGather();
        if (resource != null) {
            int tier = StealingCreation.getResourceIndex(resource.getId());
            int score = 380 + Math.max(0, tier) * 90;
            if (assignment == Assignment.T5_SPECIALIST && tier >= 4) {
                score += 180;
            }
            if (assignment == Assignment.TOOL_UPGRADER) {
                score += 80;
            }
            return GoalChoice.forObject(Goal.GATHER, score, resource, "gather " + getTierName(tier));
        }
        StealingCreationTeamMemory.ResourceHint hint = teamMemory == null ? null
                : teamMemory.getBestKnownResource(bot(), gatherStyle, getTargetResourceIndex(), RESOURCE_SEARCH_RADIUS);
        if (hint != null && !memory.isRecentlyFailedRoute(hint.getTile())) {
            return GoalChoice.forTile(Goal.GATHER, 330 + hint.getTier() * 75, hint.getTile(), "known resource");
        }
        WorldTile remembered = memory.getLastResourceTile(gatherStyle, getTargetResourceIndex());
        if (remembered != null && !memory.isRecentlyFailedRoute(remembered)) {
            return GoalChoice.forTile(Goal.GATHER, 300, remembered, "remembered resource");
        }
        return null;
    }

    private int getTargetValue(Player target, boolean calledTarget) {
        if (target == null) {
            return 0;
        }
        int distance = Utils.getDistance(bot().getX(), bot().getY(), target.getX(), target.getY());
        int maxHp = target.getMaxHitpoints();
        int hpPercent = maxHp > 0 ? (target.getHitpoints() * 100) / maxHp : 100;
        int inventoryValue = StealingCreation.getInventoryScoreValue(target);
        int score = 180 - distance * 5;
        score += inventoryValue / 2;
        if (inventoryValue >= CARRIER_VALUE_SCORE) {
            score += assignment == Assignment.ANTI_CARRIER ? 180 : 90;
        }
        if (hpPercent <= 35) {
            score += 130;
        } else if (hpPercent <= 60) {
            score += 60;
        }
        if (assignment == Assignment.DEFENDER && isNearOwnBase(target, DEFEND_BASE_RADIUS)) {
            score += 160;
        }
        if (memory.isFocusedTarget(target)) {
            score += 40;
        }
        if (memory.shouldAvoid(target, bot(), LAST_KILLER_AVOID_RADIUS)) {
            score -= 170;
        }
        score -= memory.getThreatScore(target);
        if (calledTarget) {
            score += 40;
        }
        return score;
    }

    private void tryEquipBestTool() {
        int toolIndex = StealingCreation.getBestToolIndexForStyle(bot(), gatherStyle);
        if (toolIndex < 0) {
            return;
        }
        int baseId = StealingCreation.getToolBaseIdForStyle(gatherStyle);
        if (baseId < 0) {
            return;
        }
        int toolId = baseId + (toolIndex * 2);
        if (bot().getEquipment().getWeaponId() == toolId) {
            return;
        }
        if (bot().getInventory().getAmountOf(toolId) > 0) {
            api().equipItemId(toolId);
        }
    }

    /**
     * Attempts to cross the team base barrier from inside to outside. Only call
     * when the bot is currently inside its own base. Finds a helper that's also
     * inside the base (the only ones whose target tile lies outside) and passes
     * through it. Falls back to door / climb-over if no helper is available.
     */
    private boolean tryLeaveOwnBase() {
        if (bot().isLocked() || !StealingCreation.isInsideOwnBase(bot(), inRedTeam)) {
            return false;
        }
        WorldObject helper = findGateHelperInside();
        if (passBaseGateHelper(helper)) {
            return true;
        }
        WorldObject door = findOwnDoor();
        if (door != null && StealingCreation.canPassBaseDoor(bot(), door)) {
            state = State.RECOVER;
            lastAction = "leaving via door";
            StealingCreation.passBaseDoor(bot(), door);
            delay(personality.reactionDelay(3, 5));
            return true;
        }
        if (StealingCreation.passOwnBaseBarrier(bot())) {
            state = State.RECOVER;
            lastAction = "climbing out of base";
            delay(personality.reactionDelay(3, 5));
            return true;
        }
        return false;
    }

    /**
     * Attempts to cross the team base barrier from outside to inside. Only call
     * when the bot is currently outside its own base. Finds a helper that's
     * outside the base (whose target tile lies inside) and passes through it.
     */
    private boolean tryEnterOwnBase() {
        if (bot().isLocked() || StealingCreation.isInsideOwnBase(bot(), inRedTeam)) {
            return false;
        }
        WorldObject helper = findGateHelperOutside();
        if (passBaseGateHelper(helper)) {
            return true;
        }
        WorldObject door = findOwnDoor();
        if (door != null && StealingCreation.canPassBaseDoor(bot(), door)) {
            state = State.RECOVER;
            lastAction = "entering via door";
            StealingCreation.passBaseDoor(bot(), door);
            delay(personality.reactionDelay(3, 5));
            return true;
        }
        return false;
    }

    private WorldObject findOwnDoor() {
        return inRedTeam
                ? api().findNearestObject(10, StealingCreation.RED_DOOR_1, StealingCreation.RED_DOOR_2)
                : api().findNearestObject(10, StealingCreation.BLUE_DOOR_1, StealingCreation.BLUE_DOOR_2);
    }

    private WorldObject findGateHelperInside() {
        final int helperId = inRedTeam ? StealingCreation.RED_GATE_HELPER : StealingCreation.BLUE_GATE_HELPER;
        return api().findNearestObject(BASE_GATE_PASS_RADIUS,
                object -> object.getId() == helperId
                        && StealingCreation.canPassBaseGateHelper(bot(), object)
                        && StealingCreation.isOwnBaseGateHelperOnPlayerSide(bot(), object));
    }

    private WorldObject findGateHelperOutside() {
        final int helperId = inRedTeam ? StealingCreation.RED_GATE_HELPER : StealingCreation.BLUE_GATE_HELPER;
        return api().findNearestObject(BASE_GATE_FALLBACK_RADIUS,
                object -> object.getId() == helperId
                        && StealingCreation.canPassBaseGateHelper(bot(), object)
                        && StealingCreation.isOwnBaseGateHelperOnPlayerSide(bot(), object));
    }

    private boolean passBaseGateHelper(WorldObject helper) {
        if (helper == null || !StealingCreation.canPassBaseGateHelper(bot(), helper)) {
            return false;
        }
        state = State.RECOVER;
        lastAction = "passing base helper " + helper.getId();
        StealingCreation.passBaseGateHelper(bot(), helper);
        delay(personality.reactionDelay(3, 5));
        return true;
    }

    private boolean attackTarget(Player target) {
        if (StealingCreation.isNearAnyBase(bot(), BASE_COMBAT_PADDING)) {
            return false;
        }
        if (target == null) {
            return false;
        }
        state = State.FIGHT;
        lastAction = "attacking " + target.getDisplayName();
        processRouteAttempts = 0;
        actionTicks = 0;
        if (personality.callsTargets() && personality.rollChat() && sinceLastChatTicks > 10) {
            api().forceTalk(BotChat.pick(BotChat.FIGHT_OPEN));
            sinceLastChatTicks = 0;
        }
        if (personality.usesSpec() && specCooldownTicks <= 0) {
            int targetMax = target.getMaxHitpoints();
            int targetHpPercent = targetMax > 0 ? (target.getHitpoints() * 100) / targetMax : 100;
            if (targetHpPercent <= 60 && api().tryUseSpecial(50)) {
                specCooldownTicks = 30;
                if (personality.rollChat()) {
                    api().forceTalk(BotChat.pick(BotChat.SPECIAL_CALLOUT));
                    sinceLastChatTicks = 0;
                }
            }
        }
        delay(personality.reactionDelay(2, 5));
        boolean attacked = api().attack(target);
        if (attacked) {
            memory.rememberTarget(target);
            if (teamMemory != null) {
                teamMemory.reportTarget(bot(), target, getTargetValue(target, true), TEAM_CALL_TICKS);
            }
        }
        return attacked;
    }

    private boolean pickpocketTarget(Player target) {
        if (target == null || !StealingCreation.canPickpocket(bot(), target, false)) {
            return false;
        }
        state = State.PICKPOCKET;
        lastAction = "pickpocketing " + target.getDisplayName();
        processRouteAttempts = 0;
        actionTicks = 0;
        delay(personality.reactionDelay(2, 4));
        boolean started = api().pickpocket(target);
        if (started) {
            memory.rememberTarget(target);
            if (teamMemory != null && StealingCreation.getInventoryScoreValue(target) >= CARRIER_VALUE_SCORE) {
                teamMemory.reportTarget(bot(), target, getPickpocketTargetValue(target, true), TEAM_CALL_TICKS);
            }
        }
        return started;
    }

    private TargetScore pickBestPickpocketTarget() {
        Player called = teamMemory == null ? null : teamMemory.getCalledTarget(bot(), PICKPOCKET_SEARCH_RADIUS);
        TargetScore best = called != null && isPickpocketableEnemy(called)
                ? new TargetScore(called, getPickpocketTargetValue(called, true))
                : null;
        for (Player enemy : api().findPlayers(PICKPOCKET_SEARCH_RADIUS, this::isPickpocketableEnemy)) {
            best = betterTarget(best, new TargetScore(enemy, getPickpocketTargetValue(enemy, false)));
        }
        return best == null || best.score < 120 ? null : best;
    }

    private int getPickpocketTargetValue(Player target, boolean calledTarget) {
        if (target == null) {
            return 0;
        }
        int inventoryValue = StealingCreation.getInventoryScoreValue(target);
        if (inventoryValue <= 0) {
            return -1000;
        }
        int chance = StealingCreation.getPickpocketChance(bot(), target);
        if (chance <= 0) {
            return -1000;
        }
        int distance = Utils.getDistance(bot().getX(), bot().getY(), target.getX(), target.getY());
        int score = 180 - distance * 6;
        score += inventoryValue;
        score += chance * 2;
        if (inventoryValue >= CARRIER_VALUE_SCORE) {
            score += assignment == Assignment.ANTI_CARRIER ? 220 : 120;
        }
        if (assignment == Assignment.SABOTEUR || assignment == Assignment.HARASSER) {
            score += 70;
        }
        if (assignment == Assignment.OPPORTUNIST && distance <= 6) {
            score += 80;
        }
        if (memory.isFocusedTarget(target)) {
            score += 30;
        }
        if (memory.shouldAvoid(target, bot(), LAST_KILLER_AVOID_RADIUS)) {
            score -= 160;
        }
        score -= memory.getThreatScore(target) / 2;
        if (calledTarget) {
            score += 40;
        }
        return score;
    }

    private boolean isPickpocketableEnemy(Player target) {
        return target != null && !StealingCreation.isInsideFog(target)
                && StealingCreation.canPickpocket(bot(), target, false);
    }

    private TargetScore pickBestTarget(boolean defending) {
        Player called = teamMemory == null ? null : teamMemory.getCalledTarget(bot(), FIGHT_SEARCH_RADIUS);
        TargetScore best = called != null && isAttackableEnemy(called)
                ? new TargetScore(called, getTargetValue(called, true))
                : null;
        if (best != null && personality.rollChat() && sinceLastChatTicks > 14) {
            // We're answering a teammate callout - shout a quick "+1".
            api().forceTalk(BotChat.pick(BotChat.PILED_ON));
            sinceLastChatTicks = 0;
        }
        if (personality.rollPileOn()) {
            Player teammateTarget = findTeammateEngagedEnemy();
            if (teammateTarget != null) {
                best = betterTarget(best, new TargetScore(teammateTarget, getTargetValue(teammateTarget, true) + 90));
            }
        }
        for (Player enemy : api().findPlayers(FIGHT_SEARCH_RADIUS, this::isAttackableEnemy)) {
            if (defending && !isNearOwnBase(enemy, DEFEND_BASE_RADIUS)) {
                continue;
            }
            int score = getTargetValue(enemy, false);
            if (lastKillerName != null && lastKillerAvoidTicks > 0
                    && enemy.getDisplayName().equals(lastKillerName)
                    && Utils.getDistance(bot().getX(), bot().getY(), enemy.getX(), enemy.getY())
                            <= LAST_KILLER_AVOID_RADIUS) {
                score -= 120;
            }
            best = betterTarget(best, new TargetScore(enemy, score));
        }
        if (best == null && personality.rollSubOptimal()) {
            Player random = pickRandomEnemy();
            if (random != null) {
                best = new TargetScore(random, getTargetValue(random, false) - 40);
            }
        }
        return best == null || best.score < -80 ? null : best;
    }

    private TargetScore betterTarget(TargetScore current, TargetScore candidate) {
        if (candidate == null) {
            return current;
        }
        if (current == null || candidate.score > current.score) {
            return candidate;
        }
        return current;
    }

    private Player findTeammateEngagedEnemy() {
        Player best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (Player player : World.getPlayers()) {
            if (player == null || player == bot() || player.hasFinished()) {
                continue;
            }
            if (!isAttackableEnemy(player)) {
                continue;
            }
            int distance = Utils.getDistance(bot().getX(), bot().getY(), player.getX(), player.getY());
            if (distance > PILE_ON_RADIUS) {
                continue;
            }
            Entity attacker = player.getAttackedBy();
            if (!(attacker instanceof Player)) {
                continue;
            }
            Player attackingPlayer = (Player) attacker;
            if (attackingPlayer == bot() || !StealingCreation.isSameTeam(bot(), attackingPlayer)) {
                continue;
            }
            if (distance < bestDistance) {
                best = player;
                bestDistance = distance;
            }
        }
        return best;
    }

    private Player pickRandomEnemy() {
        List<Player> candidates = new ArrayList<>();
        for (Player player : World.getPlayers()) {
            if (player == null || player == bot() || player.hasFinished()) {
                continue;
            }
            if (!isAttackableEnemy(player)) {
                continue;
            }
            int distance = Utils.getDistance(bot().getX(), bot().getY(), player.getX(), player.getY());
            if (distance <= FIGHT_SEARCH_RADIUS) {
                candidates.add(player);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }

    private boolean isAttackableEnemy(Player target) {
        return target != null && StealingCreation.isInGame(target)
                && !StealingCreation.isSameTeam(bot(), target)
                && !StealingCreation.isInsideFog(bot())
                && !StealingCreation.isInsideFog(target)
                && !StealingCreation.isNearAnyBase(target, BASE_COMBAT_PADDING);
    }

    private boolean gatherClay() {
        WorldObject resource = findResourceToGather();
        if (resource == null) {
            return false;
        }
        return gatherClay(resource);
    }

    private boolean gatherClay(WorldObject resource) {
        if (resource == null) {
            return false;
        }
        state = State.GATHER;
        int tier = StealingCreation.getResourceIndex(resource.getId());
        lastAction = "gathering " + getTierName(tier) + " object " + resource.getId();
        processRouteAttempts = 0;
        if (teamMemory != null) {
            teamMemory.claimResource(bot(), resource, tier, StealingCreation.getResourceAnimationStyle(resource),
                    RESOURCE_CLAIM_TICKS);
            teamMemory.rememberResource(bot(), resource, tier, StealingCreation.getResourceAnimationStyle(resource));
        }
        memory.rememberResource(resource, tier, StealingCreation.getResourceAnimationStyle(resource));
        if (tier >= 4 && personality.rollChat()) {
            api().forceTalk(BotChat.pick(BotChat.FOUND_T5));
            sinceLastChatTicks = 0;
        }
        delay(personality.reactionDelay(5, 10));
        return api().interactObject(resource, 1);
    }

    private boolean processClay() {
        state = State.PROCESS;
        WorldTile kilnTile = StealingCreation.getNearestProcessingKilnTile(bot());
        WorldObject kiln = api().findNearestObject(90, StealingCreation.PROCESSING_KILN);
        WorldTile targetTile = kiln == null ? kilnTile : kiln;
        if (personality.rollSubOptimal()) {
            List<WorldTile> tiles = StealingCreation.getProcessingKilnTiles();
            if (tiles.size() > 1) {
                WorldTile alt = tiles.get(ThreadLocalRandom.current().nextInt(tiles.size()));
                if (!alt.matches(kilnTile)) {
                    kilnTile = alt;
                    targetTile = alt;
                }
            }
        }
        int distanceToTarget = Utils.getDistance(bot().getX(), bot().getY(), targetTile.getX(), targetTile.getY());
        int distanceToKnownKiln = Utils.getDistance(bot().getX(), bot().getY(), kilnTile.getX(), kilnTile.getY());
        if (distanceToTarget <= 2 || distanceToKnownKiln <= 2) {
            return processClayNow("processed clay");
        }

        if (processRouteAttempts >= 3 && isNearKilnArea(distanceToTarget, distanceToKnownKiln)) {
            return processClayNow("processed clay near kiln");
        }

        lastAction = "walking to kiln";
        delay(personality.reactionDelay(2, 5));
        processRouteAttempts++;
        boolean walking = routeToProcessingKiln(targetTile, kilnTile);
        if (!walking && isNearKilnArea(distanceToTarget, distanceToKnownKiln)) {
            return processClayNow("processed clay near kiln");
        }
        if (!walking) {
            lastAction = "cannot reach kiln";
        }
        return walking;
    }

    private boolean routeToProcessingKiln(WorldTile targetTile, WorldTile kilnTile) {
        if (api().routeNear(targetTile, 4) || api().routeNear(kilnTile, 4)) {
            return true;
        }
        List<WorldTile> kilnTiles = StealingCreation.getProcessingKilnTiles();
        for (WorldTile tile : kilnTiles) {
            if (tile.matches(kilnTile) || tile.matches(targetTile)) {
                continue;
            }
            if (api().routeNear(tile, 4)) {
                lastAction = "walking to alternate kiln";
                return true;
            }
        }
        return false;
    }

    private boolean isNearKilnArea(int distanceToTarget, int distanceToKnownKiln) {
        return distanceToTarget <= KILN_PROCESS_FALLBACK_RADIUS
                || distanceToKnownKiln <= KILN_PROCESS_FALLBACK_RADIUS;
    }

    private boolean processClayNow(String successAction) {
        int processed = StealingCreation.processBestClayForBot(bot(), gatherStyle);
        processedClay += processed;
        if (processed > 0) {
            processRouteAttempts = 0;
            lastAction = successAction;
            if (personality.swapsWeapon()) {
                tryEquipBestTool();
            }
        } else {
            lastAction = "failed to process";
        }
        delay(personality.reactionDelay(2, 4));
        return processed > 0;
    }

    private boolean processSupplies() {
        state = State.PROCESS;
        WorldTile kilnTile = getPreferredKilnTile();
        int distance = Utils.getDistance(bot().getX(), bot().getY(), kilnTile.getX(), kilnTile.getY());
        if (distance <= 2) {
            return processSuppliesNow();
        }
        lastAction = "walking to kiln for supplies";
        delay(personality.reactionDelay(2, 5));
        boolean walking = routeToProcessingKiln(kilnTile, kilnTile);
        if (!walking && distance <= KILN_PROCESS_FALLBACK_RADIUS) {
            return processSuppliesNow();
        }
        if (!walking) {
            memory.rememberFailedRoute(kilnTile);
        }
        return walking;
    }

    private WorldTile getPreferredKilnTile() {
        WorldTile tile = teamMemory == null ? null : teamMemory.getKnownKiln();
        if (tile == null) {
            tile = memory.getLastKilnTile();
        }
        return tile == null ? StealingCreation.getNearestProcessingKilnTile(bot()) : tile;
    }

    private boolean processSuppliesNow() {
        int crafted = 0;
        int barrierCount = StealingCreation.getBestBarrierItemCountForBot(bot());
        if (assignment == Assignment.BARRIER_BUILDER && StealingCreation.getBestBarrierItemIdForBot(bot()) < 0
                && barrierCount + StealingCreation.getSacredClayAmount(bot()) >= 4) {
            crafted += StealingCreation.processBestClayProductForBot(bot(), StealingCreation.BOT_PRODUCT_BARRIER,
                    Math.max(1, 4 - barrierCount));
        }
        if (!api().hasFood() && StealingCreation.getSacredClayAmount(bot()) > 0) {
            crafted += StealingCreation.processBestClayProductForBot(bot(), StealingCreation.BOT_PRODUCT_FOOD,
                    role == Role.FIGHTER ? 3 : 2);
        }
        supplyCraftCooldownTicks = crafted > 0 ? 30 : 12;
        lastAction = crafted > 0 ? "crafted supplies" : "failed to craft supplies";
        delay(personality.reactionDelay(2, 4));
        return crafted > 0;
    }

    private boolean buildBarrier(WorldObject spot) {
        if (spot == null) {
            return false;
        }
        state = State.BARRIER;
        lastAction = "building resource barrier";
        delay(personality.reactionDelay(3, 6));
        return api().interactObject(spot, 2);
    }

    private boolean breakBarrier(WorldObject barrier) {
        if (barrier == null) {
            return false;
        }
        state = State.BARRIER;
        lastAction = "breaking enemy barrier";
        delay(personality.reactionDelay(3, 6));
        return api().interactObject(barrier, 1);
    }

    private boolean escortCarrier(Player carrier) {
        if (carrier == null || carrier.hasFinished() || !StealingCreation.isSameTeam(bot(), carrier)) {
            return false;
        }
        state = State.ESCORT;
        TargetScore threat = pickBestTarget(true);
        if (threat != null && Utils.getDistance(bot().getX(), bot().getY(), threat.target.getX(), threat.target.getY()) <= 8) {
            return attackTarget(threat.target);
        }
        lastAction = "escorting " + carrier.getDisplayName();
        delay(personality.reactionDelay(2, 5));
        return api().routeNear(carrier, 2) || api().walkNear(carrier, 2);
    }

    private boolean scout() {
        state = State.SCOUT;
        lastAction = "scouting";
        WorldTile center = assignment == Assignment.SCOUT && personality.rollSubOptimal()
                ? StealingCreation.getRandomGameMapTileNear(bot(), 20)
                : StealingCreation.getGameMapCenterTile();
        delay(personality.reactionDelay(4, 8));
        return api().walkTo(StealingCreation.getRandomGameMapTileNear(center, 22));
    }

    private boolean depositItems() {
        state = State.DEPOSIT;
        processRouteAttempts = 0;
        if (StealingCreation.isInsideOwnBase(bot(), inRedTeam)) {
            int valueBeforeDeposit = StealingCreation.getInventoryScoreValue(bot());
            int deposited = StealingCreation.depositInventory(bot());
            if (deposited > 0) {
                processedClay = 0;
                lastAction = "deposited " + deposited + " items";
                // A big haul gets a flex; a normal deposit gets the routine line.
                String[] pool = valueBeforeDeposit >= 800
                        ? BotChat.DEPOSIT_BIG : BotChat.DEPOSITING;
                if (personality.rollChat() && sinceLastChatTicks > 12) {
                    api().forceTalk(BotChat.pick(pool));
                    sinceLastChatTicks = 0;
                }
                if (styleSwitchCooldownTicks <= 0 && ThreadLocalRandom.current().nextInt(4) == 0) {
                    setGatherStyle(ThreadLocalRandom.current().nextInt(4));
                    styleSwitchCooldownTicks = 80;
                }
                delay(personality.reactionDelay(3, 6));
                return true;
            }
            lastAction = "nothing to deposit";
            processedClay = 0;
            delay(personality.reactionDelay(2, 4));
            return false;
        }
        if (tryEnterOwnBase()) {
            lastAction = "entering base";
            return true;
        }
        lastAction = "returning to base";
        delay(personality.reactionDelay(2, 5));
        return api().routeNear(StealingCreation.getGameTile(inRedTeam), 5)
                || api().walkNear(StealingCreation.getGameTile(inRedTeam), 4);
    }

    private WorldObject findResourceToGather() {
        int targetIndex = getTargetResourceIndex();
        WorldObject resource = findNearestResourceAtOrBelow(StealingCreation.getResourceObjectsForStyle(gatherStyle),
                targetIndex, RESOURCE_SEARCH_RADIUS);
        if (resource != null) {
            return resource;
        }
        resource = findNearestResourceAtOrBelow(StealingCreation.RESOURCE_OBJECTS, targetIndex, RESOURCE_SEARCH_RADIUS);
        if (resource != null) {
            return resource;
        }
        resource = findNearestResourceAtOrBelow(StealingCreation.getResourceObjectsForStyle(gatherStyle), 4,
                RESOURCE_SEARCH_RADIUS);
        if (resource != null) {
            return resource;
        }
        return findNearestResourceAtOrBelow(StealingCreation.RESOURCE_OBJECTS, 4, RESOURCE_SEARCH_RADIUS);
    }

    private WorldObject findNearestResourceAtOrBelow(int[] objectIds, int maxResourceIndex, int radius) {
        for (int index = maxResourceIndex; index >= 0; index--) {
            WorldObject nearest = null;
            int nearestDistance = Integer.MAX_VALUE;
            WorldObject second = null;
            int secondDistance = Integer.MAX_VALUE;
            for (int objectId : objectIds) {
                if (StealingCreation.getResourceIndex(objectId) != index) {
                    continue;
                }
                WorldObject object = api().findNearestObject(radius, objectId);
                if (object == null) {
                    continue;
                }
                if (teamMemory != null && teamMemory.isClaimedByOther(bot(), object)) {
                    continue;
                }
                int distance = Utils.getDistance(bot().getX(), bot().getY(), object.getX(), object.getY());
                if (distance < nearestDistance) {
                    second = nearest;
                    secondDistance = nearestDistance;
                    nearest = object;
                    nearestDistance = distance;
                } else if (distance < secondDistance) {
                    second = object;
                    secondDistance = distance;
                }
            }
            if (nearest != null) {
                if (second != null && personality.rollSubOptimal()) {
                    return second;
                }
                return nearest;
            }
        }
        return null;
    }

    private int getTargetResourceIndex() {
        return StealingCreation.getTargetResourceIndexForStyle(bot(), gatherStyle);
    }

    private String getTierName(int index) {
        return index < 0 ? "none" : "t" + (index + 1);
    }

    private boolean routeToResourceArea(int maxResourceIndex) {
        WorldTile resourceTile = StealingCreation.getNearestResourceTileForStyle(bot(), gatherStyle, maxResourceIndex);
        if (resourceTile == null && maxResourceIndex < 4) {
            resourceTile = StealingCreation.getNearestResourceTileForStyle(bot(), gatherStyle, 4);
        }
        if (resourceTile == null) {
            return false;
        }
        return routeToResourceTile(resourceTile, "routing to " + getTierName(maxResourceIndex) + " resource area");
    }

    private boolean routeToResourceTile(WorldTile resourceTile, String reason) {
        if (resourceTile == null) {
            return false;
        }
        state = State.GATHER;
        lastAction = reason == null ? "routing to resource" : reason;
        busyTicks = 0;
        processRouteAttempts = 0;
        delay(personality.reactionDelay(3, 6));
        boolean routed = api().routeNear(resourceTile, RESOURCE_ROUTE_RADIUS) || api().walkNear(resourceTile, 3, 35);
        if (!routed) {
            memory.rememberFailedRoute(resourceTile);
        }
        return routed;
    }

    private boolean maybeIdleBanter() {
        if (!personality.rollIdle()) {
            return false;
        }
        state = State.IDLE;
        lastAction = "idle";
        // Rare longer micro-AFK for chatty/rookie bots - typed something then
        // disappeared for a few ticks before resuming. Limited to non-fighters.
        boolean afkRoll = role != Role.FIGHTER && personality.rollIdle()
                && (personality.getArchetype() == BotPersonality.Archetype.SOCIAL
                        || personality.getArchetype() == BotPersonality.Archetype.ROOKIE)
                && ThreadLocalRandom.current().nextInt(40) == 0;
        if (afkRoll) {
            if (personality.rollChat()) {
                api().forceTalk(BotChat.pick(BotChat.AFK_QUICK));
                sinceLastChatTicks = 0;
            }
            lastAction = "afk";
            delay(personality.reactionDelay(8, 16));
            return true;
        }
        if (personality.rollChat() && sinceLastChatTicks > 18) {
            api().forceTalk(BotChat.pick(BotChat.IDLE_BANTER));
            sinceLastChatTicks = 0;
        }
        api().walkNear(bot(), 2);
        delay(personality.reactionDelay(3, 7));
        return true;
    }

    private void recover() {
        state = State.RECOVER;
        busyTicks = 0;
        processRouteAttempts = 0;
        if (role != Role.FIGHTER && bot().getInventory().hasFreeSlots()
                && routeToResourceArea(getTargetResourceIndex())) {
            return;
        }
        lastAction = role == Role.FIGHTER ? "patrolling" : "wandering";
        WorldTile center = role == Role.FIGHTER ? StealingCreation.getGameMapCenterTile() : bot();
        api().walkTo(StealingCreation.getRandomGameMapTileNear(center, role == Role.FIGHTER ? 20 : 6));
        delay(personality.reactionDelay(3, 7));
    }

    private boolean waitForCurrentWork() {
        Action action = bot().getActionManager().getAction();
        if (action != null) {
            if (state == State.FIGHT && shouldClearFightAction(action)) {
                clearFightAction(action);
                return false;
            }
            actionTicks++;
            busyTicks = 0;
            lastAction = "working";
            delay(2);
            return true;
        }
        actionTicks = 0;
        if (bot().getRouteEvent() == null && !bot().hasWalkSteps()) {
            busyTicks = 0;
            lastX = bot().getX();
            lastY = bot().getY();
            return false;
        }
        if (bot().getX() == lastX && bot().getY() == lastY) {
            busyTicks++;
        } else {
            busyTicks = 0;
            lastX = bot().getX();
            lastY = bot().getY();
        }
        if (busyTicks > 8) {
            bot().setRouteEvent(null);
            bot().resetWalkSteps();
            busyTicks = 0;
            if (state == State.PROCESS) {
                processRouteAttempts++;
                lastAction = "cleared stuck kiln route";
                return false;
            }
            lastAction = "cleared stuck route";
            return false;
        }
        lastAction = bot().getRouteEvent() != null ? "routing" : "walking";
        delay(2);
        return true;
    }

    private boolean shouldClearFightAction(Action action) {
        if (!(action instanceof PlayerCombat)) {
            return actionTicks > STALE_FIGHT_ACTION_TICKS;
        }
        Entity target = ((PlayerCombat) action).getTarget();
        if (!(target instanceof Player)) {
            return actionTicks > STALE_FIGHT_ACTION_TICKS;
        }
        Player targetPlayer = (Player) target;
        if (!isAttackableEnemy(targetPlayer) || StealingCreation.isNearAnyBase(bot(), BASE_COMBAT_PADDING)) {
            return true;
        }
        return actionTicks > STALE_FIGHT_ACTION_TICKS && !bot().withinDistance(targetPlayer, 2);
    }

    private void clearFightAction(Action action) {
        Entity target = action instanceof PlayerCombat ? ((PlayerCombat) action).getTarget() : null;
        boolean killed = target instanceof Player && (target.isDead() || target.hasFinished());
        bot().getActionManager().forceStop();
        bot().setTarget(null);
        bot().setNextFaceEntity(null);
        bot().setAttackingDelay(0);
        if (target != null && target.getAttackedBy() == bot()) {
            target.setAttackedBy(null);
            target.setAttackedByDelay(0);
        }
        bot().setRouteEvent(null);
        bot().resetWalkSteps();
        actionTicks = 0;
        busyTicks = 0;
        state = State.RECOVER;
        lastAction = "cleared stalled fight";
        if (killed) {
            recordKillTaunt();
        }
        delay(2);
    }

    private void recordKillTaunt() {
        if (personality.rollChat() && sinceLastChatTicks > 6) {
            api().forceTalk(BotChat.pick(BotChat.KILL_TAUNT));
            sinceLastChatTicks = 0;
        }
    }

    /**
     * Records who just defeated this bot so it avoids them on respawn for a
     * short cooldown. Also fires a death-reaction chat line.
     */
    public void observeDeathBy(Player killer) {
        if (killer == null) {
            return;
        }
        lastKillerName = killer.getDisplayName();
        lastKillerAvoidTicks = LAST_KILLER_FORGET_TICKS;
        memory.rememberDeathBy(killer);
        if (teamMemory != null) {
            teamMemory.reportDanger(bot(), killer, TEAM_CALL_TICKS);
        }
        if (personality.rollChat()) {
            api().forceTalk(BotChat.pick(BotChat.DEATH_REACTION));
            sinceLastChatTicks = 0;
        }
    }

    private static final class GoalChoice {
        private final Goal goal;
        private final int score;
        private final Player target;
        private final WorldObject object;
        private final WorldTile tile;
        private final String reason;

        private GoalChoice(Goal goal, int score, Player target, WorldObject object, WorldTile tile, String reason) {
            this.goal = goal;
            this.score = score;
            this.target = target;
            this.object = object;
            this.tile = tile == null ? null : new WorldTile(tile);
            this.reason = reason;
        }

        private static GoalChoice of(Goal goal, int score, String reason) {
            return new GoalChoice(goal, score, null, null, null, reason);
        }

        private static GoalChoice forPlayer(Goal goal, int score, Player target, String reason) {
            return new GoalChoice(goal, score, target, null, null, reason);
        }

        private static GoalChoice forObject(Goal goal, int score, WorldObject object, String reason) {
            return new GoalChoice(goal, score, null, object, null, reason);
        }

        private static GoalChoice forTile(Goal goal, int score, WorldTile tile, String reason) {
            return new GoalChoice(goal, score, null, null, tile, reason);
        }
    }

    private static final class TargetScore {
        private final Player target;
        private final int score;

        private TargetScore(Player target, int score) {
            this.target = target;
            this.score = score;
        }
    }

    private String getBusyState() {
        if (bot().getActionManager().getAction() != null) {
            return "action";
        }
        if (bot().getRouteEvent() != null) {
            return "route";
        }
        if (bot().hasWalkSteps()) {
            return "walk";
        }
        return "idle";
    }

    private boolean shouldPickpocket(int clayAmount, int inventoryValue, TargetScore target) {
        if (target == null || target.target == null || !bot().getInventory().hasFreeSlots()) {
            return false;
        }
        if (StealingCreation.hasToolUpgradeClayForBot(bot(), gatherStyle)) {
            return false;
        }
        if (inventoryValue >= 260 && role != Role.FIGHTER) {
            return false;
        }
        if (clayAmount > 0 && assignment != Assignment.ANTI_CARRIER && assignment != Assignment.SABOTEUR) {
            return false;
        }
        if (assignment == Assignment.ANTI_CARRIER || assignment == Assignment.SABOTEUR
                || assignment == Assignment.HARASSER) {
            return true;
        }
        if (assignment == Assignment.OPPORTUNIST) {
            return target.score > 280 || ThreadLocalRandom.current().nextDouble() < personality.getAggression();
        }
        return role == Role.FIGHTER && target.score > 380
                && ThreadLocalRandom.current().nextDouble() < Math.max(0.35, personality.getAggression());
    }

    private boolean shouldFight(int clayAmount, int inventoryValue) {
        if (role == Role.FIGHTER) {
            return true;
        }
        if (role == Role.GATHERER || clayAmount > 0 || inventoryValue > 0) {
            return false;
        }
        double aggression = personality.getAggression();
        if (assignment == Assignment.ANTI_CARRIER || assignment == Assignment.DEFENDER
                || assignment == Assignment.HARASSER || assignment == Assignment.SABOTEUR) {
            aggression = Math.max(aggression, 0.65);
        }
        return ThreadLocalRandom.current().nextDouble() < aggression;
    }

    private boolean shouldProcess(int clayAmount) {
        return StealingCreation.hasToolUpgradeClayForBot(bot(), gatherStyle) || !bot().getInventory().hasFreeSlots();
    }

    private boolean shouldDeposit(int clayAmount) {
        if (StealingCreation.getInventoryScoreValue(bot()) <= 0) {
            return false;
        }
        if (StealingCreation.hasToolUpgradeClayForBot(bot(), gatherStyle)) {
            return false;
        }
        int barrierCount = StealingCreation.getBestBarrierItemCountForBot(bot());
        if (assignment == Assignment.BARRIER_BUILDER && barrierCount > 0 && barrierCount < 4
                && StealingCreation.getSacredClayAmount(bot()) > 0) {
            return false;
        }
        if (!bot().getInventory().hasFreeSlots()) {
            return true;
        }
        int toolIndex = StealingCreation.getBestToolIndexForStyle(bot(), gatherStyle);
        if (toolIndex < 1) {
            return false;
        }
        // Greed shifts thresholds: hoarders deposit later, cautious bots earlier.
        int clayThreshold = (int) Math.round(24 + (personality.getGreed() - 0.5) * 16);
        int processedThreshold = (int) Math.round(6 + (personality.getGreed() - 0.5) * 4);
        clayThreshold = Math.max(8, clayThreshold);
        processedThreshold = Math.max(2, processedThreshold);
        return clayAmount >= clayThreshold || processedClay >= processedThreshold;
    }

    private int pickInitialGatherStyle() {
        int seed = Math.abs(bot().getUsername().hashCode());
        if (personality.misclicks() && ThreadLocalRandom.current().nextInt(3) == 0) {
            return ThreadLocalRandom.current().nextInt(4);
        }
        if (assignment == Assignment.T5_SPECIALIST || assignment == Assignment.BARRIER_BUILDER) {
            return (seed + 1) % 4;
        }
        return seed % 4;
    }

    /**
     * Updates gatherStyle and re-grades the affected gather skills so that the
     * new primary skill is at primary-skill-level range and the old primary
     * isn't artificially boosted any more.
     */
    private void setGatherStyle(int newStyle) {
        if (gatherStyle == newStyle) {
            return;
        }
        gatherStyle = newStyle;
        ensurePrimaryGatherLevel();
    }

    /**
     * Ensures the gather skill matching the current style is high enough to
     * harvest the bot's role-appropriate target tier. Without this, switching
     * style mid-match could leave the bot unable to mine/chop/fish at higher
     * tiers because the new "primary" was rolled as a secondary skill at spawn.
     */
    private void ensurePrimaryGatherLevel() {
        int primarySkill = getPrimaryGatherSkillId();
        if (primarySkill < 0) {
            return;
        }
        int desired = getPrimarySkillLevel();
        if (bot().getSkills().getLevelForXp(primarySkill) < desired) {
            bot().getSkills().set(primarySkill, desired);
            bot().getSkills().setXp(primarySkill, Skills.getXPForLevel(primarySkill, desired));
        }
    }

    private int getPrimaryGatherSkillId() {
        switch (Math.abs(gatherStyle) % 4) {
            case 0:
                return Skills.MINING;
            case 1:
                return Skills.FISHING;
            case 2:
                return Skills.HUNTER;
            default:
                return Skills.WOODCUTTING;
        }
    }

    private Assignment pickAssignment() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int roll = random.nextInt(100);
        switch (role) {
            case GATHERER:
                if (roll < 25) {
                    return Assignment.TOOL_UPGRADER;
                }
                if (roll < 50) {
                    return Assignment.T5_SPECIALIST;
                }
                if (roll < 68) {
                    return Assignment.BARRIER_BUILDER;
                }
                if (roll < 85) {
                    return Assignment.RUNNER;
                }
                return Assignment.SCOUT;
            case FIGHTER:
                if (roll < 28) {
                    return Assignment.RUSHER;
                }
                if (roll < 48) {
                    return Assignment.DEFENDER;
                }
                if (roll < 70) {
                    return Assignment.ANTI_CARRIER;
                }
                if (roll < 86) {
                    return Assignment.SABOTEUR;
                }
                return Assignment.HARASSER;
            case HYBRID:
            default:
                if (personality.getArchetype() == BotPersonality.Archetype.GRINDER && roll < 55) {
                    return Assignment.RUNNER;
                }
                if (personality.getArchetype() == BotPersonality.Archetype.RUSHER && roll < 55) {
                    return Assignment.ANTI_CARRIER;
                }
                if (personality.getArchetype() == BotPersonality.Archetype.SOCIAL && roll < 45) {
                    return Assignment.ESCORT;
                }
                if (roll < 18) {
                    return Assignment.BARRIER_BUILDER;
                }
                if (roll < 35) {
                    return Assignment.ESCORT;
                }
                if (roll < 52) {
                    return Assignment.ANTI_CARRIER;
                }
                if (roll < 68) {
                    return Assignment.SCOUT;
                }
                if (roll < 82) {
                    return Assignment.TOOL_UPGRADER;
                }
                return Assignment.OPPORTUNIST;
        }
    }

    private void prepareStats() {
        for (int skill : SC_BOT_SKILLS) {
            int level = getProfileLevel(skill);
            bot().getSkills().set(skill, level);
            bot().getSkills().setXp(skill, Skills.getXPForLevel(skill, level));
        }
        bot().setHitpoints(bot().getMaxHitpoints());
        bot().refreshHitPoints();
        bot().getAppearence().generateAppearenceData();
    }

    private int getProfileLevel(int skill) {
        int level;
        if (isCombatSkill(skill)) {
            level = getCombatProfileLevel();
        } else if (isPrimaryGatherSkill(skill)) {
            level = getPrimarySkillLevel();
        } else if (isGatherSkill(skill)) {
            level = getSecondarySkillLevel();
        } else {
            level = getSupportSkillLevel();
        }
        if (assignment == Assignment.BARRIER_BUILDER && skill == Skills.CONSTRUCTION) {
            level = Math.max(level, randomLevel(75, 99));
        }
        if ((role == Role.FIGHTER || assignment == Assignment.ESCORT) && skill == Skills.COOKING) {
            level = Math.max(level, randomLevel(55, 90));
        }
        if (assignment == Assignment.T5_SPECIALIST && isPrimaryGatherSkill(skill)) {
            level = Math.max(level, randomLevel(88, 99));
        }
        if (skill == Skills.THIEVING) {
            if (assignment == Assignment.ANTI_CARRIER || assignment == Assignment.SABOTEUR
                    || assignment == Assignment.HARASSER) {
                level = Math.max(level, randomLevel(78, 99));
            } else if (role == Role.FIGHTER || assignment == Assignment.OPPORTUNIST) {
                level = Math.max(level, randomLevel(60, 92));
            }
        }
        return clampLevel(level);
    }

    private int getCombatProfileLevel() {
        switch (role) {
            case FIGHTER:
                return randomLevel(78, 99);
            case GATHERER:
                return randomLevel(42, personality.getArchetype() == BotPersonality.Archetype.VETERAN ? 86 : 74);
            case HYBRID:
            default:
                return randomLevel(62, 94);
        }
    }

    private int getPrimarySkillLevel() {
        switch (role) {
            case GATHERER:
                return randomLevel(78, 99);
            case FIGHTER:
                return randomLevel(42, 78);
            case HYBRID:
            default:
                return randomLevel(66, 96);
        }
    }

    private int getSecondarySkillLevel() {
        switch (role) {
            case GATHERER:
                return randomLevel(58, 92);
            case FIGHTER:
                return randomLevel(30, 70);
            case HYBRID:
            default:
                return randomLevel(50, 88);
        }
    }

    private int getSupportSkillLevel() {
        switch (personality.getArchetype()) {
            case ROOKIE:
                return randomLevel(35, 72);
            case GRINDER:
                return randomLevel(58, 90);
            case RUSHER:
                return randomLevel(42, 82);
            case VETERAN:
                return randomLevel(70, 99);
            case SOCIAL:
            default:
                return randomLevel(48, 88);
        }
    }

    private boolean isCombatSkill(int skill) {
        return skill == Skills.ATTACK || skill == Skills.STRENGTH || skill == Skills.DEFENCE || skill == Skills.HITPOINTS
                || skill == Skills.RANGE || skill == Skills.MAGIC || skill == Skills.PRAYER || skill == Skills.SUMMONING;
    }

    private boolean isGatherSkill(int skill) {
        return skill == Skills.MINING || skill == Skills.FISHING || skill == Skills.HUNTER || skill == Skills.WOODCUTTING;
    }

    private boolean isPrimaryGatherSkill(int skill) {
        switch (Math.abs(gatherStyle) % 4) {
            case 0:
                return skill == Skills.MINING;
            case 1:
                return skill == Skills.FISHING;
            case 2:
                return skill == Skills.HUNTER;
            default:
                return skill == Skills.WOODCUTTING;
        }
    }

    private int randomLevel(int min, int max) {
        int safeMin = Math.max(1, Math.min(99, min));
        int safeMax = Math.max(safeMin, Math.min(99, max));
        return ThreadLocalRandom.current().nextInt(safeMin, safeMax + 1);
    }

    private int clampLevel(int level) {
        return Math.max(1, Math.min(99, level));
    }
}
