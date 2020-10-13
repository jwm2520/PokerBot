import java.util.*;

public class EquityAnalyzer {

    private Table table;

    public EquityAnalyzer(Table table) {
        this.table = table;
    }

    /**
     * Determine each player's equity in the hand, based on the number of cards dealt so far
     */
    public void determineEquity() {
        Board board = table.getBoard();
        switch (board.getSize()) {
            // Who is leading pre flop?
            case 0:
                break;

            // Who is leading after the flop?
            case 3:
                break;

            // Who is leading after the turn?
            case 4:
                break;

            // Who won?
            case 5:
                determineShowdownWinner();
                break;

            default:
                // ERROR (invalid size)
                break;
        }
    }

    private void determineShowdownWinner() {
        for (Player p : this.table.getPlayers()) {
            getBestHand(this.table.getBoard(), p);
            System.out.println("Player " + p.getName() + " has " + p.getHandRanking() + ": " + p.getBestPossibleHand());
        }
    }

    /**
     * Determine the best hand a player can make, given a board, and the player.
     * @param board The board
     * @param p The player (allows access to their hole cards)
     */
    private void getBestHand(Board board, Player p) {
        ArrayList<Card> allCards = new ArrayList<>();
        allCards.add(p.getHand().getCard1());
        allCards.add(p.getHand().getCard2());
        allCards.addAll(board.getBoard());

        boolean royalFlush = checkRoyalFlush(allCards);
        if (royalFlush) {
            p.setHandRanking(HandRanking.ROYAL_FLUSH);
            return;
        }
        boolean straightFlush = checkStraightFlush(allCards);
        if (straightFlush) {
            p.setHandRanking(HandRanking.STRAIGHT_FLUSH);
            return;
        }
        boolean quads = checkQuads(allCards);
        if (quads) {
            p.setHandRanking(HandRanking.QUADS);
            p.setBestPossibleHand(quadsBestHand(allCards));
            return;
        }
        boolean fullHouse = checkFullHouse(allCards);
        if (fullHouse) {
            p.setHandRanking(HandRanking.FULL_HOUSE);
            p.setBestPossibleHand(fullHouseBestHand(allCards));
            return;
        }
        boolean flush = checkFlush(allCards);
        if (flush) {
            p.setHandRanking(HandRanking.FLUSH);
            p.setBestPossibleHand(flushBestHand(allCards));
            return;
        }
        boolean straight = checkStraight(allCards);
        if (straight) {
            p.setHandRanking(HandRanking.STRAIGHT);
            return;
        }
        boolean trips = checkTrips(allCards);
        if (trips) {
            p.setHandRanking(HandRanking.TRIPS);
            p.setBestPossibleHand(tripsBestHand(allCards));
            return;
        }
        boolean twoPair = checkTwoPair(allCards);
        if (twoPair) {
            p.setHandRanking(HandRanking.TWO_PAIR);
            p.setBestPossibleHand(twoPairBestHand(allCards));
            return;
        }
        boolean pair = checkPair(allCards);
        if (pair) {
            p.setHandRanking(HandRanking.ONE_PAIR);
            p.setBestPossibleHand(onePairBestHand(allCards));
            return;
        }
        p.setHandRanking(HandRanking.HIGH_CARD);
        p.setBestPossibleHand(highCardBestHand(allCards));
    }

    /**
     * Creates a map that holds a count of the numerical frequency of the list of cards (by value)
     * @param cards The list of 7 cards
     * @return The map
     */
    private Map<Value, Integer> mapCardsToValue(ArrayList<Card> cards) {
        Map<Value, Integer> valueMap = new HashMap<>();
        cards.sort(new SortByValueDescending());
        int valueCount = 0;
        for (Card c : cards) {
            if (valueMap.containsKey(c.getValue())) {
                valueCount = valueMap.get(c.getValue()) + 1; }
            else {
                valueCount = 1;
            }
            valueMap.put(c.getValue(), valueCount);
        }
        return valueMap;
    }

    /**
     * Creates a map that holds a count of the suit frequency of the list of cards
     * @param cards The list of 7 cards
     * @return The map
     */
    private Map<Suit, Integer> mapCardsToSuit(ArrayList<Card> cards) {
        Map<Suit, Integer> suitMap = new HashMap<Suit, Integer>();
        cards.sort(new SortByValueDescending());
        int suitCount;
        for (Card c : cards) {
            if (suitMap.containsKey(c.getSuit())) {
                suitCount = suitMap.get(c.getSuit()) + 1; }
            else {
                suitCount = 1;
            }
            suitMap.put(c.getSuit(), suitCount);
        }
        return suitMap;
    }

    /**
     * Given a list of 7 cards, determine if they can create a flush
     * @param cards The list of 7 cards
     * @return True if a flush is possible, false if not
     */
    private boolean checkFlush(ArrayList<Card> cards) {
        Map<Suit, Integer> suitMap = mapCardsToSuit(cards);
        cards.sort(new SortByValueDescending()); //sort the cards by value (ace is high)

        for (Suit s : suitMap.keySet()) {
            if (suitMap.get(s) >= 5) {
                return true;
            }
        }
        return false;
    }

    /**
     * If a flush is possible in 'cards', determine which 5 cards make the best flush
     * @param cards The list of 7 cards
     * @return The 5 cards to make the best possible flush hand
     */
    private ArrayList<Card> flushBestHand(ArrayList<Card> cards) {
        Map<Suit, Integer> suitMap = mapCardsToSuit(cards);
        cards.sort(new SortByValueDescending()); //sort the cards by value (ace is high)
        ArrayList<Card> bestFiveCards = new ArrayList<>(); //create array to hold the final hand
        Suit flushSuit = null; //does not matter what this is initialized to
        for (Suit s : suitMap.keySet()) {
            if (suitMap.get(s) >= 5) {
                flushSuit = s;
            }
        }

        for (Card c : cards) {
            if (c.getSuit() == flushSuit && bestFiveCards.size() < 5) {
                bestFiveCards.add(c);
            }
        }
        return bestFiveCards;
    }

    /**
     * If the best hand is high cardd, determine which 5 cards make the best hand
     * @param cards The list of 7 cards
     * @return The 5 cards to make the best possible high card hand
     */
    private ArrayList<Card> highCardBestHand(ArrayList<Card> cards) {
        cards.sort(new SortByValueDescending()); //sort the cards by value, high to low (ace is high)
        ArrayList<Card> bestFiveCards = new ArrayList<>(); //create array to hold the final hand

        while (bestFiveCards.size() < 5) {
            bestFiveCards.add(cards.get(0));
            cards.remove(0);
        }
        return bestFiveCards;
    }

    /**
     * Given a list of 7 cards, determine if they can create a pair
     * @param cards The list of 7 cards
     * @return True if a pair is possible, false if not
     */
    private boolean checkPair(ArrayList<Card> cards) {
        boolean pairFlag = false;
        Map<Value, Integer> valueMap = mapCardsToValue(cards);
        cards.sort(new SortByValueDescending()); //sort the cards by value (ace is high)

        for (Value v : valueMap.keySet()) {
            // if the map shows 2 of the same card, then you have a pair!
            if (valueMap.get(v) == 2) {
                pairFlag = true;
            }
        }
        return pairFlag;
    }

    /**
     * If the best hand is one pair, determine which 5 cards make the best hand
     * @param cards The list of 7 cards
     * @return The 5 cards to make the best possible one pair hand
     */
    private ArrayList<Card> onePairBestHand(ArrayList<Card> cards) {
        // If the player's hand is 1 pair, create the best hand
        Value pairValue = null; //doesn't matter what this is
        Map<Value, Integer> valueMap = mapCardsToValue(cards); //create counts of card values
        cards.sort(new SortByValueDescending()); //sort the cards by value, high to low (ace is high)
        ArrayList<Card> bestFiveCards = new ArrayList<>(); //create array to hold the final hand
        for (Value v : valueMap.keySet()) { //
            if (valueMap.get(v) == 2) {
                pairValue = v;
            }
        }

        for (Card c : cards) { // iterate through all cards (highest to lowest)
            if (c.getValue() == pairValue) { // get a card that makes up the pair
                bestFiveCards.add(c); //add that card to the hand
            }
        }

        cards.removeAll(bestFiveCards); // remove all the cards added to the best hand from the remaining cards
        while (bestFiveCards.size() < 5) { //go through the sorted card values and add them until there are 5 cards in the final hand
            bestFiveCards.add(cards.get(0)); //add the last card in the sorted array to the best hand
            cards.remove(cards.get(0)); //remove the added card from the list
        }
        return bestFiveCards;
    }

    /**
     * Given a list of 7 cards, determine if they can create 2 pair
     * @param cards The list of 7 cards
     * @return True if 2 pair is possible, false if not
     */
    private boolean checkTwoPair(ArrayList<Card> cards) {
        Map<Value, Integer> valueMap = mapCardsToValue(cards);
        boolean onePair = false;
        for (Value v : Value.values()) { //requires this to be sorted in descending order
            if (valueMap.containsKey(v)) {
                if (valueMap.get(v) == 2) {
                    if (onePair) {
                        return true;
                    } else {
                        onePair = true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * If the best hand is two pair, determine which 5 cards make the best hand
     * @param cards The list of 7 cards
     * @return The 5 cards to make the best possible two pair hand
     */
    private ArrayList<Card> twoPairBestHand(ArrayList<Card> cards) {
        // If the player's hand is 2 pair, create the best hand
        Value biggerPair = null; //doesn't matter what this is
        Value smallerPair = null;
        boolean foundBiggerPair = false; //did I find the first pair?
        Map<Value, Integer> valueMap = mapCardsToValue(cards); //create counts of card values
        cards.sort(new SortByValueDescending()); //sort the cards by value (high to low)
        ArrayList<Card> bestFiveCards = new ArrayList<>(); //create array to hold the final hand
        for (Value v : valueMap.keySet()) {
            if (valueMap.get(v) == 2) {
                if (foundBiggerPair) {
                    smallerPair = v;
                    break;
                }
                else {
                    biggerPair = v;
                    foundBiggerPair = true;
                }
            }
        }

        for (Card c : cards) { // iterate through all cards (high to low)
            if (c.getValue() == smallerPair || c.getValue() == biggerPair) { // get a card that makes up either pair
                bestFiveCards.add(c); //add that card to the hand
            }
        }

        cards.removeAll(bestFiveCards); // remove all the cards added to the best hand from the remaining cards
        while (bestFiveCards.size() < 5) { //go through the sorted card values and add them until there are 5 cards in the final hand
            bestFiveCards.add(cards.get(0)); //add the last card in the sorted array to the best hand
            cards.remove(cards.get(0)); //remove the added card from the list
        }
        return bestFiveCards;
    }

    /**
     * Given a list of 7 cards, determine if they can create trips
     * @param cards The list of 7 cards
     * @return True if trips is possible, false if not
     */
    private boolean checkTrips(ArrayList<Card> cards) {
        Map<Value, Integer> valueMap = mapCardsToValue(cards);
        for (Value v : valueMap.keySet()) {
            if (valueMap.get(v) == 3) {return true; }
        }
        return false;
    }

    /**
     * If the best hand is trips, determine which 5 cards make the best hand
     * @param cards The list of 7 cards
     * @return The 5 cards to make the best possible trips hand
     */
    private ArrayList<Card> tripsBestHand(ArrayList<Card> cards) {
        // If the player's hand is trips, create the best hand
        Value tripsValue = null; //doesn't matter what this is
        Map<Value, Integer> valueMap = mapCardsToValue(cards); //create counts of card values
        cards.sort(new SortByValueDescending()); //sort the cards by value (ace is high)
        ArrayList<Card> bestFiveCards = new ArrayList<>(); //create array to hold the final hand
        for (Value v : valueMap.keySet()) { //
            if (valueMap.get(v) == 3) {
                tripsValue = v;
                break; // break because if there are 2 sets of trips, this will ignore the lower value
            }
        }

        for (Card c : cards) { // iterate through all cards (high to low)
            if (c.getValue() == tripsValue) { // get a card that makes up the trips
                bestFiveCards.add(c); //add that card to the hand
            }
        }

        cards.removeAll(bestFiveCards); // remove all the cards added to the best hand from the remaining cards
        while (bestFiveCards.size() < 5) { //go through the sorted card values and add them until there are 5 cards in the final hand
            bestFiveCards.add(cards.get(0)); //add the last card in the sorted array to the best hand
            cards.remove(cards.get(0)); //remove the added card from the list
        }
        return bestFiveCards;
    }

    /**
     * Given a list of 7 cards, determine if they can create quads
     * @param cards The list of 7 cards
     * @return True if quads is possible, false if not
     */
    private boolean checkQuads(ArrayList<Card> cards) {
        Map<Value, Integer> valueMap = mapCardsToValue(cards);
        for (Value v : valueMap.keySet()) {
            if (valueMap.get(v) == 4) {return true; }
        }
        return false;
    }

    /**
     * If the best hand is quads, determine which 5 cards make the best hand
     * @param cards The list of 7 cards
     * @return The 5 cards to make the best possible quads hand
     */
    private ArrayList<Card> quadsBestHand(ArrayList<Card> cards) {
        // If the player's hand is quads, create the best hand
        Value quadsValue = null; //doesn't matter what this is
        Map<Value, Integer> valueMap = mapCardsToValue(cards); //create counts of card values
        cards.sort(new SortByValueDescending()); //sort the cards by value (high to low)
        ArrayList<Card> bestFiveCards = new ArrayList<>(); //create array to hold the final hand
        for (Value v : valueMap.keySet()) { //
            if (valueMap.get(v) == 4) {
                quadsValue = v;
                break;
            }
        }

        for (Card c : cards) { // iterate through all cards
            if (c.getValue() == quadsValue) { // get a card that makes up the quads
                bestFiveCards.add(c); //add that card to the hand
            }
        }

        cards.removeAll(bestFiveCards); // remove all the cards added to the best hand from the remaining cards
        while (bestFiveCards.size() < 5) { //go through the sorted card values and add them until there are 5 cards in the final hand
            bestFiveCards.add(cards.get(cards.size()-1)); //add the last card in the sorted array to the best hand
            cards.remove(cards.get(cards.size()-1)); //remove the added card from the list
        }
        return bestFiveCards;
    }

    /**
     * Given a list of 7 cards, determine if they can create a full house
     * @param cards The list of 7 cards
     * @return True if a full house is possible, false if not
     */
    private boolean checkFullHouse(ArrayList<Card> cards) {
        Map<Value, Integer> valueMap = mapCardsToValue(cards);
        boolean trips = false;
        boolean pair = false;

        for (Value v : Value.values()) { // get list of values already sorted from high to low
            if (valueMap.containsKey(v)) { // make sure the key exists in the map
                if (valueMap.get(v) == 3 && !trips) {
                    trips = true;
                }
                else if (valueMap.get(v) == 2 && !pair) {
                    pair = true;
                }
                else if (valueMap.get(v) >= 2 && trips && !pair) {
                    pair = true;
                }
            }
        }
        return pair && trips;
    }

    /**
     * If the best hand is a full house, determine which 5 cards make the best hand
     * @param cards The list of 7 cards
     * @return The 5 cards to make the best possible full house hand
     */
    private ArrayList<Card> fullHouseBestHand(ArrayList<Card> cards) {
        // If the player's hand is a full house, create the best hand
        Value tripsValue = null; //doesn't matter what this is
        Value pairValue = null;
        boolean trips = false;
        boolean pair = false;
        Map<Value, Integer> valueMap = mapCardsToValue(cards); //create counts of card values
        cards.sort(new SortByValueDescending()); //sort the cards by value (high to low)
        ArrayList<Card> bestFiveCards = new ArrayList<>(); //create array to hold the final hand

        for (Value v : Value.values()) { // get list of values already sorted from high to low
            if (valueMap.containsKey(v)) { // make sure the key exists in the map
                if (valueMap.get(v) == 3 && !trips) {
                    tripsValue = v;
                    trips = true;
                }
                else if (valueMap.get(v) == 2 && !pair) {
                    pairValue = v;
                    pair = true;
                }
                else if (valueMap.get(v) >= 2 && trips && !pair) {
                    pairValue = v;
                    pair = true;
                }
            }
        }

        for (Card c : cards) { // iterate through all cards to get trips value
            if (c.getValue() == tripsValue) { // get a card that makes up the trips
                bestFiveCards.add(c); //add that card to the hand
            }
        }
        for (Card c : cards) { // iterate through all cards to get pair value
            if (c.getValue() == pairValue && bestFiveCards.size() < 5) { // get a card that makes up the pair
                bestFiveCards.add(c); //add that card to the hand
            }
        }

        cards.removeAll(bestFiveCards); // remove all the cards added to the best hand from the remaining cards
        return bestFiveCards;
    }

    /**
     * Given a list of 7 cards, determine if they can create a straight
     * @param cards The list of 7 cards
     * @return True if a straight is possible, false if not
     */
    private boolean checkStraight(ArrayList<Card> cards) {
        Map<Value, Integer> valueMap = new HashMap<>();
        Integer valueCount;
        for (Card c : cards) {
            if (valueMap.containsKey(c.getValue())) {
                valueCount = valueMap.get(c.getValue()) + 1; }
            else {
                valueCount = 1;
            }
            valueMap.put(c.getValue(), valueCount);
        }
        int straightCounter = 0;
        for (Value v : Value.values()) {
            if (valueMap.containsKey(v) && valueMap.get(v) >= 1) {
                for (Value vNext : Value.values()) {
                    if (Value.getIntValue(vNext) > Value.getIntValue(v)) { //if v = 4, don't start looking until vNext = 5
                        if (straightCounter == 5) {
                            return true;
                        }
                        if (!valueMap.containsKey(vNext)) {
                            straightCounter = 0;
                            break;
                        }
                        else if (valueMap.containsKey(vNext) && valueMap.get(vNext) >= 1) {
                            straightCounter++;
                        }

                    }
                }

            }
        }
        //check for wheel, since ACE can be high or low
        if (valueMap.containsKey(Value.ACE) && valueMap.get(Value.ACE) == 1 &&
                valueMap.containsKey(Value.TWO) && valueMap.get(Value.TWO) == 1 &&
                valueMap.containsKey(Value.THREE) && valueMap.get(Value.THREE) == 1 &&
                valueMap.containsKey(Value.FOUR) && valueMap.get(Value.FOUR) == 1 &&
                valueMap.containsKey(Value.FIVE) && valueMap.get(Value.FIVE) == 1) {return true;}
        return false;
    }

    // TODO - requires implementation if there are 2 cards that make a straight, but only 1 makes a flush
    // TODO - requires implementation if there are multiple numbers that make a straight (2,3,4,5,6, and the board is 2,2,3,4,5,6,K)
    /**
     * Given a list of 7 cards, determine if they can create a straight flush
     * @param cards The list of 7 cards
     * @return True if a straight flush is possible, false if not
     */
    private boolean checkStraightFlush(ArrayList<Card> cards) {
        ArrayList<Card> straightCards = new ArrayList<>();
        if (!checkStraight(cards)) {return false;}
        cards.sort(new SortByValueDescending()); //sort the list by value
        //Determine the straight values
        Map<Value, Integer> valueMap = new HashMap<>();
        Integer valueCount;
        for (Card c : cards) {
            if (valueMap.containsKey(c.getValue())) {
                valueCount = valueMap.get(c.getValue()) + 1; }
            else {
                valueCount = 1;
            }
            valueMap.put(c.getValue(), valueCount);
        }
        int straightCounter = 0;
        Value straightStart = Value.KING;
        for (Value v : Value.values()) {
            if (valueMap.containsKey(v) && valueMap.get(v) == 1) {
                for (Value vNext : Value.values()) {
                    if (Value.getIntValue(vNext) > Value.getIntValue(v)) { //if v = 4, don't start looking until vNext = 5
                        if (straightCounter == 5) {
                            straightStart = v;
                        }
                        if (valueMap.containsKey(vNext) && valueMap.get(vNext) == 0) {
                            break;
                        } else if (valueMap.containsKey(vNext) && valueMap.get(vNext) == 1) {
                            straightCounter++;
                        }

                    }
                }

            }
        }

        //build the array of cards that make the straight
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).getValue() == straightStart) {
                for (int j = 0; j < 4; j++) {
                    straightCards.add(cards.get(j));
                }
            }
        }


        //check for wheel, since ACE can be high or low
        //check for wheel, since ACE can be high or low
        if (valueMap.containsKey(Value.ACE) && valueMap.get(Value.ACE) == 1 &&
                valueMap.containsKey(Value.TWO) && valueMap.get(Value.TWO) == 1 &&
                valueMap.containsKey(Value.THREE) && valueMap.get(Value.THREE) == 1 &&
                valueMap.containsKey(Value.FOUR) && valueMap.get(Value.FOUR) == 1 &&
                valueMap.containsKey(Value.FIVE) && valueMap.get(Value.FIVE) == 1) {
            for (Card c : cards) {
                if (c.getValue() == Value.ACE || c.getValue() == Value.TWO || c.getValue() == Value.THREE || c.getValue() == Value.FOUR || c.getValue() == Value.FIVE) {
                    straightCards.add(c);
                }
            }
        }

        return checkFlush(straightCards);
    }

    /**
     * Given a list of 7 cards, determine if they can create a royal flush
     * @param cards The list of 7 cards
     * @return True if a royal flush is possible, false if not
     */
    private boolean checkRoyalFlush(ArrayList<Card> cards) {
        Map<Value, Integer> valueMap = mapCardsToValue(cards);
        if (!checkFlush(cards)) { return false; } // immediately return false if a flush is not possible
        ArrayList<Card> flushBestHand = flushBestHand(cards); // the best flush hand is a royal flush (always)
        if (flushBestHand.get(0).getValue() == Value.ACE && // these cards are only in the map's keys if they are in the list of cards
                flushBestHand.get(1).getValue() == Value.KING &&
                flushBestHand.get(2).getValue() == Value.QUEEN &&
                flushBestHand.get(3).getValue() == Value.JACK &&
                flushBestHand.get(4).getValue() == Value.TEN) {
            return true;
        }
        return false;
    }

    /**
     * Anonymous class, also comparator for sorting from low value to high
     */
    class SortByValueAscending implements Comparator<Card> {
        @Override
        public int compare(Card a, Card b) {
            return Value.getIntValue(a.getValue()) - Value.getIntValue(b.getValue());
        }
    }

    /**
     * Anonymous class, also comparator for sorting from high value to low
     */
    class SortByValueDescending implements Comparator<Card> {
        @Override
        public int compare(Card a, Card b) {
            return Value.getIntValue(b.getValue()) - Value.getIntValue(a.getValue());
        }
    }

}
