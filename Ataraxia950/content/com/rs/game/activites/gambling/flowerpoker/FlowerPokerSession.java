package com.rs.game.activites.gambling.flowerpoker;

public interface FlowerPokerSession {
    String FLOWER_POKER_REQUEST_TARGET_KEY = "flower poker request target";
    String PLAYING_FLOWER_POKER_KEY = "playing flower poker";
    String BETTING_FLOWER_POKER_KEY = "betting flower poker";
    void start();
    void abort();
}
