package com.rs.game.npc.gwd2.faction;

import com.rs.Settings;
import com.rs.game.WorldTile;
import com.rs.game.npc.gwd2.FactionNPC;
import com.rs.game.player.content.HeartOfGielinor;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public final class FactionManager {

	private static final List<FactionCluster> CLUSTERS = new ArrayList<FactionCluster>();
	private static final FactionNode[] NODES = new FactionNode[FactionNodeLocation.values().length];
	private static final WorldTile SEREN = new WorldTile(3260, 6918, 1),
			SLISKE = new WorldTile(3268, 7038, 1),
			ZAMORAK = new WorldTile(3147, 7038, 1),
			ZAROS = new WorldTile(3138, 6911, 1);
	
	static {
		for (int i = 0; i < NODES.length; i++) {
			final FactionNodeLocation location = FactionNodeLocation.values()[i];
			NODES[i] = new FactionNode(location.getTile(), location.getDefaultType(), location.toString());
		}
	}
	
	public static final void init() {
		/*for (int i = 0; i < NODES.length; i++) {
			FactionCluster cluster = new FactionCluster(NODES[i].getType(), NODES[i]);
			CoresManager.getServiceProvider().scheduleFixedLengthTask(cluster, 1, 1);
			callReinforcements(cluster, NODES[i]);
		}*/
	}
	
	public static final void callReinforcements(final FactionCluster cluster, final FactionNode location) {
		final int amount = Utils.random(4, 7);
		for (int x = 0; x < amount; x++) {
			final FactionNPC npc = getNPC(cluster);
			npc.setLocation(location);
			cluster.getClusterNPCs().add(npc);
		}
		if (Settings.DEBUG)
			Logger.getGlobal().info(HeartOfGielinor.getGod(cluster.getType()) + " has called reinforcements for the " + location.getName() + ".");
	}
	
	public static final void addCluster(final FactionCluster cluster) {
		CLUSTERS.add(cluster);
	}
	
	public static final boolean removeCluster(final FactionCluster cluster) {
		return CLUSTERS.remove(cluster);
	}
	
	public static final FactionNode[] getNodes() {
		return NODES;
	}
	
	private static final FactionNPC getNPC(final FactionCluster cluster) {
		switch(cluster.getType()) {
		case HeartOfGielinor.SEREN:
			return new SerenClusterNPC(Utils.random(22470, 22477), new WorldTile(SEREN, 1), -1, true, true, cluster);
		case HeartOfGielinor.SLISKE:
			return new SliskeClusterNPC(Utils.random(22478, 22485), new WorldTile(SLISKE, 1), -1, true, true, cluster);
		case HeartOfGielinor.ZAMORAK:
			return new ZamorakClusterNPC(Utils.random(22486, 22495), new WorldTile(ZAMORAK, 1), -1, true, true, cluster);
		default:
			return new ZarosClusterNPC(Utils.random(22496, 22502), new WorldTile(ZAROS, 1), -1, true, true, cluster);
		}
	}
	
}
