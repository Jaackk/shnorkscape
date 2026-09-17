package com.rs.tools;


import com.rs.cache.Cache;
import com.rs.cache.filestore.io.InputStream;
import com.rs.cache.filestore.store.Index;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.cache.loaders.RenderAnimDefinitions;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.npcs.NPCCombatDefinitionsDataParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class TestTool {
		
	private static class Definition {
		
		int hp = -1;
		int[] max;
		int speed = -1;
		int[] combat;
		int[] accuracy;
		int armour = -1;
		int[] affinities;
		String name;
		
		public Definition(String name, int hp, int[] max, int speed, int[] combat, int[] accuracy, int armour, int[] affinities) {
			this.hp = hp;
			this.max = max;
			this.speed = speed;
			this.combat = combat;
			this.accuracy = accuracy;
			this.armour = armour;
			this.affinities = affinities;
			this.name = name;
		}
		
	}
	
	private static final Map<Integer, String> map = new HashMap<Integer, String>();
	
	//private static final List<String> defs = new ArrayList<String>();
	
	//private static final List<Definition> defs = new ArrayList<Definition>();
	
	public static void main(String[] args) throws Exception {
		
		if (args != null) {
			Cache.init();
			BufferedWriter writer = new BufferedWriter(new FileWriter("Latest npc list.txt", true));
			for (int i = 0; i < Utils.getNPCDefinitionsSize(); i++) {
				NPCDefinitions defs = NPCDefinitions.getNPCDefinitions(i);
				RenderAnimDefinitions d = RenderAnimDefinitions.getRenderAnimDefinitions(defs.renderEmote);
				writer.flush();
				writer.newLine();
				if (d != null)
					writer.write(i + " - " + defs.name + ": Stand: " + d.standAnimation + ", Walk: " + d.walkAnimation + ", Run: " + d.runAnimation);
				else
					writer.write(i + " - " + defs.name + ": Stand: " + -1 + ", Walk: " + -1 + ", Run: " + -1);
				writer.flush();
			}
			return;
			/*Cache.init();
			BufferedWriter writer = new BufferedWriter(new FileWriter("ND.txt", true));
			for (int i = 24396; i < Utils.getNPCDefinitionsSize(); i++) {
				NPCDefinitions defs = NPCDefinitions.getNPCDefinitions(i);
				if (defs == null)
					continue;
				if (defs.name == null)
					continue;
				RenderAnimDefinitions d = RenderAnimDefinitions.getRenderAnimDefinitions(defs.renderEmote);
				if (d == null)
					continue;
				writer.flush();
				writer.write(i + " - " + defs.name + ": Stand: " + d.standAnimation + ", Walk: " + d.walkAnimation + ", Run: " + d.runAnimation);
				writer.newLine();
				writer.flush();
			}
			/*ItemBonuses.init();
			BufferedWriter writer = new BufferedWriter(new FileWriter("Item list.txt", true));
			for (int i = 25555; i < Utils.getItemDefinitionsSize(); i++) {
				ItemDefinitions defs = ItemDefinitions.getItemDefinitions(i);
				if (defs == null)
					continue;
				if (defs.isWearItem() && ItemBonuses.getItemBonuses(i) == null) {
					writer.flush();
					writer.newLine();
					writer.write(i + " - " + defs.name);
					writer.flush();
				}
			}*/
			/*writer.close();
			return;*/
		}
		
		/*BufferedReader reader = new BufferedReader(new FileReader("NPC Data RS.txt"));
		BufferedWriter writer = new BufferedWriter(new FileWriter("NPC Animations unpacked.txt", true));
		String line = null;
		int id = 0;
		while ((line = reader.readLine()) != null) {
			try {
				String[] split = line.split(", ");
				id = Integer.parseInt(split[0].substring(7));
				String[] groups = split[1].split("animations");
				String animations = groups[1].substring(groups[1].indexOf('{') + 1, groups[1].indexOf('}'));
				String[] anim = animations.split(",");
				int attack = -1;
				int death = -1;
				for (String a : anim) {
					if (a.startsWith("\"death"))
						death = Integer.valueOf(a.substring(8));
					else if (a.startsWith("\"attack"))
						attack = Integer.valueOf(a.substring(9));
					else if (attack == -1 && a.startsWith("range"))
						attack = Integer.valueOf(a.substring(8));
				}
				if (attack == -1 && death == -1)
					continue;
				map.put(id, attack + " " + death);
				
			} catch (Exception e) {
				continue;
			}
		}
		map.forEach((k, v) -> {
			try {
		writer.flush();
				writer.write(k + " - " + v);
				writer.newLine();
				writer.flush();
			} catch (Exception e) {
				
			}
		} );*/
		
		Cache.init();
		Index index = Cache.STORE.getIndexes()[24];
		for (int i = 0; i <= index.getLastFileId(1); i++) {
			Logger.getGlobal().info(i + " - " + new InputStream(index.getFile(1, i)).readString());
		}
		if (index != null)
			return;
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		Cache.init();
		NPCCombatDefinitionsDataParser.init();
		BufferedReader reader = new BufferedReader(new FileReader("Anims.txt"));
		BufferedWriter writer = new BufferedWriter(new FileWriter("Updated defs.txt", true));
		Iterator<String> it = reader.lines().iterator();
		while (it.hasNext()) {
			String line = it.next();
			String[] anims = line.split(" - ");
			map.put(Integer.valueOf(anims[0]), anims[1]);
		}
	Logger.getGlobal().info("Failed to write: " + NPCCombatDefinitionsDataParser.getDefinitions().size());
	for (Entry<Integer, NPCCombatDefinition> def : NPCCombatDefinitionsDataParser.getDefinitions().entrySet()) {
		String name = NPCDefinitions.getNPCDefinitions(def.getKey()).getName();
		if (name == null)
			continue;
		NPCCombatDefinition de = def.getValue();
		int attack = map.containsKey(def.getKey()) ? Integer.parseInt(map.get(def.getKey()).split(" ")[0]) : de.getAttackEmote();
		int death = map.containsKey(def.getKey()) ? Integer.valueOf(map.get(def.getKey()).split(" ")[1]) : de.getDeathEmote();
		
		String d = def.getKey() + " - " + de.getHitpoints() + " " + attack + " " + de.getDefenceEmote() + " " + death + " " + de.getAttackDelay() + " " + de.getDeathDelay() + " " + de.getRespawnDelay() + " " + de.getMaxHit() + " " + (de.getAttackStyle() == 0 ? "MELEE" : de.getAttackStyle() == 1 ? "RANGED" : de.getAttackStyle() == 2 ? "MAGIC" : de.getAttackStyle() == 3 ? "SPECIAL" : "SPECIAL2") + " " + de.getAttackGfx() + " " + de.getAttackProjectile() + " " + (de.getAggressivenessType() == 0 ? "PASSIVE" : "AGGRESSIVE");
		writer.flush();
		writer.newLine();
		writer.write(d);
		writer.flush();
	}
		/*writer.write("//Format: ID - Armour - Max hit (melee, ranged, magic, special) - Combat (Melee, ranged, magic) - Accuracy (Melee, ranged, magic) - Affinities (Type-specific, melee, ranged, magic)");
		loop : for (int i = 0; i < Utils.getNPCDefinitionsSize(); i++) {
			NPCDefinitions def = NPCDefinitions.getNPCDefinitions(i);
			if (def == null)
				continue;
			for (Definition d : defs) {
				if (d.name.equals(def.name)) {
				writer.flush();
			writer.newLine();
			writer.write(i + " - " + d.armour + " - " + d.max[0] / 10 + " " + d.max[1] / 10 + " " + d.max[2] / 10 + " " + d.max[3] / 10 +
					" - " + d.combat[0] + " " + d.combat[1] + " " + d.combat[2] + " - " + d.accuracy[0] + " " + d.accuracy[1] + " " + d.accuracy[2] + " - " + d.affinities[0] + " " + d.affinities[1] + " " + d.affinities[2] + " " + d.affinities[3]);
			writer.flush();
			continue loop;
				}
			}
		}*/
	
	
		writer.close();
		reader.close();
		
		
		
		
		
		
		
		/*BufferedReader reader = new BufferedReader(new FileReader("NPC Definitions V1.txt"));
		BufferedWriter writer = new BufferedWriter(new FileWriter("NPC Definitions V2.txt", true));
		Iterator<String> it = reader.lines().iterator();
		int amount = 0;
		while (it.hasNext()) {
			String line = it.next();
			String[] lines = line.split(" - ");
			String name = lines[0];
			String[] bonuses = lines[1].split(" ");
			int[] b = new int[bonuses.length];
			for (int i = 0; i < bonuses.length; i++)
				b[i] = Integer.valueOf(bonuses[i]);
			Definition definition = null;
			try {
				definition = new Definition(name, b[0], new int[] { b[1], b[2], b[3], b[4] }, b[5], new int[] { b[6], b[7], b[8] }, new int[] { b[9], b[10], b[11] }, b[12], new int[] { b[13], b[14], b[15], b[16] });
			} catch (Exception e) {
				amount++;
				continue;
			}
			if (definition != null)
				defs.add(definition);
		}
	Logger.getGlobal().info("Failed to write: " + amount);
		defs.forEach(d -> {
			try {
			writer.flush();
			writer.newLine();
			writer.write(d.name + " - " + (d.hp / 10) + " " +  d.speed + " " + d.armour + " - " + d.max[0] + " " + d.max[1] + " " + d.max[2] + " " + d.max[3] +
					" - " + d.combat[0] + " " + d.combat[1] + " " + d.combat[2] + " - " + d.accuracy[0] + " " + d.accuracy[1] + " " + d.accuracy[2] + " - " + d.affinities[0] + " " + d.affinities[1] + " " + d.affinities[2] + " " + d.affinities[3]);
			writer.flush();
			} catch (Exception e) {
			}
		});
		writer.close();
		reader.close();*/
	/*	BufferedWriter writer = new BufferedWriter(new FileWriter("NPC Definitions V1.txt", true));
		  File dir = new File("npcdump/");
		  File[] directoryListing = dir.listFiles();
		  if (directoryListing != null) {
		  fileLoop :  for (File child : directoryListing) {
		      BufferedReader reader = new BufferedReader(new FileReader(child));
		      Iterator<String> it = reader.lines().iterator();
		      StringBuilder builder = new StringBuilder();
		      int c = 0;
		     // Definition def;
		      boolean hp = false;
		      boolean max = false;
		      boolean speed = false;
		      boolean combatlv = false;
		      boolean accuracy = false;
		      boolean armour = false;
		      boolean affinities = false;
		      boolean cont = false;
		      while (it.hasNext()) {
		    	  if (cont)
		    		  break;
		    	  if (c > 0) {
		    		  c--;
		    		  it.next();
		    		  continue;
		    	  }
		    	  String line = it.next();
		    	  if (max) {
		    		  try {
		    		  String[] levels = line.split("	");
		    		  if (builder.length() == 0)
		    			  System.err.println(child.getName());
		    		  //int hpInt = Integer.valueOf(levels[1].replaceAll(",", ""));
		    		 // builder.append("-");
		    		  for (String lv : levels) {
		    			  try {
		    				 builder.append(" " + Integer.valueOf(lv));
		    			  } catch (Exception e) {
		    				  builder.append(" " + Integer.valueOf(0));
		    			  }
		    		  }
		    		 // builder.append("-");
		    		  } catch (Exception e) { }
		    		  max = false;
		    	  }
		    	  if (affinities) {
		    		  try {
			    		  String[] levels = line.split("	");
			    		  if (builder.length() == 0)
			    			  Logger.getGlobal().info(child.getName());
			    		  //int hpInt = Integer.valueOf(levels[1].replaceAll(",", ""));
			    		//  builder.append("-");
			    		  for (String lv : levels) {
			    			  try {
			    				 builder.append(" " + Integer.valueOf(lv));
			    			  } catch (Exception e) {
			    				  builder.append(" " + Integer.valueOf(1));
			    			  }
			    		  }
			    		//  builder.append("-");
			    		  } catch (Exception e) { }
			    		  affinities = false;
			    		  cont = true;
			    		 // continue fileLoop;
		    	  }
		    	  if (armour) {
		    		  try {
			    		  String[] levels = line.split("	");
			    		  if (builder.length() == 0)
			    			  continue;
			    		  //int hpInt = Integer.valueOf(levels[1].replaceAll(",", ""));
			    		  //for (String lv : levels) {
			    			  builder.append(" " + Integer.valueOf(levels[0]));
			    		 // }
			    		  } catch (Exception e) { }
			    		  armour = false;
		    	  }
		    	  if (accuracy) {
		    		  try {
			    		  String[] levels = line.split("	");
			    		  if (builder.length() == 0)
			    			  System.err.println(child.getName());
			    		  //int hpInt = Integer.valueOf(levels[1].replaceAll(",", ""));
			    		//  builder.append("-");
			    		  for (String lv : levels) {
			    			  try {
			    				 builder.append(" " + Integer.valueOf(lv));
			    			  } catch (Exception e) {
			    				  builder.append(" " + Integer.valueOf(1));
			    			  }
			    		  }
			    		//  builder.append("-");
			    		  } catch (Exception e) { }
			    		  accuracy = false;
		    	  }
		    	  if (combatlv) {
		    		  try {
			    		  String[] levels = line.split("	");
			    		  if (builder.length() == 0)
			    			  System.err.println(child.getName());
			    		  //int hpInt = Integer.valueOf(levels[1].replaceAll(",", ""));
			    	//	  builder.append("-");
			    		  for (String lv : levels) {
			    			  try {
			    				 builder.append(" " + Integer.valueOf(lv));
			    			  } catch (Exception e) {
			    				  builder.append(" " + Integer.valueOf(1));
			    			  }
			    		  }
			    	//	  builder.append("-");
			    		  } catch (Exception e) { }
			    		  combatlv = false;
		    	  }
		    	  if (hp) {
		    		  try {
		    		  String[] levels = line.split("	");
		    		  int hpInt = 1;
		    		  if (levels[1].equals("N/A"))
		    			  hpInt = 1;
		    		  else if (levels[1].equals("Varies"))
		    			  hpInt = 1;
		    		  else if (levels[1].equals("? (edit)"))
		    			  hpInt = 1;
		    		  else
		    			  hpInt = Integer.valueOf(levels[1].replaceAll(",", ""));
		    		  builder.append(child.getName().replace(".txt", "") + " - " + hpInt);
		    		  } catch (Exception e) { }
		    		  hp = false;

		    	  }
		    	  if (line.equals("Affinities")) {
		    		  c += 3;
		    		  affinities = true;
		    	  }
		    	  if (line.equals("Armour	")) {
		    		  armour = true;
		    		  c++;
		    	  }
		    	  if (!speed && line.contains(" ticks (")) {
		    		  try {
		    		  builder.append(" " + Integer.valueOf(line.split(" ")[0]));
		    		  } catch (Exception e) {
		    			  System.err.println("Fix speed for: " + child.getName());
		    		  }
		    	  }
		    		 if (line.equals("Combat levels")) {
		    			 c +=2;
		    			 combatlv = true;
		    		 }
		    	  if (line.equals("Accuracy")) {
		    		  accuracy = true;
		    		  c += 2;
		    	  }
		    	  if (line.equals("Max hit")) {
		    		  c += 3;
		    		  max = true;
		    	  }
		    	  if (line.equals("Level	LP	 XP	 XP"))
		    		  hp = true;
		    	  else if (line.equals("Level	LP	 XP	")) {
		    		  hp = true;
		    		  c++;
		    	  } else if (line.equals("Level	LP	")) {
		    		  hp = true;
		    		  c += 2;
		    	  }
		    	  
		      }
		      if (builder.toString().length() > 0) {
		      writer.flush();
		      writer.newLine();
		      writer.write(builder.toString());
		      writer.flush();
		      }
		      reader.close();
		    }
		  } else {
		    System.err.println("Couldn't find files.");
		  }
		writer.close();*/
	}
}