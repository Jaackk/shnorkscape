package com.rs.game.player.questing.framework.conditions;

/**
 * An integer implementation of the {@link InteractionKey} interface.
 * @author David O'Neill
 */
final class IntArrayInteractionKey implements InteractionKey<int[]> {

    private int[] key;

    @Override
    public void setKey(int[] ints) {
        this.key = ints;
    }

    @Override
    public int[] getKey() {
        return key;
    }

    @Override
    public boolean equals(Object other) {

        if(!(other instanceof IntArrayInteractionKey))
            return false;
        if(((IntArrayInteractionKey) other).getKey().length != key.length)
            return false;

        for(int i = 0; i < key.length; i++) {
            if(((IntArrayInteractionKey) other).getKey()[i] != key[i]){
                return false;
            }
        }

        return true;
    }
}
