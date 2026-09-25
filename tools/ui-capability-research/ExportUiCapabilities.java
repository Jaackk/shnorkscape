import com.rs.cache.Cache;
import java.nio.file.*;

/** Read-only, bounded interface export for the 950 capability report. No server boot. */
public final class ExportUiCapabilities {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("cache-directory new-output-directory");
        Path output = Paths.get(args[1]);
        if (Files.exists(output)) throw new IllegalArgumentException("Use a new output directory");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Files.createDirectories(output);
        for (int group : new int[]{1448,1311,1495,549,753,908,1092,1499,1850,1218,
                656,1384,900,623,475,1462,517,1265,1507,1519,1524,1591,1315,
                1638,1719,1421,1422,1460}) {
            int[] files = Cache.STORE.getIndexes()[3].getTable().getArchives()[group].getValidFileIds();
            for (int file : files)
                Files.write(output.resolve("3-" + group + "-" + file + ".bin"),
                        Cache.STORE.getIndexes()[3].getFile(group, file));
            System.out.println(group + ": " + files.length + " components");
        }
    }
}
