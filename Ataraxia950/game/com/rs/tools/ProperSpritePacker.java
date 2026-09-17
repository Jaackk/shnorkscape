package com.rs.tools;

import com.displee.cache.CacheLibrary;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.DataOutputStream;

public class ProperSpritePacker {

    public static void main(String[] args) {
        try {
            System.out.println("Loading cache...");
            CacheLibrary library = CacheLibrary.create("data/cache");

            File imgFile = new File("new_background.png");
            BufferedImage bi = ImageIO.read(imgFile);
            int width = bi.getWidth();
            int height = bi.getHeight();

            System.out.println("Encoding " + width + "x" + height + " image...");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            // 1. Write the Pixel Data (ARGB)
            // Most modern RSPS clients can read raw ARGB if the footer is correct
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    dos.writeInt(bi.getRGB(x, y));
                }
            }

            // 2. Write the Jagex Sprite Footer (The magic part)
            dos.writeShort(width);  // 2 bytes
            dos.writeShort(height); // 2 bytes
            dos.writeByte(0);       // 1 byte (Palette size: 0 for ARGB)

            byte[] spriteData = baos.toByteArray();

            // 3. Pack into the IDs we found earlier
            int[] targets = {11588, 9040};
            for (int id : targets) {
                library.put(40, id, 0, spriteData);
                System.out.println("Packed formatted sprite into ID: " + id);
            }

            library.index(8).update();
            System.out.println("SUCCESS: Cache updated with manual Sprite format.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}