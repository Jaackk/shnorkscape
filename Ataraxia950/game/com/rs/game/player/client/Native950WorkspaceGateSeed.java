package com.rs.game.player.client;

/** Offline one-time import of Jack's explicitly account-identified application receipt. */
public final class Native950WorkspaceGateSeed {
    private Native950WorkspaceGateSeed() { }
    public static void main(String[] args) throws Exception {
        if(args.length!=1||!args[0].equals("reviewed-layoutgate2-37768"))
            throw new IllegalArgumentException("Only reviewed-layoutgate2-37768 is accepted");
        java.nio.file.Path root=Native950DisposableWorkspaceRestore.ROOT;
        com.google.gson.JsonObject schema=Native950LayoutFixture.loadSchema(root);
        com.google.gson.JsonObject capture=Native950LayoutFixture.json(Native950LayoutFixture.readPinned(
            root.resolve("logs/workspace-static-v4-37768-C.json"),
            "a3d22bfa661cad064859c892625e04dbe6f85f30327f4716ddef3dc793c428a8",256000));
        Native950WorkspaceStore store=new Native950WorkspaceStore(root.resolve("workspace-state950"),schema);
        Native950WorkspaceStore.Record record=store.capture("layoutgate2",1,capture);
        Native950WorkspaceStore.Record existing=store.load("layoutgate2");
        if(existing!=null) {
            if(existing.revision!=1||!existing.values.equals(record.values))throw new IllegalStateException("Never overwrite an existing different workspace");
        } else store.save(record);
        if(!store.load("layoutgate2").values.equals(record.values))throw new IllegalStateException("Stored workspace readback mismatch");
        System.out.println("PASS account-bound layoutgate2 revision1 workspace sidecar persisted/read back; no player save touched");
    }
}
