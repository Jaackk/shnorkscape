package com.rs.game.player.client;

import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;
import java.util.Map;

/** Native DB rows1303/1307/2089; CS2526 getters and enum17328/13443/15425. */
final class Native950CombatPreferences {
    static final int NUMERIC=55114, SHIFT_DROP=54933, PRESET_SWITCH=39242, BIND_DISABLED=39243;
    static final int NUMERIC_ROW=10496, SHIFT_ROW=9474, BIND_BASE=10752;
    private static final int[] STYLE={39244,39245,39246,39247,39248,39249,39250,39251,39252,39253,44722,44723,44724,49961,49962,53210,53211,53213};
    private static final int[] HUD={44725,44726,44727,44728,44729,44730,44731,44732,44733,44734,44735,44736,44737,49963,49964,53212,53214,53215};
    private static final int[] PRESET={44738,44739,44740,44741,44742,44743,44744,44745,44746,44747,44748,44749,44750,49960,49965,53216,53218,53219};
    final int[] styles=new int[18], bars=new int[18];
    int numeric=2;
    boolean shiftDrop=true, bindingEnabled, presetSwitch=true;
    private int selected=-1;
    void close(){selected=-1;}
    boolean select(Player p,Channel c,int row){
        selected=-1;
        if(row==SHIFT_ROW){shiftDrop=!shiftDrop;sync(p,c);ack(c);return true;}
        if(row==BIND_BASE+1){presetSwitch=!presetSwitch;sync(p,c);ack(c);return true;}
        if(row==BIND_BASE+2){bindingEnabled=!bindingEnabled;sync(p,c);ack(c);return true;}
        if(row==NUMERIC_ROW||bindingField(row)>=0){selected=row;return true;}
        return false;
    }
    boolean value(Player p,Channel c,int value){
        int row=selected;selected=-1;
        if(row==NUMERIC_ROW){if(value<0||value>2)return false;numeric=value;}
        else {
            int field=bindingField(row);if(field<0)return false;
            int index=field/3,kind=field%3;
            if(kind==0){if(value<0||value>12)return false;styles[index]=value;}
            else if(kind==1){if(value!=0){c.write(Native950Packets.gameMessage(0,"This build supports bindings for the main action bar only."));sync(p,c);ack(c);return true;}}
            else {if(value<0||value>Native950ActionBar.BARS){c.write(Native950Packets.gameMessage(0,"Choose a saved action bar preset from 1 to 4, or None."));sync(p,c);ack(c);return true;}bars[index]=value;}
        }
        sync(p,c);ack(c);return true;
    }
    private static int bindingField(int row){int offset=row-BIND_BASE-4;if(offset<0)return -1;int index=offset/4,kind=offset%4;return index<18&&kind<3?index*3+kind:-1;}
    private static void ack(Channel c){c.write(Native950Packets.runClientScript(2929));}
    void sync(Player p,Channel c){
        publish(p,c,NUMERIC,numeric);publish(p,c,SHIFT_DROP,shiftDrop?1:0);
        publish(p,c,PRESET_SWITCH,presetSwitch?1:0);publish(p,c,BIND_DISABLED,bindingEnabled?0:1);
        for(int i=0;i<18;i++){publish(p,c,STYLE[i],styles[i]);publish(p,c,HUD[i],0);publish(p,c,PRESET[i],bars[i]);}
    }
    private static void publish(Player p,Channel c,int bit,int value){if(p!=null)p.getVarsManager().setVarBit(bit,value);c.write(Native950Packets.varbitSmall(bit,value));}
    int boundBar(int style,boolean twoHanded,boolean dual){
        if(!bindingEnabled||style<0||style>3)return -1;
        int any=style*3+1,specific=twoHanded?any+2:dual?any+1:-1;
        for(int i=0;i<18;i++)if(bars[i]>0&&(styles[i]==specific||styles[i]==any))return bars[i]-1;
        return -1;
    }
    void equipmentChanged(Player p,boolean preset){
        if(!bindingEnabled||preset&&!presetSwitch||p.getRealChannel()==null)return;
        int main=p.getEquipment().getWeaponId();if(main<0)return;
        try{
            Native950EquipmentTypes.Type type=Native950EquipmentTypes.resolve(main);
            int style=Native950CombatStyles.classify(Native950CacheItems.definition(main));
            int off=p.getEquipment().getShieldId();
            boolean dual=off>=0&&Native950CombatStyles.classify(Native950CacheItems.definition(off))==style;
            int bar=boundBar(style,type!=null&&type.twoHanded,dual);
            if(bar>=0&&bar!=p.getNative950ActionBar().activeBar())p.getNative950ActionBar().setActiveBar(p,p.getRealChannel(),bar);
        }catch(IllegalArgumentException unavailable){/* Unsupported equipment has no binding. */}
    }
    // Base65 packs five (style0..12,preset0..4) tuples per positive int. Three extra keys
    // plus the unused high bits of actionBar.active keep complete profiles within48 keys.
    private int pack(int from,int count){int value=0;for(int i=from+count-1;i>=from;i--)value=value*65+styles[i]*5+bars[i];return value;}
    private void unpack(int from,int count,int value){if(value<0)value=0;for(int i=from;i<from+count;i++){int v=value%65;styles[i]=v/5;bars[i]=v%5;value/=65;}}
    int save(Map<String,Integer> s,int active){for(int i=0;i<3;i++)s.put("actionBar.binding."+i,pack(i*5,5));return active|(pack(15,3)<<2);}
    int flags(){return numeric|(shiftDrop?4:0)|(bindingEnabled?8:0)|(presetSwitch?16:0)|32;}
    void restore(Map<String,Integer> s,int active,int flags){
        for(int i=0;i<3;i++)unpack(i*5,5,s.getOrDefault("actionBar.binding."+i,0));unpack(15,3,active>>>2);
        if((flags&32)!=0){numeric=Math.min(2,flags&3);shiftDrop=(flags&4)!=0;bindingEnabled=(flags&8)!=0;presetSwitch=(flags&16)!=0;}
    }
}
