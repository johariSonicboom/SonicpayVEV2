package com.sonicboom.sonicpayvui.models;

public enum eResult {
    ACK ("ACK"),
    NAK ("NAK"),
    CAN ("CAN"),
    ESC ("ESC");

    private final String name;

    private eResult(String s) {
        name = s;
    }

    public String toString() {
        return this.name;
    }
}
