package com.rs.game;

public class HeadIcon {

	private final int spriteId;
    private final int fileId;

	public HeadIcon(int spriteId, int fileId) {
		this.spriteId = spriteId;
		this.fileId = fileId;
	}

	public int getSpriteId() {
		return spriteId;
	}

	public int getFileId() {
		return fileId;
	}
}
