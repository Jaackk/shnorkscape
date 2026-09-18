package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.rs3.RS3ClientScriptMap;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Packets;

/** Identity adapter, not a second prayer owner. 950 enum order is not Prayer's logical order. */
public final class Native950PrayerContract {
    // Structures read from the paired950 enums6759/6760. Logical order belongs to Prayer.
    private static final int[][] STRUCTURES={
        {14540,14544,14548,14556,14552,14564,14560,14572,14573,14575,14579,
         14576,14577,14578,14580,14581,14582,14568,14574,14569,14571,14570},
        {14583,14584,14585,14586,14587,14588,14589,14590,14591,14592,14593,
         14594,14595,14596,14597,14598,14599,14600,14601,14602,32273,32274,
         14603,14604,14605,29241,32272,14606,32275,32276,14607,32278,
         14608,14610,14609,35360,35361,35362}
    };
    // Paired950 CS2 7083 maps prayer STRUCTURE to these active-state varbits.
    // The higher curse bits have moved to other parent varps; do not send legacy5859.
    private static final int[][] ACTIVE_BITS={
        {16739,16740,16741,16753,16751,16754,16752,16742,16743,16744,16755,
         16745,16746,16747,16748,16749,16750,16756,16758,16757,16760,16759},
        {16761,16762,16763,16786,16764,16785,16765,16788,16787,16766,16767,
         16768,16769,16770,16771,16772,16781,16773,16782,16774,29066,29067,
         16775,16776,16777,49330,29065,16778,29068,29069,16779,29071,
         16780,16783,16784,34866,34867,34868}
    };
    private Native950PrayerContract(){}
    static int activeBit(boolean curses,int logicalSlot){return ACTIVE_BITS[curses?1:0][logicalSlot];}
    public static int structure(boolean curses,int logicalSlot){
        int[] book=STRUCTURES[curses?1:0];
        if(logicalSlot<0||logicalSlot>=book.length)throw new IllegalArgumentException("Unsupported logical prayer slot");
        return book[logicalSlot];
    }
    static int logicalSlot(boolean curses,int nativeSlot){
        if(Cache.STORE==null||nativeSlot<0||nativeSlot>44)return -1;
        return logicalSlotForStructure(curses,RS3ClientScriptMap.getMap(curses?6760:6759).getIntValue(nativeSlot));
    }
    static int logicalSlotForStructure(boolean curses,int structure){
        int[] book=STRUCTURES[curses?1:0];
        for(int slot=0;slot<book.length;slot++)if(book[slot]==structure)return slot;
        return -1;
    }
    public static void sync(Player player){
        if(Cache.STORE==null||player.getRealChannel()==null)return;
        int changed=0;
        for(int book=0;book<ACTIVE_BITS.length;book++)for(int slot=0;slot<ACTIVE_BITS[book].length;slot++)
            if(publishBit(player,ACTIVE_BITS[book][slot],player.getPrayer().usingPrayer(book,slot)?1:0))changed++;
        publishBit(player,16789,player.getPrayer().isAncientCurses()?1:0);
        Native950BugTest.event(player,"prayer","native-state-sync","changedActiveBits",changed,"contract","950-script7083");
    }
    private static boolean publishBit(Player player,int bit,int value){
        if(player.getVarsManager().getBitValue(bit)==value)return false;
        player.getVarsManager().setVarBit(bit,value);
        player.getRealChannel().write(Native950Packets.varbitSmall(bit,value));
        return true;
    }
    public static void traceState(Player player,int logical){
        int book=player.getPrayer().isAncientCurses()?1:0;
        Native950BugTest.event(player,"prayer","state","logicalSlot",logical,"curses",book==1,
                "activeCount",player.getPrayer().getOnPrayersCount(),
                "active",logical>=0&&logical<STRUCTURES[book].length&&player.getPrayer().usingPrayer(book,logical),
                "points",player.getPrayer().getPrayerpoints());
    }
}
