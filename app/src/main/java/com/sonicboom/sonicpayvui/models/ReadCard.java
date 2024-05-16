package com.sonicboom.sonicpayvui.models;

public class ReadCard {
    public static class ReadCardRequest{
        public boolean EmvPAN = false;
    }

    public static class ReadCardResponse {
        public int CardType;
        public String CardNo;
        public String HashedPAN;
        public String CardStatus;
        public String CardUID;
    }
}