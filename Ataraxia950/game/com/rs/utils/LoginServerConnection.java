package com.rs.utils;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;

/**
 * LoginServerConnection.java | 10:13:09 PM
 * @author Chryonic
 * @date Mar 21, 2017
 */
public class LoginServerConnection {
	
	public HashMap<Integer, LoginServers> servers = new HashMap<Integer, LoginServers>();
	public LoginServers recommendedServer = servers.get(1);

	public void init() {
		servers.put(1, new LoginServers("127.0.0.1", 43596, "Localhost"));
		servers.put(2, new LoginServers("127.0.0.1", 43595, "Main Server, USA"));
	}
	
	public LoginServers getRecommendedServer() throws InterruptedException {
		LoginServers recommended = servers.get(1);
		for (int i = 1; i <= servers.size(); i++) {
			LoginServers currentServer = servers.get(i);
			testServer(currentServer);
		}
		Thread.sleep(1000);
		for (int i = 1; i <= servers.size(); i++) {
			LoginServers currentServer = servers.get(i);
			if (recommended.getPing() > servers.get(i).getPing()) {
				recommended = currentServer;
			}
		}
		recommendedServer = recommended;
		return recommended;
	}
	
	public LoginServers getRecommended() {
		return recommendedServer;
	}

	public static void testServer(final LoginServers server) {
		Thread t = new Thread(() -> t(server));
		t.setName("PING THREAD " + server.getDetails());
		t.start();
	}

	public static void t(LoginServers server) {
		String host = server.getHost();
		int port = server.getPort();
		try {
			long start = System.currentTimeMillis();
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(host, port), 500);
			long ping = System.currentTimeMillis() - start;
			socket.close();
			server.setPing(ping);
		} catch (Exception e) {
			server.setPing(Short.MAX_VALUE);
		}
	}
	
	public class LoginServers {

		private final String host;
		private final int port;
		private final String details;
		private long ping;

		public LoginServers(String host, int port, String details) {
			this.host = host;
			this.port = port;
			this.details = details;
			ping = Short.MAX_VALUE;
		}

		public String getHost() {
			return host;
		}

		public int getPort() {
			return port;
		}

		public String getDetails() {
			return details;
		}

		public long getPing() {
			return ping;
		}

		public void setPing(long ping) {
			this.ping = ping;
		}

	}

}
