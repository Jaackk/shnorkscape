import com.opennxt.OpenNXT;
import com.opennxt.config.ServerConfig;
import com.opennxt.filesystem.FilesystemFactoryKt;
import com.opennxt.model.lobby.Native950LobbyLayout;
import com.opennxt.model.lobby.LobbyPlayer;
import com.opennxt.net.ConnectedClient;
import com.opennxt.net.Side;
import com.opennxt.net.game.protocol.ProtocolInformation;
import com.opennxt.net.game.pipeline.OpcodeWithBuffer;
import io.netty.channel.embedded.EmbeddedChannel;
import java.nio.file.Paths;

/** Standalone real-cache lobby smoke; no authentication, sockets or player-save writes. */
public class VerifyLobby950 {
 public static void main(String[] args) throws Exception {
  ServerConfig config=new ServerConfig();config.setBuild(950);OpenNXT.INSTANCE.setConfig(config);
  OpenNXT.INSTANCE.setFilesystem(FilesystemFactoryKt.openFilesystem(Paths.get(args[0],"cache")));
  OpenNXT.INSTANCE.setProtocol(new ProtocolInformation(Paths.get(args[0],"OpenNXT/data/prot/950")));
  OpenNXT.INSTANCE.getProtocol().load();
  Native950LobbyLayout.INSTANCE.verifyCache();
  EmbeddedChannel channel=new EmbeddedChannel();
  ConnectedClient client=new ConnectedClient(Side.CLIENT,channel,false,null);
  LobbyPlayer player=new LobbyPlayer(client,"lobby-test");
  player.added();client.flush();
  if(!player.getInterfaces().isOpened(906)||!player.getInterfaces().isOpened(910)||player.getInterfaces().isOpened(814))throw new AssertionError("Wrong lobby ownership");
  if(player.getWorldList().getEntries().length!=1||player.getWorldList().getEntries()[0].getId()!=1)throw new AssertionError("Fictional worlds remain");
  int frames=0;Object value;
  while((value=channel.readOutbound())!=null){if(!(value instanceof OpcodeWithBuffer))throw new AssertionError("Non-packet output");OpcodeWithBuffer packet=(OpcodeWithBuffer)value;System.out.println("LOBBY packet "+packet.getOpcode()+" bytes="+packet.getBuf().readableBytes());packet.getBuf().release();frames++;}
  channel.finishAndReleaseAll();if(frames!=7)throw new AssertionError("Expected root,worldchild,2texts,2hides,tabselection: "+frames);
  System.out.println("PASS paired950 lobby cache pins, actual bootstrap, native World panel, single World1 and seven packets; no account writes or login");
 }
}
