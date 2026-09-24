package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Packets;
import java.util.*;

/** Developer-only completion of exact950 quest thresholds and permanent CS15411 unlocks. */
final class Native950CombatProgression {
    private static Object verified;
    private static final int[][] UNLOCKS={
        {6,1},
        {28,1},
        {29,1},
        {30,1},
        {31,1},
        {32,1},
        {33,1},
        {34,1},
        {35,1},
        {36,1},
        {37,1},
        {38,1},
        {39,1},
        {40,1},
        {3901,1},
        {15884,1},
        {15885,1},
        {16374,11},
        {18523,1},
        {18524,1},
        {18525,1},
        {18526,1},
        {18527,1},
        {18528,1},
        {18529,1},
        {21067,1},
        {21068,1},
        {21069,1},
        {22430,1},
        {22464,1},
        {24967,1},
        {28166,1},
        {28742,1},
        {28743,1},
        {28747,1},
        {28754,1},
        {32628,1},
        {34061,1},
        {34065,1},
        {34066,1},
        {34067,1},
        {34068,1},
        {34069,1},
        {34070,1},
        {34071,1},
        {34072,1},
        {34073,1},
        {35826,1},
        {36173,1},
        {36971,1},
        {39926,1},
        {44136,1},
        {44270,1},
        {45680,1},
        {48719,1},
        {48720,1},
        {48721,1},
        {49489,1},
        {49490,1},
        {49491,1},
        {49492,1},
        {49799,1},
        {50371,1},
        {51565,1},
        {51566,1},
        {51567,1},
        {52518,1},
        {52857,1},
        {53270,1},
        {53571,1},
        {53572,1},
        {53573,1},
        {53574,1},
        {53575,1},
        {53576,1},
        {53577,1},
        {53578,1},
        {53579,1},
        {53580,1},
        {53581,1},
        {53582,1},
        {53583,1},
        {53584,1},
        {53585,1},
        {53586,1},
        {53587,3},
        {54631,1},
        {54732,1},
        {55973,1},
        {55974,1},
        {60739,1},
    };
    private Native950CombatProgression(){}
    static void grant(Player player){grant(player,true);}
    static void restore(Player player){grant(player,false);}
    private static void grant(Player player,boolean publish){
        if(!player.isNative950()||!player.isCompT()||Cache.STORE==null||!Cache.isFlatReadOnly())return;
        verify();
        Map<Integer,Integer> vars=new TreeMap<>(),bits=new TreeMap<>();
        for(Native950QuestCatalog.Quest quest:Native950QuestCatalog.all()){
            for(int[] row:quest.varps)vars.merge(row[0],row[2],Math::max);
            for(int[] row:quest.varbits)bits.merge(row[0],row[2],Math::max);
        }
        for(int[] row:UNLOCKS)bits.merge(row[0],row[1],Math::max);
        // CS18289 maps four selections through enum17157. Initialise the empty developer army.
        boolean emptyArmy=true;for(int id=11499;id<=11502;id++)emptyArmy&=player.getVarsManager().getValue(id)==0;
        for(int i=0;i<4;i++)vars.put(11499+i,emptyArmy?i+1:player.getVarsManager().getValue(11499+i));
        vars.put(1297,Native950QuestCatalog.maximumPoints());
        vars.put(423,Native950QuestCatalog.maximumPoints());
        for(Map.Entry<Integer,Integer> row:vars.entrySet()){
            player.getVarsManager().setVar(row.getKey(),row.getValue());
            if(publish&&player.getRealChannel()!=null)player.getRealChannel().write(Native950Packets.varp(row.getKey(),row.getValue()));
        }
        for(Map.Entry<Integer,Integer> row:bits.entrySet()){
            player.getVarsManager().setVarBit(row.getKey(),row.getValue());
            if(publish&&player.getRealChannel()!=null)player.getRealChannel().write(Native950Packets.varbitLarge(row.getKey(),row.getValue()));
        }
    }
    private static synchronized void verify(){
        if(verified==Cache.STORE)return;
        Native950QuestCatalog.verify();
        com.rs.cache.loaders.rs3.RS3ClientScriptMap army=com.rs.cache.loaders.rs3.RS3ClientScriptMap.getMap(17157);
        int[] conjures={48302,48304,48306,31820};
        for(int i=0;i<4;i++)if(!Integer.valueOf(conjures[i]).equals(army.getValues().get((long)i+1)))throw new IllegalStateException("Changed950 army selection table");
        try{
            byte[] raw=Cache.STORE.getIndexes()[12].getFile(15411,0);
            StringBuilder hash=new StringBuilder();
            for(byte b:java.security.MessageDigest.getInstance("SHA-256").digest(raw))hash.append(String.format("%02x",b&255));
            if(!hash.toString().equals("d6d1fe9c0a1ab5753f820a5623022b6b38532c5fe9847a740295a3d9513256c1"))throw new IllegalStateException("Changed950 combat unlock script");
            for(int[] row:UNLOCKS){
                com.rs.cache.loaders.VarBitDefinitions d=com.rs.cache.loaders.VarBitDefinitions.getClientVarpBitDefinitions(row[0]);
                if(d.varDomain!=0||d.baseVar<0||row[1]>(1L<<(d.endBit-d.startBit+1))-1)
                    throw new IllegalStateException("Invalid950 combat unlock "+row[0]);
            }
            verified=Cache.STORE;
        }catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}
    }
}
