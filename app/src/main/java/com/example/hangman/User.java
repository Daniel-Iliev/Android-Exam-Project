package com.example.hangman;

public class User{
    private String name;
    private int wins;
    private int loses;

    public User(String name, int wins, int loses) {
        this.name = name;
        this.wins = wins;
        this.loses = loses;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLoses() {
        return loses;
    }

    public void setLoses(int loses) {
        this.loses = loses;
    }

    public User() {
    }
}
