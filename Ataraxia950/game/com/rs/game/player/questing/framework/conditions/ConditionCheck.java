package com.rs.game.player.questing.framework.conditions;

import com.rs.game.player.Player;

import java.util.function.Function;

/**
 * A functional interface wrapper around the {@link Function} interface.
 * Provides {@link ConditionCheck#determine(Player)} as a wrapper around
 * {@link Function#apply(Object)} to explicity return a {@code Boolean}.
 * @author David O'Neill
 * @deprecated Made use of {@link java.util.function.Predicate}
 */
@FunctionalInterface
@Deprecated
public interface ConditionCheck<K extends Player, V extends Boolean> extends Function<K, V> {

    default V determine(K k) {
        return apply(k);
    }

}
