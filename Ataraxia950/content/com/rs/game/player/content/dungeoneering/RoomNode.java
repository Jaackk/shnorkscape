package com.rs.game.player.content.dungeoneering;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class RoomNode {

	int x;
	int y;
	int key;
	int lock;
	boolean isCritPath;
	boolean isBoss;
	
	public RoomNode parent;
	public List<RoomNode> children;
	
	public RoomNode(RoomNode parent, int x, int y) {
		this.key = -1;
		this.lock = -1;
		this.children = new ArrayList<RoomNode>();
		this.parent = parent;
		if (parent != null) //Base doesn't have a parent
			parent.children.add(this);
		this.x = x;
		this.y = y;
	}
	
	public boolean north() {
		for (RoomNode c : children) {
			if (c.y - 1 == y) {
				return true;
			}
		}
		return parent != null && parent.y - 1 == y;
	}
	
	public boolean east() {
		for (RoomNode c : children) {
			if (c.x - 1 == x) {
				return true;
			}
		}
		return parent != null && parent.x - 1 == x;
	}
	
	public boolean south() {
		for (RoomNode c : children) {
			if (c.y + 1 == y) {
				return true;
			}
		}
		return parent != null && parent.y + 1 == y;
	}
	
	public boolean west() {
		for (RoomNode c : children) {
			if (c.x + 1 == x) {
				return true;
			}
		}
		return parent != null && parent.x + 1 == x;
	}
	
	public int rotation() {
		if(parent == null) {
			throw new RuntimeException("Cannot calculate rotation for base, no parent");
		}
		if(parent.y - y == -1) {
			return 0;
		}
		if(parent.x - x == -1) {
			return 1;
		}
		if(parent.y - y == 1) {
			return 2;
		}
		if(parent.x - x == 1) {
			return 3;
		}
		throw new RuntimeException("Cannot calculate rotation, parent is not adjacent");
	}
	
	public List<RoomNode> pathToBase() {
		List<RoomNode> path = new LinkedList<RoomNode>();
		RoomNode p = this;
		while(p != null) {
			path.add(p);
			p = p.parent;
		}
		return path;
	}
	
	public List<RoomNode> getChildrenR() {
		List<RoomNode> childrenR = new ArrayList<>(children);
		for (RoomNode node : children) {
			childrenR.addAll(node.getChildrenR());
		}
		return childrenR;
	}
	
}