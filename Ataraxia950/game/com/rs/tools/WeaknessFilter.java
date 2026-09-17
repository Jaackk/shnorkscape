package com.rs.tools;

import com.rs.utils.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;

public class WeaknessFilter {

	private static final TreeMap<Integer, String[]> BONUSES = new TreeMap<Integer, String[]>();
	
	public static final void main(String[] args) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter("Bonuses.txt"));
		BufferedReader in = new BufferedReader(new FileReader("Bestiary.txt"));
		try {
			while (true) {
				final String line = in.readLine();
				if (line == null)
					break;
				String[] substrings = line.split(",");
				String[] mageSub = line.split("\\{");
				int id = Integer.parseInt(substrings[0].substring(7));
				String magic = "0", defence = "0", ranged = "0", attack = "0";
				//Logger.getGlobal().info(mageSub[1].split(":")[1]/*.indexOf(",", 1)*/);
				if (mageSub[1].startsWith("\"magic"))
					magic = mageSub[1].split(":")[1].substring(0, mageSub[1].split(":")[1].indexOf(",", 1));
				for (int i = 0; i < substrings.length; i++) {
					if (substrings[i].startsWith("\"defence"))
						defence = substrings[i].substring(10);
					if (substrings[i].startsWith("\"attack") && !substrings[i].startsWith("\"attackable") && !substrings[i].endsWith("\u007D")) {
						if (Integer.valueOf(substrings[i].substring(9)) < 100)
							attack = substrings[i].substring(9);
					}
					if (substrings[i].startsWith("\"ranged"))
						ranged = substrings[i].substring(9);
					if (substrings[i].startsWith("\"magic"))
						magic= substrings[i].substring(8);
				}
				if (!(Integer.valueOf(attack) == 0 && Integer.valueOf(magic) == 0 && Integer.valueOf(ranged) == 0 && Integer.valueOf(defence) == 0))
					BONUSES.put(id, new String[] { attack, magic, ranged, defence });
			}
			BONUSES.forEach((k, v) -> {
				try {
					writer.flush();
					writer.newLine();
					writer.write(k + " - " + v[0] + ", " + v[1] + ", " + v[2] + ", " + v[3]);
				} catch (Exception e) {
					Logger.getGlobal().catching(e);
				}
			});
		} catch (Exception e) {
			Logger.getGlobal().catching(e);}
		writer.close();
		in.close();
	}
}
