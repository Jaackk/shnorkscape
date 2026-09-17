package com.rs.utils.data.parsers.npcs.pojos;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.content.eds.EliteDungeonsConstants;
import com.rs.game.player.controllers.Dungeoneering;
import com.rs.utils.data.parsers.items.pojos.ItemDisassembleData.Component;

public class NPCDrop {

    private int itemId, minAmount, maxAmount;
    private double rate;
    // private boolean rare;

    public NPCDrop(int itemId, double rate, int minAmount, int maxAmount/* , boolean rare */) {
        this.itemId = itemId;
        this.rate = rate;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        // this.rare = rare;
    }

    public NPCDrop(int itemId, double rate, int minAmount) {
        this.itemId = itemId;
        this.rate = rate;
        this.minAmount = minAmount;
        this.maxAmount = minAmount;
        // this.rare = false;
    }

    public static NPCDrop create(int itemId, double rate, int minAmount, int maxAmount/* , boolean rare */) {
        return new NPCDrop((short) itemId, rate, minAmount, maxAmount/* , rare */);
    }

    public int getExtraAmount() {
        return maxAmount - minAmount;
    }

    public int getItemId() {
        return itemId;
    }


    public void setItemId(short itemId) {
        this.itemId = itemId;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(int amount) {
        this.maxAmount = amount;
    }

    public int getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(int amount) {
        this.minAmount = amount;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    @Override
    public String toString() {
        return "NPCDrop [item=" + ItemDefinitions.getItemDefinitions(itemId).name + "]";
    }

    /*
     * public boolean isFromRareTable() { return rare; }
     */
    public static void main(String[] args) throws IOException {
        Cache.init();
      Map<NPCDrop, Integer> appear = new LinkedHashMap<NPCDrop, Integer>();
      int totalTrys = 10000000;
      NPCDrop[] drops = EliteDungeonsConstants.mobDrops;





        for (int i = 0; i < totalTrys; i++) {
          NPCDrop s = selectRandomNPCDrop(drops);
          if (appear.containsKey(s)) {
              appear.put(s, appear.get(s) + 1);
          } else {
              appear.put(s, 1);
          }
      }
        System.out.println("appearance:");
        for (Entry<NPCDrop, Integer> e : appear.entrySet()) {
            System.out.println(e.getKey().getItemId() + " appeared " + e.getValue() + " times, perc=" + (((double) e.getValue() / (double) totalTrys) * 100.00) + "% (1/"+(1.00 / ((double) (e.getValue()) / (double) totalTrys))+")");
        }
    }

    public static NPCDrop selectRandomNPCDrop(NPCDrop[] drops) {
        double[] bins = generateBins(drops);
        double r = new Random().nextDouble() * bins[bins.length - 1];
        for (int i = 0; i < bins.length; i++) {
            if (r <= bins[i])
                return drops[i];
        }
        return null;
    }

    public static double[] generateBins(NPCDrop[] drops) {
        double[] arr = new double[drops.length];
        for (int i = 0; i < drops.length; i++) {
            if (i == 0)
                arr[i] = drops[i].getRate() / 100;
            else
                arr[i] = arr[i - 1] + (drops[i].getRate() / 100);
        }
        return arr;
    }

}