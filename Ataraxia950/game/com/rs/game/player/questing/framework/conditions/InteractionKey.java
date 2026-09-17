package com.rs.game.player.questing.framework.conditions;

/**
 * Wrapper interface for interaction keys. This interface
 * contains static methods for generating appropriately
 * typed keys.
 *
 * @author David O'Neill
 */
public interface InteractionKey<K> {

    void setKey(K k);

    K getKey();

    /**
     * Generates an {@code Integer} typed interaction key.
     * @param integer the value of the integer to be wrapped in an interaction key
     * @return a new {@code InteractionKey} containing the supplied key value.
     */
    static InteractionKey<Integer> intInteractionKey(Integer integer) {

        IntegerInteractionKey key = new IntegerInteractionKey();
        key.setKey(integer);
        return key;

    }

    /**
     * Generates an {@code int[]} typed interaction key.
     * @param intArray the int array to be wrapped in an interaction key
     * @return a new {@code InteractionKey} containing the supplied key value.
     */
    static InteractionKey<int[]> intArrayInteractionKey(int[] intArray) {

        IntArrayInteractionKey key = new IntArrayInteractionKey();
        key.setKey(intArray);
        return key;

    }


}
