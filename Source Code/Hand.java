package com.example.talker.blackjack;
import java.util.Vector;

class Hand{
    private Vector<Card> hand;
    private boolean ace;

    Hand(){ hand = new Vector<>(); }

    void clear(){ hand.removeAllElements(); }

    Card removeCard(){ return hand.remove(0); }

    void addCard(Card card){
        if(card != null)
            hand.addElement(card);
    }

    int getCardCount(){ return hand.size(); }

    Card getCard(int position) {
        if(position >= 0 && position < hand.size())
            return hand.elementAt(position);
        return null;
    }

    int getBlackjackValue(){
        int handValue = 0;
        int numberCards = getCardCount();

        setAce(false);
        Card card;
        int cardValue;
        for(int i = 0; i < numberCards; i++){
            card = getCard(i);
            cardValue = card.getValue();
            if (cardValue >= 10)
                cardValue = 10;
            if (cardValue == 1)
                setAce(true);
            handValue += cardValue;
        }
        if (ace && handValue + 10 <= 21)
            handValue += 10;
        return handValue;
    }

    private void setAce(boolean ace){ this.ace = ace; }
}
