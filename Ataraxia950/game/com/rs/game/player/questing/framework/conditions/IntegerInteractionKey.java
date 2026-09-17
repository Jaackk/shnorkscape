package com.rs.game.player.questing.framework.conditions;

/**
 * An integer array implementation of the {@link InteractionKey} interface.
 * @author David O'Neill
 */
final class IntegerInteractionKey implements InteractionKey<Integer> {

    private Integer key;

    @Override
    public void setKey(Integer integer) {
        this.key = integer;
    }

    @Override
    public Integer getKey() {
        return key;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof IntegerInteractionKey) &&
                ((IntegerInteractionKey) other).getKey().intValue() == key.intValue();
    }
}
