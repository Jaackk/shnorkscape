package com.rs.network.codec.update;

import com.rs.cache.Cache;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class JS5UpdateHelper {

    private static final byte[] INFORMATION_STORE_MAIN;
    private static final byte[] INFORMATION_STORE_WEB;

    static {
        ByteBuf header = Unpooled.buffer(5);
        ByteBuf info = Unpooled.wrappedBuffer(Cache.createInformationStore(false));
        header.writeByte(0);
        header.writeInt(info.capacity());
        header.writeBytes(info);
        INFORMATION_STORE_MAIN = header.array().clone();
        header.release();
        info.release();
        header = Unpooled.buffer(5);
        info = Unpooled.wrappedBuffer(Cache.createInformationStore(true));
        header.writeByte(0);
        header.writeInt(info.capacity());
        header.writeBytes(info);
        INFORMATION_STORE_WEB = header.array().clone();
        header.release();
        info.release();
    }

    public static byte[] get(int fs, int folder, boolean web) {
        if (fs == 255 && folder == 255) {
            return web ? INFORMATION_STORE_WEB : INFORMATION_STORE_MAIN;
        }
        if (fs == 255) {
            return Cache.STORE.getIndex255().getArchiveData(Cache.resolveJs5IndexId(folder));
        }
        return Cache.getJs5Index(fs).getMainFile().getArchiveData(folder);
    }
}
