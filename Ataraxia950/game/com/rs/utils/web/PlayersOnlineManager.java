package com.rs.utils.web;

import com.rs.Settings;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Updates Players online on the website.
 *
 * @author Noel
 */
public class PlayersOnlineManager {

    public static boolean ENABLED = false;
	
    private static void setWebsitePlayersOnline(int amount) throws IOException {
        //Auto-catch to debug on test server.
        if(amount < 10)
            return;
        String key = System.getProperty("ataraxia.website.key", "");
        String endpoint = System.getProperty("ataraxia.website.playersEndpoint", "");
        if (key.isEmpty() || endpoint.isEmpty()) return;
        System.setProperty("http.agent", "Chrome");
        URL url = new URL(endpoint + "?key=" + java.net.URLEncoder.encode(key, "UTF-8") + "&players=" + amount);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.addRequestProperty("User-Agent", "Mozilla/4.76");
        int responseCode = httpConn.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            httpConn.getInputStream().close();
        }
    }

	
	/**
	 * Updates the players online.
	 */
	public static void updatePlayersOnline() {
		if (!ENABLED)
			return;

		/*try {
			setWebsitePlayersOnline(World.getPlayersOnline());
		} catch (Exception e) {
			System.err.println("Error updating players online - website.");
			ENABLED = false;
		}*/
	}

}
