package com.rs.game.player.client;

import com.google.gson.*;
import com.rs.cache.Cache;
import com.rs.network.protocol.modern950.Native950Packets;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** Verified950 bank controls. Native scripts own labels, layout, and local search. */
public final class Native950BankUi {
    private Native950BankUi() { }
    public static final int INTERFACE=517, ITEMS=201, INVENTORY=15, WORN_ITEMS=35;
    public static final int DEPOSIT_INVENTORY=39, DEPOSIT_EQUIPMENT=42, DEPOSIT_FAMILIAR=45,
            DEPOSIT_POUCH=48, AUTO_TAB_SWITCH=51, DESTINATION_BACKPACK=56,
            DESTINATION_WORN=60, DESTINATION_FAMILIAR=64, QUANTITY_ONE=93,
            QUANTITY_FIVE=96, QUANTITY_TEN=99, QUANTITY_ALL=103, QUANTITY_X=106,
            CHANGE_X=114, PLACEHOLDER=123, NOTES=127, BANK_PIN=134,
            COSTUME_ROOM=140, DIANGO=143, METAL_BANK=146, MORE_STORAGE=149,
            TRANSFER_VIEW=152, PRESET_VIEW=153, ALL_ITEMS=165, SEARCH=237,
            CANCEL_SEARCH_OVERLAY=238, CANCEL_SEARCH=239, CLOSE=317;
    public static final int QUANTITY_VARBIT=45189, CUSTOM_QUANTITY_VARP=111, NOTES_VARP=160,
            DESTINATION_VARBIT=45139, TRANSFER_PRESET_VARBIT=45191, PLACEHOLDER_VARBIT=45190, AUTO_TAB_VARBIT=45911;
    /** Operations1..7 and10 only: no placeholder, context-sensitive op9, or drag flags. */
    public static final int ITEM_EVENTS=0x4fe;
    private static final int[] PRIMARY={DEPOSIT_INVENTORY,DEPOSIT_EQUIPMENT,QUANTITY_ONE,
            QUANTITY_FIVE,QUANTITY_TEN,QUANTITY_ALL,QUANTITY_X,CHANGE_X,NOTES,SEARCH,CLOSE};
    private static final int[] FEEDBACK={DEPOSIT_FAMILIAR,DEPOSIT_POUCH,AUTO_TAB_SWITCH,
            DESTINATION_BACKPACK,DESTINATION_WORN,DESTINATION_FAMILIAR,BANK_PIN,
            COSTUME_ROOM,DIANGO,METAL_BANK,MORE_STORAGE,TRANSFER_VIEW,PRESET_VIEW,ALL_ITEMS};
    private static volatile Object verifiedStore;

    /** CS13353 acquires context24. IF_CLOSESUB alone does not run its matching CS9299 cleanup. */
    static void closeInput(com.rs.game.player.Player player,io.netty.channel.Channel channel) {
        Native950BugTest.event(player,"input-focus","bank-release-requested","context",24,"script",9299);
        channel.write(Native950Packets.runClientScript(9299));
    }

    /** Run after mounting517; this does not change authoritative quantity or note preferences. */
    public static List<Native950Packets.Packet> openControls() {
        List<Native950Packets.Packet> out=new ArrayList<>(resetUnsupportedModes());
        out.add(Native950Packets.hideInterface(INTERFACE,91,false));
        out.add(Native950Packets.hideInterface(INTERFACE,126,false));
        for(int component:new int[]{122,62,58}) out.add(Native950Packets.hideInterface(INTERFACE,component,true));
        out.add(Native950Packets.interfaceEvents(INTERFACE,ITEMS,0,599,ITEM_EVENTS));
        out.add(Native950Packets.interfaceEvents(INTERFACE,INVENTORY,0,27,ITEM_EVENTS));
        // Responsive layouts can show this column independently of destination selector58.
        out.add(Native950Packets.interfaceEvents(INTERFACE,WORN_ITEMS,0,18,0));
        for(int component:PRIMARY) out.add(Native950Packets.interfaceEvents(INTERFACE,component,-1,-1,2));
        for(int component:FEEDBACK) out.add(Native950Packets.interfaceEvents(INTERFACE,component,-1,-1,2));
        // These are exact authored static operation ranges, not arbitrary dynamic slot ranges.
        out.add(Native950Packets.interfaceEvents(INTERFACE,PLACEHOLDER,-1,-1,6));
        out.add(Native950Packets.interfaceEvents(INTERFACE,68,-1,-1,0x7fe));
        out.add(Native950Packets.interfaceEvents(INTERFACE,69,-1,-1,2));
        return Collections.unmodifiableList(out);
    }

    /** Restore supported destinations without clearing search or quantity/note preferences. */
    public static List<Native950Packets.Packet> resetUnsupportedModes() {
        return Collections.unmodifiableList(Arrays.asList(
                Native950Packets.varbitSmall(DESTINATION_VARBIT,0),
                Native950Packets.varbitSmall(TRANSFER_PRESET_VARBIT,0),
                Native950Packets.varbitSmall(PLACEHOLDER_VARBIT,0),
                Native950Packets.varbitSmall(AUTO_TAB_VARBIT,0)));
    }

    /** quantityMode is the cache45189 encoding2/3/4/5/7, never a requested item count. */
    public static List<Native950Packets.Packet> sync(int quantityMode,int savedX,boolean notes) {
        if(!validQuantityMode(quantityMode) || savedX<1) throw new IllegalArgumentException("Invalid bank quantity preference");
        return Collections.unmodifiableList(Arrays.asList(
                Native950Packets.varbitSmall(QUANTITY_VARBIT,quantityMode),
                Native950Packets.varp(CUSTOM_QUANTITY_VARP,savedX),
                Native950Packets.varp(NOTES_VARP,notes?1:0)));
    }
    public static boolean validQuantityMode(int mode) { return mode==2 || mode==3 || mode==4 || mode==5 || mode==7; }
    /** Returns the encoded native mode, or0 for a control that does not select quantity. */
    public static int quantityMode(int component) {
        switch(component) {
            case QUANTITY_ONE:return 2;case QUANTITY_FIVE:return 3;case QUANTITY_TEN:return 4;
            case QUANTITY_ALL:return 7;case QUANTITY_X:case CHANGE_X:return 5;default:return 0;
        }
    }
    /** Recognizes only cache-audited controls; callers still validate interface, option and slot. */
    public static boolean isControl(int component) {
        for(int value:PRIMARY) if(value==component)return true;
        for(int value:FEEDBACK) if(value==component)return true;
        return component==67 || component==68 || component==69 || component==PLACEHOLDER
                || component==CANCEL_SEARCH_OVERLAY || component==CANCEL_SEARCH;
    }
    /** Known UI operations with no complete native backend; report them without invoking910 actions. */
    public static boolean isUnsupportedControl(int component) {
        switch(component) {
            case DEPOSIT_FAMILIAR:case DEPOSIT_POUCH:case AUTO_TAB_SWITCH:
            case DESTINATION_WORN:case DESTINATION_FAMILIAR:case 67:case 68:case 69:
            case PLACEHOLDER:case BANK_PIN:case COSTUME_ROOM:case DIANGO:case METAL_BANK:
            case MORE_STORAGE:case PRESET_VIEW:return true;
            default:return false;
        }
    }
    public static boolean isLocalSearchControl(int component) {
        return component==SEARCH || component==CANCEL_SEARCH_OVERLAY || component==CANCEL_SEARCH;
    }

    /** Verify before any live bank controls are enabled. Recipes remain pure for packet tests. */
    public static synchronized void verify() {
        if(Cache.STORE!=null && verifiedStore==Cache.STORE)return;
        if(Cache.STORE==null || !Cache.isFlatReadOnly())throw new IllegalStateException("Bank UI requires the paired950 flat cache");
        try(InputStream stream=Native950BankUi.class.getResourceAsStream("/native950/bank-ui-950.json")) {
            if(stream==null)throw new IllegalStateException("Missing950 bank UI bindings");
            JsonObject data=new JsonParser().parse(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();
            for(JsonElement element:data.getAsJsonArray("bindings")) {
                JsonObject pin=element.getAsJsonObject();int index=pin.get("index").getAsInt(),group=pin.get("group").getAsInt(),file=pin.get("file").getAsInt();
                byte[] raw=Cache.STORE.getIndexes()[index].getFile(group,file);
                if(raw==null)throw new IllegalStateException("Missing950 bank UI binding "+index+"/"+group+"/"+file);
                StringBuilder actual=new StringBuilder();
                for(byte value:MessageDigest.getInstance("SHA-256").digest(raw))actual.append(String.format("%02x",value&255));
                String key=index+"/"+group+"/"+file;
                if(!Native950LibraryBridge.matches(key,pin.get("sha256").getAsString(),actual.toString()))
                    NativeCacheVerification.requireBinding("Bank UI",key,pin.get("sha256").getAsString(),actual.toString());
            }
        } catch(java.io.IOException|java.security.NoSuchAlgorithmException failure) {
            throw new IllegalStateException("Cannot verify950 bank UI bindings",failure);
        }
        verifiedStore=Cache.STORE;
    }
}

