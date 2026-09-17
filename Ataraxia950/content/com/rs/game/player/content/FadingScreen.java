package com.rs.game.player.content;

import com.rs.cores.CoresManager;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.util.concurrent.TimeUnit;

public final class FadingScreen {

	public static void fade(final Player player, long fadeTime, final Runnable event) {
		unfade(player, fade(player, fadeTime), event);
	}

	public static void fade(final Player player, final Runnable event) {
		unfade(player, fade(player), event);
	}

	public static void unfade(final Player player, long startTime, final Runnable event) {
		unfade(player, 2500, startTime, event);
	}

	public static void unfade(final Player player, long endTime, long startTime, final Runnable event) {
		player.getTemporaryAttributtes().put("fading", true);
		long leftTime = endTime - (Utils.currentTimeMillis() - startTime);
		if (leftTime > 0) {
			CoresManager.getServiceProvider().executeWithDelay(new Runnable() {
				@Override
				public void run() {
					try {
						unfade(player, event);
					} catch (Throwable e) {
						Logger.getGlobal().catching(e);
					}
				}
			}, leftTime, TimeUnit.MILLISECONDS);
		} else
			unfade(player, event);
	}

	public static void unfade(final Player player, Runnable event) {
		player.getTemporaryAttributtes().put("fading", true);
		event.run();
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				player.getInterfaceManager().sendFadingInterface(170);
				CoresManager.getServiceProvider().executeWithDelay(new Runnable() {

					@Override
					public void run() {
						try {
							player.getInterfaceManager().closeFadingInterface();
							player.getTemporaryAttributtes().remove("fading");
						} catch (Throwable e) {
							Logger.getGlobal().catching(e);
						}
					}
					
				}, 2, TimeUnit.MILLISECONDS);
			}

		});
	}

	public static long fade(Player player, long fadeTime) {
		player.getInterfaceManager().sendFadingInterface(120);
		return Utils.currentTimeMillis() + fadeTime;
	}

	public static long fade(Player player) {
		return fade(player, 0);
	}
}