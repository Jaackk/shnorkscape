import com.rs.cache.Cache;
import java.nio.file.Paths;
import java.security.MessageDigest;
/** Read-only actual cache-container decode of the staged bank sort UI. */
public final class Native950BankSortCacheProbe {
    public static void main(String[] args)throws Exception{
        Cache.initFlatReadOnly(Paths.get(args[0]));
        check(12,13830,0,"598822507b1e6f9a0611e2f5886e3a2527668c7894278c7fbded768a6f09594e");
        check(3,517,250,"cf974489d2ac7d5dbcbe4219220e66ed7e2422dd3b0d662df296330f9c79edd6");
        System.out.println("PASS: real950 cache loader reads staged bank-sort script/reference and unchanged plus component");
    }
    private static void check(int index,int group,int file,String expected)throws Exception{
        byte[] bytes=Cache.STORE.getIndexes()[index].getFile(group,file);
        StringBuilder hash=new StringBuilder();for(byte value:MessageDigest.getInstance("SHA-256").digest(bytes))hash.append(String.format("%02x",value&255));
        if(!expected.equals(hash.toString()))throw new AssertionError("Changed bank-sort asset");
    }
}
