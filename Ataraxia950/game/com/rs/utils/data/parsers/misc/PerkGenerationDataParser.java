package com.rs.utils.data.parsers.misc;

import com.rs.external.api.json.JsonParser;
import com.rs.utils.DataPaths;
import com.rs.utils.data.parsers.misc.pojos.PerkGenerationData;

public class PerkGenerationDataParser {

    /** Relative to DataPaths.root(); resolved when init() runs so the 947 JVM can point at the staged tree. */
    public static final String DEFINITIONS_FILE_PATH = "perkGenerationData.json";

    public static PerkGenerationData DATA;

    public static void init() {
        JsonParser parser = new JsonParser(DataPaths.resolve(DEFINITIONS_FILE_PATH), PerkGenerationData.class);
        DATA = parser.getFileLoaded();
    }
}
