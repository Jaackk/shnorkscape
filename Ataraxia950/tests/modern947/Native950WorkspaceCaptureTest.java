package com.rs.game.player.client;

import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class Native950WorkspaceCaptureTest {
    @Test public void captureRequiresBothExplicitFlagsAndExcludesJaxa() {
        String[] keys={Native950WorkspaceCapture.PROPERTY,Native950DisposableWorkspaceRestore.PROPERTY,Native950DevelopmentCommands.PROPERTY};
        String[] old=Arrays.stream(keys).map(System::getProperty).toArray(String[]::new);
        io.netty.channel.embedded.EmbeddedChannel ch=new io.netty.channel.embedded.EmbeddedChannel(){
            @Override protected SocketAddress remoteAddress0(){return new InetSocketAddress("127.0.0.1",1234);}
        };
        try {
            com.rs.game.player.Player disposable=com.rs.game.player.Player.createNative950("layoutgate2",new com.rs.game.WorldTile(3217,3258,0),ch);
            com.rs.game.player.Player other=com.rs.game.player.Player.createNative950("jaxa",new com.rs.game.WorldTile(3217,3258,0),ch);
            for(String key:keys)System.setProperty(key,"true");
            assertTrue(Native950WorkspaceCapture.allowed(disposable,ch));assertFalse(Native950WorkspaceCapture.allowed(other,ch));
            System.clearProperty(keys[0]);assertFalse(Native950WorkspaceCapture.allowed(disposable,ch));
            System.setProperty(keys[0],"true");System.clearProperty(keys[1]);assertFalse(Native950WorkspaceCapture.allowed(disposable,ch));
        }finally{ch.finishAndReleaseAll();for(int i=0;i<keys.length;i++){if(old[i]==null)System.clearProperty(keys[i]);else System.setProperty(keys[i],old[i]);}}
    }
    private JsonObject schema() throws Exception {
        return Native950LayoutFixture.json(Native950LayoutFixture.readPinned(Paths.get("../tools/vulkan-static-probe/snapshot-schema-v4.json"),Native950LayoutFixture.SCHEMA_SHA,256000));
    }
    private Map<Integer,Integer> values() throws Exception {
        Map<Integer,Integer> values=new TreeMap<>();for(JsonElement e:schema().getAsJsonArray("ids"))values.put(e.getAsInt(),0);
        values.put(6056,null);values.put(6057,null);values.put(8372,6);values.put(8373,6);return values;
    }
    private byte[] payload(Map<Integer,Integer> values,long entry,long exit) throws Exception {
        ByteArrayOutputStream b=new ByteArrayOutputStream();DataOutputStream out=new DataOutputStream(b);
        out.writeLong(entry);out.writeLong(exit);out.writeLong(exit);out.writeShort(values.size());
        for(Map.Entry<Integer,Integer> e:values.entrySet()){out.writeShort(e.getKey());out.writeByte(e.getValue()==null?0:1);if(e.getValue()!=null)out.writeInt(e.getValue());}
        return b.toByteArray();
    }
    @Test public void cancelNeverClaimsAndSaveClaimsOnceIncludingUnchangedResaves() {
        Native950WorkspaceCapture.Request canceled=new Native950WorkspaceCapture.Request(1);
        assertFalse(canceled.claim());canceled.cancel();canceled.save();assertFalse(canceled.claim());
        Native950WorkspaceCapture.Request saved=new Native950WorkspaceCapture.Request(2);
        saved.save();assertTrue(saved.claim());assertFalse(saved.claim());
        Native950WorkspaceCapture.Request sameValuesNewSave=new Native950WorkspaceCapture.Request(3);
        sameValuesNewSave.save();assertTrue(sameValuesNewSave.claim());
        Native950WorkspaceCapture.Request race=new Native950WorkspaceCapture.Request(4);
        race.save();race.cancel();assertFalse(race.claim());
    }
    @Test public void exactScopePresenceTypesAndNativeFenceRequired() throws Exception {
        Map<Integer,Integer> values=values();byte[] good=payload(values,100,200);
        assertEquals(values,Native950WorkspaceCapture.decode(good,schema()));
        assertNull(Native950WorkspaceCapture.decode(good,schema()).get(6056));
        assertEquals(Integer.valueOf(0),Native950WorkspaceCapture.decode(good,schema()).get(3296));
        for(byte[] bad:new byte[][]{Arrays.copyOf(good,good.length-1),Arrays.copyOf(good,good.length+1),payload(values,0,200),payload(values,200,100),new byte[7001]}) {
            try{Native950WorkspaceCapture.decode(bad,schema());fail();}catch(IOException expected){}
        }
        Map<Integer,Integer> unknown=new TreeMap<>(values);unknown.remove(3296);unknown.put(65535,1);
        try{Native950WorkspaceCapture.decode(payload(unknown,100,200),schema());fail();}catch(IOException expected){}
        good[28]=2;try{Native950WorkspaceCapture.decode(good,schema());fail();}catch(IOException expected){}
    }
    @Test public void wrongSessionSequenceAndMagicRejected() throws Exception {
        byte[] nonce=new byte[16];nonce[0]=7;
        byte[] response=Arrays.copyOf(Native950WorkspaceCapture.header(nonce,3,1),36);
        assertEquals(1,Native950WorkspaceCapture.response(response,nonce,3).readInt());
        try{Native950WorkspaceCapture.response(response,nonce,2);fail();}catch(IOException expected){}
        try{Native950WorkspaceCapture.response(response,new byte[16],3);fail();}catch(IOException expected){}
        response[0]^=1;try{Native950WorkspaceCapture.response(response,nonce,3);fail();}catch(IOException expected){}
    }
    @Test public void peerMustOwnExactLiveGameSocketNotJustPidOrLoopback() throws Exception {
        InetSocketAddress remote=new InetSocketAddress("127.0.0.1",12345),local=new InetSocketAddress("127.0.0.2",43650);
        byte[] table=new byte[28];ByteBuffer b=ByteBuffer.wrap(table).order(ByteOrder.LITTLE_ENDIAN);b.putInt(0,1);b.putInt(4,5);b.putInt(24,777);
        System.arraycopy(remote.getAddress().getAddress(),0,table,8,4);table[12]=(byte)(12345>>8);table[13]=(byte)12345;
        System.arraycopy(local.getAddress().getAddress(),0,table,16,4);table[20]=(byte)(43650>>8);table[21]=(byte)43650;
        assertTrue(Native950WorkspacePipe.owns(table,777,remote,local));
        assertFalse(Native950WorkspacePipe.owns(table,778,remote,local));
        assertFalse(Native950WorkspacePipe.owns(table,777,new InetSocketAddress("127.0.0.1",12346),local));
        b.putInt(4,2);assertFalse(Native950WorkspacePipe.owns(table,777,remote,local));
        b.putInt(0,16385);try{Native950WorkspacePipe.owns(table,777,remote,local);fail();}catch(IOException expected){}
    }
    @Test public void newImageUpdateKeepsPreviousGenerationAndCannotChangeAnotherAccount() throws Exception {
        Path dir=Files.createTempDirectory("workspace-capture-");Native950WorkspaceStore store=new Native950WorkspaceStore(dir,schema());
        Map<Integer,Integer> old=values();store.save(new Native950WorkspaceStore.Record("testa",1,old));
        Map<Integer,Integer> next=new TreeMap<>(old);next.put(3296,4097);
        store.save(new Native950WorkspaceStore.Record("testa",2,next,Native950WorkspaceCapture.IMAGE));
        Native950WorkspaceStore reopened=new Native950WorkspaceStore(dir,schema());
        assertEquals(next,reopened.load("testa").values);assertEquals(Native950WorkspaceCapture.IMAGE,reopened.load("testa").image);
        assertNull(reopened.load("testb"));
        Path previous=store.path("testa").resolveSibling(store.path("testa").getFileName()+".previous");
        assertEquals(old,store.decode(Files.readAllBytes(previous),"testa").values);
        Map<Integer,Integer> bad=new TreeMap<>(next);bad.put(8372,9);
        try{store.save(new Native950WorkspaceStore.Record("testa",3,bad,Native950WorkspaceCapture.IMAGE));fail();}catch(IllegalArgumentException expected){}
        assertEquals(2,store.load("testa").revision);assertEquals(next,store.load("testa").values);
        Files.delete(previous);Files.createDirectory(previous);
        try{store.save(new Native950WorkspaceStore.Record("testa",3,old,Native950WorkspaceCapture.IMAGE));fail();}catch(IOException expected){}
        assertEquals(2,store.load("testa").revision);assertEquals(next,store.load("testa").values);
    }
}
