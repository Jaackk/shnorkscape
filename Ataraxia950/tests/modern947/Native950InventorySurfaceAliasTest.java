package modern947;

import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Actions.*;
import org.junit.Test;
import static org.junit.Assert.*;

/** The live Hero backpack aliases only its component identity, never another client's claims. */
public final class Native950InventorySurfaceAliasTest {
    private static final int HERO=(1474<<16)|8,BACKPACK=(1473<<16)|5,BANK=(517<<16)|201;
    @Test public void interfaceOptionsKeepEveryUntrustedValueAndDoNotMutateTheOriginal(){
        for(int code:new int[]{18,122,89,100,81,126,49,66,31,59})for(int item:new int[]{0xabcdef,0xffffff}){
            byte[] b={(byte)(item>>>16),(byte)(item>>>8),(byte)item,(byte)(HERO>>>16),(byte)(HERO>>>24),(byte)HERO,(byte)(HERO>>>8),(byte)255,(byte)255};
            InterfaceAction original=(InterfaceAction)Native950Actions.decode(code,b),alias=(InterfaceAction)map(original);
            assertNotSame(original,alias);assertEquals(BACKPACK,alias.componentHash());assertEquals(HERO,original.componentHash());assertEquals(original.option(),alias.option());assertEquals(item==0xffffff?-1:item,alias.itemId());assertEquals(-1,alias.slot());
            assertSame(alias,map(alias));assertSame(original,Native950Actions.aliasInventorySurface(original,HERO,HERO));
        }
    }
    @Test public void dragAliasesEitherEndpointWithoutLosing24BitItemsOrSlotSentinels(){
        for(int source:new int[]{HERO,BANK})for(int target:new int[]{HERO,BANK}){
            byte[] b=new byte[18];b[0]=0x45;b[1]=0x23;v1(b,2,source);b[6]=0x56;b[7]=0x34;b[8]=0x12;b[9]=-1;b[10]=-1;v2(b,11,target);b[15]=-1;b[16]=-1;b[17]=-1;
            DragAction a=(DragAction)Native950Actions.decode(12,b),z=(DragAction)map(a);assertEquals(source==HERO?BACKPACK:source,z.sourceComponentHash());assertEquals(target==HERO?BACKPACK:target,z.targetComponentHash());assertEquals(0x2345,z.sourceSlot());assertEquals(-1,z.targetSlot());assertEquals(0x123456,z.sourceItemId());assertEquals(-1,z.targetItemId());if(source==BANK&&target==BANK)assertSame(a,z);
        }
    }
    @Test public void itemUseCanAliasBothInventoriesOrOnlyOneSide(){
        for(int source:new int[]{HERO,BANK})for(int target:new int[]{HERO,BANK}){
            byte[] b=new byte[18];b[0]=-1;b[1]=0x7f;b[2]=(byte)0xab;b[3]=(byte)0xef;b[4]=(byte)0xcd;v1(b,5,target);b[9]=0x34;b[10]=0x56;v1(b,11,source);b[15]=0x21;b[16]=0x43;b[17]=0x65;
            ItemOnItemAction a=(ItemOnItemAction)Native950Actions.decode(69,b),z=(ItemOnItemAction)map(a);assertEquals(source==HERO?BACKPACK:source,z.sourceHash());assertEquals(target==HERO?BACKPACK:target,z.targetHash());assertEquals(-1,z.sourceSlot());assertEquals(0x3456,z.targetSlot());assertEquals(0xabcdef,z.sourceItemId());assertEquals(0x654321,z.targetItemId());if(source==BANK&&target==BANK)assertSame(a,z);
        }
    }
    @Test public void itemOnObjectRetainsObjectCoordinatesModifierAndItemIdentity(){
        byte[] b=new byte[18];b[0]=-1;b[1]=12;b[2]=(byte)(154+128);b[3]=0;b[4]=27;b[5]=(byte)(185+128);b[6]=12;b[7]=0x56;b[8]=0x34;b[9]=0x12;b[10]=0x78;b[11]=0x56;b[12]=0x34;b[13]=0x12;v1(b,14,HERO);
        ItemOnObjectAction a=(ItemOnObjectAction)Native950Actions.decode(90,b),z=(ItemOnObjectAction)map(a);assertEquals(BACKPACK,z.sourceHash());assertEquals(HERO,a.sourceHash());assertEquals(27,z.sourceSlot());assertEquals(0x123456,z.sourceItemId());assertEquals(0x12345678,z.objectId());assertEquals(3226,z.x());assertEquals(3257,z.y());assertEquals(1,z.modifier());assertSame(z,map(z));
    }
    @Test public void itemOnNpcRetainsNpcIndexModifierAndSentinels(){
        byte[] b={(byte)0xab,(byte)0xcd,-1,-1,-1,-1,-1,-1,(byte)(HERO>>>24),(byte)(HERO>>>16),(byte)(HERO>>>8),(byte)HERO};
        ItemOnNpcAction a=(ItemOnNpcAction)Native950Actions.decode(19,b),z=(ItemOnNpcAction)map(a);assertEquals(BACKPACK,z.sourceHash());assertEquals(-1,z.sourceSlot());assertEquals(-1,z.sourceItemId());assertEquals(0xabcd,z.index());assertEquals(1,z.modifier());assertEquals(HERO,a.sourceHash());assertSame(z,map(z));
    }
    @Test public void unrelatedPacketsAndNonmatchingTargetsRetainObjectIdentity(){
        Action pause=Native950Actions.decode(11,new byte[0]),count=Native950Actions.decode(120,new byte[]{0,0,0,0,0,0,0,5}),dialogue=Native950Actions.decode(101,new byte[]{(byte)(HERO>>>24),(byte)(HERO>>>16),(byte)(HERO>>>8),(byte)HERO,-1,127});
        assertSame(pause,map(pause));assertSame(count,map(count));assertSame(dialogue,map(dialogue));assertNull(map(null));
        InterfaceAction bank=(InterfaceAction)Native950Actions.decode(18,new byte[]{-1,-1,-1,(byte)(BANK>>>16),(byte)(BANK>>>24),(byte)BANK,(byte)(BANK>>>8),0,3});assertSame(bank,map(bank));
    }
    private static Action map(Action a){return Native950Actions.aliasInventorySurface(a,HERO,BACKPACK);}
    private static void v1(byte[] b,int at,int h){b[at]=(byte)(h>>>8);b[at+1]=(byte)h;b[at+2]=(byte)(h>>>24);b[at+3]=(byte)(h>>>16);}
    private static void v2(byte[] b,int at,int h){b[at]=(byte)(h>>>16);b[at+1]=(byte)(h>>>24);b[at+2]=(byte)h;b[at+3]=(byte)(h>>>8);}
}
