package com.rs.game.player.client;

import com.google.gson.*;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

/** Disposable, fixed-fixture receive/application gate. Not account persistence. */
final class Native950LayoutFixture {
    static final String TARGET = "layoutgate2";
    static final String FIXTURE_SHA = "958508114f036246a433060ab0b02e6afccbd964e52e567e9efd8dac4efec1e0";
    static final String SCHEMA_SHA = "53c06c70b12ba0f68ff71a3e46db4c46fdc7f3a3792e3ce6c2656066e692119d";
    private static final AttributeKey<Integer> PHASE = AttributeKey.valueOf("native950.disposable.layoutfixture.phase");
    private static final Path ROOT = Paths.get("C:/Games/950OpenSource");
    private Native950LayoutFixture() { }

    static boolean target(String name) { return TARGET.equalsIgnoreCase(name); }

    static void handle(Player player, Channel channel, String[] args) {
        if (!target(player.getUsername()) || !Native950DevelopmentCommands.allowed(
                Boolean.getBoolean(Native950DevelopmentCommands.PROPERTY), player.getClientProfile(), channel.remoteAddress())) {
            reply(channel,"Fixture gate is restricted to local disposable layoutgate2."); return;
        }
        if (args.length != 2 || !(args[1].equals("stage") || args[1].equals("apply"))) {
            reply(channel,"Use ;;layoutfixture stage, capture B, then ;;layoutfixture apply, capture C."); return;
        }
        if (!player.isActive() || player.hasFinished() || player.isDead() || player.isLocked()
                || player.getInterfaceManager().containsInterface(1475)) {
            reply(channel,"Wait for normal world interaction; close Edit Mode first."); return;
        }
        int phase = channel.attr(PHASE).get() == null ? 0 : channel.attr(PHASE).get();
        if ((args[1].equals("stage") && phase != 0) || (args[1].equals("apply") && phase != 1)) {
            reply(channel,"Refused out-of-order/repeated fixture action. Use a fresh client session."); return;
        }
        try {
            // Validate every input and construct the entire bounded packet list before any write.
            Plan plan = load(ROOT);
            channel.attr(PHASE).set(3); // Fail closed after any partially queued send; never retry in-place.
            if (args[1].equals("stage")) {
                for (Native950Packets.Packet packet : plan.packets) channel.write(packet);
                channel.attr(PHASE).set(1);
                reply(channel,"Pinned fixture setters queued. Settle and capture B before apply. No layout application claimed.");
            } else {
                channel.write(application());
                channel.attr(PHASE).set(2);
                reply(channel,"Native Load Custom 1 queued. Settle and capture C; visual reconstruction still requires confirmation.");
            }
            System.out.println("[LayoutFixture] target="+TARGET+" action="+args[1]+" fixture="+FIXTURE_SHA+" schema="+SCHEMA_SHA);
        } catch (Exception refused) {
            reply(channel,"Fixture gate refused: "+refused.getClass().getSimpleName()+". No automatic retry.");
            System.err.println("[LayoutFixture] refused: "+refused.getMessage());
        }
    }

    static Native950Packets.Packet application() { return Native950Packets.runClientScript(8741, 6); }

    static Plan load(Path root) throws Exception {
        JsonObject schema = json(readPinned(root.resolve("tools/vulkan-static-probe/snapshot-schema-v4.json"),SCHEMA_SHA,256000));
        JsonObject fixture = json(readPinned(root.resolve("logs/workspace-static-v4-35192-A.json"),FIXTURE_SHA,256000));
        Properties pins = new Properties();
        try (InputStream in = Native950LayoutFixture.class.getResourceAsStream("/native950/layout-fixture-950.properties")) {
            if (in == null) throw new IOException("Missing application evidence pins");
            pins.load(in);
        }
        if (pins.size() != 11) throw new IOException("Incomplete application evidence pins");
        for (String path : pins.stringPropertyNames()) readPinned(root.resolve(path),pins.getProperty(path),16000000);
        return plan(schema,fixture);
    }

    static byte[] readPinned(Path path, String expected, int limit) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (InputStream in = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192]; int n;
            while ((n=in.read(buffer)) != -1) {
                if (out.size()+n > limit) throw new IOException("Oversized input: "+path.getFileName());
                out.write(buffer,0,n);
            }
        }
        byte[] data=out.toByteArray();
        StringBuilder hash=new StringBuilder();
        for(byte b:MessageDigest.getInstance("SHA-256").digest(data)) hash.append(String.format("%02x",b&255));
        if(!hash.toString().equals(expected)) throw new IOException("Hash mismatch: "+path.getFileName());
        return data;
    }
    static JsonObject json(byte[] data) { return new JsonParser().parse(new String(data,StandardCharsets.UTF_8)).getAsJsonObject(); }
    static void require(boolean ok,String message) { if(!ok) throw new IllegalArgumentException(message); }

    static final class Plan {
        final List<Native950Packets.Packet> packets = new ArrayList<>();
        final Set<Integer> integerIds = new TreeSet<>();
        final Map<Integer,Integer> bitValues = new TreeMap<>();
    }

    static Plan plan(JsonObject schema,JsonObject fixture) {
        require(fixture.get("version").getAsInt()==4 && fixture.get("status").getAsString().equals("snapshot-stable")
                && !fixture.get("nativeWrites").getAsBoolean() && fixture.get("schemaSha256").getAsString().equals(SCHEMA_SHA),"Invalid V4 fixture");
        Set<Integer> allowed=new TreeSet<>();
        for(JsonElement id:schema.getAsJsonArray("ids")) allowed.add(id.getAsInt());
        require(allowed.size()==912,"Expected complete912 schema");
        Map<Integer,Integer> values=new HashMap<>();
        for(JsonElement e:fixture.getAsJsonArray("items")) {
            JsonObject item=e.getAsJsonObject(); int id=item.get("id").getAsInt();
            require(allowed.contains(id)&&!values.containsKey(id)&&item.get("stable").getAsBoolean(),"Unknown/duplicate/unstable ID");
            Integer value=null;
            if(item.get("found").getAsBoolean()) {
                require(item.get("variantTag").getAsInt()==0,"Noninteger native value");
                value=new java.math.BigDecimal(item.get("int32").getAsString()).intValueExact();
            } else require(item.get("variantTag").isJsonNull()&&item.get("int32").isJsonNull(),"Absent value fabricated");
            values.put(id,value);
        }
        require(values.keySet().equals(allowed),"Incomplete fixture");
        require(Integer.valueOf(6).equals(values.get(8372))&&Integer.valueOf(6).equals(values.get(8373)),"Custom1 must be selected");
        require(values.get(3296)!=null&&values.get(3295)!=null,"Custom1 missing");
        Plan plan=new Plan();
        for(String slot:new String[]{"6","7","12","13"}) {
            Set<Integer> ids=new TreeSet<>();
            for(Map.Entry<String,JsonElement> row:schema.getAsJsonObject("slots").getAsJsonObject(slot).entrySet())
                for(JsonElement ref:row.getValue().getAsJsonArray()) ids.add(ref.getAsJsonArray().get(1).getAsInt());
            require(ids.size()==180,"Invalid matrix size");
            for(int id:ids) addInteger(plan,values,id);
        }
        for(Map.Entry<String,JsonElement> entry:schema.getAsJsonObject("slotMetadata").entrySet()) {
            JsonObject m=entry.getValue().getAsJsonObject(); Integer value=values.get(m.get("id").getAsInt());
            require(m.get("mask").getAsInt()==31&&m.get("excludedFlagMask").getAsInt()==32,"Invalid metadata mask");
            if(value!=null) {
                int bit=m.get("varbit").getAsInt();
                plan.bitValues.put(bit,value&31);
                plan.packets.add(Native950Packets.varcBitSmall(bit,value&31));
            }
        }
        for(JsonElement id:schema.getAsJsonArray("integerMetadataIds")) addInteger(plan,values,id.getAsInt());
        require(plan.packets.size()<=732,"Oversized plan");
        return plan;
    }
    private static void addInteger(Plan plan,Map<Integer,Integer> values,int id) {
        if(values.get(id)!=null) {
            require(plan.integerIds.add(id),"Duplicate setter");
            plan.packets.add(Native950Packets.varcLarge(id,values.get(id)));
        }
    }
    private static void reply(Channel channel,String message) { channel.write(Native950Packets.gameMessage(0,message)); }
}
