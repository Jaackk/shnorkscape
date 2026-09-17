package com.rs.utils;

import java.util.ArrayList;

/**
 * PushList.java | 3:07:52 PM
 * 
 * @author Chryonic
 * @date Feb 21, 2017
 */
public class PushList<T> extends ArrayList<T> {

	private static final long serialVersionUID = -6711255482583996329L;

	private final int maximumSize;

	public PushList(int maximumSize) {
		this.maximumSize = maximumSize;
		for (int i = 0; i < maximumSize; i++) {
			super.add(null);
		}
	}

	public int getSize() {
		for (int i = 0; i < maximumSize; i++) {
			if (get(i) == null)
				return i;
		}
		return maximumSize;
	}

	@Override
	public boolean add(T e) {
		for (int i = size() - 1; i > 0; i--) {
			set(i, get(i - 1));
		}
		set(0, e);
		return false;
	}
}