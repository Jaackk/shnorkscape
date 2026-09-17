package com.rs.game.player.actions.magic.lunar.impl;

import com.rs.game.Animation;
import com.rs.game.item.Item;
import com.rs.game.player.FarmingManager;
import com.rs.game.player.FarmingManager.FarmingSpot;
import com.rs.game.player.FarmingManager.SpotInfo;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.magic.lunar.DefaultSpell;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class RemoteFarm implements DefaultSpell {
    
    public static final int[][] LINES = new int[][] { { 10, 11 }, { 12, 13 }, { 14, 15 }, { 16, 17 }, { 18, 19 }, { 20, 139 }, { 28, 29 }, { 30, 31 }, { 32, 33 }, { 34, 35 }, { 36, 37 }, { 38, 39 }, { 40, 41 }, { 43, 44 }, { 45, 46 }, { 47, 48 }, { 49, 50 }, { 51, 52 }, { 53, 54 }, { 55, 56 }, { 57, 58 }, { 136, 137 }, { 60, 61 }, { 62, 63 }, { 64, 65 }, { 66, 67 }, { 69, 70 }, { 71, 72 }, { 73, 74 }, { 75, 76 }, { 77, 138 }, { 79, 80 }, { 81, 82 }, { 83, 84 }, { 86, 87 }, { 88, 89 }, { 90, 91 }, { 92, 93 }, { 99, 100 }, { 101, 102 }, { 103, 104 }, { 105, 106 }, { 107, 108 }, { 109, 110 }, { 141, 142 }, { 143, 144 }, { 145, 146 }, { 147, 148 }, { 6, 7 }, { 8, 9 }, { 97, 111 }, { 112, 113 }, { 114, 115 }, { 116, 117 }, 
        { 118, 119 }, { 120, 140 }, { 4, 5 }, { 121, 122 }, { 123, 124 }, { 125, 126 }, { 128, 129 }, { 130, 131 }, { 132, 133 }, { 134, 135 } };
        
	private static class Patch {
		
		@SuppressWarnings("unused")
        private final int component, index;
		private final String name;
		
		public Patch(final String name, final int component, final int index) {
			this.name = name;
			this.component = component;
			this.index = index;
		}
		
		
		public final int getIndex() {
			return index;
		}
		
		public final String getName() {
			return name;
		}
	}
	
	@Override
	public int getId() {
		return 14846;
	}

	@Override
	public int getLevel() {
		return 78;
	}

	@Override
	public Item[] getRunes() {
		return new Item[] { new Item(ASTRAL_RUNE, 2), new Item(EARTH_RUNE, 2), new Item(NATURE_RUNE, 3) };
	}

	@Override
	public int getDelay() {
		return 3000;
	}
	
	public static final void handleRemoteFarming(final Player player, final int componentId) {
		for (int i=0;i<PATCHES.length;i++) {
		    Patch patch = PATCHES[i];
			if (componentId == LINES[i][1]) {
				if (patch.getIndex() == -1) {
					player.sendMessage("This patch is currently unavailable.");
					return;
				} 
				final SpotInfo info = FarmingManager.SpotInfo.getInfo().get(patch.getIndex());
				final FarmingSpot spot = player.getFarmingManager().getSpot(info);
				if (spot == null) {
					player.sendMessage("There's nothing but weeds growing here.");
					return;
				}
				if (spot.isDiseased()) {
					spot.setDiseased(false);
					player.sendMessage("You've cured your the crops.");
					player.getPackets().sendIComponentText(1082, LINES[i][1], "Patch is healthy");
					return;
				} else if (spot.isCleared()) {
					player.sendMessage("There's nothing growing here.");
					return;
				} else if (spot.isDead()) {
					player.sendMessage("You cannot cure dead crops.");
					return;
				} else if (spot.reachedMaxStage()) {
					player.sendMessage("The crops have already fully grown.");
					return;
				} 
				player.sendMessage("The crops already are healthy.");
				return;
			}
		}
	}
	
	private static final Patch[] PATCHES = new Patch[] {
			new Patch("Tree patch - Taverley", 30, 0),
			new Patch("Tree patch - Falador park", 32, 1),
			new Patch("Tree patch - Varrock", 34, 2),
			new Patch("Tree patch - Lumbridge", 36, 3),
			new Patch("Tree patch - Gnome stronghold", 38, 4),
			new Patch("Fruit tree patch - Gnome stronghold", 49, 5),
			new Patch("Fruit tree patch - Tree gnome village", 51, 6),
			new Patch("Fruit tree patch - Brimhaven", 53, 7),
			new Patch("Fruit tree patch - Catherby", 55, 8),
			new Patch("Fruit tree patch - Lletya", 57, 9),
			new Patch("Fruit tree patch - Herblore habitat", 59, -1),
			new Patch("Allotment patch - Falador (north)", 62, 10),
			new Patch("Allotment patch - Falador (south)", 64, 11),
			new Patch("Allotment patch - Catherby (north)", 66, 12),
			new Patch("Allotment patch - Catherby (south)", 68, 13),
			new Patch("Allotment patch - Ardougne (north)", 70, 14),
			new Patch("Allotment patch - Ardougne (south)", 72, 15),
			new Patch("Allotment patch - Canifis (north)", 74, 16),
			new Patch("Allotment patch - Canifis (south)", 76, 17),
			new Patch("Allotment patch - Harmony island", 190, -1),
			new Patch("Hops patch - Yanille", 79, 18),
			new Patch("Hops patch - Taverley", 81, 19),
			new Patch("Hops patch - Lumbridge", 83, 20),
			new Patch("Hops patch - McGrubor's woods", 85, 21),
			new Patch("Flower patch - Falador", 88, 22),
			new Patch("Flower patch - Catherby", 90, 23),
			new Patch("Flower patch - Ardougne", 92, 24),
			new Patch("Flower patch - Canifis", 94, 25),
			new Patch("Spirit tree patch - Etceteria", 97, -1),
			new Patch("Spirit tree patch - Port sarim", 99, -1),
			new Patch("Spirit tree patch - Brimhaven", 101, -1),
			new Patch("Bush patch - Champions' guild", 104, -1),
			new Patch("Bush patch - Rimmington", 106, 26),
			new Patch("Bush patch - Etceteria", 108, 27),
			new Patch("Bush patch - Ardougne", 110, 28),
			new Patch("Potato patch - Taverley", 115, 29),
			new Patch("Evil turnip patch - Draynor manor", 117, -1),
			new Patch("Belladonna patch - Draynor manor", 119, 40),
			new Patch("Calquat patch - Tai Bwo Wannai", 121, -1),
			new Patch("Herb patch - Falador", 123, 30),
			new Patch("Herb patch - Catherby", 125, 31),
			new Patch("Herb patch - Ardougne", 131, 32),
			new Patch("Herb patch - Canifis", 127, 33),
			new Patch("Herb patch - Troll stronghold", 129, -1),
			new Patch("Mushroom patch - Canifis", 2, 39),
			new Patch("Mushroom patch - Tirannwn", 173, -1),
			new Patch("Limpwurt patch - Wilderness", 175, -1),
			new Patch("Herb patch - Wilderness", 177, -1),
			new Patch("Cactus patch - Al-Kharid", 182, -1),
			new Patch("Jade vine patch - Ardougne", 184, -1),
			new Patch("Elder patch - Prifddinas", 186, -1),
			new Patch("Crystal patch - Prifddinas", 188, -1)
	};

	@Override
	public boolean spellEffect(Player player) {
		WorldTasksManager.schedule(new WorldTask() {
			private int loop;
			@Override
			public void run() {
				if (loop == 0) {
					player.setNextAnimation(new Animation(4823));
				} else if (loop == 1) {
					player.getSkills().addXp(Skills.MAGIC, 79);
				       for (int i=0;i<PATCHES.length;i++) {
					Patch patch = PATCHES[i];
						player.getPackets().sendIComponentText(1082, LINES[i][0], patch.getName());
						if (patch.getIndex() == -1)
							player.getPackets().sendIComponentText(1082, LINES[i][1], "N/A");
						else {
							final SpotInfo info = FarmingManager.SpotInfo.getInfo().get(patch.getIndex());
							final FarmingSpot spot = player.getFarmingManager().getSpot(info);
							if (spot == null)
								player.getPackets().sendIComponentText(1082, LINES[i][1], "Full of weeds");
							else {
								if (spot.isDiseased())
									player.getPackets().sendIComponentText(1082, LINES[i][1], "<col=ff0000>Crops are diseased");
								else if (spot.isDead())
									player.getPackets().sendIComponentText(1082, LINES[i][1], "Crops are dead");
								else if (spot.isCleared() && spot.getStage() == 3)
									player.getPackets().sendIComponentText(1082, LINES[i][1], "Patch is empty");
								else if (spot.reachedMaxStage())
									player.getPackets().sendIComponentText(1082, LINES[i][1], "Ready for harvesting");
								else
									player.getPackets().sendIComponentText(1082, LINES[i][1], "Patch is healthy");
							}
						}
					}
				    for(int i=PATCHES.length;i< LINES.length;i++) {
				        player.getPackets().sendHideIComponent(1082, LINES[i][0], true);
				        player.getPackets().sendHideIComponent(1082, LINES[i][1], true);
				    }
					player.getInterfaceManager().sendInterface(1082);
					player.getTemporaryAttributtes().put("remoteFarm", true);
					player.setCloseInterfacesEvent(() -> player.getTemporaryAttributtes().remove("remoteFarm"));
					stop();
				}
				loop++;
			}
		}, 0, 2);
		return true;
	}

}
