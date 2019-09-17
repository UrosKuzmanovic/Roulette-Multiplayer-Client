package services;

import java.io.Serializable;

public class FieldInfo implements Serializable{

    private int number;
    private int bet;

    public FieldInfo(int number, int bet) {
        this.number = number;
        this.bet = bet;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public double getBet() {
        return bet;
    }

    public void setBet(int bet) {
        this.bet = bet;
    }

}