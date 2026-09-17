package com.rs.game;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.rs.game.player.TemporaryAttributes.Key;
import com.rs.utils.Utils;

import lombok.Getter;

public class TemporaryModifiersManager {

    @Getter
    private final transient Map<Key, TemporaryModifier> modifiers;

    public TemporaryModifiersManager() {
        modifiers = new HashMap<Key, TemporaryModifier>();
    }

    public void process() {
        Iterator<Entry<Key, TemporaryModifier>> iter = modifiers.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Key, TemporaryModifier> e = iter.next();
            if (e != null && hasActiveModifier(e.getKey()))
                iter.remove();
        }
    }

    public void clear() {
        modifiers.clear();
    }

    public void applyModifier(Key key, long time, double modifier) {
        if (modifiers.containsKey(key)) {
            if (Math.abs(modifiers.get(key).getModifier()) > Math.abs(modifier))
                modifier = modifiers.get(key).getModifier();
        }
        modifiers.put(key, new TemporaryModifier(modifier, Utils.currentTimeMillis() + time));
    }

    public double getModifier(Key key) {
        return modifiers.containsKey(key) ? modifiers.get(key).getModifier() : 0;
    }

    public boolean hasActiveModifier(Key key) {
        return modifiers.containsKey(key) && modifiers.get(key).getTimer() >= Utils.currentTimeMillis();
    }

    public static class TemporaryModifier {
        @Getter
        private final double modifier;
        @Getter
        private final long timer;

        public TemporaryModifier(double modifier, long timer) {
            this.modifier = modifier;
            this.timer = timer;
        }
    }
}
