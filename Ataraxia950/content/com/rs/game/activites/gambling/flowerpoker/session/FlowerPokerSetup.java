package com.rs.game.activites.gambling.flowerpoker.session;

import com.rs.game.activites.gambling.flowerpoker.FlowerPokerSession;
import com.rs.game.activites.gambling.flowerpoker.FlowerPokerTransaction;
import com.rs.game.player.Player;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FlowerPokerSetup implements FlowerPokerSession {
    private final Player player1;
    private final Player player2;

    public FlowerPokerSetup(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    private void inverseAction(BiConsumer<Player, Player> playerConsumer) {
        playerConsumer.accept(player1, player2);
        playerConsumer.accept(player2, player1);
    }

    private void forEachPlayer(Consumer<Player> playerConsumer) {
        playerConsumer.accept(player1);
        playerConsumer.accept(player2);
    }

    @Override
    public void start() {
        forEachPlayer(player -> player.setItemTransaction(new FlowerPokerTransaction(player)));
        inverseAction((p1, p2) -> p1.getItemTransaction().openTransaction(p2));
    }

    @Override
    public void abort() {
        // item handling upon abortion is handled via FlowerPokerTransaction class
        forEachPlayer(player -> player.setFlowerPokerSession(null));
    }
}
