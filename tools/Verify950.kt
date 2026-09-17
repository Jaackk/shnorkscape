import com.opennxt.net.login.Native950Ribbon
import com.opennxt.net.login.Native950CacheContent
import com.opennxt.net.game.serverprot.RebuildNormal
import com.opennxt.net.login.Native950InterfaceBootstrap
import com.rs.game.player.client.Native950InventoryMenu
import com.opennxt.model.world.Native950WorldBootstrap
import com.opennxt.model.world.Native947WorldBootstrap
import com.opennxt.model.InterfaceHash
import com.opennxt.net.buf.GamePacketBuilder
import com.opennxt.net.game.protocol.PacketFieldDeclaration
import com.opennxt.net.game.serverprot.ifaces.*
import com.opennxt.net.game.serverprot.RunClientScript
import io.netty.buffer.ByteBufUtil
import java.nio.file.Files
import java.nio.file.Path
fun main() {
 fun fields(name:String)=Files.readAllLines(Path.of("OpenNXT/data/prot/950/serverProt/$name.txt")).filter{it.isNotBlank()}.map{PacketFieldDeclaration.fromString(it)}.toTypedArray()
 fun verify(name:String,expected:String,write:(GamePacketBuilder)->Unit) {
  val b=GamePacketBuilder();try {write(b);val actual=ByteBufUtil.hexDump(b.buffer);check(actual==expected){"$name: $actual != $expected"};println("PASS $name (${b.length} bytes)")}finally{b.buffer.release()}
 }
 // Independent wire vectors derived from the950native receive code, not codec round trips.
 verify("IF_OPENTOP", "8a0300"+"00".repeat(16)){IfOpenTop.Codec(fields("IF_OPENTOP")).encode(IfOpenTop(906),it)}
 verify("IF_OPENSUB", "038a0025"+"00".repeat(12)+"ae037f"+"00".repeat(4)){IfOpenSub.Codec(fields("IF_OPENSUB")).encode(IfOpenSub(814,true,InterfaceHash((906 shl 16) or 37)),it)}
 verify("IF_SETHIDE", "3344112201"){IfSethide.Codec(fields("IF_SETHIDE")).encode(IfSethide(InterfaceHash(0x11223344),true),it)}
 verify("RUNCLIENTSCRIPT mixed arguments", "69730074657374001122334410203040"){RunClientScript.Codec.encode(RunClientScript(0x10203040,arrayOf(0x11223344,"test")),it)}
 verify("RUNCLIENTSCRIPT lobby init", "0000002ab8"){RunClientScript.Codec.encode(RunClientScript(10936),it)}
 // Bytes 0..1 are chunkY (ushort128) and bytes 6..7 chunkX (plain ushort): bytes 6..7 land in
 // scene slot 0 (0x1400f6fe5), the axis bounds-checked against +0x14034 and fed by hash1's high
 // 14 bits, and TileLocation.tileHash packs x high. The declaration used to name these the other
 // way round, which is invisible only while chunkX == chunkY. This vector is deliberately
 // off-diagonal so a transposition cannot pass.
 verify("REBUILD_NORMAL 950 header", "011705870000013a01da800000007fffffff") {
  RebuildNormal.Codec(fields("REBUILD_NORMAL")).encode(RebuildNormal(0,314,0,407,7,5,474,Int.MIN_VALUE,Int.MAX_VALUE),it)
 }
 // The raw builder used for scene re-centring must agree with the codec byte for byte.
 check(ByteBufUtil.hexDump(io.netty.buffer.Unpooled.wrappedBuffer(
   Native950WorldBootstrap.rebuildHeader950(314,407,7,474,Int.MIN_VALUE,Int.MAX_VALUE)))
   =="011705870000013a01da800000007fffffff"){"raw rebuild header disagrees with the codec"}
 println("PASS REBUILD_NORMAL raw header matches the codec")
 // 0x140B5FEC9 holds transform 0x02, so 0x14010DE7C adds 0x80 to every body byte on receive.
 check(ByteBufUtil.hexDump(Native950WorldBootstrap.initialAppearance(byteArrayOf(1,2,3)))=="c07ff400002083818283")
 println("PASS PLAYER_INFO 950 appearance mask, length and body bias")
 // 0x140131C60-0x140131C8B: unsigned LEB128, low 7-bit group first, continue while byte > 0x7F.
 fun slot(v:Int)=ByteBufUtil.hexDump(io.netty.buffer.Unpooled.wrappedBuffer(Native950WorldBootstrap.encodeWearposSlot(v)))
 check(slot(0)=="00"){"empty slot: "+slot(0)}
 check(slot(2+3)=="05"){"kit 3: "+slot(5)}
 check(slot(2+18)=="14"){"kit 18: "+slot(20)}
 check(slot(2+42)=="2c"){"kit 42: "+slot(44)}
 check(slot(0x7F)=="7f"){"varint boundary low: "+slot(0x7F)}
 check(slot(0x80)=="8001"){"varint boundary high: "+slot(0x80)}
 check(slot(0x800)=="8010"){"item base: "+slot(0x800)}
 check(slot(0x800+1)=="8110"){"item 1: "+slot(0x801)}
 println("PASS wearpos slot LEB128 vectors")
 // PLAYER_INFO type-3 short relocate. Decoder 0x140126030; style table 0x140C6EC88 index 2 = WALK.
 fun step(dx:Int,dy:Int,style:Int=2)=ByteBufUtil.hexDump(io.netty.buffer.Unpooled.wrappedBuffer(Native950WorldBootstrap.walkStep(dx,dy,style)))
 check(step(1,0)=="b202007ff4"){"east: "+step(1,0)}
 check(step(-1,1)=="b23e107ff4"){"north-west: "+step(-1,1)}
 check(step(2,0,3)=="b304007ff4"){"run east: "+step(2,0,3)}
 check(ByteBufUtil.hexDump(io.netty.buffer.Unpooled.wrappedBuffer(Native950WorldBootstrap.idle()))=="007ff4")
 println("PASS PLAYER_INFO walk step vectors")
 // Walk request, sender 0x1400e46b0: y BE u16, (mod+128), (x+128), x>>>8.
 val walk=Native950WorldBootstrap.readWalkRequest950(88, byteArrayOf(0x0C,0x92.toByte(),0x80.toByte(),0x16,0x0C))
 check(walk!=null && walk.x==3222 && walk.y==3218 && walk.modifier==0 && !walk.minimap){"walk decode: "+walk}
 check(Native950WorldBootstrap.readWalkRequest950(88, byteArrayOf(0,0,0,0))==null){"short frame must be declined"}
 check(Native950WorldBootstrap.readWalkRequest950(3, byteArrayOf(0,0,0x80.toByte(),0x80.toByte(),0))==null){"947 opcode must be declined"}
 // The 947 formula on the same bytes must NOT agree - this guards against re-porting it.
 val legacy=Native947WorldBootstrap.readWalkRequest(3, byteArrayOf(0x0C,0x92.toByte(),0x80.toByte(),0x16,0x0C))
 check(legacy==null || legacy.x!=3222 || legacy.y!=3218){"947 decode must differ from 950 on the same payload"}
 println("PASS walk request decode vectors")
 // IF_SETEVENTS parser 0x140107970: mask intv1, toSlot ushort, fromSlot ushort128, parent intle.
 verify("IF_SETEVENTS backpack", "37fe006d001b00800500c105"){IfSetevents.Codec(fields("IF_SETEVENTS")).encode(IfSetevents(InterfaceHash((1473 shl 16) or 5),0,27,Native950InventoryMenu.EVENT_MASK),it)}
 verify("IF_SETEVENTS run orb 1465:15", "00020000ffffff7f0f00b905"){IfSetevents.Codec(fields("IF_SETEVENTS")).encode(IfSetevents(InterfaceHash((1465 shl 16) or 15),-1,-1,2),it)}
 println("PASS IF_SETEVENTS 950 wire vectors")
 // Inspect the actual bridge bootstrap, independently of the Java menu constant's own tests.
 val menuSlots=(Native950InterfaceBootstrap.hiddenSlots+listOf(1000,1004,2,3,18)).distinct()
   .mapIndexed { index,key -> key to Native950InterfaceBootstrap.Slot((1477 shl 16) or (100+index),(1477 shl 16) or (400+index)) }.toMap()
 val backpackEvents=Native950InterfaceBootstrap.packets(menuSlots).filter {
   it.type().opcode()==24 && ByteBufUtil.hexDump(io.netty.buffer.Unpooled.wrappedBuffer(it.payload())).endsWith("0500c105")
 }
 check(backpackEvents.size==1 && backpackEvents.single().payload().contentEquals(ByteBufUtil.decodeHexDump("37fe006d001b00800500c105"))) { "backpack bootstrap must enable native Use and all authored operations" }
 println("PASS actual950 backpack bootstrap Use source and target masks")
 // The bank's modal anchor is selected by the same cache layout flag as CS10906.
 val bankHost=(1477 shl 16) or 695
 val bankAnchor=(1477 shl 16) or 693
 check(Native950CacheContent.bankMount(bankHost,bankAnchor,21329)==bankAnchor)
 check(Native950CacheContent.bankMount(bankHost,bankAnchor,0)==bankHost)
 val bankUi=Native950CacheContent.bankUi(bankAnchor,bankAnchor)
 val bankMounts=bankUi.open.filter { it.type()==com.rs.network.protocol.modern950.Native950Protocol.ServerPacket.IF_OPENSUB }
 check(bankMounts.size==1 && bankMounts.single().payload().contentEquals(com.rs.network.protocol.modern950.Native950Packets.openSub(1477,693,517,false).payload()))
 check(bankUi.close.first().payload().contentEquals(com.rs.network.protocol.modern950.Native950Packets.closeSub(1477,693).payload()))
 println("PASS actual950 bank single modal-anchor mount and matching close")
 // Interface clicks, pinned to real captures taken from a live 950 session.
 fun btn(hex:String)=Native950WorldBootstrap.readInterfaceButton(ByteBufUtil.decodeHexDump(hex))
 val escape=btn("ffffffc5050800ffff"); check(escape!=null&&escape.interfaceId==1477&&escape.component==8&&escape.slot==-1&&escape.item==-1){"escape: "+escape}
 val gear=btn("ffffff970500000007"); check(gear!=null&&gear.interfaceId==1431&&gear.component==0&&gear.slot==7){"gear: "+gear}
 val orb=btn("ffffffb9050f00ffff"); check(orb!=null&&orb.interfaceId==1465&&orb.component==15){"run orb: "+orb}
 val lobby=btn("ffffff8a032000ffff"); check(lobby!=null&&lobby.interfaceId==906&&lobby.component==32){"lobby world select: "+lobby}
 check(btn("ffffffc50508")==null){"short button frame must be declined"}
 println("PASS interface button decode against live captures")
 // VARBIT_SMALL opcode 28 and CLIENT_SETVARC_LARGE opcode 119 payloads.
 fun hex(a:ByteArray)=ByteBufUtil.hexDump(io.netty.buffer.Unpooled.wrappedBuffer(a))
 check(hex(Native950WorldBootstrap.encodeVarbit(18797,1))=="816d49"){"varbit 18797: "+hex(Native950WorldBootstrap.encodeVarbit(18797,1))}
 check(hex(Native950WorldBootstrap.encodeVarbit(39917,98))=="e2ed9b"){"varbit 39917: "+hex(Native950WorldBootstrap.encodeVarbit(39917,98))}
 check(hex(Native950WorldBootstrap.encodeVarc(2911,9))=="0bdf00000900"){"varc 2911=9: "+hex(Native950WorldBootstrap.encodeVarc(2911,9))}
 println("PASS varbit and varc payload vectors")
 // Collision: index 5 group key, and fail-closed behaviour for an unloaded map square.
 check(com.opennxt.model.world.Collision950.mapGroup(50,50)==6450){"Lumbridge group"}
 check(com.opennxt.model.world.Collision950.mapGroup(0,0)==0){"origin group"}
 check(com.opennxt.model.world.Collision950.mapGroup(127,199)==25599){"max group"}
 // An unread region must read as fully blocked, never as walkable.
 check(com.opennxt.model.world.Collision950.mask(0,3222,3218)==-1){"unloaded region must fail closed"}
 val a=com.opennxt.model.world.TileLocation(3222,3218,0)
 val b=com.opennxt.model.world.TileLocation(3223,3218,0)
 check(!com.opennxt.model.world.Collision950.checkWalkStep(a,b)){"step into an unloaded region must be refused"}
 check(!com.opennxt.model.world.Collision950.checkWalkStep(a,com.opennxt.model.world.TileLocation(3225,3218,0))){"non-adjacent step must be refused"}
 check(!com.opennxt.model.world.Collision950.checkWalkStep(a,com.opennxt.model.world.TileLocation(3222,3218,1))){"plane change must be refused"}
 println("PASS collision group key and fail-closed behaviour")
 // Inspect actual ribbon initialization, including unsigned selections above127 and list terminator.
 val ribbon=Native950Ribbon.initialization()
 val expectedButtons=listOf(129,130,131,132,133,136,137,138,1,3,4,19,0)
 expectedButtons.forEachIndexed { i,value ->
  val expected=if(value>127) com.rs.network.protocol.modern950.Native950Packets.varcBitLarge(21788+i,value)
      else com.rs.network.protocol.modern950.Native950Packets.varcBitSmall(21788+i,value)
  check(ribbon[i].type()==expected.type() && ribbon[i].payload().contentEquals(expected.payload())) { "ribbon entry $i" }
 }
 val actors=com.rs.network.protocol.modern950.Native950Packets.interfaceEvents(1431,0,0,7,2)
 check(ribbon.count { it.type()==actors.type() && it.payload().contentEquals(actors.payload()) }==1)
 println("PASS actual950 twelve-icon ribbon, unsigned selections, terminator and eight management actors")
 com.rs.cache.Cache.initFlatReadOnly(Path.of("cache").toAbsolutePath())
 Native950Ribbon.verify()
 println("PASS expanded ribbon pins against paired950 cache")
}
