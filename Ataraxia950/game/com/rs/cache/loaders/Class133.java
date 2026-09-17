package com.rs.cache.loaders;

import com.rs.network.io.InputStream;

public class Class133 {
    boolean aBool7822;
    boolean aBool7823;
    int anInt7807;
    int anInt7820;
    int anInt7828;
    int anInt7830;
    int anInt7831;
    int anInt7832 = -1;
    short[] aShortArray7829;
//    public Linkable_Sub12 aClass564_Sub32_7821;
    public int anInt7824;
    public int anInt7826;

    public Class133(int i, InputStream databuffer, int i_0_) {
        anInt7824 = databuffer.readUnsignedByte();
        aBool7822 = 0 != (anInt7824 & 0x8);
        aBool7823 = (anInt7824 & 0x10) != 0;
        anInt7824 = anInt7824 & 0x7;
        int i_1_ = databuffer.readUnsignedShort() << i_0_;
        int i_2_ = databuffer.readUnsignedShort() << i_0_;
        int i_3_ = databuffer.readUnsignedShort() << i_0_;
        int i_4_ = databuffer.readUnsignedByte();
        int i_5_ = 1 + i_4_ * 2;
        aShortArray7829 = new short[i_5_];
        for (int i_6_ = 0; i_6_ < aShortArray7829.length; i_6_++) {
            int i_7_ = (short) databuffer.readUnsignedShort();
            int i_8_ = i_7_ >>> 8;
            int i_9_ = i_7_ & 0xff;
            if (i_8_ >= i_5_)
                i_8_ = i_5_ - 1;
            if (i_9_ > i_5_ - i_8_)
                i_9_ = i_5_ - i_8_;
            aShortArray7829[i_6_] = (short) (i_8_ << 8 | i_9_);
        }
        i_4_ = (i_4_ << i) + (1 << i >> 1);
        int i_10_ = (null != HSL_TABLE ? HSL_TABLE[databuffer.readUnsignedShort()] : (HSV_TABLE[(method1823(databuffer.readUnsignedShort()) & 0xffff)]));
        int i_11_ = databuffer.readUnsignedByte();
        anInt7826 = i_11_ & 0x1f;
        anInt7807 = (i_11_ & 0xe0) << 3;
        if (anInt7826 != 31)
            method10192();
        anInt7832 = databuffer.readShort();
//        method10195(toolkit, i_1_, i_3_, i_2_, i_4_, i_10_);
    }

    public void method10206(int i, int i_16_, int i_17_, int i_18_) {
        anInt7820 = i;
        anInt7831 = i_18_;
        anInt7830 = i_17_;
        anInt7828 = i_16_;
    }

    void method10192() {
        switch (anInt7826) {
        default:
            anInt7831 = 0;
            anInt7820 = 0;
            anInt7830 = 2048;
            anInt7828 = 2048;
            break;
        case 13:
            anInt7820 = 2;
            anInt7831 = 0;
            anInt7830 = 2048;
            anInt7828 = 8192;
            break;
        case 15:
            anInt7820 = 1;
            anInt7831 = 1536;
            anInt7830 = 512;
            anInt7828 = 4096;
            break;
        case 16:
            anInt7820 = 1;
            anInt7831 = 1792;
            anInt7830 = 256;
            anInt7828 = 8192;
            break;
        case 6:
            anInt7820 = 3;
            anInt7831 = 1280;
            anInt7830 = 768;
            anInt7828 = 2048;
            break;
        case 2:
            anInt7820 = 1;
            anInt7831 = 0;
            anInt7830 = 2048;
            anInt7828 = 2048;
            break;
        case 10:
            anInt7820 = 3;
            anInt7831 = 1536;
            anInt7830 = 512;
            anInt7828 = 2048;
            break;
        case 7:
            anInt7820 = 3;
            anInt7831 = 1280;
            anInt7830 = 768;
            anInt7828 = 4096;
            break;
        case 11:
            anInt7820 = 3;
            anInt7831 = 1536;
            anInt7830 = 512;
            anInt7828 = 4096;
            break;
        case 14:
            anInt7820 = 1;
            anInt7831 = 1280;
            anInt7830 = 768;
            anInt7828 = 2048;
            break;
        case 5:
            anInt7820 = 4;
            anInt7831 = 0;
            anInt7830 = 2048;
            anInt7828 = 8192;
            break;
        case 8:
            anInt7820 = 3;
            anInt7831 = 1024;
            anInt7830 = 1024;
            anInt7828 = 2048;
            break;
        case 9:
            anInt7820 = 3;
            anInt7831 = 1024;
            anInt7830 = 1024;
            anInt7828 = 4096;
            break;
        case 3:
            anInt7820 = 1;
            anInt7831 = 0;
            anInt7830 = 2048;
            anInt7828 = 4096;
            break;
        case 12:
            anInt7820 = 2;
            anInt7831 = 0;
            anInt7830 = 2048;
            anInt7828 = 2048;
            break;
        case 4:
            anInt7820 = 4;
            anInt7831 = 0;
            anInt7830 = 2048;
            anInt7828 = 2048;
        }
    }

    public static int[] HSL_TABLE, anIntArray8884, HSV_TABLE;

    static {
        if (HSL_TABLE == null) {
            HSL_TABLE = new int[65536];
            anIntArray8884 = new int[65536];
            double d = 0.7;
            for (int i = 0; i < 65536; i++) {
                double d_1_ = (double) (i >> 10 & 0x3f) / 64.0 + 0.0078125;
                double d_2_ = (double) (i >> 7 & 0x7) / 8.0 + 0.0625;
                double d_3_ = (double) (i & 0x7f) / 128.0;
                double d_4_ = d_3_;
                double d_5_ = d_3_;
                double d_6_ = d_3_;
                if (d_2_ != 0.0) {
                    double d_7_;
                    if (d_3_ < 0.5)
                        d_7_ = d_3_ * (d_2_ + 1.0);
                    else
                        d_7_ = d_3_ + d_2_ - d_3_ * d_2_;
                    double d_8_ = 2.0 * d_3_ - d_7_;
                    double d_9_ = 0.3333333333333333 + d_1_;
                    if (d_9_ > 1.0)
                        d_9_--;
                    double d_10_ = d_1_;
                    double d_11_ = d_1_ - 0.3333333333333333;
                    if (d_11_ < 0.0)
                        d_11_++;
                    if (6.0 * d_9_ < 1.0)
                        d_4_ = d_8_ + (d_7_ - d_8_) * 6.0 * d_9_;
                    else if (2.0 * d_9_ < 1.0)
                        d_4_ = d_7_;
                    else if (3.0 * d_9_ < 2.0)
                        d_4_ = 6.0 * ((d_7_ - d_8_) * (0.6666666666666666 - d_9_)) + d_8_;
                    else
                        d_4_ = d_8_;
                    if (d_10_ * 6.0 < 1.0)
                        d_5_ = d_8_ + 6.0 * (d_7_ - d_8_) * d_10_;
                    else if (2.0 * d_10_ < 1.0)
                        d_5_ = d_7_;
                    else if (d_10_ * 3.0 < 2.0)
                        d_5_ = 6.0 * ((0.6666666666666666 - d_10_) * (d_7_ - d_8_)) + d_8_;
                    else
                        d_5_ = d_8_;
                    if (6.0 * d_11_ < 1.0)
                        d_6_ = 6.0 * (d_7_ - d_8_) * d_11_ + d_8_;
                    else if (2.0 * d_11_ < 1.0)
                        d_6_ = d_7_;
                    else if (d_11_ * 3.0 < 2.0)
                        d_6_ = 6.0 * ((d_7_ - d_8_) * (0.6666666666666666 - d_11_)) + d_8_;
                    else
                        d_6_ = d_8_;
                }
                d_4_ = Math.pow(d_4_, d);
                d_5_ = Math.pow(d_5_, d);
                d_6_ = Math.pow(d_6_, d);
                int i_12_ = (int) (d_4_ * 256.0);
                int i_13_ = (int) (d_5_ * 256.0);
                int i_14_ = (int) (256.0 * d_6_);
                int i_15_ = i_14_ + ((i_12_ << 16) + (i_13_ << 8));
                HSL_TABLE[i] = i_15_ & 0xffffff;
                int i_16_ = (i_13_ << 8) + (i_14_ << 16) + i_12_;
                anIntArray8884[i] = i_16_;
            }
        }
        if (HSV_TABLE == null) {
            HSV_TABLE = new int[65536];
            double d = 0.7;
            int i = 0;
            for (int i_0_ = 0; i_0_ < 512; i_0_++) {
                float f = ((float) (i_0_ >> 3) / 64.0F + 0.0078125F) * 360.0F;
                float f_1_ = (float) (i_0_ & 0x7) / 8.0F + 0.0625F;
                for (int i_2_ = 0; i_2_ < 128; i_2_++) {
                    float f_3_ = (float) i_2_ / 128.0F;
                    float f_4_ = 0.0F;
                    float f_5_ = 0.0F;
                    float f_6_ = 0.0F;
                    float f_7_ = f / 60.0F;
                    int i_8_ = (int) f_7_;
                    int i_9_ = i_8_ % 6;
                    float f_10_ = f_7_ - (float) i_8_;
                    float f_11_ = f_3_ * (1.0F - f_1_);
                    float f_12_ = (1.0F - f_1_ * f_10_) * f_3_;
                    float f_13_ = (1.0F - f_1_ * (1.0F - f_10_)) * f_3_;
                    if (i_9_ == 0) {
                        f_4_ = f_3_;
                        f_5_ = f_13_;
                        f_6_ = f_11_;
                    } else if (1 == i_9_) {
                        f_4_ = f_12_;
                        f_5_ = f_3_;
                        f_6_ = f_11_;
                    } else if (i_9_ == 2) {
                        f_4_ = f_11_;
                        f_5_ = f_3_;
                        f_6_ = f_13_;
                    } else if (i_9_ == 3) {
                        f_4_ = f_11_;
                        f_5_ = f_12_;
                        f_6_ = f_3_;
                    } else if (i_9_ == 4) {
                        f_4_ = f_13_;
                        f_5_ = f_11_;
                        f_6_ = f_3_;
                    } else if (5 == i_9_) {
                        f_4_ = f_3_;
                        f_5_ = f_11_;
                        f_6_ = f_12_;
                    }
                    f_4_ = (float) Math.pow(f_4_, d);
                    f_5_ = (float) Math.pow(f_5_, d);
                    f_6_ = (float) Math.pow(f_6_, d);
                    int i_14_ = (int) (256.0F * f_4_);
                    int i_15_ = (int) (f_5_ * 256.0F);
                    int i_16_ = (int) (f_6_ * 256.0F);
                    int i_17_ = i_16_ + ((i_14_ << 16) + -16777216 + (i_15_ << 8));
                    HSV_TABLE[i++] = i_17_;
                }
            }
        }
    }

    
    public static short method1823(int i) {
        int i_0_ = i >> 10 & 0x3f;
        int i_1_ = i >> 3 & 0x70;
        int i_2_ = i & 0x7f;
        i_1_ = i_2_ <= 64 ? i_1_ * i_2_ >> 7 : (127 - i_2_) * i_1_ >> 7;
        int i_3_ = i_1_ + i_2_;
        int i_4_;
        if (i_3_ != 0)
            i_4_ = (i_1_ << 8) / i_3_;
        else
            i_4_ = i_1_ << 1;
        int i_5_ = i_3_;
        return (short) (i_0_ << 10 | i_4_ >> 4 << 7 | i_5_);
    }
}
