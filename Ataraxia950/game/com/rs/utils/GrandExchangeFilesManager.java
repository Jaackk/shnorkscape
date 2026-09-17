package com.rs.utils;

import com.rs.game.player.content.grandExchange.Offer;
import com.rs.game.player.content.grandExchange.OfferHistory;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.HashMap;

public class GrandExchangeFilesManager {
	
	public static final void packOffersHistory(ArrayList<OfferHistory> history) {
		try {
			DataOutputStream out = new DataOutputStream(new FileOutputStream("data/grandExchange/geOffersTrack.data"));
			history.forEach(offer -> {
				try {
					out.writeShort(offer.getId());
					out.writeInt(offer.getQuantity());
					out.writeInt(offer.getPrice());
					out.writeByte(offer.isBought() ? 1 : 0);
				} catch (Exception e) {}
			});
			out.flush();
			out.close();
		} catch (Exception e) {}
	}
	
	public static final void packGEPrices(HashMap<Integer, Integer> prices) {
		try {
			DataOutputStream out = new DataOutputStream(new FileOutputStream("data/grandExchange/gePrices.data"));
			prices.forEach((k, v) -> {
				try {
					out.writeShort(k);
					out.writeInt(v);
				} catch (Exception e) {}
			});
			out.flush();
			out.close();
		} catch (Exception e) {}
	}
	
	public static final HashMap<Integer, Integer> loadGEPrices() {
		try {
			HashMap<Integer, Integer> prices;
			RandomAccessFile in = new RandomAccessFile("data/grandExchange/gePrices.data", "r");
			FileChannel channel = in.getChannel();
			ByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, channel.size());
			prices = new HashMap<Integer, Integer>(buffer.remaining() / 6);
			while (buffer.hasRemaining()) {
				int id = buffer.getShort() & 0xffff;
				int price = buffer.getInt();
				prices.put(id, price);
			}
			channel.close();
			in.close();
			return prices;
		} catch (Throwable e) {
			Logger.getGlobal().info("No GE prices found.");
		}
		return null;
	}
	
	public static final ArrayList<OfferHistory> loadOffersHistory() {
		try {
			ArrayList<OfferHistory> history;
			RandomAccessFile in = new RandomAccessFile("data/grandExchange/geOffersTrack.data", "r");
			FileChannel channel = in.getChannel();
			ByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, channel.size());
			history = new ArrayList<OfferHistory>(buffer.remaining() / 11);
			while (buffer.hasRemaining()) {
				int id = buffer.getShort() & 0xffff;
				int quantity = buffer.getInt();
				int price = buffer.getInt();
				boolean isBought = buffer.get() == 1;
				history.add(new OfferHistory(id, quantity, price, isBought));
			}
			channel.close();
			in.close();
			return history;
		} catch (Throwable e) {
			Logger.getGlobal().info("No offers' history file found.");
		}
		return null;
	}
	
	public static final HashMap<Long, Offer> loadOffers() {
		try {
			HashMap<Long, Offer> offers;
			RandomAccessFile in = new RandomAccessFile("data/grandExchange/geOffers.data", "r");
			FileChannel channel = in.getChannel();
			ByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, channel.size());
			offers = new HashMap<Long, Offer>(buffer.remaining() / 48);
			while (buffer.hasRemaining()) {
				long uid = buffer.getLong();
				int price = buffer.getInt();
				int totalPriceSoFar = buffer.getInt();
				int totalAmountSoFar = buffer.getInt();
				boolean cancelled = buffer.get() == 1;
				boolean buying = buffer.get() == 1;
				long time = buffer.getLong();
				int id = buffer.getShort() & 0xffff;
				int amount = buffer.getInt();
				int receivedItem = buffer.getShort() & 0xffff;
				int receivedItemAmount = buffer.getInt();
				int leftoverCoinsAmount = buffer.getInt();
				offers.put(uid, new Offer(price, totalPriceSoFar, totalAmountSoFar, cancelled, buying, time, id, amount, receivedItem, receivedItemAmount, leftoverCoinsAmount));
			}
			channel.close();
			in.close();
			return offers;
		} catch (Throwable e) {
			Logger.getGlobal().info("No offers file found.");
		}
		return null;
	}
}
