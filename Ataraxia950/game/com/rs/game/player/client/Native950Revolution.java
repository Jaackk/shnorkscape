package com.rs.game.player.client;

import java.util.function.IntPredicate;

/** Ordered selection only; activation must use the same world-thread manual-ability path. */
final class Native950Revolution {
    static int select(int[] structures,int enabledSlots,IntPredicate canExecute){
        if(structures==null)return -1;
        int end=Math.min(structures.length,Math.min(Native950ActionBar.SLOTS,Math.max(0,enabledSlots)));
        for(int i=0;i<end;i++){
            Native950AbilityCatalog.Definition d=Native950AbilityCatalog.get(structures[i]);
            if(d!=null&&d.targetRequired()&&canExecute.test(d.struct))return d.struct;
        }
        return -1;
    }
    private Native950Revolution(){}
}
