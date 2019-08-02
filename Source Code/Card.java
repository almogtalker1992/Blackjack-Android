package com.example.talker.blackjack;

public class Card{
    final static int CLUBS = 0, SPADES = 1, HEARTS = 2, DIAMONDS = 3;
    private final int value;
    private final int suit;

    Card(int value, int suit){
        this.value = value;
        this.suit = suit;
    }


    int getValue(){ return value; }

    char getSuitAsChar(){
        switch (suit){
            case CLUBS:    return 'c';
            case SPADES:   return 's';
            case HEARTS:   return 'h';
            case DIAMONDS: return 'd';
            default:       return '?';
        }
    }

    private String getValueAsString(){
        switch(value){
            case 1:   return "1";
            case 2:   return "2";
            case 3:   return "3";
            case 4:   return "4";
            case 5:   return "5";
            case 6:   return "6";
            case 7:   return "7";
            case 8:   return "8";
            case 9:   return "9";
            case 10:  return "10";
            case 11:  return "11";
            case 12:  return "12";
            case 13:  return "13";
            default:  return "??";
        }
    }

    public String toString(){ return getSuitAsChar() + getValueAsString(); }
}
