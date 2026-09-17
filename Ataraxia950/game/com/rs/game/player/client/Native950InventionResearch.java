package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.InventionDefinitions;
import com.rs.game.Animation;
import com.rs.game.WorldObject;
import com.rs.game.player.InventionManager;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import java.security.MessageDigest;

/** Ordinary junk-reduction research, with950 DB-row requirements and910 progression math.
 * The native optimisation puzzle is not mounted: study awards only the basic (Poor) XP.
 */
public final class Native950InventionResearch {
    private static final String STATE="native950.invention.junkResearch";
    private static final double[] MULTIPLIERS={1,.99,.97,.95,.93,.91,.88,.86,.83,.80};
    private static final int[] LEVELS={34,49,64,69,78,83,91,95,105};
    private static final String[] HASHES={
        "7212868afe65eb9607ab0e6e891bb3a7021e8033d04f953c27fb95e62150da6b",
        "d455b5e53b087bea961aea72dc9352f695e14498d33d99641dec675bb60f4153",
        "984f407602b7d27682d76c02a88c49dba388ff69489b59b0b74a1ab891f38294",
        "302da7dc57127932f847dfb0fc423290086884c7bd275ce07a11e0d3fbd545e8",
        "70be2788080d36944ae3c1627d3743b4ad58424686aa4a197d3316d24573f41c",
        "91309bbb9c5b8230cdb002797ab01a9732d9bda6c94176c4bfbb32671c19fbc7",
        "4181d6fe79b2d1b35e9ac71fe7368b60f71249faf27ce6ab3841453d425ea74d",
        "80f21356b5707c6e3aa8ee809f8386d0f3194634d32155b0cf9fc0085d8a7194",
        "c8776f4755a45e5f3adab1dfe0986b83608c6ccf1d3b0e87a7ce76a764e908a6"};
    private static Object verifiedStore;
    private Native950InventionResearch(){}
    public static int validate(int tier){if(tier<0||tier>9)throw new IllegalArgumentException("Invalid Invention research tier");return tier;}
    public static int tier(Player p){Object value=p.getTemporaryAttributtes().get(STATE);return value instanceof Integer?validate((Integer)value):0;}
    public static void restore(Player p,int tier){p.getTemporaryAttributtes().put(STATE,validate(tier));}
    public static double multiplier(int tier){return MULTIPLIERS[validate(tier)];}
    public static double effectiveJunk(double base,int tier){if(!Double.isFinite(base)||base<0||base>100)throw new IllegalArgumentException("Invalid junk chance");return base*multiplier(tier);}
    public static synchronized void verifyCacheBindings(){
        if(Cache.STORE==null||!Cache.isFlatReadOnly())throw new IllegalStateException("Research requires paired950 cache");
        if(verifiedStore==Cache.STORE)return;
        for(int i=0;i<9;i++)try{
            int row=262+i;byte[] bytes=Cache.STORE.getIndexes()[2].getFile(41,row);
            if(bytes==null)throw new IllegalStateException("Missing research row");
            StringBuilder hash=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(bytes))hash.append(String.format("%02x",b&255));
            InventionDefinitions d=InventionDefinitions.getData(row);
            //950 moved the required level to column9 (the910 manager used8).
            if(!HASHES[i].equals(hash.toString())||!Integer.valueOf(LEVELS[i]).equals(d.getDataInIndex(9))
                ||!("Junk chance reduction "+(i+1)).equals(d.getDataInIndex(1))
                ||!Integer.valueOf(56+i).equals(d.getDataInIndex(0))
                ||(i>0&&!Integer.valueOf(55+i).equals(d.getDataInIndex(12))))throw new IllegalStateException("Research data changed: "+row);
        }catch(Exception e){throw new IllegalStateException("Unverified950 Invention research",e);}
        verifiedStore=Cache.STORE;
    }
    public static Native950ProductionMenu.Choice nextChoice(Player p,WorldObject station){
        int done=tier(p);if(done==9)return new Native950ProductionMenu.Choice("Junk reduction: all nine researched",()->p.sendMessage("Your junk chance is reduced by 20% of its base value."));
        verifyCacheBindings();int level=(Integer)InventionDefinitions.getData(262+done).getDataInIndex(9);
        return new Native950ProductionMenu.Choice("Research junk reduction "+(done+1)+" (level "+level+")",()->start(p,station,done+1));
    }
    public static boolean start(Player p,WorldObject station,int next){
        if(p==null||next<1||next>9)return false;verifyCacheBindings();
        String why=refusal(p,station,next);if(why!=null){p.sendMessage(why);return false;}
        return p.getActionManager().setAction(new Study(station,next));
    }
    private static String refusal(Player p,WorldObject station,int next){
        if(!Native950Invention.disassemblyReady(p)||!Native950Invention.reach(p,station))return "Stand beside the inventor's workbench to research.";
        String why=Native950Invention.requirement(p);if(why!=null)return why;
        if(tier(p)!=next-1)return "That research has already been completed, or its prerequisite is missing.";
        int level=(Integer)InventionDefinitions.getData(261+next).getDataInIndex(9);
        if(p.getSkills().getLevel(Skills.INVENTION)<level)return "You need Invention level "+level+" for that research.";
        return null;
    }
    private static final class Study extends Action {
        final WorldObject station;final int next;com.rs.game.WorldTile origin;Object controller;
        Study(WorldObject station,int next){this.station=station;this.next=next;}
        public boolean start(Player p){String why=refusal(p,station,next);if(why!=null){p.sendMessage(why);return false;}
            origin=new com.rs.game.WorldTile(p);controller=p.getControlerManager().getControler();p.sendMessage("Studying junk reduction "+next+" for basic discovery XP.");p.setNextAnimation(new Animation(27997));setActionDelay(p,4);return true;}
        public boolean process(Player p){return p.getActionManager().getAction()==this&&origin!=null&&origin.matches(p)&&controller==p.getControlerManager().getControler()&&refusal(p,station,next)==null;}
        public int processWithDelay(Player p){if(!process(p))return -1;
            int level=(Integer)InventionDefinitions.getData(261+next).getDataInIndex(9);
            restore(p,next);p.getSkills().addXp(Skills.INVENTION,InventionManager.getBluePrintXpForLevel(level)*.2);
            p.sendMessage("Researched junk reduction "+next+". Future disassembly uses "+Math.round(multiplier(next)*100)+"% of the item's base junk chance.");return -1;}
        public void stop(Player p){p.setNextAnimation(new Animation(-1));setActionDelay(p,1);}
    }
}
