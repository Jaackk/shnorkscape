package com.rs.game.activites.gim.bank;

import com.google.common.cache.CacheLoader;
import com.rs.utils.SerializableFilesManager;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A cache loader that will lazily load GIM bank instances.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMBankLoader extends CacheLoader<String, GIMBank> {

    @Override
    public GIMBank load(String key) throws Exception {
        Path path = GIMBankManager.getBankPath(key);
        if (!Files.exists(path)) {
            return new GIMBank(key);
        }
        GIMBank loadedBank = (GIMBank) SerializableFilesManager.loadSerializedFile(path.toFile());
        if (loadedBank == null) {
            throw new IllegalStateException("Unable to load bank for group=" + key);
        }
        return loadedBank;
    }
}
