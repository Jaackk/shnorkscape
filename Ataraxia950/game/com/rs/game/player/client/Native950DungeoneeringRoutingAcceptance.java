package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cores.CoresManager;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.network.modern.Native950GameTransport;
import com.rs.network.modern.Native950Isaac;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import com.rs.utils.data.parsers.npcs.NPCCombatDefinitionsDataParser;
import com.rs.utils.data.parsers.npcs.NPCStatsDataParser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** Encrypted ordinary clicks and production combat death callbacks in a disposable actual-cache world. */
public final class Native950DungeoneeringRoutingAcceptance {
    private static int ticks, frames;
    private Native950DungeoneeringRoutingAcceptance() { }
    public static void main(String[] args) throws Exception {
        require(args.length == 1, "Provide the selected950 flat cache path");
        require(NativeCacheVerification.isEnforced(), "Cache verification must remain enabled");
        System.setProperty(Native950World.SPAWNS_PROPERTY, "false");
        System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY, "false");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950World.getInstance().execute(() -> {
            require(World.getNPCs().isEmpty() && World.getPlayers().isEmpty(), "Disposable empty world required");
            NPCCombatDefinitionsDataParser.init(); NPCStatsDataParser.init();
            com.rs.utils.data.parsers.npcs.NPCDropsDataParser.init();
            Native950Dungeoneering.verifyCacheBindings();
            Native950DungeoneeringAcceptance.verifySceneAreas();
            for (int i = 0; i < 4; i++) Native950Dungeoneering.verifyRoom(i, 2);
            try (Fixture f = new Fixture()) { check(f); checkChallenge(f); }
            require(World.getNPCs().isEmpty() && World.getPlayers().isEmpty(), "Probe leaked world entities");
            return null;
        }).get(90, TimeUnit.SECONDS);
        require(CoresManager.getNative950Scheduler().failed() == 0, "World scheduler failed");
        System.out.println("PASS: encrypted950 ring/entrance/exit, ordinary three-target run and level-20 nine-target three-wave challenge through production combat, exactly-once rewards and abort cleanup; " + ticks + " ticks and " + frames + " decoded encrypted frames.");
        System.out.println("LIMIT: deterministic accuracy and maximum damage use a disposable high-level player; no authenticated client, rendered pixels or wall-clock pacing are tested.");
    }
    private static void check(Fixture f) {
        f.give(Native950Dungeoneering.RING); f.run(2);
        f.ring("Teleport to Daemonheim"); f.run(2);
        require(Native950Dungeoneering.ringDestination().matches(f.player), "Ring teleport did not reach Daemonheim");
        f.ring("Open party interface");
        require(f.player.getDialogueManager().getDialogue() instanceof Native950DungeoneeringDialogue, "Ring did not open the ordinary dialogue");
        f.choose(3); // Dismiss and use the actual entrance route next.
        WorldTile entranceTile = new WorldTile(3454, 3722, 0); World.getRegion(entranceTile.getRegionId(), true);
        WorldObject entrance = World.getRegion(entranceTile.getRegionId()).getObjectWithType(0, 3454 & 63, 3722 & 63, 10);
        require(entrance != null && entrance.getId() == Native950Dungeoneering.ENTRANCE, "Actual entrance missing");
        f.move(new WorldTile(3452, 3718, 0)); f.object(entrance);
        require(f.player.hasWalkSteps(), "Remote entrance click did not approach");
        require(!Native950Dungeoneering.interrupted(f.player), "Remote click admitted a room before reaching the entrance");
        f.run(24);
        require(f.player.getDialogueManager().getDialogue() instanceof Native950DungeoneeringDialogue, "Actual entrance failed to open dialogue after approach");
        f.choose(0); f.run(2);
        require(Native950Dungeoneering.inRoom(f.player, 0), "Entrance dialogue failed to enter solo chamber");
        List<NPC> guardians = new ArrayList<>();
        for (NPC npc : World.getNPCs()) if (npc.getId() == Native950Dungeoneering.GUARDIAN) guardians.add(npc);
        require(guardians.size() == 3, "Entry did not register three guardians");
        double before = f.player.getSkills().getXp(Skills.DUNGEONEERING);
        int remaining = 3;
        for (NPC npc : guardians) {
            List<WorldTile> contact = Native950MeleeReach.contactTiles(npc, npc.getSize());
            require(!contact.isEmpty(), "Guardian has no reachable attack tile");
            f.move(contact.get(0));
            String refusal = f.combat.attack(f.player, npc);
            require(refusal == null, "Production combat rejected owner: " + refusal);
            for (int wait = 0; wait < 100 && !npc.isDead(); wait++) f.run(1);
            require(npc.isDead() && npc.isNative950DeathVisible(), "Production combat did not commit a guardian death");
            require(f.player.getRealChannel().isActive(), "Combat callback closed the transport");
            require(Native950Dungeoneering.status(f.player).contains("remaining: " + --remaining),
                    "Production Rewards callback did not credit this death before any test replay");
            Native950Dungeoneering.onNpcDeath(f.player, npc); // Replayed callback must not count twice.
        }
        require(Native950Dungeoneering.status(f.player).contains("remaining: 0"), "Production death Rewards did not reach the objective ledger");
        require(f.player.getSkills().getXp(Skills.DUNGEONEERING) == before && Native950Dungeoneering.tokens(f.player) == 0,
                "Defeats awarded completion before the exit");
        WorldObject exit = World.getRegion(f.player.getRegionId()).getObjectWithType(0, 112 & 63, 4994 & 63, 10);
        f.object(exit); f.run(24);
        require(Native950Dungeoneering.outside().matches(f.player), "Actual exit packet failed to return outside");
        double after = f.player.getSkills().getXp(Skills.DUNGEONEERING);
        require(after > before && Native950Dungeoneering.tokens(f.player) == 15 && Native950Dungeoneering.completed(f.player) == 1,
                "Actual exit did not award XP, tokens and completion");
        require(f.stats.contains(Skills.DUNGEONEERING), "Completion emitted no native Dungeoneering stat frame");
        f.object(exit); f.run(2);
        require(f.player.getSkills().getXp(Skills.DUNGEONEERING) == after && Native950Dungeoneering.tokens(f.player) == 15,
                "Stale exit packet duplicated completion");
        require(World.getNPCs().isEmpty(), "Completed chamber leaked guardians");
        f.ring("Open party interface"); f.choose(0); f.run(2);
        require(World.getNPCs().size() == 3 && Native950Dungeoneering.inRoom(f.player, 0), "Ring dialogue did not start a repeat run");
        f.ring("Teleport to Daemonheim"); f.run(2);
        require(World.getNPCs().isEmpty() && !Native950Dungeoneering.interrupted(f.player), "Ring abort leaked the chamber");
        require(Native950Dungeoneering.tokens(f.player) == 15 && f.player.getSkills().getXp(Skills.DUNGEONEERING) == after,
                "Ring abort awarded completion");
    }
    private static void checkChallenge(Fixture f) {
        f.player.getSkills().setXpWithoutRefresh(Skills.DUNGEONEERING, Skills.getXPForLevel(Skills.DUNGEONEERING, 19));
        f.player.getSkills().setLevelWithoutRefresh(Skills.DUNGEONEERING, 19);
        f.ring("Open party interface"); f.choose(4); f.run(2);
        require(!Native950Dungeoneering.interrupted(f.player) && World.getNPCs().isEmpty(), "Challenge admitted level 19");
        f.player.getSkills().setXpWithoutRefresh(Skills.DUNGEONEERING, Skills.getXPForLevel(Skills.DUNGEONEERING, 20));
        f.player.getSkills().setLevelWithoutRefresh(Skills.DUNGEONEERING, 20);
        f.ring("Open party interface"); f.choose(4); f.run(2);
        require(Native950Dungeoneering.inRoom(f.player, 0), "Fifth ordinary dialogue row did not start the level-20 challenge");
        double before = f.player.getSkills().getXp(Skills.DUNGEONEERING);
        int beforeTokens = Native950Dungeoneering.tokens(f.player), beforeCompleted = Native950Dungeoneering.completed(f.player);
        int remaining = 9;
        for (int wave = 0; wave < 3; wave++) {
            List<NPC> targets = new ArrayList<>();
            for (NPC npc : World.getNPCs()) if (npc.getId() == Native950Dungeoneering.GUARDIAN && !npc.isDead()) targets.add(npc);
            require(targets.size() == 3, "Challenge wave " + (wave + 1) + " did not have exactly three live targets");
            for (NPC npc : targets) {
                List<WorldTile> contact = Native950MeleeReach.contactTiles(npc, npc.getSize());
                require(!contact.isEmpty(), "Challenge guardian has no contact tile");
                f.move(contact.get(0));
                require(f.combat.attack(f.player, npc) == null, "Challenge combat rejected owner");
                for (int wait = 0; wait < 100 && !npc.isDead(); wait++) f.run(1);
                require(npc.isDead(), "Challenge guardian did not die through production combat");
                require(Native950Dungeoneering.status(f.player).contains("remaining: " + --remaining), "Wave kill missed the objective ledger");
                Native950Dungeoneering.onNpcDeath(f.player, npc);
            }
            require(f.player.getSkills().getXp(Skills.DUNGEONEERING) == before && Native950Dungeoneering.tokens(f.player) == beforeTokens,
                    "Challenge paid rewards before the exit");
            f.run(12);
        }
        require(remaining == 0, "Challenge did not credit all nine kills");
        WorldObject exit = World.getRegion(f.player.getRegionId()).getObjectWithType(0, 112 & 63, 4994 & 63, 10);
        f.object(exit); f.run(24);
        require(Native950Dungeoneering.outside().matches(f.player), "Challenge exit did not return outside");
        require(Native950Dungeoneering.tokens(f.player) == beforeTokens + 45 && Native950Dungeoneering.completed(f.player) == beforeCompleted + 1,
                "Level-20 challenge did not award the snapshotted 450 base XP / 45 tokens exactly once");
        require(f.player.getSkills().getXp(Skills.DUNGEONEERING) > before && World.getNPCs().isEmpty(), "Challenge XP or cleanup failed");
        double after = f.player.getSkills().getXp(Skills.DUNGEONEERING);
        f.object(exit); f.run(2);
        require(f.player.getSkills().getXp(Skills.DUNGEONEERING) == after && Native950Dungeoneering.tokens(f.player) == beforeTokens + 45,
                "Stale challenge exit duplicated rewards");
    }
    private static final class Fixture implements AutoCloseable {
        final int[] incoming = {9,5,0,2}, outgoing = {59,55,50,52};
        final Native950Isaac clientCipher = new Native950Isaac(incoming), cipher = new Native950Isaac(outgoing);
        final Native950GameTransport transport = new Native950GameTransport(new Native950Isaac(incoming), new Native950Isaac(outgoing), Thread.currentThread());
        final EmbeddedChannel channel = new EmbeddedChannel(transport);
        final Set<Integer> stats = new HashSet<>();
        final Player player;
        final Native950Interactions input;
        final Native950MeleeCombat combat = new Native950MeleeCombat(Thread.currentThread(), new Native950MeleeCombat.Rolls() {
            public boolean accurate(long attack, long defence) { return true; }
            public int damage(int maximum) { return maximum; }
        });
        Fixture() {
            player = Player.createNative950("dung-route", new WorldTile(3217,3258,0), channel);
            player.setActive(true); player.setRunning(true); Native950World.installVarpSink(player);
            World.addNative950Player(player,1); World.updateEntityRegion(player); player.loadMapRegions(); player.setClientHasLoadedMapRegion();
            Native950ItemCatalog catalog = new Native950ItemCatalog(Collections.<Native950ItemCatalog.Entry>emptyList()).withLegacyDrops();
            Native950Content.BankUi bank = new Native950Content.BankUi(517,201,15,317,39,new int[11],new int[11],Collections.emptyList(),Collections.emptyList());
            input = new Native950Interactions(player,channel,new Native950Content(catalog,bank),null,null);
            for (int slot = 0; slot < 28; slot++) player.getInventory().items.set(slot,null);
            for (int skill : new int[]{Skills.ATTACK,Skills.STRENGTH,Skills.DEFENCE,Skills.HITPOINTS}) {
                player.getSkills().setXpWithoutRefresh(skill,Skills.getXPForLevel(skill,99));
                player.getSkills().setLevelWithoutRefresh(skill,99);
            }
            player.setHitpoints(player.getMaxHitpoints()); combat.attach(player); input.bootstrap(); drain();
        }
        void move(WorldTile tile) { input.walking(); player.resetWalkSteps(); player.setNextWorldTile(null); player.setLocation(tile); World.updateEntityRegion(player); player.loadMapRegions(); player.setClientHasLoadedMapRegion(); player.resetMasks(); run(2); }
        void give(int id) { require(Native950Skilling.giveItem(player,id,1), "Could not give fixture ring"); }
        void ring(String label) {
            require(Native950InventoryMenu.usesOrdinaryOperations(Native950Dungeoneering.RING), "Ring uses an unsupported specialized menu");
            Native950ItemCatalog.Entry entry = Native950CacheItems.entry(Native950Dungeoneering.RING);
            int option = -1; for (int i=1;i<=5;i++) if (label.equals(entry.option(i))) option = i;
            require(option > 0, "Missing actual950 ring menu: " + label);
            int uiOption = option <= 3 ? option : option + 3;
            int[] opcodes = {18,122,89,100,81,126,49,66,31,59}; int hash=(1473<<16)|5;
            int slot=player.getInventory().items.lookupSlot(Native950Dungeoneering.RING); require(slot >= 0, "Ring disappeared");
            ByteBuf b=Unpooled.buffer(10);b.writeByte((opcodes[uiOption-1]+clientCipher.getAsInt())&255);b.writeMedium(Native950Dungeoneering.RING);
            b.writeByte(hash>>>16);b.writeByte(hash>>>24);b.writeByte(hash);b.writeByte(hash>>>8);b.writeShort(slot);send(b);
        }
        void object(WorldObject o) {
            require(o != null, "Missing clicked world object"); ByteBuf b=Unpooled.buffer(10);
            b.writeByte((34+clientCipher.getAsInt())&255);b.writeByte(o.getY()+128);b.writeByte(o.getY()>>>8);b.writeInt(o.getId());
            b.writeByte(o.getX()>>>8);b.writeByte(o.getX()+128);b.writeByte(0);send(b);
        }
        void choose(int row) { ByteBuf b=Unpooled.buffer(7);b.writeByte((101+clientCipher.getAsInt())&255);b.writeInt((1188<<16)|(8+5*row));b.writeByte(255);b.writeByte(127);send(b); }
        void send(ByteBuf b) { channel.writeInbound(b);channel.runPendingTasks();require(transport.drainActions(input::handle)==1,"Encrypted click failed to reach ordinary input"); }
        void run(int count) {
            for(int i=0;i<count;i++) {
                ticks++;input.beginTick();combat.beforeMovement();Native950Dungeoneering.tick(player);player.processEntity();player.processEntityUpdate();
                for(NPC npc:new ArrayList<>(Native950World.getInstance().nativeNpcs()))npc.processNative950Movement();
                combat.afterMovement();input.afterMovement();channel.write(Native950Packets.tickEnd());drain();player.resetMasks();
                for(NPC npc:World.getNPCs())npc.resetMasks();
            }
        }
        void drain() {
            channel.flush();channel.runPendingTasks();Object message;
            while((message=channel.readOutbound())!=null)try {
                require(message instanceof ByteBuf,"Transport emitted nonbytes");ByteBuf b=(ByteBuf)message;
                while(b.isReadable()) {
                    int opcode=(b.readUnsignedByte()-cipher.getAsInt())&255;
                    if(opcode>=128)opcode=((opcode-128)<<8)|((b.readUnsignedByte()-cipher.getAsInt())&255);
                    ServerPacket kind=null;for(ServerPacket packet:ServerPacket.values())if(packet.opcode()==opcode){kind=packet;break;}
                    require(kind!=null,"Unknown950 packet "+opcode);int length=kind.size();if(length==-1)length=b.readUnsignedByte();else if(length==-2)length=b.readUnsignedShort();
                    require(length>=0&&length<=b.readableBytes(),"Invalid950 frame length "+kind);
                    if(kind==ServerPacket.UPDATE_STAT&&length>0)stats.add((-b.getUnsignedByte(b.readerIndex()))&255);
                    b.skipBytes(length);frames++;
                }
            } finally {ReferenceCountUtil.release(message);}
            channel.checkException();require(channel.isActive()&&transport.terminalFailure()==null,"Transport failed: "+transport.terminalFailure());
        }
        public void close() {
            Native950Dungeoneering.onLogout(player);combat.detach(player);combat.clear();input.close();
            for(int y:Native950Dungeoneering.ROOM_Y)for(com.rs.game.item.floor.FloorItem item:World.getRegion(new WorldTile(119,y+7,0).getRegionId()).getGroundItemsSafe())World.removeGroundItem(item);
            World.removeNative950Player(player);channel.finishAndReleaseAll();
        }
    }
    private static void require(boolean ok,String message) { if(!ok)throw new AssertionError(message); }
}
