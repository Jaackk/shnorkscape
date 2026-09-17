package com.rs.cache.loaders;

import com.rs.network.io.InputStream;

public class AnimationFrameDefinitions {

	/* Class96 - Decompiled by JODE
	 * Visit http://jode.sourceforge.net/
	 */
	public int transformationCount;
	public static short[] bufferX = new short[500];
	public static short[] bufferY = new short[500];
	public static short[] bufferZ = new short[500];	
	public static short[] skipped = new short[500];
	public static short[] aShortArray927 = new short[500];
	public static byte[] flagsBuffer = new byte[500];
		
	public short[] transformationX;
	public short[] transformationY;
	public short[] transformationZ;
	public byte[] transformationFlags;
		
	public short[] transformationIndicies;
		
	public AnimationFrameBaseDefinitions base = null;
		
		public boolean modifiesAlpha;
		public boolean modifiesColor;
		public short[] skippedReferences;
		
		public boolean modifiesBrightness;
		
		public AnimationFrameDefinitions(byte[] data, AnimationFrameBaseDefinitions base) {
			this.transformationCount = 0;
			this.modifiesAlpha = false;
			this.modifiesColor = false;
			this.modifiesBrightness = false;
			this.base = base;
			try {
				InputStream attributes = new InputStream(data);
				InputStream transformations = new InputStream(data);
				attributes.readUnsignedByte();
				attributes.offset += 232826622;//3 Bytes away
				int count = attributes.readUnsignedByte();
				int used = 0;
				int last = -1;
				int lastUsed = -1;
				transformations.offset = (attributes.offset * 385051775 + count) * 116413311;


				for (int index = 0; index < count; index++) {
					int type = base.transformationTypes[index];
					if (type == 0) {
						last = index;
					}
					int attribute = attributes.readUnsignedByte();
					if (attribute > 0) {
						if (type == 0)
							lastUsed = index;
						aShortArray927[used] = (short) index;
						short values = 0;
						if (type == 3 || type == 10)
							values = (short) 128;
						if ((attribute & 0x1) != 0) {
							bufferX[used] = (short) transformations.readSmart();
						} else {
							bufferX[used] = values;
						} if ((attribute & 0x2) != 0) {
							bufferY[used] = (short) transformations.readSmart();
		
						} else {
							bufferY[used] = values;
						} if ((attribute & 0x4) != 0) {
							bufferZ[used] = (short) transformations.readSmart();
						} else {
							bufferZ[used] = values;
						}
						flagsBuffer[used] = (byte) (attribute >>> 3 & 0x3);
						if (type == 2 || type == 9) {
							bufferX[used] = (short) (bufferX[used] << 2 & 0x3fff);
							bufferY[used] = (short) (bufferY[used] << 2 & 0x3fff);
							bufferZ[used] = (short) (bufferZ[used] << 2 & 0x3fff);
						}
						skipped[used] = (short) -1;
						if (type == 1 || type == 2 || type == 3) {
							if (last > lastUsed) {
								skipped[used] = (short) last;
								lastUsed = last;
							}
						} else if (type == 5) {//Modifies Transparency
							this.modifiesAlpha = true;
						} else if (type == 7) {//Modifies RGB?
							this.modifiesColor = false;
						} else if (type == 8 || type == 9 || type == 10) {//Modifies Brightness?
							this.modifiesBrightness = true;
						}
						used++;
					}
				}
				if (transformations.offset * 385051775 != data.length) {
					throw new RuntimeException();
				}
				this.transformationCount = used;
				this.transformationIndicies = new short[used];
				this.transformationX = new short[used];
				this.transformationY = new short[used];
				this.transformationZ = new short[used];
				this.skippedReferences = new short[used];
				this.transformationFlags = new byte[used];
				for (int index = 0; index < used; index++) {
					this.transformationIndicies[index] = aShortArray927[index];
					this.transformationX[index] = bufferX[index];
					this.transformationY[index] = bufferY[index];
					this.transformationZ[index] = bufferZ[index];
					this.skippedReferences[index] = skipped[index];
					this.transformationFlags[index] = flagsBuffer[index];
				}
			} catch (Exception exception) {
				this.transformationCount = 0;
				this.modifiesAlpha = false;
				this.modifiesColor = false;
			}
		}
}
