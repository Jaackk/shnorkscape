/* Class351 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */
package com.rs.cache.loaders;

import java.util.Arrays;

import com.rs.cache.Cache;
import com.rs.network.io.InputStream;
import com.rs.utils.Utils;

public class AtmosphereDefinition {
    public Vector3 aClass462_8537;
//    Class28 aClass551_8548;
    public float aFloat8538;
    public float aFloat8540;
    public float aFloat8541;
    public float aFloat8545;
    public float aFloat8546;
    public float aFloat8547;
    public float aFloat8549 = 1.0F;
    public float aFloat8550 = 0.0F;
    public float aFloat8552;
    public float aFloat8553;
    public float aFloat8554;
    public float[] aFloatArray8555;
    public int anInt8542;
    public int anInt8543;
    public int anInt8551;
    public int[] anIntArray8539;
    public int flag;
    public int i_23_;

    public AtmosphereDefinition() {
        aFloat8546 = 1.0F;
        aFloat8552 = 0.0F;
        aFloat8553 = 1.0F;
        anIntArray8539 = new int[] { -1, -1, -1 };
        aFloatArray8555 = new float[] { 0.0F, 0.0F, 0.0F };
        method11135();
    }

    public AtmosphereDefinition(InputStream databuffer) {
        aFloat8546 = 1.0F;
        aFloat8552 = 0.0F;
        aFloat8553 = 1.0F;
        anIntArray8539 = new int[] { -1, -1, -1 };
        aFloatArray8555 = new float[] { 0.0F, 0.0F, 0.0F };
        method11152(databuffer);
    }

    void method11135() {
        anInt8551 = 16777215;
        aClass462_8537 = Vector3.method2004(-50.0F, -60.0F, -50.0F);
        aFloat8547 = 1.1523438F;
        aFloat8538 = 0.69921875F;
        aFloat8540 = 1.2F;
        anInt8542 = 13156520;
        anInt8543 = 0;
//        aClass187_8544 = Class506.aClass187_2463;
        aFloat8545 = 1.0F;
        aFloat8541 = 0.25F;
        aFloat8554 = 1.0F;
//        aClass551_8548 = Class342.aClass551_8676;
        aFloat8549 = 1.0F;
        aFloat8550 = 0.0F;
        aFloat8546 = 1.0F;
        aFloat8552 = 0.0F;
        aFloat8553 = 1.0F;
        int[] is = anIntArray8539;
        int[] is_0_ = anIntArray8539;
        anIntArray8539[2] = -1;
        is_0_[1] = -1;
        is[0] = -1;
        float[] fs = aFloatArray8555;
        float[] fs_1_ = aFloatArray8555;
        aFloatArray8555[2] = 0.0F;
        fs_1_[1] = 0.0F;
        fs[0] = 0.0F;
    }

    void method11114(AtmosphereDefinition class351_2_) {
        anInt8551 = class351_2_.anInt8551;
        aFloat8547 = class351_2_.aFloat8547;
        aFloat8538 = class351_2_.aFloat8538;
        aFloat8540 = class351_2_.aFloat8540;
        aClass462_8537.method2013(class351_2_.aClass462_8537);
        anInt8542 = class351_2_.anInt8542;
        anInt8543 = class351_2_.anInt8543;
//        aClass187_8544 = class351_2_.aClass187_8544;
        aFloat8545 = class351_2_.aFloat8545;
        aFloat8541 = class351_2_.aFloat8541;
        aFloat8554 = class351_2_.aFloat8554;
//        aClass551_8548 = class351_2_.aClass551_8548;
        aFloat8549 = class351_2_.aFloat8549;
        aFloat8550 = class351_2_.aFloat8550;
        aFloat8546 = class351_2_.aFloat8546;
        aFloat8552 = class351_2_.aFloat8552;
        aFloat8553 = class351_2_.aFloat8553;
        anIntArray8539[0] = class351_2_.anIntArray8539[0];
        anIntArray8539[1] = class351_2_.anIntArray8539[1];
        anIntArray8539[2] = class351_2_.anIntArray8539[2];
        aFloatArray8555[0] = class351_2_.aFloatArray8555[0];
        aFloatArray8555[1] = class351_2_.aFloatArray8555[1];
        aFloatArray8555[2] = class351_2_.aFloatArray8555[2];
    }

    public void method11152(InputStream databuffer) {
        flag = databuffer.readUnsignedByte();
        if ((flag & Class189.aClass703_8981.anInt8984) != 0)
            anInt8551 = databuffer.readInt();
        else
            anInt8551 = 16777215;
        if (0 != (flag & Class189.aClass703_8960.anInt8984))
            aFloat8547 = (float) databuffer.readUnsignedShort() / 256.0F;
        else
            aFloat8547 = 1.1523438F;
        if (0 != (flag & Class189.aClass703_8961.anInt8984))
            aFloat8538 = (float) databuffer.readUnsignedShort() / 256.0F;
        else
            aFloat8538 = 0.69921875F;
        if (0 != (flag & Class189.aClass703_8965.anInt8984))
            aFloat8540 = (float) databuffer.readUnsignedShort() / 256.0F;
        else
            aFloat8540 = 1.2F;
        if ((flag & Class189.aClass703_8963.anInt8984) != 0)
            aClass462_8537 = Vector3.method2004((float) databuffer.readShort(), (float) databuffer.readShort(), (float) databuffer.readShort());
        else
            aClass462_8537 = Vector3.method2004(-50.0F, -60.0F, -50.0F);
        if (0 != (flag & Class189.aClass703_8972.anInt8984))
            anInt8542 = databuffer.readInt();
        else
            anInt8542 = 13156520;
        if (0 != (flag & Class189.aClass703_8964.anInt8984))
            anInt8543 = databuffer.readUnsignedShort();
        else
            anInt8543 = 0;
        if ((flag & Class189.aClass703_8966.anInt8984) != 0) {
             i_23_ = databuffer.readUnsignedShort();
//            aClass187_8544 = class342.method11232(i_23_);
        }
//        else
//            aClass187_8544 = Class506.aClass187_2463;
    }

    public void method11117(InputStream databuffer) {
        aFloat8545 = databuffer.readFloat();
        aFloat8541 = databuffer.readFloat();
        aFloat8554 = databuffer.readFloat();
    }

    public void method11142(InputStream databuffer) {
        method11130(databuffer, 0);
    }

    void method11130(InputStream databuffer, int i) {
        anIntArray8539[i] = databuffer.readUnsignedShort();
        aFloatArray8555[i] = databuffer.readFloat();
    }

    public void method11120(InputStream databuffer) {
        int i = databuffer.readUnsignedShort();
        int i_24_ = databuffer.readShort();
        int i_25_ = databuffer.readShort();
        int i_26_ = databuffer.readShort();
        int i_27_ = databuffer.readUnsignedShort();
//        Class519.anInt2538 = i_27_;
//        aClass551_8548 = class342.method11233(i, i_24_, i_25_, i_26_);
    }

    boolean method11141(AtmosphereDefinition class351_28_) {
        return (anInt8551 == class351_28_.anInt8551 && class351_28_.aFloat8547 == aFloat8547 && aFloat8538 == class351_28_.aFloat8538 && class351_28_.aFloat8540 == aFloat8540 && class351_28_.aFloat8541 == aFloat8541 && aFloat8545 == class351_28_.aFloat8545 && class351_28_.aFloat8554 == aFloat8554 && anInt8542 == class351_28_.anInt8542 && class351_28_.anInt8543 == anInt8543 /*
                                                                                                                                                                                                                                                                                                                                                                                         * && aClass187_8544 == class351_28_.aClass187_8544 &&
                                                                                                                                                                                                                                                                                                                                                                                         * class351_28_.aClass551_8548 == aClass551_8548
                                                                                                                                                                                                                                                                                                                                                                                         */ && aFloat8549 == class351_28_.aFloat8549 && aFloat8550 == class351_28_.aFloat8550 && class351_28_.aFloat8546 == aFloat8546 && class351_28_.aFloat8552 == aFloat8552 && aFloat8553 == class351_28_.aFloat8553 && class351_28_.anIntArray8539[0] == anIntArray8539[0] && anIntArray8539[1] == class351_28_.anIntArray8539[1] && class351_28_.anIntArray8539[2] == anIntArray8539[2] && class351_28_.aFloatArray8555[0] == aFloatArray8555[0] && aFloatArray8555[1] == class351_28_.aFloatArray8555[1] && class351_28_.aFloatArray8555[2] == aFloatArray8555[2]);
    }

    public int method11123() {
        return anInt8542;
    }

    public static AtmosphereDefinition getAtmosphereDefinitions(int regionId) {
        final int regionX = (regionId >> 8);
        final int regionY = (regionId & 0xff);
        final int archiveId = Utils.getMapArchiveId(regionX, regionY);
        final byte[] mapSettingsData = Cache.STORE.getIndexes()[5].getFile(archiveId, 3);
        if (mapSettingsData != null) {
            final InputStream stream = new InputStream(mapSettingsData);
            for (int plane = 0; plane < 4; plane++) {
                for (int x = 0; x < 64; x++) {
                    for (int y = 0; y < 64; y++) {

                        final int value = stream.readUnsignedByte();
                        if ((value & 0x1) != 0) {
                            stream.readUnsignedByte();
                            stream.readUnsignedSmart();

                        }
                        if ((value & 0x2) != 0) {
                            stream.readByte();
                        }
                        if ((value & 0x4) != 0) {
                            stream.readUnsignedSmart(); // setted to 30

                        }
                        if ((value & 0x8) != 0) {
                            stream.readUnsignedByte();

                        }
                    }
                }
            }
            return decodeAtmosphere(stream);
        }
        return null;
    }

    public static AtmosphereDefinition decodeAtmosphere(InputStream stream) {
        if (stream.getRemaining() <= 0)
            return null;
        int dx = 0;
        int dy = 0;
        boolean bool = false;
        boolean bool_3_ = false;
        AtmosphereDefinition class351 = null;
        int mapHeight = 0;
        int mapWidth = 0;
        stream.setOffset(stream.getOffset() + 8);
        while (stream.offset < stream.getBuffer().length) {
            int i_4_ = stream.readUnsignedByte();
            if (0 == i_4_) {
                if (null == class351)
                    class351 = new AtmosphereDefinition(stream);
                else
                    class351.method11152(stream);
            } else if (i_4_ == 1) {
                int i_5_ = stream.readUnsignedByte();
                if (i_5_ > 0) {
                    Class133 class133 = new Class133(0, stream, 2);
                    if (31 == class133.anInt7826) {
                        stream.readUnsignedShort();
                    }
                }
            } else if (i_4_ == 2) {
                if (null == class351)
                    class351 = new AtmosphereDefinition();
                class351.method11117(stream);
            } else if (i_4_ == 3) {
                if (class351 == null)
                    class351 = new AtmosphereDefinition();
                class351.method11142(stream);
            } else if (i_4_ == 128) {
                if (null == class351)
                    class351 = new AtmosphereDefinition();
                class351.method11120(stream);
            } else if (i_4_ == 129) {
                byte[][][] aByteArrayArrayArray5330 = null;
                if (null == aByteArrayArrayArray5330)
                    aByteArrayArrayArray5330 = new byte[4][][];
                for (int i_11_ = 0; i_11_ < 4; i_11_++) {
                    byte i_12_ = (byte) stream.readByte();
                    if (i_12_ == 0 && null != aByteArrayArrayArray5330[i_11_]) {
                        int i_13_ = dx;
                        int i_14_ = 64 + dx;
                        int i_15_ = dy;
                        int i_16_ = 64 + dy;
                        if (i_13_ < 0)
                            i_13_ = 0;
                        else if (i_13_ >= mapWidth)
                            i_13_ = mapWidth;
                        if (i_14_ < 0)
                            i_14_ = 0;
                        else if (i_14_ >= mapWidth)
                            i_14_ = mapWidth;
                        if (i_15_ < 0)
                            i_15_ = 0;
                        else if (i_15_ >= mapHeight)
                            i_15_ = mapHeight;
                        if (i_16_ < 0)
                            i_16_ = 0;
                        else if (i_16_ >= mapHeight)
                            i_16_ = mapHeight;
                        for (/**/; i_13_ < i_14_; i_13_++) {
                            for (/**/; i_15_ < i_16_; i_15_++)
                                aByteArrayArrayArray5330[i_11_][i_13_][i_15_] = (byte) 0;
                        }
                    } else if (i_12_ == 1) {
                        if (aByteArrayArrayArray5330[i_11_] == null)
                            aByteArrayArrayArray5330[i_11_] = new byte[1 + mapWidth][mapHeight + 1];
                        for (int i_17_ = 0; i_17_ < 64; i_17_ += 4) {
                            for (int i_18_ = 0; i_18_ < 64; i_18_ += 4) {
                                byte i_19_ = (byte) stream.readByte();
                                for (int i_20_ = dx + i_17_; i_20_ < i_17_ + dx + 4; i_20_++) {
                                    for (int i_21_ = dy + i_18_; i_21_ < dy + i_18_ + 4; i_21_++) {
                                        if (i_20_ >= 0 && i_20_ < mapWidth && i_21_ >= 0 && i_21_ < mapHeight)
                                            aByteArrayArrayArray5330[i_11_][i_20_][i_21_] = i_19_;
                                    }
                                }
                            }
                        }
                    } else if (i_12_ == 2) {
                        if (aByteArrayArrayArray5330[i_11_] == null)
                            aByteArrayArrayArray5330[i_11_] = new byte[mapWidth + 1][mapHeight + 1];
                        if (i_11_ > 0) {
                            int i_22_ = dx;
                            int i_23_ = 64 + dx;
                            int i_24_ = dy;
                            int i_25_ = 64 + dy;
                            if (i_22_ < 0)
                                i_22_ = 0;
                            else if (i_22_ >= mapWidth)
                                i_22_ = mapWidth;
                            if (i_23_ < 0)
                                i_23_ = 0;
                            else if (i_23_ >= mapWidth)
                                i_23_ = mapWidth;
                            if (i_24_ < 0)
                                i_24_ = 0;
                            else if (i_24_ >= mapHeight)
                                i_24_ = mapHeight;
                            if (i_25_ < 0)
                                i_25_ = 0;
                            else if (i_25_ >= mapHeight)
                                i_25_ = mapHeight;
                            for (/**/; i_22_ < i_23_; i_22_++) {
                                for (/**/; i_24_ < i_25_; i_24_++)
                                    aByteArrayArrayArray5330[i_11_][i_22_][i_24_] = (aByteArrayArrayArray5330[i_11_ - 1][i_22_][i_24_]);
                            }
                        }
                    }
                }
                bool = true;
            } else if (130 == i_4_)
                bool_3_ = true;
            else {
                    throw new IllegalStateException("i_4_="+i_4_);
            }
        }
        return class351;
    }

    @Override
    public String toString() {
        return "AtmosphereDefinition [aClass462_8537=" + aClass462_8537 + ", aFloat8538=" + aFloat8538 + ", aFloat8540=" + aFloat8540 + ", aFloat8541=" + aFloat8541 + ", aFloat8545=" + aFloat8545 + ", aFloat8546=" + aFloat8546 + ", aFloat8547=" + aFloat8547 + ", aFloat8549=" + aFloat8549 + ", aFloat8550=" + aFloat8550 + ", aFloat8552=" + aFloat8552 + ", aFloat8553=" + aFloat8553 + ", aFloat8554=" + aFloat8554 + ", aFloatArray8555=" + Arrays.toString(aFloatArray8555) + ", anInt8542=" + anInt8542 + ", anInt8543=" + anInt8543 + ", anInt8551=" + anInt8551 + ", anIntArray8539=" + Arrays.toString(anIntArray8539) + "]";
    }

    
    
}
