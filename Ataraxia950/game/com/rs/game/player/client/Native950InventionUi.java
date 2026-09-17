package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;
import java.security.MessageDigest;

/** Paired950 backpack DB row1012: Invention Materials, native icon and drop destination. */
final class Native950InventionUi {
    //16557 mode0 uses child1's background for ops; child2 is only artwork.
    //Native0x140269e4c stores packed child at+0x1c; group0/child1 -> network slot1.
    static final int HASH=(1473<<16)|9, SLOT=1, UNLOCK=30224;
    private static Object verified;
    private int unlocked=-1;
    static boolean target(int hash,int slot,int item){return hash==HASH&&slot==SLOT&&item==-1;}
    void sync(Player player,Channel channel){
        if(Cache.STORE==null||!Cache.isFlatReadOnly())return;
        int value=Native950Invention.requirement(player)==null?1:0;
        if(unlocked==value)return;
        verify(); unlocked=value;
        //16564 reads30224 before showing DB row1012; parent5987 bit7, not910's unlock IDs.
        player.getVarsManager().sendVarBit(UNLOCK,value);
        channel.write(Native950Packets.interfaceEvents(1473,9,SLOT,SLOT,(1<<1)|(1<<21)));
        channel.write(Native950Packets.runClientScript(16558,HASH,2));
    }
    static synchronized void verify(){
        if(verified==Cache.STORE)return;
        pin(3,1473,9,"81d30d654505ade03e70ed1a54d5538ea8ef215c1a1c89d893f342879284aa3d");
        pin(2,41,1012,"48753b776c68a43e4a979b6a2833520e027ab913b7b67143e3ca19731df00ca6");
        pin(2,69,30224,"46c0427eec466154271430f3be7c69598bf9338a4c45e6a006f170348c786a25");
        pin(12,16554,0,"163fc8c5e2a9dfbaa0b439acc5f5d4bdbf73330703bea865c2d635eb30c726db");
        pin(12,16557,0,"cfd1d73fa7d532e28193f87759d66bec4ebbb1bc9898a13118839bb2ae28d9af");
        pin(12,16564,0,"344e6f026f9f49d462a4ff1f83493cc14b4fb3008c6fd7f1cc2a32f084f5d7cb");
        verified=Cache.STORE;
    }
    private static void pin(int index,int group,int file,String expected){
        try{byte[] raw=Cache.STORE.getIndexes()[index].getFile(group,file);StringBuilder hex=new StringBuilder();
            for(byte b:MessageDigest.getInstance("SHA-256").digest(raw))hex.append(String.format("%02x",b&255));
            if(!expected.equals(hex.toString()))throw new IllegalStateException("Changed950 Invention UI "+index+":"+group+":"+file);
        }catch(java.security.NoSuchAlgorithmException e){throw new AssertionError(e);}
    }
}
