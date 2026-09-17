package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

/** Four reserved original frozen chambers with native combat and one completion reward per run. */
public final class Native950Dungeoneering {
    private Native950Dungeoneering() { }
    public static final int RING = 15707, TUTOR = 9712, ENTRANCE = 48496, EXIT = 51156, GUARDIAN = 88;
    public static final int BASE_XP = 150, COMPLETION_TOKENS = 15, OBJECTIVES = 3;
    private static final Object STATE_KEY = new Object();
    // Original DungeonConstants.START_ROOMS: chunk14,624/626/630/632, frozen type0.
    static final int[] ROOM_Y = {4992, 5008, 5040, 5056};
    private static final Player[] ROOM_OWNERS = new Player[ROOM_Y.length];
    private static final Map<NPC, Player> GUARDIAN_OWNERS = new IdentityHashMap<>();
    private static final int[][] GUARDIAN_OFFSETS = {{3, 2}, {7, 2}, {3, 9}};

    public static State state(Player player) {
        if (player == null || !player.isNative950()) throw new IllegalArgumentException("Native950 player required");
        Object prior = player.getTemporaryAttributtes().get(STATE_KEY);
        if (prior instanceof State) return (State) prior;
        State created = new State();
        Object raced = player.getTemporaryAttributtes().putIfAbsent(STATE_KEY, created);
        return raced instanceof State ? (State) raced : created;
    }
    public static int tokens(Player player) { return state(player).tokens; }
    public static int completed(Player player) { return state(player).completed; }
    public static void verifyCacheBindings() { Native950DungeoneeringAssets.verifyCacheBindings(); }
    public static boolean handlesItem(int id, String option) {
        return id == RING && ("Open party interface".equals(option) || "Teleport to Daemonheim".equals(option));
    }
    public static boolean item(Player player, int itemId, String option) {
        if (!handlesItem(itemId, option)) return false;
        if (player != null && player.isNative950()) for (int slot = 0; slot < 28; slot++) {
            Item item = player.getInventory().getItem(slot);
            if (item != null && item.getId() == itemId) return handleItem(player, slot, itemId, option);
        }
        return true;
    }
    public static boolean object(Player player, WorldObject object, int option) { return handleObject(player, object, option); }
    public static boolean interrupted(Player player) { State s = state(player); return s.run != null || s.recover; }
    /** Packet-free hydration; the world tick returns interrupted characters before admitting another run. */
    public static void restoreProgress(Player player, int tokens, int completed, boolean interrupted) {
        State s = state(player);
        if (s.run != null) throw new IllegalStateException("Cannot restore Dungeoneering over a live run");
        s.tokens = Math.max(0, tokens); s.completed = Math.max(0, completed); s.recover = interrupted;
    }
    public static WorldTile outside() { return new WorldTile(3452, 3718, 0); }
    public static WorldTile ringDestination() { return new WorldTile(3448, 3699, 0); }
    static WorldTile entry(int room) { return new WorldTile(119, ROOM_Y[room] + 7, 0); }
    static boolean inRoom(WorldTile tile, int room) {
        return tile != null && room >= 0 && room < ROOM_Y.length && tile.getPlane() == 0
                && tile.getX() >= 112 && tile.getX() < 128
                && tile.getY() >= ROOM_Y[room] && tile.getY() < ROOM_Y[room] + 16;
    }
    static boolean inAnyRoom(WorldTile tile) { for (int i = 0; i < ROOM_Y.length; i++) if (inRoom(tile, i)) return true; return false; }
    static boolean lobby(WorldTile p) { return p.getPlane() == 0 && p.getX() >= 3438 && p.getX() <= 3470 && p.getY() >= 3690 && p.getY() <= 3730; }
    static boolean usable(Player p) { return p != null && p.isNative950() && p.isActive() && !p.hasFinished() && !p.isDead() && !p.isLocked(); }

    public static boolean handlesObject(WorldObject object, int option) {
        if (object == null || option != 1 || object.getPlane() != 0) return false;
        if (object.getId() == ENTRANCE) return object.getY() == 3722 && (object.getX() == 3445 || object.getX() == 3454);
        if (object.getId() != EXIT || object.getX() != 112) return false;
        for (int y : ROOM_Y) if (object.getY() == y + 2) return true;
        return false;
    }
    /** Invoke after the normal visible-object and route-arrival validation. */
    public static boolean handleObject(Player p, WorldObject object, int option) {
        if (!handlesObject(object, option)) return false;
        if (!usable(p) || !Native950Mining.current(object) || !Native950Mining.inReach(p, object)) return true;
        Native950DungeoneeringAssets.verifyCacheBindings();
        if (object.getId() == ENTRANCE) p.getDialogueManager().startDialogue(new Native950DungeoneeringDialogue());
        else exit(p, true);
        return true;
    }
    public static boolean handlesNpc(NPC npc, int option) {
        return npc != null && npc.isNative950() && npc.getId() == TUTOR && "Talk to".equals(npc.getNative950MenuOption(option));
    }
    public static void interactNpc(Player p, NPC npc, int option) {
        if (!usable(p) || !handlesNpc(npc, option) || npc.hasFinished() || npc.isDead()
                || p.getPlane() != npc.getPlane() || !p.withinDistance(npc, 3) || !World.containsNPC(npc)) return;
        Native950DungeoneeringAssets.verifyCacheBindings();
        NPCDefinitions d = NPCDefinitions.decodeStrict947(TUTOR, Cache.STORE.getIndexes()[18].getFile(TUTOR >>> 7, TUTOR & 127), null);
        if (!Native950DungeoneeringAssets.verifiedTutor(TUTOR, d)) return;
        if (!p.getInventory().containsItem(RING, 1) && p.getEquipment().getRingId() != RING) {
            if (!p.getInventory().hasFreeSlots()) { p.sendMessage("Make room in your backpack and I will give you a ring of kinship."); return; }
            if (!p.getInventory().addItem(RING, 1)) return;
            p.sendMessage("The tutor gives you a ring of kinship. Bring your equipment and food for a solo dungeon.");
        }
        p.getDialogueManager().startDialogue(new Native950DungeoneeringDialogue());
    }
    /** Labels come from the verified950 inventory menu; the clicked slot is checked again here. */
    public static boolean handleItem(Player p, int slot, int itemId, String option) {
        if (itemId != RING || !("Open party interface".equals(option) || "Teleport to Daemonheim".equals(option))) return false;
        if (!usable(p) || slot < 0 || slot >= 28) return true;
        Item live = p.getInventory().getItem(slot);
        if (live == null || live.getId() != RING) return true;
        Native950DungeoneeringAssets.verifyCacheBindings();
        if ("Teleport to Daemonheim".equals(option)) travel(p);
        else p.getDialogueManager().startDialogue(new Native950DungeoneeringDialogue());
        return true;
    }
    static String start(Player p) { return start(p,false); }
    static String startChallenge(Player p) { return start(p,true); }
    private static String start(Player p,boolean challenge) {
        if (!usable(p) || !lobby(p)) return "Travel to Daemonheim and speak to the tutor or use a dungeon entrance.";
        State s = state(p);
        if (s.run != null || s.recover) return "Finish or leave your current dungeon first.";
        if (p.getNative950Combat() == null) return "Dungeoneering combat is not available yet.";
        int dungeonLevel=p.getSkills().getLevelForXp(Skills.DUNGEONEERING);
        if(challenge&&dungeonLevel<20)return "You need Dungeoneering level 20 for the three-wave frozen challenge.";
        Native950DungeoneeringAssets.verifyCacheBindings();
        Native950NpcCombatProfile profile = Native950NpcCombatCatalog.fromRunningCache(GUARDIAN);
        if (profile == null || profile.size != 2 || !"Dungeon rat".equals(profile.name)) return "The dungeon guardians are unavailable in this cache.";
        int room = -1;
        for (int i = 0; i < ROOM_Y.length; i++) if (ROOM_OWNERS[i] == null) { room = i; break; }
        if (room < 0) return "All four solo chambers are occupied. Please try again shortly.";
        verifyRoom(room, profile.size);
        for (Player other : World.getPlayers()) if (other != p && inRoom(other, room)) return "That chamber is still occupied. Please try again shortly.";
        Run run = new Run(room,challenge,dungeonLevel);
        ROOM_OWNERS[room] = p; s.run = run;
        try {
            spawnWave(p,run,profile);
            p.getNative950Combat().stop(p); p.getActionManager().forceStop(); p.setRouteEvent(null); p.resetWalkSteps();
            p.setNextWorldTile(entry(room));
            return (challenge?"Defeat all three waves of dungeon rats":"Defeat the three dungeon rats")
                    +", then climb up the dungeon exit. Completion: "+run.baseXp+" base XP and "+(run.baseXp/10)+" tokens. Your equipment stays with you.";
        } catch (RuntimeException failure) { retire(p, false); throw failure; }
    }
    private static void spawnWave(Player p,Run run,Native950NpcCombatProfile profile) {
        if(profile==null)throw new IllegalStateException("Dungeon guardian profile unavailable");
        for(int[] offset:GUARDIAN_OFFSETS){
            if(run.spawned>=run.objectives.total)break;
            NPC npc=NPC.createNative950(GUARDIAN,new WorldTile(112+offset[0],ROOM_Y[run.room]+offset[1],0),profile.size);
            run.targets.put(npc,run.spawned++);World.addNative950Npc(npc);World.updateEntityRegion(npc);
            Native950World.getInstance().nativeNpcs().add(npc);p.getNative950Combat().register(npc);
            if(!p.getNative950Combat().supports(npc))throw new IllegalStateException("Dungeon guardian combat admission failed");
            GUARDIAN_OWNERS.put(npc,p);
        }
    }
    static void verifyRoom(int room, int size) {
        WorldTile entry = entry(room); World.getRegion(entry.getRegionId(), true);
        if (!World.isFloorFree(0, entry.getX(), entry.getY(), 1)) throw new IllegalStateException("Dungeon entry is blocked");
        for (int[] offset : GUARDIAN_OFFSETS)
            if (!World.isFloorFree(0, 112 + offset[0], ROOM_Y[room] + offset[1], size)) throw new IllegalStateException("Dungeon guardian footprint is blocked");
        WorldObject exit = World.getRegion(entry.getRegionId()).getObjectWithType(0, 112 & 63, (ROOM_Y[room] + 2) & 63, 10);
        if (exit == null || exit.getId() != EXIT) throw new IllegalStateException("Dungeon exit is unavailable");
    }
    public static String attackRefusal(Player p, NPC npc) {
        Player owner = GUARDIAN_OWNERS.get(npc);
        if (owner == null) return null;
        Run run = state(owner).run;
        if (owner != p) return "That guardian belongs to another adventurer's solo dungeon.";
        if (run == null || !inRoom(p, run.room) || !inRoom(npc, run.room)) return "You must be inside your solo chamber to fight its guardians.";
        Integer slot = run.targets.get(npc);
        return slot == null || run.objectives.defeated(slot) ? "You have already defeated that guardian." : null;
    }
    /** Called only by the committed native death callback with its damage-credit winner. */
    public static void onNpcDeath(Player p, NPC npc) {
        if (p == null || GUARDIAN_OWNERS.get(npc) != p || !usable(p) || npc == null || !npc.isDead()
                || !npc.isNative950DeathVisible() || !World.containsNPC(npc)) return;
        Run run = state(p).run;
        if (run == null || !inRoom(p, run.room) || !inRoom(npc, run.room)) return;
        Integer slot = run.targets.get(npc);
        if (slot == null || !run.objectives.kill(slot)) return;
        Native950NpcCombatProfile profile = npc.getNative950CombatProfile();
        run.corpses.put(npc, Math.max(profile.deathTicks, profile.deathAnimationTicks) + 1);
        p.sendMessage(run.objectives.remaining() == 0 ? "All guardians are defeated. Climb up the dungeon exit to finish and claim your reward."
                : "Dungeon guardians remaining: " + run.objectives.remaining() + ".");
    }
    static void travel(Player p) {
        if (!usable(p)) return;
        Native950DungeoneeringAssets.verifyCacheBindings();
        WorldTile target = ringDestination(); World.getRegion(target.getRegionId(), true);
        if (!World.isFloorFree(target.getPlane(), target.getX(), target.getY(), 1)) { p.sendMessage("The Daemonheim arrival point is unavailable."); return; }
        boolean unfinished = state(p).run != null;
        retire(p, false); stop(p); p.setNextWorldTile(target);
        if (unfinished) p.sendMessage("You leave the dungeon without a completion reward.");
    }
    static void exit(Player p, boolean allowReward) {
        State s = state(p); Run run = s.run;
        if (!usable(p) || run == null || !inRoom(p, run.room)) return;
        Reward reward = allowReward ? s.claim() : null;
        retire(p, false); stop(p); p.setNextWorldTile(outside());
        if (reward != null) {
            p.getSkills().addXp(Skills.DUNGEONEERING, reward.xp);
            p.sendMessage("Solo dungeon complete! You receive Dungeoneering experience and " + reward.tokens
                    + " tokens. Total tokens: " + s.tokens + ".");
        } else p.sendMessage("You leave the dungeon without a completion reward.");
    }
    private static void stop(Player p) { if (p.getNative950Combat() != null) p.getNative950Combat().stop(p); p.getActionManager().forceStop(); p.resetWalkSteps(); p.setRouteEvent(null); }
    private static void removeGuardian(NPC npc) {
        GUARDIAN_OWNERS.remove(npc);
        Native950World.getInstance().nativeNpcs().remove(npc);
        if (npc.isNative950() && !npc.hasFinished()) World.removeNative950Npc(npc);
    }
    private static void retire(Player p, boolean recover) {
        State s = state(p); Run run = s.run; s.run = null; s.recover = recover;
        if (run == null) return;
        if (ROOM_OWNERS[run.room] == p) ROOM_OWNERS[run.room] = null;
        for (NPC npc : new ArrayList<>(run.targets.keySet())) removeGuardian(npc);
    }
    /** World-thread housekeeping: retire corpses before respawn and abort a departed/dead run. */
    public static void tick(Player p) {
        if (p == null || !p.isNative950()) return;
        State s = state(p);
        if (s.recover) {
            if (!p.isDead() && !p.hasFinished()) {
                if (inAnyRoom(p)) { stop(p); p.setNextWorldTile(outside()); }
                else if (p.getNextWorldTile() == null) s.recover = false;
            }
            return;
        }
        Run run = s.run; if (run == null) return;
        if (p.isDead() || p.hasFinished()) { onPlayerDeath(p); return; }
        WorldTile pending = p.getNextWorldTile();
        if (pending != null && !inRoom(pending, run.room) || pending == null && !inRoom(p, run.room)) { retire(p, false); return; }
        Iterator<Map.Entry<NPC, Integer>> dead = run.corpses.entrySet().iterator();
        while (dead.hasNext()) {
            Map.Entry<NPC, Integer> entry = dead.next();
            if (entry.getValue() <= 1) { removeGuardian(entry.getKey()); dead.remove(); }
            else entry.setValue(entry.getValue() - 1);
        }
        if(run.spawned<run.objectives.total&&run.corpses.isEmpty()
                &&run.objectives.remaining()==run.objectives.total-run.spawned){
            spawnWave(p,run,Native950NpcCombatCatalog.fromRunningCache(GUARDIAN));
            p.sendMessage("Frozen challenge wave "+(run.spawned/OBJECTIVES)+" of "+(run.objectives.total/OBJECTIVES)+" begins.");
        }
    }
    public static void onPlayerDeath(Player p) { if (p != null && p.isNative950() && state(p).run != null) retire(p, true); }
    /** Before final capture: committed position cannot remain in an abandoned reserved chamber. */
    public static void onLogout(Player p) {
        if (p == null || !p.isNative950()) return;
        boolean returnOutside = interrupted(p) || inAnyRoom(p);
        retire(p, false);
        if (returnOutside) p.setLocation(outside());
    }
    public static String status(Player p) {
        State s = state(p);
        return (s.run == null ? "No dungeon is active." : "Dungeon guardians remaining: " + s.run.objectives.remaining() + ".")
                + " Completed solo dungeons: " + s.completed + ". Tokens: " + s.tokens + ".";
    }
    public static final class State {
        private int tokens, completed;
        private boolean recover;
        private Run run;
        Reward claim() {
            if (run == null || !run.objectives.claim()) return null;
            int award = Math.min(run.baseXp/10, Integer.MAX_VALUE - tokens);
            tokens += award; if (completed < Integer.MAX_VALUE) completed++;
            return new Reward(run.baseXp, award);
        }
    }
    static final class Run {
        final int room,baseXp;
        final Objectives objectives;
        int spawned;
        final Map<NPC, Integer> targets = new IdentityHashMap<>(), corpses = new IdentityHashMap<>();
        Run(int room) { this(room,false,1); }
        Run(int room,boolean challenge,int level) {
            if(room<0||room>=ROOM_Y.length)throw new IllegalArgumentException("Unknown dungeon room");this.room=room;
            objectives=new Objectives(challenge?OBJECTIVES*3:OBJECTIVES);
            baseXp=challenge?450+Math.max(0,Math.min(120,level)-20)*10:BASE_XP;
        }
    }
    /** Small pure objective ledger: unrelated/replayed deaths and repeated exits cannot award twice. */
    static final class Objectives {
        final int total;
        private int mask;
        private boolean claimed;
        Objectives(){this(OBJECTIVES);}
        Objectives(int total){if(total<1||total>30)throw new IllegalArgumentException("Invalid objective count");this.total=total;}
        boolean defeated(int slot) { return slot >= 0 && slot < total && (mask & 1 << slot) != 0; }
        boolean kill(int slot) { if (slot < 0 || slot >= total || claimed || defeated(slot)) return false; mask |= 1 << slot; return true; }
        int remaining() { return total - Integer.bitCount(mask); }
        boolean claim() { if (claimed || remaining() != 0) return false; claimed = true; return true; }
    }
    static final class Reward { final int xp, tokens; Reward(int xp, int tokens) { this.xp = xp; this.tokens = tokens; } }
}
