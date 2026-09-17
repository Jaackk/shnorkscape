import com.rs.cache.Cache;
import com.rs.game.*;
import java.nio.file.Paths;
import java.util.*;
public class Native950AgilityCourseProbe {
 public static void main(String[] args)throws Exception {
  System.setProperty("native950.spawns","false");Cache.initFlatReadOnly(Paths.get(args[0]));
  Set<Integer> ids=new HashSet<>(Arrays.asList(20210,43526,43595,20211,2302,1948,65365,65367,65362,65734,64696,64699,64698));
  for(int[] area:new int[][]{{2527,3538,2557,3565},{2990,3912,3009,3965}}){
   Set<Integer> visited=new HashSet<>();
   for(int x=area[0];x<=area[2];x++)for(int y=area[1];y<=area[3];y++)visited.add(((x>>6)<<8)|(y>>6));
   List<WorldObject> objects=new ArrayList<>();
   for(int region:visited)objects.addAll(World.getRegion(region,true).getObjects().values());
   objects.sort(Comparator.comparingInt(WorldObject::getId).thenComparingInt(WorldObject::getX).thenComparingInt(WorldObject::getY));
   for(WorldObject o:objects)if(o.getX()>=area[0]&&o.getX()<=area[2]&&o.getY()>=area[1]&&o.getY()<=area[3]&&
      (ids.contains(o.getId())||o.getDefinitions().name.toLowerCase().matches(".*(ledge|balance|stepping|crumbl|obstacle|rope swing|cliff|gate|stair|ladder).*"))) {
    System.out.println("OBJ "+o.getId()+" "+o.getX()+","+o.getY()+","+o.getPlane()+" type="+o.getType()+" rot="+o.getRotation()+" size="+o.getDefinitions().sizeX+"x"+o.getDefinitions().sizeY+" "+o.getDefinitions().name+" "+Arrays.toString(o.getDefinitions().options));
   }
  }
  for(int[] t:new int[][]{{2532,3546,1},{2532,3546,0},{3004,3937,0},{3004,3950,0},{3005,3951,0},{2998,3915,0},{2998,3931,0},{3002,3945,0},{2552,3561,0},{2552,3558,0},{2551,3556,0},{2551,3549,0},{2551,3546,0},{2541,3546,0},{2539,3546,0},{2537,3546,1},{2536,3547,1},{2532,3547,1},{2532,3547,0},{2534,3552,0},{2998,3916,0},{2998,3931,0},{3004,3938,0},{3004,3949,0},{3005,3954,0},{3005,3958,0},{3002,3960,0},{2996,3960,0},{2994,3952,0},{2994,3945,0},{2995,3939,0},{2995,3935,0}})
   System.out.println("FLOOR "+Arrays.toString(t)+" free="+World.isFloorFree(t[2],t[0],t[1],1));
  System.exit(0);
 }
}