package com.rs.cores;

import com.rs.Settings;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

public final class WorldThread extends Thread {

	public static volatile long WORLD_CYCLE;
	public static StringBuilder DEBUG_MESSAGE = new StringBuilder();

	protected WorldThread() {
		setPriority(Thread.MAX_PRIORITY);
		setName("World Thread");
	}

	// private static int GC_COUNTER = 0;

	@Override
	public final void run() {
		while (!CoresManager.shutdown) {
			WORLD_CYCLE++; // made the cycle update at begin instead of end cuz
			// at end theres 600ms then to next cycle
			final long currentTime = Utils.currentTimeMillis();
			//DEBUG_MESSAGE.append("a ");
			// long debug = Utils.currentTimeMillis();
			CoresManager.getServiceProvider().runGameTasks();
			WorldTasksManager.processTasks();
			//DEBUG_MESSAGE.append("b ");
			//DEBUG_MESSAGE.append("c ");
			try {
				for (final Player player : World.getPlayers()) {
					try {
						//DEBUG_MESSAGE.append("extra measure1 (" + player.getDisplayName() + ") ");
						if (!player.isActive() || player.hasFinished()) {
							continue;
						}
						//DEBUG_MESSAGE.append("extra measure2 (" + player.getDisplayName() + ") ");
						// hopefully prevent accounts from getting stuck online
						if (!player.isBot() && player.getFlowerPokerSession() == null && Settings.AUTO_KICK_SOULLESS && (Utils.currentTimeMillis() - player.getLastPacketReceivedTime() > (10_000))) {
							World.sendWorldMessage("<img=7><col=ff0000>Auto-kick Log: " + player.getDisplayName()
									+ " was soulless and was automatically kicked.", true);
							Logger.getGlobal().info("Auto-kick Log: " + player.getDisplayName()
									+ " was soulless and was automatically kicked.");
							//DEBUG_MESSAGE.append("extra measure3 (" + player.getDisplayName() + ") ");
							World.removePlayerLobby(player.getUsername());
							player.forceLogout();
							//DEBUG_MESSAGE.append("extra measure4 (" + player.getDisplayName() + ") ");
							continue;
						}
						//DEBUG_MESSAGE.append("d (" + player.getDisplayName() + ") ");
						player.processEntity();
						//DEBUG_MESSAGE.append("e (" + player.getDisplayName() + ") ");
					} catch (final Exception e) {
						Logger.getGlobal().catching(e);
					}
				}
				for (final NPC npc : World.getNPCs()) {
					try {
						if (npc == null || npc.hasFinished()) {
							continue;
						}
						//DEBUG_MESSAGE.append("f (" + npc.getId() + ")");
						npc.processEntity();
						//DEBUG_MESSAGE.append("g (" + npc.getId() + ")");
					} catch (final Exception e) {
						Logger.getGlobal().catching(e);
					}
				}
			} catch (final Throwable e) {
				Logger.getGlobal().catching(e);
			}
			try {
				for (final Player player : World.getPlayers()) {
					try {
						if (!player.isActive() || player.hasFinished()) {
							continue;
						}
						//DEBUG_MESSAGE.append("h (" + player.getDisplayName() + ") ");
						player.processEntityUpdate();
						//DEBUG_MESSAGE.append("i (" + player.getDisplayName() + ") ");
					} catch (final Exception e) {
						Logger.getGlobal().catching(e);
					}
				}
				for (final NPC npc : World.getNPCs()) {
					try {
						if (npc == null || npc.hasFinished()) {
							continue;
						}
						//DEBUG_MESSAGE.append("j (" + npc.getId() + ")");
						npc.processEntityUpdate();
						//DEBUG_MESSAGE.append("j (" + npc.getId() + ")");
					} catch (final Exception e) {
						Logger.getGlobal().catching(e);
					}
				}
			} catch (final Throwable e) {
				Logger.getGlobal().catching(e);
			}
			try {
				// System.out.print(" ,NPCS PROCESS: "+(Utils.currentTimeMillis()-debug));
				// debug = Utils.currentTimeMillis();

				for (final Player player : World.getPlayers()) {
					try {
						if (!player.isActive() || player.hasFinished()) {
							continue;
						}
						if (player.isBot()) {
							player.processProjectiles();
							continue;
						}
						//DEBUG_MESSAGE.append("k (" + player.getDisplayName() + ") ");
						player.getPackets().sendLocalPlayersUpdate();
						//DEBUG_MESSAGE.append("l (" + player.getDisplayName() + ") ");
						player.getPackets().sendLocalNPCsUpdate();
						//DEBUG_MESSAGE.append("m (" + player.getDisplayName() + ") ");
						player.processProjectiles();// waits for player to walk and so on
						//DEBUG_MESSAGE.append("n (" + player.getDisplayName() + ") ");
					} catch (final Exception e) {
						Logger.getGlobal().catching(e);
					}
				}
			} catch (final Throwable e) {
				Logger.getGlobal().catching(e);
			}
			
			try {
				//DEBUG_MESSAGE.append("o ");
				World.removeProjectiles();
				//DEBUG_MESSAGE.append("o2 ");
			} catch (final Throwable e) {
				Logger.getGlobal().catching(e);
			}
			try {
				// System.out.print(" ,PLAYER UPDATE:
				// "+(Utils.currentTimeMillis()-debug)+",
				// "+World.getPlayers().size()+", "+World.getNPCs().size());
				// debug = Utils.currentTimeMillis();
				for (final Player player : World.getPlayers()) {
					try {
						if (!player.isActive() || player.hasFinished()) {
							continue;
						}
						//DEBUG_MESSAGE.append("p (" + player.getDisplayName() + ") ");
						player.resetMasks();
						//DEBUG_MESSAGE.append("q (" + player.getDisplayName() + ") ");
					} catch (final Exception e) {
						Logger.getGlobal().catching(e);
					}
				}
				for (final NPC npc : World.getNPCs()) {
					try {
						if (npc == null || npc.hasFinished()) {
							continue;
						}
						//DEBUG_MESSAGE.append("r (" + npc.getId() + ")");
						npc.resetMasks();
						//DEBUG_MESSAGE.append("s (" + npc.getId() + ")");
					} catch (final Exception e) {
						Logger.getGlobal().catching(e);
					}
				}
			} catch (final Throwable e) {
				Logger.getGlobal().catching(e);
			}

			try {
				for (final Player player : World.getPlayers()) {
					try {
						if (player == null || !player.isActive() || player.hasFinished()) {
							continue;
						}
						if (player.isBot()) {
							continue;
						}
						player.getPackets().sendServerTickEndPacket();
						if (player.getRealChannel() == null || !player.getRealChannel().isActive()) {
							//DEBUG_MESSAGE.append("t (" + player.getDisplayName() + ")");
							player.finish();
							//DEBUG_MESSAGE.append("u (" + player.getDisplayName() + ")");
						}
					} catch (final Exception e) {
						Logger.getGlobal().catching(e);
					}
				}
			} catch (final Throwable e) {
				Logger.getGlobal().catching(e);
			}
			//DEBUG_MESSAGE.append("v ");
			/***
			 * if (GC_COUNTER++ % 1000 == 0) { final long nanoseconds = System.nanoTime(); try { for (final Index index :
			 * Cache.STORE.getIndexes()) index.resetCachedFiles(); System.gc(); if (!Settings.SQL_ENABLED && Settings.SQL_RETRY) {
			 * Settings.SQL_ENABLED = true; Logger.getGlobal().info("Executing clean memory task; turning SQL back on."); } } catch (final Throwable e) {
			 * Logger.getGlobal().info("Launcher", "Failed executing clean memory task..."); ServerLauncher.cleanMemory(Runtime.getRuntime().freeMemory()
			 * < Settings.MINIMUM_RAM_ALLOCATED); } Logger.getGlobal().info("Garbage collecting the server took: " + (System.nanoTime() -
			 * nanoseconds) + " nanoseconds!"); }
			 */

			// //
			// Logger.getGlobal().info(this, "TOTAL:
			// "+(Utils.currentTimeMillis()-currentTime));
			final long sleepTime = Settings.WORLD_CYCLE_TIME + currentTime - Utils.currentTimeMillis();
			//DEBUG_MESSAGE.append("w ");
			if (sleepTime <= 0) {
				//DEBUG_MESSAGE.append("CUT ABRUPTLY ");
				continue;
			}
			//DEBUG_MESSAGE.append("x ");
			try {
				//DEBUG_MESSAGE.append("y ");
				Thread.sleep(sleepTime);
				//DEBUG_MESSAGE.append("z ");
			} catch (final InterruptedException e) {
				Logger.getGlobal().catching(e);
			}
			//DEBUG_MESSAGE.setLength(0);
		}
	}

}
