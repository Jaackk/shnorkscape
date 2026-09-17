package com.rs.tools;

import com.displee.cache.CacheLibrary;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.DataOutputStream;

public class ManualSniperPacker {

    public static void main(String[] args) {
        try {
            System.out.println("Loading cache...");
            CacheLibrary library = CacheLibrary.create("data/cache");

            File imgFile = new File("new_background.png");
            if (!imgFile.exists()) {
                System.out.println("ERROR: 'new_background.png' not found!");
                return;
            }

            BufferedImage bi = ImageIO.read(imgFile);
            int width = bi.getWidth();
            int height = bi.getHeight();

            System.out.println("Encoding " + width + "x" + height + " image for the Heavyweight Target...");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            // 1. Write the Pixel Data (ARGB)
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    dos.writeInt(bi.getRGB(x, y));
                }
            }

            // 2. Write the Jagex Sprite Footer
            dos.writeShort(width);
            dos.writeShort(height);
            dos.writeByte(0);

            byte[] spriteData = baos.toByteArray();

            // 3. Target the Heavyweight ID found in Index 8
            int targetId = 20453;

            System.out.println("Overwriting the $10.8$ MB archive with your custom Sprite...");
            library.put(8, targetId, 0, spriteData);

            library.index(8).update();
            System.out.println("SUCCESS: Archive 20453 has been replaced.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}