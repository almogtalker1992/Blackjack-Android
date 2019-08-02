package com.example.talker.blackjack;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private Deck[] deck;
    private int index, playerCurrentEmpty, dealerCurrentEmpty;
    private Hand playerHand, dealerHand, splitHand, weakestHand;
    private int bet = 0, money = 1000;
    private int last_bet = 0;
    private Card newCard, dealerSecondCard;
    private ImageView[] playerView = new ImageView[5];
    private ImageView[] dealerView = new ImageView[5];
    private SeekBar betSeekBar;
    private Button hitButton, standButton, splitButton, doubleButton, hitSplitButton, standSplitButton, dealButton;
    private TextView moneyTextView, dealerTextView, playerTextView, betTextView, splitTextView;
    private MediaPlayer shuffleSound, endGameSound;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeComponents();
        initializeGame();
        shuffleSound = MediaPlayer.create(getBaseContext(), R.raw.shuffling2);
        shuffleSound.start();
        endGameSound = MediaPlayer.create(getBaseContext(), R.raw.fail2);
        newGame();
        betSeekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.dealButton:
                if (bet == 0)
                    break;
                money -= bet;
                moneyTextView.setText(money + "");
                betSeekBar.setEnabled(false);
                betSeekBar.setVisibility(View.INVISIBLE);
                dealCards();
                checkForBlackjack();
                break;

            case R.id.hitButton:
                buttonChange(doubleButton, false, false);
                buttonChange(splitButton, false, false);
                if(playerHand.getCardCount() < 5)
                    hitPlayer(playerHand);
                else{
                    alertMessage("Can't hit more than 5 cards!");
                    hitButton.setEnabled(false);
                }
                if(playerHand.getBlackjackValue() > 21){
                    buttonChange(hitButton, false, false);
                    buttonChange(standButton, false, false);
                    if(splitHand.getCard(0) != null){
                        alertMessage("You lost the first hand (over 21), now you can play second hand!");
                        alertMessage("Please scroll down to see more options.");
                        if(deck[index].cardsLeft() == 0)
                            shuffle();
                        playerCurrentEmpty = 1;
                        splitHand.addCard(deck[index].dealCard());
                        printPlayerHand(splitHand);
                        playerCurrentEmpty++;
                        buttonChange(hitSplitButton, true, true);
                        buttonChange(standSplitButton, true, true);
                        splitTextView.setVisibility(View.VISIBLE);
                    } else {
                        dealerHand.addCard(dealerSecondCard);
                        printDealerHand();
                        checkWinner(playerHand);
                        newGame();
                    }
                }
                break;

            case R.id.standButton:
                buttonChange(hitButton, false, false);
                buttonChange(standButton, false, false);
                buttonChange(splitButton, false, false);
                buttonChange(doubleButton, false, false);

                if (splitHand.getCard(0) == null) {
                    while (dealerHand.getBlackjackValue() < 17 || playerHand.getBlackjackValue() > dealerHand.getBlackjackValue()) {
                        if (dealerHand.getCardCount() == 5)
                            break;
                        if (dealerHand.getCard(1) == null)
                            dealerHand.addCard(dealerSecondCard);
                        else
                            hitDealer();
                    }
                    checkWinner(playerHand);
                    newGame();
                } else {
                    if (deck[index].cardsLeft() == 0)
                        shuffle();
                    alertMessage("Now - choose what to do for the second hand.");
                    alertMessage("Please scroll down to see more options.");
                    splitHand.addCard(deck[index].dealCard());
                    printPlayerHand(splitHand);
                    playerCurrentEmpty = 2;
                    buttonChange(hitSplitButton, true, true);
                    buttonChange(standSplitButton, true, true);
                    splitTextView.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.splitButton:
                money -= bet;
                moneyTextView.setText(money + "");
                buttonChange(splitButton, false, false);
                buttonChange(doubleButton, false, false);
                splitHand.addCard(playerHand.removeCard());
                resetImagesView(playerView);
                playerCurrentEmpty = 0;
                Card temp = playerHand.getCard(0);
                showCard(temp.getSuitAsChar(), temp.getValue(), getWhichCard(playerHand));
                if (playerHand.getCard(0).getValue() == splitHand.getCard(0).getValue() && playerHand.getCard(0).getValue() == 1) {
                    hitPlayer(playerHand);
                    playerCurrentEmpty--;
                    hitPlayer(splitHand);
                    weakestHand = weakestHand(playerHand, splitHand);
                    while (dealerHand.getBlackjackValue() < 17 || weakestHand.getBlackjackValue() > dealerHand.getBlackjackValue()) {
                        if (dealerHand.getCardCount() == 5)
                            break;
                        if (dealerHand.getCard(1) == null)
                            dealerHand.addCard(dealerSecondCard);
                        else
                            hitDealer();
                    }
                    printDealerHand();
                    checkTwoHands();
                    newGame();
                } else
                    hitPlayer(playerHand);
                break;

            case R.id.doubleButton:
                hitPlayer(playerHand);
                money -= bet;
                moneyTextView.setText(money + "");
                bet *= 2;
                while (dealerHand.getBlackjackValue() < 17 || playerHand.getBlackjackValue() > dealerHand.getBlackjackValue()) {
                    if (dealerHand.getCardCount() == 5)
                        break;
                    hitDealer();
                }
                checkWinner(playerHand);
                newGame();
                break;

            case R.id.hitSplitButton:
                if (splitHand.getCardCount() < 5)
                    hitPlayer(splitHand);
                else {
                    alertMessage("Can't hit more than 5 cards!");
                    hitSplitButton.setEnabled(false);
                }
                weakestHand = weakestHand(playerHand, splitHand);
                if (splitHand.getBlackjackValue() > 21) {
                    while (dealerHand.getBlackjackValue() < 17 || weakestHand.getBlackjackValue() > dealerHand.getBlackjackValue()) {
                        if (dealerHand.getCardCount() == 5)
                            break;
                        if (dealerHand.getCard(1) == null)
                            dealerHand.addCard(dealerSecondCard);
                        else
                            hitDealer();
                    }
                    checkTwoHands();
                    newGame();
                }
                break;

            case R.id.standSplitButton:
                weakestHand = weakestHand(playerHand, splitHand);
                while (dealerHand.getBlackjackValue() < 17 || weakestHand.getBlackjackValue() > dealerHand.getBlackjackValue()) {
                    if (dealerHand.getCardCount() == 5)
                        break;
                    if (dealerHand.getCard(1) == null)
                        dealerHand.addCard(dealerSecondCard);
                    else
                        hitDealer();
                }
                printDealerHand();
                checkTwoHands();
                newGame();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_game:
                money = 1000;
                moneyTextView.setText(money + "");
                initializeGame();
                for (ImageView iv : playerView) {
                    iv.setImageResource(R.drawable.b1fv);
                    iv.setVisibility(View.VISIBLE);
                }
                for (ImageView iv : dealerView) {
                    iv.setImageResource(R.drawable.b1fv);
                    iv.setVisibility(View.VISIBLE);
                }
                shuffleSound.start();
                newGame();
                return true;

            case R.id.game_rules:
                Intent rules = new Intent(this, GameRules.class);
                startActivity(rules);
                return true;

            case R.id.about:
                Intent about = new Intent(this, About.class);
                startActivity(about);
                return true;
        }
        return false;
    }

    private void initializeComponents() {
        dealButton = findViewById(R.id.dealButton);
        dealButton.setOnClickListener(this);
        hitButton = findViewById(R.id.hitButton);
        hitButton.setOnClickListener(this);
        standButton = findViewById(R.id.standButton);
        standButton.setOnClickListener(this);
        splitButton = findViewById(R.id.splitButton);
        splitButton.setOnClickListener(this);
        doubleButton = findViewById(R.id.doubleButton);
        doubleButton.setOnClickListener(this);
        hitSplitButton = findViewById(R.id.hitSplitButton);
        hitSplitButton.setOnClickListener(this);
        standSplitButton = findViewById(R.id.standSplitButton);
        standSplitButton.setOnClickListener(this);
        betSeekBar = findViewById(R.id.betSeekBar);
        playerView[0] = findViewById(R.id.playerIV1);
        playerView[1] = findViewById(R.id.playerIV2);
        playerView[2] = findViewById(R.id.playerIV3);
        playerView[3] = findViewById(R.id.playerIV4);
        playerView[4] = findViewById(R.id.playerIV5);
        dealerView[0] = findViewById(R.id.dealerIV1);
        dealerView[1] = findViewById(R.id.dealerIV2);
        dealerView[2] = findViewById(R.id.dealerIV3);
        dealerView[3] = findViewById(R.id.dealerIV4);
        dealerView[4] = findViewById(R.id.dealerIV5);
        moneyTextView = findViewById(R.id.moneyTextView);
        dealerTextView = findViewById(R.id.dealerTextView);
        playerTextView = findViewById(R.id.playerTextView);
        splitTextView = findViewById(R.id.splitTextView);
        betTextView = findViewById(R.id.betTextView);
    }

    private void initializeGame() {
        deck = new Deck[6];
        for (index = 0; index < deck.length; index++) {
            deck[index] = new Deck();
            deck[index].shuffleDeck();
        }
        index = 0;
        playerHand = new Hand();
        dealerHand = new Hand();
        splitHand = new Hand();
    }

    private void newGame() {
        buttonChange(hitButton, false, false);
        buttonChange(standButton, false, false);
        buttonChange(doubleButton, false, false);
        buttonChange(splitButton, false, false);
        buttonChange(hitSplitButton, false, false);
        buttonChange(standSplitButton, false, false);
        splitTextView.setVisibility(View.INVISIBLE);
        buttonChange(dealButton, true, true);
        betSeekBar.setMax(money);
        if(bet > money)
            last_bet = money;
        else last_bet = bet;
        betSeekBar.setEnabled(true);
        betSeekBar.setVisibility(View.VISIBLE);
        betTextView.setText("Bet Value - $" + bet);
        playerCurrentEmpty = 0;
        dealerCurrentEmpty = 0;
        playerHand.clear();
        dealerHand.clear();
        splitHand.clear();
    }

    private void resetImagesView(ImageView[] imageViews) {
        for (ImageView imageView : imageViews)
            imageView.setVisibility(View.INVISIBLE);
    }

    private void dealCards() {
        buttonChange(dealButton, false, false);
        resetImagesView(playerView);
        resetImagesView(dealerView);
        if (deck == null || deck[deck.length - 1].cardsLeft() < 15) {
            for (index = 0; index < deck.length; index++) {
                deck[index] = new Deck();
                deck[index].shuffleDeck();
            }
            index = 0;
            alertMessage("Shuffling deck!");
        }
        if (checkDeck(playerHand, false))
            hitPlayer(playerHand);
        if (checkDeck(dealerHand, false))
            hitDealer();
        if (checkDeck(playerHand, false))
            hitPlayer(playerHand);
        if (checkDeck(dealerHand, true))
            hitDealerDown();
        if (playerHand.getCard(0).getValue() == playerHand.getCard(1).getValue() && bet * 2 <= money) {
            if (dealerHand.getBlackjackValue() < 21)
                buttonChange(splitButton, true, true);
        }
        if (bet * 2 <= money && !splitButton.isEnabled())
            buttonChange(doubleButton, true, true);
        buttonChange(hitButton, true, true);
        buttonChange(standButton, true, true);
    }

    private void checkForBlackjack() {
        if (playerHand.getBlackjackValue() == 21) {
            int dealerFCV = dealerHand.getCard(0).getValue();
            if ((dealerSecondCard.getValue() >= 10 && dealerFCV == 1) ||
                    (dealerSecondCard.getValue() == 1 && dealerFCV >= 10)) {
                alertMessage("It's a tie! Both players drew blackjack");
                money += bet;
            } else {
                int blackjackWin = bet * 2 + bet / 2;
                alertMessage("Blackjack! You Won $" + blackjackWin + "!");
                money += blackjackWin;
            }
            dealerHand.addCard(dealerSecondCard);
            printDealerHand();
            moneyTextView.setText(money + "");
            moneyCheck(playerHand);
            newGame();
        }
    }

    private void printPlayerHand(Hand hand) {
        resetImagesView(playerView);
        for (int i = 0; i < hand.getCardCount(); i++) {
            Card temp = hand.getCard(i);
            showCard(temp.getSuitAsChar(), temp.getValue(), playerView[i]);
        }
        playerTextView.setText("Player: (Score - " + hand.getBlackjackValue() + "):");
    }

    private void hitPlayer(Hand hand) {
        if (deck[index].cardsLeft() == 0)
            shuffle();
        newCard = deck[index].dealCard();
        hand.addCard(newCard);
        showCard(newCard.getSuitAsChar(), newCard.getValue(), getWhichCard(playerHand));
        playerTextView.setText("Player: (Score - " + hand.getBlackjackValue() + "):");
    }

    private void printDealerHand() {
        resetImagesView(dealerView);
        for (int i = 0; i < dealerHand.getCardCount(); i++) {
            Card temp = dealerHand.getCard(i);
            showCard(temp.getSuitAsChar(), temp.getValue(), dealerView[i]);
        }
        dealerTextView.setText("Dealer: (Score - " + dealerHand.getBlackjackValue() + "):");
    }

    private void hitDealerDown() {
        if (deck[index].cardsLeft() == 0)
            shuffle();
        dealerSecondCard = deck[index].dealCard();
        dealerView[1].setImageResource(R.drawable.b1fv);
        dealerView[1].setVisibility(View.VISIBLE);
        imageAnimation(dealerView[1]);
    }

    private void hitDealer() {
        if (deck[index].cardsLeft() == 0)
            shuffle();
        newCard = deck[index].dealCard();
        dealerHand.addCard(newCard);
        showCard(newCard.getSuitAsChar(), newCard.getValue(), getWhichCard(dealerHand));
        dealerTextView.setText("Dealer: (Score - " + dealerHand.getBlackjackValue() + "):");
    }

    private Hand weakestHand(Hand playerHand, Hand splitHand) {
        int firstHandValue = playerHand.getBlackjackValue();
        int secondHandValue = splitHand.getBlackjackValue();
        if (firstHandValue > 21 || secondHandValue > 21) {
            if (firstHandValue > 21)
                return splitHand;
            else
                return playerHand;
        }
        if (firstHandValue > secondHandValue)
            return splitHand;
        return playerHand;
    }

    private void checkWinner(Hand hand) {
        if (hand == playerHand && splitHand.getCard(0) == null) {
            printDealerHand();
            playerTextView.setText("Player (Score - " + playerHand.getBlackjackValue() + "):");
        }
        if (hand == splitHand) {
            printDealerHand();
            playerTextView.setText("Player: (Score - " + splitHand.getBlackjackValue() + "):");
        }
        if (hand.getBlackjackValue() > 21) {
            alertMessage("You Lost! (Over 21)");
        } else if (dealerHand.getBlackjackValue() > 21) {
            alertMessage("You won $" + bet + "!");
            money += bet * 2;
        } else if (dealerHand.getBlackjackValue() < hand.getBlackjackValue()) {
            alertMessage("You Won $" + bet + "!");
            money += bet * 2;
        } else if (dealerHand.getBlackjackValue() > hand.getBlackjackValue()) {
            alertMessage("Dealer Won!");
        } else if (dealerHand.getCardCount() == 2 && hand.getCardCount() > 2 && dealerHand.getBlackjackValue() == 21) {
            alertMessage("Dealer Won! BLACKJACK!");
        } else {
            alertMessage("It's a tie!");
            money += bet;
        }
        moneyCheck(playerHand);
    }

    private void moneyCheck(Hand hand) {
        moneyTextView.setText(money + "");
        if ((money == 0.0 && splitHand.getCard(0) == null) || (money == 0.0 && hand == splitHand)) {
            newGame();
            betSeekBar.setEnabled(false);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("You're out of money. Click OK to start a new game, and Cancel to stop!");
            endGameSound.start();
            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    money = 1000;
                    moneyTextView.setText(money + "");
                    newGame();
                }
            });
            alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    alertMessage("See you next time ;)");
                }
            });
            alertDialogBuilder.setCancelable(false);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    private ImageView getWhichCard(Hand hand) {
        if (hand == dealerHand) {
            dealerCurrentEmpty++;
            return dealerView[dealerCurrentEmpty - 1];
        } else {
            playerCurrentEmpty++;
            return playerView[playerCurrentEmpty - 1];
        }
    }

    private void showCard(char cardSuit, int cardNumber, ImageView imageView) {
        imageView.setVisibility(View.VISIBLE);
        imageAnimation(imageView);
        switch (cardSuit) {
            case 'c':
                switch (cardNumber) {
                    case 1:
                        imageView.setImageResource(R.drawable.c1);
                        break;
                    case 2:
                        imageView.setImageResource(R.drawable.c2);
                        break;
                    case 3:
                        imageView.setImageResource(R.drawable.c3);
                        break;
                    case 4:
                        imageView.setImageResource(R.drawable.c4);
                        break;
                    case 5:
                        imageView.setImageResource(R.drawable.c5);
                        break;
                    case 6:
                        imageView.setImageResource(R.drawable.c6);
                        break;
                    case 7:
                        imageView.setImageResource(R.drawable.c7);
                        break;
                    case 8:
                        imageView.setImageResource(R.drawable.c8);
                        break;
                    case 9:
                        imageView.setImageResource(R.drawable.c9);
                        break;
                    case 10:
                        imageView.setImageResource(R.drawable.c10);
                        break;
                    case 11:
                        imageView.setImageResource(R.drawable.c11);
                        break;
                    case 12:
                        imageView.setImageResource(R.drawable.c12);
                        break;
                    case 13:
                        imageView.setImageResource(R.drawable.c13);
                        break;
                    default:
                }
                break;

            case 'd':
                switch (cardNumber) {
                    case 1:
                        imageView.setImageResource(R.drawable.d1);
                        break;
                    case 2:
                        imageView.setImageResource(R.drawable.d2);
                        break;
                    case 3:
                        imageView.setImageResource(R.drawable.d3);
                        break;
                    case 4:
                        imageView.setImageResource(R.drawable.d4);
                        break;
                    case 5:
                        imageView.setImageResource(R.drawable.d5);
                        break;
                    case 6:
                        imageView.setImageResource(R.drawable.d6);
                        break;
                    case 7:
                        imageView.setImageResource(R.drawable.d7);
                        break;
                    case 8:
                        imageView.setImageResource(R.drawable.d8);
                        break;
                    case 9:
                        imageView.setImageResource(R.drawable.d9);
                        break;
                    case 10:
                        imageView.setImageResource(R.drawable.d10);
                        break;
                    case 11:
                        imageView.setImageResource(R.drawable.d11);
                        break;
                    case 12:
                        imageView.setImageResource(R.drawable.d12);
                        break;
                    case 13:
                        imageView.setImageResource(R.drawable.d13);
                        break;
                    default:
                }
                break;

            case 'h':
                switch (cardNumber) {
                    case 1:
                        imageView.setImageResource(R.drawable.h1);
                        break;
                    case 2:
                        imageView.setImageResource(R.drawable.h2);
                        break;
                    case 3:
                        imageView.setImageResource(R.drawable.h3);
                        break;
                    case 4:
                        imageView.setImageResource(R.drawable.h4);
                        break;
                    case 5:
                        imageView.setImageResource(R.drawable.h5);
                        break;
                    case 6:
                        imageView.setImageResource(R.drawable.h6);
                        break;
                    case 7:
                        imageView.setImageResource(R.drawable.h7);
                        break;
                    case 8:
                        imageView.setImageResource(R.drawable.h8);
                        break;
                    case 9:
                        imageView.setImageResource(R.drawable.h9);
                        break;
                    case 10:
                        imageView.setImageResource(R.drawable.h10);
                        break;
                    case 11:
                        imageView.setImageResource(R.drawable.h11);
                        break;
                    case 12:
                        imageView.setImageResource(R.drawable.h12);
                        break;
                    case 13:
                        imageView.setImageResource(R.drawable.h13);
                        break;
                    default:
                }
                break;

            case 's':
                switch (cardNumber) {
                    case 1:
                        imageView.setImageResource(R.drawable.s1);
                        break;
                    case 2:
                        imageView.setImageResource(R.drawable.s2);
                        break;
                    case 3:
                        imageView.setImageResource(R.drawable.s3);
                        break;
                    case 4:
                        imageView.setImageResource(R.drawable.s4);
                        break;
                    case 5:
                        imageView.setImageResource(R.drawable.s5);
                        break;
                    case 6:
                        imageView.setImageResource(R.drawable.s6);
                        break;
                    case 7:
                        imageView.setImageResource(R.drawable.s7);
                        break;
                    case 8:
                        imageView.setImageResource(R.drawable.s8);
                        break;
                    case 9:
                        imageView.setImageResource(R.drawable.s9);
                        break;
                    case 10:
                        imageView.setImageResource(R.drawable.s10);
                        break;
                    case 11:
                        imageView.setImageResource(R.drawable.s11);
                        break;
                    case 12:
                        imageView.setImageResource(R.drawable.s12);
                        break;
                    case 13:
                        imageView.setImageResource(R.drawable.s13);
                        break;
                    default:
                }
                break;
            default:
        }
    }

    private void checkTwoHands() {
        buttonChange(hitSplitButton, false, false);
        buttonChange(standSplitButton, false, false);
        printDealerHand();
        resetImagesView(playerView);
        alertMessage("First hand results!");
        printPlayerHand(playerHand);
        checkWinner(playerHand);
        alertMessage("Second hand results!");
        printPlayerHand(splitHand);
        checkWinner(splitHand);
    }

    private boolean checkDeck(Hand hand, boolean isDealerDown) {
        if (deck[index].cardsLeft() > 0)
            return true;
        else {
            index++;
            if (hand == dealerHand && !isDealerDown)
                hitDealer();
            else if ((hand == dealerHand && isDealerDown))
                hitDealerDown();
            else
                hitPlayer(hand);
        }
        return false;
    }

    private void shuffle() {
        for (index = 0; index < deck.length; index++) {
            deck[index] = new Deck();
            deck[index].shuffleDeck();
        }
        index = 0;
        shuffleSound.start();
    }

    private void alertMessage(String alert) {
        Toast.makeText(getApplicationContext(), alert, Toast.LENGTH_SHORT).show();
    }

    private void buttonChange(Button button, boolean isOn, boolean isVisible) {
        if (isVisible)
            button.setVisibility(View.VISIBLE);
        else
            button.setVisibility(View.INVISIBLE);
        button.setEnabled(isOn);
    }

    private void imageAnimation(ImageView imageView) {
        Animation animationFadeIn;
        animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_top);
        imageView.startAnimation(animationFadeIn);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        betTextView.setText("Bet Value - $" + progress);
        bet = progress;
        if (bet > 0) {
            dealButton.setEnabled(true);
        } else
            dealButton.setEnabled(false);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
