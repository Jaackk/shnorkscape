package com.rs.utils.data.parsers.items.pojos;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
public class ItemDisassembleData {
    private int materialCount;
    private final int requiredQuantity;
    private final double xp;
    private final double junkChance;
    private Component[] components;

    public int getMaterialCount() {
        return materialCount;
    }

    public int getRequiredQuantity() {
        return requiredQuantity;
    }

    public double getXp() {
        return xp;
    }

    public double getJunkChance() {
        return junkChance;
    }

    public Component[] getComponents() {
        return components;
    }

    public Component[] getComponentsCopy() {
        if (components == null)
            return null;
        Component[] copy = new Component[components.length];
        for (int i = 0; i < copy.length; i++)
            copy[i] = components[i] == null ? null : components[i].getCopy();
        return copy;
    }
    
    public List<Component> getComponentsCopyAsList() {
        if (components == null)
            return null;
        List<Component> copy = new ArrayList<Component>();
        for (int i = 0; i < components.length; i++)
            copy.add(components[i].getCopy());
        return copy;
    }
    
    public void setMaterialCount(int materialCount) {
        this.materialCount = materialCount;
    }

    public void setComponents(Component[] components) {
        this.components = components;
    }

    @Data
    @AllArgsConstructor
    public static class Component {
        private String name;
        private int id;
        private int amount;
        private double chance;

        public Component getCopy() {
            return new Component(name, id, amount, chance);
        }
    }
}
