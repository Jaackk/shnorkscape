package com.rs.game.player.client;

import com.google.gson.*;
import com.rs.cache.Cache;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** The paired950 lodestone network; cache programs own its frame, hover text and keyboard shortcuts. */
public final class Native950Lodestones {
    public static final int INTERFACE = 1092, ROOT = 1477, HOST = 735, WRAPPER = 732;
    private static final String RESOURCE = "/native950/lodestones-950.json";
    private static volatile Object verifiedStore;
    private final Player player;
    private final Channel channel;
    private final Runnable verifier;
    private final Teleporter teleporter;
    private boolean open, cacheVerified;
    private Destination previous;

    interface Teleporter { boolean start(Player player, WorldTile destination); }
    public Native950Lodestones(Player player, Channel channel) {
        this(player, channel, Native950Lodestones::verify, Native950LodestoneTeleport::start);
    }
    Native950Lodestones(Player player, Channel channel, Runnable verifier, Teleporter teleporter) {
        this.player=Objects.requireNonNull(player,"player"); this.channel=Objects.requireNonNull(channel,"channel");
        this.verifier=Objects.requireNonNull(verifier,"verifier"); this.teleporter=Objects.requireNonNull(teleporter,"teleporter");
        player.getInterfaceManager().setNative950Lodestones(this);
    }
    public boolean isOpen() { return open; }
    public void bootstrap() {
        verifyBeforeOpen();
        // VARBIT packets enter the client's pending variable map. CS6001 reads
        // the reconciled map, so unlocks must arrive during login, before the
        // first interface onLoad can permanently choose the locked sprites.
        publishUnlocks();
        channel.write(Native950Packets.interfaceEvents(1465,34,-1,-1,6));
    }
    public void verifyBeforeOpen() { if(!cacheVerified) { verifier.run(); cacheVerified=true; } }
    public static boolean isOpenRequest(Native950Actions.InterfaceAction action) {
        return action.interfaceId()==1465 && action.componentId()==34 && action.slot()==-1 && action.itemId()==-1
                && (action.option()==1 || action.option()==2);
    }
    public boolean handle(Native950Actions.InterfaceAction action) {
        if(isOpenRequest(action)) {
            verifyBeforeOpen();
            if(action.option()==2 && previous!=null) travel(previous,false);
            else if(!open) open();
            return true;
        }
        if(!open || action.interfaceId()!=INTERFACE || action.slot()!=-1 || action.itemId()!=-1) return false;
        if(action.componentId()==66 && action.option()==1) { close(); return true; }
        if((action.componentId()==64 || action.componentId()==61) && action.option()==1) {
            channel.write(Native950Packets.gameMessage(0,"Quick Teleport charges are not available yet. Use the ordinary lodestone teleport."));
            return true;
        }
        if(action.option()!=1 && action.option()!=2) return false;
        Destination destination=byComponent(action.componentId());
        if(destination==null) return false;
        travel(destination,action.option()==2);
        return true;
    }
    private void travel(Destination destination, boolean quick) {
        // The existing action checks combat, death, locks, controller restrictions and actual collision.
        if(!teleporter.start(player,destination.arrival())) return;
        previous=destination;
        channel.write(Native950Packets.varbitSmall(41,destination.networkId));
        if(quick) channel.write(Native950Packets.gameMessage(0,"Using ordinary Home Teleport; Quick Teleport charges are not available yet."));
        close();
    }
    private void publishUnlocks() {
        // This local exploration policy unlocks the client presentation only. It does not complete server quests.
        for(Destination destination:destinations()) channel.write(Native950Packets.varbitLarge(destination.unlockVarbit,destination.unlockValue));
        channel.write(Native950Packets.varbitLarge(23198,400)); // Prifddinas tooltip prerequisite, CS14999.
        channel.write(Native950Packets.varbitSmall(36140,100)); // Menaphos tooltip prerequisite, CS14999.
        channel.write(Native950Packets.varp(2102,15)); // Tirannwn tooltip prerequisite, CS14999.
        channel.write(Native950Packets.varbitSmall(28634,0)); // Ordinary teleport remains the primary option.
        channel.write(Native950Packets.varbitSmall(28635,0)); // No invented quick charges.
    }
    private void open() {
        verifyBeforeOpen();
        player.resetWalkSteps(); player.setRouteEvent(null);
        publishUnlocks();
        // 1092 is576x360; the generic central host is512x334. Enlarge both
        // clipping ancestors, preserving the wrapper's16px vertical margin.
        // Keep the cache's centered position modes and native frame controls.
        channel.write(Native950Packets.runClientScript(11145,576,376,0,0,(ROOT<<16)|WRAPPER));
        channel.write(Native950Packets.runClientScript(11145,576,360,0,0,(ROOT<<16)|HOST));
        channel.write(Native950Packets.openSub(ROOT,HOST,INTERFACE,false));
        player.getInterfaceManager().registerNativeOpen(INTERFACE,ROOT,HOST);
        open=true;
        channel.write(Native950Packets.hideInterface(ROOT,WRAPPER,false));
        channel.write(Native950Packets.runClientScript(1364));
        for(Destination destination:destinations()) channel.write(Native950Packets.interfaceEvents(INTERFACE,destination.component,-1,-1,6));
        channel.write(Native950Packets.interfaceEvents(INTERFACE,66,-1,-1,2));
        channel.write(Native950Packets.interfaceEvents(INTERFACE,64,-1,-1,2));
        channel.write(Native950Packets.interfaceEvents(INTERFACE,61,-1,-1,2));
        // These five overlapping entries are the JMod target and seasonal event teleports, not ordinary lodestones.
        for(int component=36;component<=40;component++) {
            channel.write(Native950Packets.hideInterface(INTERFACE,component,true));
            channel.write(Native950Packets.interfaceEvents(INTERFACE,component,-1,-1,0));
        }
    }
    public void close() {
        if(!open) return;
        open=false;
        // CS6001 registers keyboard context30. Use only CS1027's key cleanup; its remaining
        // generic modal-close hook would send a stale acknowledgement after another panel opens.
        channel.write(Native950Packets.runClientScript(8841,30,0));
        channel.write(Native950Packets.closeSub(ROOT,HOST));
        player.getInterfaceManager().unregisterNativeOpen(INTERFACE);
        channel.write(Native950Packets.hideInterface(ROOT,WRAPPER,true));
        channel.write(Native950Packets.runClientScript(11145,512,352,0,0,(ROOT<<16)|WRAPPER));
        channel.write(Native950Packets.runClientScript(8389)); // Restore the native central-host size policy.
        channel.write(Native950Packets.runClientScript(1364));
    }
    public static final class Destination {
        public final int component,packedAnchor,networkId,unlockVarbit,unlockValue;
        public final String name;
        private final int arrivalX,arrivalY,arrivalPlane;
        private Destination(JsonObject row) {
            component=row.get("component").getAsInt(); name=row.get("name").getAsString(); packedAnchor=row.get("packedAnchor").getAsInt();
            networkId=row.get("networkId").getAsInt(); unlockVarbit=row.get("unlockVarbit").getAsInt(); unlockValue=row.get("unlockValue").getAsInt();
            JsonObject landing=row.getAsJsonObject("arrival");
            if(landing==null) throw new IllegalStateException("Missing950 lodestone arrival for "+name);
            arrivalX=landing.get("x").getAsInt(); arrivalY=landing.get("y").getAsInt(); arrivalPlane=landing.get("plane").getAsInt();
        }
        public WorldTile anchor() { return new WorldTile(packedAnchor); }
        /**
         * The landing tile the table records, not a tile recomputed here. 28 of the 29 rows carry
         * the original910 "south of the stone" convention; City of Um's south tile is solid in
         * index5 group3472 - as is its whole surrounding ring bar the east tile - so that row
         * carries1084,1768,1 instead. Reading the field keeps the one exception in the data file
         * beside the map evidence for it, rather than splitting the rule across two places.
         */
        public WorldTile arrival() { return new WorldTile(arrivalX,arrivalY,arrivalPlane); }
    }
    private static final class Data {
        static final JsonObject JSON=load();
        static final List<Destination> DESTINATIONS=readDestinations(JSON);
        private static JsonObject load() {
            java.io.InputStream stream=Native950Lodestones.class.getResourceAsStream(RESOURCE);
            if(stream==null) throw new IllegalStateException("Missing950 lodestone resource");
            try(InputStreamReader reader=new InputStreamReader(stream,StandardCharsets.UTF_8)) { return new JsonParser().parse(reader).getAsJsonObject(); }
            catch(java.io.IOException failure) { throw new IllegalStateException("Cannot read950 lodestone resource",failure); }
        }
        private static List<Destination> readDestinations(JsonObject data) {
            List<Destination> rows=new ArrayList<>(); Set<Integer> components=new HashSet<>(), ids=new HashSet<>();
            for(JsonElement element:data.getAsJsonArray("destinations")) {
                Destination row=new Destination(element.getAsJsonObject());
                if(row.component<8 || (row.component>35 && row.component!=41) || !components.add(row.component)
                        || row.networkId<1 || row.networkId>33 || !ids.add(row.networkId) || row.unlockVarbit<0 || row.unlockValue<1)
                    throw new IllegalStateException("Invalid950 lodestone row");
                WorldTile stone=row.anchor(), landing=row.arrival();
                if(landing.getPlane()!=stone.getPlane()
                        || Math.abs(landing.getX()-stone.getX())+Math.abs(landing.getY()-stone.getY())!=1)
                    throw new IllegalStateException("950 lodestone arrival must be a cardinal neighbour of its stone: "+row.name);
                rows.add(row);
            }
            if(rows.size()!=29) throw new IllegalStateException("Expected29 ordinary950 lodestones");
            return Collections.unmodifiableList(rows);
        }
    }
    public static List<Destination> destinations() { return Data.DESTINATIONS; }
    public static Destination byComponent(int component) {
        for(Destination row:destinations()) if(row.component==component) return row;
        return null;
    }
    public static synchronized void verify() {
        if(Cache.STORE!=null && verifiedStore==Cache.STORE) return;
        if(Cache.STORE==null || !Cache.isFlatReadOnly()) throw new IllegalStateException("Lodestones require the paired950 flat cache");
        try {
            for(JsonElement element:Data.JSON.getAsJsonArray("bindings")) {
                JsonObject pin=element.getAsJsonObject(); int index=pin.get("index").getAsInt(),group=pin.get("group").getAsInt(),file=pin.get("file").getAsInt();
                byte[] raw=Cache.STORE.getIndexes()[index].getFile(group,file);
                if(raw==null) throw new IllegalStateException("Missing950 lodestone binding "+index+"/"+group+"/"+file);
                byte[] digest=MessageDigest.getInstance("SHA-256").digest(raw); StringBuilder actual=new StringBuilder();
                for(byte value:digest) actual.append(String.format("%02x",value&255));
                if(!pin.get("sha256").getAsString().equals(actual.toString())) throw new IllegalStateException("Changed950 lodestone binding "+index+"/"+group+"/"+file);
            }
        } catch(java.security.NoSuchAlgorithmException impossible) { throw new AssertionError(impossible); }
        destinations();
        verifiedStore=Cache.STORE;
    }
}
