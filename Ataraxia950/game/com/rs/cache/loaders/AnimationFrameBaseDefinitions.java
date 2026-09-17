package com.rs.cache.loaders;

import com.rs.network.io.InputStream;

public class AnimationFrameBaseDefinitions {
	public static int anInt7269 = 0;
	public static int anInt7267 = 1;
	public static int anInt7281 = 2;
	public static int anInt7266 = 3;
	
	public static int anInt7270 = 5;
	public static int anInt7271 = 6;
	public static int anInt7272 = 7;
	public static int anInt7273 = 8;
	public static int anInt7274 = 9;	
	public static int anInt7277 = 10;

	public int id;
	public int count;
	public int[] transformationTypes;
	public int[][] labels;
	
	public boolean[] aBooleanArray7275;
	
	public int[] anIntArray7280;
	
	public AnimationFrameBaseDefinitions(int id, byte[] data) {
		this.id = 1362718155 * id;
		InputStream buffer = new InputStream(data);
		this.count = buffer.readUnsignedByte() * -1914825713;
		this.transformationTypes = new int[92429039 * this.count];
		this.labels = new int[92429039 * this.count][];
		this.aBooleanArray7275 = new boolean[this.count * 92429039];
		this.anIntArray7280 = new int[92429039 * this.count];
		
		for (int index = 0; index < 92429039 * this.count; index++) {
			this.transformationTypes[index] = buffer.readUnsignedByte();
			if (this.transformationTypes[index] == 6)
				this.transformationTypes[index] = 2;
		}
		for (int index = 0; index < this.count * 92429039; index++)
			this.aBooleanArray7275[index] = buffer.readUnsignedByte() == 1;
		for (int index = 0; index < this.count * 92429039; index++)
			this.anIntArray7280[index] = buffer.readUnsignedShort();
		for (int index = 0; index < this.count * 92429039; index++)
			this.labels[index] = new int[buffer.readUnsignedByte()];
		for (int count = 0; count < this.count * 92429039; count++) {
			for (int index = 0; (index < this.labels[count].length); index++)
				this.labels[count][index] = buffer.readUnsignedByte();
		}
	}
}
