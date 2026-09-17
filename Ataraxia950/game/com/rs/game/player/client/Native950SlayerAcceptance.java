package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.cores.CoresManager;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.slayer.SlayerMasterData;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import com.rs.network.modern.Native950GameTransport;
import com.rs.network.modern.Native950Isaac;
import io.netty.buffer.ByteBuf;
import com.rs.utils.data.parsers.npcs.NPCCombatDefinitionsDataParser;
import com.rs.utils.data.parsers.npcs.NPCStatsDataParser;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Actual950 cache, master dialogue and native credited deaths in a disposable world; no live accounts. */
public final class Native950SlayerAcceptance {
    private static int ticks, packets, slayerStatUpdates, lastSlayerXp;
    private static final Map<EmbeddedChannel,Native950Isaac> ciphers = new java.util.IdentityHashMap<>();
    private Native950SlayerAcceptance() { }
    public static void main(String[] args) throws Exception {
        require(args.length == 1,"Usage: Native950SlayerAcceptance <950-flat-cache-directory>; run from Ataraxia950");
        require(NativeCacheVerification.isEnforced(),"Keep cache verification enabled");
        System.setProperty(Native950World.SPAWNS_PROPERTY,"false");
        System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950World world = Native950World.getInstance();
        world.execute(() -> { run(); return null; }).get(90,TimeUnit.SECONDS);
        require(CoresManager.getNative950Scheduler().failed() == 0,"World scheduler failure");
        System.out.println("PASS: actual950 Slayer master, assignment, credited kill/respawn/completion, confirmed paid cancellation/XP reward and isolated save restore; "
                + ticks + " combat ticks, " + packets + " encrypted native frames, " + slayerStatUpdates + " Slayer stat updates");
        System.out.println("LIMIT: verifies cache/state and encrypted native output, not client input routing/rendering or a live account.");
    }
    private static void run() throws Exception {
        require(World.getPlayers().isEmpty() && World.getNPCs().isEmpty(),"Fresh disposable JVM required");
        NPCCombatDefinitionsDataParser.init(); NPCStatsDataParser.init();
        com.rs.utils.data.parsers.npcs.NPCDropsDataParser.init();
        int verified = 0;
        for (SlayerMasterData master : SlayerMasterData.values()) {
            int id = master.getNpcId();
            byte[] raw = Cache.STORE.getIndexes()[18].getFile(id >>> 7,id & 127);
            NPCDefinitions definition = raw == null ? null : NPCDefinitions.decodeStrict947(id,raw,null);
            boolean valid = Native950Slayer.verifiedMaster(id,definition)
                    && Native950IdValidity.get().isSafe(Native950IdValidity.Kind.NPC,id);
            if (valid) verified++;
            System.out.println("SLAYER MASTER " + id + " " + master.name() + " verified=" + valid);
            if (master == SlayerMasterData.TURAEL) require(valid,"Novice master950 identity/menu is unavailable");
        }
        require(verified > 0,"No supported Slayer masters");
        WorldTile masterTile = new WorldTile(3219,3258,0);
        World.getRegion(masterTile.getRegionId(),true);
        require(World.isFloorFree(0,3219,3258,1),"New Turael spawn tile is blocked");
        WorldTile playerTile = new WorldTile(3218,3258,0), targetTile = new WorldTile(3217,3258,0);
        require(World.isFloorFree(0,3218,3258,1) && World.isFloorFree(0,3217,3258,1),"Slayer probe combat tiles are blocked");
        EmbeddedChannel channel = channel(), restoredChannel = channel();
        NPC master = NPC.createNative950(8461,masterTile,1), target = NPC.createNative950(12353,targetTile,1);
        Player player = null;
        Native950MeleeCombat combat = new Native950MeleeCombat(Thread.currentThread(),new Native950MeleeCombat.Rolls() {
            public boolean accurate(long attack,long defence) { return true; }
            public int damage(int maximum) { return maximum; }
        });
        try {
            World.addNative950Npc(master); World.updateEntityRegion(master);
            World.addNative950Npc(target); World.updateEntityRegion(target); combat.register(target);
            require(combat.supports(target),"Current goblin profile unavailable");
            player = Player.createNative950("slayerprobe",playerTile,channel);
            player.setActive(true); World.addNative950Player(player,1); World.updateEntityRegion(player); player.loadMapRegions();
            Native950World.installVarpSink(player);
            player.setNative950Dialogues(new Native950Dialogues(player,channel,() -> {}));
            player.getSkills().setLevelWithoutRefresh(Skills.STRENGTH,20);
            player.getSkills().setLevelWithoutRefresh(Skills.HITPOINTS,20); player.setHitpoints(200);
            combat.attach(player);
            int talk = -1;
            for (int option = 1; option <= 5; option++)
                if (Native950Slayer.isTalkOption(master.getNative950MenuOption(option))) talk = option;
            require(talk > 0 && Native950Slayer.handles(master,talk),"Actual master Talk-to menu was not routed");
            Native950Slayer.interact(player,master,talk);
            require(player.getDialogueManager().getDialogue() instanceof Native950SlayerDialogue,"Native master dialogue not opened");
            player.getDialogueManager().continueDialogue(1188,8);
            require(player.getNative950Slayer().hasTask(),"Master failed to assign populated goblins");
            int initial = player.getNative950Slayer().remaining();
            require(initial >= 15 && initial <= 50,"Original Turael count range changed");
            require(player.getNative950Slayer().description().toLowerCase().contains("goblins"),"Assigned an unavailable family");
            player.getDialogueManager().continueDialogue(1186,8); drain(channel);
            double before = player.getSkills().getXp(Skills.SLAYER);
            Native950Slayer.onDeath(player,target);
            require(player.getSkills().getXp(Skills.SLAYER) == before,"A living NPC awarded Slayer XP");
            require(combat.attack(player,target) == null,"Native Slayer combat did not start");
            for (int count = 0; count < 100 && !target.isDead(); count++) tick(combat,player,target,channel);
            require(target.isDead(),"Native goblin encounter did not reach death");
            require(player.getNative950Slayer().remaining() == initial - 1,"Credited death did not decrement exactly once");
            require(player.getSkills().getXp(Skills.SLAYER) > before,"Matching credited kill awarded no Slayer XP");
            double earned = player.getSkills().getXp(Skills.SLAYER);
            require(slayerStatUpdates == 1 && lastSlayerXp == (int)earned,"Matching kill omitted authoritative encrypted Slayer XP");
            for (int count = 0; count < 100 && target.isDead(); count++) tick(combat,player,target,channel);
            require(!target.isDead(),"Goblin failed to respawn");
            require(player.getNative950Slayer().remaining() == initial - 1 && player.getSkills().getXp(Skills.SLAYER) == earned,
                    "Death display/respawn repeated Slayer reward");
            int[] empty = new int[28]; Arrays.fill(empty,-1);
            Native950Save base = new Native950Save("slayerprobe",3218,3258,0,empty,new int[28],new int[0],new int[0]);
            Native950Save saved = Native950PlayerBinder.capture(player,base,1234L);
            Path folder = Files.createTempDirectory("native950-slayer-acceptance-");
            Native950SaveStore store = new Native950SaveStore(folder); store.save(saved);
            Player restored = Player.createNative950("slayerprobe",playerTile,restoredChannel);
            Native950PlayerBinder.restore(restored,store.load("slayerprobe"));
            require(restored.getNative950Slayer().remaining() == initial - 1,"Remaining task did not survive save reload");
            require(restored.getSkills().getXp(Skills.SLAYER) == earned,"Slayer XP did not survive save reload");
            Map<String,Integer> oneLeft = new LinkedHashMap<>(); player.getNative950Slayer().writeSettings(oneLeft);
            oneLeft.put(Native950Slayer.REMAINING,1); player.getNative950Slayer().restore(oneLeft);
            player.setHitpoints(200);
            require(combat.attack(player,target) == null,"Respawned task NPC could not be attacked");
            for (int count = 0; count < 100 && !target.isDead(); count++) tick(combat,player,target,channel);
            require(target.isDead() && !player.getNative950Slayer().hasTask() && player.getNative950Slayer().completed() == 1,
                    "Final credited task kill did not complete once");
            require(player.getNative950Slayer().points() == 0 && player.getNative950Slayer().streak() == 0,"Turael awarded unsupported points/streak");
            drain(channel); drain(restoredChannel);
            require(slayerStatUpdates == 2 && lastSlayerXp == (int)player.getSkills().getXp(Skills.SLAYER),"Completion omitted authoritative encrypted Slayer XP");
            oneLeft.put(Native950Slayer.POINTS,430);oneLeft.put(Native950Slayer.COMPLETED,12);oneLeft.put(Native950Slayer.STREAK,9);
            player.getNative950Slayer().restore(oneLeft);
            Native950Slayer.interact(player,master,talk);
            player.getDialogueManager().continueDialogue(1188,18); // Rewards.
            player.getDialogueManager().continueDialogue(1188,8); // Cancel.
            require(player.getNative950Slayer().hasTask() && player.getNative950Slayer().points()==430,"Cancel spent points before confirmation");
            player.getDialogueManager().continueDialogue(1188,8); // Confirm.
            require(!player.getNative950Slayer().hasTask() && player.getNative950Slayer().points()==400
                    && player.getNative950Slayer().streak()==9 && player.getNative950Slayer().completed()==12,"Confirmed paid cancel damaged progress");
            player.getDialogueManager().continueDialogue(1186,8);
            player.getSkills().setXpWithoutRefresh(Skills.SLAYER,Skills.getXPForLevel(Skills.SLAYER,34));player.getSkills().setLevelWithoutRefresh(Skills.SLAYER,34);
            reward(player,master,talk);
            require(player.getNative950Slayer().points()==400,"Low-level XP purchase spent points");
            player.getDialogueManager().continueDialogue(1186,8);
            player.getSkills().setXpWithoutRefresh(Skills.SLAYER,Skills.getXPForLevel(Skills.SLAYER,35));player.getSkills().setLevelWithoutRefresh(Skills.SLAYER,35);
            before=player.getSkills().getXp(Skills.SLAYER);reward(player,master,talk);
            require(player.getNative950Slayer().points()==0 && player.getSkills().getXp(Skills.SLAYER)>before,"Confirmed XP purchase did not debit/reward");
            earned=player.getSkills().getXp(Skills.SLAYER);player.getDialogueManager().continueDialogue(1188,8);
            require(player.getNative950Slayer().points()==0 && player.getSkills().getXp(Skills.SLAYER)==earned,"Repeated confirmation duplicated reward");
            store.save(Native950PlayerBinder.capture(player,base,1235L));Native950PlayerBinder.restore(restored,store.load("slayerprobe"));
            require(!restored.getNative950Slayer().hasTask() && restored.getNative950Slayer().points()==0 && restored.getNative950Slayer().streak()==9
                    && restored.getSkills().getXp(Skills.SLAYER)==earned,"Paid task/XP progress did not survive save roundtrip");
            drain(channel);drain(restoredChannel);
        } finally {
            if (player != null) {combat.detach(player);World.removeNative950Player(player);}
            combat.clear(); World.removeNative950Npc(target);World.removeNative950Npc(master);
            channel.finishAndReleaseAll();restoredChannel.finishAndReleaseAll();
        }
        require(World.getPlayers().isEmpty() && World.getNPCs().isEmpty(),"Disposable registry cleanup failed");
    }
    private static void reward(Player player,NPC master,int talk) {
        Native950Slayer.interact(player,master,talk);
        player.getDialogueManager().continueDialogue(1188,18);
        player.getDialogueManager().continueDialogue(1188,13);
        player.getDialogueManager().continueDialogue(1188,8);
    }
    private static void tick(Native950MeleeCombat combat,Player player,NPC target,EmbeddedChannel channel) {
        ticks++;combat.beforeMovement();player.processMovement();target.processNative950Movement();combat.afterMovement();
        player.resetMasks();target.resetMasks();drain(channel);
    }
    private static EmbeddedChannel channel() {
        int[] in = {9,5,0,8}, out = {59,55,50,58};
        EmbeddedChannel channel = new EmbeddedChannel(new Native950GameTransport(
                new Native950Isaac(in),new Native950Isaac(out),Thread.currentThread()));
        ciphers.put(channel,new Native950Isaac(out));return channel;
    }
    private static void drain(EmbeddedChannel channel) {
        channel.flush();channel.runPendingTasks();Object message;
        Native950Isaac cipher = ciphers.get(channel);
        while ((message = channel.readOutbound()) != null) {
            try {
                require(message instanceof ByteBuf,"Native transport did not emit bytes");
                ByteBuf bytes = (ByteBuf)message;
                while (bytes.isReadable()) {
                    int opcode = (bytes.readUnsignedByte() - cipher.getAsInt()) & 255;
                    if (opcode >= 128) opcode = ((opcode - 128) << 8) | ((bytes.readUnsignedByte() - cipher.getAsInt()) & 255);
                    ServerPacket kind = null;
                    for (ServerPacket candidate : ServerPacket.values()) if (candidate.opcode() == opcode) {kind=candidate;break;}
                    require(kind != null,"Unknown encrypted native output opcode " + opcode);
                    int length = kind.size();
                    if (length == -1) length = bytes.readUnsignedByte(); else if (length == -2) length = bytes.readUnsignedShort();
                    require(length >= 0 && length <= bytes.readableBytes(),"Native output frame length mismatch");
                    byte[] body = new byte[length];bytes.readBytes(body);packets++;
                    if (kind == ServerPacket.UPDATE_STAT && ((-body[0]) & 255) == Skills.SLAYER) {
                        require(length == 6,"Slayer stat frame width changed");slayerStatUpdates++;
                        lastSlayerXp = ((body[2]&255)<<24)|((body[3]&255)<<16)|((body[4]&255)<<8)|(body[5]&255);
                    }
                }
            } finally {ReferenceCountUtil.release(message);}
        }
        channel.checkException();require(channel.isActive(),"Native packet channel closed");
        require(channel.pipeline().get(Native950GameTransport.class).terminalFailure()==null,"Native transport failure");
    }
    private static void require(boolean value,String message) {if (!value) throw new IllegalStateException(message);}
}
