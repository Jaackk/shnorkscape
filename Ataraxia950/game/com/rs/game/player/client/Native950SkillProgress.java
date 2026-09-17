package com.rs.game.player.client;
import com.rs.game.player.Player;
import java.io.*;
import java.util.*;

/** Schema4 skill state. Stored atomically alongside XP/containers, never in a separate sidecar. */
public final class Native950SkillProgress {
    public static final int MAX_BYTES=12288;
    public static final Native950SkillProgress EMPTY=new Native950SkillProgress(new int[128],new int[64],Collections.<Native950Farming.Plot>emptyList(),0,0,false);
    private final int[] invention,archaeology,excavation,familiar,toolbelt;
    private final boolean[] boons;
    public final int inventionResearch;
    private final List<Native950Farming.Plot> plots;
    public final int dungeonTokens,dungeonCompletions;
    public final boolean interruptedDungeon;
    public Native950SkillProgress(int[] invention,int[] archaeology,List<Native950Farming.Plot> plots,int tokens,int completions,boolean interrupted){
        this(invention,archaeology,plots,tokens,completions,interrupted,new int[2]);
    }
    public Native950SkillProgress(int[] invention,int[] archaeology,List<Native950Farming.Plot> plots,int tokens,int completions,boolean interrupted,int[] excavation){
        this(invention,archaeology,plots,tokens,completions,interrupted,excavation,0,new boolean[12],new int[2]);
    }
    public Native950SkillProgress(int[] invention,int[] archaeology,List<Native950Farming.Plot> plots,int tokens,int completions,boolean interrupted,int[] excavation,int research,boolean[] boons,int[] familiar){
        this(invention,archaeology,plots,tokens,completions,interrupted,excavation,research,boons,familiar,new int[0]);
    }
    public Native950SkillProgress(int[] invention,int[] archaeology,List<Native950Farming.Plot> plots,int tokens,int completions,boolean interrupted,int[] excavation,int research,boolean[] boons,int[] familiar,int[] toolbelt){
        this.toolbelt=Native950Toolbelt.validateSnapshot(toolbelt);
        this.inventionResearch=Native950InventionResearch.validate(research);
        this.boons=Native950Divination.validateBoons(boons);
        this.familiar=Native950Familiars.validateSnapshot(familiar);
        this.invention=validated(invention,128);this.archaeology=validated(archaeology,64);
        this.excavation=Native950Archaeology.validateExcavationProgress(excavation);
        if(plots==null||plots.size()>256||tokens<0||completions<0)throw new IllegalArgumentException("Invalid skill progress.");
        Set<String> keys=new HashSet<>();List<Native950Farming.Plot> copied=new ArrayList<>();
        for(Native950Farming.Plot plot:plots){
            if(!Native950Farming.valid(plot)||!keys.add(Integer.toString(plot.objectId)))throw new IllegalArgumentException("Invalid or duplicate Farming patch.");
            copied.add(plot);
        }
        this.plots=Collections.unmodifiableList(copied);dungeonTokens=tokens;dungeonCompletions=completions;interruptedDungeon=interrupted;
    }
    private static int[] validated(int[] values,int size){
        if(values==null||values.length!=size)throw new IllegalArgumentException("Invalid material storage.");
        int[] result=values.clone();for(int v:result)if(v<0)throw new IllegalArgumentException("Negative material count.");return result;
    }
    public int[] invention(){return invention.clone();}public int[] archaeology(){return archaeology.clone();}public int[] excavation(){return excavation.clone();}
    public List<Native950Farming.Plot> plots(){return plots;}
    public boolean[] boons(){return boons.clone();}public int[] familiar(){return familiar.clone();}public int[] toolbelt(){return toolbelt.clone();}
    public static Native950SkillProgress capture(Player p){return new Native950SkillProgress(Native950Invention.materials(p),
        Native950Archaeology.materials(p),Native950Farming.snapshot(p),Native950Dungeoneering.tokens(p),
        Native950Dungeoneering.completed(p),Native950Dungeoneering.interrupted(p),Native950Archaeology.excavationProgress(p),Native950InventionResearch.tier(p),Native950Divination.boons(p),Native950Familiars.snapshot(p),Native950Toolbelt.snapshot(p));}
    public void restore(Player p){
        Native950InventionResearch.restore(p,inventionResearch);Native950Divination.restoreBoons(p,boons());Native950Familiars.restore(p,familiar());Native950Toolbelt.restore(p,toolbelt());
        Native950Invention.restoreMaterials(p,invention());Native950Archaeology.restoreMaterials(p,archaeology());
        Native950Archaeology.restoreExcavationProgress(p,excavation());
        Native950Farming.restore(p,plots);Native950Dungeoneering.restoreProgress(p,dungeonTokens,dungeonCompletions,interruptedDungeon);
    }
    public byte[] encode() {
        try{ByteArrayOutputStream bytes=new ByteArrayOutputStream();DataOutputStream out=new DataOutputStream(bytes);
            out.writeInt(4);for(int v:invention)out.writeInt(v);for(int v:archaeology)out.writeInt(v);for(int v:excavation)out.writeInt(v);
            out.writeInt(dungeonTokens);out.writeInt(dungeonCompletions);out.writeBoolean(interruptedDungeon);out.writeInt(plots.size());
            for(Native950Farming.Plot p:plots){out.writeInt(p.objectId);out.writeInt(p.x);out.writeInt(p.y);out.writeInt(p.plane);
                out.writeInt(p.seedId);out.writeLong(p.plantedAt);out.writeBoolean(p.cleared);out.writeInt(p.compost);out.writeInt(p.harvestRemaining);}
            out.writeByte(inventionResearch);int boonBits=0;for(int i=0;i<boons.length;i++)if(boons[i])boonBits|=1<<i;out.writeShort(boonBits);
            out.writeInt(familiar[0]);out.writeInt(familiar[1]);
            out.writeShort(toolbelt.length);for(int id:toolbelt)out.writeInt(id);
            out.flush();if(bytes.size()>MAX_BYTES)throw new IllegalArgumentException("Skill progress exceeds limit.");return bytes.toByteArray();
        }catch(IOException impossible){throw new AssertionError(impossible);}
    }
    public static Native950SkillProgress decode(DataInputStream in)throws IOException {
        int version=in.readInt();if(version<1||version>4)throw new IOException("Unsupported skill progress format.");
        int[] inv=new int[128],arch=new int[64];for(int i=0;i<inv.length;i++)inv[i]=in.readInt();for(int i=0;i<arch.length;i++)arch[i]=in.readInt();
        int[] excavation=new int[2];if(version>=2)for(int i=0;i<excavation.length;i++)excavation[i]=in.readInt();
        int tokens=in.readInt(),completed=in.readInt(),interrupted=in.readUnsignedByte(),count=in.readInt();
        if(interrupted>1||count<0||count>256)throw new IOException("Invalid saved skill state.");
        List<Native950Farming.Plot> plots=new ArrayList<>();
        for(int i=0;i<count;i++){
            int id=in.readInt(),x=in.readInt(),y=in.readInt(),plane=in.readInt(),seed=in.readInt();long planted=in.readLong();
            int cleared=in.readUnsignedByte(),compost=in.readInt(),remaining=in.readInt();if(cleared>1)throw new IOException("Invalid patch flag.");
            plots.add(new Native950Farming.Plot(id,x,y,plane,seed,planted,cleared==1,compost,remaining));
        }
        int research=0;boolean[] boons=new boolean[12];int[] familiar=new int[2];
        if(version>=3){research=in.readUnsignedByte();int bits=in.readUnsignedShort();if((bits&~4095)!=0)throw new IOException("Invalid Divination boon flags.");for(int i=0;i<12;i++)boons[i]=(bits&(1<<i))!=0;familiar[0]=in.readInt();familiar[1]=in.readInt();}
        int[] toolbelt=new int[0];
        if(version>=4){int size=in.readUnsignedShort();if(size>256)throw new IOException("Too many saved toolbelt items.");toolbelt=new int[size];for(int i=0;i<size;i++)toolbelt[i]=in.readInt();}
        return new Native950SkillProgress(inv,arch,plots,tokens,completed,interrupted==1,excavation,research,boons,familiar,toolbelt);
    }
    @Override public boolean equals(Object other){return other instanceof Native950SkillProgress&&Arrays.equals(encode(),((Native950SkillProgress)other).encode());}
    @Override public int hashCode(){return Arrays.hashCode(encode());}
}
