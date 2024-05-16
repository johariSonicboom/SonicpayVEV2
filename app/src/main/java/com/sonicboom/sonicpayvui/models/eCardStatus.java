package com.sonicboom.sonicpayvui.models;

public enum eCardStatus {
    ValidCard ("00"),
    ExpiredCard ("01"),
    CardDetected("02"),
    Timeout("03"),
    InvalidCard("99");

    private final String name;

    private eCardStatus(String s) {
        name = s;
    }

    public String getValue() {
        return this.name;
    }
}
