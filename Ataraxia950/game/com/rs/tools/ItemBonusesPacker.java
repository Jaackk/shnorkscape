package com.rs.tools;

import com.rs.cache.Cache;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class ItemBonusesPacker {

	@SuppressWarnings("resource")
	public static final void main(String[] args) throws IOException {
		Cache.init();
		DataOutputStream out = new DataOutputStream(new FileOutputStream("data/items/bonuses.ib"));
			//File folder = new File("data/items/bonuses/");
			//File[] listOfFiles = folder.listFiles();

			/**for (int i = 0; i < listOfFiles.length; i++) {
			  File file = listOfFiles[i];
			  try {
			  if (file.isFile() && file.getName().endsWith(".txt")) {
				  BufferedReader reader = new BufferedReader(new FileReader(file));
				  final String name = file.getName();
				  final int itemId = Integer.valueOf(name.split(" - ")[0]);
				  out.writeShort(itemId);
				  reader.readLine();
				  out.writeShort(Integer.valueOf(reader.readLine().substring(6)));
				  out.writeShort(Integer.valueOf(reader.readLine().substring(7)));
				  out.writeShort(Integer.valueOf(reader.readLine().substring(7)));
				  out.writeShort(Integer.valueOf(reader.readLine().substring(7)));
				  out.writeShort(Integer.valueOf(reader.readLine().substring(8)));
				  reader.readLine();
				  reader.readLine();
				  out.writeShort(Integer.valueOf(reader.readLine().substring(6)));
				  out.writeShort(Integer.valueOf(reader.readLine().substring(7)));
				  out.writeShort(Integer.valueOf(reader.readLine().substring(7)));
				  out.writeShort(Integer.valueOf(reader.readLine().substring(7)));
				  out.writeShort(Integer.valueOf(reader.readLine().substring(8)));
				  out.writeShort(Integer.valueOf(reader.readLine().substring(11)));
				  reader.readLine();
				  reader.readLine();
				  out.writeShort(Integer.valueOf(reader.readLine().substring(7)));
				  out.writeShort(Integer.valueOf(reader.readLine().substring(7)));
				  out.writeShort(Integer.valueOf(reader.readLine().substring(8)));
				  reader.readLine();
				  reader.readLine();
				  out.writeShort(Integer.valueOf(reader.readLine().substring(10)));
				  out.writeShort(Integer.valueOf(reader.readLine().substring(17)));
				  out.writeShort(Integer.valueOf(reader.readLine().substring(8)));
				  out.writeShort(Integer.valueOf(reader.readLine().substring(14)));
			    /* do somthing with content */
			 /** } } catch (Exception e) {
				Logger.getGlobal().info(file.getName());
			}
			}*/
		
		for (int itemId = 0; itemId < Utils.getItemDefinitionsSize(); itemId++) {
			File file = new File("data/items/bonuses/" + itemId + ".txt");
			if (file.exists()) {
				try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				out.writeShort(itemId);
				reader.readLine();
				// att bonuses
				out.writeShort(Integer.parseInt(reader.readLine()));
				out.writeShort(Integer.parseInt(reader.readLine()));
				out.writeShort(Integer.parseInt(reader.readLine()));
				out.writeShort(Integer.parseInt(reader.readLine()));
				out.writeShort(Integer.parseInt(reader.readLine()));
				reader.readLine();
				// def bonuses
				out.writeShort(Integer.parseInt(reader.readLine()));
				out.writeShort(Integer.parseInt(reader.readLine()));
				out.writeShort(Integer.parseInt(reader.readLine()));
				out.writeShort(Integer.parseInt(reader.readLine()));
				out.writeShort(Integer.parseInt(reader.readLine()));
				out.writeShort(Integer.parseInt(reader.readLine()));
				reader.readLine();
				// Damage absorption
				out.writeShort(Integer.parseInt(reader.readLine()));
				out.writeShort(Integer.parseInt(reader.readLine()));
				out.writeShort(Integer.parseInt(reader.readLine()));
				reader.readLine();
				// Other bonuses
				out.writeShort(Integer.parseInt(reader.readLine()));
				out.writeShort(Integer.parseInt(reader.readLine()));
				out.writeShort(Integer.parseInt(reader.readLine()));
				out.writeShort(Integer.parseInt(reader.readLine()));
				if (reader.readLine() != null)
					throw new RuntimeException("Should be null line" + itemId);
				} catch (Exception e) {
					Logger.getGlobal().info(file.getName());
				}
			}
		}
		out.flush();
		out.close();
		Logger.getGlobal().info("Finished packing Item equipment bonuses!");
	//}
	}
}