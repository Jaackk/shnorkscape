package com.rs.utils;

import java.util.ArrayList;

/**
 * Anti Flood
 *
 * @Author Apache Ah64
 */
public final class AntiFlood {

	private static final ArrayList<String> connections;
	private static final int CAPACITY = 3;

	static {
		connections = new ArrayList<String>(CAPACITY);
	}

	public static void add(String ip) {
		connections.add(ip);
	}
	
	public static void remove(String ip) {
		connections.remove(ip);
	}

	public static int getSessionsIP(String IP) {
		int count = 0;
		for (int index = 0; index < connections.size(); index++) {
			String connection = connections.get(index);
			if (connection == null || connection.isEmpty() || !connection.equalsIgnoreCase(IP))
				continue;
			count++;

		}
		return ++count;
	}
}