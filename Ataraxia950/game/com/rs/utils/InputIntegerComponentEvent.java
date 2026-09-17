package com.rs.utils;

public abstract class InputIntegerComponentEvent extends InputIntegerEvent {

    @Override
    public void setInteger(int i) {
        i--;
        super.setInteger(i);
    }
}
