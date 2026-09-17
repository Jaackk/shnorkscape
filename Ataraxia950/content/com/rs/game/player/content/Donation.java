//package org.osrspvp.mysql.impl;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//
//import org.osrspvp.model.Client;
//import org.osrspvp.model.player.Player;
//
///**
// * Using this class:
// * To call this class, it's best to make a new thread. You can do it below like so:
// * new Thread(new Donation(player)).start();
// */
//
//public class Donation implements Runnable {
//
//	public static final String HOST = "localhost"; // website ip address
//	public static final String USER = "";
//	public static final String PASS = "";
//	public static final String DATABASE = "";
//
//	private Client player;
//	private Connection conn;
//	private Statement stmt;
//
//	/**
//	 * The constructor
//	 * @param player
//	 */
//	public Donation(Client player) {
//		this.player = player;
//	}
//
//	@Override
//	public void run() {
//		try {
//			if (!connect(HOST, DATABASE, USER, PASS)) {
//				return;
//			}
//
//			String name = player.getUsername().replace("_", " ");
//			ResultSet rs = executeQuery("SELECT * FROM payments WHERE player_name='"+name+"' AND status='Completed' AND claimed=0");
//
//			while (rs.next()) {
//				int item_number = rs.getInt("item_number");
//				double paid = rs.getDouble("amount");
//				int quantity = rs.getInt("quantity");
//
//				switch (item_number) {// add products according to their ID in the ACP
//
//				case 1: // example
//					player.getItems().addItem(995, quantity);
//					break;
//
//				}
//
//				rs.updateInt("claimed", 1); // do not delete otherwise they can reclaim!
//				rs.updateRow();
//			}
//
//			destroy();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	/**
//	 *
//	 * @param host the host ip address or url
//	 * @param database the name of the database
//	 * @param user the user attached to the database
//	 * @param pass the users password
//	 * @return true if connected
//	 */
//	public boolean connect(String host, String database, String user, String pass) {
//		try {
//			this.conn = DriverManager.getConnection("jdbc:mysql://"+host+":3306/"+database, user, pass);
//			return true;
//		} catch (SQLException e) {
//			System.out.println("Failing connecting to database!");
//			return false;
//		}
//	}
//
//	/**
//	 * Disconnects from the MySQL server and destroy the connection
//	 * and statement instances
//	 */
//	public void destroy() {
//        try {
//    		conn.close();
//        	conn = null;
//        	if (stmt != null) {
//    			stmt.close();
//        		stmt = null;
//        	}
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//	/**
//	 * Executes an update query on the database
//	 * @param query
//	 * @see {@link Statement#executeUpdate}
//	 */
//	public int executeUpdate(String query) {
//        try {
//        	this.stmt = this.conn.createStatement(1005, 1008);
//            int results = stmt.executeUpdate(query);
//            return results;
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }
//        return -1;
//    }
//
//	/**
//	 * Executres a query on the database
//	 * @param query
//	 * @see {@link Statement#executeQuery(String)}
//	 * @return the results, never null
//	 */
//	public ResultSet executeQuery(String query) {
//        try {
//        	this.stmt = this.conn.createStatement(1005, 1008);
//            ResultSet results = stmt.executeQuery(query);
//            return results;
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }
//        return null;
//    }
//}
//*/