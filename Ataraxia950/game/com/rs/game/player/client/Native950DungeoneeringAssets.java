package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.NPCDefinitions;
import java.security.MessageDigest;

/** Selected950 definitions and original frozen-room terrain/locations, verified before admission. */
public final class Native950DungeoneeringAssets {
    private Native950DungeoneeringAssets() { }
    private static Object verifiedStore;
    private static final String[] PINS = {
        "18/0/88:405648d2b2a02d80754a920a96ad07088d33fa3cf14bc503cf2d18d99a9842ce",
        "18/75/112:233fdef01be762eb740c6a7f28b62068c3c525ffd1b2b25057572a3bd0fea02b",
        "19/61/91:19cf6222291478b75f7d0df9e0f5e250e068b1f24ce217affb946c4e773dc9ae",
        "16/189/112:8257d2c320f94399bc4190ef963c060ca03925ecc19b6330cd43ce33e74a9a9d",
        "16/199/212:53f740e75ec6e5a5abf790c17610aa0ecfa907f518e102b76d9ccdbc768b0933",
        "5/9985/3:525131f0d38e67a3b1f28353fd62dd0787e8f43b4724890ec5db5796c95a1334",
        "5/9985/0:5e2ef2afdc6a57fd3c11020d76ad2557759bd8cdc85f480ad52c2dce7262e93f",
        "5/10113/3:50593b0c392aa28b577f7dae29631dcb048ca9edaf0e4a184d9feeffe33b6290",
        "5/10113/0:7898b2b3594fe0081edb0f05837f994c472b4d6b446aae00106132807c5b61a9",
        "5/7349/3:d0c48c5569d11b0fb0693f9426fba0e3caaeee2863278bae7c1425fbf7521b61",
        "5/7349/0:9fc6d8050bfc587cb06ba9f0119807c39cbf5ed4aab816dabcd961f57ace1da9",
        "5/7477/3:521f5623674f0bd7c17141f865337397b71d92d2ad31d663b0a16796f0d53af6",
        "5/7477/0:9cd290add43af14e419b43db555d727d5713972a32fd704ab1f56e539ba8fd40",
        "5/7350/3:d9304f46ffbd3aeec30493e327d399c971b911fa71bd0a42c0b7307c7b0322a0",
        "5/7350/0:a99e3fdfd68967bec3bed6f58441e6aff29379732fdec931e2a2ddc3909459dd",
        "5/7478/3:1a608a8f1cf50793ddf2fe78279ae8adaccc1f9cae5cb17b50a80d7f6d5d5c2b",
        "5/7478/0:863ef79b1fb2adda2c86bc69ecf33122c9a5b4813428ef0d60489a4b97079435"
    };
    public static synchronized void verifyCacheBindings() {
        if (Cache.STORE != null && Cache.STORE == verifiedStore) return;
        if (Cache.STORE == null || !Cache.isFlatReadOnly())
            throw new IllegalStateException("Dungeoneering requires the selected950 cache");
        try {
            for (String pin : PINS) {
                String[] pair = pin.split(":"), key = pair[0].split("/");
                byte[] bytes = Cache.STORE.getIndexes()[Integer.parseInt(key[0])]
                        .getFile(Integer.parseInt(key[1]), Integer.parseInt(key[2]));
                if (bytes == null) throw new IllegalStateException("Missing Dungeoneering asset " + pair[0]);
                StringBuilder hash = new StringBuilder();
                for (byte value : MessageDigest.getInstance("SHA-256").digest(bytes))
                    hash.append(String.format("%02x", value & 255));
                if (!pair[1].equals(hash.toString()))
                    throw new IllegalStateException("Changed950 Dungeoneering asset " + pair[0]);
            }
        } catch (java.security.NoSuchAlgorithmException failure) { throw new IllegalStateException(failure); }
        verifiedStore = Cache.STORE;
    }
    public static boolean tutorCandidate(int id) { return id == Native950Dungeoneering.TUTOR; }
    public static boolean verifiedTutor(int id, NPCDefinitions definition) {
        return tutorCandidate(id) && definition != null && definition.decodeFailure == null
                && definition.transformTo == null && definition.models != null && definition.models.length > 0
                && definition.size == 1 && "Dungeoneering tutor".equals(definition.name)
                && !definition.hasOption("Attack") && definition.hasOption("Talk to");
    }
}
