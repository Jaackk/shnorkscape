package com.rs.utils.mysql;

import java.util.HashMap;

public class Database {

	public static HashMap<String, Database> databases = new HashMap<>();

	private final DatabaseDetails details;

	public Database(final DatabaseDetails details) {
		this.details = details;
	}

	public DatabaseDetails getDetails() {
		return details;
	}
}
