package com.rs.game.player.client;

import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;
import java.nio.file.*;
import java.util.concurrent.*;

/** Explicit disposable restart gate, disabled in the normal launch profile. */
final class Native950DisposableWorkspaceRestore {
    static final String PROPERTY="ataraxia950.layoutDurabilityGate";
    static final Path ROOT=Paths.get("C:/Games/950OpenSource");
    private static final ExecutorService IO=new ThreadPoolExecutor(1,1,0L,TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<Runnable>(4),r->{Thread t=new Thread(r,"workspace-gate-io");t.setDaemon(true);return t;},new ThreadPoolExecutor.AbortPolicy());
    private final Future<Native950LayoutFixture.Plan> pending;
    private boolean consumed;
    Native950DisposableWorkspaceRestore(Future<Native950LayoutFixture.Plan> pending){this.pending=pending;}
    static boolean allowed(Player p,Channel ch) {
        return Boolean.getBoolean(PROPERTY)&&Native950LayoutFixture.target(p.getUsername())
            &&Native950DevelopmentCommands.allowed(Boolean.getBoolean(Native950DevelopmentCommands.PROPERTY),p.getClientProfile(),ch.remoteAddress());
    }
    static Native950DisposableWorkspaceRestore sceneReady(Player p,Channel ch) {
        if(!allowed(p,ch))return null;
        final String account=Native950Save.canonicalUsername(p.getUsername());
        try { return new Native950DisposableWorkspaceRestore(IO.submit(()->{
            com.google.gson.JsonObject schema=Native950LayoutFixture.loadSchema(ROOT);
            Native950WorkspaceStore store=new Native950WorkspaceStore(ROOT.resolve("workspace-state950"),schema);
            Native950WorkspaceStore.Record record=store.load(account);
            return record==null?null:Native950LayoutFixture.workspacePlan(schema,record.snapshot());
        })); } catch(RejectedExecutionException full) {
            System.err.println("[WorkspaceDurabilityGate] bounded IO queue full; no restore queued");return null;
        }
    }
    // Called only while this one pending load exists; no recurring workspace synchronization.
    boolean finishOnWorldThread(Player p,Channel ch) {
        if(consumed)return true;
        if(!allowed(p,ch)||!ch.isActive()||!p.isActive()||p.hasFinished()){pending.cancel(false);consumed=true;return true;}
        if(!pending.isDone())return false;
        consumed=true;
        try {
            Native950LayoutFixture.Plan plan=pending.get();
            if(plan==null)return true;
            if(p.getInterfaceManager().containsInterface(1475))throw new IllegalStateException("Editor opened before restore");
            for(Native950Packets.Packet packet:plan.packets)ch.write(packet);
            ch.write(Native950Packets.runClientScript(8741,plan.selected));
            System.out.println("[WorkspaceDurabilityGate] account=layoutgate2 restored-setters-and-native-load-queued; visual/readback pending");
        } catch(Exception failure) {
            System.err.println("[WorkspaceDurabilityGate] refused: "+failure);
            ch.write(Native950Packets.gameMessage(0,"Disposable workspace restore refused; healthy defaults retained where no setters were sent."));
        }
        return true;
    }
}
