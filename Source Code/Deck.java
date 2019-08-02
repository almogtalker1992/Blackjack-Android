package com.example.talker.blackjack;

/**
 * Created by Talker on 11/04/2015.
 **/
class Deck
{
    private Card [] deck;
    private int cardsUsed;

    Deck(){
        deck = new Card[52];
        int currentCard = 0;
        for (int suit = 0; suit <= 3; suit++){
            for (int value = 1; value <= 13; value++){
                deck[currentCard] = new Card(value,suit);
                currentCard++;
            }
        }
        cardsUsed = 0;
    }

    void shuffleDeck(){
        for ( int i = 51; i > 0; i-- ){
            int rand = (int)(Math.random()*(i+1));
            Card swap = deck[i];
            deck[i] = deck[rand];
            deck[rand] = swap;
        }
        cardsUsed = 0;
    }

    int cardsLeft(){
        return 52 - cardsUsed;
    }

    Card dealCard(){
        if (cardsUsed == 52){
            deck = new Card[52];
            shuffleDeck();
        }
        cardsUsed++;
        return deck[cardsUsed - 1];
    }
}

