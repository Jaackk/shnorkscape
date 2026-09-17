import com.rs.cache.Cache;
import com.rs.game.*;
import java.nio.file.Paths;
public final class CookCollisionProbe {
 public static void main(String[] args) throws Exception {
  Cache.initFlatReadOnly(Paths.get(args[0]));
  Region region=World.getRegion(new WorldTile(3209,3215,0).getRegionId(),true);
  for(int y=3218;y>=3212;y--)for(int x=3206;x<=3212;x++) {
   boolean adjacent=Math.max(Math.abs(x-3209),Math.abs(y-3215))==1;
   System.out.println("TILE "+x+","+y+" mask="+Integer.toHexString(World.getMask(0,x,y))+" clear="+World.canMoveNPC(0,x,y,1)+(adjacent?" edgeToCook="+World.checkWalkStep(0,x,y,3209-x,3215-y,1):""));
  }
  for(WorldObject obj:region.getObjects().values()) if(obj.getPlane()==0&&Math.abs(obj.getX()-3209)<=3&&Math.abs(obj.getY()-3215)<=3)
   System.out.println("OBJECT "+obj.getId()+" "+obj.getX()+","+obj.getY()+" type="+obj.getType()+" rotation="+obj.getRotation()+" name="+obj.getDefinitions().getName());
 }
}