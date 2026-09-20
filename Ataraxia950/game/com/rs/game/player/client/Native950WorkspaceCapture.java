package com.rs.game.player.client;

import com.google.gson.JsonObject;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import java.io.*;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/** Disposable-only native Save capture; the normal launch profile cannot start this endpoint. */
final class Native950WorkspaceCapture {
    static final String PROPERTY="ataraxia950.workspaceCaptureGate";
    static final String IMAGE="19323515092bbccd0090033badbc56177d0ca599216e4d702be27432cf693a13";
    static final String DLL="1df5b7a0f33c94932c4ecdbacd9b65ec23b8e4539e6d591d797d34c891a404c6";
    static final int MAGIC=0x57534335;
    private static final AttributeKey<Native950WorkspaceCapture> KEY=AttributeKey.valueOf("workspace-capture-v5");
    private final Channel channel;
    private final String account;
    private final BlockingQueue<Request> requests=new ArrayBlockingQueue<>(1);
    private final AtomicReference<String> notice=new AtomicReference<>();
    private Request current;
    private long sequence;
    static final class Request {
        final long sequence;
        private int state; // 0 editor open, 1 Save queued, 2 canceled, 3 consumed
        Request(long sequence){this.sequence=sequence;}
        synchronized void save(){if(state==0)state=1;}
        synchronized void cancel(){if(state!=3)state=2;}
        synchronized boolean claim(){if(state!=1)return false;state=3;return true;}
    }
    private Native950WorkspaceCapture(String account,Channel channel){this.account=account;this.channel=channel;}
    static boolean allowed(Player player,Channel ch){return Boolean.getBoolean(PROPERTY)&&Native950DisposableWorkspaceRestore.allowed(player,ch);}
    static void sceneReady(Player player,Channel ch) {
        if(!allowed(player,ch)||ch.attr(KEY).get()!=null)return;
        Native950WorkspaceCapture capture=new Native950WorkspaceCapture(Native950Save.canonicalUsername(player.getUsername()),ch);
        ch.attr(KEY).set(capture);
        Thread worker=new Thread(capture::run,"workspace-capture-layoutgate2");worker.setDaemon(true);worker.start();
    }
    static void opened(Channel ch) {
        Native950WorkspaceCapture c=ch.attr(KEY).get();if(c==null)return;
        if(c.current!=null)c.current.cancel(); // A new editor cycle cannot complete an old capture.
        Request request=new Request(++c.sequence);c.current=request;
        if(!c.requests.offer(request)){request.cancel();c.notice.set("Workspace capture busy; this edit is not armed for durable saving.");}
    }
    static void saveQueued(Channel ch){Native950WorkspaceCapture c=ch.attr(KEY).get();if(c!=null&&c.current!=null)c.current.save();}
    static void canceled(Channel ch){Native950WorkspaceCapture c=ch.attr(KEY).get();if(c!=null&&c.current!=null)c.current.cancel();}
    static void drain(Channel ch) {
        Native950WorkspaceCapture c=ch.attr(KEY).get();if(c==null)return;
        String text=c.notice.getAndSet(null);if(text!=null)ch.write(Native950Packets.gameMessage(0,text));
    }
    private void run() {
        try {
            JsonObject schema=Native950LayoutFixture.loadSchema(Native950DisposableWorkspaceRestore.ROOT);
            Native950WorkspaceStore store=new Native950WorkspaceStore(Native950DisposableWorkspaceRestore.ROOT.resolve("workspace-state950"),schema);
            while(channel.isActive()) {
                try(Native950WorkspacePipe pipe=new Native950WorkspacePipe()) {
                    while(channel.isActive()&&!pipe.connect())Thread.sleep(100);
                    if(!channel.isActive())return;
                    int pid=pipe.peerPid();
                    InetSocketAddress remote=(InetSocketAddress)channel.remoteAddress(),local=(InetSocketAddress)channel.localAddress();
                    if(!pipe.ownsGameSocket(pid,remote,local))throw new IOException("Peer does not own authenticated game connection");
                    pipe.verifyImage(pid,Native950DisposableWorkspaceRestore.ROOT.resolve("client/rs2client-vulkan-workspace-diag-v5.exe"),IMAGE,DLL);
                    byte[] nonce=new byte[16];new SecureRandom().nextBytes(nonce);
                    System.out.println("[WorkspaceCaptureGate] authenticated disposable peer pid="+pid);
                    notice.set("Workspace automatic capture ready (disposable gate).");
                    while(channel.isActive()) {
                        Request r=requests.poll(250,TimeUnit.MILLISECONDS);if(r==null)continue;
                        pipe.write(header(nonce,r.sequence,1));
                        long deadline=System.nanoTime()+TimeUnit.MINUTES.toNanos(3);
                        DataInputStream response=response(pipe.read(36,deadline,channel::isActive),nonce,r.sequence);
                        int status=response.readInt(),length=response.readInt();
                        if(length<0||length>7000)throw new IOException("Oversized capture");
                        byte[] body=pipe.read(length,deadline,channel::isActive);
                        if(status!=1){r.cancel();notice.set("Automatic capture failed; previous durable layout retained.");continue;}
                        Map<Integer,Integer> values=decode(body,schema);
                        if(!r.claim())continue; // X/Escape/open-only snapshots never reach storage.
                        if(!channel.isActive()||!pipe.ownsGameSocket(pid,remote,local))throw new IOException("Game session ended before storage");
                        Native950WorkspaceStore.Record previous=store.load(account);
                        long revision=previous==null?1:Math.addExact(previous.revision,1);
                        store.save(new Native950WorkspaceStore.Record(account,revision,values,IMAGE));
                        notice.set("Workspace saved durably (disposable gate, revision "+revision+").");
                        System.out.println("[WorkspaceCaptureGate] account=layoutgate2 durable revision="+revision+" pid="+pid);
                        pipe.write(header(nonce,r.sequence,2)); // Receipt only after forced atomic replacement.
                    }
                }catch(IOException failure) {
                    if(channel.isActive()){notice.set("Workspace capture disconnected/refused; no new durable-save confirmation.");System.err.println("[WorkspaceCaptureGate] "+failure.getMessage());Thread.sleep(500);}
                }
            }
        }catch(Exception failure){notice.set("Workspace capture unavailable; existing stored layout retained.");System.err.println("[WorkspaceCaptureGate] stopped: "+failure);}
    }
    static byte[] header(byte[] nonce,long sequence,int kind) throws IOException {
        if(nonce.length!=16||sequence<1)throw new IOException("Invalid capture identity");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();DataOutputStream out=new DataOutputStream(bytes);
        out.writeInt(MAGIC);out.write(nonce);out.writeLong(sequence);out.writeInt(kind);return bytes.toByteArray();
    }
    static DataInputStream response(byte[] bytes,byte[] nonce,long sequence) throws IOException {
        if(bytes.length!=36||nonce.length!=16)throw new IOException("Capture header bounds");
        DataInputStream in=new DataInputStream(new ByteArrayInputStream(bytes));
        if(in.readInt()!=MAGIC)throw new IOException("Wrong capture protocol");
        byte[] echo=new byte[16];in.readFully(echo);
        if(!java.security.MessageDigest.isEqual(nonce,echo)||in.readLong()!=sequence)throw new IOException("Stale/cross-session capture");
        return in;
    }
    static Map<Integer,Integer> decode(byte[] body,JsonObject schema) throws IOException {
        if(body.length>7000)throw new IOException("Capture size bound");
        DataInputStream in=new DataInputStream(new ByteArrayInputStream(body));
        long entered=in.readLong(),exited=in.readLong(),captured=in.readLong();
        if(entered<=0||exited<=entered||captured<exited)throw new IOException("Missing native entry/exit fence");
        if(in.readUnsignedShort()!=912)throw new IOException("Wrong capture count");
        Map<Integer,Integer> values=new TreeMap<>();int last=-1;
        for(int i=0;i<912;i++) {
            int id=in.readUnsignedShort(),tag=in.readUnsignedByte();
            if(id<=last||tag>1)throw new IOException("Unknown type/duplicate capture ID");last=id;
            values.put(id,tag==0?null:in.readInt());
        }
        if(in.available()!=0)throw new IOException("Trailing capture data");
        try{Native950LayoutFixture.workspacePlan(schema,new Native950WorkspaceStore.Record("layoutgate2",1,values,IMAGE).snapshot());}
        catch(RuntimeException bad){throw new IOException("Invalid native workspace",bad);}
        return values;
    }
}
