package com.rs.game.activites.gambling.flowerpoker;

import lombok.Getter;

import java.util.Arrays;

public class FlowerPokerWinValidator {
    private final Flower[] playerOnesFlowers;
    private final Flower[] playerTwosFlowers;

    public FlowerPokerWinValidator(Flower[] playerOnesFlowers, Flower[] playerTwosFlowers) {
        this.playerOnesFlowers = playerOnesFlowers;
        this.playerTwosFlowers = playerTwosFlowers;
    }

    public WinResponse determineWinner() {
        WinType playerOneWinType = determineWinType(playerOnesFlowers);
        WinType playerTwoWinType = determineWinType(playerTwosFlowers);

        if (playerOneWinType.ordinal() > playerTwoWinType.ordinal()) {
            return new WinResponse(WinResponseType.PLAYER_ONE, playerOneWinType, playerTwoWinType);
        } else if (playerOneWinType.ordinal() < playerTwoWinType.ordinal()) {
            return new WinResponse(WinResponseType.PLAYER_TWO, playerTwoWinType, playerOneWinType);
        }
        return new WinResponse(WinResponseType.DRAW, playerOneWinType, playerTwoWinType);
    }

    private WinType determineWinType(Flower[] flowers) {
        Arrays.sort(flowers);
        if (isFiveOfAKind(flowers)) {
            return WinType.FIVE_OF_A_KIND;
        } else if (isFourOfAKind(flowers)) {
            return WinType.FOUR_OF_A_KIND;
        } else if (isFullHouse(flowers)) {
            return WinType.FULL_HOUSE;
        } else if (isThreeOfAKind(flowers)) {
            return WinType.THREE_OF_A_KIND;
        } else if (isTwoPair(flowers)) {
            return WinType.TWO_PAIR;
        } else if (isTwoOfAKind(flowers)) {
            return WinType.TWO_OF_A_KIND;
        }
        return WinType.ONE_OF_A_KIND;
    }

    private boolean isTwoOfAKind(Flower[] flowers) {
        for (int i = 0; i < flowers.length - 1; i++) {
            if (flowers[i] == flowers[i + 1]) {
                return true;
            }
        }
        return false;
    }

    private boolean isTwoPair(Flower[] flowers) {
        boolean pairFoundBefore = false;
        for (int i = 0; i < flowers.length - 1; i++) {
            if (flowers[i] == flowers[i + 1]) {
                if (!pairFoundBefore) {
                    pairFoundBefore = true;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isThreeOfAKind(Flower[] flowers) {
        Flower flowerNow = flowers[0];
        int consistencyCount = 1;
        for (int i = 1; i < flowers.length; i++) {
            if (consistencyCount == 3) {
                return true;
            }
            if (flowerNow != flowers[i]) {
                flowerNow = flowers[i];
                consistencyCount = 1;
            } else {
                consistencyCount++;
            }
        }
        return consistencyCount == 3;
    }

    private boolean isFullHouse(Flower[] flowers) {
        boolean firstPair = flowers[0] == flowers[1] && flowers[1] != flowers[2];
        if (firstPair) {
            Flower constant = flowers[2];
            for (int i = 3; i < flowers.length; i++) {
                if (flowers[i] != constant) {
                    return false;
                }
            }
        } else {
            boolean lastPair = flowers[3] == flowers[4] && flowers[2] != flowers[3];
            if (!lastPair) {
                return false;
            }
            Flower constant = flowers[0];
            for (int i = 0; i <= flowers.length - 3; i++) {
                if (flowers[i] != constant) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isFourOfAKind(Flower[] flowers) {
        Flower constant = flowers[0];
        int startingIndex = 1;
        int endingIndex = flowers.length - 2;
        if (constant != flowers[1]) {
            constant = flowers[1];
            startingIndex = 2;
            endingIndex = flowers.length - 1;
        }
        for (int i = startingIndex; i <= endingIndex; i++) {
            if (flowers[i] != constant) {
                return false;
            }
        }
        return true;
    }

    private boolean isFiveOfAKind(Flower[] flowers) {
        Flower constant = flowers[0];
        for (int i = 1; i < flowers.length; i++) {
            if (flowers[i] != constant) {
                return false;
            }
        }
        return true;
    }

    public class WinResponse {
        @Getter
        private final WinResponseType winResponseType;
        @Getter
        private final WinType winnerWinType;
        @Getter
        private final WinType loserWinType;

        WinResponse(WinResponseType winResponseType, WinType winnerWinType, WinType loserWinType) {
            this.winResponseType = winResponseType;
            this.winnerWinType = winnerWinType;
            this.loserWinType = loserWinType;
        }
    }

    public enum WinResponseType {
        PLAYER_ONE, PLAYER_TWO, DRAW
    }

    private enum WinType {
        ONE_OF_A_KIND,
        TWO_OF_A_KIND,
        TWO_PAIR,
        THREE_OF_A_KIND,
        FULL_HOUSE,
        FOUR_OF_A_KIND,
        FIVE_OF_A_KIND,
        ;

        @Override
        public String toString() {
            String name = name().toLowerCase().replace("_", " ");
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
    }
}
