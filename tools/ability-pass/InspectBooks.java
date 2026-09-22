import com.rs.cache.Cache;
import com.rs.cache.loaders.IComponentDefinitions;
import java.nio.file.Paths;
import java.util.Arrays;

/** Read-only paired-cache interface hook witnesses for the ability pass. */
class InspectBooks {
    public static void main(String[] args) throws Exception {
        Cache.initFlatReadOnly(Paths.get(args[0]));
        for(int id:new int[]{1201,13740}){
            com.rs.cache.loaders.ItemDefinitions d=com.rs.cache.loaders.ItemDefinitions.getItemDefinitions(id);
            System.out.println("item "+id+" "+d.name+" params="+d.clientScriptData);
        }
        for(int id:new int[]{36453,36454,44637,27344}){
            com.rs.cache.loaders.VarBitDefinitions v=com.rs.cache.loaders.VarBitDefinitions.getClientVarpBitDefinitions(id);
            System.out.println("varbit "+id+" domain="+v.varDomain+" parent="+v.baseVar+" bits="+v.startBit+".."+v.endBit);
        }
        for(int script:new int[]{564,8437,8426}){
            byte[] bytes=Cache.STORE.getIndexes()[12].getFile(script,0);
            StringBuilder hash=new StringBuilder();
            for(byte b:java.security.MessageDigest.getInstance("SHA-256").digest(bytes))hash.append(String.format("%02x",b&255));
            System.out.println("12/"+script+"/0="+hash);
        }
        for(int face=1440;face<=1895;face++) {
            if(face!=1456&&face!=1459&&face!=1460&&face!=1461&&face!=1880&&face!=1887)continue;
            try {
                int[] ids=Cache.STORE.getIndexes()[3].getTable().getArchives()[face].getValidFileIds();
                for(int i:ids) {
                    byte[] raw=Cache.STORE.getIndexes()[3].getFile(face,i);
                    IComponentDefinitions d=new IComponentDefinitions();
                    d.ihash=face<<16|i;
                    d.decode(new com.rs.network.io.InputStream(raw));
                    for(java.lang.reflect.Field f:IComponentDefinitions.class.getFields())
                        if(f.getType()==Object[].class&&f.get(d)!=null)
                            System.out.println(face+":"+i+" "+f.getName()+"="+Arrays.toString((Object[])f.get(d)));
                }
            } catch(RuntimeException invalid) { System.out.println(face+" refused "+invalid.getClass().getSimpleName()); }
        }
    }
}
