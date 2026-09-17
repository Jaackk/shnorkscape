package com.rs.tools;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

import com.rs.cache.Cache;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.cache.loaders.RenderAnimDefinitions;
import com.rs.utils.Utils;

public class RuneWikiNPCDefinitionsDumper {

	public static void main(String[] args) {
		try {
			new RuneWikiNPCDefinitionsDumper();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public RuneWikiNPCDefinitionsDumper() throws IOException {

		Cache.init();
		BufferedWriter writer = new BufferedWriter(new FileWriter("Latest npc list.txt", true));
		for (int i = 0; i < Utils.getNPCDefinitionsSize(); i++) {
			NPCDefinitions defs = NPCDefinitions.getNPCDefinitions(i);
			RenderAnimDefinitions d = RenderAnimDefinitions.getRenderAnimDefinitions(defs.renderEmote);
			writer.flush();
			writer.newLine();
			if (d != null)
				writer.write(i + " - " + defs.name + ": Stand: " + d.loopAnimDurations + ", Walk: " + d.loopAnimDurations + ", Run: " + d.loopAnimDurations);
			else
				writer.write(i + " - " + defs.name + ": Stand: " + -1 + ", Walk: " + -1 + ", Run: " + -1);
			writer.flush();
		}
		return;
	}



	public static int convertInt(String str) {
		try {
			int i = Integer.parseInt(str);
			return i; } catch (NumberFormatException e) {
		}
		return 1;
	}

}