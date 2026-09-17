package com.rs.game.player.content.jujupotions;

import com.google.common.collect.ImmutableMap;
import com.rs.game.Graphics;
import com.rs.game.player.Player;
import com.rs.game.player.content.Pots.Effects;
import com.rs.game.player.content.interfaces.potiontimers.PotionTimer;
import com.rs.game.player.content.interfaces.potiontimers.PotionTimerInterface;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import lombok.Setter;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class JujuPotionManager implements Serializable {

    private static final class PotionTimerTask extends WorldTask {

        private final Player player;
        private final Set<Map.Entry<Effects, LocalDateTime>> allActive;
        private final EnumMap<Effects, Long> secondsMap = new EnumMap<>(Effects.class);

        public PotionTimerTask(Player player) {
            this.player = player;
            allActive = player.jujuPotions.active.entrySet();
        }

        @Override
        public void run() {
            Iterator<Map.Entry<Effects, LocalDateTime>> iter = allActive.iterator();
            while (iter.hasNext()) {
                Map.Entry<Effects, LocalDateTime> entry = iter.next();
                Effects potion = entry.getKey();
                LocalDateTime finished = entry.getValue();
                if (LocalDateTime.now().isAfter(finished)) {
                    player.sendMessage(Colors.RED + "Your " + potion.name().toLowerCase().replace("_", " ") + " potion has expired!");
                    secondsMap.remove(potion);
                    iter.remove();
                    continue;
                }
                Long secondsBefore = secondsMap.get(potion);
                long secondsNow = TimeUnit.SECONDS.convert(Duration.between(LocalDateTime.now(), finished).toNanos(), TimeUnit.NANOSECONDS);
                if (secondsBefore != null && secondsBefore > 60 && secondsNow <= 60) {
                    player.sendMessage(Colors.RED + "Your " + potion.name().toLowerCase().replace("_", " ") + " potion will expire in 1 minute!");
                }
                secondsMap.put(potion, secondsNow);
            }
            if (allActive.isEmpty()) {
                stop();
            }
        }
    }

    private static final long serialVersionUID = -5802353799621995456L;
    private final EnumMap<Effects, LocalDateTime> active = new EnumMap<>(Effects.class);
    public static final ImmutableMap<String, Effects> JUJU_EFFECTS;
    public static final ImmutableMap<Effects, PotionTimer> JUJU_TIMERS;
    @Setter
    private transient Player player;
    private transient PotionTimerTask timerTask;

    static {
        Map<String, Effects> effects = new HashMap<>();
        Map<Effects, PotionTimer> timers = new HashMap<>();
        for (Effects effect : Effects.values()) {
            if (effect.name().endsWith("JUJU")) {
                effects.put(effect.name(), effect);
                timers.put(effect, PotionTimer.valueOf(effect.name()));
            }
        }
        JUJU_EFFECTS = ImmutableMap.copyOf(effects);
        JUJU_TIMERS = ImmutableMap.copyOf(timers);
    }

    public void startTimerTask() {
        if (active.isEmpty())
            return;
        if (timerTask == null || timerTask.isCancelled()) {
            timerTask = new PotionTimerTask(player);
            WorldTasksManager.schedule(timerTask, 1, 1);
        }
    }

    public void onLogin() {
        Iterator<Map.Entry<Effects, LocalDateTime>> iter = active.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Effects, LocalDateTime> entry = iter.next();
            Effects potion = entry.getKey();
            LocalDateTime finished = entry.getValue();
            if (LocalDateTime.now().isAfter(finished)) {
                iter.remove();
                continue;
            }
//            PotionTimerInterface.addTimer(player, JUJU_TIMERS.get(potion));
        }
        startTimerTask();
    }

    private boolean isActive(Effects effect, LocalDateTime finished) {
        if (finished == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isEqual(finished) || now.isAfter(finished)) {
            active.remove(effect);
            return false;
        }
        return true;
    }

    public boolean isActive(Effects effect) {
        return isActive(effect, active.get(effect));
    }

    public void addActive(Effects effect, int hours, int minutes) {
        checkState(hours >= 0, "Hours must be positive!");
        checkState(minutes >= 0, "Minutes must be positive!");

        active.put(effect, LocalDateTime.now().plusHours(hours).plusMinutes(minutes));
//        PotionTimerInterface.addTimer(player, JUJU_TIMERS.get(effect));
        startTimerTask();
    }

    public String getRemainingTime(String name) {
        Effects effect = JUJU_EFFECTS.get(name);
        if (effect == null) {
            return "";
        }
        LocalDateTime finished = active.get(effect);
        if (!isActive(effect, finished)) {
            return "";
        }
        Duration diff = Duration.between(LocalDateTime.now(), finished);
        long minutesPart = diff.toMinutes();
        long secondsPart = diff.minusMinutes(minutesPart).getSeconds();
        if (minutesPart > 0) {
            return minutesPart + "m";
        } else {
            return secondsPart + "s";
        }
    }

    public void sendWoodSpirit() {
        if (player.woodSpirit)
            return;
        player.sendMessage("Through the power of juju, a wood spirit aids you!");
        player.woodSpirit = true;
        WorldTasksManager.schedule(new WorldTask() {
            int ticks = 50;

            @Override
            public void run() {
                if (!player.woodSpirit || player.hasFinished() || --ticks < 0) {
                    player.woodSpirit = false;
                    stop();
                    return;
                }
                player.setNextGraphics(new Graphics(ThreadLocalRandom.current().nextBoolean() ? 1678 : 1679));
            }
        }, 2, 2);
    }
}