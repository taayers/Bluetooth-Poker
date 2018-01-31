package com.triadicsoftware.bluetoothpoker;

/**
 * Created by todd on 11/20/16.
 */

public class Card {
    private int rank, suit;

    private boolean faceUp = true;

    private static String[] suits = {"hearts", "spades", "diamonds", "clubs"};

    private static String[] ranks = {"ace", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "jack", "queen", "king"};

    public static String rankAsString(int __rank) {

        return ranks[__rank];

    }

    Card(int suit, int rank) {

        this.rank = rank;

        this.suit = suit;

    }

//    Card(int a, int b) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }

    public @Override
    String toString() {

        return ranks[rank] + " of " + suits[suit];

    }

    public boolean getFaceUp(){
        return faceUp;
    }

    public void setFaceUp(Boolean faceUp){
        this.faceUp = faceUp;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getRank() {

        return rank;

    }

    public void setSuit(int suit) {
        this.suit = suit;
    }

    public int getSuit() {

        return suit;

    }

    public String getSuitString(){
        return suits[suit];
    }

    public String getRankString(){
        return ranks[rank];
    }

    public void toggleFaceUp(){
        faceUp = !faceUp;
    }

    public int getIndexOfSuit(String suit){
        int index = 0;
        for (int i=0;i<suits.length;i++) {
            if (suits[i].equals(suit)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public int getIndexOfRank(String rank){
        int index = 0;
        for (int i=0;i<ranks.length;i++) {
            if (ranks[i].equals(rank)) {
                index = i;
                break;
            }
        }
        return index;
    }

}
