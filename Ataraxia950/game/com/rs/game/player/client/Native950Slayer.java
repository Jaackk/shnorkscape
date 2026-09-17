package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.slayer.SlayerMasterData;
import com.rs.game.player.actions.slayer.SlayerTaskData;
import com.rs.game.player.actions.slayer.TaskSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntUnaryOperator;

/** Native task ownership over the original Slayer task/master tables, without legacy death callbacks. */
public final class Native950Slayer {
    private Native950Slayer() { }
    public static final String TASK = "slayerTask", REMAINING = "slayerRemaining", MASTER = "slayerMaster",
            COMPLETED = "slayerCompleted", STREAK = "slayerStreak", POINTS = "slayerPoints";
    private static final Map<Integer, SlayerTaskData> TASK_CODES = taskCodes();

    private static Map<Integer, SlayerTaskData> taskCodes() {
        Map<Integer, SlayerTaskData> result = new HashMap<>();
        for (SlayerTaskData task : SlayerTaskData.values()) {
            int key = code(task);
            if (key == 0 || result.put(key, task) != null)
                throw new IllegalStateException("Slayer task save-code collision: " + task.name());
        }
        return Collections.unmodifiableMap(result);
    }
    /** Stable enum-name key, independent of declaration order; collisions fail at startup. */
    static int code(SlayerTaskData task) { return task.name().hashCode() & Integer.MAX_VALUE; }
    public static SlayerMasterData master(int npcId) {
        for (SlayerMasterData master : SlayerMasterData.values())
            if (master.getNpcId() == npcId) return master;
        return null;
    }
    public static void verifyCacheBindings() {
        if(Cache.STORE==null||!Cache.isFlatReadOnly())throw new IllegalStateException("Slayer requires selected950 cache");
        for(SlayerMasterData master:SlayerMasterData.values()) {
            int id=master.getNpcId();byte[] raw=Cache.STORE.getIndexes()[18].getFile(id>>>7,id&127);
            if(raw==null||!Native950IdValidity.get().isSafe(Native950IdValidity.Kind.NPC,id)
                    ||!verifiedMaster(id,NPCDefinitions.decodeStrict947(id,raw,null)))
                throw new IllegalStateException("Unavailable950 Slayer master "+id);
        }
    }
    public static boolean masterCandidate(int npcId) { return master(npcId) != null; }
    /** Population hook; callers still retain their normal current-cache identity and clear-tile gates. */
    public static boolean verifiedMaster(int npcId, NPCDefinitions definition) {
        SlayerMasterData master = master(npcId);
        return master != null && definition != null && definition.decodeFailure == null
                && definition.transformTo == null && definition.models != null && definition.models.length > 0
                && definition.size >= 1 && definition.size <= 255
                && master.name().replace('_', ' ').equalsIgnoreCase(definition.name)
                && !definition.hasOption("Attack")
                && hasMasterOption(definition);
    }
    static boolean isTalkOption(String option) {
        return "Talk to".equalsIgnoreCase(option) || "Talk-to".equalsIgnoreCase(option);
    }
    static boolean isAssignmentOption(String option) {
        return "Get task".equalsIgnoreCase(option) || "Get-task".equalsIgnoreCase(option) || "Assignment".equalsIgnoreCase(option);
    }
    private static boolean hasMasterOption(NPCDefinitions definition) {
        if (definition.menuOptions != null) for (String option : definition.menuOptions)
            if (isTalkOption(option) || isAssignmentOption(option)) return true;
        return false;
    }
    static boolean liveMaster(NPC npc) {
        if (npc == null || !npc.isNative950() || npc.hasFinished() || npc.isDead() || npc.isCantInteract()
                || !masterCandidate(npc.getId()) || Cache.STORE == null || !Cache.isFlatReadOnly()
                || !Native950IdValidity.get().isSafe(Native950IdValidity.Kind.NPC, npc.getId())) return false;
        try {
            byte[] raw = Cache.STORE.getIndexes()[18].getFile(npc.getId() >>> 7, npc.getId() & 127);
            return raw != null && verifiedMaster(npc.getId(), NPCDefinitions.decodeStrict947(npc.getId(), raw, null));
        } catch (RuntimeException invalid) { return false; }
    }
    public static boolean handles(NPC npc, int option) {
        if (!liveMaster(npc)) return false;
        String label = npc.getNative950MenuOption(option);
        return isTalkOption(label) || isAssignmentOption(label) || "Rewards".equalsIgnoreCase(label);
    }
    /** Call after the normal visible-NPC, controller and route-arrival checks. */
    public static void interact(Player player, NPC npc, int option) {
        if (player == null || !player.isNative950() || !handles(npc, option) || !nearMaster(player, npc)) return;
        boolean quick = isAssignmentOption(npc.getNative950MenuOption(option));
        player.getDialogueManager().startDialogue(new Native950SlayerDialogue(npc, quick));
    }
    static boolean nearMaster(Player player, NPC npc) {
        return player != null && !player.hasFinished() && !player.isDead() && !player.isLocked()
                && npc != null && player.getPlane() == npc.getPlane() && player.withinDistance(npc, 3)
                && npc.getIndex() >= 0 && World.getNPCs().get(npc.getIndex()) == npc && liveMaster(npc);
    }

    /** A task requires at least one real, already registered native combat spawn. */
    static Set<String> populatedNames() { return populatedNames(120); }
    static Set<String> populatedNames(int slayerLevel) {
        Set<String> names = new HashSet<>();
        for (NPC npc : World.getNPCs()) {
            if (npc == null || !npc.isNative950() || npc.hasFinished() || npc.isCantInteract()) continue;
            Native950NpcCombatProfile profile = npc.getNative950CombatProfile();
            if (profile != null && profile.npcId == npc.getId() && profile.name != null
                    && requiredLevel(profile.npcId, profile.name) <= slayerLevel)
                names.add(profile.name.toLowerCase(Locale.ROOT));
        }
        return names;
    }
    /** Explicitly defer tasks whose unlocks, finishing items or special defences have not been ported. */
    static boolean baselineTask(SlayerTaskData task) {
        switch (task) {
            case BANSHEES: case CAVE_SLIME: case DESERT_LIZARDS: case GELATINOUS_ABOMINATION:
            case COCKATRICES: case ROCK_SLUGS: case WALL_BEASTS: case ABERRANT_SPECTRES:
            case BASILISKS: case DUST_DEVILS: case HARPIE_BUG_SWARMS: case KILLERWATTS:
            case TUROTH: case KURASKS: case CAVE_HORROR: case MOLANISKS: case AVIANSIE:
            case ICE_STRYKEWYRMS: case AQUANITES: case EDIMMUS: case GLACORS:
            case KALGERION_DEMONS: case MUSPAH: case NIHIL: case NIGHTMARE_CREATURES:
            case TORMENTED_DEMONS: case TORMENTED_DEMON: case VOLCANIC_CREATURES:
            case WARPED_TERRORBIRDS: case SKELETAL_WYVERNS: case LIVING_WYVERNS:
            case GARGOYLES: case CORRUPTED_CREATURES: case SOUL_DEVOURERS:
            case DESERT_STRYKEWYRMS: case JUNGLE_STRYKEWYRMS: case LAVA_STRYKEWYRMS:
                return false;
            default: return true;
        }
    }
    static List<SlayerTaskData> eligible(SlayerMasterData master, int slayerLevel, int combatLevel,
            Set<String> populatedNames) {
        List<SlayerTaskData> result = new ArrayList<>();
        if (master == null || slayerLevel < master.getSlayerRequirement()
                || combatLevel < master.getCombatRequirement() || populatedNames == null) return result;
        for (SlayerTaskData task : SlayerTaskData.values()) {
            TaskSet set = task.getCertainTaskSet(master.ordinal());
            if (!baselineTask(task) || set == null || set.getWeight() <= 0 || set.getMinimumAmount() <= 0
                    || set.getMaximumAmount() < set.getMinimumAmount() || set.getMaximumAmount() > 10000
                    || slayerLevel < task.getSlayerRequirement()) continue;
            for (String name : task.getMonsters()) {
                if (populatedNames.contains(name.toLowerCase(Locale.ROOT))) { result.add(task); break; }
            }
        }
        return result;
    }
    static boolean assign(State state, SlayerMasterData master, int slayerLevel, int combatLevel,
            Set<String> names, IntUnaryOperator random) {
        if (state == null || state.hasTask()) return false;
        List<SlayerTaskData> tasks = eligible(master, slayerLevel, combatLevel, names);
        int total = 0;
        for (SlayerTaskData task : tasks) total = Math.addExact(total, task.getCertainTaskSet(master.ordinal()).getWeight());
        if (total == 0) return false;
        int roll = boundedRandom(random, total);
        for (SlayerTaskData task : tasks) {
            TaskSet set = task.getCertainTaskSet(master.ordinal());
            if (roll < set.getWeight()) {
                int amount = set.getMinimumAmount() + boundedRandom(random, set.getMaximumAmount() - set.getMinimumAmount() + 1);
                state.task = task; state.master = master; state.remaining = amount;
                return true;
            }
            roll -= set.getWeight();
        }
        throw new IllegalStateException("Slayer weighted selection exhausted");
    }
    private static int boundedRandom(IntUnaryOperator random, int bound) {
        int value = random.applyAsInt(bound);
        if (value < 0 || value >= bound) throw new IllegalArgumentException("Slayer random value exceeds its bound");
        return value;
    }
    static String assign(Player player, NPC npc) {
        if (!nearMaster(player, npc)) return "Please speak to the Slayer master nearby.";
        State state = player.getNative950Slayer();
        if (state.hasTask()) return state.description();
        SlayerMasterData master = master(npc.getId());
        int slayer = player.getSkills().getLevelForXp(Skills.SLAYER);
        int combat = player.getSkills().getCombatLevelWithSummoning();
        if (slayer < master.getSlayerRequirement() || combat < master.getCombatRequirement())
            return "You need Slayer level " + master.getSlayerRequirement() + " and combat level "
                    + master.getCombatRequirement() + " to receive my assignments.";
        // The original Morvran dialogue also requires access to Prifddinas.
        if (master == SlayerMasterData.MORVRAN && !player.hasAccessToPrifddinas())
            return "You need access to Prifddinas to receive Morvran's assignments.";
        if (!assign(state, master, slayer, combat, populatedNames(slayer), bound -> ThreadLocalRandom.current().nextInt(bound)))
            return "I have no suitable creatures available in the currently populated world. Try Turael near Lumbridge.";
        return state.description();
    }

    static String cancelTask(Player player,NPC npc) {
        if(!nearMaster(player,npc))return "Speak to a Slayer master nearby.";
        State state=player.getNative950Slayer();
        if(!state.hasTask())return "You have no task to cancel.";
        if(!state.cancelForPoints())return "Cancelling a task costs 30 Slayer points.";
        return "Your task is cancelled for 30 Slayer points. Your task streak is preserved.";
    }
    static String buyExperience(Player player,NPC npc) {
        if(!nearMaster(player,npc))return "Speak to a Slayer master nearby.";
        if(player.getSkills().getLevelForXp(Skills.SLAYER)<35)return "This 10,000 XP reward requires Slayer level 35.";
        if(player.getSkills().getXp(Skills.SLAYER)>=Native950Save.MAX_XP)return "You have reached the Slayer XP limit.";
        if(!player.getNative950Slayer().spend(400))return "This reward costs 400 Slayer points.";
        player.getSkills().addXp(Skills.SLAYER,10000);
        return "You exchange 400 Slayer points for 10,000 base Slayer XP.";
    }
    /** Original master base rates, with the RuneScape fifth-task gate and 5x/15x milestones. */
    static int taskPoints(SlayerMasterData master,int streak) {
        if(master==null||master==SlayerMasterData.TURAEL||streak<5)return 0;
        return master.getPointsPerTask()*(streak%50==0?15:streak%10==0?5:1);
    }

    /** Read-only native attack gate; no old PlayerCombat/Slayer manager callbacks. */
    public static String attackRefusal(Player player, NPC npc) {
        if (player == null || npc == null || !player.isNative950() || !npc.isNative950()) return null;
        Native950NpcCombatProfile profile = npc.getNative950CombatProfile();
        if (profile == null || profile.npcId != npc.getId()) return null;
        int required = requiredLevel(profile.npcId, profile.name);
        return player.getSkills().getLevelForXp(Skills.SLAYER) < required
                ? "You need Slayer level " + required + " to fight this creature." : null;
    }
    static int requiredLevel(int identitySafeNpcId, String cacheName) {
        int level = Math.max(1, com.rs.game.player.content.Combat.getSlayerLevelForNPC(identitySafeNpcId));
        if (cacheName == null) return level;
        for (SlayerTaskData task : SlayerTaskData.values())
            for (String name : task.getMonsters())
                if (name.equalsIgnoreCase(cacheName)) { level = Math.max(level, task.getSlayerRequirement()); break; }
        return level;
    }
    /** Invoke once per committed death with the native melee damage-credit winner, never per hit. */
    public static void onDeath(Player player, NPC npc) {
        if (player == null || !player.isNative950() || player.hasFinished() || npc == null || !npc.isNative950()
                || !npc.isDead() || !npc.isNative950DeathVisible() || npc.hasFinished()
                || npc.getIndex() < 0 || World.getNPCs().get(npc.getIndex()) != npc) return;
        Native950NpcCombatProfile profile = npc.getNative950CombatProfile();
        State state = player.getNative950Slayer();
        if (profile == null || profile.npcId != npc.getId() || !state.hasTask() || attackRefusal(player, npc) != null
                || player.getSkills().getLevelForXp(Skills.SLAYER) < state.task.getSlayerRequirement()) return;
        Kill result = state.killed(profile.name, profile.hp);
        if (!result.matched) return;
        // The original910 death reward uses integer engine HP/10; Skills owns rates, XP drops and save arrays.
        if (result.baseXp > 0) player.getSkills().addXp(Skills.SLAYER, result.baseXp);
        if (result.completed) player.sendMessage("You have finished your Slayer task. Speak to a Slayer master for another."
                + (result.points > 0 ? " You receive " + result.points + " Slayer points." : ""));
        else if (state.remaining == 1 || state.remaining == 5 || state.remaining % 10 == 0)
            player.sendMessage(state.description());
    }

    public static final class State {
        private SlayerTaskData task;
        private SlayerMasterData master;
        private int remaining, completed, streak, points;
        public State() { }
        public boolean hasTask() { return task != null && remaining > 0 && master != null; }
        public int remaining() { return remaining; }
        public int completed() { return completed; }
        public int streak() { return streak; }
        public int points() { return points; }
        boolean spend(int cost) { if(cost<1||points<cost)return false;points-=cost;return true; }
        boolean cancelForPoints() {
            if(!hasTask()||!spend(30))return false;
            task=null;remaining=0;return true;
        }
        public String description() {
            return hasTask() ? "Your Slayer task is to kill " + remaining + " " + task.toString().toLowerCase(Locale.ROOT) + "."
                    : "You have no Slayer task. Speak to a Slayer master for an assignment.";
        }
        public void writeSettings(Map<String, Integer> settings) {
            settings.put(TASK, hasTask() ? code(task) : 0);
            settings.put(REMAINING, hasTask() ? remaining : 0);
            settings.put(MASTER, master == null ? 0 : master.getNpcId());
            settings.put(COMPLETED, completed); settings.put(STREAK, streak); settings.put(POINTS, points);
        }
        /** Packet-free restoration. Invalid active tuples clear together; unrelated counters remain bounded. */
        public void restore(Map<String, Integer> settings) {
            task = null; master = null; remaining = 0;
            completed = nonnegative(settings, COMPLETED); streak = Math.min(nonnegative(settings, STREAK), completed);
            points = nonnegative(settings, POINTS);
            SlayerMasterData restoredMaster = Native950Slayer.master(nonnegative(settings, MASTER));
            SlayerTaskData restoredTask = TASK_CODES.get(nonnegative(settings, TASK));
            int count = nonnegative(settings, REMAINING);
            if (restoredMaster == null) return;
            master = restoredMaster;
            TaskSet set = restoredTask == null ? null : restoredTask.getCertainTaskSet(master.ordinal());
            if (set != null && baselineTask(restoredTask) && set.getWeight() > 0 && count > 0
                    && count <= set.getMaximumAmount() && count <= 10000) { task = restoredTask; remaining = count; }
        }
        private static int nonnegative(Map<String, Integer> settings, String key) {
            Integer value = settings == null ? null : settings.get(key);
            return value == null || value < 0 ? 0 : value;
        }
        Kill killed(String cacheName, int engineHp) {
            if (!hasTask() || cacheName == null || engineHp < 1 || engineHp > 100000000) return Kill.NONE;
            boolean matched = false;
            for (String candidate : task.getMonsters()) if (candidate.equalsIgnoreCase(cacheName)) { matched = true; break; }
            if (!matched) return Kill.NONE;
            remaining--;
            int earned = 0;
            boolean finished = remaining == 0;
            if (finished) {
                task = null; completed = increment(completed);
                if (master != SlayerMasterData.TURAEL) {
                    streak = increment(streak);
                    earned = taskPoints(master,streak);
                    earned = Math.min(earned, Integer.MAX_VALUE - points);
                    points += earned;
                }
            }
            return new Kill(true, finished, engineHp / 10, earned);
        }
        private static int increment(int value) { return value == Integer.MAX_VALUE ? value : value + 1; }
    }
    static final class Kill {
        static final Kill NONE = new Kill(false, false, 0, 0);
        final boolean matched, completed;
        final int baseXp, points;
        Kill(boolean matched, boolean completed, int baseXp, int points) {
            this.matched = matched; this.completed = completed; this.baseXp = baseXp; this.points = points;
        }
    }
}
