package com.rs.game.npc.gwd2.faction;

import com.rs.cores.FixedLengthRunnable;
import com.rs.game.npc.gwd2.FactionNPC;
import com.rs.game.player.content.HeartOfGielinor;
import com.rs.utils.Logger;

import java.util.ArrayList;
import java.util.List;

public class FactionCluster extends FixedLengthRunnable {

	private final int type;
	private final List<FactionNPC> npcs;
	private FactionNode node;
	private int seconds;
	
	/**
	 * Creates a FactionCluster object with an empty arraylist of npcs.
	 * @param type type of the faction.
	 */
	public FactionCluster(final int type, final FactionNode node) {
		this.type = type;
		this.node = node;
		npcs = new ArrayList<FactionNPC>();
	}
	
	/**
	 * Gets a list of FactionNPCs that belong to the cluster.
	 * @return list of FactionNPCs in cluster.
	 */
	public final List<FactionNPC> getClusterNPCs() {
		return npcs;
	}
	
	/**
	 * Gets the faction type (SEREN = 0, SLISKE = 1, ZAROS = 2, ZAMORAK = 3).
	 * @return type.
	 */
	public final int getType() {
		return type;
	}
	
	/**
	 * Gets the FactionNode object of this cluster.
	 * @return node.
	 */
	public final FactionNode getNode() {
		return node;
	}
	
	/**
	 * Merges two of the same type clusters into one.
	 * @param cluster the opposite cluster that's merged with this.
	 * @return {@code true} if the merge was successful
	 */
	public final boolean mergeCluster(final FactionCluster cluster) {
		if (cluster.getType() != type)
			return false;
		final int size = npcs.size();
		cluster.getClusterNPCs().forEach(n -> {
			if (!n.isDead() && !n.hasFinished())
				npcs.add(n);
		});
		if (size == npcs.size())
			return false;
		return cluster.destroyCluster();
	}
	
	/**
	 * Destroys the cluster from the game completely.
	 * @return {@code true} if the cluster was removed successfully.
	 */
	private final boolean destroyCluster() {
		stopNow(true);
		npcs.clear();
		return FactionManager.removeCluster(this);
	}
	
	/**
	 * Processes the faction with a second interval.
	 */
	@Override
	public boolean repeat() {
		try {
			if (node == null)
				return true;
			if (npcs.size() < 4) {
				FactionManager.callReinforcements(this, node);
				return true;
			}
			if (!isFighting()) {
				seconds++;
				if (seconds == 10) 
					splitCluster();
			} else
				seconds = 0;
		} catch (Exception e) {
			Logger.getGlobal().catching(e);
		}
		return true;
	}
	
	/**
	 * Splits the cluster into two parties after they've conquered a node.
	 * A part of the cluster will remain on the node to defend it,
	 * the rest of it will pick a new random cluster and attempt to go conquer that.
	 * The defending entities will no longer ever be a part of a cluster.
	 * After conquering a node, members of the faction that conquered the node
	 * will have their NPCs respawn in that location.
	 */
	private final void splitCluster() {
		final List<FactionNPC> npcs = new ArrayList<FactionNPC>();
		for (FactionNPC n : this.npcs) {
			if (n.withinDistance(node.getTile(), 25))
				npcs.add(n);
		}
		/*int protectors = Utils.random(3, 6);
		if (protectors > npcs.size())
			protectors = npcs.size();
		while (true) {
			if (protectors <= 0)
				break;
			final FactionNPC npc = npcs.get(Utils.random(protectors--));
			npc.setState(FactionNPC.DEFAULT);
			npc.setRun(false);
			this.npcs.remove(npc);
		}*/
		final FactionNode currentNode = node;
		node.setType(type);
		generateFaction();
		Logger.getGlobal().info(HeartOfGielinor.getGod(getType()) + " has conquered the " + currentNode.getName() + ". " + (this.npcs.size() - npcs.size()) + " have taken the protective stance; " + this.npcs.size() + " have left to conquer the " + node.getName() + ".");
		seconds = 0;
	}
	
	/**
	 * Whether at least one of the NPCs in the cluster is near destination & fighting.
	 * @return {@code true} cluster is fighting.
	 */
	private final boolean isFighting() {
		boolean fighting = false;
		for (FactionNPC n : npcs) {
			if (n == null)
				continue;
			if (n.withinDistance(node.getTile(), 25) && n.isUnderCombat()) {
				fighting = true;
				break;
			}
		}
		return fighting;
	}
	
	/**
	 * Generates a new FactionNode that the non-protective NPCs will start moving towards.
	 */
	private final void generateFaction() {
		final List<FactionNode> nodes = new ArrayList<FactionNode>();
		for (FactionNode node : FactionManager.getNodes()) {
			if (!node.equals(this.node) && node.getType() != this.type)
				nodes.add(node);
		}
		if (nodes.size() == 0)
			return;
		FactionNode node = nodes.get(0);
		for (FactionNode n : nodes) {
			if (n.getTile().getDistance(this.node.getTile()) < node.getTile().getDistance(this.node.getTile()))
				node = n;
		}
		this.node = node;
		for (FactionNPC n : npcs) {
			if (n == null)
				continue;
			n.setRun(true);
			n.setLocation(node);
			n.sendConquering();
		}
	}
	
}
