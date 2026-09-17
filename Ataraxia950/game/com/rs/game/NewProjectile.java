package com.rs.game;

import com.rs.utils.Utils;

public class NewProjectile {

	private final WorldTile from;
	private final WorldTile to;
	private final int graphicsId;
	private final int startHeight;
	private final int endHeight;
	private final int startTime;
	private final int slope;
	private final int speed;
	private final int distanceOffset;
	public static final int DEFAULT_SLOPE = 0, DEFAULT_OFFSET = 0, DEFAULT_SPEED = 50, DEFAULT_DELAY = 0;
	
	public NewProjectile(WorldTile from, WorldTile to, int graphicsId, int startHeight, int endHeight, int delay, int slope, int speed, int distanceOffset) {
		this.from = from;
		this.to = to;
		this.graphicsId = graphicsId;
		this.startHeight = startHeight;
		this.endHeight = endHeight;
		this.startTime = delay;
		this.slope = slope;
		this.speed = speed;
		this.distanceOffset = distanceOffset;
	}
	
	public NewProjectile(WorldTile from, WorldTile to, int graphicsId, int startHeight, int endHeight, int speed, int delay) {
		this.from = from;
		this.to = to;
		this.graphicsId = graphicsId;
		this.startHeight = startHeight;
		this.endHeight = endHeight;
		this.startTime = delay;
		this.speed = speed;
		this.slope = DEFAULT_SLOPE;
		this.distanceOffset = DEFAULT_OFFSET;
	}
	
	public NewProjectile(WorldTile from, WorldTile to, int graphicsId, int startHeight, int endHeight, int delay) {
		this.from = from;
		this.to = to;
		this.graphicsId = graphicsId;
		this.startHeight = startHeight;
		this.endHeight = endHeight;
		this.startTime = delay;
		this.slope = DEFAULT_SLOPE;
		this.speed = DEFAULT_SPEED;
		this.distanceOffset = DEFAULT_OFFSET;
	}
	
	public NewProjectile(WorldTile from, WorldTile to, int graphicsId, int startHeight, int endHeight) {
		this.from = from;
		this.to = to;
		this.graphicsId = graphicsId;
		this.startHeight = startHeight;
		this.endHeight = endHeight;
		this.startTime = DEFAULT_DELAY;
		this.slope = DEFAULT_SLOPE;
		this.speed = DEFAULT_SPEED;
		this.distanceOffset = DEFAULT_OFFSET;
	}
	
	public WorldTile getFrom() {
		return from;
	}
	
	public WorldTile getTo() {
		return to;
	}
	
	public int getGraphicsId() {
		return graphicsId;
	}
	
	public int getStartHeight() {
		return startHeight;
	}
	
	public int getEndHeight() {
		return endHeight;
	}
	
	public int getDelay() {
		return startTime;
	}
	
	public int getTime() {
		final int duration = Utils.getDistance(getFrom().getX(), getFrom().getY(), getTo().getX(), getTo().getY()) * 30 / (getSpeed() / 10 < 1 ? 1 : getSpeed() / 10) + getDelay();
		return (int) (duration * 11.75);
	}
	
	public int getSlope() {
		return slope;
	}
	
	public int getSpeed() {
		return speed;
	}
	
	public int getDistanceOffset() {
		return distanceOffset;
	}
}
