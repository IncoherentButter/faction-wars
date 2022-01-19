package com.factionkingdom.factionwars;

public enum GameState {
    DORMANT("Dormant"),
    RECRUITING("Recruiting"),
    COUNTDOWN("Countdown"),
    LIVE("Live");

    private String display;

    GameState(String display){
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }
}
