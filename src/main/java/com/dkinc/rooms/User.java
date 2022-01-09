package com.dkinc.rooms;

import java.util.Random;

public class User {

    private long id;
    private String name;
    private boolean isBank;
    private boolean canGetCard;



    private int score = 0;

    public User(String name, boolean isBank, boolean canGetCard){
        id = new Random().nextLong();
        this.name = name;
        this.isBank = isBank;
        this.canGetCard = canGetCard;

    }
    public User(String name){
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isCanGetCard() {
        return canGetCard;
    }

    public void setCanGetCard(boolean canGetCard) {
        this.canGetCard = canGetCard;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isBank() {
        return isBank;
    }

    public void setBank(boolean bank) {
        isBank = bank;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return name + " " + isBank + " " + score;
    }
}
